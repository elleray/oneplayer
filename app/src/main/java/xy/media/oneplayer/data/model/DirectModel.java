package xy.media.oneplayer.data.model;

import java.util.ArrayList;


/**
 * Created by elleray on 16/8/3.
 */
public class DirectModel {
    public static final int TYPE_SDCARD = 1;
    public static final int TYPE_EXT_SDCARD = 2;
    public static final int TYPE_DOC = 5;
    public static final int TYPE_TXT = 6;
    public static final int TYPE_ZIP = 7;
    public static final int TYPE_BIG_FILE = 8;
    public static final int TYPE_VIDEO = 9;
    public static final int TYPE_APK = 10;

    private String title;
    private int icon;
    private int number;
    private String sub_title;
    private int type;

    public DirectModel(String title, String sub_title, int icon, int number, int type) {
        this.title = title;
        this.icon = icon;
        this.number = number;
        this.sub_title = sub_title;
        this.type = type;
    }

    public String getTitle() {
            return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
         this.number = number;
    }

    public String getSub_title() {
            return sub_title;
    }

    public void setSub_title(String sub_title) {
            this.sub_title = sub_title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static ArrayList<DirectModel> getDirects(int doc_num, int txt_num, int zip_num, int big_file_num, int apk_num){
            ArrayList<DirectModel> directModels = new ArrayList<DirectModel>();

            return directModels;
    }
}