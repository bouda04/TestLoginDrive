package com.example.bouda04.testlogindrive;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import com.example.bouda04.testlogindrive.ItemsFrag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bouda04 on 15/3/2018.
 */

public class LocalItems extends ItemsFrag {

    @Override
    protected void initFrag() {
        new CollectFiles().execute();
    }

    private class CollectFiles extends AsyncTask<Void, Void, List<MyFile>> {
        CollectFiles() {
        }

        @Override
        protected List<MyFile> doInBackground(Void... voids) {
            List<MyFile> myFiles = new ArrayList<MyFile>();

            myFiles.add(new MyFile("local1", "path1"));
            myFiles.add(new MyFile("local2", "path2"));
            return myFiles;
        }

        @Override
        protected void onPostExecute(List<MyFile> output) {
            setListAdapter(new ArrayAdapter<MyFile>(context, android.R.layout.simple_list_item_1, output));
        }

    }
}
