package net.sf.supercollider.android;
import java.util.ArrayList;
import java.lang.Math;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;

public class Patch {
    private JSONObject patchValues;

    public Patch() {
        patchValues = new JSONObject();
        setToDefault();
    }

    public JSONObject getPatchValues() {
        return patchValues;
    }

    public void setValue(String name, float value) {
        try {        
            getPatchValues().put(name, value);
        }
        catch(JSONException e) {
            Log.d("Kosmische", "Failed to update patch parameter value " + e);
        }
    }

    public void setToDefault() {
        try {
            patchValues.put("osc1_level", 0.5f);
            patchValues.put("osc1_type", 0f);
            patchValues.put("osc1_width", 0.5f);
            patchValues.put("osc1_detune", 0.5f);
            patchValues.put("osc1_tune", 0.5f);

            patchValues.put("osc2_level", 0.5f);
            patchValues.put("osc2_type", 0f);
            patchValues.put("osc2_width", 0.5f);
            patchValues.put("osc2_detune", 0.5f);
            patchValues.put("osc2_tune", 0.5f);

            patchValues.put("lfo1_type", 0f);
            patchValues.put("lfo1_freq", 0f);
            patchValues.put("lfo1_depth", 0f);
            patchValues.put("lfo1_target", 0f);

            patchValues.put("lfo2_type", 0f);
            patchValues.put("lfo2_freq", 0f);
            patchValues.put("lfo2_depth", 0f);
            patchValues.put("lfo2_target", 0f);

            patchValues.put("amp_attack", 0f);
            patchValues.put("amp_decay", 0.5f);
            patchValues.put("amp_sustain", 0.25f);
            patchValues.put("amp_release", 0.25f);

            patchValues.put("cutoff", 0.5f);
            patchValues.put("resonance", 0f);

            patchValues.put("filter_attack", 0f);
            patchValues.put("filter_decay", 0.5f);
            patchValues.put("filter_sustain", 0.25f);
            patchValues.put("filter_release", 0.25f);

            patchValues.put("filter_env_amount", 0f);

            patchValues.put("delaytime", 0.5f);
            patchValues.put("decaytime", 0.5f);
            patchValues.put("reverb_mix", 0f);
            patchValues.put("delay_mix", -1f);
            patchValues.put("reverb_room_size", 0.5f);
            patchValues.put("reverb_damp", 0.5f);
        }
        catch(JSONException e) {
            Log.d("Kosmische", "could not initialize default patch " + e);
        }
    }

    public void setToRandom() {
        try {
            patchValues.put("osc1_level", Math.random());
            patchValues.put("osc1_type", (int) (Math.random() * 4));
            patchValues.put("osc1_width", Math.random());
            patchValues.put("osc1_detune", Math.random());
            patchValues.put("osc1_tune", Math.random());

            patchValues.put("osc2_level", Math.random());
            patchValues.put("osc2_type", 0f);
            patchValues.put("osc2_width", Math.random());
            patchValues.put("osc2_detune", Math.random());
            patchValues.put("osc2_tune", Math.random());

            patchValues.put("lfo1_type", (int) (Math.random() * 4));
            patchValues.put("lfo1_freq", Math.random());
            patchValues.put("lfo1_depth", Math.random());
            patchValues.put("lfo1_target", (int) (Math.random() * 8));

            patchValues.put("lfo2_type", (int) (Math.random() * 4));
            patchValues.put("lfo2_freq", Math.random());
            patchValues.put("lfo2_depth", Math.random());
            patchValues.put("lfo2_target", (int) (Math.random() * 8));

            patchValues.put("amp_attack", Math.random());
            patchValues.put("amp_decay", Math.random());
            patchValues.put("amp_sustain", Math.random());
            patchValues.put("amp_release", Math.random());

            patchValues.put("cutoff", Math.random());
            patchValues.put("resonance", Math.random());

            patchValues.put("filter_attack", Math.random());
            patchValues.put("filter_decay", Math.random());
            patchValues.put("filter_sustain", Math.random());
            patchValues.put("filter_release", Math.random());

            patchValues.put("filter_env_amount", Math.random());

            patchValues.put("delaytime", Math.random());
            patchValues.put("decaytime", Math.random());
            patchValues.put("reverb_mix", Math.random());
            patchValues.put("delay_mix", Math.random());
            patchValues.put("reverb_room_size", Math.random());
            patchValues.put("reverb_damp", Math.random());
        }
        catch(JSONException e) {
            Log.d("Kosmische", "could not generate random patch " + e);
        }
    }

    public JSONObject asJSONObject(String sequenceName) throws JSONException {
        patchValues.put("name", sequenceName);
        return patchValues;
    }

    public void loadFromJSON(JSONObject json) throws JSONException {
        patchValues = json;
    }
}
