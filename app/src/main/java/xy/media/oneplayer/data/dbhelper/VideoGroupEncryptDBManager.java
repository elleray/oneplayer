package xy.media.oneplayer.data.dbhelper;

import java.util.List;

import de.greenrobot.dao.query.Query;
import xy.media.oneplayer.data.greendao.VideoGroupEncrypt;
import xy.media.oneplayer.data.greendao.VideoGroupEncryptDao;
import xy.media.oneplayer.util.TextUtil;

/**
 * Created by Administrator on 2017/6/22 0022.
 */

public class VideoGroupEncryptDBManager {

    private static VideoGroupEncryptDBManager instance;
    private VideoGroupEncryptDao mDVideoGroupEncryptDao;

    public static VideoGroupEncryptDBManager getInstance(){
        if(instance == null){
            synchronized (VideoGroupEncryptDBManager.class){
                if(instance == null){
                    instance = new VideoGroupEncryptDBManager();
                }
            }
        }
        return instance;
    }

    private VideoGroupEncryptDBManager(){
        mDVideoGroupEncryptDao = DBBaseManager.getInstance().getDaoSession().getVideoGroupEncryptDao();
    }

    public long createAndAdd(){
        VideoGroupEncrypt model = new VideoGroupEncrypt();
        add(model);
        return model.getId();
    }

    public void add(VideoGroupEncrypt model){
        if(model != null){
            mDVideoGroupEncryptDao.insertOrReplace(model);
        }
    }

    public VideoGroupEncrypt query(Long id){
        Query query = mDVideoGroupEncryptDao.queryBuilder()
                .where(VideoGroupEncryptDao.Properties.Id.eq(id))
                .build();
        List files = query.list();

        VideoGroupEncrypt model;
        if(files != null && !files.isEmpty()){
            model = (VideoGroupEncrypt) files.get(0);
            return model;
        }

        return null;
    }


    public void encrypt(Long id){
        encrypt(id, "");
    }

    public boolean encrypt(Long id, String key){
        VideoGroupEncrypt model = query(id);
        if(model != null){
            add(new VideoGroupEncrypt(id, true, key));
            return true;
        }

        return false;
    }

    public boolean decode(Long id, String key){
        VideoGroupEncrypt model = query(id);
        if(model != null){
            if(( TextUtil.isNull(key) && TextUtil.isNull(model.getKey()))
                    || model.getKey().equals(key) ){
                add(new VideoGroupEncrypt(id, false, ""));
                return true;
            }
        }

        return false;
    }
}
