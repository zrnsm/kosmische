package net.sf.supercollider.android;
import java.util.ArrayList;
import java.lang.Math;
import org.json.JSONObject;
import org.json.JSONException;

public class Sequence {
    private ArrayList<Note> notes;
    private ArrayList<Boolean> isEnabled;
    private int length;

    public Sequence(int length) {
        this.length = length;
        notes = new ArrayList<Note>(length);
        isEnabled = new ArrayList<Boolean>(length);
        randomPentatonicSequence(50);
    }

    private void randomPentatonicSequence(int root) {
        int[] degrees = {0, 3, 5, 7, 10};
        for(int i = 0; i < length; i++) {
            notes.add(new Note(root + degrees[(int) (Math.random() * 5)]));
            isEnabled.add(true);
        }
    }

    public int getLength() {
        return length;
    }

    public void setNote(int i, Note note) {
        notes.set(i, note);
    }

    public Note getNote(int i) {
        return notes.get(i);
    }

    public void setEnabled(int i, boolean enabled) {
        isEnabled.set(i, enabled);
    }

    public Boolean getEnabled(int i) {
        return isEnabled.get(i);
    }

    public JSONObject asJSONObject(String sequenceName) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", sequenceName);
        for(Integer i = 0; i < length; i++) {
            json.put("note" + i, notes.get(i).getMidiNumber());
            json.put("enabled" + i, isEnabled.get(i));
        }
        return json;
    }

    public void loadFromJSON(JSONObject json) throws JSONException {
        for(Integer i = 0; i < length; i++) {
            notes.set(i, new Note((Integer) json.get("note" + i)));
            isEnabled.set(i, (Boolean) json.get("enabled" + i));
        }
    }
}
