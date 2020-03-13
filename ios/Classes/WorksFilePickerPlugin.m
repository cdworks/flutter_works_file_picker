#import "WorksFilePickerPlugin.h"
#import "FilePickerViewController.h"

@interface WorksFilePickerPlugin()<UIDocumentInteractionControllerDelegate>

@property(nonatomic,weak)UIViewController* controller;

@property(nonatomic,strong)UIDocumentInteractionController* fileInteractionController;

@end

@implementation WorksFilePickerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"works_file_picker"
            binaryMessenger:[registrar messenger]];
  WorksFilePickerPlugin* instance = [[WorksFilePickerPlugin alloc] init];
    instance.controller = UIApplication.sharedApplication.delegate.window.rootViewController;
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"pickFile" isEqualToString:call.method]) {
      
    NSArray *documentTypes = @[@"public.content", @"public.text", @"public.source-code",@"public.data", @"public.image",@"public.archive", @"public.audiovisual-content", @"com.adobe.pdf", @"com.apple.keynote.key",@"public.movie", @"com.microsoft.word.doc",@"com.microsoft.word.docx", @"com.microsoft.excel.xls",@"com.microsoft.excel.xlsx", @"com.microsoft.powerpoint.ppt",@"com.microsoft.powerpoint.pptx"];
    FilePickerViewController* documentPicker = [[FilePickerViewController alloc] initWithDocumentTypes:documentTypes inMode:UIDocumentPickerModeOpen];
    //            UIDocumentPickerViewController *documentPicker = [[UIDocumentPickerViewController alloc] initWithDocumentTypes:documentTypes inMode:UIDocumentPickerModeOpen];
      documentPicker.result = result;
      
      if (@available(iOS 11.0, *)) {
          documentPicker.allowsMultipleSelection = YES;
      } else {
          // Fallback on earlier versions
      }
    
    
    documentPicker.modalPresentationStyle = UIModalPresentationFormSheet;
    
    [_controller presentViewController:documentPicker animated:YES completion:nil];
  }
else if ([@"openFile" isEqualToString:call.method]) {
    
    
    NSDictionary* info = call.arguments;
    NSString* filePaht = info[@"filePath"];
    self.fileInteractionController.URL = [NSURL fileURLWithPath:filePaht];
    
    
    BOOL canPreview =  [self.fileInteractionController presentPreviewAnimated:YES];
    //无法打开提示
    if (!canPreview) {
        canPreview = [self.fileInteractionController presentOpenInMenuFromRect:_controller.view.frame inView:_controller.view animated:YES];
        if(!canPreview)
        {
            UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"提示" message:@"无可打开此文件的应用!" preferredStyle:UIAlertControllerStyleAlert];
            UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil];
            [alertController addAction:okAction];
            [_controller presentViewController:alertController animated:YES completion:nil];
        }
    }
    
}
  else {
    result(FlutterMethodNotImplemented);
  }
}

- (UIDocumentInteractionController *)fileInteractionController
{
    if(!_fileInteractionController)
    {
        _fileInteractionController = [[UIDocumentInteractionController alloc] init];
        _fileInteractionController.delegate = self;
    }
    return _fileInteractionController;
}

- (UIViewController *)documentInteractionControllerViewControllerForPreview:(UIDocumentInteractionController *)controller {
    return _controller;
}

- (void)documentInteractionControllerDidEndPreview:(UIDocumentInteractionController *)controller
{
    _fileInteractionController = nil;
}


@end
