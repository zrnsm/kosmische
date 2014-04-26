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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.view.Gravity;
import java.util.HashMap;

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
    private HashMap<Integer, String> parameterMap;

    // Kosmische parameters
    //     osc1_level = 0.5, 
    //     osc1_type = 0, 
    //     osc1_detune = 0, 
    //     osc1_width = 0.5, 
    //     osc1_octave = 0,
    //     osc1_tune = 0,
    //     osc2_level = 0.5, 
    //     osc2_type = 0, 
    //     osc2_detune = 0, 
    //     osc2_width = 0.5, 
    //     osc2_octave = 0,
    //     osc2_tune = 0,
    //     amp_attack = 0.001,
    //     amp_decay = 0.5,
    //     amp_sustain = 0.1,
    //     amp_release = 0.1,
    //     cutoff = 5000,
    //     resonance = 1,
    //     filter_attack = 0.001,
    //     filter_decay = 0.5,
    //     filter_sustain = 0.1,
    //     filter_release = 0.1,
    //     filter_env_amount = -1,
    //     delaytime = 0.5,
    //     decaytime = 1.0,
    //     reverb_mix = 0.5,
    //     reverb_room_size = 0.5,
    //     reverb_damp = 0.5

    private void registerWidget(int id, String parameter) {
        parameterMap.put(id, parameter);
    }
    
    public void sendControlMessage(int id, float value) {
        Log.d("Kosmische", id + ", " + parameterMap.get(id) + ", " + value);
        OscMessage controlMessage = new OscMessage( new Object[] {
                "/n_set", defaultNodeId, parameterMap.get(id), value
            });
        try {
            superCollider.sendMessage(controlMessage);
        } catch (RemoteException e) {
            Toast.makeText(
                           KosmischeActivity.this, 
                           "Failed to communicate with SuperCollider service.", 
                           Toast.LENGTH_SHORT);
            
            e.printStackTrace();
        }
    }

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
	
    // /**
    //  * Provide the glue between the user's greasy fingers and the supercollider's shiny metal body
    //  */
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

        parameterMap = new HashMap<Integer, String>();

        LinearLayout lLayout = new LinearLayout(this);
        lLayout.setOrientation(LinearLayout.VERTICAL);
        lLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        // Knob knob = new Knob(this);
        // knob.setId(1);
        // knob.name = "knob1";
        // knob.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        // //knob.setLayoutParams(new LinearLayout.LayoutParams(500, 500, 0));
        // lLayout.addView(knob);

        // Knob knob2 = new Knob(this);
        // knob2.setId(2);
        // knob2.name = "knob2";
        // knob2.setRGB(0,255,0);
        // knob2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        // //knob2.setLayoutParams(new LinearLayout.LayoutParams(500, 500, 0));
        // lLayout.addView(knob2);

        Slider slider = new Slider(this, 1);
        slider.name = "slider1";
        slider.setLabelText("osc1_type");
        slider.setIntegerValued(true);
        slider.setRange(0, 3);
        slider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        registerWidget(slider.getId(), "osc1_type");
        lLayout.addView(slider);

        Slider slider2 = new Slider(this, 2);
        slider2.name = "slider2";
        slider2.setLabelText("osc1_detune");
        slider2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider2.setRange(-1000, 1000);
        registerWidget(slider2.getId(), "osc1_detune");
        lLayout.addView(slider2);

        Slider slider3 = new Slider(this, 3);
        slider3.name = "slider3";
        slider3.setLabelText("osc2_type");
        slider3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider3.setIntegerValued(true);
        slider3.setRange(0, 3);
        registerWidget(slider3.getId(), "osc2_type");
        lLayout.addView(slider3);

        Slider slider4 = new Slider(this, 4);
        slider4.name = "slider4";
        slider4.setLabelText("osc2_detune");
        slider4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider4.setRange(-1000, 1000);
        registerWidget(slider4.getId(), "osc2_detune");
        lLayout.addView(slider4);

        Knob knob2 = new Knob(this);
        knob2.setId(2);
        knob2.name = "knob2";
        knob2.setRGB(0,255,0);
        knob2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        //knob2.setLayoutParams(new LinearLayout.LayoutParams(500, 500, 0));
        lLayout.addView(knob2);

        setContentView(lLayout);

        bindService(new Intent("supercollider.START_SERVICE"), conn, BIND_AUTO_CREATE);
    }
	
    @Override
    public void onPause() {
        super.onPause();
        try {
            // Free up audio when the activity is not in the foreground
            if (superCollider != null) superCollider.stop();
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
            if (superCollider != null) superCollider.stop();
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
