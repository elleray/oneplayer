package xy.media.oneplayer;

import android.app.Activity;

import java.util.ArrayList;

import xy.media.oneplayer.data.model.VideoInfo;
import xy.media.oneplayer.io.VideoPlayedModel;

/**
 * Created by Administrator on 2018/1/16.
 */

public class VideoListContract {

    interface View {
        void showProgress();
        void hideProgress();
        void refreshVideos(ArrayList<VideoInfo> mVideoInfos);
        void reLoad();

    }

    public interface Presenter {
        void init();
        void start();
        void loadData(boolean refresh, boolean isFirstLoad);
        void openVideo(Activity activity, VideoInfo model);

    }
}
