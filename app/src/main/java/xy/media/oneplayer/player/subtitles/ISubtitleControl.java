package xy.media.oneplayer.player.subtitles;

import java.util.ArrayList;

/**
 * @descraption 字幕控制接口
 */

public interface ISubtitleControl {
    /**
     * 设置中文字幕
     *
     * @param item
     */
    void setItemSubtitleChina(String item);

    /**
     * 设置英文字幕
     *
     * @param item
     */
    void setItemSubtitleEnglish(String item);

    /**
     * 定位设置字幕
     *
     * @param position
     */
    void seekTo(int position);

    /**
     * 定位设置字幕
     *
     * @param currentTime
     */
    void seekToTime(long currentTime);

    /**
     * 设置数据
     *
     * @param list
     */
    void setData(ArrayList<SubtitlesModel> list);

    /**
     * 设置显示的语言
     *
     * @param type
     */
    void setLanguage(int type);

    /**
     * 暂停
     *
     * @param pause
     */
    void setPause(boolean pause);

    /**
     * 开始
     *
     * @param start
     */
    void setStart(boolean start);

    /**
     * 停止
     *
     * @param stop
     */
    void setStop(boolean stop);

    /**
     * 后台播放
     *
     * @param pb
     */
    void setPlayOnBackground(boolean pb);
}
