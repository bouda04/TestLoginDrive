package com.example.bouda04.testlogindrive;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class MainActivity extends Activity implements View.OnClickListener {
    private final int RC_SIGN_IN = 1;
    private final String TAG = "MainActivity";
    static final int REQUEST_AUTHORIZATION = 1001;
    GoogleSignInClient mGoogleSignInClient;
    DriveClient mDriveClient;
    DriveResourceClient mDriveResourceClient;
    private static final String[] SCOPES = { DriveScopes.DRIVE_READONLY };
    GoogleAccountCredential mCredential;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
// Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null){
            // Set the dimensions of the sign-in button.
            SignInButton signInButton = findViewById(R.id.sign_in_button);
            signInButton.setVisibility(View.VISIBLE);
            signInButton.setSize(SignInButton.SIZE_STANDARD);
            signInButton.setOnClickListener(this);
        }
        else{
            accessDrive2(account);
        }

    }


    private void accessDrive(GoogleSignInAccount account){
        Set<Scope> scopes =account.getGrantedScopes();

        mDriveClient = Drive.getDriveClient(this, account);
        // Build a drive resource client.
        mDriveResourceClient =
                Drive.getDriveResourceClient(this, account);

        Query query = new Query.Builder()
 //               .addFilter(Filters.eq(SearchableField.TITLE, "ex6-2017"))
                .build();
        Task<MetadataBuffer> queryTask = mDriveResourceClient.query(query);

        queryTask
                .addOnSuccessListener(this,
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                int cnt = metadataBuffer.getCount();

                                if (cnt>0){
                                    Metadata metadata = metadataBuffer.get(0);
                                    DriveId did =metadata.getDriveId();
                                }
                                metadataBuffer.release();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure...
                    }
                });
    }


    private void accessDrive2(GoogleSignInAccount account){
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                this, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());


        mCredential.setSelectedAccount(account.getAccount());
        new MakeRequestTask(mCredential).execute();
    }
    @Override
    public void onClick(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case RC_SIGN_IN:
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    new MakeRequestTask(mCredential).execute();
                }
                break;
        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            accessDrive2(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "handleSignInResult:signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }


    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.drive.Drive mDrive = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mDrive = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Drive API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> fileInfo = new ArrayList<String>();
            FileList result = mDrive.files().list()
                 //   .setPageSize(10)
                    .setQ("mimeType='audio/mp3'")
         //           .setFields("nextPageToken, files(id, name)")
                    .execute();

            List<File> files = result.getItems();
            if (files != null) {
                for (File file : files) {
                    String downloadUrl = file.getDownloadUrl();
                    String url2 = file.getDefaultOpenWithLink();
                    fileInfo.add(String.format("%s, %s, %s (%s)\n",
                            file.getEmbedLink(), file.getWebViewLink(), file.getWebContentLink(),file.getId()));
                }
            }

            java.io.File testFile = new java.io.File(getApplicationContext().getExternalFilesDir(null), "tempfile.mp3");
            FileOutputStream fos = new FileOutputStream (testFile);
            //OutputStream outputStream = new ByteArrayOutputStream();
            mDrive.files().get(files.get(0).getId())
                    .executeMediaAndDownloadTo(fos);
            fos.close();

            playFile(Uri.parse(testFile.getAbsolutePath()));
            return fileInfo;
        }


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(List<String> output) {

            if (output == null || output.size() == 0) {
                Log.w(TAG, "No results returned.");
            } else {
                output.add(0, "Data retrieved using the Drive API:");
                Log.w(TAG, TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {

            if (mLastError != null) {
                if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else
                    Log.w(TAG, "The following error occurred:\n"
                            + mLastError.getMessage());
            } else {
                Log.w(TAG, "Request cancelled.");
            }
        }
    }

    private void playFile(Uri uri){
        MediaPlayer mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
 //           String str = "https://drive.google.com/uc?id=1_uEMXQiJn6O54Qon82-6qdV4xmH0Pkot";
 //           String str2 = "https://doc-0c-2g-docs.googleusercontent.com/docs/securesc/g3991q79q9scnr4elrnc8247moeij6np/o3jme3ir8k55ai5ep5dnbo2qp3lg615v/1519898400000/11907581883552072574/11907581883552072574/1_uEMXQiJn6O54Qon82-6qdV4xmH0Pkot";
 //           uri = Uri.parse(str2);
            mPlayer.setDataSource(getApplicationContext(), uri);

            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
