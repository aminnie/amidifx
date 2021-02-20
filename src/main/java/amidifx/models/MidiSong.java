package amidifx.models;

public class MidiSong {

    private int songIdx;        // Index
    private String songTitle;   // Song Title
    private String presetFile;  // Song Preset file
    private String midiFile;    // Song MIDI file
    private int chanBass;       // Bass MIDI Channel
    private int chanLower;      // Lower MIDI Channel
    private int chanUpper;      // Upper MIDI Channel
    private int moduleIdx;      // MIDI Module
    private String timeSig;     // Time Signature

    public MidiSong () {
    }

    public MidiSong(int songIdx, String songTitle, String presetFile, String midiFile, int chanBass, int chanLower, int chanUpper, int moduleIdx, String timeSig) {
        this.songIdx = songIdx;
        this.songTitle = songTitle;
        this.presetFile = presetFile;
        this.midiFile = midiFile;
        this.chanBass = chanBass;
        this.chanLower = chanLower;
        this.chanUpper = chanUpper;
        this.moduleIdx = moduleIdx;
        this.timeSig = timeSig;
    }

    public int getSongId() {
        return this.songIdx;
    }
    public void setSongId(int songIdx) {
        this.songIdx = songIdx;
    }

    public String getSongTitle() {
        return this.songTitle;
    }
    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getPresetFile() {
        return this.presetFile;
    }
    public void setPresetFile(String presetFile) {
        this.presetFile = presetFile;
    }

    public String getMidiFile() {
        return this.midiFile;
    }
    public void setMidiFile(String midiFile) {
        this.midiFile = midiFile;
    }

    public int getChanBass() {
        return chanBass;
    }
    public void setChanBass(int chanBass) {
        this.chanBass = chanBass;
    }

    public int getChanLower() {
        return chanLower;
    }
    public void setChanLower(int chanLower) {
        this.chanLower = chanLower;
    }

    public int getChanUpper() {
        return chanUpper;
    }
    public void setChanUpper(int chanUpper) {
        this.chanUpper = chanUpper;
    }

    public String getTimeSig() {
        return timeSig;
    }
    public void setTimeSig(String timeSig) {
        this.timeSig = timeSig;
    }

    public int getModuleIdx() {
        return moduleIdx;
    }
    public void setModuleIdx(int moduleIdx) {
        this.moduleIdx = moduleIdx;
    }

    @Override
    public String toString() {
        return "Song Listing [songIdx=" + this.songIdx + ", songTitle=" + this.songTitle
                + ", presetFile=" + this.presetFile + ", midiFile=" + this.midiFile
                + ", chanBass=" + this.getChanBass() + ", chanLower=" + this.getChanLower() + ", chanUpper=" + this.getChanUpper()
                + ", timeSig=" + this.getTimeSig() + ", module=" + moduleIdx
                + "]";
    }
}
