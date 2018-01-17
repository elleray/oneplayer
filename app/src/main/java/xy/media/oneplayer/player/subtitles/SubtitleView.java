package xy.media.oneplayer.player.subtitles;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import xy.media.oneplayer.R;
import xy.media.oneplayer.gl.Global;
import xy.media.oneplayer.log.log.GLog;

/**
 * @descraptio 显示字幕的图层
 */
public class SubtitleView extends LinearLayout implements ISubtitleControl, SubtitleClickListener {
    /**
     * 只显示中文
     */
    public final static int LANGUAGE_TYPE_CHINA = 0;

    /**
     * 只显示英文
     */
    public final static int LANGUAGE_TYPE_ENGLISH = LANGUAGE_TYPE_CHINA + 1;

    /**
     * 双语显示
     */
    public final static int LANGUAGE_TYPE_BOTH = LANGUAGE_TYPE_ENGLISH + 1;

    /**
     * 不显示字幕
     */
    public final static int LANGUAGE_TYPE_NONE = LANGUAGE_TYPE_BOTH + 1;

    /**
     * 更新UI
     */
    private static int UPDATE_SUBTITLE = LANGUAGE_TYPE_NONE + 1;

    /**
     * 字幕所有的数据
     */
    private ArrayList<SubtitlesModel> data = new ArrayList<>();

    /**
     * 中文字幕
     */
    private SubtitleTextView subChina;

    /**
     * 英文字幕
     */
    private SubtitleTextView subEnglish;

    /**
     * 当前显示节点
     */
    private View subTitleView;

    /**
     * 单条字幕数据
     */
    private SubtitlesModel model = null;

    /**
     * 后台播放
     */
    private boolean palyOnBackground = false;

    private Context context;

    public SubtitleView(Context context)
    {
        this(context, null);
    }

    public SubtitleView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        subTitleView = View.inflate(context, R.layout.subtitleview, null);
        subChina = (SubtitleTextView) subTitleView.findViewById(R.id.sub_title_china);
        subEnglish = (SubtitleTextView) subTitleView.findViewById(R.id.sub_title_english);
        subChina.setSubtitleOnTouchListener(this);
        subEnglish.setSubtitleOnTouchListener(this);
        this.setOrientation(VERTICAL);
        this.setGravity(Gravity.BOTTOM);
        this.addView(subTitleView);
    }

    @Override
    public void setItemSubtitleChina(String item)
    {
        subChina.setText(item);
    }

    @Override
    public void setItemSubtitleEnglish(String item)
    {
        subEnglish.setText(item);
    }

    @Override
    public void seekTo(int position) {
        if (data != null && !data.isEmpty()) {
            model = searchSub(data, position);
        }
        GLog.d( ":" + position + "/" + data.get(data.size() - 1).end);
        if (model != null) {
            setItemSubtitleChina(model.contextC);
            setItemSubtitleEnglish(model.contextE);
        } else {
            setItemSubtitleChina("");
            setItemSubtitleEnglish("");
        }
    }

    @Override
    public void seekToTime(long currentTime) {
        if (data != null && !data.isEmpty()) {
            model = searchSubByTime(data, currentTime);
        }
        if (model != null) {
            setItemSubtitleChina(model.contextC);
            setItemSubtitleEnglish(model.contextE);
        } else {
            setItemSubtitleChina("");
            setItemSubtitleEnglish("");
        }
    }

    @Override
    public void setData(ArrayList<SubtitlesModel> list) {
        if (list == null || list.size() <= 0) {
            GLog.e( "subtitle data is empty");
            return;
        }
        this.data = list;

        seekTo(0);
    }

    @Override
    public void setLanguage(int type) {
        if (type == LANGUAGE_TYPE_CHINA) {
            subChina.setVisibility(View.VISIBLE);
            subEnglish.setVisibility(View.GONE);
        } else if (type == LANGUAGE_TYPE_ENGLISH) {
            subChina.setVisibility(View.GONE);
            subEnglish.setVisibility(View.VISIBLE);
        } else if (type == LANGUAGE_TYPE_BOTH) {
            subChina.setVisibility(View.VISIBLE);
            subEnglish.setVisibility(View.VISIBLE);
        } else {
            subChina.setVisibility(View.GONE);
            subEnglish.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        GLog.d( "onWindowFocusChanged:" + hasWindowFocus);
    }

    @Override
    public void setPause(boolean pause) {
    }

    @Override
    public void setStart(boolean start) {
    }

    @Override
    public void setStop(boolean stop) {
    }

    @Override
    public void setPlayOnBackground(boolean pb)
    {
        this.palyOnBackground = pb;
    }

    /**
     * 采用二分法去查找当前应该播放的字幕
     *
     * @param list 全部字幕
     * @param key 播放的时间点
     * @return
     */
    public static SubtitlesModel searchSub(ArrayList<SubtitlesModel> list, int key) {
        int start = 0;
        int end = list.size() - 1;
        while (start <= end) {
            int middle = (start + end) / 2;
            if (key < list.get(middle).star) {
                if (key > list.get(middle).end) {
                    return list.get(middle);
                }
                end = middle - 1;
            }
            else if (key > list.get(middle).end) {
                if (key < list.get(middle).star) {
                    return list.get(middle);
                }
                start = middle + 1;
            }
            else if (key >= list.get(middle).star && key <= list.get(middle).end) {
                return list.get(middle);
            }
        }
        return null;
    }

    public static SubtitlesModel searchSubByTime(ArrayList<SubtitlesModel> list, long time) {
        GLog.d("Start  search video subtitile time = " + time);
        SubtitlesModel model = searchSubByTime(list, time, 0, list.size() -1);
        GLog.d("End search video subtitle time = " + time);
        return model;
    }

    public static SubtitlesModel searchSubByTime(ArrayList<SubtitlesModel> list, long time, int start, int end) {
        while (start <= end) {
            int middle = (start + end) / 2;
            SubtitlesModel middleModel = list.get(middle);
            int middleStart = middleModel.star;
            int middleEnd = middleModel.end;

            if(time < middleStart) {
                return searchSubByTime(list, time, start, middle -1);
            } else if(time > middleEnd) {
                return searchSubByTime(list, time, middle+1, end);
            } else {
                return middleModel;
            }
        }
        return null;
    }

    @Override
    public void ClickDown()
    {
        Toast.makeText(context, "ClickDown", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void ClickUp()
    {
        Toast.makeText(context, "ClickUp", Toast.LENGTH_SHORT).show();
    }
}
