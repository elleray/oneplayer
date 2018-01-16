package xy.media.gaogenerater;


import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class DBDaoGenerator {
    /**
     * 版本更新记录
     */
    private static int mVersion = 2;         //每次修改genenator都需修改

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(mVersion, "xy.media.oneplayer.data.greendao");
        addBaseFile(schema);
        addCenterFile(schema);
        addVideoPlayRecord(schema);
        addVideoPosition(schema);
        addVideoGroupEncrypt(schema);

        new DaoGenerator().generateAll(schema, "../app/src/main/java");
    }

    private static void addBaseFile(Schema schema) {
        Entity file = schema.addEntity("BaseFile");

        file.addIdProperty();
        file.addStringProperty("path");
        file.addStringProperty("type");
        file.addStringProperty("name");
        file.addBooleanProperty("selected");
        file.addBooleanProperty("isdirectory");
        file.addStringProperty("size");
        file.addDateProperty("time");
        file.addStringProperty("parent_folder_name");
        file.addBooleanProperty("copyright");
    }


    private static void addCenterFile(Schema schema) {
        Entity file = schema.addEntity("CenterFile");

        file.addStringProperty("path").primaryKey();
        file.addStringProperty("name");
        file.addStringProperty("type");
        file.addDateProperty("create_date");
        file.addLongProperty("duration");
        file.addIntProperty("video_type");
        file.addStringProperty("sha1");
        file.addBooleanProperty("isgodapfile");
    }

    private static void addVideoPlayRecord(Schema schema) {
        Entity file = schema.addEntity("VideoPlayRecord");

//        file.addIdProperty();
        file.addStringProperty("path").primaryKey();
        file.addLongProperty("play_time");
        file.addLongProperty("total_time");
        file.addIntProperty("left_time");
        file.addBooleanProperty("have_complete_watch_once");
    }

    private static void addVideoPosition(Schema schema){
        Entity entity = schema.addEntity("VideoPosition");
        entity.addStringProperty("path").primaryKey();
        entity.addIntProperty("position");
        entity.addStringProperty("tag");
        entity.addLongProperty("group_id");
    }

    private static void addVideoGroupEncrypt(Schema schema){
        Entity entity = schema.addEntity("VideoGroupEncrypt");
        entity.addLongProperty("id").primaryKey();
        entity.addBooleanProperty("state");
        entity.addStringProperty("key");
    }

}
