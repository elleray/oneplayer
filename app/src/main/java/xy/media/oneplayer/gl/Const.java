/*
 * *
 *  Copyright (c) 2015. Dingtone, inc. All rights reserved.
 * /
 */

package xy.media.oneplayer.gl;


/**
 * Created by tong on 16/8/3.
 */
public class Const {

    public final static int DEVICE_TYPE_HOTSPOT = 1;  // 热点
    public final static int DEVICE_TYPE_WLAN = 2;     // 局域网
    public final static int DEVICE_TYPE_INTERNET = 3; // Internet

    public final static int ROLE_HOST = 1; // host
    public final static int ROLE_CLIENT = 2; // client

    public final static int UNIT_S_TO_MS = 1000;

    public final static int APPLY_WAIT_TIME_MILLIS = 60 * UNIT_S_TO_MS;
    public final static int APPLY_COUNT_DOWN_INTERVAL_MILLISECOND = 500;  // 单位 毫秒

    public final static String SHARED_PERFERENCES_CONFIGURE = "sysConfig";

    public static class REQUEST_CODE{
        public final static int MUSIC_BROWSE = 1;
        public final static int VIDEO_BROWSE = 2;
        public final static int SELECT_FILE_LIST = 3;
        public final static int SINGLE_IMAGE = 4;
        public final static int TRANSFER_SENDER = 5;
        public final static int EDIT_PLAYLIST = 6;
        public final static int COMPLETE_DOWNLOAD_LIST_FRAGMENT = 7;
        public final static int SELECT_FILE_VIEW = 8;
        public final static int TO_SELECT_FILE_TO_SEND_ACTIVITY = 9;
        public final static int CHECK_PERMISION = 10;
    }

    public static class RESULT_CODE{
        public final static int VIDEO_PLAYER_ACTIVITY = REQUEST_CODE.VIDEO_BROWSE << 3;
        public final static int VIDEO_PLAYER_MENU_DELETE = VIDEO_PLAYER_ACTIVITY + 1;

        public final static int SELECT_FILE_LIST_OK = REQUEST_CODE.SELECT_FILE_LIST << 3;
        public final static int SELECT_FILE_LIST_HAS_DELETE_FILE = SELECT_FILE_LIST_OK + 1;
        public final static int SELECT_FILE_CHANGED = SELECT_FILE_LIST_OK + 2;
        public final static int SELECT_FILE_EMPTY = SELECT_FILE_LIST_OK + 3;
        public final static int DELETE_FILE_FAILED = SELECT_FILE_LIST_OK + 4;

        public final static int SINGLE_IMAGE_BACK = REQUEST_CODE.SINGLE_IMAGE << 3;
        public final static int SINGLE_IMAGE_DELETE = SINGLE_IMAGE_BACK + 1;
        public final static int SINGLE_IMAGE_SELECT = SINGLE_IMAGE_BACK + 2;
        public final static int SINGLE_IMAGE_CANCEL_SELECT_MODE = SINGLE_IMAGE_BACK + 3;

        public final static int TRANSFER_SENDER_BACK = REQUEST_CODE.TRANSFER_SENDER  << 5;

        public final static int BACK_TO_MUSIC_PLAYLIST  =TRANSFER_SENDER_BACK + 1;

        public static final int CHECK_PERMISSIONS_GRANTED = REQUEST_CODE.CHECK_PERMISION + 1;
        public static final int CHECK_PERMISSIONS_DENIED = REQUEST_CODE.CHECK_PERMISION + 2; // 权限拒绝
    }

}
