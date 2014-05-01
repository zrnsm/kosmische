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
import java.lang.InterruptedException;

public class MomentaryButton extends Button {
    protected boolean limitToSquare = true;
    private Runnable action = new Runnable() {
            public void run() {
            }
        };
    
    public MomentaryButton(Context context, int id) {
        this(context, id, true);
    }

    public MomentaryButton(Context context, int id, boolean limitToSquare) {
        super(context, id, limitToSquare);

        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    Rect hitRect = new Rect();
                    MomentaryButton.this.getHitRect(hitRect);

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        isSelected = true;
                        invalidate();
                        action.run();
                    }
                    return true;
                }
            });
    }

    public void setActionRunnable(Runnable action) {
        this.action = action;
    }
}
