package com.example.bouda04.testlogindrive;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

/**
 * Created by bouda04 on 13/3/2018.
 */

public class DriveItems extends ItemsFrag {
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int RC_SIGN_IN = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final String[] SCOPES = new String[]{ DriveScopes.DRIVE_READONLY };

    GoogleSignInClient mGoogleSignInClient;
    DriveClient mDriveClient;
    DriveResourceClient mDriveResourceClient;
    GoogleAccountCredential mCredential;

    public DriveItems() {
        super();
    }


    @Override
    protected void initFrag() {
        googleSignIn();
    }


    private void googleSignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (account == null){//not signed-in
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }else
            authenticateDriveWith(account);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case RC_SIGN_IN:
                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    // Signed in successfully, connect to Drive account
                    authenticateDriveWith(account);
                } catch (ApiException e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {

                        mCredential.setSelectedAccountName(accountName);
                        new CollectFiles(mCredential).execute();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    if (mCredential.getSelectedAccountName() == null)
                        chooseAccount();
                    else
                        new CollectFiles(mCredential).execute();
                }
                break;
        }
    }

    private void authenticateDriveWith(GoogleSignInAccount account){
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mCredential.setSelectedAccount(account.getAccount());
        if (mCredential.getSelectedAccountName() == null)
            chooseAccount();
        else
            new CollectFiles(mCredential).execute();
    }

   // @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (Build.VERSION.SDK_INT >= 23){
            if (getContext().checkSelfPermission(Manifest.permission.GET_ACCOUNTS)!= PackageManager.PERMISSION_GRANTED){
                //   if (EasyPermissions.hasPermissions(
                //            getActivity(), Manifest.permission.GET_ACCOUNTS)) {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            } else {
                // Request the GET_ACCOUNTS permission via a user dialog
                requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_PERMISSION_GET_ACCOUNTS);
            /*
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);*/
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        chooseAccount();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class CollectFiles extends AsyncTask<Void, Void, List<MyFile>> {
        private com.google.api.services.drive.Drive mDrive = null;
        private Exception mLastError = null;


        CollectFiles(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mDrive = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Drive API Android Quickstart")
                    .build();
        }

        @Override
        protected List<MyFile> doInBackground(Void... voids) {
            List<MyFile> myFiles = new ArrayList<MyFile>();
            FileList result = null;
            try {
                result = mDrive.files().list()
                        .setQ("mimeType='audio/mp3' and 'me' in owners")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<File> files = result.getItems();
            if (files != null) {
                for (File file : files) {
                    myFiles.add(new MyFile(file.getTitle(),
                            "https://drive.google.com/uc?export=download&id=" + file.getId()));
                }
            }

            return myFiles;
        }

        @Override
        protected void onPostExecute(List<MyFile> output) {
            setListAdapter(new ArrayAdapter<MyFile>(context, android.R.layout.simple_list_item_1,output));
        }

    }



}
