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

public class Button extends KosmischeWidget {
    protected boolean isSelected = false;
    private Paint rectOutline;
    private Paint fill;
    protected boolean isActive = false;

    public Button(Context context, int id) {
        super(context);
        this.setId(id);
        updateColor();

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
        setMeasuredDimension(effectiveWidth, effectiveWidth);
    }


    public void updateColor() {
        rectOutline = new Paint();
        rectOutline.setARGB(255, outline_red, outline_green, outline_blue);
        rectOutline.setStyle(Paint.Style.STROKE);

        fill = new Paint();
        fill.setARGB(255, fill_red, fill_green, fill_blue);
    }

    // protected void drawLabel(Canvas canvas) {
    //     canvas.drawText(labelText, centerX, centerY, labelPaint);
    // }

    protected void drawOutline(Canvas canvas) {
        canvas.drawRect(0, 0, width - 1, height - 1, rectOutline);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    protected void drawFill(Canvas canvas) {
        if(isSelected || isActive) {
            canvas.drawRect(1, 1, width - 1, height - 1, fill);
        }
    }
}
