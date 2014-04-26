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
    public Slider(Context context, int id) {
        super(context);
        this.setId(id);
        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_MOVE)) {
                        position = event.getX() / width;
                        invalidate();
                        ((KosmischeActivity) Slider.this.getContext()).sendControlMessage(Slider.this.getId(), getValue());
                    }
                    return true;
                }
            });
    }

    protected void drawOutline(Canvas canvas) {
        Paint rectOutline = new Paint();
        rectOutline.setARGB(255, red, green, blue);
        rectOutline.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, width - 1, height - 1, rectOutline);
    }

    protected void drawFill(Canvas canvas) {
        Paint fill = new Paint();
        fill.setARGB(255, red, green, blue);
        canvas.drawRect(0, 0, width * position, height, fill);
    }
}
