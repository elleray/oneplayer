package xy.media.oneplayer.log.log;

public interface Printer {

    Printer t(String tag, int methodCount);

    Settings init(String tag);

    Settings getSettings();

    void d(String message, Object... args);

    void e(String message, Object... args);

    void e(Throwable throwable, String message, Object... args);

    void w(String message, Object... args);

    void i(String message, Object... args);

    void v(String message, Object... args);

    void wtf(String message, Object... args);

    void obj(Object object);

    void json(String json);

    void xml(String xml);

    void tagD(String tag, String message, Object... args);



    void tagE(String tag, String message, Object... args);

    void tagE(String tag, Throwable throwable, String message, Object... args);

    void tagW(String tag, String message, Object... args);

    void tagI(String tag, String message, Object... args);

    void tagV(String tag, String message, Object... args);

    void tagWtf(String tag, String message, Object... args);

    void obj(String tag, Object object);

    void json(String tag, String json);

    void xml(String tag, String xml);

    void log(int priority, String tag, String message, Throwable throwable);

    void resetSettings();

}
