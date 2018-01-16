package xy.media.oneplayer.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;

import java.io.File;
import java.util.List;

import xy.media.oneplayer.gl.Global;
import xy.media.oneplayer.log.log.GLog;
import xy.media.oneplayer.manager.StorageManager;

/**
 * Created by elleray on 16/6/30.
 */
public class GDApplication extends MultiDexApplication  {
    private static final String KEY_INNER_CHANNEL_FLAG = "key_inner_channel_flag";
    private static GDApplication context;
    private boolean isInnerChannel;
    private static boolean isInit = false;
    private Activity currentActivity;
    private ServiceConnection mFloatingWindowConn;

    @Override
    public void onCreate() {
        super.onCreate();
        String currentProcessName = getCurProcessName();
        GLog.i("Current process name: " + currentProcessName);

        context = this;
        Global.context  = context;


        initLog();

        GLog.i("GDApplication onCreate() start.");


        // GA
//        GATracker.init(this, !BuildConfig.RELEASE, R.xml.global_tracker, CommonUtil.getAppVersion(this));

        // 存储管理
        StorageManager.init(this);


        //初始化网络请求
        initNetwork();
    }


    /**
     * 初始化Application的代码
     * 注意，该方法运行在非UI线程。如果初始化的代码必须在UI线程初始化，请将代码写在onCreate()方法内。
     */
    public void initApplication() {
        if (isInit) {
            GLog.w("GDApplication has init");
            return;
        }

        isInit = true;
        GLog.i("GDApplication initApplication(). start.");

//        AppEventsLogger.activateApp(this);

        GLog.i("GDApplication initApplication(). end.");
    }

    /**
     * 初始化Log工具，该路径根据APP名称来定
     */
    private void initLog() {
        new GLog.Builder()
                .logPath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + resolveApplicationName() + File.separator + "log" + File.separator)
                .filePrefix("log")
                .build();
    }

    public boolean isInnerChannel() {
        return isInnerChannel;
    }

    /**
     * 获取当前进程名称
     */
    protected String getCurProcessName() {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfoList = activityManager.getRunningAppProcesses();
        // getRunningAppProcesses可能返回null，因此需要增加null的判断
        if (processInfoList != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : processInfoList) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        }
        return "";
    }

    private void initConnection() {
    }

    private void initNetwork() {
    }

    public static GDApplication getContext() {
        return context;
    }



    /**
     * 解析APP名称
     */
    protected String resolveApplicationName() {
        ApplicationInfo applicationInfo = getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }
}
