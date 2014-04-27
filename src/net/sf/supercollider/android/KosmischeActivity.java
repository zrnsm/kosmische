package net.sf.supercollider.android;

import net.sf.supercollider.android.ISuperCollider;
import net.sf.supercollider.android.KKnob;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.util.Log;
import android.view.Gravity;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.Thread;
import java.lang.InterruptedException;

public class KosmischeActivity extends Activity {
    private ServiceConnection conn = new ScServiceConnection();
    private ISuperCollider.Stub superCollider;
    private KKnob mainWidget = null;
    private int defaultNodeId = 999;
    private HashMap<Integer, String> parameterMap;
    private float bpm = 120.0f;
    private int currentStep = -1;

    private Sequence sequence;
    private ArrayList<StepButton> stepButtons;
    private final int sequenceLength = 16;

    private Paint activeFill;
    private Paint previousFill;

    private int nextWidgetId = 0;

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

    private int registerWidget(String parameter) {
        int id = nextWidgetId;
        nextWidgetId++;
        parameterMap.put(id, parameter);
        return id;
    }

    public void sendControlMessage(String parameterName, float value) {
        Log.d("Kosmische", parameterName + ": " + value);
        OscMessage controlMessage = new OscMessage( new Object[] {
                "/n_set", defaultNodeId, parameterName, value
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
    
    public void sendControlMessage(int id, float value) {
        Log.d("Kosmische", id + ", " + parameterMap.get(id) + ", " + value);
        sendControlMessage(parameterMap.get(id), value);
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
        for(int i = 0; i < sequenceLength; i++) {
            StepButton button = new StepButton(this, i);
            button.setLabelText(sequence.get(i).getMidiNumber().toString());
            button.setFillRGB(200, 200, 200);
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
        stepButtons = new ArrayList<StepButton>(sequenceLength);
        activeFill = new Paint();
        activeFill.setARGB(255, 255, 0, 0);

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

        Slider osc1_type = new Slider(this, registerWidget("osc1_type"), Slider.HORIZONTAL);
        osc1_type.setLabelText("osc1_type");
        osc1_type.setRange(0, 3);
        osc1_type.setIntegerValued(true);
        osc1_type.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc1_type.setFillRGB(190,190,190);
        osc1_section_top.addView(osc1_type);

        Slider osc1_tune = new Slider(this, registerWidget("osc1_tune"), Slider.HORIZONTAL);
        osc1_tune.setLabelText("osc1_tune");
        osc1_tune.setRange(-12, 12);
        osc1_tune.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc1_tune.setFillRGB(190,190,190);
        osc1_section_top.addView(osc1_tune);

        LinearLayout osc1_section_bottom = new LinearLayout(this);
        osc1_section_bottom.setOrientation(LinearLayout.HORIZONTAL);
        osc1_section_bottom.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        Slider osc1_width = new Slider(this, registerWidget("osc1_width"), Slider.HORIZONTAL);
        osc1_width.setLabelText("osc1_width");
        osc1_width.setRange(0, 1);
        osc1_width.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc1_width.setFillRGB(190,190,190);
        //        registerWidget(osc1_width.getId(), "osc1_width");
        osc1_section_bottom.addView(osc1_width);

        Slider osc1_detune = new Slider(this, registerWidget("osc1_detune"), Slider.HORIZONTAL);
        osc1_detune.setLabelText("osc1_detune");
        osc1_detune.setRange(-500, 500);
        osc1_detune.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc1_detune.setFillRGB(190,190,190);
        osc1_section_bottom.addView(osc1_detune);

        osc1_section.addView(osc1_section_top);
        osc1_section.addView(osc1_section_bottom);
        row1.addView(osc1_section);

        // filter section
        LinearLayout filter_section = new LinearLayout(this);
        filter_section.setOrientation(LinearLayout.HORIZONTAL);
        filter_section.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        Knob cutoff_knob = new Knob(this, registerWidget("cutoff"));
        cutoff_knob.setLabelText("cutoff");
        cutoff_knob.setRange(0, 10000);
        cutoff_knob.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        cutoff_knob.setFillRGB(210,210,210);
        filter_section.addView(cutoff_knob);

        Knob resonance_knob = new Knob(this, registerWidget("resonance"));
        resonance_knob.setLabelText("reso");
        resonance_knob.setRange(0, 4);
        resonance_knob.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        resonance_knob.setFillRGB(210,210,210);
        filter_section.addView(resonance_knob);

        row1.addView(filter_section);

        LinearLayout amp_adsr = new LinearLayout(this);
        amp_adsr.setOrientation(LinearLayout.HORIZONTAL);
        amp_adsr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        Slider slider = new Slider(this, registerWidget("amp_attack"), Slider.VERTICAL);
        slider.setLabelText("a");
        slider.setRange(0, 1);
        slider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider.setFillRGB(210,210,210);
        amp_adsr.addView(slider);

        Slider slider2 = new Slider(this, registerWidget("amp_decay"), Slider.VERTICAL);
        slider2.setLabelText("d");
        slider2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider2.setFillRGB(210,210,210);
        slider2.setRange(0, 1);
        amp_adsr.addView(slider2);

        Slider slider3 = new Slider(this, registerWidget("amp_sustain"), Slider.VERTICAL);
        slider3.setLabelText("s");
        slider3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider3.setFillRGB(210,210,210);
        slider3.setRange(0, 1);
        amp_adsr.addView(slider3);

        Slider slider4 = new Slider(this, registerWidget("amp_release"), Slider.VERTICAL);
        slider4.setLabelText("r");
        slider4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider4.setFillRGB(210,210,210);
        slider4.setRange(0, 1);
        amp_adsr.addView(slider4);

        row1.addView(amp_adsr);

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

        Slider osc2_type = new Slider(this, registerWidget("osc2_type"), Slider.HORIZONTAL);
        osc2_type.setLabelText("osc2_type");
        osc2_type.setRange(0, 3);
        osc2_type.setIntegerValued(true);
        osc2_type.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc2_type.setFillRGB(150,150,150);
        osc2_section_top.addView(osc2_type);

        Slider osc2_tune = new Slider(this, registerWidget("osc2_tune"), Slider.HORIZONTAL);
        osc2_tune.setLabelText("osc2_tune");
        osc2_tune.setRange(-12, 12);
        osc2_tune.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc2_tune.setFillRGB(150,150,150);
        osc2_section_top.addView(osc2_tune);

        LinearLayout osc2_section_bottom = new LinearLayout(this);
        osc2_section_bottom.setOrientation(LinearLayout.HORIZONTAL);
        osc2_section_bottom.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        Slider osc2_width = new Slider(this, registerWidget("osc2_width"), Slider.HORIZONTAL);
        osc2_width.setLabelText("osc2_width");
        osc2_width.setRange(0, 1);
        osc2_width.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc2_width.setFillRGB(150,150,150);
        osc2_section_bottom.addView(osc2_width);

        Slider osc2_detune = new Slider(this, registerWidget("osc2_detune"), Slider.HORIZONTAL);
        osc2_detune.setLabelText("osc2_detune");
        osc2_detune.setRange(-500, 500);
        osc2_detune.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        osc2_detune.setFillRGB(150,150,150);
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

        Slider slider5 = new Slider(this, registerWidget("filter_attack"), Slider.VERTICAL);
        slider5.setLabelText("a");
        slider5.setRange(0, 1);
        slider5.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider5.setFillRGB(240,240,240);
        filter_adsr.addView(slider5);

        Slider slider6 = new Slider(this, registerWidget("filter_decay"), Slider.VERTICAL);
        slider6.setLabelText("d");
        slider6.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider6.setFillRGB(240,240,240);
        slider6.setRange(0, 1);
        filter_adsr.addView(slider6);

        Slider slider7 = new Slider(this, registerWidget("filter_sustain"), Slider.VERTICAL);
        slider7.setLabelText("s");
        slider7.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider7.setFillRGB(240,240,240);
        slider7.setRange(0, 1);
        filter_adsr.addView(slider7);

        Slider slider8 = new Slider(this, registerWidget("filter_release"), Slider.VERTICAL);
        slider8.setLabelText("r");
        slider8.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        slider8.setFillRGB(240,240,240);
        slider8.setRange(0, 1);
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
        playButton.setLabelText("Play");
        playButton.setFillRGB(0, 200, 0);
        playButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        aux.addView(playButton);
        lLayout.addView(aux);
        setContentView(lLayout);

        bindService(new Intent("supercollider.START_SERVICE"), conn, BIND_AUTO_CREATE);
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
            if(previousFill != null) {
                Log.d("Kosmische", "altering previousStep");
                stepButtons.get(previousStep).setFill(previousFill);
                stepButtons.get(previousStep).setIsCurrentStep(false);
                stepButtons.get(previousStep).invalidate();
            }

            // set the current step active
            previousFill = stepButtons.get(currentStep).getFill();
            stepButtons.get(currentStep).setFill(activeFill);
            stepButtons.get(currentStep).setIsCurrentStep(true);
            stepButtons.get(currentStep).invalidate();

            // get the note out of the sequence
            sendControlMessage("note", sequence.get(currentStep).getMidiNumber());
            sendControlMessage("trigger", 1);
            try {
                Thread.sleep((int) (60000 / bpm / 4.0 / 2.0));
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
            sendControlMessage("trigger", 0);

            timerHandler.postDelayed(this, (int) (60000 / bpm / 4.0));
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
