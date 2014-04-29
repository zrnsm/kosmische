package net.sf.supercollider.android;

import android.app.Activity;
import android.os.Bundle;
import java.lang.Math;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.content.Context;
import android.util.Log;

public class FillerWidget extends View {
    private int myId;
    public static int id = 0;
    private Paint fill;
    private float width;
    private float height;

    public FillerWidget(Context context) {
        super(context);
        myId = FillerWidget.id;
        FillerWidget.id++;
        fill = new Paint();
        //        fill.setARGB(255, randomColor(), randomColor(), randomColor());
        fill.setARGB(255, 0, 0, 0);
        setWillNotDraw(false);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        super.onSizeChanged(w, h, oldw, oldh);
        invalidate();
    }

    private int randomColor() {
        return (int) (Math.random() * 255);
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, width, height, fill);
    }
}
