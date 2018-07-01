/*
 * *
 *  Copyright (c) 2015. Dingtone, inc. All rights reserved.
 * /
 */

package xy.media.oneplayer.videoplayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import xy.media.oneplayer.R;
import xy.media.oneplayer.data.greendao.BaseFile;
import xy.media.oneplayer.data.helper.VideoDataHelper;
import xy.media.oneplayer.data.model.DownloadVideoUpdateEvent;
import xy.media.oneplayer.gl.Const;
import xy.media.oneplayer.io.OnOperaFileListener;
import xy.media.oneplayer.io.VideoPlayedModel;
import xy.media.oneplayer.listener.OperaListener;
import xy.media.oneplayer.listener.OrientationSensorListener;
import xy.media.oneplayer.log.log.GLog;
import xy.media.oneplayer.manager.FileLibListenerManager;
import xy.media.oneplayer.player.subtitles.SubtitleView;
import xy.media.oneplayer.player.subtitles.SubtitlesModel;
import xy.media.oneplayer.util.CommonUtil;
import xy.media.oneplayer.util.DeviceInfo;
import xy.media.oneplayer.util.FastClickUtil;
import xy.media.oneplayer.util.TextUtil;
import xy.media.oneplayer.util.ToastUtil;
import xy.media.oneplayer.util.UiUtils;
import xy.media.oneplayer.util.VideoUtils;
import xy.media.oneplayer.view.VerticalSeekBar;
import xy.media.oneplayer.view.base.BaseActivity;

import static xy.media.oneplayer.io.OpenVideoManager.OPEN_TYPE_LIBRARY_MODE;
import static xy.media.oneplayer.io.OpenVideoManager.OPEN_TYPE_NO_CHAT_MODE;
import static xy.media.oneplayer.io.OpenVideoManager.OPEN_TYPE_SHARE_MODE;
import static xy.media.oneplayer.io.OpenVideoManager.OPEN_TYPE_SIMPLE_MODE;

public class VideoPlayerActivity extends BaseActivity implements OnClickListener,
        OnSeekBarChangeListener, Callback, OnBufferingUpdateListener,
        OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener, OnErrorListener, VideoPlayerContract.View{

    private static final int SEEK_BAR_UPDATE_DELAY = 200;
    private final int CONTROLLER_GONE_DELAY = 5000;
    private final long MINI_TIME_TO_RECORD_AFTER_SCROLL_SEEKER = 10 * 1000;
    private final long MINI_DURATION_VIDEO_TO_RECORD = 5 * 60 * 1000;
    private final float STEP_VOLUME = 4f;
    private final float STEP_PROGRESS = 2f;
    private final int MINUTES_STEP_LARGE = 5 * 1000;
    private final int MINUTES_STEP_SMALL = 1000;
    private static final int TIME_MSG_WHAT = 2;
    private static final int DOWNING_MSG_WHAT = 3;
    private static final int PARSE_SRT = 11;

    private final int TAG_CONTROLLER = 0;
    private final int TAG_FAST_BACK_BTN = 1;
    private final int TAG_FAST_FORWARD_BTN = 2;
    private final int TAG_LAST_BTN= 3;
    private final int TAG_NEXT_BTN = 4;
    private final int TAG_BACK_IMAGE = 5;
    private final int TAG_SHARE_BTN = 6;
    private final int TAG_PLAY_BIG_BTN_LAYOUT = 7;
    private final int TAG_MUTE_BTN = 8;
    private final int TAG_OPERA_BTN = 10;
    private final int TAG_SHARE_DETAIL_BTN = 11;
    private final int TAG_DOWNALOD_BTN = 12;
    private final int TAG_LIKE_BTN = 13;

    private long mSeekScrollTime = -1;
    private int mMaxVolume;
    private int mCurrentVolume;
    private int mPlayingTime = 0;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mOrientationByVideoContent;
    private int mSecondProgressValue = -1;
    private int mPausedSecondProgressValue;

    private boolean mIsVideoSizeKnown = false;
    private boolean mIsVideoReadyToBePlayed = false;
    private boolean mIsProgressChanged = false;
    private boolean mIsPlayError = false;
    private boolean mIsDestroyed = false;
    private boolean isSurfaceCreated = false;
    private int mCurrentPos = 0;

    private ImageView mIvOpera, mIvNext, mIvPlayBigBtn, mIvMuteBtn, mIvBattery, mIvDownloadBtn;
    private View mRlController, mRlTopView;
    private SurfaceView mSurfaceView;
    private TextView mTvCurrentTime, mTotalTime, mTvTitle, mTvTime, mTvTimePos, mItShareCommentCount, mTvLikeCount;
    private SeekBar mProgressBar;
    private LinearLayout mLlBackImage;
    private RelativeLayout mRlPlayBig, mRlShare, mRlOpera, mRlLike, mRlComment , mRlDownload;
    private VerticalSeekBar mVoiceSeekBar;
    private SubtitleView mSubTitleView;

    private SurfaceHolder mHolder;

    private MediaPlayer mMediaPlayer;
    private Context mContext;
    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;

    private BroadcastReceiver mVoiceChangeReceiver = new VoiceChangeReceiver();
    private PowerManager.WakeLock mWakeLock;
    private SensorManager sm;
    private Sensor mSensor;

    private Handler mChangeOrientationHandler;
    private OnOperaFileListener mIOListener;
    private OrientationSensorListener mOrientationSensorListener;
    private boolean isPausedJustNow = false;
    private ProgressDialog mWaitingDialog;

    private VideoPlayerContract.Presenter mPresenter;

    private enum AdjustType {
        None,
        Volume,
        FastBackwardOrForward,
    }

    private AdjustType mAdjustType = AdjustType.None;

    private Handler mUpdateHandler = new UpdateHandler(this);

    private static class UpdateHandler extends Handler {
        private WeakReference<VideoPlayerActivity> activity;

        UpdateHandler(VideoPlayerActivity activity){
            this.activity = new WeakReference<VideoPlayerActivity>(activity);
        }

        public VideoPlayerActivity getActivity(){
            return activity.get();
        }

        @Override
        public void handleMessage(Message msg1) {
            if (getActivity() == null) {
                return;
            }
            switch (msg1.what) {
                case 0:
                    if (getActivity().mMediaPlayer != null) {
                        long position = getActivity().mMediaPlayer.getCurrentPosition();
                        getActivity().mTvCurrentTime.setText(VideoUtils.getDuration(position));
                        if (! getActivity().mIsProgressChanged) {
                            getActivity().mProgressBar.setProgress((int) position);
                        }
                        getActivity().mSubTitleView.seekToTime(position);
                        sendEmptyMessageDelayed(0, SEEK_BAR_UPDATE_DELAY);
                    }
                    break;
                case 1:
                    if (!getActivity().mIsProgressChanged) {
                        getActivity().hideController();
                    }
                    break;
                case TIME_MSG_WHAT:
                    getActivity().mTvTime.setText((String) msg1.obj);
                    break;
                case DOWNING_MSG_WHAT:
                    getActivity().mSecondProgressValue = msg1.arg1;
                    if(getActivity().mProgressBar != null){
                        getActivity().mProgressBar.setSecondaryProgress(getActivity().mSecondProgressValue);
                    }

                    boolean isCompleteDownload = msg1.arg2 == 1;
                    if (isCompleteDownload) {
                        getActivity().setDownloadBtn(VideoPlayerDataHelper.getInstance());
                    }
                    break;
//                case PARSE_SRT:
                    //SrtParser.showSRT(videoView,tvSrt) ;
                    //每隔500ms执行一次showSRT()，根据时间匹配显示哪句字幕
//                    mHandler.sendEmptyMessageDelayed(0, 500);
//                    break;

            }
            super.handleMessage(msg1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        setContentView(R.layout.activity_video_player);
        mContext = this;

//        MusicPlayerController.requestAudioFocus(this);

        mRlController = findViewById(R.id.controler_rl);
        mSurfaceView = (SurfaceView) this.findViewById(R.id.content_sv);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setKeepScreenOn(true);

        mRlShare = (RelativeLayout) findViewById(R.id.share_rl);
        mIvOpera = (ImageView) findViewById(R.id.other_menu_btn_iv);
        mRlOpera = (RelativeLayout) findViewById(R.id.other_menu_rl);
        mRlComment = (RelativeLayout) findViewById(R.id.share_detail_rl);
        mRlDownload = (RelativeLayout) findViewById(R.id.download_rl);
        mRlLike = (RelativeLayout) findViewById(R.id.share_like_rl);
        mIvNext = (ImageView) findViewById(R.id.next_iv);
        mTvCurrentTime = (TextView) mRlController.findViewById(R.id.current_time_tv);
        mTotalTime = (TextView) mRlController.findViewById(R.id.total_time_tv);
        mTvTime = (TextView) findViewById(R.id.time_tv);
        mTvTitle = (TextView) findViewById(R.id.title_tv);
        mIvPlayBigBtn = (ImageView) findViewById(R.id.big_play_iv);
        mRlPlayBig = (RelativeLayout) findViewById(R.id.big_play_btn_rl);
        setBigPlayBtnVisible(false);
        mProgressBar = (SeekBar) findViewById(R.id.progress_pb);
        setSecondProgress();
        mRlTopView = (View) findViewById(R.id.top_rl);
        mLlBackImage = (LinearLayout) findViewById(R.id.back_and_title_ll);
        mIvBattery = (ImageView) findViewById(R.id.battery_view_in_iv);
        mVoiceSeekBar = (VerticalSeekBar) findViewById(R.id.voice_progress_seekbar);
        mIvMuteBtn = (ImageView) findViewById(R.id.mute_voice_iv);
        mTvTimePos = (TextView) findViewById(R.id.time_position_tv);
        mTvTimePos.setVisibility(View.GONE);
        mItShareCommentCount = (TextView) findViewById(R.id.share_detail_num_tv);
        mTvLikeCount = (TextView) findViewById(R.id.share_like_num_tv);
        mIvDownloadBtn = (ImageView) findViewById(R.id.share_download_iv);
        mSubTitleView = (SubtitleView) findViewById(R.id.subtitile_sv) ;

        mGestureDetector = new GestureDetector(this, new MyGestureListener());
        registerReceiver(batteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        updateVolume();
        initListener();
        setTimeLoop();
//        if (MusicBaseDataHelper.getInstance().isPlaying()) {
//            MusicPlayerController.pause(this);
//        }
        registerBroadcastReceiver();

        //重力感应，屏幕切换
        mChangeOrientationHandler = new ChangeOrientationHandler(this);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mOrientationSensorListener = new OrientationSensorListener(mChangeOrientationHandler);
        sm.registerListener(mOrientationSensorListener, mSensor, SensorManager.SENSOR_DELAY_UI);

        EventBus.getDefault().register(this);
        mIOListener = FileLibListenerManager.getInstance().getmOnOperaFileListener();

        mSeekScrollTime = System.currentTimeMillis();

        mPresenter = new VideoPlayerPresenter(this, this);
        mPresenter.start(getIntent());
    }


    private void setTotalTimeTxView(int duration){
        if (duration > 0) {
            mTotalTime.setText(VideoUtils.getDuration(duration));
        }
    }

    @Override
    public void setTitleView(String title) {
        if(mTvTitle != null && title != null){
            mTvTitle.setText(title);
        }
    }

    @Override
    public void initDownloadView(float downloadPercent, int duration) {
        int progress =(int)(  downloadPercent * duration);
        if (mUpdateHandler != null) {
            Message msg = new Message();
            msg.what = DOWNING_MSG_WHAT;
            msg.arg1 = progress;
            mUpdateHandler.sendMessage(msg);
        }
    }

    private void setSecondProgress(){
        try {
            ColorStateList csl=(ColorStateList)getResources().getColorStateList(R.color.gray_3);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ColorStateList stateList = csl;
                mProgressBar.setSecondaryProgressTintList(stateList);
            } else {
                PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    mode = PorterDuff.Mode.MULTIPLY;
                }
                if (mProgressBar.getIndeterminateDrawable() != null) {
                    mProgressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.gray_3), mode);
                }
                if (mProgressBar.getProgressDrawable() != null) {
                    mProgressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.gray_3), mode);
                }
            }
        } catch (Exception e) {
            GLog.e(CommonUtil.getCrashReport(e));
        }
    }


    @Override
    public void setPresenter(VideoPlayerContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void initView(VideoPlayerDataHelper data) {
        int type = data.getOpenType();
        switch (type) {
            case OPEN_TYPE_LIBRARY_MODE : {
                mRlLike.setVisibility(View.GONE);
                mRlComment.setVisibility(View.GONE);
                mRlDownload.setVisibility(View.GONE);
                mRlShare.setVisibility(View.VISIBLE);
                mRlOpera.setVisibility(View.VISIBLE);
                break;
            }
            case OPEN_TYPE_SHARE_MODE : {
                mRlLike.setVisibility(View.VISIBLE);
                mRlComment.setVisibility(View.VISIBLE);
                mRlDownload.setVisibility(View.VISIBLE);
                mRlShare.setVisibility(View.GONE);
                mRlOpera.setVisibility(View.GONE);
                break;
            }
            case OPEN_TYPE_SIMPLE_MODE : {
                mRlLike.setVisibility(View.GONE);
                mRlComment.setVisibility(View.GONE);
                mRlDownload.setVisibility(View.GONE);
                mRlShare.setVisibility(View.GONE);
                mRlOpera.setVisibility(View.GONE);
                break;
            }
            case OPEN_TYPE_NO_CHAT_MODE : {
                mRlLike.setVisibility(View.VISIBLE);
                mRlComment.setVisibility(View.GONE);
                mRlDownload.setVisibility(View.VISIBLE);
                mRlShare.setVisibility(View.GONE);
                mRlOpera.setVisibility(View.GONE);
                break;
            }
        }

        setView(data);

        //保存最新播放的视频
        mPresenter.saveRecentPlayVideo();

        //加载字幕
        mPresenter.loadSubtitles();

    }

    @Override
    public void setView(VideoPlayerDataHelper data) {
        mPresenter.setTitleView();
        setNextBtnView(data.isPlayingVideolist());
    }

    @Override
    public void setNextBtnView(boolean isplaylist) {
        mIvNext.setVisibility(isplaylist ? View.VISIBLE : View.GONE);
    }

    private void initListener() {
        mProgressBar.setOnSeekBarChangeListener(this);
        mRlController.setOnClickListener(this);
        mRlController.setTag(TAG_CONTROLLER);
        mIvNext.setOnClickListener(this);
        mIvNext.setTag(TAG_NEXT_BTN);
        mLlBackImage.setOnClickListener(this);
        mLlBackImage.setTag(TAG_BACK_IMAGE);
        mRlShare.setOnClickListener(this);
        mRlShare.setTag(TAG_SHARE_BTN);
        mRlOpera.setOnClickListener(this);
        mRlOpera.setTag(TAG_OPERA_BTN);
        mRlPlayBig.setOnClickListener(this);
        mRlPlayBig.setTag(TAG_PLAY_BIG_BTN_LAYOUT);
        mIvMuteBtn.setOnClickListener(this);
        mIvMuteBtn.setTag(TAG_MUTE_BTN);
        mVoiceSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                if (progress != 0 && VideoPlayerDataHelper.getInstance().isMute()) {
                    mIvMuteBtn.setImageResource(R.drawable.ic_player_voice_mute);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mRlComment.setOnClickListener(this);
        mRlComment.setTag(TAG_SHARE_DETAIL_BTN);

        mRlDownload.setOnClickListener(this);
        mRlDownload.setTag(TAG_DOWNALOD_BTN);
        mRlLike.setOnClickListener(this);
        mRlLike.setTag(TAG_LIKE_BTN);
    }

    /**
     * 设置时钟循环更新UI
     */
    private void setTimeLoop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!mIsDestroyed) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        String str = sdf.format(new Date());
                        mUpdateHandler.sendMessage(mUpdateHandler.obtainMessage(TIME_MSG_WHAT, str));
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        GLog.d("onPause");

        if (isPlaying()) {
            pause();
        }
        if (mWakeLock != null) {
            mWakeLock.release();
        }

        if (mOrientationSensorListener != null) {
            sm.unregisterListener(mOrientationSensorListener);
        }
        isPausedJustNow = true;
        super.onPause();
    }

    private  boolean isPlaying(){
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public void onBackPressed() {
        GLog.d("onbackpress");
        mPresenter.unSubscription();

        if (!isMoveProgressRightNow()) {
            savePlayRecord();
        }
        doCleanUp();

        VideoPlayerDataHelper dataHelper = VideoPlayerDataHelper.getInstance();
        Intent intent = new Intent();
        intent.putExtra("play_video_path", dataHelper.getCurrentPath());
        setResult(Const.RESULT_CODE.VIDEO_PLAYER_ACTIVITY, intent);


        if (dataHelper.isMute()) {
            mPresenter.closeMute();
        }

        finish();
    }

    /**
     * 判断是不是刚刚用手指移动了播放进度，刚刚的意思是10s内
     * @return
     */
    private boolean isMoveProgressRightNow() {
        long now = System.currentTimeMillis();
        GLog.d("now = " + now + ", recent = " + mSeekScrollTime
                + ", delete= " + (now - mSeekScrollTime) + " ,is larger than " + MINI_TIME_TO_RECORD_AFTER_SCROLL_SEEKER +
                " ? =" + (now - mSeekScrollTime>MINI_TIME_TO_RECORD_AFTER_SCROLL_SEEKER));
        return  now - mSeekScrollTime < MINI_TIME_TO_RECORD_AFTER_SCROLL_SEEKER;
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void setControllerGone() {
        mUpdateHandler.removeMessages(1);
        mUpdateHandler.sendEmptyMessageDelayed(1, CONTROLLER_GONE_DELAY);
    }


    /**
     * 保存播放进度大哦数据库中
     */
    private void savePlayRecord() {
        if (getDuration() > MINI_DURATION_VIDEO_TO_RECORD) {
            GLog.d("save play record.");
            if (mMediaPlayer != null) {
                long currentPos = (long)mMediaPlayer.getCurrentPosition();
                VideoPlayerDataHelper.getInstance().saveVideoRecord(currentPos);
            }
        } else {
            GLog.d("video :" + VideoPlayerDataHelper.getInstance().getCurrentPath() + " is not long enough to record.");
        }

    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    /**
     * 根据播放记录从原来播放的位置进行播放
     */
    private void startVideoPlayback(int pos) {
        GLog.d("startVideoPlayback at pos = " + pos);
        mProgressBar.setMax(getDuration());
        mHolder.setFixedSize(mVideoWidth, mVideoHeight);

        mTvCurrentTime.setText(VideoUtils.getDuration(pos));
        mProgressBar.setProgress(pos);
        if (pos > 0 && ! VideoPlayerDataHelper.getInstance().isDownloadingFile()) {
            mMediaPlayer.seekTo(pos);
        }
        start();

        mTotalTime.setText(VideoUtils.getDuration(getDuration()));
        mPresenter.setTitleView();
    }

    private int getRecordPos(){
        return  VideoPlayerDataHelper.getInstance().getRecordPos();
    }

    /**
     * 播放视频主函数操作，这里视频可以是普通视频或gdv视频
     * @param path 视频路径
     */
    @Override
    public void playLocalVideo(String path) {
        //ts格式 4.4以下不支持
        if (! checkSupport(path)) {
            GLog.e("ts video cannot btn_music_play under 4.4.");
            failOpenVideo();
            return;
        }

        GLog.d("btn_play:" + path);
        if (!TextUtils.isEmpty(path)) {
            try {
                if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setDisplay(mHolder);
                    mMediaPlayer.setOnBufferingUpdateListener(this);
                    mMediaPlayer.setOnCompletionListener(this);
                    mMediaPlayer.setOnPreparedListener(this);
                    mMediaPlayer.setOnVideoSizeChangedListener(this);
                    mMediaPlayer.setOnErrorListener(this);
                    setVolumeControlStream(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if(!mIsPlayError){
                                playNext();
                            }
                        }
                    });

                } else {
                    mMediaPlayer.reset();
                }
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepare();
            } catch (Exception e) {
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int tag = (int) v.getTag();
        switch (tag) {
            case TAG_CONTROLLER:{
                hideController();
                break;
            }

            case TAG_LAST_BTN:{
                playPrevious();
                break;
            }

            case TAG_FAST_BACK_BTN:{
                long position = mMediaPlayer.getCurrentPosition();
                long target = position - 10000;
                if (target > 0) {
                    mMediaPlayer.seekTo((int) target);
                    mProgressBar.setProgress((int) position);
                }
                break;
            }

            case TAG_PLAY_BIG_BTN_LAYOUT:{
                if (mMediaPlayer.isPlaying()) {
                    pause();
                } else {
                    start();
                }
                break;
            }

            case TAG_FAST_FORWARD_BTN:{
                long position = mMediaPlayer.getCurrentPosition();
                long target = position + 10000;
                if (target < getDuration()) {
                    mMediaPlayer.seekTo((int) target);
                    mProgressBar.setProgress((int) position);
                }
                break;
            }

            case TAG_NEXT_BTN:{
                if(VideoPlayerDataHelper.getInstance().getCurrentVideoPosition() == VideoPlayerDataHelper.getInstance().getCountOfPlayList() - 1){
                    ToastUtil.show(getString(R.string.file_center_video_player_no_next_video));
                } else {
                    playNext(false);
                }
                break;
            }

            case TAG_BACK_IMAGE:{
                if (!FastClickUtil.isFastClick()) {
                    pause();
                    onBackPressed();
                }
                break;
            }

            case TAG_SHARE_BTN:{
                //发送时暂停播放
                if (mMediaPlayer.isPlaying()) {
                    pause();
                }
                if (mIOListener != null){
                    VideoPlayedModel model = VideoPlayerDataHelper.getInstance().getVideo();
                    if (model != null) {
                        BaseFile videoInfo
                                = VideoDataHelper.getInstance().getVideoInfoByPath(
                                        model.getFilePath(),
                                        VideoDataHelper.getInstance().getVideoDuration(model.getFilePath()),
                                        VideoDataHelper.getInstance().LOCAL_VIDEO
                        );
                        mIOListener.onShare(videoInfo, this);
                    }
                }
                break;
            }

            case TAG_OPERA_BTN:{
                if (mMediaPlayer.isPlaying()) {
                    pause();
                }
                if (mIOListener != null) {
                    VideoPlayedModel model = VideoPlayerDataHelper.getInstance().getVideo();
                    if (model != null) {
                        final BaseFile videoInfo
                                = VideoDataHelper.getInstance().getVideoInfoByPath(
                                model.getFilePath(),
                                VideoDataHelper.getInstance().getVideoDuration(model.getFilePath()),
                                VideoDataHelper.getInstance().LOCAL_VIDEO
                        );
                        mIOListener.onShowMenu(videoInfo, false, true, this, mIvOpera, new OperaListener(){
                            @Override
                            public void onStart() {
                            }
                            @Override
                            public void onCancel() {
                            }

                            @Override
                            public void onFailed(String str) {
                                VideoPlayerActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtil.show("删除失败");
                                    }
                                });
                                GLog.e(str);
                            }

                            @Override
                            public void onFinish() {
                                Intent intent = new Intent();
                                intent.putExtra("play_video_path", videoInfo.getPath());
                                setResult(Const.RESULT_CODE.VIDEO_PLAYER_MENU_DELETE, intent);
                                finish();
                            }
                        });
                    }
                }
                break;
            }

            case TAG_MUTE_BTN: {
                if (FastClickUtil.isFastClick()) {
                    break;
                }
                if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0) {
                    mPresenter.openMute();
                } else {
                    mPresenter.closeMute();
                }
                break;
            }

            case TAG_DOWNALOD_BTN: {
                mPresenter.downloadVideo(this);
                break;
            }
            default:
                break;
        }

        setControllerGone();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void openMuteView() {
        mIvMuteBtn.setImageResource(R.drawable.btn_player_voice_mute_on);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                (int)(26* DeviceInfo.getInstance().getDensity()),
                (int)(26* DeviceInfo.getInstance().getDensity())
        );
        lp.addRule(RelativeLayout.BELOW,  R.id.voice_progress_seekbar);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(
                0,
                (int)(-2* DeviceInfo.getInstance().getDensity()),
                0,
                0
        );
        mIvMuteBtn.setLayoutParams(lp);
    }

    @Override
    public void closeMuteView() {
        mIvMuteBtn.setImageResource(R.drawable.ic_player_voice_mute);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                (int)(26* DeviceInfo.getInstance().getDensity()),
                (int)(26* DeviceInfo.getInstance().getDensity())
        );
        lp.addRule(RelativeLayout.BELOW,  R.id.voice_progress_seekbar);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(
                0,
                (int)(-2* DeviceInfo.getInstance().getDensity()),
                (int)(1* DeviceInfo.getInstance().getDensity()),
                0
        );
        mIvMuteBtn.setLayoutParams(lp);
    }

    /**
     * 显示视频控制栏
     */
    @Override
    public void showController() {
        mRlController.setVisibility(View.VISIBLE);
        mRlTopView.setVisibility(View.VISIBLE);
        setBigPlayBtnVisible(true);

        mUpdateHandler.removeMessages(1);
        mUpdateHandler.sendEmptyMessageDelayed(1, CONTROLLER_GONE_DELAY);
    }

    /**
     * 隐藏视频控制栏
     */
    @Override
    public void hideController() {
        mRlTopView.setVisibility(View.GONE);
        mRlController.setVisibility(View.GONE);
    }

    /**
     * 启动播放逻辑，开始播放视频
     */
    private void start() {
        mUpdateHandler.removeMessages(0);
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.start();
            } catch (Exception e) {
            }
            mIvPlayBigBtn.setImageResource(R.drawable.btn_video_player_big_pause_btn);
            mUpdateHandler.sendEmptyMessageDelayed(0, SEEK_BAR_UPDATE_DELAY);
        }
        setControllerGone();
    }

    /**
     * 暂停播放
     */
    @Override
    public void pause() {
        if (isPlaying()) {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
                mIvPlayBigBtn.setImageResource(R.drawable.btn_video_player_play_big);
                mUpdateHandler.removeMessages(0);
            }
        }
    }

    /**
     * 播放进度变化时的回掉
     * @param seekBar 进度条控件
     * @param progress 进度
     * @param fromUser 是否是用户触发的变化
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (isOverDownloadProgress(progress )) {
            showOverSecondProgressNotice();
            return;
        }

        if (fromUser) {
            if (mMediaPlayer != null) {
                mSeekScrollTime = System.currentTimeMillis();

                GLog.d("progress change : " + progress);
                mTvCurrentTime.setText(VideoUtils.getDuration(progress));
            } else {
                GLog.d("mMediaPlayer is null");
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsProgressChanged = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            mIsProgressChanged = false;
            if (mMediaPlayer != null && mProgressBar != null) {
                if (isOverDownloadProgress(mProgressBar.getProgress() )) {
                    showOverSecondProgressNotice();
                    return;
                }
                mMediaPlayer.seekTo(mProgressBar.getProgress());
            }
        } catch (Exception e) {
            e.printStackTrace();
            GLog.e(e.toString());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceCreated = true;
        mPresenter.startPlay();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mSurfaceView != null) {
            mSurfaceView.getHolder().setSizeFromLayout();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceCreated = false;

        if (mMediaPlayer != null) {
            mCurrentPos = mMediaPlayer.getCurrentPosition();
            try {
                mMediaPlayer.setDisplay(null);
            } catch (Throwable e) {
                CommonUtil.getCrashReport(e);
            }
        }
    }

    /**
     * 创建视频展示页面
     */
    private void createSurface() {
        mHolder = mSurfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //兼容4.0以下的版本
        mHolder.addCallback(this);
        mHolder.setKeepScreenOn(true);
    }


    /**
     * 视频尺寸变化回调，用来首先横竖屏切换的逻辑
     * @param mp
     * @param width
     * @param height
     */
    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (width == 0 || height == 0 || mMediaPlayer == null) {
            GLog.e("invalid video width(" + width + ") or height(" + height
                    + ")");
            return;
        }
        mIsVideoSizeKnown = true;
        mVideoHeight = height;
        mVideoWidth = width;

        int wid = mMediaPlayer.getVideoWidth();
        int hig = mMediaPlayer.getVideoHeight();
        // 根据视频的属性调整其显示的模式
        if (wid > hig) {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            mOrientationByVideoContent = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            mOrientationByVideoContent = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int surfaceViewWidth = dm.widthPixels;
        int surfaceViewHeight = dm.heightPixels;
        if (width > height) {
            // 竖屏录制的视频，调节其上下的空余
            int w = surfaceViewHeight * width / height;
            int margin = (surfaceViewWidth - w) / 2;
            GLog.d("margin:" + margin);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(margin, 0, margin, 0);
            mSurfaceView.setLayoutParams(lp);
        } else {
            // 横屏录制的视频，调节其左右的空余
            int h = surfaceViewWidth * height / width;
            int margin = (surfaceViewHeight - h) / 2;
            GLog.d("margin:" + margin);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, margin, 0, margin);
            mSurfaceView.setLayoutParams(lp);
        }
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback(getRecordPos());
        }
    }

    /**
     * 准备播放
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        GLog.d("onPrepared");
        mIsVideoReadyToBePlayed = true;
        setDuration();
        setTotalTimeTxView(getDuration());

        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback(getRecordPos());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mUpdateHandler.removeMessages(0);
        playNext();
    }

    private void playNext() {
        playNext(true);
    }

    private void playNext(boolean isAutoPlay) {
        VideoPlayerDataHelper dataHelper = VideoPlayerDataHelper.getInstance();
        if (dataHelper.getVideo() != null) {
            pause();
            boolean gotNextVideo = mPresenter.playNext(isAutoPlay, this);
            if (gotNextVideo) {
                setView(dataHelper);
            } else {
                mTvCurrentTime.setText(VideoUtils.getDuration(0));
                mProgressBar.setProgress(0);
                mIvPlayBigBtn.setImageResource(R.drawable.btn_video_player_play_big);

                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(0);
                }
            }
        }
    }

    private void playPrevious() {
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mIsPlayError) {
            return false;
        }
        mIsPlayError = true;
        GLog.e("error， what = " + what + " ; extra = "+ extra);
        failOpenVideo();
        return true;
    }

    /**
     * 监听触摸事件，主要是接收普通触摸事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return true;

        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                mAdjustType = AdjustType.None;

                if (mIsProgressChanged) {
                    mMediaPlayer.seekTo(mPlayingTime);
                    mIsProgressChanged = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mAdjustType = AdjustType.None;
                break;
            case MotionEvent.ACTION_DOWN:
                if (mMediaPlayer != null) {
                    mPlayingTime = mMediaPlayer.getCurrentPosition();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 处理双击、单击屏幕事件，还有滑动屏幕事件
     */
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mMediaPlayer.isPlaying()) {
                pause();
            } else {
                start();
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mRlController.isShown()) {
                hideController();
            } else {
                showController();
            }

            if (mTvTimePos.isShown()) {
                mTvTimePos.setVisibility(View.GONE);
                setBigPlayBtnVisible(true);
            }
            mAdjustType = AdjustType.None;
            return true;
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (mAdjustType == AdjustType.None) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    mAdjustType = AdjustType.FastBackwardOrForward;
                } else {
                    mAdjustType = AdjustType.Volume;
                }
            }

            return adjustInternal(e1, e2, distanceX, distanceY);
        }
    }

    /**
     * 滑动结果逻辑，根据不同状态，调整播放进度和声音大小
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     * @return
     */
    private boolean adjustInternal(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mAdjustType == AdjustType.FastBackwardOrForward) {
            if (mMediaPlayer == null) {
                return false;
            }
            int videoTotalTime = getDuration();
            //TODO 临时处理
            int isArabic = 1;

            if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动
                if (isArabic * distanceX >= UiUtils.dp2px(this, STEP_PROGRESS)) {// 快退，用步长控制改变速度，可微调
                    if (mPlayingTime > minuteStep(videoTotalTime)) {// 避免为负
                        mPlayingTime -= minuteStep(videoTotalTime);// scroll方法执行一次快退3秒
                    } else {
                        mPlayingTime = 0;
                    }
                } else if (isArabic * distanceX <=  -UiUtils.dp2px(this, STEP_PROGRESS)) {// 快进
                    int tmp = mPlayingTime + minuteStep(videoTotalTime);
                    if(tmp > videoTotalTime){
                        tmp = videoTotalTime ;
                    }
                    mPlayingTime = tmp;
                }
                if (mPlayingTime < 0) {
                    mPlayingTime = 0;
                }

                if (isOverDownloadProgress(mPlayingTime)) {
                    showOverSecondProgressNotice();
                    return false;
                }
                mProgressBar.setProgress(mPlayingTime);
                showTimePosText(VideoUtils.getShortDuration(mPlayingTime) + "/" + VideoUtils.getShortDuration(videoTotalTime));
                mIsProgressChanged = true;
            }
        } else if (mAdjustType == AdjustType.Volume) {
            mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
            if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                if (distanceY >= UiUtils.dp2px(this, STEP_VOLUME)) {// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                    if (mCurrentVolume < mMaxVolume) {// 为避免调节过快，distanceY应大于一个设定值
                        mCurrentVolume++;
                    }
                } else if (distanceY <= -UiUtils.dp2px(this, STEP_VOLUME)) {// 音量调小
                    if (mCurrentVolume > 0) {
                        mCurrentVolume--;
                        if (mCurrentVolume == 0) {// 静音，设定静音独有的图片
                        }
                    }
                }
                int percentage = (mCurrentVolume * 100) / mMaxVolume;
                showTimePosText(percentage + "%");
                mVoiceSeekBar.setProgress(mCurrentVolume);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);

                if (mCurrentVolume > 0 && VideoPlayerDataHelper.getInstance().isMute()) {
                    mPresenter.closeMute();
                }
            }
        }
        return true;
    }

    /**
     * 滑动屏幕时进度的步长
     * @param duration
     * @return
     */
    private int minuteStep(int duration){
        if (duration > 5* 60 * 1000) {
            return MINUTES_STEP_LARGE;
        } else {
            return MINUTES_STEP_SMALL;
        }
    }

    /**
     * 滑动屏幕时显示时间点
     * @param txt
     */
    private void showTimePosText(String txt) {
        mTvTimePos.setText(txt);
        mTvTimePos.setVisibility(View.VISIBLE);
        setBigPlayBtnVisible(false);
    }


    private void setBigPlayBtnVisible(boolean visible){
        mRlPlayBig.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 手势结束，隐藏控制栏
     */
    private void endGesture() {
        if (mTvTimePos.isShown()) {
            mTvTimePos.setVisibility(View.GONE);
            setBigPlayBtnVisible(true);
        }

        // 隐藏
        mUpdateHandler.removeMessages(1);
        mUpdateHandler.sendEmptyMessageDelayed(1, CONTROLLER_GONE_DELAY);
    }

    /**
     * 电池变化时接收广播
     */
    private BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int power = (level * 100 / scale) / 20;
                GLog.d("elleray 电池电量：:" + power);
                switch (power) {
                    case 0:
                        mIvBattery.setImageResource(R.drawable.ic_battery_20_p);
                        break;
                    case 1:
                        mIvBattery.setImageResource(R.drawable.ic_battery_40_p);
                        break;
                    case 2:
                        mIvBattery.setImageResource(R.drawable.ic_battery_60_p);
                        break;
                    case 3:
                        mIvBattery.setImageResource(R.drawable.ic_battery_80_p);
                        break;
                    case 4:
                        mIvBattery.setImageResource(R.drawable.ic_batery_in);
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        GLog.e("btn_music_play on destroy.");
        if (mUpdateHandler != null) {
            mUpdateHandler.removeCallbacksAndMessages(null);
        }
        if (mHolder != null) {
            mHolder.getSurface().release();
            mHolder.removeCallback(this);
        }

        releaseMediaPlayer();

        EventBus.getDefault().unregister(this);
        unregisterReceiver(batteryChangedReceiver);

        unregisterReceiver(mVoiceChangeReceiver);
        mIsDestroyed = true;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                    .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
        mWakeLock.acquire();
        sm.registerListener(mOrientationSensorListener, mSensor, SensorManager.SENSOR_DELAY_UI);

        if (isPausedJustNow) {
            GLog.d("resume from pause");
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (isSurfaceCreated) {
                        play(mCurrentPos, VideoPlayerDataHelper.getInstance().getCurrentPath());
                    }
                }
            }, 10);
        }

        super.onResume();
    }

    private void play(final int currentPosition, String path) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    startVideoPlayback(currentPosition);
                }
            });

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });
        }
        catch (Exception e) {
            GLog.e(CommonUtil.getCrashReport(e));
        }
    }

    /**
     * 系统声音变化时接收广播
     */
    class VoiceChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                    GLog.d("voice changed.");
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            updateVolume();
                        }
                    });
                }
        }
    }

    /**
     * 更新播放声音大小
     */
    private void updateVolume() {
        if (mVoiceSeekBar != null) {
            // 设置 seekbar的总长度
            mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mVoiceSeekBar.setMax(mMaxVolume);
            // 设置当前的进度
            mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mVoiceSeekBar.setProgress(mCurrentVolume);
        }
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");//添加接收的广播消息
        mContext.registerReceiver(mVoiceChangeReceiver, filter, null, mUpdateHandler);
    }

    public int getmOrientationByVideoContent() {
        return mOrientationByVideoContent;
    }


    private boolean checkSupport(String path){
        if ( TextUtil.isNull(path)
                ) {
            return false;
        }

        //由于原生系统不支持，暂不支持wmv格式
        String end = path.substring(path.lastIndexOf(".") + 1, path.length()).toLowerCase();
        if (!TextUtil.isNull(end) && end.equals("wmv")) {
            return false;
        }

        return true;
    }

    private void failOpenVideo(){
        ToastUtil.show("打开视频失败");
    }


    private boolean isOverDownloadProgress(int prg) {
        return mSecondProgressValue > 0 &&  prg+1500 >= mSecondProgressValue;
    }

    private void showOverSecondProgressNotice(){

    }

    @Override
    public void showVideoCacheLoading() {
    }

    private int getDuration() {
        return VideoPlayerDataHelper.getInstance().getCurrentVideoDuration();
    }

    private void setDuration(){
        if (mMediaPlayer != null) {
            int duration = mMediaPlayer.getDuration();
            mPresenter.setCurrentVideoDuration(duration);
        }

    }

    /**
     * 页面从前台到后台会执行 onPause ->onStop 此时Surface会被销毁，
     * 再一次从后台 到前台时需要 重新创建Surface
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        if (!isSurfaceCreated) {
            createSurface();
        }
    }

    @Override
    public void setVideoLikeCount(int count) {
            GLog.d("setVideoLikeCount =  " + count);
            if (count > 0) {
                mTvLikeCount.setText(count + "");
                mTvLikeCount.setVisibility(View.VISIBLE);
            } else {
                mTvLikeCount.setVisibility(View.GONE);
            }
    }

    @Override
    public void setShareCommentCount(int count) {
        GLog.d("setShareCommentCount =  " + count);
        if (count > 0) {
            mItShareCommentCount.setVisibility(View.VISIBLE);
            mItShareCommentCount.setText(count + "");
        } else {
            mItShareCommentCount.setVisibility(View.GONE);
        }
    }

    @Override
    public void setDownloadBtn(VideoPlayerDataHelper dataHelper) {
    }

    @Override
    public void hideWaitingView() {
        start();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DownloadVideoUpdateEvent event){
        if (! TextUtil.isNull(event.mVideoPath) &&
                event.mVideoPath.equals(VideoPlayerDataHelper.getInstance().getCurrentPath())
                && mMediaPlayer != null) {
            int duration  = getDuration();
            int progress =(int)(  event.mPercent * duration );
            int completeDownload =  (Math.abs(1-event.mPercent) < 0.01) ? 1 : 0;
            if (mUpdateHandler != null) {
                Message msg = new Message();
                msg.what = DOWNING_MSG_WHAT;
                msg.arg1 = progress;
                msg.arg2 = completeDownload;
                mUpdateHandler.sendMessage(msg);
            }
        }
    }

    @Override
    public void setSubtitleData(ArrayList<SubtitlesModel> list) {
        mSubTitleView.setData(list);
    }
}