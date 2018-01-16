package xy.media.oneplayer.data.dbhelper;

import java.util.List;

import de.greenrobot.dao.query.Query;
import xy.media.oneplayer.data.greendao.VideoPlayRecord;
import xy.media.oneplayer.data.greendao.VideoPlayRecordDao;
import xy.media.oneplayer.log.log.GLog;
import xy.media.oneplayer.util.TextUtil;


/**
 * Created by elleray on 16/7/28.
 */
public class VideoPlayRecordDBManager {
    private static VideoPlayRecordDBManager instance;

    private VideoPlayRecordDao mVideoPlayRecordDao;

    public static VideoPlayRecordDBManager getInstance() {
        if(instance == null){
            synchronized (BaseFileDBManager.class){
                if(instance == null){
                    instance = new VideoPlayRecordDBManager();
                }
            }
        }
        return instance;
    }

    private VideoPlayRecordDBManager(){
        mVideoPlayRecordDao = DBBaseManager.getInstance().getDaoSession().getVideoPlayRecordDao();
    }

    public VideoPlayRecord findVideoRecord(String path) throws Exception {
        return mVideoPlayRecordDao.load(path);
    }

    public List<VideoPlayRecord> loadAll(){
        return mVideoPlayRecordDao.loadAll();
    }

    public void insert(VideoPlayRecord record){
        mVideoPlayRecordDao.insertOrReplace(record);
    }

    public boolean contain(String path){
        Query query = mVideoPlayRecordDao.queryBuilder()
                .where(VideoPlayRecordDao.Properties.Path.eq(path))
                .build();
        List files = query.list();
        return files != null && !files.isEmpty();
    }

    public void delete(String path){
        Query query = mVideoPlayRecordDao.queryBuilder()
                .where(VideoPlayRecordDao.Properties.Path.eq(path))
                .build();
        List files = query.list();
        VideoPlayRecord record;
        if(files != null && !files.isEmpty()){
            record = (VideoPlayRecord) files.get(0);
            mVideoPlayRecordDao.delete(record);
        }
    }

    public boolean isWatchFinish(String path){
        Query query = mVideoPlayRecordDao.queryBuilder()
                .where(VideoPlayRecordDao.Properties.Path.eq(path))
                .build();
        List<VideoPlayRecord> records = query.list();
        if(records != null){
            for (VideoPlayRecord record : records){
                if(record.getHave_complete_watch_once()){
                    return true;
                }
            }
        }
        return false;
    }

    public void rename(String oldPath, String newPath){
        if(! TextUtil.isNull(oldPath) && contain(oldPath)){
            try {
                VideoPlayRecord record = findVideoRecord(oldPath);
                delete(oldPath);
                insert(new VideoPlayRecord(newPath, record.getPlay_time(), record.getTotal_time(), record.getLeft_time(), record.getHave_complete_watch_once()));
            } catch (Exception e){
                GLog.e(e.toString());
                e.printStackTrace();
            }
        }
    }

}
