package xy.media.oneplayer.log.log;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

public final class Settings {

    public final static String DEFAULT_LOG_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "godap" + File.separator + "log"+ File.separator;

    public final static String PREFIX = "log";
    public final static String SUFFIX = ".txt";
    private int methodCount = 2;
    private boolean showThreadInfo = true;
    private int methodOffset = 0;
    private static String logPath = DEFAULT_LOG_PATH;
    private static String filePrefix = PREFIX;
    private LogAdapter logAdapter;

    /**
     * Determines to how logs will be printed
     */
    private LogLevel logLevel = LogLevel.FULL;


    public Settings hideThreadInfo() {
        showThreadInfo = false;
        return this;
    }

    public Settings methodCount(int methodCount) {
        if (methodCount < 0) {
            methodCount = 0;
        }
        this.methodCount = methodCount;
        return this;
    }

    public Settings logLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public Settings methodOffset(int offset) {
        this.methodOffset = offset;
        return this;
    }

    public Settings logAdapter(LogAdapter logAdapter) {
        this.logAdapter = logAdapter;
        if (this.logAdapter == null) {
            this.logAdapter = new FileLogAdapter();
        }
        return this;
    }

    public Settings logPath(String logPath) {
        this.logPath = logPath;

        File dir = new File(logPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return this;
    }

    public Settings filePrefix(String filePrefix) {
        if (TextUtils.isEmpty(filePrefix)) {
            return this;
        }
        this.filePrefix = filePrefix;
        return this;
    }


    public int getMethodCount() {
        return methodCount;
    }

    public boolean isShowThreadInfo() {
        return showThreadInfo;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public int getMethodOffset() {
        return methodOffset;
    }

    public static String getLogPath() {
        return logPath;
    }

    public static String getFilePrefix() {
        return filePrefix;
    }

    public LogAdapter getLogAdapter() {
        if (logAdapter == null) {
            logAdapter = new AndroidLogAdapter();
        }
        return logAdapter;
    }

    public void reset() {
        methodCount = 2;
        methodOffset = 0;
        showThreadInfo = true;
        logLevel = LogLevel.FULL;
    }
}
