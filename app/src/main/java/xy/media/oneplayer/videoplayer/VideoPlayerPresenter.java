package xy.media.oneplayer.videoplayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;


import java.util.ArrayList;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import xy.media.oneplayer.VideolistPresenter;
import xy.media.oneplayer.io.VideoPlayedModel;
import xy.media.oneplayer.io.VideoPlayerUpdateHelper;
import xy.media.oneplayer.log.log.GLog;
import xy.media.oneplayer.player.subtitles.SubtitlesModel;
import xy.media.oneplayer.util.FastClickUtil;


/**
 * Created by yangwenjie on 2017/12/22
 */

public class VideoPlayerPresenter implements VideoPlayerContract.Presenter {
    private VideoPlayerContract.View mView;
    private Context mContext;
    private VideoPlayerDataHelper mDataHelper;
    private ProgressDialog mWaitingDialog;
    private Subscription mSubscription;


    public VideoPlayerPresenter(Context context, VideoPlayerContract.View view){
        mContext = context;
        mView = view;
        mDataHelper = VideoPlayerDataHelper.getInstance();
    }

    @Override
    public void start(Intent intent) {
        mDataHelper.initData(intent, mContext);
        mView.initView(mDataHelper);
    }

    @Override
    public void setTitleView() {
        if(mDataHelper.isPlayingVideolist()){
            mView.setTitleView((mDataHelper.getCurrentVideoPosition() + 1) + "/" + mDataHelper.getCountOfPlayList());
        } else {
            mView.setTitleView(
                    mDataHelper.getCurrentVideoName());
        }
    }

    @Override
    public void openMute() {
        mDataHelper.openMute(mContext);
        mView.openMuteView();
    }

    @Override
    public void closeMute() {
        mDataHelper.closeMute(mContext);
        mView.closeMuteView();
    }

    @Override
    public void setCurrentVideoDuration(int duration) {
        mDataHelper.setCurrentVideoDuration(duration);
    }

    @Override
    public void saveRecentPlayVideo() {
        mDataHelper.saveRecentPlayingVideo(mContext);
    }




    @Override
    public void refreshVideoLikeCountAndCommendCount() {
    }

    @Override
    public void downloadVideo(Activity activity) {
        if (isAlreadyDownload()) {
        } else {
            VideoPlayedModel model = mDataHelper.getVideo();
        }
    }




    @Override
    public boolean isAlreadyDownload() {
        VideoPlayedModel video = mDataHelper.getVideo();
        long fileId = video.getFileId();

        if (fileId == 0) {
            return true;
        } else {
        }

        return false;
    }

    @Override
    public void startPlay() {
        if (mDataHelper.getVideo() != null) {
            play(mDataHelper.getVideo());
        } else {
            mView.playLocalVideo(mDataHelper.getCurrentPath());
        }
    }

    @Override
    public void play(VideoPlayedModel model) {
        if (model != null){
            mDataHelper.setVideo(model);
            if (model.isLocal()) {
               mView.playLocalVideo(model.getFilePath());
           } else {
               String cachePath = model.getFilePath();
               mView.playLocalVideo(cachePath);

               if (Math.abs(model.getDownloadPercent() -1) > 0.0001) {
                   VideoPlayerUpdateHelper.updateVideoDownloadProgress(VideoPlayerDataHelper.getInstance().getCurrentPath(), model.getDownloadPercent());
               }
           }
        }
    }

    @Override
    public void playOnDelay(final VideoPlayedModel model, long delayMillis, Activity activity) {
    }

    @Override
    public boolean playNext(final boolean isAutoPlay, final Activity activity) {
        final VideoPlayedModel nextModel = mDataHelper.nextVideo();
        if(nextModel != null) {

            final long fileId = nextModel.getFileId();
            GLog.i("Try open video. fileId: " + fileId + "path = " + nextModel.getFilePath());
            if (nextModel.isLocal()) {
                if (isAutoPlay) {
                    playOnDelay(nextModel, VideoPlayerDataHelper.PLAY_NEXT_VIDEO_DELAY, activity);
                } else {
                    play(nextModel);
                }
            }
        }

        return false;
    }

    @Override
    public void replay() {
        mDataHelper.clearPlayRecord(mDataHelper.getVideo());
        play(mDataHelper.getVideo());
    }

    @Override
    public void unSubscription() {
        if(mSubscription != null){
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void loadSubtitles() {
        rx.Observable.create(new rx.Observable.OnSubscribe<ArrayList<SubtitlesModel>>() {
            @Override
            public void call(Subscriber<? super ArrayList<SubtitlesModel>> subscriber) {
                ArrayList<SubtitlesModel> models = mDataHelper.readSubTitles();
                subscriber.onNext(models);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<SubtitlesModel>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        GLog.e(e.toString());
                    }

                    @Override
                    public void onNext(ArrayList<SubtitlesModel> models) {
                        mView.setSubtitleData(models);
                    }
                });
    }
}
