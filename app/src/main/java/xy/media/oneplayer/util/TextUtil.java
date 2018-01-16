/*
 * *
 *  Copyright (c) 2015. Dingtone, inc. All rights reserved.
 * /
 */

package xy.media.oneplayer.util;

/**
 * Created by elleray on 16/6/23.
 */
public class TextUtil {

    public static boolean isNull(String str){
        if(str == null || str.equals("")){
            return true;
        } else {
            return false;
        }
    }
}
