package xy.media.oneplayer.data.dbhelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.dao.query.Query;
import xy.media.oneplayer.data.VideoBaseDataHelper;
import xy.media.oneplayer.data.greendao.BaseFile;
import xy.media.oneplayer.data.greendao.CenterFile;
import xy.media.oneplayer.data.greendao.CenterFileDao;
import xy.media.oneplayer.util.FileUtil;
import xy.media.oneplayer.util.TextUtil;

/**
 * Created by elleray on 16/8/3.
 */
public class CenterFileDBManager {

    private static CenterFileDBManager instance;
    private CenterFileDao mCenterFileDao;

    public CenterFileDBManager() {
        mCenterFileDao = DBBaseManager.getInstance().getDaoSession().getCenterFileDao();
    }

    public static CenterFileDBManager getInstance() {
        if (instance == null) {
            synchronized (CenterFileDBManager.class) {
                if (instance == null) {
                    instance = new CenterFileDBManager();
                }
            }
        }
        return instance;
    }

    /**
     * 用来存储新发现的文件
     * @param file 封装好的新文件
     */
    public void saveCenterFile(CenterFile file) {
        mCenterFileDao.insertOrReplaceInTx(file);
    }

    public void saveCenterFileList(ArrayList<CenterFile> list){
        if(list != null && ! list.isEmpty()){
            mCenterFileDao.insertOrReplaceInTx(list);
        }
    }

    public void saveCenterFile(String path, String name, String type, java.util.Date create_date, Long duration, Integer video_type, String sha1){
        saveCenterFile(getNewCenterFileModel(path, name, type, create_date, duration, video_type, sha1));
    }

    public void saveCenterFile(String path, String name, String type, java.util.Date create_date, Long duration, Integer video_type, String sha1, boolean isGodapFile){
        saveCenterFile(getNewCenterFileModel(path, name, type, create_date, duration, video_type, sha1, isGodapFile));
    }

    public CenterFile getNewCenterFileModel(String path, String name, String type, java.util.Date create_date, Long duration, Integer video_type, String sha1){
        CenterFile model = new CenterFile();
        model.setPath(path);
        model.setName(name);
        model.setType(type);
        model.setCreate_date(create_date);
        model.setDuration(duration);
        model.setVideo_type(video_type);
        model.setSha1(sha1);
        return model;
    }


    public CenterFile getNewCenterFileModel(String path, String name, String type, java.util.Date create_date, Long duration, Integer video_type, String sha1, boolean isGodapFile){
        CenterFile model = getNewCenterFileModel(path, name, type, create_date, duration, video_type, sha1);
        model.setIsgodapfile(isGodapFile);
        return model;
    }

    /**
     *  存储新的文件到数据库，这个目前用来存储从PC下载下来的文件
     * @param path 新文件的路径
     */
    public void saveDownloadFileToDB(String path) {
        if (FileUtil.getFileType(path).equals(FileUtil.FILE_TYPE_VIDEO)
                || FileUtil.getFileType(path).equals(FileUtil.FILE_GODAP_VIDEO_TYPE)) {
            File file = new File(path);
            saveCenterFile(getNewCenterFileModel(file.getPath(),
                    file.getName(),
                    FileUtil.FILE_TYPE_VIDEO,
                    new Date(System.currentTimeMillis()),
                    VideoBaseDataHelper.getInstance().getVideoDuration(file.getPath()),
                    0,
                    FileUtil.isBigFile(file.getPath()) ? FileUtil.FILE_BIG_FILE : ""
                    ,true
            ));
        } else if (FileUtil.getFileType(path).equals(FileUtil.FILE_TYPE_AUDIO)) {
            File file = new File(path);
            saveCenterFile(
                    getNewCenterFileModel(path,
                            file.getName(),
                            FileUtil.FILE_TYPE_AUDIO,
                            new Date(System.currentTimeMillis()),
                            0L,
                            0,
                            FileUtil.isBigFile(file.getPath()) ? FileUtil.FILE_BIG_FILE : "",
                            true));
        } else if (FileUtil.getFileType(path).equals(FileUtil.FILE_TYPE_PICTURE)) {
            File file = new File(path);
            saveCenterFile(getNewCenterFileModel(path,
                    file.getName(),
                    FileUtil.FILE_TYPE_PICTURE,
                    new Date(file.lastModified()),
                    0l,
                    0,
                    FileUtil.isBigFile(file.getPath()) ? FileUtil.FILE_BIG_FILE : "",
                    true));
        } else {
        }
    }

    public List<CenterFile> loadGodapFiles(){
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Isgodapfile.eq(true))
                .orderDesc(CenterFileDao.Properties.Create_date)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files;
    }

    /**
     * 通过类型加载数据库中文件
     * @param type 文件类型
     * @return
     */
    public List<CenterFile> loadCenterFileByType(String type) {
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Type.eq(type))
                .orderDesc(CenterFileDao.Properties.Create_date)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files;
    }

    /**
     * 加载数据库中的音乐
     * @return
     */
    public List<CenterFile> loadAudio() {
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Type.eq(FileUtil.FILE_TYPE_AUDIO))
                .orderDesc(CenterFileDao.Properties.Create_date)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files;
    }

    /**
     * 加载数据库中的图片
     * @return
     */
    public List<CenterFile> loadImage() {
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Type.eq(FileUtil.FILE_TYPE_PICTURE))
                .orderDesc(CenterFileDao.Properties.Create_date)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files;
    }

    /**
     * 加载数据库中的原godap下载的音频
     * @return
     */
    public List<CenterFile> loadGodapAudio() {
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Type.eq(FileUtil.FILE_TYPE_AUDIO))
                .orderDesc(CenterFileDao.Properties.Create_date)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files;
    }


    /**
     * 按照视频属性类型来加载数据库视频
     * @param videoType 视频属性类型，可以是LOCAL、GODAP
     * @return
     */
    public List<CenterFile> loadVideo(int videoType) {
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Type.eq(FileUtil.FILE_TYPE_VIDEO))
                .where(CenterFileDao.Properties.Isgodapfile.eq(videoType == VideoBaseDataHelper.TAB_GODAP ))
                .orderDesc(CenterFileDao.Properties.Create_date)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files;
    }

    /**
     * 按照Apk属性类型来加载数据库apk
     * @param type Apk属性类型，可以
     * @return
     */
    public List<CenterFile> loadApk(int type) {
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Type.eq(FileUtil.FILE_TYPE_APK))
                .where(CenterFileDao.Properties.Video_type.eq(type))
                .orderDesc(CenterFileDao.Properties.Create_date)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files;
    }

    public List<CenterFile> loadApk() {
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Type.eq(FileUtil.FILE_TYPE_APK))
                .orderDesc(CenterFileDao.Properties.Create_date)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files;
    }

    public List<CenterFile> loadBigFile() {
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Sha1.eq(FileUtil.FILE_BIG_FILE))
                .orderDesc(CenterFileDao.Properties.Create_date)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files;
    }

    public boolean isEmpty(){
        Query query = mCenterFileDao.queryBuilder()
                .orderDesc(CenterFileDao.Properties.Create_date)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files == null || files.isEmpty();
    }

    /**
     * 根据文件路径删除数据库中文件
     * @param path 要删除的文件路径
     */
    public void delete(String path) {
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Path.eq(path))
                .build();
        List files = query.list();
        CenterFile receive;
        if (files != null && !files.isEmpty()) {
            receive = (CenterFile) files.get(0);
            mCenterFileDao.delete(receive);
        }
    }

    public void delete(ArrayList<CenterFile> files) {
        if(files != null && ! files.isEmpty()){
            mCenterFileDao.deleteInTx(files);
        }
    }

    /**
     * 修改文件路径的方法
     * @param oldPath 原来的文件路径
     * @param newPath 新的文件路径
     */
    public void rename(String oldPath, String newPath){
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Path.eq(oldPath))
                .build();
        List files = query.list();
        if(files != null){
            CenterFile oldCenterFile = (CenterFile) files.get(0);
            CenterFile newFile = getNewCenterFileModel(
                    newPath,
                    (new File(newPath)).getName(),
                    oldCenterFile.getType(),
                    oldCenterFile.getCreate_date(),
                    oldCenterFile.getDuration(),
                    oldCenterFile.getVideo_type(),
                    oldCenterFile.getSha1(),
                    oldCenterFile.getIsgodapfile()
            );
            delete(oldPath);
            saveCenterFile(newFile);

            //如果是视频文件重命名，还需更新其他数据库
            if(oldCenterFile.getType().equals(FileUtil.FILE_TYPE_VIDEO)){
                //播放记录数据库
                if(VideoPlayRecordDBManager.getInstance().contain(oldPath)){
                    VideoPlayRecordDBManager.getInstance().rename(oldPath, newPath);
                    VideoBaseDataHelper.getInstance().loadVideoRecords();
                }
            }

        }
    }

    /**
     * 判断数据库中是否含有对应的文件
     * @param path 文件路径
     * @return
     */
    public boolean contain(String path){
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Path.eq(path))
                .build();
        List files = query.list();
        return files != null && !files.isEmpty();
    }

    /**
     * 根据sha1查询是否本地是否存在godap视频或音频
     * @param sha1 文件sha1数值
     * @return
     */
    public List<CenterFile> findBySha1(String sha1) {
        if (!TextUtil.isNull(sha1)) {
            Query query = mCenterFileDao.queryBuilder()
                    .where(CenterFileDao.Properties.Sha1.eq(sha1))
                    .build();
            List files = query.list();

            return files;
        }
        return null;
    }

    /**
     * 根据文件名字搜索数据库中的文件
     * @param name 文件名字
     * @return
     */
    public ArrayList<BaseFile> search(String name) {
        // Query 类代表了一个可以被重复执行的查询
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Name.like("%" + name + "%"))
                .build();

        // 手动精确过滤
        ArrayList<CenterFile> resultList = (ArrayList<CenterFile>) query.list();
        ArrayList<BaseFile> res = new ArrayList<BaseFile>();

        for (CenterFile info : resultList) {
            File thisfile = new File(info.getPath());
            BaseFile baseFile = new BaseFile();
            baseFile.setName(thisfile.getName());
            baseFile.setSelected(false);
            baseFile.setPath(thisfile.getPath());
            baseFile.setType(FileUtil.getFileType(baseFile.getName()));
            baseFile.setIsdirectory(false);
            baseFile.setSize(thisfile.length() + "");
            baseFile.setTime(new Date(thisfile.lastModified()));

            res.add(baseFile);
        }
        return res;
    }

    /**
     * 在特定路径下搜索符合文件名的文件
     * @param name 搜索的文件名
     * @param path 搜素位置路径
     * @return
     */
    public ArrayList<BaseFile> search(String name, String path) {
        // Query 类代表了一个可以被重复执行的查询
        Query query = mCenterFileDao.queryBuilder()
                .where(CenterFileDao.Properties.Name.like("%" + name + "%"),
                        CenterFileDao.Properties.Path.like(path + "%"))
                .build();

        // 手动精确过滤
        ArrayList<CenterFile> resultList = (ArrayList<CenterFile>) query.list();
        ArrayList<BaseFile> res = new ArrayList<BaseFile>();

        for (CenterFile info : resultList) {
            File thisfile = new File(info.getPath());
            BaseFile baseFile = new BaseFile();
            baseFile.setName(thisfile.getName());
            baseFile.setSelected(false);
            baseFile.setPath(thisfile.getPath());
            baseFile.setType(FileUtil.getFileType(baseFile.getName()));
            baseFile.setIsdirectory(false);
            baseFile.setSize(thisfile.length() + "");
            baseFile.setTime(new Date(thisfile.lastModified()));

            res.add(baseFile);
        }
        return res;
    }
}
