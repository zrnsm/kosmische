package net.sf.supercollider.android;
import java.util.ArrayList;
import java.lang.Math;
import org.json.JSONObject;
import org.json.JSONException;

public class Sequence {
    private ArrayList<Note> notes;
    private ArrayList<Boolean> isEnabled;
    private int length;

    public static enum Scale {
        MINOR_PENTATONIC,
        MAJOR_PENTATONIC,
        MAJOR,
        MELODIC_MINOR,
        HARMONIC_MINOR,
        ARABIAN,
        BEBOP,
        BYZANTINE,
        CHINESE;

        private final int value;
        private Scale() {
            this.value = 0;
        }

        private Scale(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private int[][] scaleDegrees = {
        {0, 3, 5, 7, 10},
        {0, 2, 4, 7, 9},
        {0, 2, 4, 5, 7, 9, 11},
        {0, 2, 3, 5, 7, 9, 11},
        {0, 2, 3, 5, 6, 8, 10},
        {0, 2, 4, 5, 7, 8, 9, 11},
        {0, 1, 4, 5, 7, 8, 11},
        {0, 4, 6, 7, 11}
    };

    public Sequence(int length) {
        this.length = length;
        setToRandom(Scale.MINOR_PENTATONIC);
    }

    public void setToRandom(Scale scale) {
        if(notes == null) {
            notes = new ArrayList<Note>(length);
            isEnabled = new ArrayList<Boolean>(length);
        }
        else {
            notes.clear();
            isEnabled.clear();
        }

        int[] degrees = scaleDegrees[scale.getValue()];
        int root = 60 + randomRoot() + randomRootOctave();
        for(int i = 0; i < length; i++) {
            Note note = new Note(root + randomNoteOctave() + degrees[(int) (Math.random() * degrees.length)]);
            notes.add(note);
            isEnabled.add((Math.random() > 0.5) ? true : false);
        }
    }

    private int randomRoot() {
        return (int) (Math.random() * 12);
    }

    private int randomNoteOctave() {
        int[] octaveOffsets = {-12, 0, 12};
        return octaveOffsets[(int) (Math.random() * octaveOffsets.length)];
    }

    private int randomRootOctave() {
        int[] octaveOffsets = {-24, -12, 0, 12, 24};
        return octaveOffsets[(int) (Math.random() * octaveOffsets.length)];
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
