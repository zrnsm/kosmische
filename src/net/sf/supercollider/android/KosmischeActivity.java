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
            button.setSelected(true);
            button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            stepButtons.add(button);
            steps.addView(button);
        }
    }
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        sequence = new Sequence(16);
        stepButtons = new ArrayList<StepButton>(sequenceLength);
        activeFill = new Paint();
        activeFill.setARGB(255, 255, 0, 0);
        parameterMap = new HashMap<Integer, String>();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);

        LinearLayout lLayout = new LinearLayout(this);
        lLayout.setOrientation(LinearLayout.VERTICAL);
        lLayout.setLayoutParams(layoutParams);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setLayoutParams(layoutParams);

        // osc 1 section
        LinearLayout osc1_section = new LinearLayout(this);
        osc1_section.setOrientation(LinearLayout.VERTICAL);
        osc1_section.setLayoutParams(layoutParams);

        LinearLayout osc1_section_top = new LinearLayout(this);
        osc1_section_top.setOrientation(LinearLayout.HORIZONTAL);
        osc1_section_top.setLayoutParams(layoutParams);

        ChoiceButtonGroup osc1_type = new ChoiceButtonGroup(this, 
                                                            registerWidget("osc1_type"),
                                                            new String[] {"Saw", "Pulse", "Sin", "Noise"});
        osc1_type.setLayoutParams(layoutParams);
        osc1_type.setFillRGB(190,190,190);
        osc1_section_top.addView(osc1_type);

        Slider osc1_tune = new Slider(this, registerWidget("osc1_tune"), Slider.HORIZONTAL);
        osc1_tune.setLabelText("osc1_tune");
        osc1_tune.setRange(-12, 12);
        osc1_tune.setIntegerValued(true);
        osc1_tune.setLayoutParams(layoutParams);
        osc1_tune.setFillRGB(190,190,190);
        osc1_section_top.addView(osc1_tune);

        LinearLayout osc1_section_bottom = new LinearLayout(this);
        osc1_section_bottom.setOrientation(LinearLayout.HORIZONTAL);
        osc1_section_bottom.setLayoutParams(layoutParams);

        Slider osc1_width = new Slider(this, registerWidget("osc1_width"), Slider.HORIZONTAL);
        osc1_width.setLabelText("osc1_width");
        osc1_width.setRange(0, 1);
        osc1_width.setLayoutParams(layoutParams);
        osc1_width.setFillRGB(190,190,190);
        //        registerWidget(osc1_width.getId(), "osc1_width");
        osc1_section_bottom.addView(osc1_width);

        Slider osc1_detune = new Slider(this, registerWidget("osc1_detune"), Slider.HORIZONTAL);
        osc1_detune.setLabelText("osc1_detune");
        osc1_detune.setRange(-20, 20);
        osc1_detune.setLayoutParams(layoutParams);
        osc1_detune.setFillRGB(190,190,190);
        osc1_section_bottom.addView(osc1_detune);

        osc1_section.addView(osc1_section_top);
        osc1_section.addView(osc1_section_bottom);
        row1.addView(osc1_section);

        // filter section
        LinearLayout filter_section = new LinearLayout(this);
        filter_section.setOrientation(LinearLayout.HORIZONTAL);
        filter_section.setLayoutParams(layoutParams);

        Knob cutoff_knob = new Knob(this, registerWidget("cutoff"));
        cutoff_knob.setLabelText("cutoff");
        cutoff_knob.setRange(0, 10000);
        cutoff_knob.setLayoutParams(layoutParams);
        cutoff_knob.setFillRGB(210,210,210);
        filter_section.addView(cutoff_knob);

        Knob resonance_knob = new Knob(this, registerWidget("resonance"));
        resonance_knob.setLabelText("reso");
        resonance_knob.setRange(0, 4);
        resonance_knob.setLayoutParams(layoutParams);
        resonance_knob.setFillRGB(210,210,210);
        filter_section.addView(resonance_knob);

        row1.addView(filter_section);

        LinearLayout amp_adsr = new LinearLayout(this);
        amp_adsr.setOrientation(LinearLayout.HORIZONTAL);
        amp_adsr.setLayoutParams(layoutParams);

        Slider slider = new Slider(this, registerWidget("amp_attack"), Slider.VERTICAL);
        slider.setLabelText("a");
        slider.setRange(0, 1);
        slider.setLayoutParams(layoutParams);
        slider.setFillRGB(210,210,210);
        amp_adsr.addView(slider);

        Slider slider2 = new Slider(this, registerWidget("amp_decay"), Slider.VERTICAL);
        slider2.setLabelText("d");
        slider2.setLayoutParams(layoutParams);
        slider2.setFillRGB(210,210,210);
        slider2.setRange(0, 1);
        amp_adsr.addView(slider2);

        Slider slider3 = new Slider(this, registerWidget("amp_sustain"), Slider.VERTICAL);
        slider3.setLabelText("s");
        slider3.setLayoutParams(layoutParams);
        slider3.setFillRGB(210,210,210);
        slider3.setRange(0, 1);
        amp_adsr.addView(slider3);

        Slider slider4 = new Slider(this, registerWidget("amp_release"), Slider.VERTICAL);
        slider4.setLabelText("r");
        slider4.setLayoutParams(layoutParams);
        slider4.setFillRGB(210,210,210);
        slider4.setRange(0, 1);
        amp_adsr.addView(slider4);

        row1.addView(amp_adsr);

        lLayout.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setLayoutParams(layoutParams);

        // osc 2 section
        LinearLayout osc2_section = new LinearLayout(this);
        osc2_section.setOrientation(LinearLayout.VERTICAL);
        osc2_section.setLayoutParams(layoutParams);

        LinearLayout osc2_section_top = new LinearLayout(this);
        osc2_section_top.setOrientation(LinearLayout.HORIZONTAL);
        osc2_section_top.setLayoutParams(layoutParams);

        ChoiceButtonGroup osc2_type = new ChoiceButtonGroup(this, 
                                                            registerWidget("osc2_type"),
                                                            new String[] {"Saw", "Pulse", "Sin", "Noise"});
        osc2_type.setLayoutParams(layoutParams);
        osc2_type.setFillRGB(190,190,190);
        osc2_section_top.addView(osc2_type);

        Slider osc2_tune = new Slider(this, registerWidget("osc2_tune"), Slider.HORIZONTAL);
        osc2_tune.setLabelText("osc2_tune");
        osc2_tune.setRange(-12, 12);
        osc2_tune.setIntegerValued(true);
        osc2_tune.setLayoutParams(layoutParams);
        osc2_tune.setFillRGB(150,150,150);
        osc2_section_top.addView(osc2_tune);

        LinearLayout osc2_section_bottom = new LinearLayout(this);
        osc2_section_bottom.setOrientation(LinearLayout.HORIZONTAL);
        osc2_section_bottom.setLayoutParams(layoutParams);

        Slider osc2_width = new Slider(this, registerWidget("osc2_width"), Slider.HORIZONTAL);
        osc2_width.setLabelText("osc2_width");
        osc2_width.setRange(0, 1);
        osc2_width.setLayoutParams(layoutParams);
        osc2_width.setFillRGB(150,150,150);
        osc2_section_bottom.addView(osc2_width);

        Slider osc2_detune = new Slider(this, registerWidget("osc2_detune"), Slider.HORIZONTAL);
        osc2_detune.setLabelText("osc2_detune");
        osc2_detune.setRange(-20, 20);
        osc2_detune.setLayoutParams(layoutParams);
        osc2_detune.setFillRGB(150,150,150);
        osc2_section_bottom.addView(osc2_detune);

        osc2_section.addView(osc2_section_top);
        osc2_section.addView(osc2_section_bottom);
        row2.addView(osc2_section);

        FillerWidget w5 = new FillerWidget(this);
        w5.setLayoutParams(layoutParams);
        row2.addView(w5);

        LinearLayout filter_adsr = new LinearLayout(this);
        filter_adsr.setOrientation(LinearLayout.HORIZONTAL);
        filter_adsr.setLayoutParams(layoutParams);

        Slider slider5 = new Slider(this, registerWidget("filter_attack"), Slider.VERTICAL);
        slider5.setLabelText("a");
        slider5.setRange(0, 1);
        slider5.setLayoutParams(layoutParams);
        slider5.setFillRGB(240,240,240);
        filter_adsr.addView(slider5);

        Slider slider6 = new Slider(this, registerWidget("filter_decay"), Slider.VERTICAL);
        slider6.setLabelText("d");
        slider6.setLayoutParams(layoutParams);
        slider6.setFillRGB(240,240,240);
        slider6.setRange(0, 1);
        filter_adsr.addView(slider6);

        Slider slider7 = new Slider(this, registerWidget("filter_sustain"), Slider.VERTICAL);
        slider7.setLabelText("s");
        slider7.setLayoutParams(layoutParams);
        slider7.setFillRGB(240,240,240);
        slider7.setRange(0, 1);
        filter_adsr.addView(slider7);

        Slider slider8 = new Slider(this, registerWidget("filter_release"), Slider.VERTICAL);
        slider8.setLabelText("r");
        slider8.setLayoutParams(layoutParams);
        slider8.setFillRGB(240,240,240);
        slider8.setRange(0, 1);
        filter_adsr.addView(slider8);

        Slider slider9 = new Slider(this, registerWidget("filter_env_amount"), Slider.VERTICAL);
        slider9.setLabelText("amt");
        slider9.setLayoutParams(layoutParams);
        slider9.setFillRGB(240,240,240);
        slider9.setRange(-1, 1);
        filter_adsr.addView(slider9);

        row2.addView(filter_adsr);
        lLayout.addView(row2);

        // FillerWidget w = new FillerWidget(this);
        // w.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        // //knob.setLayoutParams(new LinearLayout.LayoutParams(500, 500, 0));
        // lLayout.addView(w);

        LinearLayout steps = new LinearLayout(this);
        steps.setOrientation(LinearLayout.HORIZONTAL);
        steps.setLayoutParams(layoutParams);

        createStepButtons(steps);
        lLayout.addView(steps);

        LinearLayout aux = new LinearLayout(this);
        aux.setOrientation(LinearLayout.HORIZONTAL);
        aux.setLayoutParams(layoutParams);

        LinearLayout playTempo = new LinearLayout(this);
        playTempo.setOrientation(LinearLayout.HORIZONTAL);
        playTempo.setLayoutParams(layoutParams);

        PlayButton playButton = new PlayButton(this, 1000);
        playButton.setLabelText("Play");
        playButton.setFillRGB(0, 200, 0);
        playButton.setLayoutParams(layoutParams);
        playTempo.addView(playButton);

        TempoSlider tempoSlider = new TempoSlider(this, 1001, Slider.VERTICAL);
        tempoSlider.setLabelText("Tempo");
        tempoSlider.setRange(1, 500);
        tempoSlider.setFillRGB(0, 200, 0);
        tempoSlider.setLayoutParams(layoutParams);
        playTempo.addView(tempoSlider);
        
        aux.addView(playTempo);

        LinearLayout effects = new LinearLayout(this);
        effects.setOrientation(LinearLayout.HORIZONTAL);
        effects.setLayoutParams(layoutParams);

        Knob delaytimeKnob = new Knob(this, registerWidget("delaytime"));
        delaytimeKnob.setLabelText("delay time");
        delaytimeKnob.setRange(0, 2);
        delaytimeKnob.setLayoutParams(layoutParams);
        delaytimeKnob.setFillRGB(210,210,210);
        effects.addView(delaytimeKnob);

        Knob feedbackKnob = new Knob(this, registerWidget("decaytime"));
        feedbackKnob.setLabelText("fb");
        feedbackKnob.setRange(0, 10);
        feedbackKnob.setLayoutParams(layoutParams);
        feedbackKnob.setFillRGB(210,210,210);
        effects.addView(feedbackKnob);

        Knob reverbMixKnob = new Knob(this, registerWidget("reverb_mix"));
        reverbMixKnob.setLabelText("r mix");
        reverbMixKnob.setRange(0, 1);
        reverbMixKnob.setLayoutParams(layoutParams);
        reverbMixKnob.setFillRGB(210,210,210);
        effects.addView(reverbMixKnob);

        Knob roomSizeKnob = new Knob(this, registerWidget("reverb_room_size"));
        roomSizeKnob.setLabelText("r size");
        roomSizeKnob.setRange(0, 1);
        roomSizeKnob.setLayoutParams(layoutParams);
        roomSizeKnob.setFillRGB(210,210,210);
        effects.addView(roomSizeKnob);

        Knob reverbDamp = new Knob(this, registerWidget("reverb_damp"));
        reverbDamp.setLabelText("r damp");
        reverbDamp.setRange(0, 1);
        reverbDamp.setLayoutParams(layoutParams);
        reverbDamp.setFillRGB(210,210,210);
        effects.addView(reverbDamp);

        aux.addView(effects);

        lLayout.addView(aux);
        setContentView(lLayout);

        bindService(new Intent("supercollider.START_SERVICE"), conn, BIND_AUTO_CREATE);
    }

    public Sequence getSequence() {
        return sequence;
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
            StepButton previousStepButton = stepButtons.get(previousStep);
            if(previousFill != null) {
                Log.d("Kosmische", "altering previousStep");
                previousStepButton.setFill(previousFill);
                previousStepButton.setIsCurrentStep(false);
                previousStepButton.invalidate();
            }

            // set the current step active
            StepButton currentStepButton = stepButtons.get(currentStep);
            previousFill = currentStepButton.getFill();
            currentStepButton.setFill(activeFill);
            currentStepButton.setIsCurrentStep(true);
            currentStepButton.invalidate();

            // get the note out of the sequence
            if(currentStepButton.isSelected()) {
                sendControlMessage("note", sequence.get(currentStep).getMidiNumber());
                sendControlMessage("trigger", 1);
                try {
                    Thread.sleep((int) (60000 / bpm / 4.0 / 2.0));
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
                sendControlMessage("trigger", 0);
            }
            timerHandler.postDelayed(this, (int) (60000 / bpm / 4.0));
        }
    };

    public void setBPM(float bpm) {
        this.bpm = bpm;
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
