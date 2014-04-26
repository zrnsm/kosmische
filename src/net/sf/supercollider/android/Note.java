package net.sf.supercollider.android;

public class Note {
    private int midiNumber;
    public Note(int midiNumber) {
        this.midiNumber = midiNumber;
    }

    public int getMidiNumber() {
        return midiNumber;
    }
}
