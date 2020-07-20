package com.works.works_file_picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** WorksFilePickerPlugin */
public class WorksFilePickerPlugin implements FlutterPlugin, ActivityAware, PluginRegistry.ActivityResultListener,MethodCallHandler {

  static final int REQUEST_FILE_PICK = 3555;

  static  WorksFilePickerPlugin worksFilePickerPlugin;

  private Activity activity;
  private Context applicationContext;

  private Result mResult;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    if(worksFilePickerPlugin == null)
    {
      worksFilePickerPlugin = this;
      worksFilePickerPlugin.applicationContext = flutterPluginBinding.getApplicationContext();
      FileUtil.applicationContext =  worksFilePickerPlugin.applicationContext;

      final MethodChannel channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "works_file_picker");
      channel.setMethodCallHandler(this);

    }
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    if (registrar.activity() == null) {
      // If a background flutter view tries to register the plugin, there will be no activity from the registrar,
      // we stop the registering process immediately because the ImagePicker requires an activity.
      return;
    }

    if(worksFilePickerPlugin == null) {
      final MethodChannel channel = new MethodChannel(registrar.messenger(), "works_file_picker");
      worksFilePickerPlugin = new WorksFilePickerPlugin();
      worksFilePickerPlugin.applicationContext = registrar.context();
      FileUtil.applicationContext =  worksFilePickerPlugin.applicationContext;
      worksFilePickerPlugin.activity = registrar.activity();
      registrar.addActivityResultListener(worksFilePickerPlugin);
      channel.setMethodCallHandler(worksFilePickerPlugin);
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("pickFile")) {

      mResult = result;
      Map info = (Map) call.arguments;
      long barColor = (long) info.get("barColor");
      long titleColor = (long) info.get("titleColor");

      Intent intent = new Intent(activity, WorksFilePickerActivity.class);
      intent.putExtra("barColor",(int)barColor);
      intent.putExtra("titleColor",(int)titleColor);

      if(info.containsKey("maxNumber"))
      {
        intent.putExtra("maxNumber",(int) info.get("maxNumber"));
      }

      if(info.containsKey("maxLength"))
      {
        intent.putExtra("maxLength",(int) info.get("maxLength"));
      }


      activity.startActivityForResult(intent,REQUEST_FILE_PICK);

    }
    else if (call.method.equals("openFile"))
    {
//      mResult = result;
      Map info = (Map) call.arguments;
      long barColor = (long) info.get("barColor");
      long titleColor = (long) info.get("titleColor");

      String displayName = "";
      if(info.containsKey("fileName"))
      {
        displayName = (String)info.get("fileName");
      }

      Intent intent = new Intent(activity, WorksFilePickerActivity.class);
      intent.putExtra("barColor",(int)barColor);
      intent.putExtra("titleColor",(int)titleColor);
      intent.putExtra("displayName",displayName);

      FileOpenUtil.openFile(activity,new File((String)info.get("filePath")),
              (int)barColor,(int)titleColor,displayName);

    }
    else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    if(worksFilePickerPlugin == this)
    {
      activity = binding.getActivity();
      binding.addActivityResultListener(this);
    }
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {

  }

  @Override
  public void onDetachedFromActivity() {

  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

    if(requestCode == REQUEST_FILE_PICK )
    {
      List filePaths = null;
      if(resultCode == 1)
      {
        List<File> files = (List<File>) data.getSerializableExtra("files");
        filePaths  =  new ArrayList<>();
        for(File file : files)
        {
          String filePath = file.getPath();
          String lowerPath = filePath.toLowerCase();

          Map<String,Object> fileInfo = new HashMap();
          fileInfo.put("identifier",file.getPath());
          fileInfo.put("size",file.length());


          if(lowerPath.endsWith(".png") || lowerPath.endsWith(".jpeg") ||
                  lowerPath
                          .endsWith(".gif") || lowerPath
                  .endsWith(".bmp") || lowerPath
                  .endsWith(".jpg"))
          {
            fileInfo.put("type","1");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            fileInfo.put("originalWidth", options.outWidth);
            fileInfo.put("originalHeight",options.outHeight);

          }
          else if(lowerPath.endsWith(".mp4"))
          {
            fileInfo.put("type","2");

            File thumbPath = new File(FileUtil.getImagePath(null), "thvideo" + System.currentTimeMillis());

            try {
              FileOutputStream fos = new FileOutputStream(thumbPath);
              Bitmap ThumbBitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
              ThumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
              fos.close();
              HashMap info = new HashMap();
              info.put("thumbWidth",(double)ThumbBitmap.getWidth());
              info.put("thumbHeight",(double)ThumbBitmap.getHeight());
              ThumbBitmap.recycle();

              MediaMetadataRetriever retriever = new MediaMetadataRetriever();
              retriever.setDataSource(filePath); //在获取前，设置文件路径（应该只能是本地路径）
              String duration =       retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
              retriever.release(); //释放

              info.put("duration",Long.parseLong(duration) / 1000.0);
              info.put("thumbUrl",file.getPath());
              fileInfo.putAll(info);

            } catch (Exception e) {
              HashMap info = new HashMap();
              info.put("duration",(double)0);
              fileInfo.putAll(info);

            }

          }
          else
          {
            fileInfo.put("type","0");

          }

          filePaths.add(fileInfo);
        }
      }
      mResult.success(filePaths);
      mResult = null;

    }

    return false;
  }


}
