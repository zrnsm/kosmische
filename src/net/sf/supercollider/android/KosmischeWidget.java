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
    protected int fill_red = 255;;
    protected int fill_green = 0;
    protected int fill_blue = 0;
    protected int outline_red = 57;;
    protected int outline_green = 255;
    protected int outline_blue = 20;
    protected float minimum = 0;
    protected float maximum = 127;
    protected String labelText = "Parameter";
    protected Paint labelPaint;
    protected float width;
    protected float height;
    protected float centerX;
    protected float centerY;
    protected boolean valueLabelEnabled = false;
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
        labelPaint.setTextSize(25);
        setWillNotDraw(false);
    }

    public void setTextSize(int textSize) {
        labelPaint.setTextSize(textSize);
    }

    public void setFillRGB(int red, int green, int blue) {
        this.fill_red = red;
        this.fill_green = green;
        this.fill_blue = blue;
    }

    public void setOutlineRGB(int red, int green, int blue) {
        this.outline_red = red;
        this.outline_green = green;
        this.outline_blue = blue;
    }

    public int[] getFillRGB() {
        return new int[] {fill_red, fill_green, fill_blue};
    }

    public void setRange(float minimum, float maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public void setIntegerValued(boolean integerValued) {
        this.integerValued = integerValued;
    }

    protected float getPosition() {
        return position;
    }

    public float getValue() {
        float value = minimum + (getPosition() * (maximum - minimum));
        if(integerValued) {
            return (int) round(Math.round(value), 2);
        }
        return round(value, 2);
    }

    public void setValueLabelEnabled(boolean valueLabelEnabled) {
        this.valueLabelEnabled = valueLabelEnabled;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }

    protected abstract void drawFill(Canvas canvas);
    protected abstract void drawOutline(Canvas canvas);

    protected void drawLabel(Canvas canvas) {
        String label = valueLabelEnabled ? labelText + " " + getValue() : labelText;
        canvas.drawText(label, centerX, centerY, labelPaint);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        invalidate();
    }

    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        Log.d("Kosmische", this.name + " " + getLeft() + " " + getTop() + " " + (getLeft() + w) + " " + (getTop() + h));
        this.width = w;
        this.height = h;
        this.centerX = w / 2;
        this.centerY = h / 2;
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
