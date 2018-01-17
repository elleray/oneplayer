/*
 * *
 *  Copyright (c) 2015. Dingtone, inc. All rights reserved.
 * /
 */

package xy.media.oneplayer.util;

import android.content.Context;
import android.widget.Toast;

import xy.media.oneplayer.gl.Global;


/**
 * Created by elleray on 16/6/30.
 */
public class ToastUtil {

    public static void showToast(Context context, String string){
        Toast.makeText(context, string , Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int id){
        Toast.makeText(context, id, Toast.LENGTH_SHORT).show();
    }

    private static Toast sToast;

    public static void show(String text, int duration) {
        if (sToast != null) {
            sToast.cancel();
        }
        sToast = Toast.makeText(Global.context, text, duration);
        sToast.show();
    }

    public static void show(String text) {
        ToastUtil.show(text, Toast.LENGTH_SHORT);
    }

    public static void show(int text, int duration) {
        if (sToast != null) {
            sToast.cancel();
        }
        sToast = Toast.makeText(Global.context, text, duration);
        sToast.show();
    }

    public static void show(int text) {
        ToastUtil.show(text, Toast.LENGTH_SHORT);
    }

    public static void hide(){
        if (sToast != null) {
            sToast.cancel();
        }
    }

}
