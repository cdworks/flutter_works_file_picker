package com.works.works_file_picker;

import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.io.File;

import static com.works.works_file_picker.WorksFilePickerActivity.transparentStatusBar;

public class WebFileReaderActivity extends FragmentActivity {

    private int barColor;
    private  int titleColor;
    private File webFile;
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        barColor = getIntent().getIntExtra("barColor",0);
        titleColor = getIntent().getIntExtra("titleColor",0xffffff);
        String path = getIntent().getStringExtra("filePath");
        String mimeType = getIntent().getStringExtra("mimeType");



        setContentView(R.layout.web_file_reader_activity);
        TextView titleTextView = findViewById(R.id.title);
        webView = findViewById(R.id.webView);
        WebSettings mWebSettings = webView.getSettings();
        if (Build.VERSION.SDK_INT > 16) {
            mWebSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        mWebSettings.setJavaScriptEnabled(true);

        mWebSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
                handler.proceed();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        }




        if(path != null) {
            webFile = new File(path);
            titleTextView.setText(webFile.getName());

            if(mimeType.startsWith("image/"))
            {

                mWebSettings.setBuiltInZoomControls(true);
                //获取Options对象
                BitmapFactory.Options options = new BitmapFactory.Options();
                //仅做解码处理，不加载到内存
                options.inJustDecodeBounds = true;
            //解析文件
                BitmapFactory.decodeFile(path, options);
                //获取宽高
                int imgWidth = options.outWidth;
                int imgHeight = options.outHeight;

                Display defaultDisplay = getWindowManager().getDefaultDisplay();
                Point point = new Point();
                defaultDisplay.getSize(point);
                int x = point.x;
                int y = point.y;

                final float scale = getResources().getDisplayMetrics().density;
                int totalHeight =  y- (int)(44 * scale + 0.5f) - getStatusBarHeight();

                float ration = (float) imgHeight / (float)imgWidth;

                int imgTotalHeight = (int)(x * ration);

                int topOffset = 0;

                if(imgTotalHeight < totalHeight)
                {
                    topOffset = (int)((totalHeight-imgTotalHeight)*0.5f / scale);
                }

                String img = "<img  src='file:///"+path+"' type="+ "'"+ mimeType +"'" + "  width=100% height=auto  "+ "style='margin-top:" + topOffset+"px'" + " />";

                webView.loadDataWithBaseURL(null, "<body style='margin:0px'>" + img + "</body>", "text/html", "charset=UTF-8", null);
            }
            else if(mimeType.startsWith("video/") || mimeType.startsWith("audio/"))
            {
                String video = "<video  src='file:///"+path+"' type="+ "'"+ mimeType +"'" + " width=100% height=100% autoplay controls />";
                webView.loadDataWithBaseURL(null, "<body style='margin:0px'>" + video + "</body>", "text/html", "charset=UTF-8", null);
            }


        }

        transparentStatusBar(this,barColor);


        ImageButton backBtn = findViewById(R.id.close_button);


        titleTextView.setTextColor(titleColor);
        backBtn.setColorFilter(titleColor);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.title_bar).setBackgroundColor(barColor);

    }


    public int getStatusBarHeight(){
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0){
            result = getResources().getDimensionPixelOffset(resourceId);
        }

        return result;
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();//暂停部分可安全处理的操作，如动画，定位，视频播放等
        webView.pauseTimers();//暂停所有WebView的页面布局、解析以及JavaScript的定时器操作
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        webView.resumeTimers();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }

    @Override
    public void finish() {
        super.finish();
        //注释掉activity本身的过渡动画
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

}
