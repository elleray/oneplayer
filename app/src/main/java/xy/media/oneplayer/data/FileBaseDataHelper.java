package xy.media.oneplayer.data;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import xy.media.oneplayer.data.dbhelper.CenterFileDBManager;
import xy.media.oneplayer.data.greendao.BaseFile;
import xy.media.oneplayer.data.greendao.CenterFile;
import xy.media.oneplayer.data.helper.AutoRefreshFileShelf;
import xy.media.oneplayer.data.model.DirectModel;
import xy.media.oneplayer.gl.Global;
import xy.media.oneplayer.log.log.GLog;
import xy.media.oneplayer.manager.StorageManager;
import xy.media.oneplayer.util.CommonUtil;
import xy.media.oneplayer.util.DeviceInfo;
import xy.media.oneplayer.util.FileUtil;
import xy.media.oneplayer.util.TextUtil;
import xy.media.oneplayer.util.VideoUtils;

/**
 * Created by elleray on 2017/3/20.
 */

public class FileBaseDataHelper {
    private static FileBaseDataHelper instance;

    private ArrayList<BaseFile> mDocFileList, mtxtFileList, mZipFileList, mBigFileList, mCopyFilesInFileList, mApkFileList,mGoDapTransferFileList, mGoDapUploadFileList;
    private ArrayList<BaseFile> mSearchResultList;
    private HashSet<String> mBigFilePathSet = new HashSet<String>();
    private String[] mDocSuffix = {".doc", ".docx",".xls", ".xlsx", ".pdf", ".ppt", ".pptx"};
    private String[] mTxtSuffix = {".txt", ".chm", ".ebk", ".umb"};
    private String[] mZipSuffix = {".zip", ".rar", ".7z", ".iso"};
    private String[] mVideoSuffix = {".mp4", ".avi", "rmvb", "mkv"};
    private String[] mApkSuffix = {".apk"};
    private HashMap<String, Integer> mDocSuffixType = new HashMap<String, Integer>();
    protected HashSet<String> mHasRefreshContainer = new HashSet<>();
    private AutoRefreshFileShelf mAutoRefreshBookShelf;
    private boolean mIsSearchingBigFile;

    public static FileBaseDataHelper getInstance(){
        if(instance == null){
            synchronized (FileBaseDataHelper.class){
                if(instance == null){
                    instance = new FileBaseDataHelper();
                }
            }
        }
        return instance;
    }

    public FileBaseDataHelper(){
        init();
    }


    public void init(){
        GLog.d("init.");

        mDocFileList = new ArrayList<BaseFile>();
        mtxtFileList = new ArrayList<BaseFile>();
        mZipFileList = new ArrayList<BaseFile>();
        mBigFileList = new ArrayList<BaseFile>();
        mCopyFilesInFileList = new ArrayList<>();
        mApkFileList = new ArrayList<>();
        mSearchResultList = new ArrayList<BaseFile>();
        mGoDapTransferFileList = new ArrayList<BaseFile>();
        mGoDapUploadFileList = new ArrayList<BaseFile>();

        initDocSuffixToType();

        mHasRefreshContainer.clear();
        mAutoRefreshBookShelf = new AutoRefreshFileShelf(Global.context, new AutoRefreshFileShelf.AutoRefreshListener() {

            @Override
            public ArrayList<String> onGetBookPathList() {
                return null;
            }

            @Override
            public void onBookRefresh(ArrayList<String> changeList) {
                GLog.d("changeList size = " + changeList.size());
            }

            @Override
            public void onBookScan() {
                GLog.d("file scan.");
            }
        }, new String[]{".zip", ".rar"});
    }

    public void registerContentObserver(){
        if(mAutoRefreshBookShelf != null){
            mAutoRefreshBookShelf.initAutoRefreshBookShelf();
        }
    }

    public String[] getmVideoSuffix() {
        return mVideoSuffix;
    }

    private void initDocSuffixToType(){
        mDocSuffixType.put(".doc", DirectModel.TYPE_DOC);
        mDocSuffixType.put(".docx", DirectModel.TYPE_DOC);
        mDocSuffixType.put(".xls", DirectModel.TYPE_DOC);
        mDocSuffixType.put(".pdf", DirectModel.TYPE_DOC);
        mDocSuffixType.put(".ppt", DirectModel.TYPE_DOC);
        mDocSuffixType.put(".pptx", DirectModel.TYPE_DOC);
        mDocSuffixType.put(".xlsx", DirectModel.TYPE_DOC);
        mDocSuffixType.put(".txt", DirectModel.TYPE_TXT);
        mDocSuffixType.put(".chm", DirectModel.TYPE_TXT);
        mDocSuffixType.put(".ebk", DirectModel.TYPE_TXT);
        mDocSuffixType.put(".umb", DirectModel.TYPE_TXT);
        mDocSuffixType.put(".zip", DirectModel.TYPE_ZIP);
        mDocSuffixType.put(".rar", DirectModel.TYPE_ZIP);
        mDocSuffixType.put(".7z", DirectModel.TYPE_ZIP);
        mDocSuffixType.put(".iso", DirectModel.TYPE_ZIP);
        mDocSuffixType.put(".mp4", DirectModel.TYPE_VIDEO);
        mDocSuffixType.put(".apk", DirectModel.TYPE_APK);
    }

    public ArrayList<BaseFile> loadGodapDocumentFileFromCenterFileDB(String type){
        List<CenterFile> list = CenterFileDBManager.getInstance().loadCenterFileByType(type);
        ArrayList<BaseFile> baseFiles = new ArrayList<BaseFile>();
        GLog.d("there are " + list.size() + " " + type + "in center");

        if (list != null) {
            try {
                ArrayList<CenterFile> deleteFiles = new ArrayList<>();
                for (CenterFile file : list) {
                    BaseFile baseFile = getBaseFileByPath(file.getPath());
                    if (baseFile != null) {
                        baseFiles.add(baseFile);
                    } else {
                        GLog.e("delete File : " + file.getPath());
                        deleteFiles.add(file);
                    }
                }
                CenterFileDBManager.getInstance().delete(deleteFiles);
            } catch (Exception e) {
                GLog.e("File " + CommonUtil.getCrashReport(e));
            }
        }
        return baseFiles;
    }


    public ArrayList<BaseFile> loadUnInstallApkFileFromCenterFileDB(){
        List<CenterFile> list = CenterFileDBManager.getInstance().loadApk(0);
        ArrayList<BaseFile> baseFiles = new ArrayList<BaseFile>();

        if (list != null) {
            try {
                ArrayList<CenterFile> deleteFiles = new ArrayList<>();
                for (CenterFile file : list) {
                    BaseFile baseFile = getBaseFileByPath(file.getPath());
                    if (baseFile != null) {
                        baseFiles.add(baseFile);
                    } else {
                        GLog.e("delete file : " + file.getPath());
                        deleteFiles.add(file);
                    }
                }
                CenterFileDBManager.getInstance().delete(deleteFiles);
            } catch (Exception e) {
                GLog.e(CommonUtil.getCrashReport(e));
            }
        }
        return baseFiles;
    }

    public void loadAlldata(){
        String[] types = {FileUtil.FILE_TYPE_EBOOK, FileUtil.FILE_TYPE_APK, FileUtil.FILE_TYPE_DOC, FileUtil.FILE_TYPE_ZIP};
        for (String item : types){
            ArrayList<BaseFile> tmp = loadDiffTypeData( item);
        }
    }

    /**
     * 计算各类文档的数量并获得相关的数据，用以更新目录
     * @return
     */
    public ArrayList<BaseFile> loadDiffTypeData(String type){
        GLog.d("mNeedToRefresh : " + ! mHasRefreshContainer.contains(type));
        ArrayList<BaseFile> tmp = null;

        if(mHasRefreshContainer.contains(type)){
            refreshSelectState(mDocFileList);
            refreshSelectState(mtxtFileList);
            refreshSelectState(mZipFileList);
            refreshSelectState(mBigFileList);
            refreshSelectState(mApkFileList);
            refreshSelectState(mGoDapTransferFileList);
            refreshSelectState(mGoDapUploadFileList);
            return tmp;
        } else {
            //读取数据库中的文档等信息
            GLog.d("load local files start... type = " + type);

            mHasRefreshContainer.add(type);
            GLog.d("load local files end... type = " + type);
            return tmp;
        }
    }


    public ArrayList<String> searchLocalVideoFile( ){
        ArrayList<String> res = mAutoRefreshBookShelf.getSecialSuffixFilePathList(Global.context,mVideoSuffix);
        return res;
}

    private ArrayList<BaseFile> mergeFiles(ArrayList<BaseFile> list1, ArrayList<BaseFile> list2){
        ArrayList<BaseFile> list3 = new ArrayList<BaseFile>();
        HashSet<String> paths = new HashSet<String>();

        if(list1 != null){
            for (BaseFile file : list1){
                if(! paths.contains(file.getPath())){
                    list3.add(file);
                    paths.add(file.getPath());
                }
            }
        }

        if(list2 != null){
            for (BaseFile file : list2){
                if(! paths.contains(file.getPath())){
                    list3.add(file);
                    paths.add(file.getPath());
                }
            }
        }

        return list3;
    }

    public ArrayList<BaseFile> getDocList(){
        return mDocFileList;
    }

    public ArrayList<BaseFile> getTxtList(){
        return  mtxtFileList;
    }

    public ArrayList<BaseFile> getZipList(){
        return mZipFileList;
    }

    public ArrayList<BaseFile> getmApkFileList() {
        return mApkFileList;
    }


    public void removeTxtFiles(String path){
        deletebyPath(path, mtxtFileList);
    }
    public void removeDocFiles(String path){
        deletebyPath(path, mDocFileList);
    }

    public void removeZipFiles(String path){
        deletebyPath(path, mZipFileList);
    }

    public void removeBigFiles(String path){
        deletebyPath(path, mBigFileList);
    }

    public void removeCopyFiles(String path){
        deletebyPath(path, mCopyFilesInFileList);
    }

    public void deletebyPath(String path, ArrayList<BaseFile> models){
        if(!TextUtil.isNull(path) && models != null){
            Iterator<BaseFile> iterator = models.iterator();
            while (iterator.hasNext()){
                BaseFile file = iterator.next();
                if(file.getPath().equals(path)){
                    iterator.remove();
                }
            }
        }
    }

    boolean isStopSearch = false;

    public void setStopSearch(boolean stopSearch) {
        isStopSearch = stopSearch;
    }

    public ArrayList<BaseFile> loadBigFile(BigFileSearchResultCallback callback, ArrayList<BaseFile> bigFiles){
            try {
                isStopSearch = false;
                mBigFileList = bigFiles;
                if(isSearchingBigFile()){
                    callback.onLoad(mBigFileList);
                } else {
                    return mBigFileList;
                }

                String interPath = DeviceInfo.getInstance().getInterSDCardPath();
                File rootFile = new File(interPath);
                searchLargeFile(rootFile, callback);

                //外置SD卡大文件搜索
                if(DeviceInfo.getInstance().isExistSDCard()){
                    String extSdPath = DeviceInfo.getInstance().getExtSDCardPath();
                    File file = new File(extSdPath);
                    searchLargeFile(file, callback);
                }

            }catch (Exception e){
                GLog.e(CommonUtil.getCrashReport(e));
                e.printStackTrace();
            }

            return mBigFileList;
    }


    /**
     * 从数据库中加载大文件数据
     * @param callback
     * @return
     */
    public ArrayList<BaseFile> loadBigFilesInDB( BigFileSearchResultCallback callback){
        List<CenterFile> list = CenterFileDBManager.getInstance().loadBigFile();
        ArrayList<BaseFile> res = new ArrayList<BaseFile>();
        mBigFilePathSet.clear();
        ArrayList<CenterFile> deleteFiles = new ArrayList<>();
        for (CenterFile file : list) {
            String path = file.getPath();
            BaseFile baseFile = getBaseFileByPath(path);
            if(baseFile != null){
                if(! mBigFilePathSet.contains(path)){
                    res.add(baseFile);
                    mBigFilePathSet.add(path);
                }
            } else {
                deleteFiles.add(file);
                GLog.e("delete 0 size godap video");
            }
        }
        CenterFileDBManager.getInstance().delete(deleteFiles);

        if(callback != null && isSearchingBigFile()){
            GLog.d("load big file in DB.size = " + res.size());
            ArrayList<BaseFile> tmp = res;
            callback.onLoad(tmp);
        } else {
            return res;
        }

        res.addAll(loadBigFile(callback, res)) ;

        return res;
    }

    private void searchLargeFile(File fileold, BigFileSearchResultCallback callback){
        if(isStopSearch){
            return;
        }
        try{

            File[] files=fileold.listFiles();
            if(files.length > 0){
                for(int j=0;j<files.length;j++){
                    if(!files[j].isDirectory()){
                        File thisfile = files[j];

                        //判断大文件
                        if(FileUtil.isBigFile(thisfile) && ! mBigFilePathSet.contains(thisfile.getPath())){
                            BaseFile baseFile = new BaseFile();
                            baseFile.setName(thisfile.getName());
                            baseFile.setPath(thisfile.getPath());
                            baseFile.setType(FileUtil.getFileType(baseFile.getName()));
                            baseFile.setIsdirectory(false);
                            baseFile.setSize(thisfile.length() + "");
                            baseFile.setTime(new Date(thisfile.lastModified()));
                            GLog.d("bigfile : " + thisfile.getPath() + "; size : " + baseFile.getSize());
                            mBigFileList.add(baseFile);
                            mBigFilePathSet.add(baseFile.getPath());

                            if(! CenterFileDBManager.getInstance().contain(baseFile.getPath())){
                                int video_type = 0;
                                if(baseFile.getType().equals(FileUtil.FILE_TYPE_VIDEO)){
                                    video_type = VideoBaseDataHelper.getInstance().LOCAL_VIDEO;
                                } else if(baseFile.getType().equals(FileUtil.FILE_TYPE_APK)){
                                }
                                CenterFileDBManager.getInstance().saveCenterFile(
                                        baseFile.getPath(),
                                        baseFile.getName(),
                                        baseFile.getType(),
                                        baseFile.getTime(),
                                        FileUtil.isVideoUri(baseFile.getPath()) ? VideoBaseDataHelper.getInstance().getVideoDuration(baseFile.getPath()) : 0L,
                                        video_type,
                                        FileUtil.FILE_BIG_FILE) ;
                            }

                            ArrayList<BaseFile> tmp = mBigFileList;
                            if(isSearchingBigFile()){
                                callback.onLoad(tmp);
                            } else {
                                return;
                            }
                        }
                    } else {
                        this.searchLargeFile(files[j], callback);
                    }
                }
            }
        }catch(Exception e){
            GLog.e("searchLargeFile error : " + e.toString());
        }
    }

    private void searchLocalFileMemory(String key, File searchfile, ArrayList<String> paths){
        try{
            File[] files=searchfile.listFiles();
            if(files!= null && files.length > 0){
                for(int j=0;j<files.length;j++){
                    if(files[j] != null && !files[j].isDirectory()){

                        if(files[j].getName().contains(key)){
                            File thisfile = files[j];

                            BaseFile file = new BaseFile();
                            file.setName(thisfile.getName());
                            file.setPath(thisfile.getPath());
                            file.setType(FileUtil.getFileType(file.getName()));
                            file.setIsdirectory(false);
                            file.setSize(thisfile.length() + "");
                            file.setTime(new Date(thisfile.lastModified()));
                            if(paths == null || ! paths.contains(thisfile.getPath())){
                                mSearchResultList.add(file);
                            }
                        }
                    } else {
                        this.searchLocalFileMemory(key, files[j], paths );
                    }
                }
            }
        } catch(Exception e) {
            GLog.e(CommonUtil.getCrashReport(e));
        }
    }

    public ArrayList<BaseFile> findAllFileExceptFolder(String path){
        if(! TextUtil.isNull(path)){
            File searchFile = new File(path);
            mSearchResultList.clear();
            searchLocalFileMemory("", searchFile, null);
            return mSearchResultList;
        }

        return null;
    }

    private ArrayList<BaseFile> loadGoDapTransferFiles( ){
        final String intSdPath = StorageManager.getInstance().getInternalTranferDirPath();
        final String extSdPath = StorageManager.getInstance().getExternalGoDapTranferDirPath();
        String[] paths = {intSdPath, extSdPath};

        ArrayList<BaseFile> infos = new ArrayList<BaseFile>();
        for (String path : paths){
            if (!TextUtil.isNull(path)) {
                ArrayList<BaseFile> tmp1 = findAllFileExceptFolder(path);
                if(tmp1 != null && ! tmp1.isEmpty()){
                    infos.addAll(tmp1);
                }
            }
        }
        return infos;
    }

    private ArrayList<BaseFile> loadGoDapUploadFiles(){
        final String intSdPath = StorageManager.getInstance().getInternalGodapUploadDirPath();
        final String extSdPath = StorageManager.getInstance().getExternalGoDapUploadDirPath();
        String[] paths = {intSdPath, extSdPath};

        ArrayList<BaseFile> infos = new ArrayList<BaseFile>();
        for (String path : paths){
            if (!TextUtil.isNull(path)) {
                ArrayList<BaseFile> tmp1 = findAllFileExceptFolder(path);
                if(tmp1 != null && ! tmp1.isEmpty()){
                    infos.addAll(tmp1);
                }
            }
        }
        return infos;
    }



    public static BaseFile getBaseFileByPath(String path){
        if(TextUtil.isNull(path) ){
            return null;
        }
        File thisfile = new File(path);
        if(thisfile == null || !thisfile.exists() || thisfile.length() <=0){
            return null;
        }

        BaseFile file = new BaseFile();
        file.setName(thisfile.getName());
        file.setPath(thisfile.getPath());
        file.setType(FileUtil.getFileType(file.getName()));
        file.setIsdirectory(thisfile.isDirectory());
        file.setSize(thisfile.length() + "");
        file.setTime(new Date(thisfile.lastModified()));
        if(file.getType().equals(FileUtil.FILE_TYPE_AUDIO)
            || (file.getType().equals(FileUtil.FILE_TYPE_VIDEO) && VideoUtils.hasCopyright(VideoBaseDataHelper.getInstance().getVideoDuration(file.getPath())))
                ){
            file.setCopyright(true);
        }

        return file;
    }

    /** 获取一个文件夹下的所有文件 **/
    public ArrayList<BaseFile> getFiles(String path) {
        ArrayList<BaseFile> fileList = new ArrayList<BaseFile>();

        File f = new File(path);
        File[] files = f.listFiles();
        if (files == null || files.length == 0) {
            return fileList;
        }

        String fileName;
        // 获取文件列表
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            BaseFile fileInfo = new BaseFile();

            fileName = file.getName();
            if (file.isDirectory() && (TextUtils.isEmpty(fileName) || fileName.startsWith("."))) {
                continue;
            }

            fileInfo.setName(fileName);
            fileInfo.setPath(file.getPath());
            fileInfo.setType(FileUtil.getFileType(file.getName()));
            fileInfo.setIsdirectory(file.isDirectory());
            fileInfo.setSize(file.length() + "");
            fileInfo.setTime(new Date(file.lastModified()));
            fileList.add(fileInfo);
        }

        // 排序
        Collections.sort(fileList, new FileUtil.FileComparator());
        return fileList;
    }

    private void refreshSelectState(ArrayList<BaseFile> infoArrayList){
        try {
            if(infoArrayList != null){
                Iterator<BaseFile> it = infoArrayList.iterator();
                while (it.hasNext()){
                    BaseFile info = it.next();
                }
            }
        }catch (Exception e){
            GLog.e("refresh file select state ERROR : " + e.toString());
        }
    }

    public ArrayList<BaseFile> getmBigFileList() {
        return mBigFileList;
    }

    public void saveFilesToDB(ArrayList<BaseFile> files){
        GLog.d("start save file to db");
        if(files !=  null){
            ArrayList<CenterFile> list = new ArrayList<>();
            for(BaseFile file : files){
                CenterFile centerFile = CenterFileDBManager.getInstance().getNewCenterFileModel(
                        file.getPath(),
                        file.getName(),
                        file.getType(),
                        file.getTime(),
                        0L,
                        0,
                        FileUtil.isBigFile(file.getPath()) ? FileUtil.FILE_BIG_FILE : ""
                );
                list.add(centerFile);
            }
            CenterFileDBManager.getInstance().saveCenterFileList(list);
        }
        GLog.d("finish save file to db");
    }

    public void saveApkFilesToDB(ArrayList<BaseFile> files){
        GLog.d("start save apk file to db");
        if(files !=  null){
            ArrayList<CenterFile> list = new ArrayList<>();
            for(BaseFile file : files){
                CenterFile cf = CenterFileDBManager.getInstance().getNewCenterFileModel(
                        file.getPath(),
                        file.getName(),
                        file.getType(),
                        file.getTime(),
                        0L,
                        0,
                        FileUtil.isBigFile(file.getPath()) ? FileUtil.FILE_BIG_FILE : ""
                );
                list.add(cf);
            }

            CenterFileDBManager.getInstance().saveCenterFileList(list);
        }
        GLog.d("finish save apk file to db");
    }


    public interface BigFileSearchResultCallback{
        void onLoad(ArrayList<BaseFile> fileArrayList);
    }

    public ArrayList<BaseFile> getmCopyFilesInFileList() {
        return mCopyFilesInFileList;
    }

    public void setCopyFilesInFileList(ArrayList<BaseFile> mBigFileCopyInFileList) {
        this.mCopyFilesInFileList = mBigFileCopyInFileList;
    }

    public boolean isSearchingBigFile() {
        return mIsSearchingBigFile;
    }

    public void setmIsSearchingBigFile(boolean mIsSearchingBigFile) {
        this.mIsSearchingBigFile = mIsSearchingBigFile;
    }

    public HashSet<String> getmHasRefreshContainer() {
        return mHasRefreshContainer;
    }
}
