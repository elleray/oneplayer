/*
 * *
 *  Copyright (c) 2015. Dingtone, inc. All rights reserved.
 * /
 */

package xy.media.oneplayer.data.model;

import xy.media.oneplayer.data.greendao.BaseFile;


/**
 * Created by yangwenjie on 16/7/4.
 */
public class VideoInfo extends BaseFile {
    private int play_station;
    private String parentPath;
    private long left_time;
    private long total_time;
    private boolean is_finish_watch;
    private int headerId;
    private int tab_id;
    private int godap_download_id;
    private String simple_name;
    private float download_percent;
    private float download_speed;

    public int getPlay_station() {
        return play_station;
    }

    public void setPlay_station(int play_station) {
        this.play_station = play_station;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public VideoInfo() {
    }

    public long getLeft_time() {
        return left_time;
    }

    public void setLeft_time(long left_time) {
        this.left_time = left_time;
    }

    public long getTotal_time() {
        return total_time;
    }

    public void setTotal_time(long total_time) {
        this.total_time = total_time;
    }

    public boolean is_finish_watch() {
        return is_finish_watch;
    }

    public void setIs_finish_watch(boolean is_finish_watch) {
        this.is_finish_watch = is_finish_watch;
    }

    public int getHeaderId() {
        return headerId;
    }

    public void setHeaderId(int headerId) {
        this.headerId = headerId;
    }

    public int getTab_id() {
        return tab_id;
    }

    public void setTab_name(int tab_id) {
        this.tab_id = tab_id;
    }

    public int getGodap_download_id() {
        return godap_download_id;
    }

    public void setGodap_download_id(int godap_download_id) {
        this.godap_download_id = godap_download_id;
    }

    public String getSimple_name() {
        return simple_name;
    }

    public void setSimple_name(String simple_name) {
        this.simple_name = simple_name;
    }

    public float getDownload_percent() {
        return download_percent;
    }

    public void setDownload_percent(float download_percent) {
        this.download_percent = download_percent;
    }

    public float getDownload_speed() {
        return download_speed;
    }

    public void setDownload_speed(float download_speed) {
        this.download_speed = download_speed;
    }
}
