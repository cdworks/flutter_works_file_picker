//
//  FilePickerViewController.m
//  asset_picker
//
//  Created by 李平 on 2020/3/11.
//

#import "FilePickerViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <CommonCrypto/CommonDigest.h> 

@interface FilePickerViewController ()<UIDocumentPickerDelegate>

@end

@implementation FilePickerViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.delegate = self;

}

+ (NSString*)md5:(NSString*)str

{
    const char*concat_str = [str UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH];
    CC_MD5(concat_str, (unsigned int)strlen(concat_str), result);
    NSMutableString *hash = [NSMutableString string];
    for(int i =0; i <CC_MD5_DIGEST_LENGTH; i++){
        [hash appendFormat:@"%02X", result[i]];
        
    }
    return [hash uppercaseString];
    
}

- (void)documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentsAtURLs:(NSArray <NSURL *>*)urls
{
    
    [self getUrlFilePath:urls];
    
}

-(void)getUrlFilePath:(NSArray<NSURL*>*) urls
{
    NSMutableArray* retPaths = [NSMutableArray arrayWithCapacity:urls.count];
    NSFileManager* fileManager = [NSFileManager defaultManager];
    for(NSURL* url in urls)
    {
        BOOL fileUrlAuthozied = [url startAccessingSecurityScopedResource];
        if(fileUrlAuthozied){
            NSFileCoordinator *fileCoordinator = [[NSFileCoordinator alloc] init];
            NSError *error;
            
            [fileCoordinator coordinateReadingItemAtURL:url options:0 error:&error byAccessor:^(NSURL *newURL) {
                if (newURL) {
                    
                    NSString* name = [[newURL path] lastPathComponent];
                    NSString* md5String = [FilePickerViewController md5:newURL.absoluteString];
                    
                    NSString* filePath = [NSString stringWithFormat:@"%@/%@", [self dataPath:md5String],name];
                    
                    
                    if([fileManager fileExistsAtPath:filePath])
                    {
                        [retPaths addObject:filePath];
                    }
                    else
                    {
                        NSData *data = [NSData dataWithContentsOfURL:newURL];
                        
                        NSString* lowerString = newURL.absoluteString.lowercaseString;
                        
                        if(([lowerString hasSuffix:@".mp4"] || [lowerString hasSuffix:@".mov"]) &&data.length > 10485760)
                        {
                            __block BOOL isSucess = NO;
                            
                            //压缩
                            
                            AVURLAsset *avAsset = [AVURLAsset URLAssetWithURL:newURL options:nil];
                            NSArray *compatiblePresets = [AVAssetExportSession exportPresetsCompatibleWithAsset:avAsset];
                            
                            if([compatiblePresets containsObject:AVAssetExportPreset960x540])
                            {
                                AVAssetExportSession *exportSession = [[AVAssetExportSession alloc]initWithAsset:avAsset presetName:AVAssetExportPreset960x540];
                                
                                
                                
                                NSURL* mp4Url = [NSURL fileURLWithPath:filePath];
                                exportSession.outputURL = mp4Url;
                                exportSession.shouldOptimizeForNetworkUse = YES;
                                exportSession.outputFileType = AVFileTypeMPEG4;
                                dispatch_semaphore_t wait = dispatch_semaphore_create(0l);
                                [exportSession exportAsynchronouslyWithCompletionHandler:^{
                                    switch ([exportSession status]) {
                                        case AVAssetExportSessionStatusFailed: {
                                            NSLog(@"failed, error:%@.", exportSession.error);
                                        } break;
                                        case AVAssetExportSessionStatusCancelled: {
                                            NSLog(@"cancelled.");
                                        } break;
                                        case AVAssetExportSessionStatusCompleted: {
                                            NSLog(@"completed.");
                                            isSucess = YES;
                                        } break;
                                        default: {
                                            NSLog(@"others.");
                                        } break;
                                    }
                                    dispatch_semaphore_signal(wait);
                                }];
                                long timeout = dispatch_semaphore_wait(wait, DISPATCH_TIME_FOREVER);
                                if (timeout) {
                                    NSLog(@"timeout.");
                                }
                                if (wait) {
                                    //dispatch_release(wait);
                                    wait = nil;
                                }
                            }
                            
                            if(isSucess)
                            {
                                [retPaths addObject:filePath];
                            }
                            else
                            {
                                if([data writeToFile:filePath atomically:YES])
                                {
                                    [retPaths addObject:filePath];
                                }
                                else
                                {
                                    NSLog(@"write error!!");
                                }
                            }
                        }
                        else
                        {
                            if([data writeToFile:filePath atomically:YES])
                            {
                                [retPaths addObject:filePath];
                            }
                            else
                            {
                                NSLog(@"write error!!");
                            }
                        }
                    }
                }
                else
                {
                    NSLog(@"读取文件错误! at %@",url);
                    //                    [Util showTomastToView:weakSelf.view title:@"读取文件错误!" delay:1.f];
                }
                
            }];
            [url stopAccessingSecurityScopedResource];
        }else{
            NSLog(@"startAccessingSecurityScopedResource错误! at %@",url);
            //Error handling
            //            [Util showTomastToView:self.view title:@"读取文件错误!" delay:1.f];
        }
    }
    
    if(retPaths.count)
    {
        NSMutableArray* filesInfo = [NSMutableArray arrayWithCapacity:retPaths.count];
        for(NSString* path in retPaths)
        {
            unsigned long long len = 0;
            NSDictionary *fileAttribute = [fileManager attributesOfItemAtPath:path error:nil];
            len = [fileAttribute fileSize];
            
            NSString* lowerString = path.lowercaseString;

            //图片
            if([lowerString hasSuffix:@".png"] || [lowerString hasSuffix:@".gif"] || [lowerString hasSuffix:@".bmp"]
               || [lowerString hasSuffix:@".jpg"] || [lowerString hasSuffix:@".jpeg"])
            {
                
                CGSize size = [FilePickerViewController imagSizeOfFilePath:path];

                [filesInfo addObject:@{@"identifier":path,@"size":@(len),@"type":@"1",@"originalWidth":@(size.width),@"originalHeight":@(size.height)}];
            }
            else if([lowerString hasSuffix:@".mp4"] || [lowerString hasSuffix:@".mov"])
            {

                NSDictionary* dic = [self getVideoFileInfo:[NSURL fileURLWithPath:path]];

                [filesInfo addObject:@{@"identifier":path,@"size":@(len),@"type":@"2",@"duration":dic[@"duration"],@"thumbUrl":dic[@"thumbUrl"],@"thumbWidth":dic[@"thumbWidth"],@"thumbHeight":dic[@"thumbHeight"]}];
            }
            else
            {
                [filesInfo addObject:@{@"identifier":path,@"type":@"0",@"size":@(len)}];
            }
        }
        
        _result(filesInfo);
    }
    else
    {
        _result(nil);
    }
    
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (NSDictionary *)getVideoFileInfo:(NSURL *)videoUrl
{
    NSDictionary* info;
    NSString* thumbUrl;
    AVURLAsset *avAsset = [AVURLAsset URLAssetWithURL:videoUrl options:nil];
    
    AVAssetImageGenerator *gen = [[AVAssetImageGenerator alloc] initWithAsset:avAsset];
    
    gen.appliesPreferredTrackTransform = YES;
    
    CMTime time = CMTimeMakeWithSeconds(0.0, 600);
    
    NSError *error = nil;
    
    CMTime actualTime;
    
    CGImageRef image = [gen copyCGImageAtTime:time actualTime:&actualTime error:&error];
    
    CGFloat width = CGImageGetWidth(image);
    CGFloat height = CGImageGetHeight(image);
    CGFloat thumbWidth;
    CGFloat thumbHeight;
    if(width > height)
    {
        thumbWidth = 480;
    }
    else
    {
        thumbWidth = 320;
    }
    
    thumbHeight = height / width * thumbWidth;
    
    UIImage *thumb = [self thumbnailWithImage:[[UIImage alloc] initWithCGImage:image] size:CGSizeMake(thumbWidth, thumbHeight)];
    
    CGImageRelease(image);
    double seconds = ceil(avAsset.duration.value/avAsset.duration.timescale);
    
    thumbUrl = [NSString stringWithFormat:@"%@/%d%d.jpg", [self dataPath:nil], (int)[[NSDate date] timeIntervalSince1970], arc4random() % 100000];
    
    BOOL sucessed = [UIImageJPEGRepresentation(thumb, 0.6) writeToFile:thumbUrl atomically:YES];
    
    if(!sucessed)
    {
        thumbUrl = nil;
    }
    
    if(thumbUrl)
    {
        info = @{@"duration":@(seconds),@"thumbUrl":thumbUrl,@"thumbWidth":@(thumb.size.width),@"thumbHeight":@(thumb.size.height)};
    }
    else{
        info = @{@"duration":@(seconds),@"thumbUrl":@"",@"thumbWidth":@(0),@"thumbHeight":@(0)};
    }
    
    return info;
}

- (NSString*)dataPath:(NSString*)subPath
{
    NSString * dataPath;
    if(subPath.length)
    {
        dataPath = [NSString stringWithFormat:@"%@/Library/appdata/chatbuffer/%@", NSHomeDirectory(),subPath];
    }
    else
    {
        dataPath = [NSString stringWithFormat:@"%@/Library/appdata/chatbuffer", NSHomeDirectory()];
    }
    
    
    NSFileManager *fm = [NSFileManager defaultManager];
    if(![fm fileExistsAtPath:dataPath]){
        [fm createDirectoryAtPath:dataPath
      withIntermediateDirectories:YES
                       attributes:nil
                            error:nil];
    }
    return dataPath;
}

- (UIImage *)thumbnailWithImage:(UIImage *)image size:(CGSize)asize

{
    
    UIImage *newimage;
    
    if (nil == image) {
        
        newimage = nil;
        
    }
    
    else{
        
        UIGraphicsBeginImageContext(asize);
        
        [image drawInRect:CGRectMake(0, 0, asize.width, asize.height)];
        
        newimage = UIGraphicsGetImageFromCurrentImageContext();
        
        UIGraphicsEndImageContext();
        
    }
    
    return newimage;
    
}

+ (int)imageTypeOfFilePath:(NSString *)filePath
{
 
    int type = -1;
    
    if (filePath == nil || filePath.length == 0) {
        return type;
    }
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
        return type;
    }
    NSFileHandle *fileHandler  = [NSFileHandle fileHandleForReadingAtPath:filePath];
    if (!fileHandler) {
        return type;
    }
    
    NSData *headData = [fileHandler readDataOfLength:8];
    if (headData.length != 8) {
        [fileHandler closeFile];
        return type;
    }
    
    Byte *bytesArray = (Byte*)[headData bytes];
    if (bytesArray[0] == 0x89 &&
        bytesArray[1] == 0x50 &&  // P
        bytesArray[2] == 0x4E &&  // N
        bytesArray[3] == 0x47 &&  // G
        bytesArray[4] == 0x0D &&
        bytesArray[5] == 0x0A &&
        bytesArray[6] == 0x1A &&
        bytesArray[7] == 0x0A)
    {
        type = 1;
    }
    else if (bytesArray[0] == 0xFF && bytesArray[1] == 0xD8)
    {
        type = 0;
    }
    else if (bytesArray[0] == 0x47 &&    // G
             bytesArray[1] == 0x49 &&    // I
             bytesArray[2] == 0x46 &&    // F
             bytesArray[3] == 0x38 &&    // 8
             (bytesArray[4] == 0x39 ||   // 9
              bytesArray[4] == 0x37) &&  // 7
             bytesArray[5] == 0x61)      // a
    {
        type = 3;
    }
    else if (bytesArray[0] == 0x42 &&   //B
             bytesArray[1] == 0x4D)      //M
    {
        type = 2;
    }
    
    [fileHandler closeFile];
    
    return type;
 
}

+ (CGSize)imagSizeOfFilePath:(NSString *)filePath
{
    CGSize finalSize = CGSizeZero;
    int imgType  = [FilePickerViewController imageTypeOfFilePath:filePath];
    switch (imgType) {
        case 0:
        {
            return  [self jpgImageSizeWithFilePath:filePath];
        }
            break;
        case 1:
        {
            NSData *data = [self fileHeaderData:8 seek:16 filePath:filePath];
            finalSize =  [self pngImageSizeWithHeaderData:data];
        }
            break;
        case 2:
        {
            NSData *data = [self fileHeaderData:8 seek:18 filePath:filePath];
            finalSize = [self bmpImageSizeWithHeaderData:data];
        }
            break;
        case 3:
        {
            NSData *data = [self fileHeaderData:4 seek:6 filePath:filePath];
            finalSize = [self gifImageSizeWithHeaderData:data];
        }
            break;
        case -1:
        {
            UIImage *image = [UIImage imageWithContentsOfFile:filePath];
            if (image) {
                finalSize =  image.size;
            }
            else {
                finalSize = CGSizeZero;
            }
        }
            break;
        default:
            finalSize = CGSizeZero;
            break;
    }
    
    return finalSize;
}
 
+ (NSData*)fileHeaderData:(NSInteger)length seek:(NSInteger)seek filePath:(NSString *)filePath {
    if (![[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
        return nil;
    }
    NSDictionary *fileAttribute = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:nil];
    unsigned long long fileSize = [fileAttribute fileSize];
    if (length + seek > fileSize) {
        return nil;
    }
    
    NSFileHandle *fileHandler = [NSFileHandle fileHandleForReadingAtPath:filePath];
    [fileHandler seekToFileOffset:seek];
    NSData *data = [fileHandler readDataOfLength:length];
    [fileHandler closeFile];
    return data;
}
 
 
+ (CGSize)gifImageSizeWithHeaderData:(NSData *)data
{
    if (data.length != 4) {
        return CGSizeZero;
    }
    unsigned char w1 = 0, w2 = 0;
    [data getBytes:&w1 range:NSMakeRange(0, 1)];
    [data getBytes:&w2 range:NSMakeRange(1, 1)];
    short w = w1 + (w2 << 8);
    unsigned char h1 = 0, h2 = 0;
    [data getBytes:&h1 range:NSMakeRange(2, 1)];
    [data getBytes:&h2 range:NSMakeRange(3, 1)];
    short h = h1 + (h2 << 8);
    return CGSizeMake(w, h);
}
 
 
//JPG文件数据，分很多很多的数据段， 并且每个数据段都会以 0xFF开头
//找到一个数据断后，如果数据段的开头是0xffc0，那么该数据段将会存储 图片的尺寸信息
//否则0xffc0 后面紧跟的两个字段，存储的是当前这个数据段的长度，可跳过当前的数据段
//然后寻找下一个数据段，然后查看是否有图片尺寸信息
+ (CGSize)jpgImageSizeWithFilePath:(NSString *)filePath
{
    if (!filePath.length) {
        return CGSizeZero;
    }
    if (![[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
        return  CGSizeZero;
    }
    
    
    NSFileHandle *fileHandle = [NSFileHandle fileHandleForReadingAtPath:filePath];
    NSUInteger offset = 2;
    NSUInteger length = 0;
    while (1) {
        [fileHandle seekToFileOffset:offset];
        length = 4;
        NSData *data = [fileHandle readDataOfLength:length];
        if (data.length != length) {
            break;
        }
        offset += length;
        int marker,code;
        NSUInteger newLength;
        unsigned char value1,value2,value3,value4;
        [data getBytes:&value1 range:NSMakeRange(0, 1)];
        [data getBytes:&value2 range:NSMakeRange(1, 1)];
        [data getBytes:&value3 range:NSMakeRange(2, 1)];
        [data getBytes:&value4 range:NSMakeRange(3, 1)];
        marker = value1;
        code = value2;
        newLength = (value3 << 8) + value4;
        if (marker != 0xff) {
            [fileHandle closeFile];
            return CGSizeZero;
        }
        
        if (code >= 0xc0 && code <= 0xc3) {
            length = 5;
            [fileHandle seekToFileOffset:offset];
            NSData *data =[fileHandle readDataOfLength:length];
            if (data.length != length) {
                break;
            }
            Byte *bytesArray = (Byte*)[data bytes];
            NSUInteger height = ((unsigned char)bytesArray[1] << 8) + (unsigned char)bytesArray[2];
            NSUInteger width =  ((unsigned char)bytesArray[3] << 8) + (unsigned char)bytesArray[4];
            [fileHandle closeFile];
            return CGSizeMake(width, height);
        }
        else {
            offset += newLength;
            offset -=2;
        }
    }
    [fileHandle closeFile];
    UIImage *image = [UIImage imageWithContentsOfFile:filePath];
    if (image) {
        CGSizeMake((NSInteger)image.size.width, (NSInteger)image.size.height);
    }
    return CGSizeZero;
 
}
 
 
+ (CGSize)pngImageSizeWithHeaderData:(NSData *)data
{
    if (data.length != 8) {
        return CGSizeZero;
    }
    unsigned char w1 = 0, w2 = 0, w3 = 0, w4 = 0;
    [data getBytes:&w1 range:NSMakeRange(0, 1)];
    [data getBytes:&w2 range:NSMakeRange(1, 1)];
    [data getBytes:&w3 range:NSMakeRange(2, 1)];
    [data getBytes:&w4 range:NSMakeRange(3, 1)];
    int w = (w1 << 24) + (w2 << 16) + (w3 << 8) + w4;
    
    unsigned char h1 = 0, h2 = 0, h3 = 0, h4 = 0;
    [data getBytes:&h1 range:NSMakeRange(4, 1)];
    [data getBytes:&h2 range:NSMakeRange(5, 1)];
    [data getBytes:&h3 range:NSMakeRange(6, 1)];
    [data getBytes:&h4 range:NSMakeRange(7, 1)];
    int h = (h1 << 24) + (h2 << 16) + (h3 << 8) + h4;
    return CGSizeMake(w, h);
}
 
 
+ (CGSize)bmpImageSizeWithHeaderData:(NSData *)data {
    if (data.length != 8) {
        return CGSizeZero;
    }
    unsigned char w1 = 0, w2 = 0, w3 = 0, w4 = 0;
    [data getBytes:&w1 range:NSMakeRange(0, 1)];
    [data getBytes:&w2 range:NSMakeRange(1, 1)];
    [data getBytes:&w3 range:NSMakeRange(2, 1)];
    [data getBytes:&w4 range:NSMakeRange(3, 1)];
    int w = w1 + (w2 << 8) + (w3 << 16) + (w4 << 24);
    unsigned char h1 = 0, h2 = 0, h3 = 0, h4 = 0;
    [data getBytes:&h1 range:NSMakeRange(4, 1)];
    [data getBytes:&h2 range:NSMakeRange(5, 1)];
    [data getBytes:&h3 range:NSMakeRange(6, 1)];
    [data getBytes:&h4 range:NSMakeRange(7, 1)];
    int h = h1 + (h2 << 8) + (h3 << 16) + (h4 << 24);
    return CGSizeMake(w, h);
}

// called if the user dismisses the document picker without selecting a document (using the Cancel button)
- (void)documentPickerWasCancelled:(UIDocumentPickerViewController *)controller
{
    _result(nil);
}

- (void)documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentAtURL:(NSURL *)url
{
    [self getUrlFilePath:@[url]];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
