package com.works.works_file_picker;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;


import java.io.*;
import java.lang.ref.WeakReference;
import javax.xml.transform.ErrorListener;
import javax.sql.*;

import static com.works.works_file_picker.WorksFilePickerActivity.transparentStatusBar;

public class PDFReaderActivity extends FragmentActivity {

    private int barColor;
    private  int titleColor;
    private File pdfFile;
    private PDFView pdfView;
    private TextView pageTextView;
    private String displayName;
    private Handler mHandler;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        XMLStreamException ss = new XMLStreamException()
        barColor = getIntent().getIntExtra("barColor",0);
        titleColor = getIntent().getIntExtra("titleColor",0xffffff);
        String path = getIntent().getStringExtra("filePath");
        displayName = getIntent().getStringExtra("displayName");
        setContentView(R.layout.pdf_file_reader_activity);


        pdfView = findViewById(R.id.pdfView);
        pageTextView = findViewById(R.id.pageText);
        pageTextView.setVisibility(View.INVISIBLE);

        TextView titleTextView = findViewById(R.id.title);


        transparentStatusBar(this,barColor);


        ImageButton backBtn = findViewById(R.id.close_button);


        titleTextView.setTextColor(titleColor);
        backBtn.setColorFilter(titleColor);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
//                deleteCacheFile();
            }
        });
        findViewById(R.id.title_bar).setBackgroundColor(barColor);


        if(path != null) {
            pdfFile = new File(path);
            if(displayName.length() > 0) {
                titleTextView.setText(displayName);
            }
            else
            {
                titleTextView.setText(pdfFile.getName());
            }
        }
        readPdfFile();

    }

    private void readPdfFile()
    {
        if(pdfFile == null)
        {
            Toast.makeText(this,"文件不存在!",Toast.LENGTH_SHORT).show();
            return;
        }
        pdfView.fromFile(pdfFile).spacing(8).onError(new OnErrorListener() {
            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }
        }).onLoad(new OnLoadCompleteListener() {
            @Override
            public void loadComplete(int nbPages) {
                pageTextView.setVisibility(View.VISIBLE);

                if(mHandler == null)
                {
                    mHandler  = new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                            pageTextView.setVisibility(View.INVISIBLE);
                            Log.d("filepiker","handleMessagexx");
                        }
                    };
                }

            }
        }).onPageChange(new OnPageChangeListener() {
            @Override
            public void onPageChanged(int page, int pageCount) {
                pageTextView.setVisibility(View.VISIBLE);
                pageTextView.setText(page + 1 + "/" + pageCount);
                mHandler.removeMessages(1000);

                Message msg = Message.obtain();
                msg.what = 1000;
                mHandler.sendMessageDelayed(msg, 3000);

            }
        }).load();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mHandler != null)
        {
            mHandler.removeMessages(1000);
            mHandler.removeCallbacksAndMessages(null);
        }
    }
    @Override
    public void finish() {
        super.finish();
        //注释掉activity本身的过渡动画
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }
}
