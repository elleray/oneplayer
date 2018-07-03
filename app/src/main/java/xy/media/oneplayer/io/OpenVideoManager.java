package xy.media.oneplayer.io;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xy.media.oneplayer.data.greendao.BaseFile;
import xy.media.oneplayer.gl.Const;
import xy.media.oneplayer.util.FileUtil;
import xy.media.oneplayer.videoplayer.SimplePlayer;
import xy.media.oneplayer.videoplayer.VideoPlayerActivity;

import static xy.media.oneplayer.io.VideoPlayedModel.TYPE_OF_PLAYED_VIDEO_LOCAL;


/**
 * Created by tony on 2017/12/19.
 */

public class OpenVideoManager {
    public static final int OPEN_TYPE_LIBRARY_MODE = 0;
    public static final int OPEN_TYPE_SHARE_MODE = 1;
    public static final int OPEN_TYPE_SIMPLE_MODE = 2;
    public static final int OPEN_TYPE_NO_CHAT_MODE = 3;

    public static final int OPEN_TYPE_DEFAULT_MODE = OPEN_TYPE_LIBRARY_MODE;

    public static final long MIN_OPEN_VIDEO_SIZE = 2 * 1024 * 1024;

    /**
     * library内视频播放
     * @param file
     * @param list
     */
    public static void open(Activity activity, BaseFile file, final boolean isSupportPlaylist, ArrayList<BaseFile> list) {
        ArrayList<BaseFile> tmp = new ArrayList<>();
        if (!isSupportPlaylist || list == null || !isContain(file, list)) {
            tmp.add(file);
        } else {
            for (BaseFile item : list){
                if (FileUtil.isVideo(item)) {
                    tmp.add(item);
                }
            }
        }

        ArrayList<VideoPlayedModel> playlist = new ArrayList<>();
        int index = 0;
        int pos = -1;
        for (BaseFile baseFile : tmp) {
            VideoPlayedModel model = new VideoPlayedModel(0, baseFile.getPath());
            model.setType(TYPE_OF_PLAYED_VIDEO_LOCAL);
            playlist.add(model);

            if(baseFile.getPath().equals(file.getPath())){
                pos = index;
            }
            index ++;
        }

        openVideo(activity, pos,playlist, OPEN_TYPE_LIBRARY_MODE, false);
    }

    private static boolean isContain(BaseFile file, ArrayList<BaseFile> list){
        if (file == null || list == null) {
            return false;
        }

        for (BaseFile item : list) {
            if (item.getPath().equals(file.getPath())) {
                return true;
            }
        }
        return false;
    }


    private static void openVideo(Activity activity, int videoPosition,
                                  ArrayList<VideoPlayedModel> videoList,
                                  int openType,
                                  boolean isMineShare) {

//        Intent intent1 = new Intent();
//        intent1.setClass(activity, VideoPlayerActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("video_list", videoList);
//        bundle.putInt("video_pos", videoPosition);
//        bundle.putInt("open_type", openType);
//        bundle.putBoolean("is_mine_share", isMineShare);
//        intent1.putExtras(bundle);
//        activity.startActivityForResult(intent1, Const.REQUEST_CODE.VIDEO_BROWSE);

        SimplePlayer.start(activity, videoList.get(videoPosition).getFilePath());
    }

}
