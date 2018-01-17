package xy.media.oneplayer.player.subtitles;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * @descraption 显示字幕的字体样式
 */

public class SubtitleTextView extends TextView implements View.OnTouchListener {
    private Context context;
    private SubtitleClickListener listener;

    public SubtitleTextView(Context context) {
        this(context, null);
    }

    public SubtitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        // 默认白色字体
        setTextColor(Color.WHITE);
        setSingleLine(true);
        setShadowLayer(3, 0, 0, Color.RED);
        this.setOnTouchListener(this);
    }

    public void setSubtitleOnTouchListener(SubtitleClickListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (listener != null)
                    listener.ClickDown();
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null)
                    listener.ClickUp();
                break;
        }
        return true;
    }
}

