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
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private int orientation = HORIZONTAL;
    private float h_position = 0.5f;
    private float v_position = 0.5f;


    public Slider(Context context, int id, int orientation) {
        super(context);
        this.setId(id);
        this.orientation = orientation;
        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_MOVE)) {
                        h_position = event.getX() / width;
                        v_position = (height - event.getY()) / height;
                        invalidate();
                        //                        ((KosmischeActivity) Slider.this.getContext()).sendControlMessage(Slider.this.getId(), getValue());
                    }
                    return true;
                }
            });
    }

    protected float getPosition() {
        if(orientation == HORIZONTAL) {
            return h_position;
        }
        else {
            return v_position;
        }
    }

    protected void drawOutline(Canvas canvas) {
        Paint rectOutline = new Paint();
        rectOutline.setARGB(255, outline_red, outline_green, outline_blue);
        rectOutline.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, width - 1, height - 1, rectOutline);
    }

    protected void drawFill(Canvas canvas) {
        Paint fill = new Paint();
        fill.setARGB(255, fill_red, fill_green, fill_blue);
        Log.d("Kosmische", "orientation " + orientation);
        if(orientation == HORIZONTAL) {
            canvas.drawRect(0, 0, width * h_position, height - 1, fill);
        }
        else {
            canvas.drawRect(0, height - height * v_position, width - 1, height - 1, fill);
        }
    }
}
