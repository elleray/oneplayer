package xy.media.oneplayer.data.model;

/**
 * Created by Administrator on 2017/3/28.
 */

public class DownloadVideoUpdateEvent {
    public String mVideoPath;
    public float mPercent, mDownloadSpeed;

    public DownloadVideoUpdateEvent(String path, float percent) {
        this.mVideoPath = path;
        this.mPercent = percent;
    }

    public DownloadVideoUpdateEvent(String path, float percent, float downloadSpeed) {
        this.mVideoPath = path;
        this.mPercent = percent;
        this.mDownloadSpeed = downloadSpeed;
    }
}
