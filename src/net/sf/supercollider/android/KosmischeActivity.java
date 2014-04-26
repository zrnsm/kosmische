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
import java.util.ArrayList;
import android.os.Handler;
import android.content.pm.ActivityInfo;

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
    private float bpm = 120.0f;
    private int currentStep = -1;

    private Sequence sequence;
    private ArrayList<Button> stepButtons;
    private final int sequenceLength = 16;

    private int active_red = 255;
    private int active_green = 0;
    private int active_blue = 0;

    private int[] previousFillRGB;

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

    private void createStepButtons(LinearLayout steps) {
        for(int i = 1; i <= sequenceLength; i++) {
            Button button = new Button(this, i);
            button.name = "button" + i;
            button.setLabelText("C#");
            button.setFillRGB(200,200,200);
            button.updateColor();
            button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            stepButtons.add(button);
            steps.addView(button);
            
        }
    }
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sequence = new Sequence(16);
        stepButtons = new ArrayList<Button>(sequenceLength);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        parameterMap = new HashMap<Integer, String>();

        LinearLayout lLayout = new LinearLayout(this);
        lLayout.setOrientation(LinearLayout.VERTICAL);
        lLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));



        // osc 1 section
        LinearLayout osc1_section = new LinearLayout(this);
        osc1_section.setOrientation(LinearLayout.VERTICAL);
        osc1_section.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        LinearLayout osc1_section_top = new LinearLayout(this);
        osc1_section_top.setOrientation(LinearLayout.HORIZONTAL);
        osc1_section_top.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        Slider osc1_type = new Slider(this, 73, Slider.HORIZONTAL);
        osc1_type.name = "osc1_type";
        osc1_type.setLabelText("osc1_type");
        osc1_type.setRange(0, 3);
        osc1_type.setIntegerValued(true);
        osc1_type.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc1_type.setFillRGB(190,190,190);
        //        registerWidget(osc1_type.getId(), "osc1_type");
        osc1_section_top.addView(osc1_type);

        Slider osc1_tune = new Slider(this, 44, Slider.HORIZONTAL);
        osc1_tune.name = "osc1_tune";
        osc1_tune.setLabelText("osc1_tune");
        osc1_tune.setRange(-12, 12);
        osc1_tune.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc1_tune.setFillRGB(190,190,190);
        //        registerWidget(osc1_tune.getId(), "osc1_type");
        osc1_section_top.addView(osc1_tune);

        LinearLayout osc1_section_bottom = new LinearLayout(this);
        osc1_section_bottom.setOrientation(LinearLayout.HORIZONTAL);
        osc1_section_bottom.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        Slider osc1_width = new Slider(this, 45, Slider.HORIZONTAL);
        osc1_width.name = "osc1_width";
        osc1_width.setLabelText("osc1_width");
        osc1_width.setRange(0, 3);
        osc1_width.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc1_width.setFillRGB(190,190,190);
        //        registerWidget(osc1_width.getId(), "osc1_width");
        osc1_section_bottom.addView(osc1_width);

        Slider osc1_detune = new Slider(this, 46, Slider.HORIZONTAL);
        osc1_detune.name = "osc1_detune";
        osc1_detune.setLabelText("osc1_detune");
        osc1_detune.setRange(-12, 12);
        osc1_detune.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc1_detune.setFillRGB(190,190,190);
        //        registerWidget(osc1_detune.getId(), "osc1_type");
        osc1_section_bottom.addView(osc1_detune);

        osc1_section.addView(osc1_section_top);
        osc1_section.addView(osc1_section_bottom);
        row1.addView(osc1_section);

        // filter section
        LinearLayout filter_section = new LinearLayout(this);
        filter_section.setOrientation(LinearLayout.HORIZONTAL);
        filter_section.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        Knob cutoff_knob = new Knob(this, 41);
        cutoff_knob.name = "cutoff";
        cutoff_knob.setLabelText("cutoff");
        cutoff_knob.setRange(0, 1);
        cutoff_knob.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        cutoff_knob.setFillRGB(210,210,210);
        //        registerWidget(cutoff_knob.getId(), "osc1_type");
        filter_section.addView(cutoff_knob);

        Knob resonance_knob = new Knob(this, 42);
        resonance_knob.name = "reso";
        resonance_knob.setLabelText("reso");
        resonance_knob.setRange(0, 1);
        resonance_knob.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        resonance_knob.setFillRGB(210,210,210);
        //        registerWidget(resonance_knob.getId(), "osc1_type");
        filter_section.addView(resonance_knob);

        row1.addView(filter_section);

        LinearLayout amp_adsr = new LinearLayout(this);
        amp_adsr.setOrientation(LinearLayout.HORIZONTAL);
        amp_adsr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        Slider slider = new Slider(this, 1, Slider.VERTICAL);
        slider.name = "slider1";
        slider.setLabelText("a");
        slider.setRange(0, 1);
        slider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider.setFillRGB(210,210,210);
        //        registerWidget(slider.getId(), "osc1_type");
        amp_adsr.addView(slider);

        Slider slider2 = new Slider(this, 2, Slider.VERTICAL);
        slider2.name = "slider2";
        slider2.setLabelText("d");
        slider2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider2.setFillRGB(210,210,210);
        slider2.setRange(0, 1);
        //        registerWidget(slider2.getId(), "osc1_detune");
        amp_adsr.addView(slider2);

        Slider slider3 = new Slider(this, 3, Slider.VERTICAL);
        slider3.name = "slider3";
        slider3.setLabelText("s");
        slider3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider3.setFillRGB(210,210,210);
        slider3.setRange(0, 1);
        //        registerWidget(slider3.getId(), "osc2_type");
        amp_adsr.addView(slider3);

        Slider slider4 = new Slider(this, 4, Slider.VERTICAL);
        slider4.name = "slider4";
        slider4.setLabelText("r");
        slider4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider4.setFillRGB(210,210,210);
        slider4.setRange(0, 1);
        //        registerWidget(slider4.getId(), "osc2_detune");
        amp_adsr.addView(slider4);

        row1.addView(amp_adsr);

        // FillerWidget w3 = new FillerWidget(this);
        // w3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        // //knob.setLayoutParams(new LinearLayout.LayoutParams(500, 500, 0));
        // row1.addView(w3);

        lLayout.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        // osc 2 section
        LinearLayout osc2_section = new LinearLayout(this);
        osc2_section.setOrientation(LinearLayout.VERTICAL);
        osc2_section.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        LinearLayout osc2_section_top = new LinearLayout(this);
        osc2_section_top.setOrientation(LinearLayout.HORIZONTAL);
        osc2_section_top.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        Slider osc2_type = new Slider(this, 81, Slider.HORIZONTAL);
        osc2_type.name = "osc2_type";
        osc2_type.setLabelText("osc2_type");
        osc2_type.setRange(0, 3);
        osc2_type.setIntegerValued(true);
        osc2_type.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc2_type.setFillRGB(150,150,150);
        //        registerWidget(osc2_type.getId(), "osc2_type");
        osc2_section_top.addView(osc2_type);

        Slider osc2_tune = new Slider(this, 82, Slider.HORIZONTAL);
        osc2_tune.name = "osc2_tune";
        osc2_tune.setLabelText("osc2_tune");
        osc2_tune.setRange(-12, 12);
        osc2_tune.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc2_tune.setFillRGB(150,150,150);
        //        registerWidget(osc2_tune.getId(), "osc2_type");
        osc2_section_top.addView(osc2_tune);

        LinearLayout osc2_section_bottom = new LinearLayout(this);
        osc2_section_bottom.setOrientation(LinearLayout.HORIZONTAL);
        osc2_section_bottom.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        Slider osc2_width = new Slider(this, 83, Slider.HORIZONTAL);
        osc2_width.name = "osc2_width";
        osc2_width.setLabelText("osc2_width");
        osc2_width.setRange(0, 3);
        osc2_width.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc2_width.setFillRGB(150,150,150);
        //        registerWidget(osc2_width.getId(), "osc2_width");
        osc2_section_bottom.addView(osc2_width);

        Slider osc2_detune = new Slider(this, 84, Slider.HORIZONTAL);
        osc2_detune.name = "osc2_detune";
        osc2_detune.setLabelText("osc2_detune");
        osc2_detune.setRange(-12, 12);
        osc2_detune.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc2_detune.setFillRGB(150,150,150);
        //        registerWidget(osc2_detune.getId(), "osc2_type");
        osc2_section_bottom.addView(osc2_detune);

        osc2_section.addView(osc2_section_top);
        osc2_section.addView(osc2_section_bottom);
        row2.addView(osc2_section);

        FillerWidget w5 = new FillerWidget(this);
        w5.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        //knob.setLayoutParams(new LinearLayout.LayoutParams(500, 500, 0));
        row2.addView(w5);

        LinearLayout filter_adsr = new LinearLayout(this);
        filter_adsr.setOrientation(LinearLayout.HORIZONTAL);
        filter_adsr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        Slider slider5 = new Slider(this, 5, Slider.VERTICAL);
        slider5.name = "slider1";
        slider5.setLabelText("a");
        slider5.setRange(0, 1);
        slider5.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider5.setFillRGB(240,240,240);
        //        registerWidget(slider.getId(), "osc1_type");
        filter_adsr.addView(slider5);

        Slider slider6 = new Slider(this, 6, Slider.VERTICAL);
        slider6.name = "slider2";
        slider6.setLabelText("d");
        slider6.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider6.setFillRGB(240,240,240);
        slider6.setRange(0, 1);
        //        registerWidget(slider2.getId(), "osc1_detune");
        filter_adsr.addView(slider6);

        Slider slider7 = new Slider(this, 7, Slider.VERTICAL);
        slider7.name = "slider3";
        slider7.setLabelText("s");
        slider7.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider7.setFillRGB(240,240,240);
        slider7.setRange(0, 1);
        //        registerWidget(slider3.getId(), "osc2_type");
        filter_adsr.addView(slider7);

        Slider slider8 = new Slider(this, 8, Slider.VERTICAL);
        slider8.name = "slider8";
        slider8.setLabelText("r");
        slider8.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider8.setFillRGB(240,240,240);
        slider8.setRange(0, 1);
        //        registerWidget(slider4.getId(), "osc2_detune");
        filter_adsr.addView(slider8);

        row2.addView(filter_adsr);
        lLayout.addView(row2);

        // FillerWidget w = new FillerWidget(this);
        // w.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        // //knob.setLayoutParams(new LinearLayout.LayoutParams(500, 500, 0));
        // lLayout.addView(w);

        LinearLayout steps = new LinearLayout(this);
        steps.setOrientation(LinearLayout.HORIZONTAL);
        steps.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        createStepButtons(steps);
        lLayout.addView(steps);

        LinearLayout aux = new LinearLayout(this);
        aux.setOrientation(LinearLayout.HORIZONTAL);
        aux.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        PlayButton playButton = new PlayButton(this, 90);
        playButton.name = "play/stop";
        playButton.setLabelText("Play");
        playButton.setFillRGB(0,200,0);
        playButton.updateColor();
        playButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        aux.addView(playButton);

        lLayout.addView(aux);

        setContentView(lLayout);

        // REMEMBER TO TURN THIS ON FOR SOUND
        //        bindService(new Intent("supercollider.START_SERVICE"), conn, BIND_AUTO_CREATE);
    }
    
    public Handler getTimerHandler() {
        return timerHandler;
    }
    
    public Runnable getTimerRunnable() {
        return timerRunnable;
    }

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            currentStep++;
            Log.d("Kosmische", "currentStep " + currentStep);
            Log.d("Kosmische", "sequence length " + sequence.getLength());
            if(currentStep >= sequence.getLength()) {
                Log.d("Kosmische", "resetting");
                currentStep = 0;
            }

            int previousStep = (currentStep - 1) % sequence.getLength();
            if(currentStep == 0) {
                previousStep = sequence.getLength() - 1;
            }

            Log.d("Kosmische", "previousStep " + previousStep);
            // restore preious step color
            if(previousFillRGB != null) {
                Log.d("Kosmische", "altering previousStep");
                stepButtons.get(previousStep).setFillRGB(previousFillRGB[0], previousFillRGB[1], previousFillRGB[2]);
                stepButtons.get(previousStep).updateColor();
                stepButtons.get(previousStep).setActive(false);
                stepButtons.get(previousStep).invalidate();
            }

            // set the current step active
            previousFillRGB = stepButtons.get(currentStep).getFillRGB();
            stepButtons.get(previousStep).updateColor();
            stepButtons.get(currentStep).setFillRGB(active_red, active_green, active_blue);
            stepButtons.get(previousStep).setActive(true);
            stepButtons.get(currentStep).invalidate();

            // get the note out of the sequence
            //            sequence.get(currentStep)

            timerHandler.postDelayed(this, (int) (60000 / bpm));
        }
    };
	
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
