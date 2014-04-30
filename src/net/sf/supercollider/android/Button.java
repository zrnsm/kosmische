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

public class Button extends KosmischeWidget {
    protected boolean isSelected = false;
    protected boolean limitToSquare = true;
    
    public Button(Context context, int id) {
        this(context, id, true);
    }

    public Button(Context context, int id, boolean limitToSquare) {
        super(context);
        this.setId(id);
        this.limitToSquare = limitToSquare;

        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d("Kosmische", event.toString());
                    Rect hitRect = new Rect();
                    Button.this.getHitRect(hitRect);
                    
                    if ((event.getAction() == MotionEvent.ACTION_UP) || (event.getAction() == MotionEvent.ACTION_POINTER_UP) 
                        && hitRect.contains((int) event.getX(), (int) event.getY())) {
                        Log.d("Kosmische", event.toString());
                        isSelected = !isSelected;
                        invalidate();
                    }
                    return true;
                }
            });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int effectiveWidth = widthSize > heightSize ? heightSize : widthSize;
        if(limitToSquare) {
            setMeasuredDimension(effectiveWidth, effectiveWidth);
        }
        else {
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    protected void drawOutline(Canvas canvas) {
        canvas.drawRect(0, 0, width - 1, height - 1, outline);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    protected void drawFill(Canvas canvas) {
        if(isSelected) {
            canvas.drawRect(1, 1, width - 1, height - 1, fill);
        }
    }
}
