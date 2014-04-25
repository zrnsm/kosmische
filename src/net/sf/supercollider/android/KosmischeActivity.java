package net.sf.supercollider.android;

import net.sf.supercollider.android.ISuperCollider;
import net.sf.supercollider.android.KKnob;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.widget.BaseAdapter;
import android.content.Context;
import android.widget.GridView;
import android.widget.LinearLayout;

/**
 * An example application which exercises some basic features of a SuperCollider-enabled application.
 * 
 * It creates an ScServiceConnection object through which it gets a SuperCollider.Stub.  The stub
 * object provides access to the SuperCollider Service through IPC- it does not run in the same process 
 * space as this Application.  The stub is then wired simply through the OnTouchEventListener of the
 * main GUI widget, to play a note when you touch it.
 * 
 * The only SC-A class that this activity needs to be directly aware of is the generated ISuperCollider class
 * 
 * @TODO: be more javadoc-friendly
 * 
 * @author Dan Stowell
 * @author Alex Shaw
 *
 */
public class KosmischeActivity extends Activity {
    private ServiceConnection conn = new ScServiceConnection();
    private ISuperCollider.Stub superCollider;
    private KKnob mainWidget = null;
    private int defaultNodeId = 999;

    // private class KnobAdapter extends BaseAdapter {
    //     private Context mContext;

    //     public KnobAdapter(Context c) {
    //         mContext = c;
    //     }

    //     public int getCount() {
    //         return 4;
    //     }

    //     public Object getItem(int position) {
    //         return null;
    //     }

    //     public long getItemId(int position) {
    //         return 0;
    //     }

    //     // create a new ImageView for each item referenced by the Adapter
    //     public View getView(int position, View convertView, ViewGroup parent) {
    //         KKnob knob;
    //         if (convertView == null) {  // if it's not recycled, initialize some attributes
    //             knob = new KKnob(mContext);
    //             knob.setLayoutParams(new GridView.LayoutParams(400, 400));
    //             knob.setPadding(10, 10, 10, 10);
    //         } else {
    //             knob = (KKnob) convertView;
    //         }
    //         return knob;
    //     }
    // }

    private class ScServiceConnection implements ServiceConnection {
        //@Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            KosmischeActivity.this.superCollider = (ISuperCollider.Stub) service;
            try {
                // Kick off the supercollider playback routine
                superCollider.start();
                // Start a synth playing
                //                log("starting synth");
                superCollider.sendMessage(OscMessage.createSynthMessage("Kosmische", defaultNodeId, 0, 1));
                //                setUpControls(); // now we have an audio engine, let the activity hook up its controls
            } catch (RemoteException re) {

                //log(re.toString());
                re.printStackTrace();
            }
        }
        //@Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
	
    /**
     * Provide the glue between the user's greasy fingers and the supercollider's shiny metal body
     */
    // public void setUpControls() {
    //     if (mainWidget!=null) mainWidget.setOnTouchListener(new OnTouchListener() {
    //             //@Override
    //             public boolean onTouch(View v, MotionEvent event) {
    //                 if (event.getAction()==MotionEvent.ACTION_UP) {
    //                     // OSC message right here!
    //                     OscMessage noteMessage = new OscMessage( new Object[] {
    //                             "/n_set", defaultNodeId, "amp", 0f
    //                         });
    //                     try {
    //                         // Now send it over the interprocess link to SuperCollider running as a Service
    //                         superCollider.sendMessage(noteMessage);
    //                     } catch (RemoteException e) {
    //                         Toast.makeText(
    //                                        KosmischeActivity.this, 
    //                                        "Failed to communicate with SuperCollider!", 
    //                                        Toast.LENGTH_SHORT);
    //                         e.printStackTrace();
    //                     }
    //                 } else if ((event.getAction()==MotionEvent.ACTION_DOWN) || (event.getAction()==MotionEvent.ACTION_MOVE)) {
    //                     float vol = 1f - event.getY()/mainWidget.getHeight();
    //                     OscMessage noteMessage = new OscMessage( new Object[] {
    //                             "/n_set", defaultNodeId, "amp", vol
    //                         });
    //                     //float freq = 150+event.getX();
    //                     //0 to mainWidget.getWidth() becomes sane-ish range of midinotes:
    //                     float midinote = event.getX() * (70.f / mainWidget.getWidth()) + 28.f;
    //                     OscMessage pitchMessage = new OscMessage( new Object[] {
    //                             "/n_set", defaultNodeId, "note", midinote
    //                         });
    //                     try {
    //                         superCollider.sendMessage(noteMessage);
    //                         superCollider.sendMessage(pitchMessage);
    //                     } catch (RemoteException e) {
    //                         Toast.makeText(
    //                                        KosmischeActivity.this, 
    //                                        "Failed to communicate with SuperCollider!", 
    //                                        Toast.LENGTH_SHORT);
    //                         e.printStackTrace();
    //                     }
    //                 }
    //                 return true;
    //             }
    //         });
    //     try {
    //         superCollider.openUDP(57110);
    //     } catch (RemoteException e) {
    //         e.printStackTrace();
    //     }
    // }
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // GridView gridView = new GridView(this);
        // gridView.setAdapter(new KnobAdapter(this));

        // LinearLayour ll = new LinearLayout(this);
        // ll.add(new KKnob(ll));
        // ll.add(new KSlider(ll));
        //setContentView(new Slider(this));
        setContentView(new Knob(this));
        bindService(new Intent("supercollider.START_SERVICE"), conn, BIND_AUTO_CREATE);
    }
	
    @Override
    public void onPause() {
        super.onPause();
        try {
            // Free up audio when the activity is not in the foreground
            if (superCollider!=null) superCollider.stop();
            this.finish();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
	
    @Override
    public void onStop() {
        super.onStop();
        try {
            // Free up audio when the activity is not in the foreground
            if (superCollider!=null) superCollider.stop();
            this.finish();
        } catch (RemoteException re) {
            re.printStackTrace();
        } 
    }
	
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            superCollider.closeUDP();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(conn);
    }

    /**
     * Convert midi notes to floating point pitches - based on sc_midicps in the SC C++ code
     */
    float sc_midicps(float note)
    {
        return (float) (440.0 * Math.pow((float)2., (note - 69.0) * (float)0.083333333333));
    }
}
