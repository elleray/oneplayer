package xy.media.oneplayer.data.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import xy.media.oneplayer.data.greendao.BaseFile;
import xy.media.oneplayer.data.greendao.CenterFile;
import xy.media.oneplayer.log.log.GLog;

/**
 * Created by Administrator on 2017/3/31.
 */

public class DataCompareHelper {

    private static<T>  boolean isEmpty(ArrayList<T> model){
        return model == null || model.isEmpty();
    }

    private static  boolean isEmpty(List<CenterFile> model){
        return model == null || model.isEmpty();
    }

    /**
     * 判断appInfos1是否都在appInfos2中
     * @param appInfos1
     * @param appInfos2
     * @return
     */
    public static<T>  boolean isDBFileContainSameData(List<CenterFile> appInfos1, ArrayList<T> appInfos2){
        if(isEmpty(appInfos1) && isEmpty(appInfos2)){
            return true;
        }

        if(isEmpty(appInfos1) && ! isEmpty(appInfos2)){
            return true;
        }

        if( isEmpty(appInfos2) && ! isEmpty(appInfos1)){
            return false;
        }
        HashSet<String> paths2 = new HashSet<String>();

        for(T info : appInfos2){
            paths2.add(((BaseFile)info).getPath());
        }
        for (CenterFile appInfo : appInfos1){
            if(! paths2.contains(appInfo.getPath())){
                GLog.d("apk db detect new apk = " + appInfo.getPath());
                return false;
            }
        }

        return true;
    }

    /**
     * 判断appInfos1是否都在appInfos2中
     * @param appInfos1
     * @param appInfos2
     * @return
     */
    public static<T>  boolean  isContainSameData(ArrayList<T> appInfos1, ArrayList<T> appInfos2){
        if(isEmpty(appInfos1) && isEmpty(appInfos2)){
            return true;
        }

        if(isEmpty(appInfos1) && ! isEmpty(appInfos2)){
            return true;
        }

        if( isEmpty(appInfos2) && ! isEmpty(appInfos1)){
            return false;
        }

        HashSet<String> paths2 = new HashSet<String>();

        for(T info : appInfos2){
            paths2.add(new File(((BaseFile)info).getPath()).getPath());
        }
        for (T appInfo : appInfos1){
            File file = new File(((BaseFile)appInfo).getPath());
            if(! paths2.contains(file.getPath())){
                GLog.d("apk detect new apk = " + file.getPath());
                return false;
            }
        }

        return true;
    }

    public static <T> ArrayList<T> sum1To2(ArrayList<T> appInfos1, ArrayList<T> appInfos2){
        if(appInfos2 == null || appInfos1 == null){
            return appInfos2;
        }

        HashSet<String> paths2 = new HashSet<String>();

        for(T info : appInfos2){
            paths2.add(((BaseFile)info).getPath());
        }
        for (T info : appInfos1){
            if(! paths2.contains(((BaseFile)info).getPath())){
                appInfos2.add(info);
            }
        }

        return appInfos2;
    }
}
