package amidifx;

import amidifx.models.MidiInstrument;
import amidifx.models.MidiPreset;
import amidifx.models.MidiSong;
import amidifx.models.SharedStatus;
import amidifx.utils.AppConfig;
import amidifx.utils.MidiDevices;
import javafx.application.Platform;

import javax.sound.midi.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static amidifx.MidiPresets.*;

// Implementing PlayMidi as Singleton class with getInstance() method to ensure only one MIDI play at a time
public class PlayMidi {

    // Static variable single_instance of type PlayMidi
    private static PlayMidi single_PlayMidiInstance = null;

    // Create instance of Shared Status to report back to Scenes
    final SharedStatus sharedStatus = SharedStatus.getInstance();

    private byte SOLOKBD = sharedStatus.getSoloCHAN();
    private byte BASSKBD = sharedStatus.getBassCHAN();
    private byte LOWERKBD = sharedStatus.getLower1CHAN();
    private byte UPPERKBD = sharedStatus.getUpper1CHAN();
    private byte DRUMS = sharedStatus.getDrumCHAN();

    private int debugmode = sharedStatus.getDebugmode();

    private int TimeSigNum = 4;
    private int TimeSigDen = 4;

    // Track all PC and CC changes for current MIDI channels and only apply deltas to manage # of commands invoked on controller
    final List<MidiPreset> curPresetList = new ArrayList<>();

    File mfile;
    Synthesizer synthesizer;
    Sequence midiSeq;
    Sequencer sequencer;

    Receiver midircv;

    String midiFile;
    String presetFile;
    MidiPresets readpresets;

    int mode = 1;
    Boolean midirunning = false;

    long barzerotickposition = 0;
    int barstartnumber = 1;
    boolean barstarted = false;
    long seqresolution;

    int presetidx = 0;
    int newpresetidx = -1;

    boolean bupperrotorfast = false;    // Tracking for rotor fast/slow for back to on state
    boolean bupperrotoron = false;      // Tracking for rotor fast/slow for back to on state
    boolean blowerrotorfast = false;    // Tracking for rotor fast/slow for back to on state
    boolean blowerrotoron = false;      // Tracking for rotor fast/slow for back to on state

    // Static method to create singleton instance of PlayMidi class
    public synchronized static PlayMidi getInstance() {
        if (single_PlayMidiInstance == null) {
            single_PlayMidiInstance = new PlayMidi();

            //System.out.println("PlayMidi: Creating instance PlayMidi");
        }

        return single_PlayMidiInstance;
    }

    // *** Make constructor private for Singleton ***
    private PlayMidi() {
        AppConfig config = AppConfig.getInstance();

        try {
            System.out.println("PlayMidi: Retrieving Receiver device");

            midircv = MidiSystem.getReceiver();
        }
        catch (Exception ex) {
            System.err.println("PlayMidi: Error Retrieving Receiver device");
        }

    }

    // Play MIDI File
    public boolean startMidiPlay(MidiSong midiSong, MidiPresets dopresets, int playmode) throws Exception {

        midiFile = midiSong.getMidiFile();
        presetFile = midiSong.getPresetFile();

        readpresets = MidiPresets.getInstance();
        readpresets.loadMidiPresets(presetFile);

        System.out.println("*** PlayMidi: Playing Song " + midiSong.getSongTitle() + " in mode " + mode);
        System.out.println("*** PlayMidi: Using Preset " + midiSong.getPresetFile());

        // We can only have on instance running!
        if (midirunning)
            return false;

        // Check for valid modes: 1 = demo original, 2 = demo with presets, 3 = backing only with presets
        mode = playmode;
        if ((mode < 1) || (mode > 3))
            return false;

        resetcurPresetList();
        unmuteAllTracks();

        barstarted = false;
        presetidx = -1;

        // Get default sequencer.
        try {
            Receiver midircv = sharedStatus.getRxDevice();
            sequencer.getTransmitter().setReceiver(midircv);

        }
        catch(Exception ex) {
            System.err.println("PlayMidi Error: Sequencer initialization failed!");
            return false;
        }

        // Send Master Control volume SysEx
        ////sendMasterVolume();

        // Reset all MIDI Controllers as we start out
        sendAllControllersOff();

        // Prepare the TIme Signature for this Song
        setTimeSignature(midiSong);

        // Construct a Sequence object, and load it into sequencer.
        // Sets the current sequence on which the sequencer operates.
        try {
            if (!sequencer.isOpen()) {
                sequencer.open();
                sharedStatus.setSeqDevice(sequencer);

                System.out.println("PlayMidi: Sequencer opened: " + sequencer.toString());
            }

            System.out.println("PlayMidi: Starting Sequencer Play " + midiFile);

            mfile = new File(sharedStatus.getMIDDirectory() + midiFile);
            if (!mfile.exists()) {
                sharedStatus.setStatusText("Playing " + midiFile + ". File not found!");

                System.err.println("PlayMidi: FIle " + midiFile + " does not exist!");
                return false;
            }

            synthesizer = MidiSystem.getSynthesizer();
            midiSeq = MidiSystem.getSequence(mfile);
            sequencer.setSequence(midiSeq);

            // mode 1 = original, 2 = with presets, 3 = presets and backing
            if (mode == 3) {
                muteKeyboardTracks(midiSong);
            }

            // https://www.docs4dev.com/docs/en/java/java8/tutorials/sound-MIDI-seq-intro.html
            System.out.println("MidiPlay - Micros Seconds: " + sequencer.getMicrosecondLength());
            System.out.println("MidiPlay - Ticks: " + sequencer.getTickLength());
            System.out.println("MidiPlay - Tempo MPQ: " + sequencer.getTempoInMPQ());
            System.out.println("MidiPlay - Tempo BPM: " + sequencer.getTempoInBPM());
            System.out.println("MidiPlay - Tempo TempoFactor: " + sequencer.getTempoFactor());
            System.out.println("MidiPlay - Division Type: " + midiSeq.getDivisionType());

            seqresolution = midiSeq.getResolution();
            //System.out.println("MidiPlay - Resolution: " + midiSeq.getResolution());

        }
        catch (Exception ex) {
            System.err.println("PlayMidi: Sequencer Play Exception: " + ex);
            return false;
        }

        // Wait for and report Preset, Bar or End Play Cues
        sequencer.addMetaEventListener(metaMsg -> {
            // Returned Cue to change to Preset 1 - only if not in demo mode!
            // Inserted at the start of a MIDI file before the first note (and after any PC, etc. changes)
            if (metaMsg.getType() == 0x07) {
                System.out.println("### PlayMidi: MetaEvent Cue for " + midiFile);

                byte cuetext[] = metaMsg.getData();

                // Preset Cues: Formatted as P1 -> P8
                if ((cuetext[0] == 'P') && (mode > 1)) {

                    // Avoid duplicate triggers. Need to root cause why one CUE triggers multiple times
                    newpresetidx = cuetext[1] - 48 - 1;
                    if (newpresetidx == presetidx) {
                        //System.out.println("### PlayMidi: Duplicate MetaEvent Cue: Preset " + cuetext[0] + "  " + cuetext[1]);
                        return;
                    }
                    else presetidx = newpresetidx;

                    System.out.println("### PlayMidi: MetaEvent Cue Preset " + cuetext[0] + " " + cuetext[1]);

                    if ((presetidx >= 0) && (presetidx <= 7)) {
                        System.out.println("### PlayMidi: MetaEvent Presetidx " + presetidx);

                        for (int chanidx = 0; chanidx < 16; chanidx++) {
                            MidiPreset preset = readpresets.getPreset(presetidx * 16 + chanidx);
                            readpresets.applyMidiPreset(preset, chanidx);

                            //System.out.println("### Applied Channel " + chanidx + ", " + preset.toString());
                        }

                        sharedStatus.setStatusText("Preset " + (presetidx + 1) + " applied");
                    }
                }
                // Bar/Beat Count Cues: Get the current sequencer tick position to properly reset Bar/Beat counter
                // B0 = Lead in Bar, B1 = Start with no Lead in
                else if (cuetext[0] == 'B') {

                    // Only one Bar Cue allowed per Song
                    if (barstarted == true) {
                        System.out.println("### PlayMidi: Duplicate MetaEvent Cue: Bar " + cuetext[0] + "  " + cuetext[1]);
                        return;
                    }

                    System.out.println("### PlayMidi: MetaEvent Cue: Bar " + cuetext[0] + "  " + cuetext[1]);
                    barstarted = true;

                    barzerotickposition = sequencer.getTickPosition();
                    barstartnumber = (cuetext[1] - 48);

                    if ((barstartnumber < 0) || (barstartnumber > 1)) {
                        barstartnumber = 1;
                    }
                }
            }
            // End of file and play
            else if (metaMsg.getType() == 0x2F) {
                sequencer.close();

                midirunning = false;
                sharedStatus.isMidirunning(false);

                newpresetidx = -1;

                // Reset all MIDI Controllers as we start out
                sendAllControllersOff();

                sharedStatus.setStatusText("Sequencer play ended " + midiFile);
                //System.out.println("### PlayMidi: MetaEvent Play ended " + midiFile);
            }
        });

        // Starts MIDI play of data in loaded sequencer
        sequencer.start();

        // Get Instrument List for this MIDI file
        sharedStatus.setInstruments(listInstruments());

        midirunning = true;
        sharedStatus.isMidirunning(true);
        sharedStatus.setStatusText("Sequencer play started " + midiFile);
        return true;
    }

    public long getSequencerTickPosition() {

        // https://www.docs4dev.com/docs/en/java/java8/tutorials/sound-MIDI-seq-intro.html
        long tickposition = sequencer.getTickPosition();

        //System.out.println("PlayMidi: Get Tick Position " + tickposition);
        return tickposition;
    }


    // Get Sequencer Bar/Beat Counters for display in user interface
    // MIDI Cue event 'B0' can be used to ensure the counter position is at the start of the first bar
    // To do: Adjust the counter below to work for 6/8 and 3/4 time.
    // https://spencerpark.github.io/MellowD/build/docs/docco/src/main/java/cas/cs4tb3/mellowd/TimingEnvironment.html
    // http://midi.teragonaudio.com/tech/midifile/time.htm
    public String getSequencerBeat() {

        long barposition = 0;
        long beatposition = 0;

        long ticklen = sequencer.getTickLength() / (seqresolution * TimeSigNum);
        long barzerolen = barzerotickposition / (seqresolution * TimeSigNum);

        long tickposition = sequencer.getTickPosition();
        if ((tickposition > barzerotickposition) && (barzerotickposition) > 0) {
            tickposition = tickposition - barzerotickposition;

            barposition = tickposition / (seqresolution * TimeSigNum);
            beatposition = (tickposition - (barposition * (seqresolution * TimeSigNum))) / (seqresolution * 1);

            //System.out.println("PlayMidi: Bar.Beat Position " + (barposition + 1) + "." + (beatposition + 1));
            return (barposition + barstartnumber) + "." + (beatposition + 1) + " | " + (ticklen - barzerolen);
        } else {
            //System.out.println("PlayMidi: Bar.Beat Position " + barposition + "." + beatposition);
            return barposition + "." + beatposition;
        }
    }


    public boolean stopMidiPlay(String midiFile) {
        //System.out.println("PlayMidi: Stopping Sequencer play " + midiFile);

        newpresetidx = -1;

        midirunning = false;
        try {
            if (sequencer == null) {
                //System.out.println("### PlayMidi: stopMidiPlay Sequencer is null");
                return false;
            }
            if (sequencer.isRunning()) {
                //System.out.println("PlayMidi: stopMidiPlay stopping running Sequencer");
                sequencer.stop();
            }
        }
        catch (Exception ex) {
            System.err.println("### PlayMidi: Error attempting to stop sequencer play: " + ex);
            return false;
        }

        sharedStatus.setStatusText("Stopped Sequencer play " + midiFile);
        return true;
    }

    public boolean sendMidiNote(byte CHAN, byte NOTE, boolean NOTEON) {
        ShortMessage midiMsg = new ShortMessage();

        if ((CHAN < 0) || (CHAN > 15)) {
            System.err.println("PlayMidi: sendMidiNote Error CHAN: " + CHAN + " NOTE:" + NOTE);
            return false;
        }

        try {
            long timeStamp = -1;

            Receiver midircv = sharedStatus.getRxDevice();

            if (NOTEON) {
                midiMsg.setMessage(ShortMessage.NOTE_ON, CHAN, NOTE, 96);
                midircv.send(midiMsg, timeStamp);

                if (debugmode != 0)
                    System.out.println("PlayMidi: Sent MIDI Note ON, " + CHAN + ", " + NOTE);
            } else {
                midiMsg.setMessage(ShortMessage.NOTE_OFF, CHAN, NOTE, 0);
                midircv.send(midiMsg, timeStamp);

                if (debugmode != 0)
                    System.out.println("PlayMidi: Sent MIDI Note OFF, " + CHAN + ", " + NOTE);
            }
        } catch (Exception ex) {
            System.err.println("### PlayMidi Error: MIDI Note Send Exception " + midiMsg.toString());
            System.err.println(ex);
            return false;
        }

        return true;
    }

    // http://jsresources.sourceforge.net/faq_midi.html#hw_synth_as_synthesizer
    public boolean sendMidiProgramChange(int CHAN, int PC, int MSB, int LSB) {

        if ((CHAN < 0) || CHAN > 15) {
            System.err.println("PlayMidi Error: Send Midi Program Change CHAN: " + CHAN + " PC:" + (PC + 1) + " LSB:" + LSB + " MSB:" + MSB);
            return false;
        }

        if (debugmode != 0) System.out.println("PlayMidi: Sending MIDI Program Change  CHAN: " + CHAN  + " PC:" + PC + " MSB:" + MSB + " LSB:"+ LSB);

        long timeStamp = -1;
        ShortMessage midiMsg = new ShortMessage();
        try {

            midircv = sharedStatus.getRxDevice(); //MidiSystem.getReceiver();
            //System.out.println("PlayMidi: getReceiver: " + midircv.toString());

            // Proceed to apply Bank and Program changes. Do so only if not duplicate of previous
            if ((curPresetList.get(CHAN).getMSB() == MSB) &&
                    (curPresetList.get(CHAN).getLSB() == LSB) && (curPresetList.get(CHAN).getPC() == PC)) {

                // Proceed to overrride if in Panic mode
                if (sharedStatus.getPanic() == false) {
                    //System.out.println("PlayMidi: Duplicate Program Change on CHAN "  + CHAN + ", PC " + PC + " MSB:" + MSB + " LSB:" + LSB + " ignored");
                    return false;
                }
            }

            midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN, 0, MSB & 0xFF); //(int)(LSB & 0xFF));
            midircv.send(midiMsg, timeStamp);
            midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN, 32, LSB & 0xFF); //(int)(MSB & 0xFF));
            midircv.send(midiMsg, timeStamp);
            midiMsg.setMessage(ShortMessage.PROGRAM_CHANGE, CHAN, PC & 0XFF, 64);
            midircv.send(midiMsg, timeStamp);

            // Log the newly sent PC to compare against next send
            curPresetList.get(CHAN).setMSB(MSB);
            curPresetList.get(CHAN).setLSB(LSB);
            curPresetList.get(CHAN).setPC(PC);

        }
        catch (Exception ex) {
            if (debugmode != 0) System.out.println("### PlayMidi: Failed to Send MIDI Program Message CHAN: " + CHAN + " PC:" + PC + " MSB:" + MSB + " LSB:" + LSB);

            System.err.println("### PlayMidi Error: Send MIDI Program Change CHAN: " + CHAN + " PC:" + PC + " MSB:" + MSB + " LSB:" + LSB);
            System.err.println(ex);
            return false;
        }

        return true;
    }

    public boolean sendMidiControlChange(int CHAN, int CTRL, int VAL) {
        ShortMessage midiMsg = new ShortMessage();

        if ((CHAN < 0) || (CHAN > 15)) {
            System.err.println("### PlayMidi: sendMidiControlChange Error:  CHAN: " + CHAN + " CTRL:" + CTRL + " VAL:" + VAL);
            return false;
        }

        boolean isinit = sharedStatus.getPanic();

        try {
            if (debugmode != 0) System.out.println("PlayMidi: MIDI Control Change:  CHAN: " + (CHAN + 1) + " CTRL:" + CTRL + " VAL:" + VAL);

            Receiver midircv = sharedStatus.getRxDevice(); //MidiSystem.getReceiver();

            // Start playing note
            long timeStamp = -1;
            midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, CTRL & 0XFF, VAL & 0XFF);

            switch (CTRL) {
                case ccVOL:
                    //System.out.println("VOL setting change requested: " + VAL);
                    if ((curPresetList.get(CHAN).getVOL() != VAL) || isinit) {
                        midircv.send(midiMsg, timeStamp);

                        curPresetList.get(CHAN).setVOL(VAL);
                        //System.out.println("CHAN " + CHAN + " VOL setting changed! " + VAL);
                    }
                    //else
                    //    System.out.println("VOL setting NOT changed! " + VAL);
                    //break;
                case ccEXP:
                    if ((curPresetList.get(CHAN).getEXP() != VAL) || isinit) {
                        midircv.send(midiMsg, timeStamp);

                        curPresetList.get(CHAN).setEXP(VAL);
                    }
                    break;
                case ccREV:
                    if ((curPresetList.get(CHAN).getREV() != VAL) || isinit) {
                        midircv.send(midiMsg, timeStamp);

                        curPresetList.get(CHAN).setREV(VAL);
                    }
                    break;
                case ccCHO:
                    if ((curPresetList.get(CHAN).getCHO() != VAL) || isinit) {
                        midircv.send(midiMsg, timeStamp);

                        curPresetList.get(CHAN).setCHO(VAL);
                    }
                    break;
                case ccTIM:
                    if ((curPresetList.get(CHAN).getTIM() != VAL) || isinit) {
                        midircv.send(midiMsg, timeStamp);

                        curPresetList.get(CHAN).setTIM(VAL);
                    }
                    break;
                case ccATK:
                    if ((curPresetList.get(CHAN).getATK() != VAL) || isinit) {
                        midircv.send(midiMsg, timeStamp);

                        curPresetList.get(CHAN).setATK(VAL);
                    }
                    break;
                case ccREL:
                    if ((curPresetList.get(CHAN).getREL() != VAL) || isinit) {
                        midircv.send(midiMsg, timeStamp);

                        curPresetList.get(CHAN).setREL(VAL);
                    }
                    break;
                case ccBRI:
                    if ((curPresetList.get(CHAN).getBRI() != VAL) || isinit) {
                       midircv.send(midiMsg, timeStamp);

                        curPresetList.get(CHAN).setBRI(VAL);
                    }
                    break;
                case ccPAN:
                    if ((curPresetList.get(CHAN).getPAN() != VAL) || isinit) {
                        midircv.send(midiMsg, timeStamp);

                        curPresetList.get(CHAN).setPAN(VAL);
                    }
                    break;
                case ccMOD:
                    if ((curPresetList.get(CHAN).getMOD() != VAL) || isinit) {
                        midircv.send(midiMsg, timeStamp);

                        curPresetList.get(CHAN).setMOD(VAL);
                    }
                    break;
                case ccGP1:
                    midircv.send(midiMsg, timeStamp);

                    break;
                case ccGP2:
                    midircv.send(midiMsg, timeStamp);
                    break;
            }
        } catch (Exception ex) {
            if (debugmode != 0) System.out.println("### PlayMidi Error: Send MIDI Control Message:  CHAN: " + CHAN + " CTRL:" + CTRL + " VAL:" + VAL);

            System.err.println("### PlayMidi Error: Send MIDI Control Change  CHAN: " + CHAN + " CTRL:" + CTRL + " VAL:" + VAL);
            System.err.println(ex);
            return false;
        }

        return true;
    }


    public boolean sendUpperRotaryOn(int CHAN, boolean setrotoron) {

        int rotaryon[] = {0x63, 0x33, 0x62, 0x68, 0x06, 0x00};
        int rotarydepth[] = {0x63, 0x33, 0x62, 0x6a, 0x06, 0x45};
        int rotarystep[] = {0*6, 1*4, 2*5, 3*6, 4*6, 5*6, 6*7, 7*7, 8*7, 63};

        Receiver midircv = sharedStatus.getRxDevice(); //MidiSystem.getReceiver();

        ShortMessage midiMsg = new ShortMessage();

        long timeStamp = -1;

        if ((CHAN < 0) || (CHAN > 15)) {
            System.err.println("### PlayMidi Error: MidiControlChange:  CHAN: " + CHAN + " Rotary On/Off");
            return false;
        }

        if (setrotoron) {
            // Rotary On
            try {
                bupperrotoron = true;

                if (bupperrotorfast) {
                    midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, ccROT & 0XFF, rotarystep[9] & 0XFF);
                    midircv.send(midiMsg, timeStamp);
                }
                else {
                    midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, ccROT & 0XFF, rotarystep[2] & 0XFF);
                    midircv.send(midiMsg, timeStamp);
                }

                if (debugmode != 0) System.out.println("Upper Rotary ON!");
            }
            catch (InvalidMidiDataException ex) {
                System.err.println("### PlayMidi: Failed Upper Rotary ON!");
                System.err.println(ex);
            }
        }
        else {
            // Rotary Off
            bupperrotoron = false;

            try {
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, ccROT & 0XFF, 0 & 0XFF);
                midircv.send(midiMsg, timeStamp);

                if (debugmode != 0) System.out.println("Upper Rotary OFF! ");
            }
            catch (InvalidMidiDataException ex) {
                System.err.println("### PlayMidi: Failed  Upper Rotary OFF!");
                System.err.println(ex);
            }

        }

        return true;
    }

    public boolean sendUpperRotaryFast(int CHAN, boolean setrotaryfast) {

        int rotaryfast[] = {0x63, 0x33, 0x62, 0x69, 0x06, 0x00};
        int rotarystep[] = {0*6, 1*4, 2*5, 3*6, 4*6, 5*6, 6*7, 7*7, 8*7, 63};

        Receiver midircv = sharedStatus.getRxDevice(); //MidiSystem.getReceiver();
        //System.out.println("PlayMidi: Got Receiver: " + midircv.toString());

        ShortMessage midiMsg = new ShortMessage();

        long timeStamp = -1;

        if ((CHAN < 0) || (CHAN > 15)) {
            System.err.println("### PlayMidi Error: Midi Control Change:  CHAN: " + CHAN + " on Rotary Fast/Slow");
            return false;
        }

        if (!bupperrotoron) return false;

        if (setrotaryfast) {

            bupperrotorfast = true;
            // Ramp Rotary On Up
            Platform.runLater(() -> {
                for (int i = 2; i <= 9; i++) {
                    try {
                        midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, ccROT & 0XFF, rotarystep[i] & 0XFF);
                        midircv.send(midiMsg, timeStamp);

                        if (debugmode != 0) System.out.println("Rotary FAST: " + rotarystep[i]);
                        Thread.sleep(200);
                    }
                    catch (InvalidMidiDataException ex) {
                        System.err.println("### PlayMidi Error: Rotary FAST:  CHAN: " + CHAN);
                        System.err.println(ex);
                    }
                    catch (InterruptedException ex) {
                        System.err.println("### PlayMidi Error: Rotary FAST:  CHAN: " + CHAN);
                        System.err.println(ex);
                    }
                }
            });

        }
        else {
            bupperrotorfast = false;

            // Ramp Rotary Off Down
            Platform.runLater(() -> {
                for (int i = 9; i >= 2; i--) {
                    try {
                        midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, ccROT & 0XFF, rotarystep[i] & 0XFF);
                        midircv.send(midiMsg, timeStamp);

                        if (debugmode != 0) System.out.println("Rotary SLOW: " + rotarystep[i]);
                        Thread.sleep(200);
                    }
                    catch (InvalidMidiDataException ex) {
                        System.err.println("### PlayMidi Error: Rotary SLOW:  CHAN: " + CHAN);
                        System.err.println(ex);
                    }
                    catch (InterruptedException ex) {
                        System.err.println("### PlayMidi Error: Rotary SLOW:  CHAN: " + CHAN);
                        System.err.println(ex);
                    }
                }
            });

        }

        return true;
    }

    public boolean sendLowerRotaryOn(int CHAN, boolean setrotoron) {

        int rotaryon[] = {0x63, 0x33, 0x62, 0x68, 0x06, 0x00};
        int rotarydepth[] = {0x63, 0x33, 0x62, 0x6a, 0x06, 0x45};
        int rotarystep[] = {0*6, 1*4, 2*5, 3*6, 4*6, 5*6, 6*7, 7*7, 8*7, 63};

        Receiver midircv = sharedStatus.getRxDevice(); //MidiSystem.getReceiver();
        //System.out.println("PlayMidi: Created getReceiver: " + midircv.toString());

        ShortMessage midiMsg = new ShortMessage();

        long timeStamp = -1;

        if ((CHAN < 0) || (CHAN > 15)) {
            System.err.println("### PlayMidi Error: Midi Control Change:  CHAN: " + CHAN + " Rotary On/Off");
            return false;
        }

        if (setrotoron) {
            // Rotary On
            try {
                blowerrotoron = true;

                if (blowerrotorfast) {
                    midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, ccROT & 0XFF, rotarystep[9] & 0XFF);
                    midircv.send(midiMsg, timeStamp);
                }
                else {
                    midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, ccROT & 0XFF, rotarystep[2] & 0XFF);
                    midircv.send(midiMsg, timeStamp);
                }

                if (debugmode != 1) System.out.println("Lower Rotary ON!");
            }
            catch (InvalidMidiDataException ex) {
                System.err.println("### PlayMIDI Error: Lower Rotary ON!");
                System.err.println(ex);
            }
        }
        else {
            // Rotary Off
            blowerrotoron = false;

            try {
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, ccROT & 0XFF, 0 & 0XFF);
                midircv.send(midiMsg, timeStamp);

                if (debugmode != 0) System.out.println("Lower Rotary OFF! ");
            }
            catch (InvalidMidiDataException ex) {
                System.err.println("### PlayMIDI Error: Lower Rotary OFF! ");
                System.err.println(ex);
            }

        }

        return true;
    }

    public boolean sendLowerRotaryFast(int CHAN, boolean setrotaryfast) {

        int rotaryfast[] = {0x63, 0x33, 0x62, 0x69, 0x06, 0x00};
        int rotarystep[] = {0*6, 1*4, 2*5, 3*6, 4*6, 5*6, 6*7, 7*7, 8*7, 63};

        Receiver midircv = sharedStatus.getRxDevice(); //MidiSystem.getReceiver();
        //System.out.println("PlayMidi: Got Receiver: " + midircv.toString());

        ShortMessage midiMsg = new ShortMessage();

        long timeStamp = -1;

        if ((CHAN < 0) || (CHAN > 15)) {
            System.err.println("### PlayMidi Error: Midi Control Change CHAN: " + (CHAN + 1) + " Lower Rotary Fast/Slow");
            return false;
        }

        if (!blowerrotoron) return false;

        if (setrotaryfast) {

            blowerrotorfast = true;
            // Ramp Rotary On Up
            Platform.runLater(() -> {
                for (int i = 2; i <= 9; i++) {
                    try {
                        midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, ccROT & 0XFF, rotarystep[i] & 0XFF);
                        midircv.send(midiMsg, timeStamp);

                        if (debugmode != 0) System.out.println("Lower Rotary FAST: " + rotarystep[i]);
                        Thread.sleep(200);
                    }
                    catch (InvalidMidiDataException ex) {
                        System.err.println("### PlayMIDI Error: Lower Rotary FAST: " + rotarystep[i]);
                        System.err.println(ex);
                    }
                    catch (InterruptedException ex) {
                        System.err.println("### PlayMIDI Error: Lower Rotary FAST: " + rotarystep[i]);
                        System.err.println(ex);
                    }
                }
            });

        }
        else {
            blowerrotorfast = false;

            // Ramp Rotary Off Down
            Platform.runLater(() -> {
                for (int i = 9; i >= 2; i--) {
                    try {
                        midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, ccROT & 0XFF, rotarystep[i] & 0XFF);
                        midircv.send(midiMsg, timeStamp);

                        if (debugmode != 0) System.out.println("Rotary SLOW: " + rotarystep[i]);
                        Thread.sleep(200);
                    }
                    catch (InvalidMidiDataException ex) {
                        System.err.println("### PlayMIDI Error: Rotary SLOW: " + rotarystep[i]);
                        System.err.println(ex);
                    }
                    catch (InterruptedException ex) {
                        System.err.println("### PlayMIDI Error: Rotary SLOW: " + rotarystep[i]);
                        System.err.println(ex);
                    }
                }
            });

        }

        return true;
    }


    // Sending SysExMessages
    // https://github.com/fua94/SysEx-JAVA-Library
    public boolean sendSysEx(byte[] sysexmsg) {

        if (debugmode != 0) System.out.println("PlayMidi: Sending MIDI SysEx " + sysexmsg);

        long timeStamp = -1;
        ShortMessage midiMsg = new ShortMessage();
        try {
            //midircv = MidiSystem.getReceiver();
            midircv = sharedStatus.getRxDevice(); //MidiSystem.getReceiver();
            //System.out.println("PlayMidi: Created getReceiver " + midircv.toString());

            SysexMessage sysmmg = new SysexMessage();
            sysmmg.setMessage(sysexmsg, sysexmsg.length);
            midircv.send(sysmmg, -1);

        }
        catch (Exception ex) {
            System.err.println("### PlayMidi Error: MIDI Send SysEx " + midiMsg.toString());
            System.err.println(ex);
            return false;
        }

        return true;
    }


    //http://midi.teragonaudio.com/tech/midispec/ctloff.htm
    public boolean sendAllControllersOff() {

        int allControllersoff = 121;
        int CHAN = 0;

        long timeStamp = -1;
        ShortMessage midiMsg = new ShortMessage();

        try {
            midircv = sharedStatus.getRxDevice(); //MidiSystem.getReceiver();
            //System.out.println("PlayMidi: Created getReceiver: " + midircv.toString());

            for (CHAN = 0; CHAN < 16; CHAN++) {
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, allControllersoff & 0XFF, 0 & 0XFF);
                midircv.send(midiMsg, timeStamp);
            }
        } catch (Exception ex) {
            System.err.println("PlayMidi Error: MIDI Program Change All Controllers Reset:  CHAN: " + CHAN);
            System.err.println(ex);
            return false;
        }

        return true;
    }

    public boolean sendMidiPanic() {
        ShortMessage midiMsg = new ShortMessage();

        try {
            long timeStamp = -1;

            final byte ccALLSoundOFF = 120;
            final byte ccAllControllersOff = 121;
            final byte ccAllNotesOff = 123;

            byte VAL = 0;

            sharedStatus.setInit(true);

            midircv = sharedStatus.getRxDevice(); //MidiSystem.getReceiver();
            //System.out.println("PlayMidi: Created getReceiver: " + midircv.toString());

            for (int chanidx = 0; chanidx < 16; chanidx++) {

                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx, ccALLSoundOFF, VAL);
                midircv.send(midiMsg, timeStamp);

                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx, ccAllControllersOff, VAL);
                midircv.send(midiMsg, timeStamp);

                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx, ccAllNotesOff, VAL);
                midircv.send(midiMsg, timeStamp);

                // Default VOL to default for now
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx & 0XFF, ccVOL & 0XFF, 96 & 0XFF);
                midircv.send(midiMsg, timeStamp);

                // Default EXP to default for now
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx & 0XFF, ccEXP & 0XFF, 127 & 0XFF);
                midircv.send(midiMsg, timeStamp);

                // Default REV to default for now
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx & 0XFF, ccREV & 0XFF, 24 & 0XFF);
                midircv.send(midiMsg, timeStamp);

                // Default CHO to default for now
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx & 0XFF, ccCHO & 0XFF, 16 & 0XFF);
                midircv.send(midiMsg, timeStamp);

                // Default TIM to default for now
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx & 0XFF, ccTIM & 0XFF, 64 & 0XFF);
                midircv.send(midiMsg, timeStamp);

                // Default ATK to default for now
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx & 0XFF, ccATK & 0XFF, 0 & 0XFF);
                midircv.send(midiMsg, timeStamp);

                // Default REL to default for now
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx & 0XFF, ccREL & 0XFF, 0 & 0XFF);
                midircv.send(midiMsg, timeStamp);

                // Default BRI to default for now
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx & 0XFF, ccBRI & 0XFF, 64 & 0XFF);
                midircv.send(midiMsg, timeStamp);

                // Default PAN to default for now
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx & 0XFF, ccPAN & 0XFF, 64 & 0XFF);
                midircv.send(midiMsg, timeStamp);

                // Wait for commands to take effect before proceeding to next Channel
                Thread.sleep(100);

                //System.out.println("PlayMidi: PANIC Sound, Controllers, Notes off sent on channel: " + chanidx);
            }
        }
        catch (Exception ex) {
            System.err.println("PlayMidi Error: MIDI PANIC Control Change " + midiMsg.toString());
            System.err.println(ex);
            return false;
        }

        sendAllControllersOff();

        resetcurPresetList();

        MidiDevices mididevice = MidiDevices.getInstance();
        mididevice.resetOctaveCHAN();

        sharedStatus.setInit(false);

        sharedStatus.setStatusText("MIDI PANIC Sent");

        return true;
    }


    // Mute one specific Channel
    public boolean muteChannel(int CHAN) {

        // Do not mute Track number 0
        if (CHAN <= 0) return false;

        try {
            sequencer = MidiSystem.getSequencer(false);

            sequencer.setTrackMute(CHAN, true);

            boolean muted = sequencer.getTrackMute(CHAN);
            if (!muted) {
                System.err.println("### PlayMidi: MUTE failed on Track: " + CHAN);
                return false;        // muting failed
            }
            if (debugmode != 0) System.out.println("PlayMidi: MUTE Track: " + CHAN);
        }
        catch (Exception ex) {
            System.err.println("### PlayMidi Error: MUTE Track: " + CHAN);
            System.err.println(ex);
            return false;
        }

        return true;
    }

    // Unmute all potential Tracks
    public void unmuteAllTracks() {

        try {
            sequencer = MidiSystem.getSequencer(false);

            for (int CHAN = 0; CHAN < 16; CHAN++) {
                sequencer.setTrackMute(CHAN, false);
            }
            if (debugmode != 0) System.out.println("PlayMidi: UNMUTE all Tracks");
        }
        catch (Exception ex) {
            System.err.println(ex);
            System.err.println("### PlayMidi Error: MUTE all Tracks");
        }
    }

    // Unmute one specific Channel
    public boolean unmuteChannel(int CHAN) {

        if (CHAN < 0) return false;

        try {
            sequencer = MidiSystem.getSequencer(false);

            sequencer.setTrackMute(CHAN, false);

            boolean muted = sequencer.getTrackMute(CHAN);
            if (muted) {
                System.err.println("### PlayMidi Error: UNMUTE failed on Channel: " + CHAN);
                return false;        // Unmuting failed
            }
            if (debugmode != 0) System.out.println("PlayMidi: UNMUTE Channel: " + CHAN);
        }
        catch (Exception ex) {
            System.err.println("### PlayMidi Error: UNMUTE Channel: " + CHAN);
            System.err.println(ex);
            return false;
        }

        return true;
    }

    // Mute selected MIDI Tracks and associated MIDI Channela
    // Note: The Java MIDI mute/unmute function parameters is used as an index, and does not mute
    // the MIDI channel by channel Number on the Tracks. The user is to pick the Channels to be muted
    // by inspected the MIDI SMF file and apply the channel numbers in the Song definition.
    // Incorrect selection will result in MIDI channel mute failures or unintended channel mutes and plays.
    // Ignore mutes for a CHAN == 0.
    public boolean muteKeyboardTracks(MidiSong midiSong) {

        boolean breturn = true;

        if (debugmode != 0) {
            System.out.println("PlayMidi: MidiSong: " + midiSong.toString());
            System.out.println("PlayMidi: MUTE Bass Channel: " + midiSong.getTrackBass());
            System.out.println("PlayMidi: MUTE Lower Channel: " + midiSong.getTrackLower());
            System.out.println("PlayMidi: MUTE Upper Channel: " + midiSong.getTrackUpper());
        }

        try {
            if (sequencer == null) {
                midircv = sharedStatus.getRxDevice();

                sequencer = MidiSystem.getSequencer(false);
                sequencer.getTransmitter().setReceiver(midircv);
            }

            if (midiSong.getTrackBass() != 0) {
                sequencer.setTrackMute(midiSong.getTrackBass() - 1, true);
                boolean muted = sequencer.getTrackMute(midiSong.getTrackBass() - 1);
                if (!muted) {
                    System.err.println("### PlayMidi Error: MUTE failed Track " + midiSong.getTrackBass());
                    breturn = false;        // muting failed
                }
                else
                    if (debugmode !=0) System.out.println("PlayMidi: MUTE Track Bass " + midiSong.getTrackBass());
            }

            if (midiSong.getTrackLower() != 0) {
                sequencer.setTrackMute(midiSong.getTrackLower() - 1, true);
                boolean muted = sequencer.getTrackMute(midiSong.getTrackLower() - 1);
                if (!muted) {
                    System.err.println("### PlayMidi Error: MUTE failed Track Lower " + midiSong.getTrackLower());
                    breturn = false;        // muting failed
                }
                else if (debugmode != 0) System.out.println("PlayMidi: MUTE Track Lower " + midiSong.getTrackLower());
            }

            if (midiSong.getTrackUpper() != 0) {
                sequencer.setTrackMute(midiSong.getTrackUpper() - 1, true);
                boolean muted = sequencer.getTrackMute(midiSong.getTrackUpper() - 1);
                if (!muted) {
                    System.err.println("### PlayMidi Error: MUTE failed Track Upper  " + midiSong.getTrackUpper());
                    breturn = false;        // muting failed
                }
                else if (debugmode != 0) System.out.println("PlayMidi: MUTE Track Upper " + midiSong.getTrackUpper());
            }

        }
        catch (Exception ex) {
            System.err.println("### PlayMidi Error: MUTE Keyboard Tracks");
            System.err.println(ex);
            return false;
        }

        return breturn;
    }

    // Unmute All Keyboard Channels
    public boolean unmuteKeyboardTracks(MidiSong midiSong) {

        boolean breturn = true;
        boolean muted;

        if (debugmode != 0) {
            System.out.println("PlayMidi: MidiSong: " + midiSong.toString());
            System.out.println("PlayMidi: UNMUTE Bass Channel: " + midiSong.getTrackBass());
            System.out.println("PlayMidi: UNMUTE Lower Channel: " + midiSong.getTrackLower());
            System.out.println("PlayMidi: UNMUTE Upper Channel: " + midiSong.getTrackUpper());
        }

        try {
            if (sequencer == null) {
                //midircv = MidiSystem.getReceiver();
                midircv = sharedStatus.getRxDevice();

                sequencer = MidiSystem.getSequencer(false);
                sequencer.getTransmitter().setReceiver(midircv);
            }

            if (midiSong.getTrackBass() != 0) {
                sequencer.setTrackMute(midiSong.getTrackBass() - 1, false);
                muted = sequencer.getTrackMute(midiSong.getTrackBass() - 1);

                if (muted) {
                    breturn = false;        // muting failed
                } else System.out.println("PlayMidi: UNMUTE Bass Track: " + midiSong.getTrackBass());
            }

            if (midiSong.getTrackLower() != 0) {
                sequencer.setTrackMute(midiSong.getTrackLower() - 1, false);
                muted = sequencer.getTrackMute(midiSong.getTrackLower() - 1);

                if (muted) {
                    breturn = false;        // muting failed
                } else System.out.println("PlayMidi: UNMUTE Lower Track: " + midiSong.getTrackLower());
            }

            if (midiSong.getTrackUpper() != 0) {
                sequencer.setTrackMute(midiSong.getTrackUpper() - 1, false);
                muted = sequencer.getTrackMute(midiSong.getTrackUpper() - 1);

                if (muted) {
                    breturn = false;        // muting failed
                } else System.out.println("PlayMidi: UNMUTE Upper Track: " + midiSong.getTrackUpper());
            }
        }
        catch (Exception ex) {
            System.err.println("### PlayMidi Error:  UNMUTE Tracks!");
            System.err.println(ex);
            return false;
        }

        return breturn;
    }

    // Solo a specific Channel
    public boolean soloChannel(int CHAN) {

        if (CHAN < 0) return false;

        try {
            sequencer.setTrackSolo(CHAN, true);
            System.out.println("PlayMidi: Solo CHannel: " + CHAN);
        }
        catch (Exception ex) {
            System.err.println("### PlayMidi Error: Set SOLO on Track: " + CHAN);
            System.err.println(ex);
            return false;
        }

        return true;
    }

    // Unsolo a specific Channel
    public boolean unsoloChannel(int CHAN) {

        if (CHAN < 0) return false;

        try {
            sequencer.setTrackSolo(CHAN, false);
            System.out.println("PlayMidi: Solo CHannel: " + CHAN);
        }
        catch (Exception ex) {
            System.err.println("PlayMidi: Exception removing Solo Channel: " + CHAN);
            System.err.println(ex);
            return false;
        }

        return true;
    }

    // Initialize the patch and effects tracking list for each of the MIDI Channels
    // Used to avoid apply redundant MIDI patch and effect changes that may cause glitches in sound on some modules
    public void initCurPresetList() {

        for (int i = 0; i < 16; i++) {
            curPresetList.add(new MidiPreset());
        }
        System.out.println("PlayMidi: Initialized Tracking Preset");
    }

   public void resetcurPresetList() {

       for (int chanidx = 0; chanidx < 16; chanidx++) {
           curPresetList.get(chanidx).setPC(0);
           curPresetList.get(chanidx).setMSB(0);
           curPresetList.get(chanidx).setLSB(0);

           curPresetList.get(chanidx).setVOL(0);
           curPresetList.get(chanidx).setEXP(0);
           curPresetList.get(chanidx).setREV(0);
           curPresetList.get(chanidx).setCHO(0);
           curPresetList.get(chanidx).setTIM(0);
           curPresetList.get(chanidx).setATK(0);
           curPresetList.get(chanidx).setREL(0);
           curPresetList.get(chanidx).setBRI(0);
           curPresetList.get(chanidx).setMOD(0);
           curPresetList.get(chanidx).setPAN(0);
       }

       int presetidx = -1;
       boolean barstarted = false;
   }

    public boolean isMidiRunning() {

        boolean bisrunning = false;

        try {
            bisrunning = sequencer.isRunning();
            //System.out.println("PlayMidi: Checking sequencer is running");
        }
        catch (Exception ex) {
            System.out.println("### PlayMidi Info: Sequencer not run yet");
        }
        return bisrunning;
    }


    public String listInstruments() {

        Instrument instruments[];

        String instrumentlist = "";

        try {
            Soundbank soundbank = synthesizer.getDefaultSoundbank();

            if (soundbank != null) {
                instruments = synthesizer.getDefaultSoundbank().getInstruments();

                int trackNumber = 0;
                for (Track track : midiSeq.getTracks()) {
                    trackNumber++;

                    for (int i = 0; i < track.size(); i++) {

                        MidiEvent event = track.get(i);
                        MidiMessage message = event.getMessage();

                        if (message instanceof ShortMessage) {
                            ShortMessage sm = (ShortMessage) message;
                            if (sm.getCommand() == 192) {
                                //System.out.println("PlayMidi: Track " + trackNumber + " Channel=" + (sm.getChannel() + 1) + " " + instruments[sm.getData1()]);

                                String instrumentname = instruments[sm.getData1()].toString();
                                String instrumentnamenew = instrumentname.replace("Instrument:", "");

                                instrumentlist = instrumentlist.concat("TRK=").concat(Integer.toString(trackNumber));
                                instrumentlist = instrumentlist.concat("\t");
                                instrumentlist = instrumentlist.concat(" CHN=").concat(Integer.toString(sm.getChannel() + 1));
                                instrumentlist = instrumentlist.concat("\t");
                                instrumentlist = instrumentlist.concat(instrumentnamenew);
                                instrumentlist = instrumentlist.concat("\n");

                                break;
                            }
                        }
                    }
                }
            }
            else {
                System.out.println("PlayMidi: No Soundbank defined");
            }
        }
        catch (Exception ex) {
            System.err.println("PlayMidi Error: Listing instruments: " + ex);
            System.err.println(ex);
        }

        //System.out.println(instrumentlist);

        return instrumentlist;
    }


    public ArrayList<MidiInstrument> listChannelInstruments() {

        Instrument instruments[];
        ArrayList<MidiInstrument> midiinstruments = new ArrayList<>();

        try {
            Soundbank soundbank = synthesizer.getDefaultSoundbank();

            if (soundbank != null) {
                instruments = synthesizer.getDefaultSoundbank().getInstruments();

                int trackNumber = 0;
                for (Track track : midiSeq.getTracks()) {
                    trackNumber++;

                    for (int i = 0; i < track.size(); i++) {

                        MidiEvent event = track.get(i);
                        MidiMessage message = event.getMessage();

                        if (message instanceof ShortMessage) {
                            ShortMessage sm = (ShortMessage) message;
                            if (sm.getCommand() == 192) {
                                System.out.println("PlayMidi: Track " + trackNumber + " Channel=" + (sm.getChannel() + 1) + " " + instruments[sm.getData1()]);

                                MidiInstrument midiinstrument = new MidiInstrument();
                                midiinstrument.setTrackId(trackNumber);
                                midiinstrument.setChannelId(sm.getChannel() + 1);
                                midiinstrument.setInstrumentName(instruments[sm.getData1()].toString());
                                midiinstruments.add(midiinstrument);

                                break;
                            }
                        }
                    }
                }
            }
            else {
                System.out.println("PlayMidi: No Soundbank defined");
            }
        }
        catch (Exception ex) {
            System.err.println("### PlayMidi Error: Listing instruments: " + ex);
            System.err.println(ex);
        }

        return midiinstruments;
    }

    // Get and parse out the Time Signature for the currently loaded Song
    private void setTimeSignature(MidiSong midiSong) {

        String timeSig = midiSong.getTimeSig();
        String[] timeSigDetails = timeSig.split("/");

        TimeSigNum = Integer.parseInt(timeSigDetails[0]);
        TimeSigDen = Integer.parseInt(timeSigDetails[1]);

        if (debugmode != 0) System.out.println("MidiPlay: TimeSigNum " + TimeSigNum + ", TimeSigDen " + TimeSigDen);
    }


    // Set MIDI events to play a short Demo tune
    public void midiDemo(int channel) {

        if ((channel < 0) || (channel > 15)) channel = 0;

        demoplay(channel);
    }

    // Hard Coded Demo Song
    public void demoplay(int channel) {
        try {
            Sequencer player = MidiSystem.getSequencer();
            player.open();
            Sequence seq = new Sequence(Sequence.PPQ, 4);
            Track track = seq.createTrack();

            setMidiEvents(track, channel);

            player.setSequence(seq);
            player.start();

            // Credit: Code for stopping the player was suggested by Mr. Ryan Chapman
            while (true) {
                if(!player.isRunning()) {
                    player.stop();

                    //System.exit(0);
                }
            }
        }
        catch (MidiUnavailableException ex) {
            ex.printStackTrace();
        }
        catch (InvalidMidiDataException ex) {
            ex.printStackTrace();
        }
    }

    // Set MIDI events to play "Mary Had a Little Lamb"
    // Credit: https://github.com/ksnortum/midi-examples/blob/master/src/main/java/net/snortum/play/midi/PlaySequencer.java
    private void setMidiEvents(Track track, int channel) {
        int velocity = 64;
        int note = 61;
        int tick = 0;

        addMidiEvent(track, ShortMessage.PROGRAM_CHANGE, channel, 64, 0, tick);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 3);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note - 2, velocity, tick + 4);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note - 2, 0, tick + 7);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note - 4, velocity, tick + 8);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note - 4, 0, tick + 11);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note - 2, velocity, tick + 12);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note - 2, 0, tick + 15);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick + 16);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 19);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick + 20);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 23);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick + 24);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 31);
    }


    // Create a MIDI event and add it to the track
    private void addMidiEvent(Track track, int command, int channel, int data1, int data2, int tick) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(command, channel, data1, data2);
        }
        catch (InvalidMidiDataException ex) {
            System.err.println("### PlayMidi Error: Adding Midi Event: " + ex);
            System.err.println(ex);
            //ex.printStackTrace();
        }
        track.add(new MidiEvent(message, tick));
    }

    // The Universal SysEx message adjusts a device's master volume.
    public void sendMasterVolume() {

        // In a multitimbral device, the Volume controller messages are used to control the volumes of individual Channels.
        // The message to control Master Volume is as follows:
        //
        // 0xF0  SysEx
        // 0x7F  Realtime
        // 0x7F  The SysEx channel. Could be from 0x00 to 0x7F. Here we set it to "disregard channel".
        // 0x04  Sub-ID -- Device Control
        // 0x01  Sub-ID2 -- Master Volume
        // 0xLL  Bits 0 to 6 of a 14-bit volume
        // 0xMM  Bits 7 to 13 of a 14-bit volume
        // 0xF7  End of SysEx

        byte volLL = (byte) 0x3F; //0011 1111
        byte volMM = (byte) 0x3F; //0011 1111

        byte[] sysexvol = new byte[8];
        sysexvol[0] = (byte) 0xF0;
        sysexvol[1] = (byte) 0x7F;
        sysexvol[2] = (byte) 0x0F;
        sysexvol[3] = (byte) 0x04;
        sysexvol[4] = (byte) 0x01;
        sysexvol[5] = (byte) volLL;
        sysexvol[6] = (byte) volMM;
        sysexvol[7] = (byte) 0xF7;

        sendSysEx(sysexvol);

    }
}
