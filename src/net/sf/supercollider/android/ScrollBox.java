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

public class ScrollBox extends KosmischeWidget {
    private String[] values;
    private int selectedValue = 0;
    private Float previousY = null;
    private int noteChangeGranularity = 8;
    private int distanceFromPreviousChange = 0;
    private Runnable onChangeRunnable = null;

    public ScrollBox(Context context, int id, String[] values) {
        super(context);
        this.setId(id);
        this.values = values;
        this.setLabelText(values[selectedValue]);

        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE && previousY != null) {
                        float dy = event.getY() - previousY;
                        if(distanceFromPreviousChange > noteChangeGranularity) {
                            if(dy < 0) {
                                selectedValue--;
                                if(selectedValue < 0) {
                                    selectedValue = 0;
                                }
                            }
                            else {
                                selectedValue++;
                                if(selectedValue >= ScrollBox.this.values.length) {
                                    selectedValue = ScrollBox.this.values.length - 1;
                                }
                            }

                            if(onChangeRunnable != null) {
                                onChangeRunnable.run();
                            }
                            
                            distanceFromPreviousChange = 0;
                            activity.sendControlMessage(ScrollBox.this.getId(), selectedValue, selectedValue);
                        }

                        distanceFromPreviousChange++;
                        ScrollBox.this.setLabelText(ScrollBox.this.values[selectedValue]);
                        ScrollBox.this.invalidate();
                    }
                    previousY = event.getY();
                    return true;
                }
            });
    }

    public void setPosition(float i) {
        setSelectedValue((int) i);
    }

    public Integer getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(int i) {
        this.selectedValue = i;
        this.setLabelText(this.values[selectedValue]);
        activity.sendControlMessage(getId(), selectedValue, selectedValue);

        invalidate();
    }

    public void setOnChangeRunnable(Runnable runnable) {
        this.onChangeRunnable = runnable;
    }

    protected void drawOutline(Canvas canvas) {
        canvas.drawRect(0, 0, width - 1, height - 1, outline);
    }

    protected void drawFill(Canvas canvas) {
    }
}
