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


public class KKnob extends View {
    private double position = 270 / 2.0;
    private int radius;
    private int center_x;
    private int center_y;
    private final double TWO_PI = 2 * Math.PI;
    private final double COS_PI_4 = Math.cos(Math.PI / 4);
    private final double SIN_PI_4 = Math.sin(Math.PI / 4);

    private double normalized_atan2(double y, double x) {
        double x_prime = COS_PI_4 * x + SIN_PI_4 * y;
        double y_prime = -SIN_PI_4 * x + COS_PI_4 * y;
        double angle = Math.atan2(y_prime, x_prime);
        if(angle < 0) {
            return (angle + (TWO_PI)) / TWO_PI * 360;
        }
        return angle / TWO_PI * 360;
    }

    public KKnob(Context context) {
        super(context);
        Log.d("Kosmische", "creating knob");
        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    //                    Log.d("Kosmische", "touch");
                    if ((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_MOVE)) {
                        double dx = event.getX() - center_x;
                        double dy = event.getY() - center_y;
                        if(((dx * dx) + (dy * dy)) < (radius * radius)) {
                            double next_position = 270 - normalized_atan2(dx / radius, dy / radius);
                            if(next_position >= 0)
                                position = next_position;
                            Log.d("Kosmische", ((Double) position).toString());
                            invalidate();
                        }
                    }
                    return true;
                }
            });
    }

    protected void onDraw(Canvas canvas) {
        Paint wedgeColor = new Paint();
        wedgeColor.setARGB(255, 255, 0, 0);
        this.radius = this.getWidth() / 2;
        int left = canvas.getClipBounds().left;
        int right = canvas.getClipBounds().right;
        this.center_x = canvas.getClipBounds().centerX();
        this.center_y = canvas.getClipBounds().centerY();
        canvas.drawArc(new RectF(left, center_y - radius, right, center_y + radius), 135, (int) position, true, wedgeColor);
        Paint arcColor = new Paint();
        arcColor.setARGB(255, 255, 0, 0);
        arcColor.setStyle(Paint.Style.STROKE);
        canvas.drawArc(new RectF(left, center_y - radius, right, center_y + radius), 135, 270, false, arcColor);
        canvas.drawArc(new RectF(center_x - (radius / 2), center_y - (radius / 2), center_x + (radius / 2), center_y + (radius / 2)), 135, 270, false, arcColor);
        double r_offset = radius / Math.sqrt(2);
        canvas.drawLine((float) center_x, (float) center_y, (float) (center_x + r_offset), (float) (center_y + r_offset), arcColor);
        canvas.drawLine((float) center_x, (float) center_y, (float) (center_x - r_offset), (float) (center_y + r_offset), arcColor);
        Paint background = new Paint();
        background.setARGB(255, 0, 0, 0);
        canvas.drawCircle(center_x, center_y, (radius / 2) - 1, background);
    }
}
