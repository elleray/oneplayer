package xy.media.oneplayer.log.log;

/**
 * Logger is a wrapper of {@link android.util.Log}
 * But more pretty, simple and powerful
 */
public final class Logger {
    public static final int DEBUG = 3;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;
    public static final int INFO = 4;
    public static final int VERBOSE = 2;
    public static final int WARN = 5;

    private static final String DEFAULT_TAG = "Logger";

    private static Printer printer = new LoggerPrinter();

    //no instance
    private Logger() {
    }

    /**
     * It is used to get the settings object in order to change settings
     *
     * @return the settings object
     */
    public static Settings init() {
        return init(DEFAULT_TAG);
    }

    /**
     * It is used to change the tag
     *
     * @param tag is the given string which will be used in Logger as TAG
     */
    public static Settings init(String tag) {
        printer = new LoggerPrinter();
        return printer.init(tag);
    }

    public static void resetSettings() {
        printer.resetSettings();
    }

    public static Printer t(String tag) {
        return printer.t(tag, printer.getSettings().getMethodCount());
    }

    public static Printer t(int methodCount) {
        return printer.t(null, methodCount);
    }

    public static Printer t(String tag, int methodCount) {
        return printer.t(tag, methodCount);
    }

    public static void log(int priority, String tag, String message, Throwable throwable) {
        printer.log(priority, tag, message, throwable);
    }

    public static void d(String message, Object... args) {
        printer.d(message, args);
    }

    public static void obj(Object object) {
        printer.obj(object);
    }

    public static void e(String message, Object... args) {
        printer.e(null, message, args);
    }

    public static void e(Throwable throwable, String message, Object... args) {
        printer.e(throwable, message, args);
    }

    public static void i(String message, Object... args) {
        printer.i(message, args);
    }

    public static void v(String message, Object... args) {
        printer.v(message, args);
    }

    public static void w(String message, Object... args) {
        printer.w(message, args);
    }

    public static void wtf(String message, Object... args) {
        printer.wtf(message, args);
    }


    public static void dTag(String tag, String message, Object... args) {
        printer.tagD(tag, message, args);
    }

    public static void eTag(String tag, String message, Object... args) {
        printer.tagE(tag, null, message, args);
    }

    public static void tTag(String tag, Throwable throwable, String message, Object... args) {
        printer.tagE(tag, throwable, message, args);
    }

    public static void iTag(String tag, String message, Object... args) {
        printer.tagI(tag, message, args);
    }

    public static void vTag(String tag, String message, Object... args) {
        printer.tagV(tag, message, args);
    }

    public static void wTag(String tag, String message, Object... args) {
        printer.tagW(tag, message, args);
    }

    public static void wtfTag(String tag, String message, Object... args) {
        printer.tagWtf(tag, message, args);
    }

    public static void obj(String tag, Object object) {
        printer.obj(tag, object);
    }


    /**
     * Formats the json content and print it
     *
     * @param json the json content
     */
    public static void json(String json) {
        printer.json(json);
    }

    public static void json(String tag, String json) {
        printer.json(tag, json);
    }

    /**
     * Formats the json content and print it
     *
     * @param xml the xml content
     */
    public static void xml(String xml) {
        printer.xml(xml);
    }

    public static void xml(String tag, String xml) {
        printer.xml(tag, xml);
    }

}
