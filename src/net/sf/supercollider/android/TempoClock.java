public class TempoClock {
    public TempoClock(float bpm) {
    }
}

mport android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TestActivity extends Activity {

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

            @Override
            public void run() {
                timerHandler.postDelayed(this, 500);
            }
        };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button b = (Button) findViewById(R.id.button);
        b.setText("start");
        b.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Button b = (Button) v;
                    if (b.getText().equals("stop")) {
                        timerHandler.removeCallbacks(timerRunnable);
                        b.setText("start");
                    } else {
                        timerHandler.postDelayed(timerRunnable, 0);
                        b.setText("stop");
                    }
                }
            });
    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        Button b = (Button)findViewById(R.id.button);
        b.setText("start");
    }

}
