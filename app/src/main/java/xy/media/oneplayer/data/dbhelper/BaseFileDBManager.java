package xy.media.oneplayer.data.dbhelper;


import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.Query;
import xy.media.oneplayer.data.greendao.BaseFile;
import xy.media.oneplayer.data.greendao.BaseFileDao;

/**
 * Created by elleray on 16/7/28.
 */
public class BaseFileDBManager {

    private static volatile BaseFileDBManager instance;

    private BaseFileDao mBaseFileDao;

    public static BaseFileDBManager getInstance() {
        if(instance == null){
            synchronized (BaseFileDBManager.class){
                if(instance == null){
                    instance = new BaseFileDBManager();
                }
            }
        }
        return instance;
    }

    private BaseFileDBManager(){
        mBaseFileDao = DBBaseManager.getInstance().getDaoSession().getBaseFileDao();
    }

    public List query(String name){
        // Query 类代表了一个可以被重复执行的查询
        Query query = mBaseFileDao.queryBuilder()
                .where(BaseFileDao.Properties.Name.eq(name))
                .orderAsc(BaseFileDao.Properties.Id)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files;
    }

    public List query(String name, String type){
        // Query 类代表了一个可以被重复执行的查询
        Query query = mBaseFileDao.queryBuilder()
                .where(BaseFileDao.Properties.Name.eq(name))
                .where(BaseFileDao.Properties.Type.eq(type))
                .orderAsc(BaseFileDao.Properties.Id)
                .build();
        //查询结果以 List 返回
        List files = query.list();
        return files;
    }


    public ArrayList<BaseFile> search(String name){
        // Query 类代表了一个可以被重复执行的查询
        Query query = mBaseFileDao.queryBuilder()
                .where(BaseFileDao.Properties.Name.like("%"+name+"%"))
                .orderAsc(BaseFileDao.Properties.Id)
                .build();

        // 手动精确过滤
        ArrayList<BaseFile> resultList = (ArrayList<BaseFile>) query.list();
        if (resultList == null || resultList.size() == 0) {
            return resultList;
        }

        int size = resultList.size();
        for (int i = size - 1; i >= 0; i--) {
            if (!resultList.get(i).getName().contains(name)) {
                resultList.remove(i);
            }
        }
        return resultList;
    }

    public ArrayList<BaseFile> search(String path, String name) {
        // Query 类代表了一个可以被重复执行的查询
        Query query = mBaseFileDao.queryBuilder()
                .where(BaseFileDao.Properties.Name.like("%"+name+"%"), BaseFileDao.Properties.Path.like(path + "%"))
                .orderAsc(BaseFileDao.Properties.Id)
                .build();

        // 手动精确过滤
        ArrayList<BaseFile> resultList = (ArrayList<BaseFile>) query.list();
        if (resultList == null || resultList.size() == 0) {
            return resultList;
        }

        int size = resultList.size();
        for (int i = size - 1; i >= 0; i--) {
            if (!resultList.get(i).getName().contains(name)) {
                resultList.remove(i);
            }
        }
        return resultList;
    }


    public void delete(List<BaseFile> files){
        mBaseFileDao.deleteInTx(files);
    }
}
