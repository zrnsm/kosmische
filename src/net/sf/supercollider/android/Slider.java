package net.sf.supercollider.android;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
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
    protected int orientation = HORIZONTAL;

    protected float h_position = 0.5f;
    protected float v_position = 0.5f;

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
                        ((KosmischeActivity) Slider.this.getContext()).sendControlMessage(Slider.this.getId(), getValue());
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
        canvas.drawRect(0, 0, width - 1, height - 1, outline);
    }

    protected void drawFill(Canvas canvas) {
        Log.d("Kosmische", "orientation " + orientation);
        if(orientation == HORIZONTAL) {
            canvas.drawRect(0, 0, width * h_position, height - 1, fill);
        }
        else {
            canvas.drawRect(0, height - height * v_position, width - 1, height - 1, fill);
        }
    }
}
