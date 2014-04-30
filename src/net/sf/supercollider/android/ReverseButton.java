package net.sf.supercollider.android;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.graphics.Rect;

public class ReverseButton extends Button {

    public ReverseButton(final Context context, int id) {
        super(context, id, false);

        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    Rect hitRect = new Rect();
                    ReverseButton.this.getHitRect(hitRect);
                    
                    if ((event.getAction() == MotionEvent.ACTION_UP) || (event.getAction() == MotionEvent.ACTION_POINTER_UP) 
                        && hitRect.contains((int) event.getX(), (int) event.getY())) {
                        isSelected = !isSelected;
                        KosmischeActivity activity = (KosmischeActivity) context;
                        activity.setSequenceReversed(isSelected);
                        invalidate();
                    }
                    return true;
                }
            });
    }
}
