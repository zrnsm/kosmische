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
    protected float minimumValue = 0;
    protected float maximumValue = 127;
    protected float width;
    protected float height;
    protected float centerX;
    protected float centerY;

    protected String labelText = "Parameter";

    protected Paint labelPaint;
    protected Paint outline;
    protected Paint fill;
    protected Paint background;

    //   add default colors
    //    protected Paint defaultFill;

    protected boolean valueLabelEnabled = false;
    protected boolean integerValued = false;
    // protected int padding = 10;

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

        background = new Paint();
        background.setARGB(255, 0, 0, 0);

        fill = new Paint();
        fill.setARGB(255, 200, 200, 200);

        outline = new Paint();
        outline.setARGB(255, 255, 0, 0);
        outline.setStyle(Paint.Style.STROKE);
    }

    public void setTextSize(int textSize) {
        labelPaint.setTextSize(textSize);
    }

    public void setFill(Paint fill) {
        this.fill = fill;
    }

    public Paint getFill() {
        return fill;
    }

    public void setFillRGB(int red, int green, int blue) {
        if(fill == null) {
            fill = new Paint();
        }
        fill.setARGB(255, red, green, blue);
    }

    public void setOutlineRGB(int red, int green, int blue) {
        if(outline == null) {
            outline = new Paint();
        }
        outline.setARGB(255, red, green, blue);
        outline.setStyle(Paint.Style.STROKE);
    }

    public void setRange(float minimumValue, float maximumValue) {
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    public void setIntegerValued(boolean integerValued) {
        this.integerValued = integerValued;
    }

    protected float getPosition() {
        return position;
    }

    public float getValue() {
        float value = minimumValue + (getPosition() * (maximumValue - minimumValue));
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
        Log.d("Kosmische", this.labelText + " " + getLeft() + " " + getTop() + " " + (getLeft() + w) + " " + (getTop() + h));
        this.width = w;
        this.height = h;
        this.centerX = w / 2;
        this.centerY = h / 2;
        super.onSizeChanged(w, h, oldw, oldh);
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        Log.d("LayoutTest", this.labelText + " drawing");
        Log.d("LayoutTest", this.labelText + " clipbounds " + canvas.getClipBounds());
        drawOutline(canvas);
        drawFill(canvas);
        drawLabel(canvas);
    }
}
