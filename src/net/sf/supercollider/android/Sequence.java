package net.sf.supercollider.android;
import java.util.ArrayList;

public class Sequence {
    private ArrayList<Note> notes;
    private int length;

    public Sequence(int length) {
        this.length = length;
        notes = new ArrayList<Note>(length);
    }

    public int getLength() {
        return length;
    }

    public void set(int i, Note note) {
        notes.set(i, note);
    }

    public Note get(int i) {
        return notes.get(i);
    }
}
