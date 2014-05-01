package net.sf.supercollider.android;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.util.Log;

public class ChoiceButtonGroup extends KosmischeWidget {
    private static final int numberOfAlternatives = 4;
    private int selectedAlternative = 0;
    private String[] alternativeLabels;
    private int[][] alternativeCenterCoords;
    private int quarterWidth;
    private int quarterHeight;

    public ChoiceButtonGroup(Context context, int id, String[] alternativeLabels) {
        super(context);
        this.alternativeLabels = alternativeLabels;
        this.setId(id);
        
        alternativeCenterCoords = new int[][] {{0, 0}, {0, 0}, {0, 0}, {0, 0}};

        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    float x = event.getX();
                    float y = event.getY();
                    float centerX = ChoiceButtonGroup.this.centerX;
                    float centerY = ChoiceButtonGroup.this.centerY;

                    if(x < centerX && y < centerY) {
                        selectedAlternative = 0;
                    }
                    else if(x >= centerX && y < centerY) {
                        selectedAlternative = 1;
                    }
                    else if(x < centerX && y >= centerY) {
                        selectedAlternative = 2;                        
                    }
                    else if(x >= centerX && y >= centerY) {
                        selectedAlternative = 3;                        
                    }

                    invalidate();
                    activity.sendControlMessage(getId(), getSelectedAlternative(), getSelectedAlternative());
                    return true;
                }
            });
    }

    public void setPosition(float i) {
        setSelectedAlternative((int) i);
    }

    public void setSelectedAlternative(int i) {
        if(i >= 0 && i < numberOfAlternatives) {
            selectedAlternative = i;
        }
        activity.sendControlMessage(getId(), getSelectedAlternative(), getSelectedAlternative());
        invalidate();
    }

    public int getSelectedAlternative() {
        return selectedAlternative;
    }

    protected void drawOutline(Canvas canvas) {
        canvas.drawRect(0, 0, width - 1, height - 1, outline);
    }

    protected void drawDividers(Canvas canvas) {
        canvas.drawLine(centerX, 0, centerX, height, outline);
        canvas.drawLine(0, centerY, width, centerY, outline);
    }

    private void drawAlternativeLabels(Canvas canvas) {
        for(int i = 0; i < numberOfAlternatives; i++) {
            int x = alternativeCenterCoords[i][0];
            int y = alternativeCenterCoords[i][1];
            String label = alternativeLabels[i];
            canvas.drawText(label, x, y, labelPaint);
        }
    }

    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        this.width = w;
        this.height = h;
        this.centerX = w / 2;
        this.centerY = h / 2;
        
        this.quarterWidth = (int) (width / 4.0);
        this.quarterHeight = (int) (height / 4.0);

        alternativeCenterCoords[0][0] = quarterWidth;
        alternativeCenterCoords[0][1] = quarterHeight;

        alternativeCenterCoords[1][0] = 3 * quarterWidth;
        alternativeCenterCoords[1][1] = quarterHeight;

        alternativeCenterCoords[2][0] = quarterWidth;
        alternativeCenterCoords[2][1] = 3 * quarterHeight;

        alternativeCenterCoords[3][0] = 3 * quarterWidth;
        alternativeCenterCoords[3][1] = 3 * quarterHeight;

        super.onSizeChanged(w, h, oldw, oldh);
        invalidate();
    }

    protected void drawFill(Canvas canvas) {
        switch(selectedAlternative) {
        case 0:
            canvas.drawRect(0, 0, centerX - 1, centerY - 1, fill);
            break;
        case 1:
            canvas.drawRect(centerX, 0, width - 1, centerY - 1, fill);
            break;
        case 2:
            canvas.drawRect(0, centerY - 1, centerX - 1, height - 1, fill);
            break;
        case 3:
            canvas.drawRect(centerX, centerY, width - 1, height - 1, fill);
            break;
        }
    }

    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawOutline(canvas);
        drawDividers(canvas);
        drawFill(canvas);
        drawAlternativeLabels(canvas);
    }
}
