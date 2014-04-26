package net.sf.supercollider.android;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.graphics.Rect;

public class PlayButton extends Button {

    public PlayButton(final Context context, int id) {
        super(context, id);

        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d("Kosmische", event.toString());
                    Rect hitRect = new Rect();
                    PlayButton.this.getHitRect(hitRect);
                    
                    if ((event.getAction() == MotionEvent.ACTION_UP) || (event.getAction() == MotionEvent.ACTION_POINTER_UP) 
                        && hitRect.contains((int) event.getX(), (int) event.getY())) {
                        Log.d("Kosmische", event.toString());
                        isSelected = !isSelected;
                        KosmischeActivity activity = (KosmischeActivity) context;
                        if(!isSelected) {
                            activity.getTimerHandler().removeCallbacks(activity.getTimerRunnable());
                        } else {
                            activity.getTimerHandler().postDelayed(activity.getTimerRunnable(), 0);
                        }
                        invalidate();
                    }
                    return true;
                }
            });
    }
}
