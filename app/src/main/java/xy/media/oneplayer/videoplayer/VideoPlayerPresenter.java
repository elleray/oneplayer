package xy.media.oneplayer.videoplayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import me.godap.R;
import me.godap.channel.model.FileModel;
import me.godap.channel.modules.download.DownloadManager;
import me.godap.channel.modules.download.DownloadType;
import me.godap.channel.modules.download.FileDownloadListener;
import me.godap.channel.modules.download.FileDownloadTask;
import me.godap.filecenter.io.VideoPlayedModel;
import me.godap.lib.mod.download.model.DownloadTaskStatus;
import me.godap.lib.mod.file.data_helper.VideoBaseDataHelper;
import me.godap.lib.mod.file.io.VideoPlayerUpdateHelper;
import me.godap.lib.mod.file.utils.ToastUtil;
import me.godap.lib.pub.log.GLog;
import me.godap.lib.utils.AppManager;
import me.godap.lib.utils.FastClickUtil;
import me.godap.network.NetWorkApi;
import me.godap.network.callback.NetworkCallback;
import me.godap.network.entity.request.NewLikeFeedReq;
import me.godap.network.entity.response.ApiRes;
import me.godap.network.entity.response.NewFeedResponse;
import me.godap.network.entity.response.ObjectResponse;
import me.godap.network.exception.NetworkException;
import me.godap.ui.dialog.DialogFactory;
import me.godap.ui.dialog.DialogWrapper;
import me.godap.ui.view.progressdialog.ProgressDialogFactory;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static me.godap.filecenter.io.OpenVideoManager.MIN_OPEN_VIDEO_SIZE;

/**
 * Created by yangwenjie on 2017/12/22
 */

public class VideoPlayerPresenter implements VideoPlayerContract.Presenter {
    private VideoPlayerContract.View mView;
    private Context mContext;
    private VideoPlayerDataHelper mDataHelper;
    private ProgressDialog mWaitingDialog;
    private DialogWrapper mDialog;
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

        if(mDataHelper.isShareFile()) {
            refreshVideoLikeCountAndCommendCount();
        }
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
    public void likeTheVideo(final VideoPlayerActivity activity) {
        if(FastClickUtil.isFastClick()) {
            return;
        }

        if (mDataHelper.isShareFile()) {
            long shareId = mDataHelper.getShareId();
            FileModel model = mDataHelper.findFileModelByFileId(mDataHelper.getVideo(), mDataHelper.getFileModelList());
            if (model == null) {
                GLog.d("获得数据异常");
                return;
            }
            boolean isContainMineLike = mDataHelper.containMineLike(model);
            GLog.d("isContainMineLike = " + isContainMineLike);
            if (! isContainMineLike) {
                //如果此时刚好在refreshLikeAndComment(),则应该立即撤销刷新，防止请求的数据过时
                if (mSubscription != null) {
                    GLog.d("取消刷新share的comment和like信息");
                    mSubscription.unsubscribe();
                }

                long fileId = model.getFileId();

                final NewLikeFeedReq req = new NewLikeFeedReq();
                req.setShareId(shareId);
                req.setFileId(fileId);

                activity.showWaitingView(activity.getString(R.string.k270_please_wait));

                NetWorkApi.getInstance().newLikeFeed(req, new NetworkCallback<ObjectResponse<NewFeedResponse>>() {
                    @Override
                    public void onSuccess(final ObjectResponse<NewFeedResponse> result) {
                        if (!checkResult(result)) {
                            return;
                        }

                        //成功点赞，下面刷新share的like和comment信息
                        GLog.d("成功点赞，下面刷新share的like和comment信息");
                        refreshVideoLikeCountAndCommendCount();
                    }

                    @Override
                    public void onError(final NetworkException exception) {
                        activity.hideWaitingView();
                        GLog.d("error code = " + exception.getErrorCode() + ", message = " + exception.getMessage() );
                    }
                });
            } else {
                ToastUtil.show(mContext.getString(R.string.file_center_video_player_already_like));
            }
        }
    }


    private boolean checkResult(final ApiRes result) {
        if (!result.isSuccess()) {
            GLog.d("error code = " + result.getErrCode() + ", message = " + result.getReason() );
            return false;
        }

        return true;
    }


    @Override
    public void refreshVideoLikeCountAndCommendCount() {
        GLog.d("refreshVideoLikeCountAndCommendCount");
        mSubscription = rx.Observable.create(new GetVideoShareModelAction(mDataHelper))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetVideoShareModelObserver(mView));
    }

    @Override
    public void downloadVideo(Activity activity) {
        if (isAlreadyDownload()) {
            showHaveDownloadDialog(activity);
        } else {
            VideoPlayedModel model = mDataHelper.getVideo();
            if(mDataHelper.isShareFile()) {
                long shareId = mDataHelper.getShareId();
                FileModel fileModel = mDataHelper.findFileModelByFileId(model, mDataHelper.getFileModelList());
                if(fileModel != null) {
                    long fileId = fileModel.getFileId();

                    if (shareId != 0 && fileId != 0 ) {
                        GLog.d("开始下载");
                        DownloadManager.getInstance().downloadFile(shareId, fileModel, DownloadType.DOWNLOAD_TYPE_ORIGINAL_FILE, true);
                    }
                }
            }
        }
    }


    private void showHaveDownloadDialog(final Activity activity) {
        mDialog = DialogFactory.createDescNoBgBtn(activity,
                activity.getString(R.string.file_center_video_player_already_download),
                activity.getString(R.string.file_center_dialog_i_know),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });
        mDialog.hideCloseButton();
        mDialog.show();
    }

    @Override
    public boolean isAlreadyDownload() {
        VideoPlayedModel video = mDataHelper.getVideo();
        long fileId = video.getFileId();

        if (fileId == 0) {
            return true;
        } else {
            return DownloadManager.getInstance().hasDownloadedFile(fileId, DownloadType.DOWNLOAD_TYPE_ORIGINAL_FILE);
        }
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
        mView.hideController();

        mWaitingDialog = ProgressDialogFactory.createNextVideoChangeDialog(activity, delayMillis,
                new ProgressDialogFactory.NextVideoWaitingListener() {
            @Override
            public void playNext() {
                if (mWaitingDialog != null && mWaitingDialog.isShowing()) {
                    mWaitingDialog.dismiss();
                }
                mView.showController();
                play(model);
                mView.setView(mDataHelper);

            }

            @Override
            public void replay() {
                if (mWaitingDialog != null && mWaitingDialog.isShowing()) {
                    mWaitingDialog.dismiss();
                }
                VideoPlayerPresenter.this.replay();
            }
        });
        mWaitingDialog.show();
    }

    @Override
    public boolean playNext(final boolean isAutoPlay, final Activity activity) {
        final VideoPlayedModel nextModel = mDataHelper.nextVideo();
        if(nextModel != null){

            final long fileId = nextModel.getFileId();
            GLog.i("Try open video. fileId: " + fileId + "path = " + nextModel.getFilePath());
           if (nextModel.isLocal()){
               if (isAutoPlay) {
                   playOnDelay(nextModel, VideoPlayerDataHelper.PLAY_NEXT_VIDEO_DELAY, activity);
               } else {
                   play(nextModel);
               }
           } else {
                FileDownloadTask fileDownloadTask = DownloadManager.getInstance().queryFileDownloadTask(fileId);
                if (fileDownloadTask != null && fileDownloadTask.getDownloadedBytes() > MIN_OPEN_VIDEO_SIZE) {
                    String filePath = fileDownloadTask.getFilePath();
                    long duration = VideoBaseDataHelper.getInstance().getVideoDuration(filePath);
                    GLog.i("Video is in downloading queue. Open video: " + filePath + ", duration: " + duration);
                    if (duration <= 0) {
                        Toast.makeText(activity, activity.getString(R.string.download_cannot_instant_play), Toast.LENGTH_SHORT).show();
                    } else {
                        nextModel.setDownloadPercent(fileDownloadTask.getPercentage());
                        nextModel.setFilePath(filePath);
                        if (isAutoPlay) {
                            playOnDelay(nextModel, VideoPlayerDataHelper.PLAY_NEXT_VIDEO_DELAY, activity);
                        } else {
                            play(nextModel);
                        }
                    }
                }

               if (fileDownloadTask != null) {
                   if (fileDownloadTask.getDownloadState() != DownloadTaskStatus.DOWNLOADING) {
                       GLog.i("Video not downloading. Start download and btn_music_play.");
                       fileDownloadTask.start();
                   }
               } else {
                   //检查是否有缓存任务
                   FileDownloadTask fileCacheTask = DownloadManager.getInstance().queryFileDownloadTask(fileId, DownloadType.DOWNLOAD_TYPE_VIDEO_CACHE);

                   if (fileCacheTask != null && fileCacheTask.getDownloadedBytes() > MIN_OPEN_VIDEO_SIZE) {
                       String filePath = fileCacheTask.getFilePath();
                       long duration = VideoBaseDataHelper.getInstance().getVideoDuration(filePath);
                       GLog.i("Video is in downloading queue. Open video: " + filePath + ", duration: " + duration);
                       if (duration <= 0) {
                           Toast.makeText(activity, activity.getString(R.string.download_cannot_instant_play), Toast.LENGTH_SHORT).show();
                       } else {
                           nextModel.setDownloadPercent(fileCacheTask.getPercentage());
                           nextModel.setFilePath(filePath);

                           if (isAutoPlay) {
                               playOnDelay(nextModel, VideoPlayerDataHelper.PLAY_NEXT_VIDEO_DELAY, activity);
                           } else {
                               play(nextModel);
                           }
                       }
                   }

                   if (fileCacheTask != null){
                       if (fileCacheTask.getDownloadState() != DownloadTaskStatus.DOWNLOADING) {
                           GLog.i("Video not caching. Start cache and btn_music_play.");
                           fileCacheTask.start();
                       }
                   } else {
                       FileModel fileModel = mDataHelper.findFileModelByFileId(nextModel, mDataHelper.getFileModelList());
                       if(fileModel != null){
                           GLog.i("Video not cache yet. Start cache and btn_music_play.");
                           DownloadManager.getInstance().cacheVideoFile(mDataHelper.getShareId(), fileModel);
                       }
                   }
               }

               final ProgressDialog progressDialog = ProgressDialog.show(activity, "", "Waiting...", true, true);
               // 这里有一个loading的过程
               final FileDownloadListener tempListener = new FileDownloadListener() {
                    @Override
                    public void onFileDownloadWaiting(FileDownloadTask fileDownloadTask) {
                    }

                    @Override
                    public void onFileDownloadStart(FileDownloadTask fileDownloadTask) {
                    }

                    @Override
                    public void onFileDownloadProgressUpdate(FileDownloadTask fileDownloadTask) {
                        if (fileDownloadTask.getFileId() == fileId && fileDownloadTask.getDownloadedBytes() > MIN_OPEN_VIDEO_SIZE && fileDownloadTask.getPercentage() > 0.02) {
                            String filePath = fileDownloadTask.getFilePath();
                            long duration = VideoBaseDataHelper.getInstance().getVideoDuration(filePath);
                            GLog.i("Open video. filePath: " + filePath + ", percentage: " + fileDownloadTask.getPercentage() + ", duration: " + duration);
                            if (duration > 0) {
                                nextModel.setDownloadPercent(fileDownloadTask.getPercentage());
                                nextModel.setFilePath(filePath);
                                if (isAutoPlay) {
                                    playOnDelay(nextModel, VideoPlayerDataHelper.PLAY_NEXT_VIDEO_DELAY,  activity);
                                } else {
                                    play(nextModel);
                                }
                            } else {
                                Toast.makeText(activity, activity.getString(R.string.download_cannot_instant_play), Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                            DownloadManager.getInstance().removeFileDownloadListener(this);
                        }
                    }

                    @Override
                    public void onFileDownloadPaused(FileDownloadTask fileDownloadTask) {
                    }

                    @Override
                    public void onFileDownloadComplete(FileDownloadTask fileDownloadTask) {
                        Activity currentActivity = AppManager.getAppManager().currentActivity();
                        if (currentActivity == null || !(currentActivity instanceof VideoPlayerActivity)) {
                            String filePath = fileDownloadTask.getFilePath();
                            GLog.i("Open video. filePath: " + filePath);
                            nextModel.setDownloadPercent(fileDownloadTask.getPercentage());
                            nextModel.setFilePath(filePath);
                            play(nextModel);
                            progressDialog.dismiss();
                            DownloadManager.getInstance().removeFileDownloadListener(this);
                        }
                    }

                    @Override
                    public void onFileDownloadFailed(FileDownloadTask fileDownloadTask) {
                        progressDialog.dismiss();
                        DownloadManager.getInstance().removeFileDownloadListener(this);
                    }
                };
                DownloadManager.getInstance().addFileDownloadListener(tempListener);
                progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        GLog.i("Dismiss open video dialog.");
                        DownloadManager.getInstance().removeFileDownloadListener(tempListener);
                    }
                });
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        GLog.i("Cancel open video dialog.");
                        DownloadManager.getInstance().removeFileDownloadListener(tempListener);
                    }
                });
            }
            return true;
        } else {
            return false;
        }
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
}
