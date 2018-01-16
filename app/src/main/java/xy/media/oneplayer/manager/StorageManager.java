package xy.media.oneplayer.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * 存储管理类
 * Created by Anchorer on 2017/1/9.
 */
@SuppressWarnings("deprecation")
public class StorageManager {
    
    private static final String TAG = "StorageManager";

    private static volatile StorageManager sInstance;

    private Context mContext;
    private String mPackageName;
    private SharedPreferences mSharedPref;

    // 设备内部存储路径
    private String mInternalRootPath;
    // 设备外部存储路径
    private String mExternalRootPath;

    // 记录用户选择的存储位置，将配置写到文件里
    public static final String SP_KEY_STORAGE_LOCATION = "KEY_STORAGE_LOCATION";
    public static final String SP_VALUE_STORAGE_INTERNAL = "STORAGE_LOCATION_INTERNAL";
    public static final String SP_VALUE_STORAGE_EXTERNAL = "STORAGE_LOCATION_EXTERNAL";

    // 判断存储空间是否足够的额外尺寸数
    public static final long FILE_SIZE_EXTRA = 1024L * 1024 * 10;

    // 提醒用户空间不足的下限 500M
    public static final long STORAGE_SPACE_LOW_LIMIT = 1024L * 1024 * 500;

    // 记录提醒用户空间不足弹窗的日期
    public static final String SP_KEY_SHOW_LOW_SPACE_HINT_DATE = "SHOW_LOW_SPACE_HINT_DATE_KEY";

    // 定义一系列文件夹名称
    private static String DIR_APP_ROOT = "OnePlayer";
    private static final String DIR_TRANSFER = "transfer";
    private static final String DIR_TRANSFER_VIDEO = "video";
    private static final String DIR_TRANSFER_AUDIO = "audio";
    private static final String DIR_TRANSFER_PICTURE = "image";
    private static final String DIR_TRANSFER_AVATAR = "tmp_avatar";
    private static final String DIR_PICTURE = "picture";
    private static final String DIR_DOCUMENT = "document";
    private static final String DIR_DOWNLOAD = "download";
    private static final String DIR_DOWNLOAD_VIDEO = "video";
    private static final String DIR_DOWNLOAD_AUDIO = "music";
    private static final String DIR_DOWNLOAD_IMAGE = "image";
    private static final String DIR_DOWNLOAD_FILES = "files";
    private static final String DIR_CACHE = "cache";
    private static final String DIR_CACHE_VIDEO = "video";
    private static final String DIR_CACHE_IMAGE = "image";
    private static final String DIR_UPLOAD = "upload";
    private static final String DIR_UPLOAD_AUDIO = "audio";

    // 定义文件名
    private final String FILE_NAME_LOCATION = "sl";

    // 定义空间不足的几种场景
    public enum StorageOutType {
        ENOUGH,     // 空间充足
        OUT,        // 所有存储均空间不足
        OUT_HAS_SD_CARD  // 内部存储空间不足，但是SD卡空间充足，可以转移到SD卡
    }

    /**
     * 初始化方法
     */
    public static void init(Context context) {
        if (sInstance == null) {
            synchronized (StorageManager.class) {
                if (sInstance == null) {
                    sInstance = new StorageManager(context);
                }
            }
        }
    }

    private StorageManager(Context context) {
        this.mContext = context;
        this.mPackageName = mContext.getPackageName();
        this.mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        this.mInternalRootPath = getStoragePath(mContext, false);
        this.mExternalRootPath = getStoragePath(mContext, true);
        DIR_APP_ROOT = resolveApplicationName(context);
        createExternalGodapDir(context);
        Log.i(TAG, "StorageManager constructor: AppRoot: " + DIR_APP_ROOT + ", internalPath: " + mInternalRootPath + ", externalPath: " + mExternalRootPath);

        if (!hasSetStorageLocation()) {
            if (hasInternalStorage() && !hasExternalStorage()) {
                setInternalStorageAsDefault();
            } else if (!hasInternalStorage() && hasExternalStorage()) {
                setExternalStorageAsDefault();
            } else {
                setExternalStorageAsDefault();
            }
        }
    }

    /**
     * 创建GoDap外部存储设备的根目录
     */
    private void createExternalGodapDir(Context context) {
        File dir = new File(getExternalGoDapDirPath());
        if (dir == null) {
            Log.i(TAG, "external cache dir is null!");
        } else {
            Log.i(TAG, "create godap dir: " + dir.getAbsolutePath());
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    /**
     * 获取实例方法
     */
    public static StorageManager getInstance() {
        return sInstance;
    }

    /**
     * 判断用户是否已经设置过有效的默认存储位置，要确保如下两个逻辑：
     * 1. 用户已经选择了默认的存储位置
     * 2. 用户选择的默认存储位置当前不可用
     */
    public boolean hasSetStorageLocation() {
        String currentLocation = getCurrentDefaultStorageLocationConfig();
        // 如果还没有选择存储位置，则返回false
        if (TextUtils.isEmpty(currentLocation)) {
            return false;
        }

        // 如果选择的存储位置不可用，则返回false
        if (currentLocation.equals(SP_VALUE_STORAGE_INTERNAL)) {
            if (TextUtils.isEmpty(mInternalRootPath) || getTotalInternalSpace() == 0) {
                return false;
            }
        }
        if (currentLocation.equals(SP_VALUE_STORAGE_EXTERNAL)) {
            if (TextUtils.isEmpty(mExternalRootPath) || getTotalExternalSpace() == 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将内部存储设置为默认存储位置
     */
    public void setInternalStorageAsDefault() {
        setStorageLocation(SP_VALUE_STORAGE_INTERNAL);
    }

    /**
     * 将外部存储设置为默认存储位置
     */
    public void setExternalStorageAsDefault() {
        setStorageLocation(SP_VALUE_STORAGE_EXTERNAL);
    }

    /**
     * 获取当前默认存储位置的剩余存储空间，单位为字节
     */
    public long getDefaultStorageAvailableBytes() {
        String currentLocation = getCurrentDefaultStorageLocationConfig();
        switch (currentLocation) {
            case SP_VALUE_STORAGE_INTERNAL: {
                return getAvailableInternalSpace();
            }
            case SP_VALUE_STORAGE_EXTERNAL: {
                return getAvailableExternalSpace();
            }
        }
        return 0;
    }

    /**
     * 获取当前默认存储位置的总存储空间，单位为字节
     */
    public long getDefaultStorageTotalBytes() {
        String currentLocation = getCurrentDefaultStorageLocationConfig();
        switch (currentLocation) {
            case SP_VALUE_STORAGE_INTERNAL: {
                return getTotalInternalSpace();
            }
            case SP_VALUE_STORAGE_EXTERNAL: {
                return getTotalExternalSpace();
            }
        }
        return 0;
    }

    /**
     * 获取当前默认存储位置的配置
     * 1. 优先从本地配置文件读取
     * 2. 如果本地配置文件不存在，则从SharePreference中读取
     */
    public String getCurrentDefaultStorageLocationConfig() {
        String configFromFile = readFromFile(getStorageLocationConfigFilePath());
        if (!TextUtils.isEmpty(configFromFile)) {
            return configFromFile;
        } else {
            String configFromSp = mSharedPref.getString(SP_KEY_STORAGE_LOCATION, "");
            if (!TextUtils.isEmpty(configFromSp)) {
                setStorageLocation(configFromSp);
                return configFromSp;
            }
        }
        return "";
    }

    /**
     * 判断当前的位置是否是内部存储
     */
    public boolean isCurrentLocationInternal() {
        return SP_VALUE_STORAGE_INTERNAL.equals(getCurrentDefaultStorageLocationConfig());
    }

    /**
     * 判断当前的位置是否是外部存储
     */
    public boolean isCurrentLocationExternal() {
        return SP_VALUE_STORAGE_EXTERNAL.equals(getCurrentDefaultStorageLocationConfig());
    }

    /**
     * 设置当前选择的存储位置
     * @param selectedLocation  当前选择的存储位置，从{@link #SP_VALUE_STORAGE_INTERNAL}, {@link #SP_VALUE_STORAGE_EXTERNAL}中选择
     */
    private void setStorageLocation(String selectedLocation) {
        writeToFile(selectedLocation, getStorageLocationConfigFilePath());
        mSharedPref.edit().putString(SP_KEY_STORAGE_LOCATION, selectedLocation).apply();
    }

    /**
     * 获取存储位置配置文件路径
     */
    private String getStorageLocationConfigFilePath() {
        return getInternalRootPath() + File.separator + DIR_APP_ROOT + File.separator + DIR_CACHE + File.separator + FILE_NAME_LOCATION;
    }

    /**
     * 将contentLength转换为long类型
     * @param contentLengthStr  原始的contentLength字符串
     */
    public long parseContentLength(String contentLengthStr) {
        if (TextUtils.isEmpty(contentLengthStr)) {
            return 0;
        }
        try {
            return Long.parseLong(contentLengthStr);
        } catch (Exception e) {
            Log.w(TAG, "Parse contentLength exception: " + getCrashReport(e));
        }
        return 0;
    }

    /**
     * 获取当前选择存储位置的GoDap根目录路径
     */
    public String getCurrentGodapRootPath() {
        String currentLocation = getCurrentDefaultStorageLocationConfig();
        switch (currentLocation) {
            case SP_VALUE_STORAGE_INTERNAL: {
                return getInternalGoDapDirPath();
            }
            case SP_VALUE_STORAGE_EXTERNAL: {
                return getExternalGoDapDirPath();
            }
        }
        return null;
    }

    /**
     * 获取当前选择存储位置的根目录
     */
    public String getCurrentStorageRootPath() {
        String currentLocation = getCurrentDefaultStorageLocationConfig();
        switch (currentLocation) {
            case SP_VALUE_STORAGE_INTERNAL: {
                return getInternalRootPath();
            }
            case SP_VALUE_STORAGE_EXTERNAL: {
                return getExternalRootPath();
            }
        }
        return null;
    }

    /**
     * 获取内部存储根目录
     */
    private String getInternalRootPath() {
        return mInternalRootPath;
    }

    /**
     * 获取外部存储根目录
     */
    private String getExternalRootPath() {
        return mExternalRootPath;
    }

    /**
     * 获取内部存储GoDap根目录
     */
    public String getInternalGoDapDirPath() {
        return mInternalRootPath + File.separator + DIR_APP_ROOT + File.separator;
    }

    /**
     * 获取外部存储GoDap根目录
     * 说明：Android 4.4以上，对于SD来说，APP只对Android/data/package-name路径下具有读写权限，因此不能像内部存储那样创建GoDap文件夹
     */
    public String getExternalGoDapDirPath() {
        return mExternalRootPath + File.separator + "Android" + File.separator + "data" + File.separator + mPackageName + File.separator;
    }

    /**
     * 获取GoDap传输一级目录路径
     */
    public String getTransferDirPath() {
        return getCurrentGodapRootPath() + DIR_TRANSFER + File.separator;
    }

    /**
     * 获取GoDap视频传输二级目录路径
     */
    public String getTransferVideoDirPath() {
        return getTransferDirPath() + DIR_TRANSFER_VIDEO + File.separator;
    }

    /**
     * 获取GoDap音频传输二级目录路径
     */
    public String getTransferAudioDirPath() {
        return getTransferDirPath() + DIR_TRANSFER_AUDIO + File.separator;
    }

    /**
     * 获取GoDap图片传输二级目录路径
     */
    public String getTransferPictureDirPath() {
        return getTransferDirPath() + DIR_TRANSFER_PICTURE + File.separator;
    }

    /**
     * 获取GoDap传输所用用户头像的二级目录路径
     */
    public String getTransferAvatarDirPath() {
        return getTransferDirPath() + DIR_TRANSFER_AVATAR + File.separator;
    }

    /**
     * 获取GoDap图片一级目录路径
     */
    public String getImageDirPath() {
        return getCurrentGodapRootPath() + DIR_PICTURE + File.separator;
    }

    /**
     * 获取GoDap文档一级目录路径
     */
    public String getDocumentDirPath() {
        return getCurrentGodapRootPath() + DIR_DOCUMENT + File.separator;
    }

    /**
     * 获取GoDap下载一级目录路径
     */
    public String getDownloadDirPath() {
        return getCurrentGodapRootPath() + DIR_DOWNLOAD + File.separator;
    }

    /**
     * 获取GoDap视频下载二级目录路径
     */
    public String getDownloadVideoDirPath() {
        return getDownloadDirPath() + DIR_DOWNLOAD_VIDEO + File.separator;
    }

    /**
     * 获取GoDap音频下载二级目录路径
     */
    public String getDownloadAudioDirPath() {
        return getDownloadDirPath() + DIR_DOWNLOAD_AUDIO + File.separator;
    }

    /**
     * 获取GoDap图片下载二级目录路径
     */
    public String getDownloadImageDirPath() {
        return getDownloadDirPath() + DIR_DOWNLOAD_IMAGE + File.separator;
    }

    /**
     * 获取GoDap文件下载二级目录路径
     */
    public String getDownloadFilesDirPath() {
        return getDownloadDirPath() + DIR_DOWNLOAD_FILES + File.separator;
    }

    /**
     * 获取GoDap缓存一级目录路径
     */
    public String getCacheDirPath() {
        return getCurrentGodapRootPath() + DIR_CACHE + File.separator;
    }

    /**
     * 获取GoDap视频缓存二级目录路径
     */
    public String getCacheVideoDirPath() {
        return getCacheDirPath() + DIR_CACHE_VIDEO + File.separator;
    }

    /**
     * 获取GoDap图片缓存二级目录路径
     */
    public String getCacheImageDirPath() {
        return getCacheDirPath() + DIR_CACHE_IMAGE + File.separator;
    }

    /**
     * 获取GoDap上传一级目录路径
     */
    public String getPcToPhoneDirPath() {
        return getCurrentGodapRootPath() + DIR_UPLOAD + File.separator;
    }

    /**
     * 获取GoDap上传二级音乐目录路径
     */
    public String getPcToPhoneAudioDirPath() {
        return getPcToPhoneDirPath() + DIR_UPLOAD_AUDIO + File.separator;
    }


    public String getCameraPath(){
       return new StringBuffer( Environment
               .getExternalStorageDirectory().getAbsolutePath()).append(File.separator).append("DCIM").append(File.separator).append("Camera")
                .append(File.separator).toString();
    }


    /**
     * 获取内部存储传输文件夹路径
     */
    public String getInternalTranferDirPath() {
        return hasInternalStorage() ? (getInternalGoDapDirPath() + DIR_TRANSFER + File.separator) : "";
    }

    /**
     * 获取外部存储传输文件夹路径
     */
    public String getExternalGoDapTranferDirPath() {
        return hasExternalStorage() ? (getExternalGoDapDirPath() + DIR_TRANSFER + File.separator) : "";
    }

    /**
     * 获取内部存储备份文件夹路径
     */
    public String getInternalGodapUploadDirPath() {
        return hasInternalStorage() ? (getInternalGoDapDirPath() + DIR_UPLOAD + File.separator) : "";
    }

    /**
     * 获取外部存储备份文件夹路径
     */
    public String getExternalGoDapUploadDirPath() {
        return hasExternalStorage() ? (getExternalGoDapDirPath() + DIR_UPLOAD + File.separator) : "";
    }

    /**
     * 判断设备是否有内部存储
     */
    public boolean hasInternalStorage() {
        return !TextUtils.isEmpty(mInternalRootPath);
    }

    /**
     * 判断设备是否有外部存储，有外部存储的两个条件：
     * 1. 外部存储路径不为空
     * 2. 外部存储总容量大于0
     */
    public boolean hasExternalStorage() {
        if (TextUtils.isEmpty(mExternalRootPath)) {
            return false;
        }
        if (getAvailableExternalSpace() <= 0) {
            return false;
        }
        return true;
    }

    /**
     * 获取设备内部存储剩余空间，单位为字节
     */
    public long getAvailableInternalSpace() {
        try {
            File path = new File(mInternalRootPath);
            StatFs stat = new StatFs(path.getPath());
            int blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return blockSize * availableBlocks;
        } catch (Exception e) {
            Log.w(TAG, "get available internal space exception: " + getCrashReport(e));
        }
        return 0;
    }

    /**
     * 获取设备内部存储总空间，单位为字节
     */
    public long getTotalInternalSpace() {
        try {
            File path = new File(mInternalRootPath);
            StatFs stat = new StatFs(path.getPath());
            int blockSize = stat.getBlockSize();
            long blockCount = stat.getBlockCount();
            return blockSize * blockCount;
        } catch (Exception e) {
            Log.w(TAG, "get total internal space exception: " + getCrashReport(e));
        }
        return 0;
    }

    /**
     * 获取设备外部存储剩余空间，单位为字节
     */
    public long getAvailableExternalSpace() {
        try {
            File path = new File(mExternalRootPath);
            StatFs stat = new StatFs(path.getPath());
            int blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return blockSize * availableBlocks;
        } catch (Exception e) {
            Log.w(TAG, "get available external space exception: " + getCrashReport(e));
        }
        return 0;
    }

    /**
     * 获取设备外部存储总空间，单位为字节
     */
    public long getTotalExternalSpace() {
        try {
            File path = new File(mExternalRootPath);
            StatFs stat = new StatFs(path.getPath());
            int blockSize = stat.getBlockSize();
            long blockCount = stat.getBlockCount();
            return blockSize * blockCount;
        } catch (Exception e) {
            Log.w(TAG, "get total external space exception: " + getCrashReport(e));
        }
        return 0;
    }

    /**
     * 获取设备存储根目录
     * @param needRemovable 是否可移除。true表示内部存储，false表示外部存储
     */
    private String getStoragePath(Context mContext, boolean needRemovable) {
        android.os.storage.StorageManager mStorageManager = (android.os.storage.StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
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
                if (needRemovable == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "get storage path exception: " + getCrashReport(e));
        }
        return null;
    }

    /**
     * 读取文件内容
     * @param filePath 文件路径
     */
    private String readFromFile(String filePath) {
        Log.v(TAG, "Read from file: " + filePath);
        File file = new File(filePath);

        int length = (int) file.length();
        byte[] bytes = new byte[length];

        FileInputStream in;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        } catch (Exception e) {
            Log.v(TAG, "Read from file Exception. " + getCrashReport(e));
        }
        return new String(bytes);
    }

    /**
     * 将内容写到目标文件
     * @param content   要写的内容
     * @param filePath  文件路径
     */
    private void writeToFile(String content, String filePath) {
        File file = new File(filePath);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Write to file exception: " + getCrashReport(e));
        }
    }

    /**
     * 获取异常的日志信息
     * @param ex 异常
     */
    private String getCrashReport(Throwable ex) {
        StringBuffer exceptionStr = new StringBuffer();
        exceptionStr.append("Exception: " + ex.getMessage() + "\n");
        StackTraceElement[] elements = ex.getStackTrace();
        for (StackTraceElement element : elements) {
            exceptionStr.append(element.toString() + "\n");
        }
        return exceptionStr.toString();
    }

    /**
     * 从Context解析APP名称
     */
    private static String resolveApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

}
