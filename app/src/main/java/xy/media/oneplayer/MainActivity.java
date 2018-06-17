package xy.media.oneplayer;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import xy.media.oneplayer.adapter.VideoListAdapter;
import xy.media.oneplayer.data.model.VideoInfo;
import xy.media.oneplayer.view.DividerLine;

public class MainActivity extends AppCompatActivity implements VideoListContract.View{

    private RecyclerView mListView;
    private VideolistPresenter mPresenter;
    private VideoListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPresenter = new VideolistPresenter(this);
        initView();
        initData();

        mPresenter.start();
    }

    private void initView() {
        mListView = (RecyclerView) findViewById(R.id.video_rv);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mListView.setLayoutManager(mLayoutManager);
        mListView.setHasFixedSize(true);

    }

    private void initData() {
        mPresenter.init();
    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void refreshVideos(ArrayList<VideoInfo> mVideoInfos) {
        if (mAdapter == null) {
            mAdapter = new VideoListAdapter(this, mVideoInfos, mPresenter);
            mListView.setAdapter(mAdapter);
        } else {
            if (mVideoInfos != null) {
                mAdapter.setList(mVideoInfos);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void reLoad() {
        mPresenter.loadData(true, false);
    }
}
