package xy.media.oneplayer.videoplayer;

import android.app.Activity;
import android.content.Intent;

import java.util.ArrayList;

import xy.media.oneplayer.io.VideoPlayedModel;
import xy.media.oneplayer.player.subtitles.SubtitlesModel;

/**
 * Created by tony on 2017/12/18.
 */

public class VideoPlayerContract {
    interface View{
        void setTitleView(String title);
        void setPresenter(Presenter presenter);
        void initView(VideoPlayerDataHelper data);
        void setView(VideoPlayerDataHelper data);
        void openMuteView();
        void closeMuteView();

        void setVideoLikeCount(int count);
        void setShareCommentCount(int count);
        void setDownloadBtn(VideoPlayerDataHelper dataHelper);
        void playLocalVideo(String path);
        void initDownloadView(float downloadPercent, int duration);
        void setNextBtnView(boolean isPlaylist);

        void showController();
        void hideController();
        void showVideoCacheLoading();

        void hideWaitingView();
        void pause();
        void setSubtitleData(ArrayList<SubtitlesModel> list);
    }

    public interface Presenter{
        void setTitleView();
        void start(Intent intent);
        void openMute();
        void closeMute();
        void setCurrentVideoDuration(int duration);
        void saveRecentPlayVideo();

        void refreshVideoLikeCountAndCommendCount();
        void downloadVideo(Activity activity);
        boolean isAlreadyDownload();
        void startPlay();
        void play(VideoPlayedModel model);
        void playOnDelay(VideoPlayedModel model, long delayMillis, Activity activity);
        boolean playNext(boolean isAutoPlay, Activity activity);
        void replay();
        void unSubscription();
        void loadSubtitles();
    }
}
