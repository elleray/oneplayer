package xy.media.oneplayer.listener;

/**
 * Created by elleray on 2017/1/10.
 */

public interface OperaListener {
    void onFinish();
    void onCancel();
    void onStart();
    void onFailed(String msg);
}
