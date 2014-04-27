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

public class TempoSlider extends Slider {
    public TempoSlider(Context context, int id, int orientation) {
        super(context, id, orientation);
        this.setId(id);
        this.orientation = orientation;
        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_MOVE)) {
                        h_position = event.getX() / width;
                        v_position = (height - event.getY()) / height;
                        invalidate();
                        ((KosmischeActivity) TempoSlider.this.getContext()).setBPM(getValue());
                    }
                    return true;
                }
            });
    }
}
