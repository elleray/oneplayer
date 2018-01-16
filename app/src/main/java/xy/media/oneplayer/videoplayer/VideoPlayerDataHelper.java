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

import me.godap.application.GDApplication;
import me.godap.channel.model.FileModel;
import me.godap.channel.model.ShareModel;
import me.godap.discover.business.account.UserProxy;
import me.godap.filecenter.io.OpenVideoManager;
import me.godap.filecenter.io.VideoPlayedModel;
import me.godap.greendao.Feed;
import me.godap.lib.mod.file.db.VideoPlayRecordDBManager;
import me.godap.lib.mod.file.greendao.VideoPlayRecord;
import me.godap.lib.mod.file.selectmode.CurrentSelectHelper;
import me.godap.lib.mod.file.selectmode.SelectFileHelper;
import me.godap.lib.mod.file.utils.FileModuleSharePreferenceUtil;
import me.godap.lib.mod.file.utils.FileUtil;
import me.godap.lib.pub.log.GLog;
import me.godap.lib.utils.TextUtil;
import me.godap.network.utils.BaseWebParams;

import static me.godap.channel.model.FeedModel.FEED_TYPE_LIKE;

/**
 * Created by tony on 2017/12/18.
 */

public class VideoPlayerDataHelper {
    private static VideoPlayerDataHelper sInstance;

    public static final int PLAY_NEXT_VIDEO_DELAY = 5 * 1000;

    private SelectFileHelper mSelectFileHelper;

    private boolean mIsMute = false;

    private VideoPlayRecord mRecord;
    private int mRecentVolume = 0;
    private int mCurrentVideoDuration  = -1;

    private int mOpenType = OpenVideoManager.OPEN_TYPE_DEFAULT_MODE;
    private VideoPlayedModel mVideo = null;
    private ArrayList<VideoPlayedModel> mVideoList = null;
    private ArrayList<FileModel> mFileModelList = null;
    private boolean mIsMineShare;
    private ShareModel mShareModel = null;



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
        mSelectFileHelper = CurrentSelectHelper.getCurrent();
        if (mSelectFileHelper == null) {
            mSelectFileHelper = SelectFileHelper.getInstance();
        }

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

    public FileModel findFileModelByFileId(VideoPlayedModel video, List<FileModel> list){
        if (video != null && list != null) {
            for (FileModel model : list) {
                if (video.getFileId() != 0){
                    if (video.getFileId() == model.getFileId()){
                        return model;
                    }
                } else {
                    if (! TextUtil.isNull(video.getFilePath()) &&
                            ! TextUtil.isNull(model.getFilePath())
                            && video.getFilePath().equals(model.getFilePath())) {
                        return model;
                    }
                }
            }
        }
        return null;
    }


    public boolean containMineLike(FileModel fileModel){
        if (fileModel != null) {
            List<Feed> feeds = fileModel.getFeed();
            if (feeds != null) {
                long mineUserId = UserProxy.getInstance().getUserId();
                for (Feed feed : feeds) {
                    if (feed.getFeedType() == FEED_TYPE_LIKE
                        && mineUserId == feed.getSenderUserId()) {
                        return true;
                    }
                }
            }
        }
        return false;
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
                FileModel fileModel = findFileModelByFileId(mVideo, mFileModelList);
                if (fileModel != null){
                    return fileModel.getFileName();
                }
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

    public SelectFileHelper getSelectFileHelper() {
        return mSelectFileHelper;
    }

    public boolean isInSelectMode(){
        return mSelectFileHelper != null && mSelectFileHelper.isInSelectMode();
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
            FileModuleSharePreferenceUtil.setRecentPlayingVideo(context, currentPath);
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

    public long getShareId() {
        if (mShareModel != null) {
            return mShareModel.getShareId();
        }
        return 0;
    }

    public boolean isShareFile(){
        return mShareModel != null;
    }

    public void setFileModelList(ArrayList<FileModel> list) {
        this.mFileModelList = new ArrayList<>();
        if (list != null && ! list.isEmpty()) {
            mFileModelList.addAll(list);
        }
    }

    public ArrayList<FileModel> getFileModelList() {
        return mFileModelList;
    }

    public int getOpenType() {
        return mOpenType;
    }

    public int getLikeCount() {
        FileModel fileModel = getCurrentFileModel();
        if (fileModel != null && fileModel.getFeed() != null) {
            HashSet<Long> senderIds = new HashSet<>();
            for (Feed feed : fileModel.getFeed()) {
                senderIds.add(feed.getSenderUserId());
            }
            return senderIds.size();
        }
        return 0;
    }

    public FileModel getCurrentFileModel(){
        if (mFileModelList != null) {
            int pos = getCurrentVideoPosition();
            if (pos >= 0 && pos < mFileModelList.size()) {
                return mFileModelList.get(pos);
            }
        }
        return null;
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


    public boolean isMineShare() {
        FileModel fileModel = getCurrentFileModel();
        boolean isSameDeviceId = true;
        if (fileModel != null) {
            String localDeviceId = BaseWebParams.getDeviceId(GDApplication.getContext());
            isSameDeviceId =  fileModel.getDeviceId().equals(localDeviceId);
        }
        return mIsMineShare && isSameDeviceId;
    }

    public int getShareCommentCount() {
        if(mShareModel != null && mShareModel.getFeeds() != null) {
            return mShareModel.getFeeds().size();
        }
        return 0;
    }

    public void setShareModel(ShareModel shareModel) {
        this.mShareModel = shareModel;

        //更新shareModel时，要同时更新FileModelList
        if (shareModel != null && shareModel.getFileList() != null) {
            refreshFileModelList(shareModel, mFileModelList);
        }
    }

    public void refreshShareModel(ShareModel shareModel) {
        if(mShareModel != null){
            mShareModel.setFeeds(shareModel.getFeeds());
            mShareModel.setFileList(shareModel.getFileList());
        }
        mShareModel = shareModel;

        //更新shareModel时，要同时更新FileModelList
        if (shareModel != null && shareModel.getFileList() != null) {
            refreshFileModelList(shareModel, mFileModelList);
        }
    }


    private void refreshFileModelList(ShareModel shareModel, ArrayList<FileModel> fileModels) {
        if (shareModel != null && fileModels != null) {
            List<FileModel> newList = shareModel.getFileList();
            HashMap<Long, FileModel> tmp = new HashMap<>();
            if (newList != null) {
                for (FileModel model : newList) {
                    tmp.put(model.getFileId(), model);
                }

                int index = 0;
                for (FileModel model : fileModels) {
                    if(tmp.containsKey(model.getFileId())) {
                        fileModels.set(index, tmp.get(model.getFileId()));
                    }
                    index ++;
                }
            }
        }
    }
}
