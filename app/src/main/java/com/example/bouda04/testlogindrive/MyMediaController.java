package com.example.bouda04.testlogindrive;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.MediaController;

/**
 * Created by bouda04 on 13/3/2018.
 */

public class MyMediaController extends MediaController {


    public MyMediaController(Context context) {
        super(context);
    }

    public MyMediaController(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public MyMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            super.hide();
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                ((Activity)getContext()).finish();
            else
                ((Activity)getContext()).getFragmentManager().popBackStack();
        }

        return super.dispatchKeyEvent(event);
    }
    @Override
    public void hide() {
 //      super.hide();
    }
}
