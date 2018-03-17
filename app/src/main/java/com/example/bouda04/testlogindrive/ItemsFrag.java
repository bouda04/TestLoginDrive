package com.example.bouda04.testlogindrive;

import android.app.ListFragment;
import android.content.Context;
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
