package xy.media.oneplayer.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.List;

import xy.media.oneplayer.R;
import xy.media.oneplayer.VideoListContract;
import xy.media.oneplayer.data.model.VideoInfo;
import xy.media.oneplayer.data.model.VideoModel;

/**
 * Created by yangwenjie on 2018/1/9.
 */

public class VideoListAdapter extends RecyclerView.Adapter{
    private Context mContext;
    private List<VideoInfo> mList;
    private VideoListContract.Presenter mPresenter;

    public VideoListAdapter(Context context, List<VideoInfo> list, VideoListContract.Presenter presenter) {
        mContext = context;
        mList = list;
        mPresenter = presenter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_video, null);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new VideoViewHolder(view, mPresenter);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoViewHolder viewHolder = (VideoViewHolder) holder;
        VideoInfo model = mList.get(position);
        viewHolder.setView(model);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void setList(List<VideoInfo> mList) {
        this.mList = mList;
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView tvTitle;
        TextView tvArtist;
        TextView tvAlbumName;
        ImageView ivSelect;
        LinearLayout llArtist;
        ImageView ivCover;
        ImageView ivOpera;
        View point;

        VideoListContract.Presenter mPresenter;
        VideoInfo musicInfo;
        RequestOptions options;
        TransitionOptions transitionOptions;

        public VideoViewHolder(View itemView, VideoListContract.Presenter presenter){
            super(itemView);
            mPresenter = presenter;

            tvTitle = (TextView) itemView.findViewById(R.id.title);
            ivCover = (ImageView ) itemView.findViewById(R.id.cover) ;

            options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.color.video_default)
                    .error(R.color.video_default);
            transitionOptions = new DrawableTransitionOptions()
                    .crossFade();
        }

        public void setView(VideoInfo info) {
            musicInfo = info;

            tvTitle.setText(info.getName());
            Glide.with(mContext)
                    .load(Uri.fromFile(new File(info.getPath())))
                    .apply(options)
                    .transition(transitionOptions)
                    .into(ivCover);
        }


        @Override
        public void onClick(View v) {

        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }
}
