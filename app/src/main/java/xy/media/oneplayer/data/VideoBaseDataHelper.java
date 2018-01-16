package xy.media.oneplayer.data;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import xy.media.oneplayer.data.dbhelper.CenterFileDBManager;
import xy.media.oneplayer.data.dbhelper.VideoPlayRecordDBManager;
import xy.media.oneplayer.data.greendao.BaseFile;
import xy.media.oneplayer.data.greendao.CenterFile;
import xy.media.oneplayer.data.greendao.VideoPlayRecord;
import xy.media.oneplayer.data.model.VideoInfo;
import xy.media.oneplayer.log.log.GLog;
import xy.media.oneplayer.manager.StorageManager;
import xy.media.oneplayer.util.CommonUtil;
import xy.media.oneplayer.util.FileUtil;
import xy.media.oneplayer.util.TextUtil;
import xy.media.oneplayer.util.VideoUtils;

/**
 * Created by elleray on 16/7/20.
 */
public class VideoBaseDataHelper {
    public static final int TAB_GODAP = 11;
    public static final int TAB_DOWNLOADING = 14;
    public final int LOCAL_VIDEO = 2;
    public final static String[] VIDEO_TYPE = new String[]{"video/mp4", "video/mkv"};
    private final String[] mWhiteListKeywordInPaths = {"dcim/camera", "godap/download", "godap/transfer", "getinsta", "video/screenrecorder"};

    private static VideoBaseDataHelper instance;

    protected ArrayList<VideoInfo> mGodapVideos = null;
    protected ArrayList<VideoInfo> mLocalVideos = null;
    private ArrayList<String> mHaveVideoRecordPaths = new ArrayList<String>();
    private HashMap<String, VideoPlayRecord> mHaveVideoRecords = new HashMap<String, VideoPlayRecord>();
    private MediaMetadataRetriever mmr;
    private HashMap<String, Long> mDurationMap = new HashMap<String ,Long>();
    private boolean isShowDownloadingVideo = false;


    public VideoBaseDataHelper(){
    }

    public void initData(){
//        loadVideoRecords();
        GLog.d("end init video data");
    }

    public static VideoBaseDataHelper getInstance(){
        if(instance == null){
            synchronized (VideoBaseDataHelper.class){
                if(instance == null){
                    instance = new VideoBaseDataHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 刷新videoInfo的选择状态
     * @param videoInfoArrayList
     */
    public void refreshVideosSelectState(ArrayList<VideoInfo> videoInfoArrayList){
        try {
            if(videoInfoArrayList != null){
                Iterator<VideoInfo> it = videoInfoArrayList.iterator();
                while (it.hasNext()){
                    VideoInfo videoInfo = it.next();
                }
            }
        }catch (Exception e){
            GLog.e(e.toString());
        }
    }

    public void setVideoPlayRecord(ArrayList<VideoInfo> videoInfoArrayList){
        try{
            for (VideoInfo videoInfo : videoInfoArrayList){
                setVideoPlayRecord(videoInfo);
            }
        }catch (Exception e){
            GLog.e(" set Video btn_music_play record error.");
            GLog.e(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 返回视频的时长，单位ms
     * @param path
     * @return
     */
    public long getVideoDuration(String path){
        File file = new File(path);
        if ( !file.exists() ) {
            return -1;
        }
        if (file.length() == 0) {
            return -1;
        }

        if(mDurationMap != null && mDurationMap.containsKey(path)){
            return mDurationMap.get(path);
        }

        try{
            if(mmr == null){
                mmr = new MediaMetadataRetriever();
            }
            FileInputStream inputStream = new FileInputStream(path);
            FileDescriptor fileDescriptor = inputStream.getFD();
            mmr.setDataSource(fileDescriptor, 0, inputStream.available());

            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if(TextUtil.isNull(duration)){
                duration = getDurationAsMediaPlayer(path) + "";
            }
            long numDuration = Integer.valueOf(duration);
            if(numDuration > 0){
                mDurationMap.put(path, numDuration);
            }

            return numDuration;
        } catch (Exception e){
            GLog.e("get video duration error : " + e.toString());
            e.printStackTrace();
        }
        return -1;
    }

    private long getDurationAsMediaPlayer(String path){
        long duration = 0;
        try {
            MediaPlayer mp = new MediaPlayer();
            FileInputStream stream = new FileInputStream(path);
            mp.setDataSource(stream.getFD());
            stream.close();
            mp.prepare();
            duration = mp.getDuration();
            mp.release();
        }catch (Exception e){
            GLog.e(e.toString());
        }

        return duration;
    }

    /**
     * 扫描本地视频文件，并存储到数据库中
     * 返回本地视频数据
     * @return
     */
    public ArrayList<VideoInfo> scanLocalContent(){
        HashMap<String, ArrayList<VideoInfo>> groupMaps = new HashMap<String, ArrayList<VideoInfo>>();

        ArrayList<VideoInfo> infos = new ArrayList<>();

        ArrayList<String> paths = FileBaseDataHelper.getInstance().searchLocalVideoFile();
        if(paths != null){
            for (String path:paths){
                if(filterWhiteList(path)){
                    File file = new File(path);
                    if(file.length() == 0){
                        continue;
                    }
                    long duration = getVideoDuration(path);
                    if(duration <= 0){
                        continue;
                    }
                    VideoInfo videoInfo = new VideoInfo();
                    videoInfo.setName(file.getName());
                    videoInfo.setSimple_name(FileUtil.getSimpleName(file.getName()));
                    videoInfo.setType(FileUtil.FILE_TYPE_VIDEO);
                    videoInfo.setPath(file.getPath());
                    videoInfo.setPlay_station(0);
                    videoInfo.setParentPath(file.getParent());
                    videoInfo.setParent_folder_name(file.getParentFile().getName());
                    videoInfo.setIsdirectory(false);
                    videoInfo.setSize(file.length() + "");
                    videoInfo.setTime(new Date(file.lastModified()));

                    videoInfo.setLeft_time(duration);
                    videoInfo.setTotal_time(duration);
                    videoInfo.setTime(new Date(file.lastModified()));
                    videoInfo.setTab_name(LOCAL_VIDEO);
                    infos.add(videoInfo);

                    //获取该图片的父路径名
                    String parentName = new File(path).getParentFile().getName();
                    //根据父路径名将图片放入到mGroupMap中
                    if (! groupMaps.containsKey(parentName)) {
                        ArrayList<VideoInfo> chileList = new ArrayList<VideoInfo>();
                        chileList.add(videoInfo);
                        groupMaps.put(parentName, chileList);
                    } else {
                        groupMaps.get(parentName).add(videoInfo);
                    }
                }
            }
        }

        ArrayList<VideoInfo> godapVideos = scanGodapDir();
        infos.addAll(godapVideos);

        return infos;
    }

    private ArrayList<VideoInfo> scanGodapDir(){
        GLog.d("scan godap dir video START");
        ArrayList<VideoInfo> videoInfos = new ArrayList<>();
        String godapDownloadDir = StorageManager.getInstance().getTransferVideoDirPath();
        String godapTransferDir = StorageManager.getInstance().getDownloadVideoDirPath();
        String[] dirs =  new String[]{godapDownloadDir, godapTransferDir};
        for (String dir : dirs) {
            File f = new File(dir);
            File[] files = f.listFiles();
            if (files == null || files.length == 0) {
                continue;
            }

            // 获取文件列表
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                VideoInfo video = getVideoInfoByPath(file.getPath(), TAB_GODAP);
                if (video != null) {
                    videoInfos.add(video);
                }
            }

        }

        GLog.d("scan godap dir video END ,size = " + videoInfos.size());
        return videoInfos;
    }

    public boolean filterWhiteList(String path){
        String lowPath = path.toLowerCase();
        for (String keyword : mWhiteListKeywordInPaths){
            if(lowPath.contains(keyword)){
                return true;
            }
        }
        return false;
    }

    public void addToCenterFileDB(ArrayList<VideoInfo> videoInfos){
        if(videoInfos == null){
            return;
        }

        HashSet<String> currentVideoPaths = new HashSet<>();
        if(mLocalVideos != null){
            for (VideoInfo info : mLocalVideos){
                currentVideoPaths.add(info.getPath());
            }
        }

        ArrayList<CenterFile> list =new ArrayList<>();
        for (VideoInfo video : videoInfos) {
            if(! currentVideoPaths.contains(video.getPath())){
                GLog.d("add new video to center file db, path  = " + video.getPath());
                CenterFile centerFile = CenterFileDBManager.getInstance().getNewCenterFileModel(
                        video.getPath(),
                        video.getName(),
                        video.getType(),
                        video.getTime(),
                        video.getTotal_time(),
                        video.getTab_id(),
                        FileUtil.isBigFile(video.getPath()) ? FileUtil.FILE_BIG_FILE : "",
                        video.getTab_id() != LOCAL_VIDEO);

                list.add(centerFile);
            }
        }
        CenterFileDBManager.getInstance().saveCenterFileList(list);
    }

    public ArrayList<VideoInfo> getmGodapVideos() {
        return mGodapVideos;
    }

    public ArrayList<VideoInfo> getmLocalVideos() {
        return mLocalVideos;
    }

    public VideoInfo findLoadedVideoByPath(String path){
        for (VideoInfo  videoInfo : mGodapVideos){
            if(videoInfo.getPath().equals(path)){
                return videoInfo;
            }
        }
        for (VideoInfo  videoInfo : mLocalVideos){
            if(videoInfo.getPath().equals(path)){
                return videoInfo;
            }
        }
        return  null;
    }

    public void removeVideo(String path){
        if(TextUtil.isNull(path)){
            return;
        }
        deleteVideoBySamePath(mGodapVideos, path);
        deleteVideoBySamePath(mLocalVideos, path);

        //delete video btn_music_play record
        if(VideoPlayRecordDBManager.getInstance().contain(path)){
            VideoPlayRecordDBManager.getInstance().delete(path);
        }

        if(CenterFileDBManager.getInstance().contain(path)){
            CenterFileDBManager.getInstance().delete(path);
        }
    }

    public ArrayList<String> loadVideoRecords(){
        List<VideoPlayRecord> list = VideoPlayRecordDBManager.getInstance().loadAll();

        mHaveVideoRecordPaths.clear();
        mHaveVideoRecords.clear();
        for (VideoPlayRecord record : list){
            mHaveVideoRecordPaths.add(record.getPath());
            mHaveVideoRecords.put(record.getPath(), record);
        }

        return mHaveVideoRecordPaths;
    }

    public void setVideoPlayRecord(VideoInfo videoInfo){
        try{
            long left_time = 0;
            long total_time = 0;
            if(! mHaveVideoRecords.containsKey(videoInfo.getPath())){
                if(videoInfo.getTotal_time() > 0){
                    left_time = videoInfo.getTotal_time();
                } else {
                    left_time = getVideoDuration(videoInfo.getPath());
                }
                total_time = left_time;
            } else {
                left_time = mHaveVideoRecords.get(videoInfo.getPath()).getLeft_time();
                total_time = mHaveVideoRecords.get(videoInfo.getPath()).getTotal_time();
            }

            videoInfo.setLeft_time(left_time);
            videoInfo.setTotal_time((long) total_time);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public VideoInfo getVideoInfoByPath(String path, int tab_name){
        return getVideoInfoByPath(path, getVideoDuration(path), tab_name);

    }

    public VideoInfo getVideoInfoByPath( String path, long duration, int tab_name) {
        File file = new File(path);
        if(file == null || ! file.exists() ||  file.length() == 0 || ! path.contains(".") || duration <= 0 || file.isDirectory()){
            return null;
        }
        VideoInfo videoInfo = new VideoInfo();

        videoInfo.setName(file.getName());
        videoInfo.setSimple_name(FileUtil.getSimpleName(file.getName()));
        videoInfo.setType(FileUtil.FILE_TYPE_VIDEO);
        videoInfo.setPath(file.getPath());
        videoInfo.setPlay_station(0);
        videoInfo.setParentPath(file.getParent());
        videoInfo.setParent_folder_name(file.getParentFile().getName());
        videoInfo.setIsdirectory(false);
        videoInfo.setSize(file.length() + "");
        videoInfo.setTime(new Date(file.lastModified()));
        videoInfo.setTab_name(tab_name);
        videoInfo.setDownload_percent(1f);
        videoInfo.setCopyright(VideoUtils.hasCopyright(duration));

        //时长信息
        videoInfo.setTotal_time(duration);
        if(mHaveVideoRecords.containsKey(videoInfo.getPath())) {
            videoInfo.setLeft_time(mHaveVideoRecords.get(videoInfo.getPath()).getLeft_time());
        } else {
            videoInfo.setLeft_time(duration);
        }

        return videoInfo;
    }

    private void deleteVideoBySamePath(ArrayList<VideoInfo> infos, String path){
        if(! TextUtil.isNull(path) && infos != null){
            for (VideoInfo videoInfo : infos){
                if(path.equals(videoInfo.getPath())){
                    infos.remove(videoInfo);
                    break;
                }
            }
        }
    }
    /**
     * 重命名视频文件
     * @param newName
     * @param oldfile
     */
    public String renameFileName(String newName, BaseFile oldfile) {
        try {
            File oldFile = new File(oldfile.getPath());
            String newFilePath = oldFile.getParent() + File.separator + newName;
            if(FileUtil.isExist(newFilePath)){
                newFilePath = FileUtil.getNotSameName(newFilePath);
            }
            if(FileUtil.moveFile(oldFile, new File(newFilePath))){
                CenterFileDBManager.getInstance().rename(oldfile.getPath(), newFilePath);
                return newFilePath;
            }else {
                GLog.e("rename failed");
            }
        } catch (Exception e) {
            GLog.e("rename Exception. " + CommonUtil.getCrashReport(e));
        }

        return "";
    }

    public boolean isShowDownloadingVideo() {
        return isShowDownloadingVideo;
    }
}
