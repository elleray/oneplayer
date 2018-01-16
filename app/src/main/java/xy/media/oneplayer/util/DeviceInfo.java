package xy.media.oneplayer.util;

import android.content.Context;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

import xy.media.oneplayer.gl.Global;
import xy.media.oneplayer.log.log.GLog;


/**
 * Created by yangwenjie on 16/8/1.
 */
public class DeviceInfo {
    private String extSDCardPath =  "";
    private String interSDCardPath = "";

    private static DeviceInfo instance;

    public static DeviceInfo getInstance(){
        if(instance == null){
            synchronized (DeviceInfo.class){
                if(instance == null){
                    instance = new DeviceInfo();
                }
            }
        }

        return instance;
    }

    private DeviceInfo(){
        extSDCardPath = getStoragePath(Global.context, true);
        interSDCardPath = getStoragePath(Global.context, false);

        GLog.d("SD path : " + interSDCardPath);
        GLog.d("ext SD path : " +extSDCardPath);
    }


    /**
     * 获取屏幕的密度
     */
    public float getDensity(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();// 创建了一张白纸
        windowManager.getDefaultDisplay().getMetrics(outMetrics);// 给白纸设置宽高
        return outMetrics.density;
    }

    public float getDensity() {
        WindowManager windowManager = (WindowManager) Global.context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();// 创建了一张白纸
        windowManager.getDefaultDisplay().getMetrics(outMetrics);// 给白纸设置宽高
        return outMetrics.density;
    }


    /**
     * 获取屏幕的宽度px
     */
    public int getDeviceWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();// 创建了一张白纸
        windowManager.getDefaultDisplay().getMetrics(outMetrics);// 给白纸设置宽高
        return outMetrics.widthPixels;
    }

    /**
     * 获取屏幕的宽度px
     */
    public int getDeviceHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();// 创建了一张白纸
        windowManager.getDefaultDisplay().getMetrics(outMetrics);// 给白纸设置宽高
        return outMetrics.heightPixels;
    }

    /**
     * 获取内置SD卡路径
     * @return
     */
    public String getInterSDCardPath() {
        return interSDCardPath;
    }

    /**
     * 获取外置SD卡路径
     * @return
     */
    public String getExtSDCardPath() {
        return extSDCardPath;
    }


    /**
     * 获得SD卡总大小
     *
     * @return
     */
    public String getSDTotalSize() {
        return Formatter.formatFileSize(Global.context, getSDTotalByte());
    }

    /**
     * 获得sd卡总大小
     * @return
     */
    public long getSDTotalByte() {
        try {
            File path =new File(interSDCardPath);
            StatFs stat = new StatFs(path.getPath());
            int blockSize = stat.getBlockSize();
            long blockCount = stat.getBlockCount();
            return blockSize * blockCount;
        }catch (Exception e){
            GLog.e(e.toString());
        }
        return 0;
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public String getSDAvailableSize() {
        return Formatter.formatFileSize(Global.context, getSDAvailableByte());
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public long getSDAvailableByte() {
        try {
            File path = new File(interSDCardPath);
            StatFs stat = new StatFs(path.getPath());
            int blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return blockSize * availableBlocks;
        }catch (Exception e){
            GLog.e(e.toString());
        }
        return  0;
    }

    public  boolean isExistSDCard() {
        return ! TextUtil.isNull(extSDCardPath) && getExtSDAvailableByte() > 0;
    }
    /**
     * 获得SD卡总大小
     *
     * @return
     */
    public String getExtSDTotalSize() {
        return Formatter.formatFileSize(Global.context, getExtSDTotalByte());
    }

    /**
     * 获得sd卡总大小
     * @return
     */
    public long getExtSDTotalByte() {
        try {
            File path = new File(extSDCardPath);
            StatFs stat = new StatFs(path.getPath());
            int blockSize = stat.getBlockSize();
            long blockCount = stat.getBlockCount();
            return blockSize * blockCount;
        }catch (Exception e){
            e.printStackTrace();
            GLog.e(e.toString());
        }

        return 0;
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public String getExtSDAvailableSize() {
        return Formatter.formatFileSize(Global.context, getExtSDAvailableByte());
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public long getExtSDAvailableByte() {
        try {
            File path = new File(extSDCardPath);
            StatFs stat = new StatFs(path.getPath());
            int blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return blockSize * availableBlocks;
        }catch (Exception e){
            GLog.e(e.toString());
        }
        return 0;
    }

    private static String getStoragePath(Context mContext, boolean could_remove) {
        android.os.storage.StorageManager mStorageManager
                = (android.os.storage.StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (could_remove == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            GLog.e("get storage path exception: " + e.toString());
        }
        return null;
    }
}
