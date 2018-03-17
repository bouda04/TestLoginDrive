package com.example.bouda04.testlogindrive;

import android.app.Fragment;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

/**
 * Created by bouda04 on 13/3/2018.
 */

public class PlayerFragment extends Fragment {
    static MediaPlayer mPlayer = new MediaPlayer();
    MyMediaController mc=null;
    View myFrag=null;
    public PlayerFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_player_control, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.myFrag = view;


        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        //   MediaController mc = view.findViewById(R.id.mediaController);
        mc = new MyMediaController(getActivity());

        mc.setMediaPlayer(new Controller());
        // the delayed operation is done due to a bug in Android in landscape mode
        mc.postDelayed(new Runnable() {
            @Override
            public void run() {
                View v = myFrag.findViewById(R.id.playerContainer);
                mc.setAnchorView(v);
                mc.setEnabled(true);
                mc.show();
            }
        },1000);

        super.onActivityCreated(savedInstanceState);
    }


    public void playFile(String path){
        if (mPlayer.isPlaying())
            mPlayer.stop();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.reset();
            mPlayer.setDataSource(path);
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    mediaPlayer.start();
                    mc.show();
                }
            });
            mPlayer.prepareAsync(); // prepare async to not block main thread
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private class Controller implements MediaController.MediaPlayerControl {

        @Override
        public void start() {
            mPlayer.start();
        }


        @Override
        public void pause() {
            mPlayer.pause();
        }

        @Override
        public int getDuration() {
            return mPlayer.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            return mPlayer.getCurrentPosition();
        }

        @Override
        public void seekTo(int i) {
            mPlayer.seekTo(i);
        }

        @Override
        public boolean isPlaying() {
            return mPlayer.isPlaying();
        }

        @Override
        public int getBufferPercentage() {
            if (mPlayer.isPlaying())
                return(mPlayer.getCurrentPosition() * 100) / mPlayer.getDuration();
            else
                return 0;
        }

        @Override
        public boolean canPause() {
            return true;
        }

        @Override
        public boolean canSeekBackward() {
            return true;
        }

        @Override
        public boolean canSeekForward() {
            return true;
        }

        @Override
        public int getAudioSessionId() {
            return mPlayer.getAudioSessionId();
        }
    }
}
