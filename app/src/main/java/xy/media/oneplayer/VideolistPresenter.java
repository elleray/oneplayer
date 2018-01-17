package xy.media.oneplayer;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import xy.media.oneplayer.data.dbhelper.CenterFileDBManager;
import xy.media.oneplayer.data.greendao.CenterFile;
import xy.media.oneplayer.data.helper.DataCompareHelper;
import xy.media.oneplayer.data.helper.VideoDataHelper;
import xy.media.oneplayer.data.model.VideoInfo;
import xy.media.oneplayer.io.OpenVideoManager;
import xy.media.oneplayer.log.log.GLog;
import xy.media.oneplayer.util.CommonUtil;

import static xy.media.oneplayer.data.VideoBaseDataHelper.TAB_GODAP;

/**
 * Created by Yangwenjie on 2018/1/16.
 */

public class VideolistPresenter implements VideoListContract.Presenter {
    private VideoListContract.View mView;
    private VideoListDataHelper mData;
    private LoadVideoDataAction sLoadVideoDataAction = new LoadVideoDataAction();
    private CompositeSubscription mSubscription;


    public VideolistPresenter(VideoListContract.View w) {
        mView = w;
        mData = VideoListDataHelper.getInstance();
        mSubscription = new CompositeSubscription();

    }

    @Override
    public void init() {
        mData.initData();
    }

    @Override
    public void start() {
        GLog.d("start load video.");
        loadData(false, true);
    }

    @Override
    public void loadData(boolean isRefreshDownloadFile, boolean isFirstLoadVideo) {
        GLog.d("reLoad video data.");
        mView.showProgress();

        sLoadVideoDataAction.initData(isFirstLoadVideo, isRefreshDownloadFile);
        mSubscription.add(rx.Observable.create(sLoadVideoDataAction)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoadVideoDataObserver(mView, mSubscription, isFirstLoadVideo))
        );
    }

    private static class LoadVideoDataAction implements rx.Observable.OnSubscribe<ArrayList<VideoInfo>>{
        private boolean isFirstLoadVideo;
        private boolean isRefreshDownloadFile;

        private void initData(boolean isFirstLoadVideo, boolean isRefreshDownloadFile){
            this.isFirstLoadVideo = isFirstLoadVideo;
            this.isRefreshDownloadFile = isRefreshDownloadFile;
        }

        public LoadVideoDataAction(){
        }

        @Override
        public void call(Subscriber<? super ArrayList<VideoInfo>> subscriber) {
            VideoDataHelper mVideoData = VideoDataHelper.getInstance();
            if(isFirstLoadVideo){
                mVideoData.initData();
            }

            ArrayList<VideoInfo> infos = mVideoData.getMainShowVideos(true);
            VideoDataHelper.getInstance().printListAll("local videos" , infos);

            //排序
            subscriber.onNext(infos);
            subscriber.onCompleted();
        }
    }

    private static class LoadVideoDataObserver implements Observer<ArrayList<VideoInfo>> {
        private WeakReference<VideoListContract.View> viewWeakReference;
        private WeakReference<CompositeSubscription> subscriptionWeakReference;
        private WeakReference<Boolean> isFirstLoadVideo;

        public LoadVideoDataObserver(VideoListContract.View view, CompositeSubscription subscription, Boolean isFirstLoad){
            viewWeakReference = new WeakReference<VideoListContract.View>(view);
            subscriptionWeakReference = new WeakReference<CompositeSubscription>(subscription);
            isFirstLoadVideo = new WeakReference<Boolean>(isFirstLoad);
        }

        @Override
        public void onCompleted() {
            GLog.d("load videos completed");
            if(viewWeakReference.get() != null){
                viewWeakReference.get().hideProgress();
            }
        }

        @Override
        public void onError(Throwable e) {
            GLog.e("load video data failed: " + CommonUtil.getCrashReport(e));
        }

        @Override
        public void onNext(ArrayList<VideoInfo> videoInfos) {
            if(viewWeakReference.get() != null){
                GLog.d("video :"  + videoInfos.size());
                VideoDataHelper.getInstance().setmLists(videoInfos);
                viewWeakReference.get().refreshVideos(videoInfos);

                if(isFirstLoadVideo.get()){
                    GLog.d("This is first load video.should check video update.");
                    checkOutVideoUpdate(subscriptionWeakReference.get(), viewWeakReference.get());
                }
            }
        }
    }

    private static void checkOutVideoUpdate(CompositeSubscription subscription,VideoListContract.View view) {
        subscription.add(
                Observable.create(new CheckVideoUpdateAction())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CheckOutVideoUpdateObserver(view))
        );
    }


    private CheckVideoUpdateAction sCheckVideoUpdateAction = new CheckVideoUpdateAction();
    private static class CheckVideoUpdateAction implements Observable.OnSubscribe<Boolean>{
        @Override
        public void call(Subscriber<? super Boolean> subscriber) {
            GLog.d("check local videos");
            ArrayList<VideoInfo> locals = VideoDataHelper.getInstance().scanLocalContent();
            List<CenterFile> dbFiles = CenterFileDBManager.getInstance().loadVideo(TAB_GODAP);
            if(dbFiles != null) {
                Iterator<CenterFile> iterator = dbFiles.iterator();
                while (iterator.hasNext()) {
                    CenterFile centerFile = iterator.next();
                    if (! VideoDataHelper.getInstance().filterWhiteList(centerFile.getPath())) {
                        iterator.remove();
                    }
                }
            }

            boolean refresh2 = ! DataCompareHelper.isDBFileContainSameData(dbFiles, VideoDataHelper.getInstance().getmMainShowVideos());
            boolean refresh = ! DataCompareHelper.isContainSameData(locals, VideoDataHelper.getInstance().getmMainShowVideos());
            GLog.d(" local video need refresh ? = " +refresh + ", center DB need refresh = " + refresh2 );
            if(refresh){
                VideoDataHelper.getInstance().addToCenterFileDB(locals);
                VideoDataHelper.getInstance().setVideoPlayRecord(locals);
            }

            boolean result = refresh || refresh2;


            subscriber.onNext(result);
            subscriber.onCompleted();
        }
    }


    private static class CheckOutVideoUpdateObserver implements Observer<Boolean>{
        private WeakReference<VideoListContract.View> view;

        public CheckOutVideoUpdateObserver(VideoListContract.View view){
            this.view = new WeakReference<VideoListContract.View>(view);
        }

        @Override
        public void onCompleted() {
            if(view.get() != null){
                view.get().hideProgress();
            }
        }

        @Override
        public void onError(Throwable e) {
            GLog.e(e.toString());
        }

        @Override
        public void onNext(Boolean needRefresh) {
            GLog.d("is need refresh  = " + needRefresh);
            if(needRefresh){
                if(view.get() != null){
                    view.get().reLoad();
                }
            }
        }
    }

    @Override
    public void openVideo(Activity activity, VideoInfo model) {
        OpenVideoManager.open(activity , model, false, null);
    }
}
