package xy.media.oneplayer.videoplayer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import xy.media.oneplayer.data.dbhelper.VideoPlayRecordDBManager;
import xy.media.oneplayer.data.greendao.VideoPlayRecord;
import xy.media.oneplayer.io.OpenVideoManager;
import xy.media.oneplayer.io.VideoPlayedModel;
import xy.media.oneplayer.log.log.GLog;
import xy.media.oneplayer.manager.StorageManager;
import xy.media.oneplayer.player.subtitles.SubtitlesCoding;
import xy.media.oneplayer.player.subtitles.SubtitlesModel;
import xy.media.oneplayer.util.FileUtil;
import xy.media.oneplayer.util.SharePreferenceUtil;
import xy.media.oneplayer.util.TextUtil;

/**
 * Created by tony on 2017/12/18.
 */

public class VideoPlayerDataHelper {
    private static VideoPlayerDataHelper sInstance;

    public static final int PLAY_NEXT_VIDEO_DELAY = 5 * 1000;


    private boolean mIsMute = false;

    private VideoPlayRecord mRecord;
    private int mRecentVolume = 0;
    private int mCurrentVideoDuration  = -1;

    private int mOpenType = OpenVideoManager.OPEN_TYPE_DEFAULT_MODE;
    private VideoPlayedModel mVideo = null;
    private ArrayList<VideoPlayedModel> mVideoList = null;
    private boolean mIsMineShare;

    private ArrayList<SubtitlesModel> mSubtitleList = new ArrayList<>();



    public static VideoPlayerDataHelper getInstance(){
        if (sInstance == null) {
            synchronized (VideoPlayerDataHelper.class) {
                if (sInstance == null) {
                    sInstance = new VideoPlayerDataHelper();
                }
            }
        }

        return sInstance;
    }

    private VideoPlayerDataHelper(){
    }

    public void initData(Intent intent, Context context){
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            mVideoList = (ArrayList<VideoPlayedModel>) bundle.getSerializable("video_list");

            int index =  bundle.getInt("video_pos");
            if (index > -1 && mVideoList != null) {
                mVideo = mVideoList.get(index);
            }

            mOpenType = bundle.getInt("open_type");
            mIsMineShare = intent.getBooleanExtra("is_mine_share" ,false);

            try {
                if (mVideo != null && TextUtil.isNull(mVideo.getFilePath())) {
                    mRecord = VideoPlayRecordDBManager.getInstance().findVideoRecord(mVideo.getFilePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断是否正在播放一个播放列表，大于2个的视频才叫播放列表
     * @return
     */
    public boolean isPlayingVideolist() {
        if (mVideo != null && mVideoList != null) {
            if (mVideoList.size() > 1) {
                return true;
            }  else {
                return false;
            }
        }

        return false;
    }

    public int getCountOfPlayList(){
        if (mVideoList != null) {
           return mVideoList.size();
        }
        return -1;
    }

    private int indexOfList(VideoPlayedModel video, ArrayList<VideoPlayedModel> list){
        int index = -1;

        if (video != null && list != null) {
            for (VideoPlayedModel model : list){
                index ++;
                if (video.getFileId() != 0) {
                    if (video.getFileId() == model.getFileId()){
                        return index;
                    }
                } else {
                    if (! TextUtil.isNull(video.getFilePath()) &&
                            ! TextUtil.isNull(model.getFilePath())
                            && video.getFilePath().equals(model.getFilePath())) {
                        return index;
                    }
                }
            }
        }
        return index;
    }


    public VideoPlayedModel nextVideo(){
        if (mVideo != null && mVideoList != null) {
            int current = getCurrentVideoPosition();
            if (current >= 0 && current < getCountOfPlayList() - 1) {
                int nextIndex = current + 1;
                return mVideoList.get(nextIndex);
            }
        }
        return null;
    }

    public String getCurrentVideoName() {
        if (mVideo != null) {
            if (mVideo.isLocal()) {
                File file = new File(mVideo.getFilePath());
                String name = file.getName();
                return FileUtil.getSimpleName(name);
            } else {
            }
        }
        return "";
    }

    public String getCurrentPath() {
        if (mVideo != null) {
            return mVideo.getFilePath();
        }
        return "";
    }

    public boolean isDownloadingFile() {
        if (mVideo != null) {
            return mVideo.isOnline();
        }
        return false;
    }

    public boolean isMute() {
        return mIsMute;
    }

    public void setMute(boolean mIsMute) {
        this.mIsMute = mIsMute;
    }

    public void closeMute(Context context){
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mRecentVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        setMute(false);
    }

    public void openMute(Context context){
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mRecentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        setMute(true);
    }

    public void saveVideoRecord(long currentPlayerPos){
        VideoPlayRecord videoPlayRecord = new VideoPlayRecord();
        videoPlayRecord.setPath(getCurrentPath());
        videoPlayRecord.setPlay_time(currentPlayerPos);
        videoPlayRecord.setTotal_time((long) getCurrentVideoDuration());
        videoPlayRecord.setLeft_time((int)(getCurrentVideoDuration() - currentPlayerPos));

        VideoPlayRecordDBManager.getInstance().insert(videoPlayRecord);
    }

    public int getCurrentVideoDuration() {
        return mCurrentVideoDuration;
    }

    public void setCurrentVideoDuration(int mCurrentVideoDuration) {
        this.mCurrentVideoDuration = mCurrentVideoDuration;
    }

    public void saveRecentPlayingVideo(Context context){
        String currentPath = getCurrentPath();
        if (!TextUtil.isNull(currentPath)) {
            SharePreferenceUtil.setRecentPlayingVideo(context, currentPath);
        }
    }

    public int getRecordPos(){
        int position = 0;
        try {
            if (! TextUtil.isNull(getCurrentPath())) {
                mRecord = VideoPlayRecordDBManager.getInstance().findVideoRecord(getCurrentPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mRecord != null) {
            position = Integer.valueOf(mRecord.getPlay_time() + "");
        }

        if (isFinishWatch(position, mCurrentVideoDuration)) {
            GLog.d("finish watch this, replaying.");
            position = 0;
        }
        return  position;
    }

    public void clearPlayRecord(VideoPlayedModel model) {
        if(! TextUtil.isNull(model.getFilePath())) {
            VideoPlayRecordDBManager.getInstance().delete(model.getFilePath());
        }
    }

    /**
     * 判断播放是否结束
     * @param total 播放的视频总长度
     * @param position 播放到的位置
     * @return
     */
    private boolean isFinishWatch(long position, long total) {
        long left = total - position;

        GLog.d("video " + position + " + position + " + total + " + total");

        if (total < 5 * 60 * 1000) {
            if (position > 0.5 * total) {           //test
                return true;
            }
        } else if (total >= 5 * 60 * 1000 && total < 10 * 60 * 1000) {
            if (position > 0.8 * total) {
                return true;
            }
        } else if (total >= 10 * 60 * 1000 && total < 30 * 60 * 1000) {
            if (position > 0.9 * total) {
                return true;
            }
        } else if (total >= 30 * 60 * 1000 && total < 60 * 60 * 1000) {
            if (left < 3 * 60 * 1000) {
                return true;
            }
        } else {
            if (left < 5 * 60 * 1000) {
                return true;
            }
        }
        return false;
    }

    public VideoPlayedModel getVideo() {
        return mVideo;
    }

    public void setVideo(VideoPlayedModel mVideo) {
        this.mVideo = mVideo;
    }

    public ArrayList<VideoPlayedModel> getVideoList() {
        return mVideoList;
    }

    public float getDownloadingPercent() {
        return mVideo.getDownloadPercent();
    }



    public int getOpenType() {
        return mOpenType;
    }


    public int getCurrentVideoPosition() {
        if (mVideo != null && mVideoList != null) {
            int index = mVideoList.indexOf(mVideo);
            if (index >=0 && index < mVideoList.size()) {
                return index;
            } else {
                return indexOfList(mVideo, mVideoList);
            }
        }
        return -1;
    }

    public ArrayList<SubtitlesModel> readSubTitles(){
                String path = StorageManager.getInstance().getCurrentGodapRootPath();
                String subtitlepath = path + File.separator + "The.Nile.Hilton.Incident.2017.MULTi.1080p.BluRay.x264-LOST.简英.srt";
                SubtitlesCoding.readFile(subtitlepath);
                mSubtitleList = SubtitlesCoding.getSubtitles();
                return mSubtitleList;
    }
}
