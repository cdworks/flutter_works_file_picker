package com.works.works_file_picker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.io.*;

public class TextReaderActivity extends FragmentActivity {

    private int barColor;
    private  int titleColor;
    private File textFile;
    private TextView textView;
    private String displayName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        barColor = getIntent().getIntExtra("barColor",0);
        titleColor = getIntent().getIntExtra("titleColor",0xffffff);
        String path = getIntent().getStringExtra("filePath");
        displayName = getIntent().getStringExtra("displayName");
        if(path != null) {
            textFile = new File(path);
        }

        setContentView(R.layout.text_file_reader_activity);

        textView = findViewById(R.id.textView);
        TextView titleTextView = findViewById(R.id.title);
        if(textFile != null)
        {
            textView.setText(getFileContent());
            if(displayName.length() > 0) {
                titleTextView.setText(displayName);
            }
            else
            {
                titleTextView.setText(textFile.getName());
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

    private String getFileContent() {
        String content = "";
        try {
            InputStream instream = new FileInputStream(textFile);
            if (instream != null) {
                InputStreamReader inputreader
                        = new InputStreamReader(instream, "UTF-8");
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line = "";
                //分行读取
                while ((line = buffreader.readLine()) != null) {
                    content += line + "\n";
                }
                instream.close();//关闭输入流
            }
        } catch (Exception e) {
            Toast toast = Toast.makeText(this,"读取文本出错!",Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
        return content;
    }

    @Override
    public void finish() {
        super.finish();
        //注释掉activity本身的过渡动画
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }


    /**
     * 使状态栏透明
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    static public  void transparentStatusBar(Activity activity, int barColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //需要设置这个flag contentView才能延伸到状态栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            //状态栏覆盖在contentView上面，设置透明使contentView的背景透出来
            activity.getWindow().setStatusBarColor(barColor);
        } else {
            //让contentView延伸到状态栏并且设置状态栏颜色透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
