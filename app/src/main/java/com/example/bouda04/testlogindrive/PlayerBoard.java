package com.example.bouda04.testlogindrive;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class PlayerBoard extends Activity implements ItemsFrag.ItemsListener{
    boolean local= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_board);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,
                        local? new LocalItems(): new DriveItems())
                .commit();
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            getFragmentManager().beginTransaction()
                .replace(R.id.pfragment_container,
                        new PlayerFragment(), "player")
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        PlayerFragment pfrag = (PlayerFragment) getFragmentManager().findFragmentByTag("player");
        if (pfrag!=null)
            getFragmentManager().beginTransaction().remove(pfrag).commit();
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onNewItemSelected(MyFile item) {
        PlayerFragment pfrag = (PlayerFragment) getFragmentManager().findFragmentByTag("player");
        if (pfrag == null)
        {//portrait mode

            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.setCustomAnimations(
                    android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
            pfrag = new PlayerFragment();
            ft.add(R.id.fragment_container, pfrag, "player");
            ft.addToBackStack("details");
            ft.commit();
            fm.executePendingTransactions();
        }
        pfrag.playFile(item.getPath());
    }
}
