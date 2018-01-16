package xy.media.oneplayer.util;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by Anchorer on 2016/10/27.
 */

public class CommonUtil {

    /**
     * 获取异常的日志信息
     *
     * @param ex 异常
     */
    public static String getCrashReport(Throwable ex) {
        StringBuffer exceptionStr = new StringBuffer();
        exceptionStr.append("Exception: " + ex.getMessage() + "\n");
        StackTraceElement[] elements = ex.getStackTrace();
        for (int i = 0; i < elements.length; i++) {
            exceptionStr.append(elements[i].toString() + "\n");
        }
        return exceptionStr.toString();
    }

    public static boolean isRunningBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (currentPackageName != null && currentPackageName.equals(context.getPackageName())) {
            return false;
        }
        return true;
    }

    /**
     * 获取APP VERSION
     */

    public static String getAppVersion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", "Get App Version Exception. " + getCrashReport(e));
        }
        if (packageInfo == null) {
            return null;
        }
        return packageInfo.versionName;
    }
}
