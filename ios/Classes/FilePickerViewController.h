//
//  FilePickerViewController.h
//  asset_picker
//
//  Created by 李平 on 2020/3/11.
//

#import <UIKit/UIKit.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface FilePickerViewController : UIDocumentPickerViewController

@property(nonatomic,copy) FlutterResult result;


@end

NS_ASSUME_NONNULL_END
