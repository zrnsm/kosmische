package net.sf.supercollider.android;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.graphics.Rect;
import android.view.View.OnTouchListener;
import android.graphics.Typeface;
import java.lang.Math;
import android.util.Log;

public class Slider extends KosmischeWidget {
    public Slider(Context context) {
        super(context);
        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_MOVE)) {
                        position = (event.getX() - bounds.left) / ((double) bounds.width());
                        invalidate();
                    }
                    return true;
                }
            });
    }

    protected void drawOutline(Canvas canvas) {
        bounds = canvas.getClipBounds();
        Paint rectOutline = new Paint();
        rectOutline.setARGB(255, red, green, blue);
        rectOutline.setStyle(Paint.Style.STROKE);

        bounds.bottom = bounds.bottom - 1;
        bounds.right = bounds.right - 1;
        canvas.drawRect(bounds, rectOutline);
    }

    protected void drawFill(Canvas canvas) {
        Paint fill = new Paint();
        fill.setARGB(255, red, green, blue);
        canvas.drawRect((float) bounds.left, (float) bounds.top, (float) (bounds.width() * position), (float) bounds.bottom, fill);
    }
}
