package net.sf.supercollider.android;

public class Note {
    private int midiNumber;
    public Note(int midiNumber) {
        this.midiNumber = midiNumber;
    }

    public Integer getMidiNumber() {
        return midiNumber;
    }

    public void setMidiNumber(int midiNumber) {
        this.midiNumber = midiNumber;
    }

    public void increment() {
        midiNumber++;
        if(midiNumber > 127) {
            midiNumber = 127;
        }
    }

    public void decrement() {
        midiNumber--;
        if(midiNumber < 0) {
            midiNumber = 0;
        }
    }
}
