package net.sf.supercollider.android;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.util.Log;
import android.graphics.Typeface;
import java.lang.Math;
import android.content.res.Configuration;
import java.math.BigDecimal;


public abstract class KosmischeWidget extends View {
    protected float position = 0.5f;
    protected int red = 255;;
    protected int green = 0;
    protected int blue = 0;
    protected float minimum = 0;
    protected float maximum = 127;
    protected String labelText = "Parameter";
    protected Paint labelPaint;
    protected float width;
    protected float height;
    protected float centerX;
    protected float centerY;
    public String name;
    protected boolean integerValued = false;

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public KosmischeWidget(Context context) {
        super(context);
        labelPaint = new Paint();
        labelPaint.setARGB(255, 255, 255, 255);
        labelPaint.setTypeface(Typeface.MONOSPACE);
        labelPaint.setTextSize(45);
        setWillNotDraw(false);
    }

    public void setRGB(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void setRange(float minimum, float maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public void setIntegerValued(boolean integerValued) {
        this.integerValued = integerValued;
    }

    public float getValue() {
        float value = minimum + (position * (maximum - minimum));
        if(integerValued) {
            return (int) round(Math.round(value), 2);
        }
        return round(value, 2);
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }

    protected abstract void drawFill(Canvas canvas);
    protected abstract void drawOutline(Canvas canvas);

    protected void drawLabel(Canvas canvas) {
        Log.d("Kosmische", "drawing label");
        canvas.drawText(labelText + " " + getValue(), centerX, centerY, labelPaint);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("Kosmische", "changing orientation");
        super.onConfigurationChanged(newConfig);
        invalidate();
    }

    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        Log.d("Kosmische", this.name + " " + getLeft() + " " + getTop() + " " + (getLeft() + w) + " " + (getTop() + h));
        this.width = w;
        this.height = h;
        this.centerX = width / 2;
        this.centerY = height / 2;
        super.onSizeChanged(w, h, oldw, oldh);
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        Log.d("LayoutTest", this.name + " drawing");
        Log.d("LayoutTest", this.name + " clipbounds " + canvas.getClipBounds());
        drawOutline(canvas);
        drawFill(canvas);
        drawLabel(canvas);
    }
}
