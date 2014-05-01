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
import android.graphics.Color;
import java.io.FileNotFoundException;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class KosmischeActivity extends Activity {
    private ServiceConnection conn = new ScServiceConnection();
    private ISuperCollider.Stub superCollider;
    private KKnob mainWidget = null;
    private int defaultNodeId = 999;
    private HashMap<Integer, String> parameterMap;
    private ArrayList<KosmischeWidget> widgets;
    private float bpm = 120.0f;
    private int currentStep = -1;
    private boolean sequenceReversed = false;
    private TimerRunnable timerRunnable = new TimerRunnable();
    private Thread timerThread = new Thread(timerRunnable);
    private JSONPersister sequencePersister;
    private JSONPersister patchPersister;
    private Sequence sequence;
    private Patch patch;
    private ArrayList<StepButton> stepButtons;
    private final int sequenceLength = 16;
    private Paint activeFill;
    private Paint previousFill;
    private int nextWidgetId = 0;

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
            if(superCollider != null) {
                superCollider.sendMessage(controlMessage);
            }
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
                superCollider.start();
                superCollider.sendMessage(OscMessage.createSynthMessage("Kosmische", defaultNodeId, 0, 1));
            } catch (RemoteException re) {
                Log.d("Kosmische", re.toString());
            }
        }
        //@Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private void loadPatch() {
 
        try {
            JSONObject values = patch.getPatchValues();
            for(KosmischeWidget widget : widgets) {
                String param = parameterMap.get(widget.getId());
                Log.d("Kosmische", param);
                //Log.d("Kosmische", ((Integer) widget.getId()).toString());
                //Log.d("Kosmsiche", "what the fuck");
                // Log.d("Kosmsiche", ((Boolean) (parameterMap == null)).toString());
                Log.d("Kosmische", values.toString());
                // Log.d("Kosmsiche", parameterMap.get(widget.getId()));
                Log.d("Kosmische", values.get(param).toString());
                widget.setPosition(((Double) values.get(param)).floatValue());
            }
        }
        catch(JSONException e) {
            Log.d("Kosmische", "failed to load patch " + e);
        }
    }

    private void createStepButtons(LinearLayout steps) {
        for(int i = 0; i < sequenceLength; i++) {
            StepButton button = new StepButton(this, i);
            button.setLabelText(sequence.getNote(i).getMidiNumber().toString());
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
        patch = new Patch();
        widgets = new ArrayList<KosmischeWidget>();

        sequencePersister = new JSONPersister(this, "pattern1");
        patchPersister = new JSONPersister(this, "patch1");
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

        LinearLayout osc1_container = new LinearLayout(this);
        osc1_container.setOrientation(LinearLayout.HORIZONTAL);
        osc1_container.setLayoutParams(layoutParams);

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
        osc1_section_top.addView(osc1_type);
        widgets.add(osc1_type);

        Slider osc1_tune = new Slider(this, registerWidget("osc1_tune"), Slider.HORIZONTAL);
        osc1_tune.setLabelText("coarse tune");
        osc1_tune.setRange(-12, 12);
        osc1_tune.setIntegerValued(true);
        osc1_tune.setLayoutParams(layoutParams);
        osc1_section_top.addView(osc1_tune);
        widgets.add(osc1_tune);
        
        LinearLayout osc1_section_bottom = new LinearLayout(this);
        osc1_section_bottom.setOrientation(LinearLayout.HORIZONTAL);
        osc1_section_bottom.setLayoutParams(layoutParams);

        Slider osc1_width = new Slider(this, registerWidget("osc1_width"), Slider.HORIZONTAL);
        osc1_width.setLabelText("pulsewidth");
        osc1_width.setRange(0, 1);
        osc1_width.setLayoutParams(layoutParams);
        osc1_section_bottom.addView(osc1_width);
        widgets.add(osc1_width);

        Slider osc1_detune = new Slider(this, registerWidget("osc1_detune"), Slider.HORIZONTAL);
        osc1_detune.setLabelText("detune");
        osc1_detune.setRange(-20, 20);
        osc1_detune.setLayoutParams(layoutParams);
        osc1_section_bottom.addView(osc1_detune);
        widgets.add(osc1_detune);

        osc1_section.addView(osc1_section_top);
        osc1_section.addView(osc1_section_bottom);

        Slider osc1_level = new Slider(this, registerWidget("osc1_level"), Slider.VERTICAL);
        osc1_level.setLabelText("level");
        osc1_level.setRange(1, 10);
        osc1_level.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4f));
        widgets.add(osc1_level);

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
        filter_section.addView(cutoff_knob);
        widgets.add(cutoff_knob);

        FillerWidget w2 = new FillerWidget(this);
        w2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 30f));
        filter_section.addView(w2);

        Knob resonance_knob = new Knob(this, registerWidget("resonance"));
        resonance_knob.setLabelText("reso");
        resonance_knob.setRange(0, 4);
        resonance_knob.setLayoutParams(layoutParams);
        filter_section.addView(resonance_knob);
        widgets.add(resonance_knob);

        FillerWidget w3 = new FillerWidget(this);
        w3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 30f));
        filter_section.addView(w3);

        row1.addView(filter_section);

        LinearLayout amp_adsr = new LinearLayout(this);
        amp_adsr.setOrientation(LinearLayout.HORIZONTAL);
        amp_adsr.setLayoutParams(layoutParams);

        Slider amp_attack = new Slider(this, registerWidget("amp_attack"), Slider.VERTICAL);
        amp_attack.setLabelText("a");
        amp_attack.setRange(0, 1);
        amp_attack.setLayoutParams(layoutParams);
        amp_adsr.addView(amp_attack);
        widgets.add(amp_attack);

        Slider amp_decay = new Slider(this, registerWidget("amp_decay"), Slider.VERTICAL);
        amp_decay.setLabelText("d");
        amp_decay.setLayoutParams(layoutParams);
        amp_decay.setRange(0, 1);
        amp_adsr.addView(amp_decay);
        widgets.add(amp_decay);

        Slider amp_sustain = new Slider(this, registerWidget("amp_sustain"), Slider.VERTICAL);
        amp_sustain.setLabelText("s");
        amp_sustain.setLayoutParams(layoutParams);
        amp_sustain.setRange(0, 1);
        amp_adsr.addView(amp_sustain);
        widgets.add(amp_sustain);

        Slider amp_release = new Slider(this, registerWidget("amp_release"), Slider.VERTICAL);
        amp_release.setLabelText("r");
        amp_release.setLayoutParams(layoutParams);
        amp_release.setRange(0, 1);
        amp_adsr.addView(amp_release);
        widgets.add(amp_release);

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
        widgets.add(osc2_type);

        Slider osc2_tune = new Slider(this, registerWidget("osc2_tune"), Slider.HORIZONTAL);
        osc2_tune.setLabelText("coarse tune");
        osc2_tune.setRange(-12, 12);
        osc2_tune.setIntegerValued(true);
        osc2_tune.setLayoutParams(layoutParams);
        osc2_section_top.addView(osc2_tune);
        widgets.add(osc2_tune);

        LinearLayout osc2_section_bottom = new LinearLayout(this);
        osc2_section_bottom.setOrientation(LinearLayout.HORIZONTAL);
        osc2_section_bottom.setLayoutParams(layoutParams);

        Slider osc2_width = new Slider(this, registerWidget("osc2_width"), Slider.HORIZONTAL);
        osc2_width.setLabelText("pulsewidth");
        osc2_width.setRange(0, 1);
        osc2_width.setLayoutParams(layoutParams);
        osc2_section_bottom.addView(osc2_width);
        widgets.add(osc2_width);

        Slider osc2_detune = new Slider(this, registerWidget("osc2_detune"), Slider.HORIZONTAL);
        osc2_detune.setLabelText("detune");
        osc2_detune.setRange(-20, 20);
        osc2_detune.setLayoutParams(layoutParams);
        osc2_section_bottom.addView(osc2_detune);
        widgets.add(osc2_detune);

        Slider osc2_level = new Slider(this, registerWidget("osc2_level"), Slider.VERTICAL);
        osc2_level.setLabelText("level");
        osc2_level.setRange(1, 10);
        osc2_level.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4f));
        widgets.add(osc2_level);

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
                                                            new String[] {"Sin", "Tri", "Rect", "Rand"});
        lfo1_type.setLayoutParams(layoutParams);
        lfo1.addView(lfo1_type);
        widgets.add(lfo1_type);

        LinearLayout lfo1_freq_depth = new LinearLayout(this);
        lfo1_freq_depth.setOrientation(LinearLayout.VERTICAL);
        lfo1_freq_depth.setLayoutParams(layoutParams);

        final Slider lfo1_freq = new Slider(this, registerWidget("lfo1_freq"), Slider.HORIZONTAL);
        lfo1_freq.setLabelText("freq");
        lfo1_freq.setRange(0, 5);
        lfo1_freq.setLayoutParams(layoutParams);
        lfo1_freq_depth.addView(lfo1_freq);
        widgets.add(lfo1_freq);

        final Slider lfo1_depth = new Slider(this, registerWidget("lfo1_depth"), Slider.HORIZONTAL);
        lfo1_depth.setLabelText("depth");
        lfo1_depth.setRange(0, 200);
        lfo1_depth.setLayoutParams(layoutParams);
        lfo1_freq_depth.addView(lfo1_depth);
        widgets.add(lfo1_depth);

        lfo1.addView(lfo1_freq_depth);

        final ScrollBox lfo1_target = new ScrollBox(this, 
                                                            registerWidget("lfo1_target"),
                                                            new String[] {"osc1 freq", 
                                                                          "osc1 width", 
                                                                          "osc2 freq", 
                                                                          "osc2 width", 
                                                                          "d. time",
                                                                          "feedback",
                                                                          "cutoff",
                                                                          "resonance"
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
                    sendControlMessage(lfo1_depth.getId(), lfo1_depth.getValue());
                }
            });

        lfo1_target.setLayoutParams(layoutParams);
        lfo1_target.setFillRGB(150,150,150);
        lfo1.addView(lfo1_target);
        widgets.add(lfo1_target);
        lfo_section.addView(lfo1);

        LinearLayout lfo2 = new LinearLayout(this);
        lfo2.setOrientation(LinearLayout.HORIZONTAL);
        lfo2.setLayoutParams(layoutParams);

        ChoiceButtonGroup lfo2_type = new ChoiceButtonGroup(this, 
                                                            registerWidget("lfo2_type"),
                                                            new String[] {"Sin", "Tri", "Rect", "Rand"});
        lfo2_type.setLayoutParams(layoutParams);
        lfo2.addView(lfo2_type);
        widgets.add(lfo2_type);

        LinearLayout lfo2_freq_depth = new LinearLayout(this);
        lfo2_freq_depth.setOrientation(LinearLayout.VERTICAL);
        lfo2_freq_depth.setLayoutParams(layoutParams);

        Slider lfo2_freq = new Slider(this, registerWidget("lfo2_freq"), Slider.HORIZONTAL);
        lfo2_freq.setLabelText("freq");
        lfo2_freq.setRange(0, 5);
        lfo2_freq.setLayoutParams(layoutParams);
        lfo2_freq_depth.addView(lfo2_freq);
        widgets.add(lfo2_freq);

        final Slider lfo2_depth = new Slider(this, registerWidget("lfo2_depth"), Slider.HORIZONTAL);
        lfo2_depth.setLabelText("depth");
        lfo2_depth.setRange(0, 200);
        lfo2_depth.setLayoutParams(layoutParams);
        lfo2_freq_depth.addView(lfo2_depth);
        widgets.add(lfo2_depth);

        lfo2.addView(lfo2_freq_depth);

        final ScrollBox lfo2_target = new ScrollBox(this, 
                                                            registerWidget("lfo2_target"),
                                                            new String[] {"osc1 freq", 
                                                                          "osc1 width", 
                                                                          "osc2 freq", 
                                                                          "osc2 width", 
                                                                          "d. time",
                                                                          "feedback",
                                                                          "cutoff",
                                                                          "resonance"
                                              });

        lfo2_target.setOnChangeRunnable(new Runnable() {
                public void run() {
                    float[] range = lfoDepthRanges[lfo2_target.getSelectedValue()];
                    lfo2_depth.setRange(range[0], range[1]);
                    sendControlMessage(lfo2_depth.getId(), lfo2_depth.getValue());
                }
            });
        lfo2_target.setLayoutParams(layoutParams);
        widgets.add(lfo2_target);
        lfo2.addView(lfo2_target);

        lfo_section.addView(lfo2);

        row2.addView(lfo_section);

        LinearLayout filter_adsr = new LinearLayout(this);
        filter_adsr.setOrientation(LinearLayout.HORIZONTAL);
        filter_adsr.setLayoutParams(layoutParams);

        Slider filter_attack = new Slider(this, registerWidget("filter_attack"), Slider.VERTICAL);
        filter_attack.setLabelText("a");
        filter_attack.setRange(0, 1);
        filter_attack.setLayoutParams(layoutParams);
        filter_adsr.addView(filter_attack);
        widgets.add(filter_attack);

        Slider filter_decay = new Slider(this, registerWidget("filter_decay"), Slider.VERTICAL);
        filter_decay.setLabelText("d");
        filter_decay.setLayoutParams(layoutParams);
        filter_decay.setRange(0, 1);
        filter_adsr.addView(filter_decay);
        widgets.add(filter_decay);

        Slider filter_sustain = new Slider(this, registerWidget("filter_sustain"), Slider.VERTICAL);
        filter_sustain.setLabelText("s");
        filter_sustain.setLayoutParams(layoutParams);
        filter_sustain.setRange(0, 1);
        filter_adsr.addView(filter_sustain);
        widgets.add(filter_sustain);

        Slider filter_release = new Slider(this, registerWidget("filter_release"), Slider.VERTICAL);
        filter_release.setLabelText("r");
        filter_release.setLayoutParams(layoutParams);
        filter_release.setRange(0, 1);
        filter_adsr.addView(filter_release);
        widgets.add(filter_release);

        Slider filter_env_amount = new Slider(this, registerWidget("filter_env_amount"), Slider.VERTICAL);
        filter_env_amount.setLabelText("amt");
        filter_env_amount.setLayoutParams(layoutParams);
        filter_env_amount.setRange(-1, 1);
        filter_adsr.addView(filter_env_amount);
        widgets.add(filter_env_amount);

        row2.addView(filter_adsr);
        lLayout.addView(row2);

        LinearLayout steps = new LinearLayout(this);
        steps.setOrientation(LinearLayout.HORIZONTAL);
        steps.setLayoutParams(layoutParams);
        steps.setBackgroundColor(Color.BLACK);

        createStepButtons(steps);
        
        lLayout.addView(steps);

        LinearLayout aux = new LinearLayout(this);
        aux.setOrientation(LinearLayout.HORIZONTAL);
        aux.setLayoutParams(layoutParams);

        LinearLayout sequenceControl = new LinearLayout(this);
        sequenceControl.setOrientation(LinearLayout.HORIZONTAL);
        sequenceControl.setLayoutParams(layoutParams);

        LinearLayout playReverse = new LinearLayout(this);
        playReverse.setOrientation(LinearLayout.VERTICAL);
        playReverse.setLayoutParams(layoutParams);

        PlayButton playButton = new PlayButton(this, 1000);
        playButton.setLabelText("play");
        playButton.setLayoutParams(layoutParams);
        playReverse.addView(playButton);

        ReverseButton reverseButton = new ReverseButton(this, 1001);
        reverseButton.setLabelText("reverse");
        reverseButton.setLayoutParams(layoutParams);
        playReverse.addView(reverseButton);

        sequenceControl.addView(playReverse);

        TempoSlider tempoSlider = new TempoSlider(this, 1002, Slider.VERTICAL);
        tempoSlider.setLabelText("tempo");
        tempoSlider.setRange(1, 500);
        tempoSlider.setLayoutParams(layoutParams);
        sequenceControl.addView(tempoSlider);

        LinearLayout persistance = new LinearLayout(this);
        persistance.setOrientation(LinearLayout.VERTICAL);
        persistance.setLayoutParams(layoutParams);



        String[] patternLabels = new String[127];
        for(int i = 0; i < patternLabels.length; i++) {
            patternLabels[i] = "pattern " + (Integer) (i + 1);
        }

        final ScrollBox patternSelection = new ScrollBox(this, registerWidget("do_nothing"), patternLabels);

        patternSelection.setLayoutParams(layoutParams);
        persistance.addView(patternSelection);

        LinearLayout patternPersistance = new LinearLayout(this);
        patternPersistance.setOrientation(LinearLayout.HORIZONTAL);
        patternPersistance.setLayoutParams(layoutParams);

        final MomentaryButton patternLoad = new MomentaryButton(this, 1234, false);
        patternLoad.setLabelText("load");
        patternLoad.setLayoutParams(layoutParams);
        patternPersistance.addView(patternLoad);
        patternLoad.setActionRunnable(new Runnable() {
                public void run() {
                    try {
                        sequencePersister.setFileName("pattern" + patternSelection.getSelectedValue());
                        sequencePersister.load();
                        sequence.loadFromJSON(sequencePersister.getJSONObject());
                        for(int i = 0; i < sequence.getLength(); i++) {
                            stepButtons.get(i).setLabelText(sequence.getNote(i).getMidiNumber().toString());
                            stepButtons.get(i).setSelected(sequence.getEnabled(i));
                        }
                        for(StepButton button : stepButtons) {
                            button.invalidate();
                        }
                        patternLoad.setSelected(false);
                        patternLoad.invalidate();
                    }
                    catch(FileNotFoundException e) {
                        Log.d("Kosmische", "Could not find file: " + e);
                    }
                    catch(JSONException e) {
                        Log.d("Kosmische", "Could not load pattern json: " + e);
                    }
                    catch(IOException e) {
                        Log.d("Kosmische", "Could not load file: " + e);                        
                    }
                }
            });

        final MomentaryButton patternSave = new MomentaryButton(this, 1235, false);
        patternSave.setLabelText("save");
        patternSave.setLayoutParams(layoutParams);
        patternSave.setActionRunnable(new Runnable() {
                public void run() {
                    try {
                        String fileName = "pattern" + patternSelection.getSelectedValue();
                        sequencePersister.setJSONObject(sequence.asJSONObject(fileName));
                        Log.d("Kosmische", sequencePersister.getJSONString());           
                        sequencePersister.setFileName(fileName);
                        sequencePersister.persist();
                        patternSave.setSelected(false);
                        patternSave.invalidate();
                    }
                    catch(JSONException e) {
                        Log.d("Kosmische", "Could not save pattern json: " + e);                        
                    }
                    catch(IOException e) {
                        Log.d("Kosmische", "Could not save pattern file: " + e);                        
                    }
                }
            });

        patternPersistance.addView(patternSave);

        persistance.addView(patternPersistance);

        final ScrollBox patchSelection = new ScrollBox(this, 
                                                            registerWidget("nothing"),
                                                            new String[] {"patch 1"
                                                       });
        patchSelection.setLayoutParams(layoutParams);
        persistance.addView(patchSelection);

        LinearLayout patchPersistance = new LinearLayout(this);
        patchPersistance.setOrientation(LinearLayout.HORIZONTAL);
        patchPersistance.setLayoutParams(layoutParams);

        final MomentaryButton patchLoad = new MomentaryButton(this, 1236, false);
        patchLoad.setLabelText("load");
        patchLoad.setLayoutParams(layoutParams);
        patternLoad.setActionRunnable(new Runnable() {
                public void run() {
                    try {
                        patchPersister.setFileName("patch" + patchSelection.getSelectedValue());
                        patchPersister.load();
                        patch.loadFromJSON(patchPersister.getJSONObject());
                        loadPatch();
                        patchLoad.setSelected(false);
                        patchLoad.invalidate();
                    }
                    catch(FileNotFoundException e) {
                        Log.d("Kosmische", "Could not find file: " + e);
                    }
                    catch(JSONException e) {
                        Log.d("Kosmische", "Could not load pattern json: " + e);
                    }
                    catch(IOException e) {
                        Log.d("Kosmische", "Could not load file: " + e);                        
                    }
                }
            });
        patchPersistance.addView(patchLoad);

        final MomentaryButton patchSave = new MomentaryButton(this, 1237, false);
        patchSave.setLabelText("save");
        patchSave.setLayoutParams(layoutParams);
        patternSave.setActionRunnable(new Runnable() {
                public void run() {
                    try {
                        String fileName = "patch" + patchSelection.getSelectedValue();
                        patchPersister.setJSONObject(patch.asJSONObject(fileName));
                        patchPersister.setFileName(fileName);
                        patchPersister.persist();
                        patchSave.setSelected(false);
                        patchSave.invalidate();
                    }
                    catch(JSONException e) {
                        Log.d("Kosmische", "Could not save pattern json: " + e);                        
                    }
                    catch(IOException e) {
                        Log.d("Kosmische", "Could not save pattern file: " + e);                        
                    }
                }
            });

        patchPersistance.addView(patchSave);

        persistance.addView(patchPersistance);
        sequenceControl.addView(persistance);
        
        aux.addView(sequenceControl);

        LinearLayout effects = new LinearLayout(this);
        effects.setOrientation(LinearLayout.HORIZONTAL);
        effects.setLayoutParams(layoutParams);

        Slider delayMix = new Slider(this, registerWidget("delay_mix"), Slider.VERTICAL);
        delayMix.setLabelText("delay mix");
        delayMix.setLayoutParams(layoutParams);
        delayMix.setRange(-1, 1);
        effects.addView(delayMix);
        widgets.add(delayMix);

        Slider delayTime = new Slider(this, registerWidget("delaytime"), Slider.VERTICAL);
        delayTime.setLabelText("delay time");
        delayTime.setLayoutParams(layoutParams);
        delayTime.setRange(0, 2);
        effects.addView(delayTime);
        widgets.add(delayTime);

        Slider decayTime = new Slider(this, registerWidget("decaytime"), Slider.VERTICAL);
        decayTime.setLabelText("feedback");
        decayTime.setLayoutParams(layoutParams);
        decayTime.setRange(0, 10);
        effects.addView(decayTime);
        widgets.add(decayTime);

        Slider reverbMix = new Slider(this, registerWidget("reverb_mix"), Slider.VERTICAL);
        reverbMix.setLabelText("rev. mix");
        reverbMix.setLayoutParams(layoutParams);
        reverbMix.setRange(0, 1);
        effects.addView(reverbMix);
        widgets.add(reverbMix);

        Slider roomSize = new Slider(this, registerWidget("reverb_room_size"), Slider.VERTICAL);
        roomSize.setLabelText("room size");
        roomSize.setLayoutParams(layoutParams);
        roomSize.setRange(0, 1);
        effects.addView(roomSize);
        widgets.add(roomSize);

        Slider reverbDamp = new Slider(this, registerWidget("reverb_damp"), Slider.VERTICAL);
        reverbDamp.setLabelText("damp");
        reverbDamp.setLayoutParams(layoutParams);
        reverbDamp.setRange(0, 1);
        effects.addView(reverbDamp);
        widgets.add(reverbDamp);

        aux.addView(effects);

        lLayout.addView(aux);
        setContentView(lLayout);

        bindService(new Intent("supercollider.START_SERVICE"), conn, BIND_AUTO_CREATE);

        loadPatch();
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
                    Thread.sleep((long) (60000 / bpm / 2.0));
                    sendControlMessage("trigger", 0);
                    Thread.sleep((long) (60000 / bpm / 2.0 - 1));
                    
                    if(sequenceReversed) {
                        currentStep--;
                    }
                    else {
                        currentStep++;
                    }

                    if(currentStep < 0) {
                        currentStep = sequence.getLength() - 1;
                    }

                    if(currentStep >= sequence.getLength()) {
                        currentStep = 0;
                    }

                    // get the note out of the sequence
                    if(stepButtons.get(currentStep).isSelected()) {
                        sendControlMessage("note", sequence.getNote(currentStep).getMidiNumber());
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

    public void setBPM(float bpm) {
        this.bpm = bpm;
    }

    public void setSequenceReversed(boolean sequenceReversed) {
        this.sequenceReversed = sequenceReversed;
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
