package net.sf.supercollider.android;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import java.lang.Math;
import android.util.Log;

public class Knob extends KosmischeWidget {
    private int radius;
    private int center_x;
    private int center_y;
    private final double THREE_PI_OVER_TWO = (3 * Math.PI) / 2;
    private final double TWO_PI = 2 * Math.PI;
    private final double COS_PI_4 = Math.cos(Math.PI / 4);
    private final double SIN_PI_4 = Math.sin(Math.PI / 4);

    public Knob(Context context) {
        super(context);
        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_MOVE)) {
                        double dx = event.getX() - center_x;
                        double dy = event.getY() - center_y;
                        if(((dx * dx) + (dy * dy)) < (radius * radius)) {
                            double next_position = THREE_PI_OVER_TWO - normalized_atan2(dx / radius, dy / radius);
                            if(next_position >= 0)
                                position = next_position / THREE_PI_OVER_TWO;
                            invalidate();
                        }
                    }
                    return true;
                }
            });
    }

    private double normalized_atan2(double y, double x) {
        double x_prime = COS_PI_4 * x + SIN_PI_4 * y;
        double y_prime = -SIN_PI_4 * x + COS_PI_4 * y;
        double angle = Math.atan2(y_prime, x_prime);
        if(angle < 0) {
            return (angle + (TWO_PI));
        }
        return angle;
    }

    protected void drawOutline(Canvas canvas) {
        Paint arcColor = new Paint();
        arcColor.setARGB(255, 255, 0, 0);
        arcColor.setStyle(Paint.Style.STROKE);
        canvas.drawArc(new RectF(bounds.left, center_y - radius, bounds.right, center_y + radius), 135, 270, false, arcColor);
        canvas.drawArc(new RectF(center_x - (radius / 2), center_y - (radius / 2), center_x + (radius / 2), center_y + (radius / 2)), 135, 270, false, arcColor);
        double r_offset = radius / Math.sqrt(2);
        canvas.drawLine((float) center_x, (float) center_y, (float) (center_x + r_offset), (float) (center_y + r_offset), arcColor);
        canvas.drawLine((float) center_x, (float) center_y, (float) (center_x - r_offset), (float) (center_y + r_offset), arcColor);
    }

    protected void drawFill(Canvas canvas) {
        Paint wedgeColor = new Paint();
        wedgeColor.setARGB(255, 255, 0, 0);
        canvas.drawArc(new RectF(bounds.left, center_y - radius, bounds.right, center_y + radius), 135, (int) (270 * position), true, wedgeColor);

        Paint background = new Paint();
        background.setARGB(255, 0, 0, 0);
        canvas.drawCircle(center_x, center_y, (radius / 2) - 1, background);
    }

    protected void onDraw(Canvas canvas) {
        radius = this.getWidth() / 2;
        bounds = canvas.getClipBounds();
        center_x = bounds.centerX();
        center_y = bounds.centerY();
        drawOutline(canvas);
        drawFill(canvas);
        drawLabel(canvas);
    }
}
