package amidifx.models;

public class MidiSong {

    private int songType;       // Type 0 = Default for Modules, 1 = Normal Songs
    private String songTitle;   // Song Title
    private String presetFile;  // Song Preset file
    private String midiFile;    // Song MIDI file
    private int trkBass;       // Bass MIDI Channel
    private int trkLower;      // Lower MIDI Channel
    private int trkUpper;      // Upper MIDI Channel
    private int moduleIdx;      // MIDI Module
    private String timeSig;     // Time Signature

    public MidiSong () {
    }

    public MidiSong(int songType, String songTitle, String presetFile, String midiFile, int trkBass, int trkLower, int trkUpper, int moduleIdx, String timeSig) {
        this.songType = songType;
        this.songTitle = songTitle;
        this.presetFile = presetFile;
        this.midiFile = midiFile;
        this.trkBass = trkBass;
        this.trkLower = trkLower;
        this.trkUpper = trkUpper;
        this.moduleIdx = moduleIdx;
        this.timeSig = timeSig;
    }

    public int getSongType() {
        return this.songType;
    }
    public void setSongType(int songIdx) {
        this.songType = songType;
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

    public int getTrackBass() {
        return trkBass;
    }
    public void setTrackBass(int trkBass) {
        this.trkBass = trkBass;
    }

    public int getTrackLower() {
        return trkLower;
    }
    public void setTrackLower(int trkLower) {
        this.trkLower = trkLower;
    }

    public int getTrackUpper() {
        return trkUpper;
    }
    public void setTrackUpper(int trkUpper) {
        this.trkUpper = trkUpper;
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
        return "Song Listing [songType=" + this.songType + ", songTitle=" + this.songTitle
                + ", presetFile=" + this.presetFile + ", midiFile=" + this.midiFile
                + ", chanBass=" + this.getTrackBass() + ", chanLower=" + this.getTrackLower() + ", chanUpper=" + this.getTrackUpper()
                + ", timeSig=" + this.getTimeSig() + ", module=" + moduleIdx
                + "]";
    }
}
