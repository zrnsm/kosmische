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

public class StepButton extends Button {
    private boolean isCurrentStep = false;

    public StepButton(Context context, int id) {
        super(context, id);
    }

    public void setIsCurrentStep(boolean isCurrentStep) {
        this.isCurrentStep = isCurrentStep;
    }

    public boolean isCurrentStep() {
        return isCurrentStep;
    }

    protected void drawFill(Canvas canvas) {
        if(isSelected || isCurrentStep) {
            canvas.drawRect(1, 1, width - 1, height - 1, fill);
        }
    }
}
