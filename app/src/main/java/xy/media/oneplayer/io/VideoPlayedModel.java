package xy.media.oneplayer.io;

import java.io.Serializable;

public class VideoPlayedModel implements Serializable {

    public static final int TYPE_OF_PLAYED_VIDEO_LOCAL = 0;
    public static final int TYPE_OF_PLAYED_VIDEO_ONLINE = 1;

    private int videoType = TYPE_OF_PLAYED_VIDEO_ONLINE;
    private long fileId;
    private float downloadPercent;
    private String filePath;

    public VideoPlayedModel(long fileId) {
        this.fileId = fileId;
    }

    public VideoPlayedModel(long fileId, String filePath) {
        this.filePath = filePath;
        this.fileId = fileId;
    }

    public void setType(int type) {
        videoType = type;
    }

    public void setDownloadPercent(float downloadPercent) {
        this.downloadPercent = downloadPercent;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isLocal() {
        return videoType == TYPE_OF_PLAYED_VIDEO_LOCAL;
    }

    public boolean isOnline(){
        return videoType == TYPE_OF_PLAYED_VIDEO_ONLINE;
    }

    public int getVideoType() {
        return videoType;
    }

    public long getFileId() {
        return fileId;
    }

    public String getFilePath() {
        return filePath;
    }

    public float getDownloadPercent() {
        return downloadPercent;
    }
}
