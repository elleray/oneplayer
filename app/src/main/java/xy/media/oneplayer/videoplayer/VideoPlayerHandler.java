package xy.media.oneplayer.videoplayer;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import xy.media.oneplayer.io.VideoPlayedModel;


/**
 * Created by elleray on 2017/3/21.
 */

public class VideoPlayerHandler extends Handler {

    private Listener listener;
    private Activity activity;
    private VideoPlayedModel info;

    private int mWidth, mHeight;

    public static interface Listener{
        void onShare(VideoPlayedModel videoInfo, Activity activity, int width, int height);
        void onFailOpen(VideoPlayedModel videoInfo);
        void onRate(VideoPlayedModel videoInfo);
        void onFinish();
    }


    public VideoPlayerHandler(Activity activity, VideoPlayedModel videoInfo){
        super();
        this.activity = activity;
        this.info = videoInfo;
    }

    @Override
    public void handleMessage(Message msg) {
    }

    public void setmWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public void setmHeight(int mHeight) {
        this.mHeight = mHeight;
    }
}
