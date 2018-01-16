package xy.media.oneplayer;

import java.util.ArrayList;

import xy.media.oneplayer.data.model.VideoModel;

/**
 * Created by Yangwenjie on 2018/1/16.
 */

public class VideoListDataHelper {
    public ArrayList<VideoModel> mList;

    private static VideoListDataHelper sInstance;

    public static VideoListDataHelper getInstance() {
        if (sInstance == null) {
            synchronized (VideoListDataHelper.class) {
                if ( sInstance == null) {
                    sInstance = new VideoListDataHelper();
                }
            }
        }
        return sInstance;
    }


    public VideoListDataHelper() {
    }

    public void  initData() {

    }
}
