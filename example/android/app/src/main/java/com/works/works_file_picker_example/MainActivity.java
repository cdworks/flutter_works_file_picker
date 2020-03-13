package com.works.works_file_picker_example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (Build.VERSION.SDK_INT >= 23) {
      int REQUEST_CODE_CONTACT = 101;
      String[] permissions = {
              Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE,
      };
      //验证是否许可权限
      for (String str : permissions) {
        if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
          //申请权限
          this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
          return;
        }
      }
    }
  }

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    GeneratedPluginRegistrant.registerWith(flutterEngine);
  }
}
