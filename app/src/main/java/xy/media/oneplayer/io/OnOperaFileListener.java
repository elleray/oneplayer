package xy.media.oneplayer.io;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import java.util.ArrayList;

import xy.media.oneplayer.data.greendao.BaseFile;
import xy.media.oneplayer.listener.OperaListener;


/**
 * Created by elleray on 2017/3/23.
 */

public interface OnOperaFileListener {
     void onSend(BaseFile file);
     void onSendList(ArrayList<BaseFile> files, Activity activity);
     void onToDoSomthing(String str, Activity activity);
     void onShare(BaseFile file, Activity activity);
     void onShare(String path, Activity activity);
     void onDelete(BaseFile file, Activity activity);
     void onShowMenu(BaseFile file, boolean isShare, boolean isDelete, Activity activity, View view, OperaListener listener);
     void onShowMenu(String path, boolean isShare, boolean isDelete, Activity activity, View view, OperaListener listener);
     void onShareDetailPage(Activity activity, long shareId);
     void onGoToActivity(Context context, Intent intent);
     void onOpenVideo(BaseFile file, boolean isSupportPlaylist, ArrayList<BaseFile> list, Activity activity);
     void onOpenPicture(BaseFile file, ArrayList<BaseFile> list, Activity activity);
}
