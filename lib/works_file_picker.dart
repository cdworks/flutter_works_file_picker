import 'dart:async';

import 'package:flutter/services.dart';

class WorksFilePicker {
  static const MethodChannel _channel =
      const MethodChannel('works_file_picker');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  //maxNumber  选择文件的最大数量
  //maxLength 能选择的最大文件大小，单位 MB 默认不限制：-1

  static Future<List>  pickFile(int barColor ,int titleColor, [int maxNumber
  = 9,
      int maxLength = -1])
  async {
    final List fileList = await _channel.invokeMethod('pickFile',
        {"barColor":barColor,"titleColor":titleColor,"maxNumber":maxNumber,"m"
            "axLength":maxLength});
    return fileList;
  }
  static openFile(int barColor ,int titleColor,String filePath)
  {
    _channel.invokeMethod('openFile',
        {"barColor":barColor,"titleColor":titleColor,"filePath":filePath});
  }
}
