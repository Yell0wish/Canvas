package com.example.canvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {
    private List<PaintPath> paths; // 用于存储每条路径及其对应的画笔
    private Paint drawPaint;
    private Path path;

    private int eraserWidth = 30; // 初始化为默认值，您可以根据需要调整

    private int selectedStrokeWidth = 5; // 默认宽度

    private class PaintPath {
        Path path;
        Paint paint;

        PaintPath(Path path, Paint paint) {
            this.path = path;
            this.paint = new Paint(paint); // 复制Paint对象
        }
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        paths = new ArrayList<>();
        path = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(Color.BLACK); // 初始颜色
        drawPaint.setAntiAlias(true); // 抗锯齿
        drawPaint.setStrokeWidth(5); // 画笔宽度
        drawPaint.setStyle(Paint.Style.STROKE); // 画笔样式
        drawPaint.setStrokeJoin(Paint.Join.ROUND); // 画笔连接处为圆滑
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (PaintPath paintPath : paths) {
            canvas.drawPath(paintPath.path, paintPath.paint);
        }
        canvas.drawPath(path, drawPaint); // 绘制当前正在绘制的路径
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path = new Path();
                path.moveTo(touchX, touchY);
                drawPaint.setStrokeWidth(selectedStrokeWidth); // 使用选中的宽度
                paths.add(new PaintPath(path, new Paint(drawPaint))); // 添加新的路径和画笔到列表
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                // 当手指抬起时，可以在这里保存当前路径
                break;
        }

        invalidate(); // 重新绘制视图
        return true;
    }


    public void clearCanvas() {
        paths.clear();
        path.reset();
        invalidate(); // 重新绘制视图
    }

    public void setColor(int color) {
        drawPaint.setXfermode(null); // 禁用橡皮擦模式
        drawPaint.setStrokeWidth(5);
        drawPaint.setColor(color);
    }

    public void enableEraser() {
        drawPaint.setXfermode(null); // 重置Xfermode
        drawPaint.setColor(Color.WHITE); // 假设背景是白色
        drawPaint.setStrokeWidth(30); // 设置一个适当的橡皮擦宽度
    }

    public void setPaintStrokeWidth(int strokeWidth) {
        selectedStrokeWidth = strokeWidth; // 更新选中的宽度，而不是直接改变画笔的宽度
    }


    public int getPaintStrokeWidth() {
        return (int) drawPaint.getStrokeWidth();
    }

    public int getColor() {
        return drawPaint.getColor();
    }


    public Bitmap getBitmap() {
        // 创建一个与视图大小相同的Bitmap
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        // 使用该Bitmap创建一个Canvas对象
        Canvas canvas = new Canvas(bitmap);
        // 绘制当前视图的内容到这个Canvas上
        draw(canvas);
        return bitmap;
    }



}