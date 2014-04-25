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


public abstract class KosmischeWidget extends View {
    protected Rect bounds;
    protected double position = 0.5;
    protected int red = 255;;
    protected int green = 0;
    protected int blue = 0;
    protected double minimum = 0;
    protected double maximum = 127;
    protected String labelText = "Paramater";
    protected Paint labelPaint;

    public KosmischeWidget(Context context) {
        super(context);
        labelPaint = new Paint();
        labelPaint.setARGB(255, 255, 255, 255);
        labelPaint.setTypeface(Typeface.MONOSPACE);
        labelPaint.setTextSize(45);
    }

    public void setRGB(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void setRange(double minimum, double maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public double getValue() {
        return minimum + (position * (maximum - minimum));
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }

    protected abstract void drawFill(Canvas canvas);
    protected abstract void drawOutline(Canvas canvas);

    protected void drawLabel(Canvas canvas) {
        Log.d("Kosmische", "drawing label");
        Log.d("Kosmische", "bounds " + bounds.toString());
        canvas.drawText(labelText + " " + getValue(), bounds.centerX(), bounds.centerY(), labelPaint);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("Kosmische", "changing orientation");
        super.onConfigurationChanged(newConfig);
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        bounds = canvas.getClipBounds();
        drawOutline(canvas);
        drawFill(canvas);
        drawLabel(canvas);
    }
}
