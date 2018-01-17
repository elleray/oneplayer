package xy.media.oneplayer.manager;

import xy.media.oneplayer.io.OnOperaFileListener;
import xy.media.oneplayer.util.FileUtil;

/**
 * Created by elleray on 2017/3/21.
 */

public class FileLibListenerManager {

    public FileUtil.OpenFileCallback mOpenFileListener;

    private OnOperaFileListener mOnOperaFileListener;


    private static FileLibListenerManager instance;

    public FileLibListenerManager(){
    }

    public static FileLibListenerManager getInstance(){
        if(instance == null){
            synchronized (FileLibListenerManager.class){
                if(instance == null){
                    instance = new FileLibListenerManager();
                }
            }
        }
        return instance;
    }

    public FileUtil.OpenFileCallback getmOpenFileListener() {
        return mOpenFileListener;
    }

    public void setmOpenFileListener(FileUtil.OpenFileCallback mOpenFileListener) {
        this.mOpenFileListener = mOpenFileListener;
    }

    public OnOperaFileListener getmOnOperaFileListener() {
        return mOnOperaFileListener;
    }

    public void setmOnOperaFileListener(OnOperaFileListener mOnOperaFileListener) {
        this.mOnOperaFileListener = mOnOperaFileListener;
    }
}
