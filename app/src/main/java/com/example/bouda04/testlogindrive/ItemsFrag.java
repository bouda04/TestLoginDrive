package com.example.bouda04.testlogindrive;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by bouda04 on 15/3/2018.
 */

public abstract class ItemsFrag extends ListFragment {
    protected ItemsListener listener = null;
    protected Context context;
    protected abstract void initFrag();

    @Override
    public void onAttach(Context context) {
        this.listener = (ItemsListener) context;
        this.context = context;
        initFrag();
        super.onAttach(context);
    }

    /*
 * Deprecated on API 23

 */
    @SuppressWarnings("deprecation")
    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            this.listener = (ItemsListener) activity;
            this.context = activity;
            initFrag();
            super.onAttach(activity);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listener.onNewItemSelected((MyFile) adapterView.getItemAtPosition(i));
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    public interface ItemsListener{
        public void onNewItemSelected(MyFile item);
    }

}
