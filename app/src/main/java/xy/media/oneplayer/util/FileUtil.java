/*
 * *
 *  Copyright (c) 2015. Dingtone, inc. All rights reserved.
 * /
 */

package xy.media.oneplayer.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;

import xy.media.oneplayer.data.FileBaseDataHelper;
import xy.media.oneplayer.data.greendao.BaseFile;
import xy.media.oneplayer.log.log.GLog;

/*文件工具类*/
public class FileUtil {
    //基本类型
    public static final String FILE_TYPE_FOLDER = "folder";
    public static final String FILE_TYPE_APK = "apk";
    public static final String FILE_TYPE_VIDEO = "video";
    public static final String FILE_TYPE_AUDIO = "audio";
    public static final String FILE_TYPE_PICTURE = "image";
    public static final String FILE_TYPE_FILE = "document";

    public static final String FILE_TYPE_TEXT = "text";
    public static final String FILE_TYPE_PDF = "pdf";
    public static final String FILE_TYPE_ZIP = "zip";
    public static final String FILE_TYPE_OTHER = "other";
    public static final String FILE_TYPE_DOC = "doc";
    public static final String FILE_TYPE_EBOOK = "ebook";

    public static final String FILE_BIG_FILE = "big_file";

    public static final String FILE_GODAP_VIDEO_TYPE = "gdv";

    public static boolean isExist(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            GLog.e(e.toString());
            return false;
        }
        return true;
    }

    /**
     * 获得文件类型
     *
     * @param name
     * @return
     */
    public static String getFileType(String name) {
        String type = "";
        if (TextUtils.isEmpty(name)) {
            return type;
        }
        String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (end.equals("apk")) {
            return FILE_TYPE_APK;
        } else if (end.equals("mp4") || end.equals("avi") || end.equals("3gp")
                || end.equals("rmvb") || end.equals("ts") || end.equals("f4v")
                || end.equals("mov") || end.equals("wmv") || end.equals("mpg")) {
            type = FILE_TYPE_VIDEO;
        } else if (end.equals("mp3") || end.equals("mid") || end.equals("wav")
                || end.equals("flac") || end.equals("m4a") || end.equals("ogg")
                || end.equals("amr") || end.equals("mpeg") || end.equals("aac")) {
            type = FILE_TYPE_AUDIO;
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
                || end.equals("jpeg") || end.equals("bmp")) {
            type = FILE_TYPE_PICTURE;
        } else if (end.equals("txt") || end.equals("log")) {
            type = FILE_TYPE_TEXT;
        } else if (end.equals("zip") || end.equals("rar")) {
            type = FILE_TYPE_ZIP;
        } else if (end.equals("doc")
                || end.equals("docx")
                || end.equals("ppt")
                || end.equals("pptx")
                || end.equals("els")
                || end.equals("elsx")
                || end.equals("xls")
                || end.equals("xlsx")
                || end.equals("pdf")) {
            type = FILE_TYPE_DOC;
        } else if (end.equals("pdf")) {
            type = FILE_TYPE_PDF;
        } else if (end.equals("gdv")) {
            type = FILE_GODAP_VIDEO_TYPE;
        } else {
            type = FILE_TYPE_OTHER;
        }
        return type;
    }


    public static String getFormatType(String name) {
        String type = "";
        if (TextUtils.isEmpty(name)) {
            return type;
        }

        //增加文件类型
        if ((new File(name)).isDirectory()) {
            return FileUtil.FILE_TYPE_FOLDER;
        }

        if (name.endsWith(".gdv.temp")) {
            return FILE_TYPE_VIDEO;
        }

        if(name.endsWith(".temp")){
            name = name.substring(0, name.lastIndexOf("."));
        }

        String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (end.equals("apk")) {
            return FILE_TYPE_APK;
        } else if (end.equals("mp4") || end.equals("avi") || end.equals("3gp")
                || end.equals("rmvb") || end.equals("gdv") || end.equals("ts") || end.equals("f4v")
                || end.equals("mkv") || end.equals("mov") || end.equals("wmv") || end.equals("mpg")) {
            type = FILE_TYPE_VIDEO;
        } else if (end.equals("mp3") || end.equals("mid") || end.equals("wav")
                || end.equals("flac") || end.equals("m4a") || end.equals("ogg")
                || end.equals("amr") || end.equals("mpeg") || end.equals("aac")) {
            type = FILE_TYPE_AUDIO;
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
                || end.equals("jpeg") || end.equals("bmp")) {
            type = FILE_TYPE_PICTURE;
        } else if (end.equals("txt") || end.equals("log")) {
            type = FILE_TYPE_TEXT;
        } else if (end.equals("zip") || end.equals("rar")) {
            type = FILE_TYPE_ZIP;
        } else if (end.equals("doc")
                || end.equals("docx")
                || end.equals("ppt")
                || end.equals("pptx")
                || end.equals("els")
                || end.equals("elsx")
                || end.equals("xls")
                || end.equals("xlsx")
                || end.equals("pdf")
                ) {
            type = FILE_TYPE_DOC;
        } else if (end.equals("pdf")) {
            type = FILE_TYPE_PDF;
        } else {
            type = FILE_TYPE_OTHER;
        }
        return type;
    }

    public static boolean isVideoUri(String uri){
        return getFormatType(uri).equals(FILE_TYPE_VIDEO);
    }

    public static boolean isVideo(BaseFile baseFile){
        return getFileType(baseFile.getPath()).equals(FILE_TYPE_VIDEO);
    }

    /**
     * 转换文件大小
     **/
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");//保留2位小数
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = fileS + " B";
        } else if (fileS < 1024 * 1024) {
            fileSizeString = df.format((double) fileS / 1024) + " K";
        } else if (fileS < 1024 * 1024 * 1024) {
            fileSizeString = df.format((double) fileS / 1024 / 1024) + " M";
        } else {
            fileSizeString = df.format((double) fileS / 1024 / 1024 / 1024) + " G";
        }
        return fileSizeString;
    }

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    //删除文件夹和文件夹里面的文件
    public static boolean deleteDir(final String pPath) {
        File dir = new File(pPath);
        return deleteDirWithFile(dir);
    }

    public static boolean deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return false;

        boolean f1 = true, f2 = true, f3 = true;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                f1 = file.delete(); // 删除所有文件
            else if (file.isDirectory())
                f2 = deleteDirWithFile(file); // 递规的方式删除文件夹
        }
        f3 = dir.delete();// 删除目录本身

        return f1& f2 & f3;
    }

    /**
     * 移动文件
     **/
    public static boolean moveFile(File src, File dest) throws Exception {
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        return src.renameTo(dest);
    }

    /**
     * 复制文件
     * @param src   源文件
     * @param dest  目标文件
     */
    @TargetApi(19)
    public static void copyFile(File src, File dest) throws IOException {
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dest)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    public static boolean isGodapFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        // 匹配文件路径以.gdv结尾或者.gdv.temp结尾
        return path.endsWith("." + FileUtil.FILE_GODAP_VIDEO_TYPE) || path.endsWith("." + FileUtil.FILE_GODAP_VIDEO_TYPE + ".temp");
    }

    /**
     * 打开图片
     * @param path 要打开的图片model
     * @param list 当前目录下的图片列表
     * @param activity
     * @param <T>
     */
    public static<T> void openFile(final String path, ArrayList<T> list, Activity activity, OpenFileCallback callback) {
        BaseFile baseFile = FileBaseDataHelper.getBaseFileByPath(path);
        openFile(baseFile,  list, false, activity, callback);
    }

    /**
     * 打开文件操作
     * @param path 文件路径
     * @param activity
     */
    public static void openFile(String path, Activity activity, OpenFileCallback callback) {
        openFile(path, false, activity, callback);
    }

    /**
     * 打开文件，可以控制是否显示视频、音频播放器中的发送文件按钮
     * @param path 文件路径
     * @param isHideSendbtn true不显示send安按钮，false显示send按钮
     * @param activity
     */
    public static void openFile(String path, boolean isHideSendbtn, Activity activity, OpenFileCallback callback) {
        BaseFile baseFile = FileBaseDataHelper.getBaseFileByPath(path);
        openFile(baseFile, null, isHideSendbtn, activity, callback);
    }

    public static long getFileLengthKb(final String path) {
        if(TextUtils.isEmpty(path)){
            return 0;
        }
        return new File(path).length() / 1000;
    }

    public static interface OpenFileCallback<T>{
        void onResult();
        void finishInstall();
    }

    public static<T> void openFile(final String path, ArrayList<T> list, boolean isHideSendBtn, Activity activity, OpenFileCallback callback){
        BaseFile baseFile = FileBaseDataHelper.getBaseFileByPath(path);
        openFile(baseFile, list, isHideSendBtn, activity, callback);
    }

    public static<T> void openFile(final BaseFile fileInfo, ArrayList<T> list, boolean isHideSendBtn, Activity activity, OpenFileCallback callback) {
        if(fileInfo == null){
            return;
        }
        if (fileInfo.getIsdirectory()) {
        } else {
        }
    }

    public static void openVideo(final String path, float downloadPercent, boolean isHideSendBtn, boolean isShowShareDetailBtn, long shareId, int shareDetailNum, Activity activity){

    }

    public static interface ZipCallBack {
        void openZipFiles(String path);
    }


    public static boolean deleteBaseFile(String path, Context context) {
        File file0 = new File(path);
        if (!file0.exists()) {
            return false;
        }

        if(file0.isDirectory()){
            return deleteDir(path);
        }

        //这里先rename一个temp文件再做删除，解决某些文件删除后出现0kb文件的情况
        File temp = new File(file0.getAbsolutePath() + ".temp");
        File deleteFile = file0.renameTo(temp) ? temp : file0;

        if (deleteFile.delete()) {
            try {
                context.getContentResolver().delete(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.Media.DATA + " = '" + file0.getAbsolutePath().replace("'","''") + "'", null);
                return true;

            }catch (Exception e){
                GLog.e(e.toString());
                return false;
            }
        } else {
            return false;
        }
    }

    public static String getSimpleName(String name) {
        int pointPos = name.lastIndexOf(".");
        if (pointPos <= 0) {
            return name;
        }
        return name.substring(0, pointPos);
    }

    public static String getFileNameSuffix(String name) {
        int pointPos = name.lastIndexOf(".");
        if (pointPos <= 0) {
            return "";
        }
        return name.substring(pointPos, name.length());
    }

    public static void mkDir(final String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return;
        }

        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static class FileComparator implements Comparator<BaseFile> {

        @Override
        public int compare(BaseFile o1, BaseFile o2) {
            // 降序排列
            int result = getFileTypePriority(FileUtil.getFormatType(o1.getPath())) - getFileTypePriority(FileUtil.getFormatType(o2.getPath()));

            if (result == 0) {
                return compareFileName(o1.getName(), o2.getName());
            } else {
                return result;
            }
        }
    }

    public static class FileCreateTimeComparator implements Comparator<BaseFile> {

        @Override
        public int compare(BaseFile o1, BaseFile o2) {
                return o2.getTime().compareTo(o1.getTime());
        }
    }

    /**
     * 数值越小，优先级越高
     *
     * @param fileType
     * @return
     */
    public static int getFileTypePriority(String fileType) {
        int priority = 9;
        switch (fileType) {
            case FILE_TYPE_FOLDER:
                priority = 1;
                break;
            case FileUtil.FILE_TYPE_VIDEO:
                priority = 2;
                break;
            case FileUtil.FILE_TYPE_AUDIO:
                priority = 3;
                break;
            case FileUtil.FILE_TYPE_PICTURE:
                priority = 4;
                break;
            case FileUtil.FILE_TYPE_APK:
                priority = 5;
                break;
            case FileUtil.FILE_TYPE_DOC:
                priority = 6;
                break;
            case FileUtil.FILE_TYPE_TEXT:
                priority = 7;
                break;
            case FileUtil.FILE_TYPE_ZIP:
                priority = 8;
                break;
        }
        return priority;
    }

    /**
     * 文件优先级相同时，再比较文件名
     *
     * @param filename
     * @param compareName
     * @return
     */
    public static int compareFileName(String filename, String compareName) {
        if (TextUtils.isEmpty(filename)) {
            return -1;
        } else if (TextUtils.isEmpty(compareName)) {
            return 1;
        }
        return filename.compareTo(compareName);
    }

    /**
     * Get the md5 value of the filepath specified file
     *
     * @param filePath The filepath of the file
     * @return The md5 value
     */
    public static String fileToMD5(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath); // Create an FileInputStream instance according to the filepath
            byte[] buffer = new byte[1024]; // The buffer to read the file
            MessageDigest digest = MessageDigest.getInstance("MD5"); // Get a MD5 instance
            int numRead = 0; // Record how many bytes have been read
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead); // Update the digest
            }
            byte[] md5Bytes = digest.digest(); // Complete the hash computing
            return convertHashToString(md5Bytes); // Call the function to convert to hex digits
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close(); // Close the InputStream
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Convert the hash bytes to hex digits string
     *
     * @param hashBytes
     * @return The converted hex digits string
     */
    private static String convertHashToString(byte[] hashBytes) {
        String returnVal = "";
        for (int i = 0; i < hashBytes.length; i++) {
            returnVal += Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1);
        }
        return returnVal.toLowerCase();
    }

    public static String getNotSameName(String path) {
        String newPath = "";
        try {
            int index = 1;
            while (true) {
                int pointPos = path.lastIndexOf(".");
                if (pointPos <= 0) {
                    newPath = path + "-" + index;
                } else {
                    newPath = path.substring(0, pointPos) + "-" + index + path.substring(pointPos, path.length());
                }

                if (!isExist(newPath)) {
                    break;
                }
                index++;
            }

            GLog.d("new path : " + newPath);

        } catch (Exception e) {
            GLog.e(e.toString());
        }
        return newPath;
    }

    public static boolean isInExtSdCard(String path) {
        if (!TextUtil.isNull(path) && DeviceInfo.getInstance().isExistSDCard()) {
            return path.contains(DeviceInfo.getInstance().getExtSDCardPath());
        }
        return false;
    }


    public static boolean isBigFile(File file){
        if(file != null && file.exists()){
            long len = file.length();
            return len > FileSizeUtil.BIG_FILE_SIZE_VALUE;
        }
        return  false;
    }

    public static boolean isBigFile(String path){
        if(TextUtil.isNull(path)){
            return false;
        }

        File file = new File(path);
        return isBigFile(file);
    }

    public static boolean isPicture(BaseFile baseFile){
        if(baseFile == null){
            throw new NullPointerException();
        }
        if(FileUtil.getFileType(baseFile.getName()).equals(FileUtil.FILE_TYPE_PICTURE)){
            return true;
        } else {
            return false;
        }
    }


    /**
     * 为原始文件名加数字标号
     * @param fileName  原始文件名
     * @param index     标号数字
     */
    public static String generateFileNameWithIndex(String fileName, int index) {
        if (TextUtils.isEmpty(fileName)) {
            return "(" + index + ")";
        }
        int indexOfDot = fileName.lastIndexOf(".");
        if (indexOfDot == -1) {
            return fileName + "(" + index + ")";
        }
        return fileName.substring(0, indexOfDot) + "(" + index + ")" + fileName.substring(indexOfDot);
    }

    /**
     * 判断指定路径的文件是否存在
     */
    public static boolean checkFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        return file.exists();
    }

}
