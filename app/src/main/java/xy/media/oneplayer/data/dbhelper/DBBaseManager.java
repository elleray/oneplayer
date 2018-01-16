package xy.media.oneplayer.data.dbhelper;

import xy.media.oneplayer.data.greendao.DaoMaster;
import xy.media.oneplayer.data.greendao.DaoSession;
import xy.media.oneplayer.gl.Global;
import xy.media.oneplayer.log.log.GLog;

/**
 * Created by elleray on 16/7/11.
 */
public class DBBaseManager {
    private static final String DATABASE_NAME = "godap_module_file.db";
    private volatile static DBBaseManager instance;
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    private DBBaseManager(){
        init();
    }

    private void init(){
        GLog.i("DBBaseManager init().");
        DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(Global.context,
                DATABASE_NAME, null);
        daoMaster = new DaoMaster(helper.getWritableDatabase());
        daoSession = daoMaster.newSession();
    }

    public static DBBaseManager getInstance(){
        if(instance == null){
            synchronized (DBBaseManager.class) {
                if (instance == null) {
                    instance = new DBBaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * 取得DaoMaster
     * @return
     */
    public DaoMaster getDaoMaster() {
        return daoMaster;
    }

    /**
     * 取得DaoSession
     * @return
     */
    public DaoSession getDaoSession() {
        return daoSession;
    }
}
