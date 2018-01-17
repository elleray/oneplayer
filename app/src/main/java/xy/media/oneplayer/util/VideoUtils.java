package xy.media.oneplayer.util;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import xy.media.oneplayer.data.model.VideoInfo;
import xy.media.oneplayer.gl.Global;
import xy.media.oneplayer.log.log.GLog;

/**
 * Created by Administrator on 2017/6/14 0014.
 */

public class VideoUtils {

    public static final String LOCAL_VIDEO_ALBUM = "相册";
    public final static String DEFAULT_CAMERA = "Camera";


    private static final String SCREENRECODER = "ScreenRecorder";

    private static final long MINI_DURATION_IF_HAVE_COPYRIGHT = 5 * 60 * 1000;
    /**
     * 生成表示下载速度的字符串
     *
     * @param speed 下载速度,单位为B/s
     */
    public static String generateDownloadSpeedStr(float speed) {
        if (speed < 1024) {
            return (int) speed + "B/s";
        } else if (speed < 1024 * 1024L) {
            return formatDouble(speed / 1024, 1) + "KB/s";
        }
        return formatDouble(speed / (1024 * 1024L), 1) + "MB/s";
    }

    /**
     * 对小数类型，保留小数点后指定位数的小数
     * @param value 原始小数
     * @param scale 保留位数
     */
    public static double formatDouble(double value, int scale) {
        BigDecimal b = new BigDecimal(value);
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static boolean isInAlbum(VideoInfo i){
        return i.getParentPath().toLowerCase().contains(DEFAULT_CAMERA.toLowerCase())
                || i.getParentPath().toLowerCase().contains(SCREENRECODER.toLowerCase());
    }

    public static boolean isInAlbum(String path){
        return path.toLowerCase().contains(DEFAULT_CAMERA.toLowerCase())
                ||path.toLowerCase().contains(SCREENRECODER.toLowerCase());
    }

    public static Bitmap getFrame(VideoInfo data){
        String filePath = data.getPath();
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();

            FileInputStream inputStream = new FileInputStream(filePath);
            FileDescriptor fileDescriptor = inputStream.getFD();
            mmr.setDataSource(fileDescriptor, 0, inputStream.available());
            final Bitmap frame = mmr.getFrameAtTime();
            return frame;
        }catch (Exception e){
            GLog.d("e= "+ e.toString());
        }

        return null;
    }

    public static boolean hasCopyright(long duration){
        if(duration >= MINI_DURATION_IF_HAVE_COPYRIGHT){
            return true;
        }
        return false;
    }

    public static String getDuration(long time){
        time = time/1000;
        SimpleDateFormat format = new SimpleDateFormat("dd");

        long min = 0;
        long sec = 0;
        if(time/60 > 0 ){
            min = (int) (time/60);
            sec = time - min * 60;
        } else {
            min = 0;
            sec = time;
        }

        String minu  = String.format("%s", min + "");
        if(minu.length() < 2){
            minu = "0" + minu;
        }
        String seco = String.format("%s", sec + "");
        if(seco.length() < 2){
            seco = "0" + seco;
        }
        return  minu + " : " + seco;
    }

    public static String getShortDuration(long time){
        time = time/1000;
        SimpleDateFormat format = new SimpleDateFormat("dd");

        long min = 0;
        long sec = 0;
        if(time/60 > 0 ){
            min = (int) (time/60);
            sec = time - min * 60;
        } else {
            min = 0;
            sec = time;
        }

        String minu  = String.format("%s", min + "");
        if(minu.length() < 2){
            minu = "0" + minu;
        }
        String seco = String.format("%s", sec + "");
        if(seco.length() < 2){
            seco = "0" + seco;
        }

        return minu + ":" + seco;
    }

}
