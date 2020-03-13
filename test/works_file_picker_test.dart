import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:works_file_picker/works_file_picker.dart';

void main() {
  const MethodChannel channel = MethodChannel('works_file_picker');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await WorksFilePicker.platformVersion, '42');
  });
}
