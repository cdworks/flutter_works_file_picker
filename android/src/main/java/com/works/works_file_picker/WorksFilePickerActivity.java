package com.works.works_file_picker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.*;

public class WorksFilePickerActivity extends FragmentActivity implements View.OnClickListener {

    static final int FILE_TYPE_UNKNOWN = R.drawable.file_unknow_icon;
    static final int FILE_TYPE_VIDEO = R.drawable.file_video_icon;
    static final int FILE_TYPE_AUDIO = R.drawable.file_audio_icon;
    static final int FILE_TYPE_PPT = R.drawable.file_ppt_icon;
    static final int FILE_TYPE_PDF = R.drawable.file_pdf_icon;
    static final int FILE_TYPE_ZIP = R.drawable.file_zip_icon;
    static final int FILE_TYPE_WORD = R.drawable.file_word_icon;
    static final int FILE_TYPE_XLS = R.drawable.file_xls_icon;
    static final int FILE_TYPE_TXT = R.drawable.file_text_icon;



    private int barColor;
    private  int titleColor;
    private  int maxNumber;  //能选择文件对最大数量，默认9
    private  int maxLength;  //能选择的最大文件大小，单位 MB 默认不限制：-1
    private RecyclerView recyclerView;

    private List<File> fileList;    //当前目录的所有文件
    private List<File> selectFiles; //选择的文件

    private TextView tipsView;

    private File currentFile;

    private TextView dirTextView;

    private  Button okButton;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        barColor = getIntent().getIntExtra("barColor",0);
        titleColor = getIntent().getIntExtra("titleColor",0xffffff);

        maxNumber = getIntent().getIntExtra("maxNumber",9);
        maxLength = getIntent().getIntExtra("maxLength",-1);

        selectFiles = new ArrayList<>();
        setContentView(R.layout.works_file_picker_activity);//设置对应的XML布局文件

       transparentStatusBar(this,barColor);


        Button backBtn = findViewById(R.id.close_button);
        TextView titleTextView = findViewById(R.id.title);

        titleTextView.setTextColor(titleColor);
        backBtn.setTextColor(titleColor);

        backBtn.setOnClickListener(this);
        findViewById(R.id.title_bar).setBackgroundColor(barColor);

        okButton = findViewById(R.id.ok_button);
        okButton.setOnClickListener(this);
        okButton.setEnabled(false);
        okButton.setTextColor(titleColor);

        tipsView = findViewById(R.id.none_tips);

        dirTextView = findViewById(R.id.dir_text);
        dirTextView.setText("sdcard");

        currentFile = Environment.getExternalStorageDirectory();


        fileList = getFileListWithDictionaryId(currentFile);

        recyclerView = findViewById(R.id.recycle_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new FileAdapter());


    }

    List<File> getFileListWithDictionaryId(@Nullable File file)
    {
        List<File> list = null;
        File fold = file;
        if (fold != null) {
            try{
                list = Arrays.asList(fold.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return !file.isHidden();
                    }
                }));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }

        if(list != null && !list.isEmpty())
        {

            Collections.sort(list, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {

                    if(file1.isFile() && file2.isDirectory())
                    {
                        return 1;
                    }
                    else if(file1.isDirectory() && file2.isFile())
                    {
                        return -1;
                    }

                    return file1.getName().compareToIgnoreCase(file2.getName());
                }
            });
            tipsView.setVisibility(View.INVISIBLE);
        }
        else
        {
            tipsView.setVisibility(View.VISIBLE);
        }

        return list;
    }

    int getFileResourceId(File file)
    {
        int index = file.getName().lastIndexOf(".");

        if (index != -1) {
            String result = file.getName().substring(index + 1);
            if(result.equalsIgnoreCase("pdf"))
            {
                return FILE_TYPE_PDF;
            }
            else if(result.equalsIgnoreCase("zip") ||
                    result.equalsIgnoreCase("rar") ||
                    result.equalsIgnoreCase("7z") ||
                    result.equalsIgnoreCase("tar") ||
                    result.equalsIgnoreCase("gz") ||
                    result.equalsIgnoreCase("xz"))
            {
                return FILE_TYPE_ZIP;
            }
            else if(result.equalsIgnoreCase("doc") || result.equalsIgnoreCase("docx"))
            {
                return FILE_TYPE_WORD;
            }
            else if(result.equalsIgnoreCase("xls") || result.equalsIgnoreCase("xlsx")
                    || result.equalsIgnoreCase("xlsm") || result.equalsIgnoreCase("xlsb")
                    || result.equalsIgnoreCase("xltm")
            )
            {
                return FILE_TYPE_XLS;
            }
            else if(result.equalsIgnoreCase("ppt") || result.equalsIgnoreCase("pptx"))
            {
                return FILE_TYPE_PPT;
            }
            else if(result.equalsIgnoreCase("txt") || result.equalsIgnoreCase("rtf"))
            {
                return FILE_TYPE_TXT;
            }
            else if(result.equalsIgnoreCase("mp3") ||
                    result.equalsIgnoreCase("wma") ||
                    result.equalsIgnoreCase("acc") ||
                    result.equalsIgnoreCase("amr"))
            {
                return FILE_TYPE_AUDIO;
            }
            else if(result.equalsIgnoreCase("mp4") ||
                    result.equalsIgnoreCase("avi") ||
                    result.equalsIgnoreCase("rm") ||
                    result.equalsIgnoreCase("rmvb") ||
                    result.equalsIgnoreCase("mov") ||
                    result.equalsIgnoreCase("3gp") ||
                    result.equalsIgnoreCase("wmv")
            )
            {
                return FILE_TYPE_VIDEO;
            }
        }

        return FILE_TYPE_UNKNOWN;
    }

    @Override
    public void onBackPressed() {
//
        if(currentFile.equals(Environment.getExternalStorageDirectory()))
        {
            super.onBackPressed();
        }
        else
        {
            String text = dirTextView.getText().toString();
            dirTextView.setText(text.substring(0,text.lastIndexOf("/")));

            File file = currentFile.getParentFile();
            fileList = getFileListWithDictionaryId(file);
            recyclerView.scrollToPosition(0);
            recyclerView.getAdapter().notifyDataSetChanged();
            currentFile = file;
        }
    }

    private class FileViewHolder extends RecyclerView.ViewHolder
    {
        private TextView title,subTitle;
        private View fileCheckView;
        private View checkContainerView;

        private ImageView selectIcon;
        private ImageView coverImageView;

        private boolean isSelect;  //只对文件有效，文件夹无效
        private File mFile;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.name);
            subTitle = itemView.findViewById(R.id.subtitle);
            fileCheckView = itemView.findViewById(R.id.file_check);

            coverImageView = itemView.findViewById(R.id.head_icon);

            checkContainerView = itemView.findViewById(R.id.check_container);

            checkContainerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(maxLength != -1 && mFile.length() > maxLength * 1048576)
                    {
                       Toast toast = Toast.makeText(WorksFilePickerActivity.this,"只能选择小于" + maxLength  + "MB的文件",Toast.LENGTH_SHORT);
                       toast.setGravity(Gravity.CENTER,0,0);
                       toast.show();
                        return;
                    }

                    if(!isSelect && selectFiles.size() >= maxNumber)
                    {
                        Toast toast = Toast.makeText(WorksFilePickerActivity.this,"最多选择" + maxNumber + "个文件!",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                        return;
                    }

                    isSelect = !isSelect;

                    if(isSelect)
                    {
                        if(!selectFiles.contains(mFile))
                        {
                            selectFiles.add(mFile);
                        }

                    }
                    else
                    {
                        selectFiles.remove(mFile);
                    }

                    okButton.setEnabled(!selectFiles.isEmpty());
                   String okTitle = "确定";
                   if(!selectFiles.isEmpty())
                   {
                       okTitle += "("+ selectFiles.size() +  "/" +maxNumber + ")";
                   }
                    okButton.setText(okTitle);


                    selectIcon.setImageDrawable(getResources().getDrawable(isSelect ? R.drawable.check_box_sel:R.drawable.check_box_unselecte));
                }
            });

            selectIcon = itemView.findViewById(R.id.check_box);
        }


        public void setHolderWithFile(File file)
        {
            title.setText(file.getName());
            mFile = file;
            if(file.isDirectory())
            {
                fileCheckView.setVisibility(View.GONE);
                subTitle.setText("文件：" + file.list().length);
                coverImageView.setImageDrawable(getResources().getDrawable(R.drawable.file_fold_icon));
                isSelect = false;
            }
            else
            {
                fileCheckView.setVisibility(View.VISIBLE);
                Date modify = new Date(file.lastModified());
                long size = file.length();
                String fileSize = size + "B";

                if(size >= 1073741824)
                {
                    double sizeGB = size/1073741824.0;
                    fileSize = String.format("%.1fGB", sizeGB);
                }
                else if(size >= 1048576)
                {
                    double sizeMB = size/1048576.0;
                    fileSize = String.format("%.1fMB", sizeMB);
                }
                else if(size >= 1024)
                {
                    double sizeKB = size/1024.0;
                    fileSize = String.format("%.1fKB", sizeKB);
                }
                subTitle.setText(fileSize +"   " + new SimpleDateFormat("yyyy年M月d日").format(modify));

                String name = file.getName();

               if(name.endsWith(".gif") || name.endsWith(".jpeg") || name.endsWith(".bmp") || name.endsWith(".jpg")|| name.endsWith(".png")) //图片
                {
                    Glide.with(WorksFilePickerActivity.this).load(file)
                            .diskCacheStrategy(DiskCacheStrategy.ALL).into(coverImageView);
                }
                else
                {
                    coverImageView.setImageDrawable(getResources().getDrawable(getFileResourceId(file)));
                }

                boolean isSelect = false;
                for (File selectFile : selectFiles)
                {
                    if(selectFile.equals(file))
                    {
                        isSelect = true;
                        break;
                    }
                }

                this.isSelect = isSelect;
                selectIcon.setImageDrawable(getResources().getDrawable(isSelect ? R.drawable.check_box_sel:R.drawable.check_box_unselecte));
            }

        }

    }

    private  class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View itemView = getLayoutInflater().inflate(R.layout.file_item_cell, parent, false);

            return new FileViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {



            ((FileViewHolder) holder).setHolderWithFile(fileList.get(position));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File file = fileList.get(position);
                    if(file.isDirectory())
                    {
                        currentFile = file;
                        fileList = getFileListWithDictionaryId(file);
                        recyclerView.scrollToPosition(0);
                        notifyDataSetChanged();

                        dirTextView.setText(dirTextView.getText() + "/" + file.getName());
                    }
                    else
                    {
                        FileOpenUtil.openFile(WorksFilePickerActivity.this,file,barColor,titleColor);
                    }
                }
            });

        }


        @Override
        public int getItemCount() {
            return fileList !=null ? fileList.size():0;
        }

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



    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.close_button)
        {
            finish();
        }
        if(id == R.id.ok_button)
        {
            if(selectFiles.isEmpty())
            {
                Toast toast = Toast.makeText(WorksFilePickerActivity.this,"请选择文件!",Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
                return;
            }

            Intent intent = new Intent();



            intent.putExtra("files",(Serializable)selectFiles);

            setResult(1,intent);

            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        //注释掉activity本身的过渡动画
        overridePendingTransition(R.anim.none_anim, R.anim.push_bottom_out);
    }
}
