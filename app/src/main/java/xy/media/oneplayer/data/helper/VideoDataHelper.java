package xy.media.oneplayer.data.helper;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import xy.media.oneplayer.data.VideoBaseDataHelper;
import xy.media.oneplayer.data.dbhelper.CenterFileDBManager;
import xy.media.oneplayer.data.dbhelper.VideoGroupEncryptDBManager;
import xy.media.oneplayer.data.dbhelper.VideoPositionDBmanager;
import xy.media.oneplayer.data.greendao.BaseFile;
import xy.media.oneplayer.data.greendao.CenterFile;
import xy.media.oneplayer.data.greendao.VideoPosition;
import xy.media.oneplayer.data.model.VideoInfo;
import xy.media.oneplayer.log.log.GLog;
import xy.media.oneplayer.util.CommonUtil;
import xy.media.oneplayer.util.FileUtil;
import xy.media.oneplayer.util.TextUtil;

/**
 * Created by elleray on 16/7/20.
 */
public class VideoDataHelper extends VideoBaseDataHelper {
    private static VideoDataHelper instance;

    private List<VideoInfo> mSubVideos = new ArrayList<VideoInfo>();
    private List<VideoInfo> mSingleVideoList;
    private ArrayList<VideoInfo> mLists = new ArrayList<VideoInfo>();
    private HashMap<String, Float> mDownloadingFilePercent = new HashMap<String, Float>();
    private HashMap<String, Float> mDownloadingSpeed = new HashMap<String, Float>();
    private HashSet<String> mDownloadingFiles = new HashSet<>();

    private ArrayList<VideoInfo> mMainShowVideos = null;
    private ArrayList<VideoInfo> mUnCompleteVideos = null;


    public VideoDataHelper(){
        super();
    }

    public void initData( ){
        super.initData();
    }

    public static VideoDataHelper getInstance(){
        if(instance == null){
            synchronized (VideoDataHelper.class){
                if(instance == null){
                    instance = new VideoDataHelper();
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
            if (videoInfoArrayList != null ) {
                Iterator<VideoInfo> it = videoInfoArrayList.iterator();
                while (it.hasNext()) {
                    VideoInfo videoInfo = it.next();
                }
            }
        } catch (Exception e) {
            GLog.e(CommonUtil.getCrashReport(e));
        }
    }

    public long getVideoDuration(String path){
        return VideoBaseDataHelper.getInstance().getVideoDuration(path);
    }

    public VideoInfo getDownloadingVideoInfoByPath(String path, int tab_name){
        return getVideoInfoByPath(path, tab_name);
    }


    /**
     * 获取GoDap视频
     * GoDap视频包括：通过下载模块获得的视频，通过传输获得的视频
     * @return
     */
    public ArrayList<VideoInfo> getGodapVideos(boolean needRefresh){
        if (mGodapVideos != null && !needRefresh) {
            GLog.i("no need refresh, godap videos : " + mGodapVideos.toString());
            return mGodapVideos;
        } else {
            GLog.d("loading godap videos.");

            ArrayList<VideoInfo> res = new ArrayList<VideoInfo>();

            List<CenterFile> list = CenterFileDBManager.getInstance().loadVideo(TAB_GODAP);
            if (list != null) {
                ArrayList<CenterFile> filesNeedDelete = new ArrayList<>();
                for (CenterFile file : list) {
                    if (filterWhiteList(file.getPath())) {
                        String path = file.getPath();
                        long duration = file.getDuration();
                        VideoInfo videoInfo = getVideoInfoByPath(path, duration, TAB_GODAP);
                        if (videoInfo != null) {
                            res.add(videoInfo);
                        } else {
                            filesNeedDelete.add(file);
                        }
                    }
                }
                setVideoPlayRecord(res);
                CenterFileDBManager.getInstance().delete(filesNeedDelete);
            }

            mGodapVideos = res;
            GLog.i("NEED refresh, godap videos : " + res.toString());
            return res;
        }
    }

    /**
     * 获得本地视频，默认不更新
     * @return
     */
    public ArrayList<VideoInfo> getLocalVideos(){
        return getLocalVideos(false);
    }


    /**
     * 获得本地视频
     * 本地视频：本Godap应用内产生，而是通过扫描磁盘获得的视频
     * @return
     */
    public ArrayList<VideoInfo> getLocalVideos(boolean needRefresh){
        if (mLocalVideos != null  && !needRefresh) {
            GLog.i("no need refresh.");
            return mLocalVideos;
        } else {
            GLog.d("loading local videos.");
            ArrayList<VideoInfo> res = new ArrayList<VideoInfo>();

            List<CenterFile> list = CenterFileDBManager.getInstance().loadVideo(LOCAL_VIDEO);
            if (list != null && ! list.isEmpty()) {
                ArrayList<CenterFile> filesNeedDelete = new ArrayList<>();
                for (CenterFile file : list) {
                    if (filterWhiteList(file.getPath())) {
                        String path = file.getPath();
                        long duration = file.getDuration();
                        VideoInfo videoInfo = getVideoInfoByPath(path, duration, LOCAL_VIDEO);
                        if (videoInfo != null) {
                            res.add(videoInfo);
                        } else {
                            GLog.e("delete video : " + file.getPath());
                            filesNeedDelete.add(file);
                        }
                    }
                }
                CenterFileDBManager.getInstance().delete(filesNeedDelete);
                setVideoPlayRecord(res);
            } else {
                GLog.d("scanning local content videos.");
                res = scanLocalContent();
                //存到视频数据库
                addToCenterFileDB(res);
                //设置各视频的播放记录
                setVideoPlayRecord(res);
            }

            GLog.d("Local video num = " + res.size());
            mLocalVideos = res;
            return res;
        }
    }

    public void scanLocalVideos(){
        GLog.d("scan local video");
        int recentPicNum = mLocalVideos == null ? 0 : mLocalVideos.size();
        ArrayList<VideoInfo> infos = scanLocalContent();


        if (infos.size() != recentPicNum) {
            GLog.d("has new video");
            addToCenterFileDB(infos);
            setVideoPlayRecord(infos);

            mLocalVideos = infos;
//            FileUpdateManager.sendBroadCastToFreshFileCenter(me.godap.lib.mod.file.io.Global.context, FileUtil.FILE_TYPE_VIDEO);
            GLog.d("scan video end");
        } else {
            GLog.d("have no new video file");
        }
    }

    public VideoInfo findLoadedVideoByPath(String path){
        if (mMainShowVideos != null) {
            for (VideoInfo  videoInfo : mMainShowVideos) {
                if (videoInfo.getPath().equals(path)) {
                    return videoInfo;
                }
            }
        }

       return super.findLoadedVideoByPath(path);
    }

    public void removeVideo(String path){
        if (TextUtil.isNull(path)) {
            return;
        }

        deleteBySamePath(mMainShowVideos, path);
        deleteFileBySamePath(mLists, path);
        deleteFileBySamePath(mSubVideos, path);
        deleteFileBySamePath(mSingleVideoList, path);

        if (mLists.isEmpty()) {
            VideoPositionDBmanager.getInstance().clear();
        }
        super.removeVideo(path);
    }

    private void deleteBySamePath(ArrayList<VideoInfo> infos, String path){
        if (! TextUtil.isNull(path) && infos != null) {
            for (VideoInfo videoInfo : infos) {
                if (path.equals(videoInfo.getPath())) {
                    infos.remove(videoInfo);
                    break;
                }
            }
        }
    }

    private void deleteFileBySamePath(List<VideoInfo> infos, String path){
        if (! TextUtil.isNull(path) && infos != null) {
            for (VideoInfo videoInfo : infos) {
                    if (path.equals(videoInfo.getPath())) {
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
        // 重命名文件
        try {
            File oldFile = new File(oldfile.getPath());
            String newFilePath = oldFile.getParent() + File.separator + newName;
            if (FileUtil.isExist(newFilePath)) {
                newFilePath = FileUtil.getNotSameName(newFilePath);
            }
            if (FileUtil.moveFile(oldFile, new File(newFilePath))) {
                CenterFileDBManager.getInstance().rename(oldfile.getPath(), newFilePath);
                return newFilePath;
            } else {
                GLog.e("rename failed");
            }
        } catch (Exception e) {
            GLog.e("rename Exception. " + CommonUtil.getCrashReport(e));
        }

        return "";
    }

    public void addNewVideo(ArrayList<String> paths){
        if (paths == null) {
            return;
        }

        HashSet<String> currentVideoPaths = new HashSet<>();
        if (mLocalVideos != null) {
            for (VideoInfo info : mLocalVideos){
                currentVideoPaths.add(info.getPath());
            }
        }

        for (String path : paths) {
            if (!  currentVideoPaths.contains(path)) {
                try {
                    VideoInfo info = getVideoInfoByPath( path, LOCAL_VIDEO);

                    if (info != null) {
                        GLog.d("ic_add new video to center file level, path  = " + path);
                        CenterFileDBManager.getInstance().saveCenterFile(
                                path,
                                info.getName(),
                                FileUtil.FILE_TYPE_VIDEO,
                                new Date(System.currentTimeMillis()),
                                info.getTotal_time(),
                                VideoDataHelper.getInstance().LOCAL_VIDEO,
                                "");
                    } else {
                        CenterFileDBManager.getInstance().delete(path);
                    }

                } catch (Exception e) {
                    GLog.e("ic_add new video error :  msg = " + e.toString());
                    GLog.e(CommonUtil.getCrashReport(e));
                    continue;
                }
            }
        }
    }

    /**
     * 获得视频首页要展示的视频
     * @return
     */
    public ArrayList<VideoInfo> getMainShowVideos(boolean needRefresh){
        return getMainShowVideos(getGodapVideos(needRefresh), getLocalVideos(needRefresh), needRefresh);
    }

    public ArrayList<VideoInfo> getmMainShowVideos() {
        return mMainShowVideos;
    }

    /**
     * 获得视频首页要展示的视频
     * @param godapVideos godap视频
     * @param localVideos 本地视频
     * @return
     */
    public ArrayList<VideoInfo> getMainShowVideos(ArrayList<VideoInfo> godapVideos, ArrayList<VideoInfo> localVideos, boolean needRefresh) {
        if (mMainShowVideos != null && ! needRefresh) {
            return mMainShowVideos;
        } else {
            GLog.d("loading main show videos.");
            mMainShowVideos  = merge(localVideos, godapVideos);
            return mMainShowVideos;
        }
    }

    private ArrayList<VideoInfo> merge(ArrayList<VideoInfo> list1 , ArrayList<VideoInfo> list2) {
        if  (list1 == null && list2 == null) {
            return new ArrayList<>();
        }
        if (list2 == null) {
           return list1;
        }
        if (list1 == null) {
           return list2;
        }


        HashSet<String> paths = new HashSet<>();

        ArrayList<VideoInfo> res = new ArrayList<>();
        for (VideoInfo videoInfo : list1) {
            if( !paths.contains(videoInfo.getPath())) {
                res.add(videoInfo);
                paths.add(videoInfo.getPath());
            }
        }
        for (VideoInfo videoInfo : list2) {
            if( !paths.contains(videoInfo.getPath())) {
                res.add(videoInfo);
                paths.add(videoInfo.getPath());
            }
        }

        return res;
    }

    public void setmUnCompleteVideos(ArrayList<VideoInfo> mUnCompleteVideos) {
        this.mUnCompleteVideos = mUnCompleteVideos;
    }




    public ArrayList<VideoInfo> getmLists() {
        return mLists;
    }

    public int sizeOfVideoAtPos(int pos){
        if (mLists == null || pos < 0 || pos >= mLists.size()) {
            return -1;
        }

        VideoInfo tmp = mLists.get(pos);
        return 1;
    }

    public void setmLists(ArrayList<VideoInfo> mLists) {
        this.mLists = mLists;
    }

    public HashMap<String, Float> getmDownloadingFilePercent(){
        return mDownloadingFilePercent;
    }

    public HashMap<String ,Float> getDownloadingSpeed(){
        return mDownloadingSpeed;
    }
    public void printListAll(String tag, ArrayList<VideoInfo> infos){
        if (infos == null) {
            GLog.d("info is null!");
            return;
        }
        GLog.d(tag + " size = "  + infos.size());
    }

    public void saveVideoPosition( List<VideoInfo> lists){
        if (lists == null || lists.isEmpty()) {
            return;
        }
        ArrayList<VideoPosition> newVideoPositions = new ArrayList<VideoPosition>();

        int position = 0;
        for (VideoInfo data : lists) {

//            data.setPosition(position);
//            newVideoPositions.add(new VideoPosition(data.getPath(), position, data.getTag(), data.getGroup_id()));

            position++;
        }

        //检查是否和原来的数量匹配
        VideoPositionDBmanager.getInstance().clear();
        VideoPositionDBmanager.getInstance().addAll(newVideoPositions);
    }


    public List<VideoInfo> getSubVideos() {
        return mSubVideos;
    }

    public void setSubVideos(List<VideoInfo> mSubVideos) {
        this.mSubVideos = mSubVideos;
    }


    public boolean containDownloadingFile(String path){
        return mDownloadingFiles.contains(path);
    }

    public void addDownloadingFile(String path){
        mDownloadingFiles.add(path);
    }

    public void clearDownloadingFile(String path){
        if(mDownloadingFiles.contains(path)){
            mDownloadingFiles.remove(path);
        }
    }
}
