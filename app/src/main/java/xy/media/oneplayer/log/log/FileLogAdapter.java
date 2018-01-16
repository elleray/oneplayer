package xy.media.oneplayer.log.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class FileLogAdapter extends AndroidLogAdapter {

    @Override
    public void d(String tag, String message) {
        super.d(tag, message);
        loggingToFile("D", tag, message);
    }

    @Override
    public void e(String tag, String message) {
        super.d(tag, message);
        loggingToFile("E", tag, message);
    }

    @Override
    public void w(String tag, String message) {
        super.w(tag, message);
        loggingToFile("W", tag, message);
    }

    @Override
    public void i(String tag, String message) {
        super.i(tag, message);
        loggingToFile("I", tag, message);
    }

    @Override
    public void v(String tag, String message) {
        super.v(tag, message);
        loggingToFile("V", tag, message);
    }

    @Override
    public void wtf(String tag, String message) {
        super.wtf(tag, message);
    }


    public static void loggingToFile(String level, String tag, String message) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentTime = sdf.format(new java.util.Date());
        String logFile = Settings.getFilePrefix() + "_" + currentTime.split(" ")[0] + Settings.SUFFIX;

        File file = new File(Settings.getLogPath(), logFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // BufferedWriter for performance, true to set append to file flag
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter buf = new BufferedWriter(fileWriter);
            buf.append(currentTime + " ");
            buf.append(level + "/");
            buf.append(tag);
            buf.append("  ");
            buf.append(message);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
