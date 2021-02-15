package amidifx.models;

import amidifx.MidiSongs;
import javafx.scene.Scene;

public class SharedStatus {

    private String SongTitle = "Organ";          // Current Song Details
    private String PresetFile = "default.csv";
    private String MidiFile = "None";
    private String StatusText = "Ready";         // Latest Status Bar text

    private String performgm = "midigm.prf";
    private String performdb = "deebach.prf";
    private String performin = "integra.prf";

    //private String selindevice = "2- Seaboard RISE 49";
    //private String seloutdevice = "Deebach-Blackbox";
    private String selindevice = "default";
    private String seloutdevice = "default";

    // Hardcoded keyboard channels for now. Note channels are coded from 0 - 15!
    private static final int DRUMS = 10;
    private static final int BASSKBD = 11;
    private static final int LOWERKBD = 12;
    private static final int UPPERKBD = 14;
    private static final int SOLOKBD = 16;

    private String timeSig = "4/4";

    private String RxDevice = "None";           // Selected MIDI Device
    private String TxDevice = "None";           // Selected MIDI Device
    private int moduleidx = 1;                  // Sound Module index (defaults to Deebach, unless not found)

    private Scene sceneOrgan;
    private Scene sceneSongs;
    private Scene scenePresets;

    private MidiSongs dosongs;

    private String instruments = "";

    private boolean midirunning = false;    // Sequencer running true/false

    private boolean presetreload = true;    // Reload preset file that was changed
    private boolean songreload = true;      // Reload song file that may have changed

    private static final String CSV_DIRECTORY = "C:\\amidifx\\midifiles\\";

    // Static variable single_instance of type PlayMidi
    private static SharedStatus single_SharedStatus_Instance = null;

    // Static method to create singleton instance of PlayMidi class
    public synchronized static SharedStatus getInstance() {
        if (single_SharedStatus_Instance == null) {
            single_SharedStatus_Instance = new SharedStatus();

            System.out.println("PlayMidi: Creating instance StatusBar");
        }

        return single_SharedStatus_Instance;
    }

    // *** Make constructor private for Singleton ***
    private SharedStatus() { }

    public Scene getOrganScene() {
        System.out.println("SharedStatus: Getting Songs Scene: " + sceneOrgan.toString());
        return this.sceneOrgan;
    }

    public void setOrganScene(Scene sceneOrgan) {
        this.sceneOrgan = sceneOrgan;
        System.out.println("SharedStatus: Setting Songs Scene: " + sceneOrgan.toString());
    }

    public Scene getSongsScene() {
        System.out.println("SharedStatus: Getting Songs Scene: " + sceneSongs.toString());
        return this.sceneSongs;
    }

    public void setSongsScene(Scene sceneSongs) {
        this.sceneSongs = sceneSongs;
        System.out.println("SharedStatus: Setting Songs Scene: " + sceneSongs.toString());
    }

    public Scene getPresetsScene() {
        System.out.println("SharedStatus: Getting Songs Scene: " + scenePresets.toString());
        return this.scenePresets;
    }

    public void setPresetsScene(Scene scenePresets) {
        this.scenePresets = scenePresets;
        System.out.println("SharedStatus: Setting Songs Scene: " + scenePresets.toString());
    }

    public void setDoSongs(MidiSongs dosongs) {
        this.dosongs = dosongs;
    }

    public MidiSongs getDoSongs() {
        return this.dosongs;
    }

    public void setRxDevice(String synth) {
        this.RxDevice = RxDevice;
    }

    public String getRxDevice() {
        return this.RxDevice;
    }

    public void setTxDevice(String synth) {
        this.TxDevice = TxDevice;
    }

    public String getTxDevice() {
        return this.TxDevice;
    }

    public void setMidiFile(String midiFile) {
        this.MidiFile = midiFile;
    }

    public String getMidiFile() {
        return this.MidiFile;
    }

    public void setPresetFile(String presetFile) {
        this.PresetFile = presetFile;
    }

    public String getPresetFile() {
        return this.PresetFile;
    }

    public void setSongTitle(String songTitle) {
        this.SongTitle = songTitle;
    }

    public String getSongTitle() {
        return this.SongTitle;
    }

    public String getStatusText() {
        return this.StatusText;
    }

    public void setStatusText(String statusText) {  this.StatusText = statusText; }

    public int getModuleidx() {
        return this.moduleidx;
    }

    public void setModuleidx(int moduleidx) {

        this.moduleidx = moduleidx;
        System.out.println("Setting moduleIdx = " + this.moduleidx);
    }

    public String getInstruments() {
        return instruments;
    }

    public void setInstruments(String instruments) {
        this.instruments = instruments;
    }

    public void setTimeSig(String timeSig) {
        this.timeSig = timeSig;
    }

    public String getTimeSig() {
        return timeSig;
    }

    public boolean getPresetReload() {
        return presetreload;
    }

    public void setPresetReload(boolean presetreload) {
        this.presetreload = presetreload;
    }

    public boolean getSongReload() {
        return songreload;
    }

    public void setSongReload(boolean presetreload) {
        this.songreload = songreload;
    }

    public int getUpper1CHAN() {
        return UPPERKBD;
    }

    public int getUpper2CHAN() {
        return UPPERKBD+1;
    }

    public int getUpper3CHAN() {
        return UPPERKBD+2;
    }

    public int getLower1CHAN() {
        return LOWERKBD;
    }

    public int getLower2CHAN() {
        return LOWERKBD+1;
    }

    public int getBassCHAN() {
        return BASSKBD;
    }

    public int getSoloCHAN() {
        return SOLOKBD;
    }

    public int getDrumCHAN() {
        return DRUMS;
    }

    public String getPerformFile() {
        if (moduleidx == 0)
            return performdb;
        //else if (moduleidx == 1)
        //    return performin;
        else
            return performgm;
    }

    public void setSelInDevice(String selindevice) {
        this.selindevice = selindevice;
    }

    public String getSelInDevice() {
        return this.selindevice;
    }

    public void setSelOutDevice(String seloutdevice) {
        this.seloutdevice = seloutdevice;
    }

    public String getSelOutDevice() {
        return this.seloutdevice;
    }

    public void isMidirunning(boolean midirunning) {
        this.midirunning = midirunning;
    }

    public boolean isMidirunning() {
        return this.midirunning;
    }
}
