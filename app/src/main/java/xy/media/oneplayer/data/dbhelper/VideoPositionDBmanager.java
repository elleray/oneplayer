package xy.media.oneplayer.data.dbhelper;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.Query;
import xy.media.oneplayer.data.greendao.VideoPosition;
import xy.media.oneplayer.data.greendao.VideoPositionDao;

/**
 * Created by Administrator on 2017/6/15 0015.
 */

public class VideoPositionDBmanager {

    private static VideoPositionDBmanager instance;

    private VideoPositionDao mVideoPositionDao;

    public static VideoPositionDBmanager getInstance(){
        if(instance == null){
            synchronized (VideoPositionDBmanager.class){
                if(instance == null){
                    instance = new VideoPositionDBmanager();
                }
            }
        }

        return instance;
    }

    private VideoPositionDBmanager(){
        mVideoPositionDao = DBBaseManager.getInstance().getDaoSession().getVideoPositionDao();
    }

    public List<VideoPosition> loadAll(){
        return mVideoPositionDao.loadAll();
    }

    public void add(VideoPosition videoPosition){
        mVideoPositionDao.insertOrReplace(videoPosition);
    }

    public void addAll(ArrayList<VideoPosition> list){
        mVideoPositionDao.insertOrReplaceInTx(list);
    }



    public void remove(String path){
        Query query = mVideoPositionDao.queryBuilder()
                .where(VideoPositionDao.Properties.Path.eq(path))
                .build();
        List files = query.list();

        VideoPosition model;
        if(files != null && !files.isEmpty()){
            model = (VideoPosition) files.get(0);
            mVideoPositionDao.delete(model);
        }
    }

    public boolean isEmpty(){
        return 0 == size();
    }

    public long size(){
        return mVideoPositionDao.count();
    }

    public void add(String path, int position, String tag, Long group_id){
        VideoPosition videoPosition = new VideoPosition(path, position, tag, group_id);
        add(videoPosition);
    }

    public void clear(){
        mVideoPositionDao.deleteAll();
    }

    public VideoPosition query(String path){
        Query query = mVideoPositionDao.queryBuilder()
                .where(VideoPositionDao.Properties.Path.eq(path))
                .build();
        List files = query.list();

        VideoPosition model;
        if(files != null && !files.isEmpty()){
            model = (VideoPosition) files.get(0);
            return model;
        }

        return null;
    }

    public VideoPosition queryTag(String tag){
        Query query = mVideoPositionDao.queryBuilder()
                .where(VideoPositionDao.Properties.Tag.eq(tag))
                .build();
        List files = query.list();

        VideoPosition model;
        if(files != null && !files.isEmpty()){
            model = (VideoPosition) files.get(0);
            return model;
        }

        return null;
    }

    public void renameTag(String path, String newTag){
        VideoPosition model = query(path);
        if(model != null){
            add(path, model.getPosition(), newTag, model.getGroup_id());
        }
    }
}
