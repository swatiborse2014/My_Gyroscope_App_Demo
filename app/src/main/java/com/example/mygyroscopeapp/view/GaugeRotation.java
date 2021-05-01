package com.example.mygyroscopeapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public final class GaugeRotation extends View {

    private static final String TAG = GaugeRotation.class.getSimpleName();

    private RectF rimOuterRect;
    private Paint rimOuterPaint;

    private Bitmap bezelBitmap;
    private Bitmap faceBitmap;
    private Bitmap skyBitmap;
    private Bitmap mutableBitmap;

    private float x;
    private float y;

    private RectF rimRect;

    private RectF faceBackgroundRect;
    private RectF skyBackgroundRect;

    private Paint backgroundPaint;

    private Paint rimPaint;

    private Paint skyPaint;

    public GaugeRotation(Context context) {
        super(context);

        initDrawingTools();
    }

    public GaugeRotation(Context context, AttributeSet attrs) {
        super(context, attrs);

        initDrawingTools();
    }

    public GaugeRotation(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initDrawingTools();
    }

    public void updateRotation(float x, float y) {
        this.x = x;
        this.y = y;

        this.invalidate();
    }

    private void initDrawingTools() {
        rimRect = new RectF(0.12f, 0.12f, 0.88f, 0.88f);

        rimPaint = new Paint();
        rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        rimPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

        float rimOuterSize = -0.04f;
        rimOuterRect = new RectF();
        rimOuterRect.set(rimRect.left + rimOuterSize, rimRect.top
                + rimOuterSize, rimRect.right - rimOuterSize, rimRect.bottom
                - rimOuterSize);

        rimOuterPaint = new Paint();
        rimOuterPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        rimOuterPaint.setColor(Color.GRAY);

        float rimSize = 0.02f;

        faceBackgroundRect = new RectF();
        faceBackgroundRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
                rimRect.right - rimSize, rimRect.bottom - rimSize);

        skyBackgroundRect = new RectF();
        skyBackgroundRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
                rimRect.right - rimSize, rimRect.bottom - rimSize);

        skyPaint = new Paint();
        skyPaint.setAntiAlias(true);
        skyPaint.setFlags(Paint.CURSOR_AFTER);
        skyPaint.setColor(Color.CYAN);

        backgroundPaint = new Paint();
        backgroundPaint.setFilterBitmap(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int chosenWidth = chooseDimension(widthMode, widthSize);
        int chosenHeight = chooseDimension(heightMode, heightSize);

        int chosenDimension = Math.min(chosenWidth, chosenHeight);

        setMeasuredDimension(chosenDimension, chosenDimension);
    }

    private int chooseDimension(int mode, int size) {
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            return size;
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            return getPreferredSize();
        }
    }

    private int getPreferredSize() {
        return 300;
    }

    private void drawRim(Canvas canvas) {
        canvas.drawOval(rimOuterRect, rimOuterPaint);
        canvas.drawOval(rimRect, rimPaint);
    }

    private void drawFace(Canvas canvas) {

        float halfHeight = ((rimRect.top - rimRect.bottom)/2);

        double top = rimRect.top - halfHeight + (x*halfHeight);
        
        if(rimRect.left <= rimRect.right && top <= rimRect.bottom) {
            if (faceBitmap != null) {
                faceBitmap.recycle();
            }

            if (skyBitmap != null) {
                skyBitmap.recycle();
            }

            if (mutableBitmap != null) {
                mutableBitmap.recycle();
            }

            skyPaint.setFilterBitmap(false);

            faceBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_8888);
            skyBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_8888);
            mutableBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_8888);

            Canvas faceCanvas = new Canvas(faceBitmap);
            Canvas skyCanvas = new Canvas(skyBitmap);
            Canvas mutableCanvas = new Canvas(mutableBitmap);
            float scale = (float) getWidth();
            faceCanvas.scale(scale, scale);
            skyCanvas.scale(scale, scale);

            faceBackgroundRect.set(rimRect.left, rimRect.top, rimRect.right,
                    rimRect.bottom);


            skyBackgroundRect.set(rimRect.left, (float) top, rimRect.right,
                    rimRect.bottom);

            faceCanvas.drawArc(faceBackgroundRect, 0, 360, true, skyPaint);
            skyCanvas.drawRect(skyBackgroundRect, skyPaint);

            float angle = (float) -Math.toDegrees(y);

            canvas.save();
            canvas.rotate(angle, faceBitmap.getWidth() / 2f,
                    faceBitmap.getHeight() / 2f);

            mutableCanvas.drawBitmap(faceBitmap, 0, 0, skyPaint);
            skyPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            mutableCanvas.drawBitmap(skyBitmap, 0, 0, skyPaint);
            skyPaint.setXfermode(null);

            canvas.drawBitmap(mutableBitmap, 0, 0, backgroundPaint);
            canvas.restore();
        }
    }

    private void drawBezel(Canvas canvas) {
        if (bezelBitmap == null) {
            Log.w(TAG, "Bezel not created");
        } else {
            canvas.drawBitmap(bezelBitmap, 0, 0, backgroundPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "Size changed to " + w + "x" + h);

        regenerateBezel();
    }

    /**
     * Regenerate the background image. This should only be called when the size
     * of the screen has changed. The background will be cached and can be
     * reused without needing to redraw it.
     */
    private void regenerateBezel() {
        if (bezelBitmap != null) {
            bezelBitmap.recycle();
        }

        bezelBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas bezelCanvas = new Canvas(bezelBitmap);
        float scale = (float) getWidth();
        bezelCanvas.scale(scale, scale);

        drawRim(bezelCanvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBezel(canvas);
        drawFace(canvas);

        float scale = (float) getWidth();
        canvas.save();
        canvas.scale(scale, scale);

        canvas.restore();
    }

}
