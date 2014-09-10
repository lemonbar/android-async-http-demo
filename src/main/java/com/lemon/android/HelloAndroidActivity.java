package com.lemon.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.lemon.android.json.DialogResponse;
import com.lemon.android.json.LoginResponse;
import com.lemon.android.model.PhotoWrapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.apache.http.Header;
import org.json.JSONObject;

import java.io.*;

public class HelloAndroidActivity extends Activity {

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.lemon.android.R.menu.main, menu);
        return true;
    }

    private String TAG = "android-http";

    private String baseUrl = "http://edu.gehealthcare.cn/api/v1";

    private String getAbsoluteUrl(String url) {
        return baseUrl + "/" + url;
    }

    private void init() {
        Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("mobile", "13333333333");
                params.put("password", "333333");
                client.get(getAbsoluteUrl("login"), params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        ((TextView) findViewById(R.id.textView2)).setText(Integer.toString(statusCode));
                        LoginResponse loginResponse = new Gson().fromJson(response.toString(), LoginResponse.class);
                        ((EditText) findViewById(R.id.editText)).setText(loginResponse.userinfo.getAvatar());
                    }

                    @Override
                    public void onFinish() {
                        Toast.makeText(HelloAndroidActivity.this, "onFinish method is invoked!!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        Button b2 = (Button) findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                //gid=171&login_uid=22&type=1&token=11072bf4ce03377fb009efa90f909727&maxId=324&
                params.put("login_uid", "22");
                params.put("token", "11072bf4ce03377fb009efa90f909727");
                params.put("gid", "171");
                params.put("type", "1");
                params.put("text", "使用AsyncHttpClient");
                client.post(getAbsoluteUrl("newDialog"), params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        ((TextView) findViewById(R.id.textView2)).setText(Integer.toString(statusCode));
                        DialogResponse dialogResponse = new Gson().fromJson(response.toString(), DialogResponse.class);
                        ((EditText) findViewById(R.id.editText)).setText(dialogResponse.dialog.getText());
                    }
                });
            }
        });

        Button b3 = (Button) findViewById(R.id.button3);
        b3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        //http://edu.gehealthcare.cn/api/v1/course
        //?token=11072bf4ce03377fb009efa90f909727&login_uid=22&course_id=14&
    }

    private String photoPath = null;

    private void takePhoto() {
        //执行拍照前，应该先判断SD卡是否存在
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            //"android.media.action.IMAGE_CAPTURE"
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String fileName = Long.toString(System.currentTimeMillis()) + ".png";
            Log.d(TAG, "photo file name is " + fileName);
            File out = new File(Environment.getExternalStorageDirectory(), fileName);
            photoPath = out.getPath();
            Log.d(TAG, "take photo uri is " + photoPath);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(out));
            startActivityForResult(intent, 1);
        } else {
            Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "start to deal with picture.");
            int photoMaxWidth = 300;
            PhotoWrapper pw = getCompressedPhoto(photoPath, photoMaxWidth);
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("login_uid", "22");
            params.put("token", "11072bf4ce03377fb009efa90f909727");
            params.put("gid", "171");
            params.put("type", "1");
            params.put("width", pw.getWidth());
            params.put("height", pw.getHeight());
            params.put("file", new ByteArrayInputStream(getUploadImageBytes(pw.getBitmap())));
            client.post(getAbsoluteUrl("newPicDialog"), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ((TextView) findViewById(R.id.textView2)).setText(Integer.toString(statusCode));
                    DialogResponse dialogResponse = new Gson().fromJson(response.toString(), DialogResponse.class);
                    ((EditText) findViewById(R.id.editText)).setText(dialogResponse.dialog.getPicURL());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    ((TextView) findViewById(R.id.textView2)).setText(Integer.toString(statusCode));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    ((TextView) findViewById(R.id.textView2)).setText(Integer.toString(statusCode));
                    ((EditText) findViewById(R.id.editText)).setText(responseString);
                }
            });
        }
    }

    public byte[] getUploadImageBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (Exception e) {
            return null;
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public PhotoWrapper getCompressedPhoto(String filePath, int maxwidth) {
        PhotoWrapper photoWrapper = new PhotoWrapper();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int height = options.outHeight;
        int width = options.outWidth;
        photoWrapper.setWidth(maxwidth);
        photoWrapper.setHeight((maxwidth * height) / width);
        // 在内存中创建bitmap对象，这个对象按照缩放大小创建的
        options.inSampleSize = calculateInSampleSize(options, photoWrapper.getWidth(), photoWrapper.getHeight());
        options.inJustDecodeBounds = false;
        int rotateDegree = readPictureDegree(filePath);
        Log.d(TAG, "rotate degress is " + rotateDegree);
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        bitmap = rotaingImage(rotateDegree, bitmap);
        if (bitmap == null) {
            return null;
        }
        photoWrapper.setBitmap(compressImage(bitmap));
        return photoWrapper;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        if (inSampleSize < 0) {
            inSampleSize = 1;
        }
        return inSampleSize;
    }

    public Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        Log.d(TAG, "image size before compress is " + baos.toByteArray().length / 1024 + "kb");
        //循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > 100) {
            //重置baos即清空baos
            baos.reset();
            //每次都减少10
            options -= 10;
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        Log.d(TAG, "image size after compress is " + baos.toByteArray().length / 1024 + "kb");
        //把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        //把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public Bitmap rotaingImage(int angle, Bitmap bitmap) {
        //旋转图片动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

}

