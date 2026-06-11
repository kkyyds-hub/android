package com.moon.moonmusic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义绘图：用 Canvas / Paint 绘制谢霆锋推荐页的唱片与音浪图案。
 * Canvas 负责“画在哪里”，Paint 负责“怎么画”，本 View 用 invalidate 形成简单动画。
 */
public class MusicWaveCanvasView extends View {

    private final Paint discPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();
    private int animationFrame = 0;
    private boolean animationEnabled = false;

    private final Runnable animator = new Runnable() {
        @Override
        public void run() {
            if (!animationEnabled) return;
            // animationFrame 相当于动画帧编号；每次变化后 invalidate，会触发 onDraw 重新绘制。
            animationFrame = (animationFrame + 1) % 60;
            invalidate();
            postDelayed(this, 72);
        }
    };

    public MusicWaveCanvasView(Context context) {
        super(context);
        init();
    }

    public MusicWaveCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MusicWaveCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化绘图用的 Paint。
     * 构造方法都会调用这里，提前配置颜色、线宽和文字样式，避免绘制时重复设置。
     */
    private void init() {
        // Paint 提前初始化，onDraw 时直接复用，避免绘制过程中频繁创建对象。
        discPaint.setColor(Color.rgb(24, 24, 30));
        discPaint.setStyle(Paint.Style.FILL);

        ringPaint.setColor(Color.argb(210, 255, 255, 255));
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(dp(2));

        wavePaint.setColor(Color.rgb(123, 75, 255));
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeCap(Paint.Cap.ROUND);
        wavePaint.setStrokeWidth(dp(5));

        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(dp(18));
    }

    /**
     * 完成自定义 View 的整体绘制。
     * 系统刷新 View 或动画调用 invalidate 后，会重新进入这里绘制唱片、音浪和文字。
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 所有位置都按 View 当前宽高计算，布局尺寸变化时图形也能等比例适配。
        float width = getWidth();
        float height = getHeight();
        float centerX = width * 0.31f;
        float centerY = height * 0.5f;
        float radius = Math.min(width, height) * 0.26f;

        drawRecord(canvas, centerX, centerY, radius);
        drawWaves(canvas, width, height);
        drawLabel(canvas, width, height);
    }

    /**
     * 绘制左侧唱片图形。
     * 唱片中心和高光弧线会根据 animationFrame 轻微变化，形成动态效果。
     */
    private void drawRecord(Canvas canvas, float centerX, float centerY, float radius) {
        // 唱片主体：先画黑色圆，再画三圈白色圆环，体现 Canvas 基础图形绘制。
        canvas.drawCircle(centerX, centerY, radius, discPaint);

        for (int i = 1; i <= 3; i++) {
            float r = radius * (0.35f + i * 0.18f);
            canvas.drawCircle(centerX, centerY, r, ringPaint);
        }

        Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.rgb(123, 75, 255));
        centerPaint.setStyle(Paint.Style.FILL);
        float centerPulse = calculateCenterPulse(animationFrame);
        // 中心圆半径随帧数轻微变化，看起来像跟着音乐呼吸。
        canvas.drawCircle(centerX, centerY, radius * centerPulse, centerPaint);

        arcRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setColor(Color.argb(230, 255, 255, 255));
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(dp(4));
        // drawArc 画出唱片上的高光弧线，起始角度随帧数变化形成旋转感。
        canvas.drawArc(arcRect, calculateArcStartAngle(animationFrame), 85, false, arcPaint);
    }

    /**
     * 绘制右侧音浪竖线。
     * 竖线高度由当前帧计算出来，视频播放时会不断刷新形成节奏变化。
     */
    private void drawWaves(Canvas canvas, float width, float height) {
        float startX = width * 0.58f;
        float baseY = height * 0.5f;
        float gap = dp(14);
        int[] heights = calculateAnimatedWaveHeights((int) height, animationFrame);

        // 音浪由多条竖线组成，高度不断变化，体现 Paint 线条和动画刷新。
        for (int i = 0; i < heights.length; i++) {
            float x = startX + i * gap;
            float half = heights[i] / 2f;
            canvas.drawLine(x, baseY - half, x, baseY + half, wavePaint);
        }
    }

    private void drawLabel(Canvas canvas, float width, float height) {
        canvas.drawText("锋味音浪", width * 0.71f, height * 0.24f, textPaint);

        Paint smallTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallTextPaint.setColor(Color.argb(190, 255, 255, 255));
        smallTextPaint.setTextAlign(Paint.Align.CENTER);
        smallTextPaint.setTextSize(dp(12));
        canvas.drawText("Viva Live 节奏可视化", width * 0.71f, height * 0.78f, smallTextPaint);
    }

    /**
     * 计算静止状态下的音浪高度。
     * 测试和关闭动画后的静态绘制会使用这一组默认高度。
     */
    public static int[] calculateWaveHeights(int viewHeight) {
        return calculateAnimatedWaveHeights(viewHeight, 0);
    }

    /**
     * 根据 View 高度和动画帧计算音浪高度。
     * 返回值会被 drawWaves 使用，决定每条竖线在当前帧的长度。
     */
    public static int[] calculateAnimatedWaveHeights(int viewHeight, int frame) {
        int safeHeight = Math.max(viewHeight, 120);
        // 用简单数学公式生成节奏感，不依赖真实音频频谱，绘制逻辑更稳定。
        float beat = Math.abs((frame % 18) - 9) / 9f;
        float pulse = 0.16f + beat * 0.34f;
        float counterPulse = 0.16f + (1f - beat) * 0.28f;
        return new int[]{
                Math.round(safeHeight * (0.20f + pulse * 0.55f)),
                Math.round(safeHeight * (0.34f + counterPulse * 0.45f)),
                Math.round(safeHeight * (0.44f + pulse * 0.42f)),
                Math.round(safeHeight * (0.28f + counterPulse * 0.48f)),
                Math.round(safeHeight * (0.36f + pulse * 0.36f))
        };
    }

    public static float calculateArcStartAngle(int frame) {
        return -35f + (frame % 60) * 6f;
    }

    public static float calculateCenterPulse(int frame) {
        float beat = Math.abs((frame % 18) - 9) / 9f;
        return 0.20f + beat * 0.06f;
    }

    /**
     * 控制音浪动画的启动和停止。
     * VideoActivity 会根据视频播放状态调用它，开启时持续刷新，关闭时回到静止帧。
     */
    public void setAnimationEnabled(boolean enabled) {
        animationEnabled = enabled;
        if (enabled) {
            // 开启动画时先移除旧任务，再重新 post，避免重复定时刷新。
            removeCallbacks(animator);
            post(animator);
        } else {
            // 关闭时重置帧数并重绘一次，让音浪回到静止状态。
            removeCallbacks(animator);
            animationFrame = 0;
            invalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (animationEnabled) {
            post(animator);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(animator);
        super.onDetachedFromWindow();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
