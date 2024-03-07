package com.example.canvas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {
    private DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingView = findViewById(R.id.drawing_view);

        Button clearButton = findViewById(R.id.btn_clear);
        Button colorButton = findViewById(R.id.btn_color);
        Button eraseButton = findViewById(R.id.btn_erase);

        // 清屏按钮
        clearButton.setOnClickListener(v -> drawingView.clearCanvas());

        // 更换颜色按钮（示例为红色）
        colorButton.setOnClickListener(v -> showColorPicker());

        eraseButton.setOnClickListener(v -> {
            drawingView.enableEraser();
        });


        Button saveButton = findViewById(R.id.btn_save); // 假设你的布局文件中有一个保存按钮
        saveButton.setOnClickListener(v -> {
            Bitmap bitmap = drawingView.getBitmap();
            String base64String = convertToBase64(bitmap);
            // 现在base64String包含了画布内容的Base64字符串
            // 这里可以根据需要处理这个字符串，例如显示、发送到服务器等
//            Log.d("base64", base64String);
            fetchStoryWithOptions(base64String);
        });

        // 根据需要添加更多按钮的监听器，例如保存画图、更换画笔粗细等
    }


    private void showColorPicker() {
        int initialColor = drawingView.getColor(); // 获取当前画笔颜色
        int initialStrokeWidth = drawingView.getPaintStrokeWidth(); // 获取当前画笔宽度

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.color_picker_dialog); // 使用自定义布局文件

        View colorPickerView = dialog.findViewById(R.id.color_picker_view);
        colorPickerView.setBackgroundColor(initialColor); // 设置颜色视图的背景为当前画笔颜色

        final AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                drawingView.setColor(color); // 设置所选颜色
                colorPickerView.setBackgroundColor(color); // 更新颜色视图的背景颜色
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // 对话框被取消
            }
        });

        SeekBar strokeWidthSeekBar = dialog.findViewById(R.id.stroke_width_seekbar);
        strokeWidthSeekBar.setProgress(initialStrokeWidth);
        strokeWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setPaintStrokeWidth(progress); // 实时更新画笔宽度
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        colorPickerView.setOnClickListener(v -> colorPicker.show()); // 点击颜色视图时显示颜色选择器
        dialog.show();
    }

    public String convertToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream); // 压缩位图
        byte[] imageBytes = outputStream.toByteArray(); // 转换为字节数组
        return Base64.encodeToString(imageBytes, Base64.DEFAULT); // 将字节数组编码为字符串
    }


    private void fetchStoryWithOptions(String pic_ori) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://192.168.1.13:8080/getModifiedPicture"; // 请替换为实际的URL
        // 创建POST请求体
        RequestBody requestBody = new FormBody.Builder()
                .add("pic_ori", pic_ori)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 处理请求失败的情况
                Log.d("EEEEERRR", "coming");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // 在这里解析和使用响应体
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String pic_now = jsonObject.getString("pic_now");
                        String passed = jsonObject.getString("passed");
                        if (passed.equals("1")) {
                            passed = "闯关成功";
                        } else {
                            passed = "闯关失败TAT";
                        }
                        String finalPassed = passed;
                        runOnUiThread(() -> {
                            showImageInNewWindow(pic_now); // 在这里调用方法显示图片
                            showToastMessage(finalPassed);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void showImageInNewWindow(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_image);

        ImageView imageView = dialog.findViewById(R.id.dialog_image_view);
        imageView.setImageBitmap(decodedByte);

        // 获取图片的宽度和高度
        int imageWidth = decodedByte.getWidth();
        int imageHeight = decodedByte.getHeight();

        // 设置点击事件
        imageView.setOnClickListener(v -> dialog.dismiss()); // 点击图片关闭Dialog

        // 确保Dialog窗口的大小与图片大小相匹配
        dialog.getWindow().setLayout(imageWidth, imageHeight);

        dialog.show();
    }



    private void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}