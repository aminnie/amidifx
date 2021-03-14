package amidifx.models;

import amidifx.MidiSongs;
import amidifx.utils.AppConfig;
import javafx.scene.Scene;

import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

public class SharedStatus {

    private String SongTitle = "Organ";          // Current Song Details
    private String PresetFile = "default.csv";
    private String MidiFile = "None";
    private String StatusText = "Ready";         // Latest Status Bar text

    private String songlist = "songs.sng";

    private String performgm = "midigm.prf";
    private String performdb = "deebach.prf";
    private String performin = "integra.prf";

    private String modulename0 = "MIDI GM";
    private String modulename1 = "Deebach BlackBox";
    private String modulename2 = "Roland Integra7";

    private String MID_DIRECTORY = "C:/amidifx/midifiles/";
    private String CFG_DIRECTORY = "C:/amidifx/config/";

    private String selindevice = "default";
    private String seloutdevice = "default";

    // Hardcoded keyboard channels for now. Note channels are coded from 0 - 15!
    private static final int DRUMS = 10;
    private static final int BASSKBD = 11;
    private static final int LOWERKBD = 12;
    private static final int UPPERKBD = 14;
    private static final int SOLOKBD = 16;

    int expchannel = 0xFF; // Expression Channel Number if used. Value of 0 is not used, 0xFF if not yet set

    private boolean lower1kbdlayerenabled = true;
    private boolean upper1kbdlayerenabled = true;
    private boolean upper2kbdlayerenabled = true;

    private String timeSig = "4/4";

    private Receiver RxDevice;          // Selected MIDI Device
    private Transmitter TxDevice;       // Selected MIDI Device
    private Sequencer SeqDevice;
    private int moduleidx = 0;          // Sound Module index (defaults to Deebach, unless not found)

    private Scene scenePerform;
    private Scene sceneSongs;
    private Scene scenePresets;
    private Scene sceneHome;

    private MidiSongs dosongs;

    private String instruments = "";

    private boolean midirunning = false;    // Sequencer running true/false

    private boolean presetreload = true;    // Reload preset file that was changed
    private boolean songreload = true;      // Reload song file that may have changed

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

    public Scene getPerformScene() {
        System.out.println("SharedStatus: Getting Perform Scene: " + scenePerform.toString());
        return this.scenePerform;
    }

    public void setPerformScene(Scene sceneOrgan) {
        this.scenePerform = sceneOrgan;
        System.out.println("SharedStatus: Setting Perform Scene: " + scenePerform.toString());
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
        System.out.println("SharedStatus: Getting Presets Scene: " + scenePresets.toString());
        return this.scenePresets;
    }

    public void setPresetsScene(Scene scenePresets) {
        this.scenePresets = scenePresets;
        System.out.println("SharedStatus: Setting Presets Scene: " + scenePresets.toString());
    }

    public void setHomeScene(Scene sceneHome) {
        this.sceneHome = sceneHome;
        System.out.println("SharedStatus: Setting Home Scene: " + sceneHome.toString());
    }

    public Scene getHomeScene() {
        System.out.println("SharedStatus: Getting Home Scene: " + sceneHome.toString());
        return this.sceneHome;
    }

    public void setDoSongs(MidiSongs dosongs) {
        this.dosongs = dosongs;
    }

    public MidiSongs getDoSongs() {
        return this.dosongs;
    }

    public void setRxDevice(Receiver RxDevice) {
        this.RxDevice = RxDevice;
    }

    public Receiver getRxDevice() {
        return this.RxDevice;
    }

    public void setTxDevice(Transmitter TxDevice) {
        this.TxDevice = TxDevice;
    }

    public Transmitter getTxDevice() {
        return this.TxDevice;
    }

    public void setSeqDevice(Sequencer SeqDevice) {
        this.SeqDevice = SeqDevice;
    }

    public Sequencer getSeqDevice() {
        return this.SeqDevice;
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

    // Return Deebach MaxPlus (1), Roland Integra7, or MIDI GM (0)
    public String getModuleName(int moduleidx) {

        if (moduleidx == 1)
            return this.modulename1;

        // Return Roland Integra7
        if (moduleidx == 2)
            return this.modulename2;

        return modulename0;
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

    public void setLower1KbdLayerEnabled(boolean lower1kbdlayerenabled) {
        this.lower1kbdlayerenabled = lower1kbdlayerenabled;
    }

    public boolean getlower1Kbdlayerenabled() {
        return lower1kbdlayerenabled;
    }

    public void setUpper1KbdLayerEnabled(boolean upper1kbdlayerenabled) {
        this.upper1kbdlayerenabled = upper1kbdlayerenabled;
    }

    public boolean getUpper1KbdLayerEnabled() {
        return upper1kbdlayerenabled;
    }

    public void setUpper2KbdLayerEnabled(boolean upper2kbdlayerenabled) {
        this.upper2kbdlayerenabled = upper2kbdlayerenabled;
    }

    public boolean getupper2Kbdlayerenabled() {
        return upper2kbdlayerenabled;
    }

    public String getPerformFile() {
        if (moduleidx == 0) {
            System.out.println("Button File: " + performgm);
            return performgm;
        }
        else {
            System.out.println("Button File: " + performdb);
            return performdb;
        }
    }

    public int setExpressionCHAN(int expchannel) {
        return this.expchannel;
    }

    public int getExpressionCHAN() {

        // If no set yet, read from AppConfig
        if (expchannel == 0xFF) {
            AppConfig appconfig = AppConfig.getInstance();
            this.expchannel = appconfig.getExpressionChannel();
        }

        return expchannel;
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

    public void setSongList(String songlist) {
        this.songlist = songlist;
    }

    public String getSongList() {
        return this.songlist;
    }

    public void setMIDDirectory(String MID_DIRECTORY) {
        this.MID_DIRECTORY = MID_DIRECTORY;
    }

    public String getMIDDirectory() {
        return MID_DIRECTORY;
    }

    public void setCFGDirectory(String CFG_DIRECTORY) {
        this.CFG_DIRECTORY = CFG_DIRECTORY;
    }

    public String getCFGDirectory() {
        return CFG_DIRECTORY;
    }

    public void isMidirunning(boolean midirunning) {
        this.midirunning = midirunning;
    }

    public boolean isMidirunning() {
        return this.midirunning;
    }

}
