package xy.media.oneplayer.player.subtitles;

/**
 * @descraption 字幕数据结构
 */

public class SubtitlesModel {
    /**
     * 当前节点
     */
    public int node;

    /**
     * 开始显示的时间
     */
    public int star;

    /**
     * 结束显示的时间
     */
    public int end;

    /**
     * 显示的内容《英文》
     */
    public String contextE;

    /**
     * 显示的内容《中文》
     */
    public String contextC;
}
