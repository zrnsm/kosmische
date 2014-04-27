package net.sf.supercollider.android;
import java.util.ArrayList;
import java.lang.Math;

public class Sequence {
    private ArrayList<Note> notes;
    private int length;

    public Sequence(int length) {
        this.length = length;
        notes = new ArrayList<Note>(length);
        randomPentatonicSequence(50);
    }

    private void randomPentatonicSequence(int root) {
        int[] degrees = {0, 3, 5, 7, 10};
        for(int i = 0; i < length; i++) {
            notes.add(new Note(root + degrees[(int) (Math.random() * 5)]));
        }
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
