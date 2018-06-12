package xyz.nuptalex.demo01;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button cameraButton;
    Button albumButton;
    Bitmap photo;
    Bitmap tempBm;
    String smpicPath;
    String originPicPath;
    String sdCardPath;
    String filename;
    ImageView imageView;

    private static final int REQUEST_THUMBNAIL = 1;// 请求缩略图信号标识
    private static final int REQUEST_ORIGINAL = 2;// 请求原图信号标识
    private static final int REQUEST_GAllERLY = 3;// 请求原图信号标识

    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 0x03;
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0x04;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        albumButton = (Button) findViewById(R.id.takeGallery);
        cameraButton = (Button) findViewById(R.id.takePic);
        sdCardPath = Environment.getExternalStorageDirectory().getPath();
        SimpleDateFormat t = new SimpleDateFormat("yyyyMMddssSSS");
        filename = "Demo01_" + (t.format(new Date())) + "_origin.jpg";
        originPicPath  = sdCardPath + "/Demo01_Photos" + "/" + filename;
        albumButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.img1);

        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

    }

    @Override
    public void onClick(View viewid) {
        switch (viewid.getId()) {
            case R.id.takePic: {// 打开相机
                // 自动获取相机权限
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        ToastUtils.showShort(this, "您已经拒绝过一次");
                    }
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_PERMISSIONS_REQUEST_CODE);
                } else {
                    // 有权限的话
                    if(hasSdcard()){
                        // 内存状态可用，拍摄照片
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        //为拍摄的图片指定一个存储的路径
                        Uri originURL = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            //大于7.0，应使用该路径
                            originURL = FileProvider.getUriForFile(MainActivity.this, "xyz.nuptalex.demo01.fileprovider", new File(originPicPath));
                        } else {
                            //小于7.0
                            originURL = Uri.fromFile(new File(originPicPath));
                        }


                        intent.putExtra(MediaStore.EXTRA_OUTPUT, originURL);

                        // 请求原图，而不是缩略图
                        startActivityForResult(intent, REQUEST_ORIGINAL);
                    } else {
                        ToastUtils.showShort(this, "设备没有SD卡！");
                    }
                }
                break;
            }
            case R.id.takeGallery: {// 打开相册
                // 打开本地相册
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // 设定结果返回
                startActivityForResult(i, REQUEST_GAllERLY);
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.tempBm = null;
        this.imageView.setImageDrawable(null);

        switch (requestCode) {
            case REQUEST_THUMBNAIL: {
                // 通过Bundle获取缩略图
                Log.d("case1","1");
                if (data.getData() != null || data.getExtras() != null) { // 防止没有返回结果
                    Log.d("datahas", "true");
                    Uri uri = data.getData();
                    Log.d("data.getData", String.valueOf(uri));   // 一直为null
                    Log.d("data.getExtras", String.valueOf(data.getExtras()));  //为；Bundle[mParcelledData.dataSize=57708
                    // Log.d("getPath", String.valueOf(uri.getPath()));

                    if (uri != null) {
                        this.photo = BitmapFactory.decodeFile(uri.getPath()); // 拿到图片
                    }
                    if (photo == null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            photo = (Bitmap) bundle.get("data"); // 获取相机返回的数据，并转换为Bitmap图片格式
                            FileOutputStream fileOutputStream = null;
                            try {
                                // 获取 SD 卡根目录 生成图片并保存
                                String saveDir = Environment.getExternalStorageDirectory() + "/Demo01_Photos";
                                // 新建目录
                                File dir = new File(saveDir);
                                if (!dir.exists())
                                    dir.mkdir();
                                // 生成文件名
                                SimpleDateFormat t = new SimpleDateFormat("yyyyMMddssSSS");
                                String filename = "Demo01_" + (t.format(new Date())) + "_sm.jpg";
                                // 新建文件
                                File file = new File(saveDir, filename);
                                // 打开文件输出流
                                fileOutputStream = new FileOutputStream(file);

                                // 生成图片文件
                                this.photo.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                                photo = null;

                                // 相片的完整路径
                                this.smpicPath = file.getPath();

                                // 防止bitmap过大导致不显示
                                Bitmap bm = BitmapFactory.decodeFile(smpicPath);

                                // 需旋转图片
                                bm = rotateBitmap(bm, 90);

                                DisplayMetrics dm = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(dm);
                                int screenWidth=dm.widthPixels;

                                Log.d("picPath", smpicPath);

                                if(bm.getWidth()<=screenWidth){
                                    imageView.setImageBitmap(bm);
                                    tempBm = bm;
                                }else{
                                    Bitmap bmp = Bitmap.createScaledBitmap(bm, screenWidth, bm.getHeight()*screenWidth/bm.getWidth(), true);
                                    imageView.setImageBitmap(bmp);
                                    tempBm = bmp;
                                }

                                // 设置点击事件，打开图片编辑界面SecondActivity
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        // 转换图片进数组，传递图片数组到activity2
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        tempBm.compress(Bitmap.CompressFormat.PNG, 100, baos);

                                        byte [] bitmapByte = baos.toByteArray();
                                        int size = tempBm.getRowBytes() * tempBm.getHeight();

                                        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                                        intent.putExtra("bitmap", bitmapByte);
                                        intent.putExtra("path", smpicPath);
                                        intent.putExtra("size", size);

                                        // 给startActivity传递一个意图(intent)来启动一个新的活动
                                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            Toast.makeText(getApplicationContext(), "成功获取图片",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "找不到图片",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            }
            case REQUEST_ORIGINAL: {
                // 获取原图
//                    // 图片需要压缩
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inJustDecodeBounds = true; // 只获取图片的大小信息，而不是将整张图片载入在内存中，避免内存溢出
//                    BitmapFactory.decodeFile(originPicPath, options);
//                    int height = options.outHeight;
//                    int width= options.outWidth;
//                    int inSampleSize = 2; // 默认像素压缩比例，压缩为原图的1/2
//                    int minLen = Math.min(height, width); // 原图的最小边长
//                    if(minLen > 100) { // 如果原始图像的最小边长大于100dp（此处单位我认为是dp，而非px）
//                        float ratio = (float)minLen / 100.0f; // 计算像素压缩比例
//                        inSampleSize = (int)ratio;
//                    }
//                    options.inJustDecodeBounds = false; // 计算好压缩比例后，这次可以去加载原图了
//                    options.inSampleSize = inSampleSize; // 设置为刚才计算的压缩比例
//                    Bitmap bm = BitmapFactory.decodeFile(originPicPath, options); // 解码文件
//                    Log.w("TAG", "size: " + bm.getByteCount() + " width: " + bm.getWidth() + " heigth:" + bm.getHeight()); // 输出图像数据
//                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                    imageView.setImageBitmap(bm);
                Bitmap bm = BitmapFactory.decodeFile(originPicPath);
                this.tempBm = bm;

                this.imageView.setImageBitmap(null);
                this.imageView.setImageBitmap(tempBm);
                Toast.makeText(getApplicationContext(), "拍摄图片保存在：" + originPicPath, Toast.LENGTH_LONG).show();


                // 设置点击事件，打开图片编辑界面SecondActivity
                this.imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        int size = tempBm.getRowBytes() * tempBm.getHeight();
                        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                        intent.putExtra("path", originPicPath);
                        intent.putExtra("size", size);

                        // 给startActivity传递一个意图(intent)来启动一个新的活动
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                    }
                });
                break;
            }
            case REQUEST_GAllERLY: {
                Log.d("case3","3");
                Toast.makeText(getApplicationContext(), "从相册选取图片", Toast.LENGTH_SHORT).show();

                //打开相册并选择照片，这个方式选择单张
                // 获取返回的数据，这里是android自定义的Uri地址
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                // 获取选择照片的数据视图
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                // 从数据视图中获取已选择图片的路径
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                final String picturePath = cursor.getString(columnIndex);
                cursor.close();

//                // 从相册取出的图片需要压缩
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true; // 只获取图片的大小信息，而不是将整张图片载入在内存中，避免内存溢出
//                BitmapFactory.decodeFile(picturePath, options);
//                int height = options.outHeight;
//                int width= options.outWidth;
//                int inSampleSize = 2; // 默认像素压缩比例，压缩为原图的1/2
//                int minLen = Math.min(height, width); // 原图的最小边长
//                if(minLen > 100) { // 如果原始图像的最小边长大于100dp（此处单位我认为是dp，而非px）
//                    float ratio = (float)minLen / 100.0f; // 计算像素压缩比例
//                    inSampleSize = (int)ratio;
//                }
//                options.inJustDecodeBounds = false; // 计算好压缩比例后，这次可以去加载原图了
//                options.inSampleSize = inSampleSize; // 设置为刚才计算的压缩比例
//                Bitmap bm = BitmapFactory.decodeFile(picturePath, options); // 解码文件
//                Log.w("TAG", "size: " + bm.getByteCount() + " width: " + bm.getWidth() + " heigth:" + bm.getHeight()); // 输出图像数据
//                this.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Bitmap bm = BitmapFactory.decodeFile(picturePath);
                this.tempBm = bm;

                this.imageView.setImageBitmap(null);
                this.imageView.setImageBitmap(tempBm);

                this.imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, SecondActivity.class);

//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        tempBm.compress(Bitmap.CompressFormat.PNG, 100, baos);
//                        byte [] bitmapByte = baos.toByteArray();
                        int size = tempBm.getRowBytes() * tempBm.getHeight();
//                        intent.putExtra("bitmap", bitmapByte);
                        intent.putExtra("path", picturePath);
                        intent.putExtra("size", size);


                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);


                        // 给startActivity传递一个意图(intent)来启动一个新的活动
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                    }
                });
                break;
            }
            default:
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            //调用系统相机申请拍照权限回调
            case CAMERA_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (hasSdcard()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {}
                    } else {
                        ToastUtils.showShort(this, "设备没有SD卡！");
                    }
                } else {
                    ToastUtils.showShort(this, "请允许打开相机！！");
                }
                break;


            }
            // 调用系统相册申请Sdcard权限回调
            case STORAGE_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {}
                else {
                    ToastUtils.showShort(this, "请允许打操作SDCard！！");
                }
                break;
            default:
        }
    }
    /**
     * 检查设备是否存在SDCard的工具方法
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }


    private Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    private Bitmap resizePhoto(String url)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //不会加载，只会获取图片的一个尺寸
        //options里面储存了图片的高度和宽度
        //读取文件
        BitmapFactory.decodeFile(url ,options);
        //改变图片的大小
        double ratio = Math.max(options.outWidth *1.0d/1024f,options.outHeight *1.0d/1024);
        options.inSampleSize =(int) Math.ceil(ratio);
        //设置后会加载图片
        options.inJustDecodeBounds = false;
        //图片压缩完成
        Bitmap newBmp  =  BitmapFactory.decodeFile(url ,options);
        return newBmp;
    }
}
