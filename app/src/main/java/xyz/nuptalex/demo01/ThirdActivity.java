package xyz.nuptalex.demo01;


import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class ThirdActivity extends AppCompatActivity {

    String picSy;
    private File file, share;
    private String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        getWindow().setExitTransition(new Slide().setDuration(100));
        getWindow().setEnterTransition(new Slide().setDuration(100));

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        ImageButton share_btn = (ImageButton) findViewById(R.id.share_btn);
        ImageView img3 = (ImageView) findViewById(R.id.img3);

        this.picSy = bundle.getString("path");
        Toast.makeText(getApplicationContext(), this.picSy, Toast.LENGTH_SHORT).show();
        // 按路径解码
        Bitmap bmp = BitmapFactory.decodeFile(picSy);

        img3.setImageBitmap(bmp);

//        String saveDir = Environment.getExternalStorageDirectory() + "/Demo01_Photos";
//        this.dir = new File(saveDir);
//        if (!dir.exists())
//            dir.mkdir();
        this.file = new File(picSy);

        if(bmp == null) {
            Toast.makeText(getApplicationContext(), "bmp空！", Toast.LENGTH_SHORT).show();
        }

        Log.d("file", String.valueOf(file));
        this.share = bitmapToFile(bmp, file);

        share_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                checkPermission();
                boolean flag = isWeixinAvilible(ThirdActivity.this);
                if (flag)
                {
                    Log.d("微信是否安装呢", String.valueOf(flag));
                    //分享至微信朋友
//                    share(file1);
                    // 分享至微信朋友圈
                    if (share == null || !share.exists()) {
                        Toast.makeText(ThirdActivity.this, "File not found!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    shareToCommunity(share);
                    // 分享至QQ好友
//                    shareToQQFriend();
                } else {
                    Toast.makeText(getApplicationContext(), "微信没有安装！", Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }

    //检查版本，大于23就申请权限
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(this, permission[0]);
            // 权限是否已经 授权 GRANTED---授权 DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                //无权限，准备申请权限
                ActivityCompat.requestPermissions(this, permission, 321);
            }
        }
    }

    //判断是否大于安卓7.0
    private Uri isSeven(File file, Intent intent) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //大于7.0，应使用该路径
            uri = FileProvider.getUriForFile(ThirdActivity.this, "xyz.nuptalex.demo01.fileprovider", file);
            Log.d("7.0的路径", String.valueOf(uri));
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        } else {
            //小于7.0
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    //分享图片 到微信朋友
    private void share(File file) {
        Intent intent = new Intent();
        //判断是否大于安卓7.0
        Uri uri = isSeven(file, intent);
        ComponentName componentName = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
        intent.setComponent(componentName);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        Log.d("分享至微信朋友", String.valueOf(uri));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        this.grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    //分享图片 至朋友圈
    private void shareToCommunity(File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        //判断是否大于安卓7.0
        Uri uri = isSeven(file, intent);

        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(intent);


//        ComponentName componentName = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
//        intent.setComponent(componentName);
//        intent.setAction(Intent.ACTION_SEND);
//        intent.setType("image/*");
//        intent.putExtra(Intent.EXTRA_STREAM, uri);
//        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        this.grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivity(intent);
    }

    //分享文本 到QQ好友（微信，朋友圈同理,这里分享文本不涉及访问文件就不用判断安卓是否大于7.0了）
    private void shareToQQFriend() {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");
        intent.setComponent(componentName);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_TEXT, "这是分享内容");
        startActivity(intent);
    }

    //判断微信是否安装（判断QQ改包名就行啦"com.tencent.mobileqq"）
    public static boolean isWeixinAvilible(Context context)
    {
        // 获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null)
        {
            for (int i = 0; i < pinfo.size(); i++)
            {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm"))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public File bitmapToFile(Bitmap bitmap, File file)
    {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return file;
    }


}