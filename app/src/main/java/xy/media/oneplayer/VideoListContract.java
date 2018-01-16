package xy.media.oneplayer;

import java.util.ArrayList;

import xy.media.oneplayer.data.model.VideoInfo;

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

    }
}
