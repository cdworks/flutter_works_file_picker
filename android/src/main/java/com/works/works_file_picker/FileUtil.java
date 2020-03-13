package com.works.works_file_picker;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class FileUtil {
    private static File storageDir = null;
    private static File videoPath = null;
    public static Context applicationContext = null;

    private static File getStorageDir() {

        if (storageDir == null) {
            //try to use sd card if possible
            File sdPath = Environment.getExternalStorageDirectory();
            if (sdPath.exists()) {
                return sdPath;
            }
            //use application internal storage instead
            storageDir = applicationContext.getFilesDir();
        }
        return storageDir;
    }

    public  static String getVideoPath(String subPath)
    {
        if (videoPath == null) {

            String appPackageName = applicationContext.getPackageName();
            String pathPrefix = "/Android/data/" + appPackageName + "/";
            videoPath = new File(getStorageDir(), pathPrefix);
        }

        String path = "";
        if (subPath != null && !subPath.isEmpty()) {
            path += "subPath/";
        }
        path += "video/";

        File videoPathFile = new File(videoPath, path);
        if (!videoPathFile.exists()) {
            videoPathFile.mkdirs();
        }
        return videoPathFile.getPath();
    }

    public  static String getImagePath(String subPath)
    {
        if (videoPath == null) {

            String appPackageName = applicationContext.getPackageName();
            String pathPrefix = "/Android/data/" + appPackageName + "/";
            videoPath = new File(getStorageDir(), pathPrefix);
        }

        String path = "";
        if (subPath != null && !subPath.isEmpty()) {
            path += "subPath/";
        }
        path += "image/";

        File imagePathFile = new File(videoPath, path);
        if (!imagePathFile.exists()) {
            imagePathFile.mkdirs();
        }
        return imagePathFile.getPath();
    }
}
