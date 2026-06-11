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

    private void init() {
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float centerX = width * 0.31f;
        float centerY = height * 0.5f;
        float radius = Math.min(width, height) * 0.26f;

        drawRecord(canvas, centerX, centerY, radius);
        drawWaves(canvas, width, height);
        drawLabel(canvas, width, height);
    }

    private void drawRecord(Canvas canvas, float centerX, float centerY, float radius) {
        canvas.drawCircle(centerX, centerY, radius, discPaint);

        for (int i = 1; i <= 3; i++) {
            float r = radius * (0.35f + i * 0.18f);
            canvas.drawCircle(centerX, centerY, r, ringPaint);
        }

        Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.rgb(123, 75, 255));
        centerPaint.setStyle(Paint.Style.FILL);
        float centerPulse = calculateCenterPulse(animationFrame);
        canvas.drawCircle(centerX, centerY, radius * centerPulse, centerPaint);

        arcRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setColor(Color.argb(230, 255, 255, 255));
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(dp(4));
        canvas.drawArc(arcRect, calculateArcStartAngle(animationFrame), 85, false, arcPaint);
    }

    private void drawWaves(Canvas canvas, float width, float height) {
        float startX = width * 0.58f;
        float baseY = height * 0.5f;
        float gap = dp(14);
        int[] heights = calculateAnimatedWaveHeights((int) height, animationFrame);

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

    public static int[] calculateWaveHeights(int viewHeight) {
        return calculateAnimatedWaveHeights(viewHeight, 0);
    }

    public static int[] calculateAnimatedWaveHeights(int viewHeight, int frame) {
        int safeHeight = Math.max(viewHeight, 120);
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

    public void setAnimationEnabled(boolean enabled) {
        animationEnabled = enabled;
        if (enabled) {
            removeCallbacks(animator);
            post(animator);
        } else {
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
