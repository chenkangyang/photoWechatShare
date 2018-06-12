package xyz.nuptalex.demo01;

import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SecondActivity extends AppCompatActivity {

    String sy;
    String picPath;
    Bitmap bitmap;
    Bitmap picSy;
    ImageView imageView;
    File file;
    String TAG="SecondActivity";
    private Notification notification = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        getWindow().setEnterTransition(new Slide().setDuration(100));
        getWindow().setExitTransition(new Slide().setDuration(100));

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        this.picPath = bundle.getString("path");//getString()返回指定key的值
        TextView pathTxt = (TextView)findViewById(R.id.sy1);//用TextView显示值
        pathTxt.setText("图片路径：" + picPath);
        Log.d("图片路径", picPath);

        String picSize = String.valueOf(bundle.getInt("size"));
        TextView sizeTxt = (TextView)findViewById(R.id.sy2);//用TextView显示值
        sizeTxt.setText("图片大小：" + picSize);

        this.bitmap = BitmapFactory.decodeFile(picPath);
        this.imageView = (ImageView)findViewById(R.id.img2);
        this.imageView.setImageBitmap(bitmap);

        Button btnInput = (Button) findViewById(R.id.insertContent);
        registerForContextMenu(btnInput);

        Button btnFinish = (Button) findViewById(R.id.finish);

        ImageButton btnRotate = (ImageButton) findViewById(R.id.rotate);

        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("click","rotate");
                bitmap = rotateBitmap(bitmap,90);
                imageView.setImageBitmap(bitmap);
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("finish", "完成编辑");
                finishEdit(bitmap);
            }
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert:
                inputTitleDialog(bitmap, imageView);
                return true;
            case R.id.action_date:
                Bitmap picDate = addDate(getApplicationContext(), bitmap);
                bitmap = picDate;
                imageView.setImageBitmap(bitmap);
                return true;
                default:
                    return super.onContextItemSelected(item);
        }
    }

    public void inputTitleDialog(final Bitmap bitmap, final ImageView imageView) {

        final EditText inputServer = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入水印内容").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton("Cancel", null);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                sy = inputServer.getText().toString();
                if(sy.length() != 0) {
                    picSy = drawCenterLable(getApplicationContext(), bitmap, sy);
                    Log.d("picSy_size", String.valueOf(picSy.getByteCount()));
                    imageView.setImageBitmap(picSy);
                }
                else {
                    Toast.makeText(getApplicationContext(), "输入不能为空",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    public static Bitmap drawCenterLable(Context context, Bitmap bmp, String text) {
        float scale = context.getResources().getDisplayMetrics().density;
        Log.d("scale", String.valueOf(scale));
        //创建一样大小的图片
        Bitmap newBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        //创建画布
        Canvas canvas = new Canvas(newBmp);
        canvas.drawBitmap(bmp, 0, 0, null);  //绘制原始图片
        canvas.save();

        canvas.rotate(45); //顺时针转45度
        Paint paint = new Paint();
        paint.setColor(Color.argb(50, 255, 255, 255)); //白色半透明
        paint.setTextSize(32);
        paint.setAlpha(80);

        Rect rectText = new Rect();  //得到text占用宽高， 单位：像素
        paint.getTextBounds(text, 0, text.length(), rectText);


        int w = rectText.width();
        int step = bmp.getHeight()/10;
        int bmpWidth = bmp.getWidth();

        // draw text
        for (int y = step; y < bmp.getHeight(); y += step) {
            for (int x = -bmpWidth; x < bmpWidth; x+= w *2) {
                canvas.drawText(text, x, y, paint);
            }
        }

        canvas.restore();

//
//        double beginX = (bmp.getHeight()/2 - rectText.width()/2) * 1.4;  //45度角度值是1.414
//        double beginY = (bmp.getWidth()/2 - rectText.width()/2) * 1.4;
//
//        Log.d("beginX", String.valueOf(beginX));
//        Log.d("beginY", String.valueOf(beginY));
//        canvas.drawText(text, (int)beginX, (int)beginY, paint);
//        canvas.restore();

        return newBmp;
    }


    public Bitmap addDate(Context context, Bitmap bmp) {
        float scale = context.getResources().getDisplayMetrics().density;
        Log.d("scale", String.valueOf(scale));
        //创建一样大小的图片
        Bitmap newBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        //创建画布
        Canvas canvas = new Canvas(newBmp);
        canvas.drawBitmap(bmp, 0, 0, null);  //绘制原始图片
        canvas.save();

        Paint photoPaint = new Paint();
        photoPaint.setDither(true); // 获取更清晰的图像采样
        photoPaint.setFilterBitmap(true);// 过滤一些
        int width = bmp.getWidth();
        int hight = bmp.getHeight();
        Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());// 创建一个指定的新矩形的坐标
        Rect dst = new Rect(0, 0, width, hight);// 创建一个指定的新矩形的坐标
        canvas.drawBitmap(bmp, src, dst, photoPaint);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        float ratioWidth = (float)screenWidth / 1080;
        float ratioHeight = (float)screenHeight / 1920;
        float RATIO = Math.min(ratioWidth, ratioHeight);
        float TEXT_SIZE = Math.round(35 * RATIO);


        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG| Paint.DEV_KERN_TEXT_FLAG);// 设置画笔
        textPaint.setTextSize(TEXT_SIZE);// 字体大小 35.0f
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);// 采用默认的宽度
        textPaint.setColor(Color.GREEN);// 采用的颜色
        // 绘制上去字，开始未知x,y采用那只笔绘制
        canvas.drawText(String.valueOf("照片日期："+ new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(System.currentTimeMillis()))), 20, 65, textPaint);
        canvas.drawBitmap(bmp, width - 5, hight -5, textPaint);// 在bmp的右下角画入水印
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        return newBmp;
    }

    public Bitmap rotateBitmap(Bitmap origin, float alpha) {
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

    public void finishEdit(Bitmap bmp) {

        File appDir = new File(Environment.getExternalStorageDirectory(), "Demo01_Photos");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
        String fileName = sdf.format(new Date())+"_sy.jpg";
        Log.d("file_sy", fileName);
        this.file = new File(appDir, fileName);
        if (bmp != null) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Log.d("save_uri", file.getAbsolutePath());
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 保存到系统图库
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(), appDir.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //  scanIntent.setData(Uri.fromFile(new File("/sdcard/Demo01_Photos/"+fileName)));
        scanIntent.setData(Uri.fromFile(new File(Environment.getExternalStorageDirectory() +"/Demo01_Photos/"+ fileName)));
        sendBroadcast(scanIntent);
        // 发送通知，点击通知打开图片保存位置。
        sendNotification();
        // 跳转第三个界面（预览图片，并且分享到微信）。
        Intent to_third = new Intent(SecondActivity.this, ThirdActivity.class);
        to_third.putExtra("path",Environment.getExternalStorageDirectory() +"/Demo01_Photos/"+ fileName);
        Log.d("path2", Environment.getExternalStorageDirectory() +"/Demo01_Photos/"+ fileName);

        startActivity(to_third, ActivityOptions.makeSceneTransitionAnimation(SecondActivity.this).toBundle());
    }

    private void sendNotification() {
        NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(SecondActivity.this);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.setData(Uri.fromFile(file));
        //intent.setType("image/*");
        intent.setDataAndType(Uri.fromFile(file),"image/*");//试了下上面分开写setData和setType不能实现相同效果
        Log.i(TAG,(file==null)+ "");
        Log.i(TAG, "uri============"+Uri.fromFile(file));
        //  startActivityForResult(intent,0);填充imageview
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setTicker("正在保存...");
        builder.setContentTitle("已保存添加水印的照片");
        builder.setContentText("点击以查看添加水印的照片");
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeFile(file.toString()));
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        notification = builder.build();
        mManager.notify(0, notification);
    }

}
