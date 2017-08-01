package com.bielang.customserver.activity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.appcompat.BuildConfig;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bielang.customserver.MyApplication;
import com.bielang.customserver.R;
import com.bielang.customserver.bean.UserInfo;
import com.bielang.customserver.util.FileUtils;
import com.bielang.customserver.util.HttpPost;
import com.bumptech.glide.Glide;

import java.io.File;

public class UserInfoActivity extends AppCompatActivity {
    private ImageView header;
    private boolean hasChanged=false;
    private TextView done;
    private ImageView edit;
    private EditText name;
    private EditText userName;
    private EditText userPhone;
    private TextView sex;
    private File mTmpFile;
    private File mCropImageFile;
    private static final int REQUEST_CAMERA= 100;
    private static final int REQUEST_GALLERY= 101;
    private static final int REQUEST_CROP= 102;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_user_info);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        edit=(ImageView)findViewById(R.id.edit_user_info);
        header=(ImageView)findViewById(R.id.userHeader);
        TextView id=(TextView)findViewById(R.id.userID);
        TextView type=(TextView)findViewById(R.id.userType);
        name=(EditText) findViewById(R.id.name);
        userName=(EditText)findViewById(R.id.userName);
        userPhone=(EditText)findViewById(R.id.userPhone);
        TextView registTime=(TextView)findViewById(R.id.userRegist_time);
        sex=(TextView)findViewById(R.id.userSex);
        done=(TextView)findViewById(R.id.edit_done);


        Glide.with(this).load(MyApplication.getInstance().getMyInfo().getHeader()).error(R.drawable.pic_sul2).into(header);
        id.setText(String.valueOf(MyApplication.getInstance().getMyInfo().getId()));
        type.setText(MyApplication.getInstance().getMyInfo().getType());
        name.setText(MyApplication.getInstance().getMyInfo().getName());
        userName.setText(MyApplication.getInstance().getMyInfo().getUsername());
        userPhone.setText(MyApplication.getInstance().getMyInfo().getPhone());
        sex.setText(MyApplication.getInstance().getMyInfo().getSex());
        registTime.setText(MyApplication.getInstance().getMyInfo().getRegisttime());

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupDialog();
            }
        });
        header.setClickable(false);
        sex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(UserInfoActivity.this);
                builder.setTitle("请选择性别");

                final String[] items={"男","女"};
                int checkedItem;
                if (sex.getText().equals("男"))
                    checkedItem=0;
                else
                    checkedItem=1;
                builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sex.setText(items[i]);
                    }
                });
                builder.setCancelable(true);
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });
        sex.setClickable(false);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setVisibility(View.GONE);
                done.setVisibility(View.VISIBLE);
                userName.setFocusable(true);
                userName.setFocusableInTouchMode(true);
                name.setFocusable(true);
                name.setFocusableInTouchMode(true);
                userPhone.setFocusableInTouchMode(true);
                userPhone.setFocusable(true);
                header.setClickable(true);
                sex.setClickable(true);
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setVisibility(View.VISIBLE);
                done.setVisibility(View.GONE);
                userName.setFocusable(false);
                userName.setFocusableInTouchMode(false);
                userPhone.setFocusableInTouchMode(false);
                userPhone.setFocusable(false);
                name.setFocusable(false);
                name.setFocusableInTouchMode(false);
                header.setClickable(false);
                sex.setClickable(false);
                hasChanged=true;
                UserInfo userInfo=MyApplication.getInstance().getMyInfo();
                userInfo.setName(name.getText().toString());
                userInfo.setUsername(userName.getText().toString());
                userInfo.setSex(sex.getText().toString());
                userInfo.setPhone(userPhone.getText().toString());
                MyApplication.getInstance().setMyInfo(userInfo);
                new Thread(keepUserInfo).start();
            }
        });
        ImageButton back=(ImageButton)findViewById(R.id.user_info_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (done.getVisibility()==View.VISIBLE){
                    AlertDialog isExit = new AlertDialog.Builder(UserInfoActivity.this).create();
                    isExit.setTitle("系统提示");
                    isExit.setMessage("退出将放弃更改，是否退出？");
                    isExit.setButton(DialogInterface.BUTTON_POSITIVE,"确定", listener);
                    isExit.setButton(DialogInterface.BUTTON_NEGATIVE,"取消", listener);
                    isExit.show();
                }else {
                    Intent intent = new Intent();
                    intent.putExtra("hasChanged", hasChanged);
                    UserInfoActivity.this.setResult(1000, intent);
                    UserInfoActivity.this.finish();
                }
            }
        });
    }
    private void setupDialog(){
        final String[] items = {"拍照", "相册"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(UserInfoActivity.this);
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0){
                    camera();
                }else if (i == 1){
                    gallery();
                }
            }
        });
        listDialog.show();
    }

    private void gallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void camera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            mTmpFile = new File(FileUtils.createRootPath(getBaseContext()) + "/" + System.currentTimeMillis() + ".jpg");
            FileUtils.createFile(mTmpFile);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(getBaseContext(), BuildConfig.APPLICATION_ID + ".provider", mTmpFile));
            }else {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK){
                    crop(mTmpFile.getAbsolutePath());
                }else {
                    Toast.makeText(this, "拍照失败", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_CROP:
                if (resultCode == RESULT_OK){
                    Glide.with(UserInfoActivity.this).load(Uri.fromFile(mCropImageFile)).into(header);
                    MyApplication.getInstance().setHeader(Uri.fromFile(mCropImageFile).toString());
                    hasChanged=true;
                }else {
                    Toast.makeText(this, "裁剪失败", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK && data != null){
                    String imagePath = handleImage(data);
                    crop(imagePath);
                }else {
                    Toast.makeText(this, "打开图库失败", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    private void crop(String imagePath){
        //mCropImageFile = FileUtils.createTmpFile(getBaseContext());
        mCropImageFile = getmCropImageFile();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(getImageContentUri(new File(imagePath)), "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCropImageFile));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUEST_CROP);
    }

    //把fileUri转换成ContentUri
    public Uri getImageContentUri(File imageFile){
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            cursor.close();
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    //获取裁剪的图片保存地址
    private File getmCropImageFile(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"temp.jpg");
            return new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        }
        return null;
    }

    private String handleImage(Intent data) {
        Uri uri = data.getData();
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("" +
                        "content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equals(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        }
        return imagePath;
    }

    private String getImagePath(Uri uri, String seletion) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, seletion, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    @Override
    public void onBackPressed() {
        if (done.getVisibility()==View.VISIBLE){
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            isExit.setTitle("系统提示");
            isExit.setMessage("退出将放弃更改，是否退出？");
            isExit.setButton(DialogInterface.BUTTON_POSITIVE,"确定", listener);
            isExit.setButton(DialogInterface.BUTTON_NEGATIVE,"取消", listener);
            isExit.show();
        }else {
            Intent intent = new Intent();
            intent.putExtra("hasChanged", hasChanged);
            this.setResult(1000, intent);
            this.finish();
            super.onBackPressed();
        }
    }
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    edit.setVisibility(View.VISIBLE);
                    done.setVisibility(View.GONE);
                    userName.setFocusable(false);
                    userName.setFocusableInTouchMode(false);
                    userPhone.setFocusableInTouchMode(false);
                    userPhone.setFocusable(false);
                    name.setFocusable(false);
                    name.setFocusableInTouchMode(false);
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };
    private Runnable keepUserInfo=new Runnable() {
        @Override
        public void run() {
            HttpPost.change_user_info();
        }
    };
}
