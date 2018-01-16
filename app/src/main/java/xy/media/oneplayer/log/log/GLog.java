package xy.media.oneplayer.log.log;


/**
 * Created by tong on 2017/3/20.
 */

public class GLog {

    private GLog(Builder builder) {
    }


    public static void iTag(String tag, String message, Object... args) {
        Logger.iTag(tag, message, args);
    }

    public static void i(String message, Object... args) {
        Logger.i(message, args);
    }

    public static void vTag(String tag, String message, Object... args) {
        Logger.vTag(tag, message, args);
    }

    public static void v(String message, Object... args) {
        Logger.v(message, args);
    }

    public static void dTag(String tag, String message, Object... args) {
        Logger.dTag(tag, message, args);
    }

    public static void d(String message, Object... args) {
        Logger.d(message, args);
    }

    public static void wTag(String tag, String message, Object... args) {
        Logger.wTag(tag, message, args);
    }

    public static void w(String message, Object... args) {
        Logger.w(message, args);
    }

    public static void eTag(String tag, String message, Object... args) {
        Logger.eTag(tag, message, args);
    }

    public static void e(String message, Object... args) {
        Logger.e(message, args);
    }

    public static void wtfTag(String tag, String message, Object... args) {
        Logger.wtfTag(tag, message, args);
    }

    public static void wtf(String message, Object... args) {
        Logger.wtf(message, args);
    }

    public static void json(String json) {
        Logger.json(json);
    }

    public static void xml(String xml) {
        Logger.xml(xml);
    }

    public static void obj(Object obj) {
        Logger.obj(obj);
    }

    public static void json(String tag, String json) {
        Logger.json(tag, json);
    }

    public static void xml(String tag, String xml) {
        Logger.xml(tag, xml);
    }

    public static void obj(String tag, Object obj) {
        Logger.obj(tag, obj);
    }


    public static final class Builder {
        private String filePrefix = Settings.PREFIX;
        private int methodCount = 1;
        private int methodOffset = 1;
        private String logPath = Settings.DEFAULT_LOG_PATH;
        private LogAdapter logAdapter = null;
        private String Tag = "GLog";

        public Builder() {
        }

        public Builder filePrefix(String filePrefix) {
            this.filePrefix = filePrefix;
            return this;
        }

        public Builder methodCount(int methodCount) {
            this.methodCount = methodCount;
            return this;
        }

        public Builder methodOffset(int methodOffset) {
            this.methodOffset = methodOffset;
            return this;
        }

        public Builder logPath(String logPath) {
            this.logPath = logPath;
            return this;
        }

        public Builder logAdapter(LogAdapter logAdapter) {
            this.logAdapter = logAdapter;
            return this;
        }

        public Builder Tag(String Tag) {
            this.Tag = Tag;
            return this;
        }

        public GLog build() {
            Logger.init(Tag)
                    .logAdapter(logAdapter)
                    .methodCount(methodCount)
                    .methodOffset(methodOffset)
                    .logPath(logPath)
                    .filePrefix(filePrefix);
            return new GLog(this);
        }
    }
}
