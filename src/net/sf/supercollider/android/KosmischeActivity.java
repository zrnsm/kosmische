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
import android.os.HandlerThread;
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
    private TimerRunnable timerRunnable = new TimerRunnable();
    private Thread timerThread = new Thread(timerRunnable);

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



        // handlerThread = new HandlerThread("Timer");
        // handlerThread.start();
        // handler = new Handler(handlerThread.getLooper());

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
        row1.setBackgroundColor(android.R.color.white);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setLayoutParams(layoutParams);

        LinearLayout osc1_container = new LinearLayout(this);
        osc1_container.setOrientation(LinearLayout.HORIZONTAL);
        osc1_container.setBackgroundColor(android.R.color.white);
        osc1_container.setLayoutParams(layoutParams);

        // osc 1 section
        LinearLayout osc1_section = new LinearLayout(this);
        osc1_section.setOrientation(LinearLayout.VERTICAL);
        osc1_section.setBackgroundColor(android.R.color.white);
        osc1_section.setLayoutParams(layoutParams);

        LinearLayout osc1_section_top = new LinearLayout(this);
        osc1_section_top.setOrientation(LinearLayout.HORIZONTAL);
        osc1_section_top.setBackgroundColor(android.R.color.white);
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
        osc1_section_bottom.setBackgroundColor(android.R.color.white);
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

        Slider osc1_level = new Slider(this, registerWidget("osc1_level"), Slider.VERTICAL);
        osc1_level.setLabelText("osc1_level");
        osc1_level.setRange(1, 10);
        osc1_level.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4f));
        osc1_level.setFillRGB(190,190,190);

        osc1_container.addView(osc1_section);
        osc1_container.addView(osc1_level);
        row1.addView(osc1_container);

        // filter section
        LinearLayout filter_section = new LinearLayout(this);
        filter_section.setOrientation(LinearLayout.HORIZONTAL);
        filter_section.setLayoutParams(layoutParams);

        FillerWidget w1 = new FillerWidget(this);
        w1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 30f));
        filter_section.addView(w1);

        Knob cutoff_knob = new Knob(this, registerWidget("cutoff"));
        cutoff_knob.setLabelText("cutoff");
        cutoff_knob.setRange(0, 10000);
        cutoff_knob.setLayoutParams(layoutParams);
        cutoff_knob.setFillRGB(210,210,210);
        filter_section.addView(cutoff_knob);

        FillerWidget w2 = new FillerWidget(this);
        w2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 30f));
        filter_section.addView(w2);

        Knob resonance_knob = new Knob(this, registerWidget("resonance"));
        resonance_knob.setLabelText("reso");
        resonance_knob.setRange(0, 4);
        resonance_knob.setLayoutParams(layoutParams);
        resonance_knob.setFillRGB(210,210,210);
        filter_section.addView(resonance_knob);

        FillerWidget w3 = new FillerWidget(this);
        w3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 30f));
        filter_section.addView(w3);

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

        LinearLayout osc2_container = new LinearLayout(this);
        osc2_container.setOrientation(LinearLayout.HORIZONTAL);
        osc2_container.setLayoutParams(layoutParams);

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

        Slider osc2_level = new Slider(this, registerWidget("osc2_level"), Slider.VERTICAL);
        osc2_level.setLabelText("osc2_level");
        osc2_level.setRange(1, 10);
        osc2_level.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4f));
        osc2_level.setFillRGB(190,190,190);

        osc2_section.addView(osc2_section_top);
        osc2_section.addView(osc2_section_bottom);

        osc2_container.addView(osc2_section);
        osc2_container.addView(osc2_level);
        row2.addView(osc2_container);

        // lfo section

        LinearLayout lfo_section = new LinearLayout(this);
        lfo_section.setOrientation(LinearLayout.VERTICAL);
        lfo_section.setLayoutParams(layoutParams);

        LinearLayout lfo1 = new LinearLayout(this);
        lfo1.setOrientation(LinearLayout.HORIZONTAL);
        lfo1.setLayoutParams(layoutParams);

        ChoiceButtonGroup lfo1_type = new ChoiceButtonGroup(this, 
                                                            registerWidget("lfo1_type"),
                                                            new String[] {"S", "T", "P", "N"});
        lfo1_type.setLayoutParams(layoutParams);
        lfo1_type.setFillRGB(190,190,190);
        lfo1.addView(lfo1_type);

        final Slider lfo1_freq = new Slider(this, registerWidget("lfo1_freq"), Slider.HORIZONTAL);
        lfo1_freq.setLabelText("freq");
        lfo1_freq.setRange(0, 5);
        lfo1_freq.setLayoutParams(layoutParams);
        lfo1_freq.setFillRGB(150,150,150);
        lfo1.addView(lfo1_freq);

        final Slider lfo1_depth = new Slider(this, registerWidget("lfo1_depth"), Slider.HORIZONTAL);
        lfo1_depth.setLabelText("depth");
        lfo1_depth.setRange(0, 200);
        lfo1_depth.setLayoutParams(layoutParams);
        lfo1_depth.setFillRGB(150,150,150);
        lfo1.addView(lfo1_depth);

        final ScrollBox lfo1_target = new ScrollBox(this, 
                                                            registerWidget("lfo1_target"),
                                                            new String[] {"O1F", 
                                                                          "O1W", 
                                                                          "O2F", 
                                                                          "O2W", 
                                                                          "DT",
                                                                          "DecT",
                                                                          "Cut",
                                                                          "Reso"
                                                            });

        final float[][] lfoDepthRanges = {
            {0,10},
            {0,1},
            {0,10},
            {0,1},
            {0,2},
            {0,10},
            {0,10000},
            {0,3},
        };

        lfo1_target.setOnChangeRunnable(new Runnable() {
                public void run() {
                    float[] range = lfoDepthRanges[lfo1_target.getSelectedValue()];
                    lfo1_depth.setRange(range[0], range[1]);
                }
            });

        lfo1_target.setLayoutParams(layoutParams);
        lfo1_target.setFillRGB(150,150,150);
        lfo1.addView(lfo1_target);
        lfo_section.addView(lfo1);

        LinearLayout lfo2 = new LinearLayout(this);
        lfo2.setOrientation(LinearLayout.HORIZONTAL);
        lfo2.setLayoutParams(layoutParams);

        ChoiceButtonGroup lfo2_type = new ChoiceButtonGroup(this, 
                                                            registerWidget("lfo2_type"),
                                                            new String[] {"S", "T", "P", "N"});
        lfo2_type.setLayoutParams(layoutParams);
        lfo2_type.setFillRGB(190,190,190);
        lfo2.addView(lfo2_type);

        Slider lfo2_freq = new Slider(this, registerWidget("lfo2_freq"), Slider.HORIZONTAL);
        lfo2_freq.setLabelText("freq");
        lfo2_freq.setRange(0, 5);
        lfo2_freq.setLayoutParams(layoutParams);
        lfo2_freq.setFillRGB(150,150,150);
        lfo2.addView(lfo2_freq);

        final Slider lfo2_depth = new Slider(this, registerWidget("lfo2_depth"), Slider.HORIZONTAL);
        lfo2_depth.setLabelText("depth");
        lfo2_depth.setRange(0, 200);
        lfo2_depth.setLayoutParams(layoutParams);
        lfo2_depth.setFillRGB(150,150,150);
        lfo2.addView(lfo2_depth);

        final ScrollBox lfo2_target = new ScrollBox(this, 
                                                            registerWidget("lfo2_target"),
                                                            new String[] {"O1F", 
                                                                          "O1W", 
                                                                          "O2F", 
                                                                          "O2W", 
                                                                          "DT",
                                                                          "DecT",
                                                                          "Cut",
                                                                          "Reso"
                                              });

        lfo2_target.setOnChangeRunnable(new Runnable() {
                public void run() {
                    float[] range = lfoDepthRanges[lfo2_target.getSelectedValue()];
                    lfo2_depth.setRange(range[0], range[1]);
                }
            });
        lfo2_target.setLayoutParams(layoutParams);
        lfo2_target.setFillRGB(150,150,150);
        lfo2.addView(lfo2_target);


        lfo_section.addView(lfo2);

        row2.addView(lfo_section);

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

        Knob delayMixKnob = new Knob(this, registerWidget("delay_mix"));
        delayMixKnob.setLabelText("delay mix");
        delayMixKnob.setRange(-1, 1);
        delayMixKnob.setLayoutParams(layoutParams);
        delayMixKnob.setFillRGB(210,210,210);
        effects.addView(delayMixKnob);

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
        lLayout.setBackgroundColor(android.R.color.white);
        setContentView(lLayout);

        bindService(new Intent("supercollider.START_SERVICE"), conn, BIND_AUTO_CREATE);
    }

    public Sequence getSequence() {
        return sequence;
    }
    
    public class TimerRunnable implements Runnable {
        private Object mPauseLock = new Object();
        private boolean mPaused = false;
        private boolean mFinished = false;

        public void onPause() {
            synchronized (mPauseLock) {
                mPaused = true;
            }
        }

        public void onResume() {
            synchronized (mPauseLock) {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }
            
        @Override
        public void run() {
            try {
                while(true) {
                    synchronized (mPauseLock) {
                        while (mPaused) {
                            try {
                                mPauseLock.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                    Thread.sleep((long) (60000 / bpm / 4.0 - 1));
                    sendControlMessage("trigger", 0);
                    
                    currentStep++;

                    if(currentStep >= sequence.getLength()) {
                        currentStep = 0;
                    }

                    // int previousStep = (currentStep - 1) % sequence.getLength();
                    // if(currentStep == 0) {
                    //     previousStep = sequence.getLength() - 1;
                    // }

                    // Log.d("Kosmische", "previousStep " + previousStep);
                    // // restore preious step color
                    // StepButton previousStepButton = stepButtons.get(previousStep);
                    // if(previousFill != null) {
                    //     Log.d("Kosmische", "altering previousStep");
                    //     previousStepButton.setFill(previousFill);
                    //     previousStepButton.setIsCurrentStep(false);
                    //     previousStepButton.invalidate();
                    // }

                    // // set the current step active
                    // StepButton currentStepButton = stepButtons.get(currentStep);
                    // previousFill = currentStepButton.getFill();
                    // currentStepButton.setFill(activeFill);
                    // currentStepButton.setIsCurrentStep(true);
                    // currentStepButton.invalidate();

                    // get the note out of the sequence
                    if(stepButtons.get(currentStep).isSelected()) {
                        sendControlMessage("note", sequence.get(currentStep).getMidiNumber());
                        sendControlMessage("trigger", 1);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public TimerRunnable getTimerRunnable() {
        return timerRunnable;
    }

    public Thread getTimerThread() {
        return timerThread;
    }

    // private Runnable timerRunnable = new Runnable() {
    //     @Override
    //     public void run() {
    //         currentStep++;

    //         if(currentStep >= sequence.getLength()) {
    //             currentStep = 0;
    //         }

    //         int previousStep = (currentStep - 1) % sequence.getLength();
    //         if(currentStep == 0) {
    //             previousStep = sequence.getLength() - 1;
    //         }

    //         // Log.d("Kosmische", "previousStep " + previousStep);
    //         // // restore preious step color
    //         // StepButton previousStepButton = stepButtons.get(previousStep);
    //         // if(previousFill != null) {
    //         //     Log.d("Kosmische", "altering previousStep");
    //         //     previousStepButton.setFill(previousFill);
    //         //     previousStepButton.setIsCurrentStep(false);
    //         //     previousStepButton.invalidate();
    //         // }

    //         // // set the current step active
    //         // StepButton currentStepButton = stepButtons.get(currentStep);
    //         // previousFill = currentStepButton.getFill();
    //         // currentStepButton.setFill(activeFill);
    //         // currentStepButton.setIsCurrentStep(true);
    //         // currentStepButton.invalidate();

    //         // get the note out of the sequence
    //         if(stepButtons.get(currentStep).isSelected()) {
    //             sendControlMessage("note", sequence.get(currentStep).getMidiNumber());
    //             sendControlMessage("trigger", 1);
    //             try {
    //                 Thread.sleep((int) (60000 / bpm / 4.0 / 4.0));
    //             }
    //             catch(InterruptedException e) {
    //                 e.printStackTrace();
    //             }
    //             sendControlMessage("trigger", 0);
    //         }
    //         handler.postDelayed(this, (int) (60000 / bpm / 4.0));
    //     }
    // };

    public void setBPM(float bpm) {
        this.bpm = bpm;
    }
	
    @Override
    public void onPause() {
        super.onPause();
        timerRunnable.onPause();
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
