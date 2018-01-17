package xy.media.oneplayer.util;

/**
 * Created by elleray on 2016/10/19.
 */
public class FastClickUtil {
    private static long lastClickTime;
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
