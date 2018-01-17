package xy.media.oneplayer.io;

import org.greenrobot.eventbus.EventBus;

import xy.media.oneplayer.data.model.DownloadVideoUpdateEvent;


/**
 * Created by elleray on 2017/3/22.
 */

public class VideoPlayerUpdateHelper {

    /**
     * 更新视频下载的视频的进度
     * @param path
     * @param percent
     */
    public static void updateVideoDownloadProgress(String path, float percent) {
        EventBus.getDefault().post(new DownloadVideoUpdateEvent(path, percent));
    }

    /**
     * 更新视频下载的视频的进度
     * @param path
     * @param percent
     */
    public static void updateVideoDownloadProgress(String path, float percent, float downloadSpeed) {
        EventBus.getDefault().post(new DownloadVideoUpdateEvent(path, percent, downloadSpeed));
    }
}
