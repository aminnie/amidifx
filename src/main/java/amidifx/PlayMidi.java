package amidifx;

import amidifx.models.MidiPreset;
import amidifx.models.MidiSong;
import amidifx.models.SharedStatus;

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

    private static final String MID_DIRECTORY = "C:/amidifx/midifiles/";

    // Hardcoded keyboard channels for now. Note channels are coded from 0 - 15!
    private static final int DRUMS = 10;
    private static final int BASSKBD = 11;
    private static final int LOWERKBD = 12;
    private static final int UPPERKBD = 14;
    private static final int SOLOKBD = 16;

    private int TimeSigNum = 4;
    private int TimeSigDen = 4;

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


    // Static method to create singleton instance of PlayMidi class
    public synchronized static PlayMidi getInstance() {
        if (single_PlayMidiInstance == null) {
            single_PlayMidiInstance = new PlayMidi();

            System.out.println("PlayMidi: Creating instance PlayMidi");
        }

        return single_PlayMidiInstance;
    }

    // *** Make constructor private for Singleton ***
    private PlayMidi() {

        // Preset MIDI OUT Device
        midircv = sharedStatus.getRxDevice();
    }

    // Play MIDI File
    public boolean startMidiPlay(MidiSong midiSong, MidiPresets dopresets, int playmode) throws Exception {

        midiFile = midiSong.getMidiFile();
        presetFile = midiSong.getPresetFile();

        readpresets = new MidiPresets();
        readpresets.makeMidiPresets(presetFile);

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
            if (sequencer == null) {
                sequencer = MidiSystem.getSequencer();
                if (!sequencer.isOpen()) {
                    sequencer.open();
                }

                midircv = sharedStatus.getRxDevice();
                sequencer.getTransmitter().setReceiver(midircv);
            }

            if (sequencer == null) {
                sharedStatus.setStatusText("No Sequencer device available. Unable to start MIDI file play!");
                System.err.println("PlayMidi Error: No Sequencer device available. Unable to start play!");
                return false;
            }

        }
        catch(Exception ex) {

        }

        // Reset all MIDI Controllers as we start out
        sendAllControllersOff();

        // Prepare the TIme Signature for this Song
        setTimeSignature(midiSong);

        // Construct a Sequence object, and load it into sequencer.
        // Sets the current sequence on which the sequencer operates.
        try {
            if (!sequencer.isOpen()) {
                sequencer.open();
                System.out.println("PlayMidi: Sequencer opened: " + sequencer.toString());
            }

            System.out.println("PlayMidi: Starting Sequencer Play " + midiFile);

            mfile = new File(MID_DIRECTORY + midiFile);
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
                muteKeyboardChannels(midiSong);
            }

            // https://www.docs4dev.com/docs/en/java/java8/tutorials/sound-MIDI-seq-intro.html
            System.out.println("MidiPlay - Micros Seconds: " + sequencer.getMicrosecondLength());
            System.out.println("MidiPlay - Ticks: " + sequencer.getTickLength());
            System.out.println("MidiPlay - Tempo MPQ: " + sequencer.getTempoInMPQ());
            System.out.println("MidiPlay - Tempo BPM: " + sequencer.getTempoInBPM());
            System.out.println("MidiPlay - Tempo TempoFactor: " + sequencer.getTempoFactor());
            System.out.println("MidiPlay - Division Type: " + midiSeq.getDivisionType());

            seqresolution = midiSeq.getResolution();
            System.out.println("MidiPlay - Resolution: " + midiSeq.getResolution());

        }
        catch (Exception ex) {
            System.err.println("PlayMidi: Sequencer Play Exception: " + ex);
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
                        System.out.println("### PlayMidi: Duplicate MetaEvent Cue: Preset " + cuetext[0] + "  " + cuetext[1]);
                        return;
                    } else presetidx = newpresetidx;

                    System.out.println("### PlayMidi: MetaEvent Cue Preset " + cuetext[0] + " " + cuetext[1]);

                    if ((presetidx >= 0) && (presetidx <= 7)) {
                        System.out.println("### PlayMidi: MetaEvent Presetidx " + presetidx);

                        for (int chanidx = 0; chanidx < 16; chanidx++) {
                            MidiPreset preset = readpresets.getPreset(presetidx * 16 + chanidx);
                            readpresets.applyMidiPreset(preset, chanidx);

                            System.out.println("### Applied Channel " + chanidx + ", " + preset.toString());
                        }

                        sharedStatus.setStatusText("Preset " + (presetidx + 1) + " auto applied ");
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
            ////if (sequencer.isOpen()) {
            ////    //System.out.println("PlayMidi: stopMidiPlay closing open Sequencer");
            ////    sequencer.close();
            ////}

        }
        catch (Exception ex) {
            //System.err.println("### PlayMidi: Error attempting to stop sequencer play: " + ex);
            return false;
        }

        sharedStatus.setStatusText("Stopped Sequencer play " + midiFile);
        return true;
    }

    public boolean sendMidiNote(byte CHAN, byte NOTE, boolean NOTEON) {
        ShortMessage midiMsg = new ShortMessage();

        CHAN = (byte) (CHAN - 1);

        try {
            //midircv = MidiSystem.getReceiver();
            long timeStamp = -1;

            if (NOTEON) {
                midiMsg.setMessage(ShortMessage.NOTE_ON, CHAN, NOTE, 48);
                midircv.send(midiMsg, timeStamp);
                //System.out.println("PlayMidi: Sent MIDI Note ON " + midiMsg.toString());
            } else {
                midiMsg.setMessage(ShortMessage.NOTE_OFF, CHAN, NOTE, 0);
                midircv.send(midiMsg, timeStamp);
                //System.out.println("PlayMidi: Sent MIDI Note OFF " + midiMsg.toString());
            }
        } catch (Exception ex) {
            System.err.println("PlayMidi Error: MIDI Note Send Exception " + midiMsg.toString());
            System.err.println(ex);
            return false;
        }

        return true;
    }

    // http://jsresources.sourceforge.net/faq_midi.html#hw_synth_as_synthesizer
    public boolean sendMidiProgramChange(int CHAN, int PC, int MSB, int LSB) {

        CHAN = CHAN - 1;
        if (CHAN < 0) {
            System.err.println("PlayMidi: sendMidiProgramChange Error CHAN: " + (CHAN + 1) + " PC:" + (PC + 1) + " LSB:" + LSB + " MSB:" + MSB);
            return false;
        }

        //System.out.println("PlayMidi: Sending MIDI Program Change  CHAN: " + (CHAN +1 )  + " PC:" + PC + " MSB:" + MSB + " LSB:"+ LSB);

        long timeStamp = -1;
        ShortMessage midiMsg = new ShortMessage();
        try {
            if (midircv == null) {
                Receiver midircv = MidiSystem.getReceiver();
                //System.out.println("PlayMidi: Created getReceiver: " + midircv.toString());
            }

            // Proceed to apply Bank and Program changes. Do so only if not duplicate of previous
            if ((curPresetList.get(CHAN).getMSB() == MSB) &&
                    (curPresetList.get(CHAN).getLSB() == LSB) && (curPresetList.get(CHAN).getPC() == PC)) {

                //System.out.println("PlayMidi: Duplicate Program Change on CHAN "  + (CHAN + 1) + ", PC " + PC + " MSB:" + MSB + " LSB:" + LSB + " ignored");
                return false;
            }

            midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN, 0, MSB & 0xFF); //(int)(LSB & 0xFF));
            midircv.send(midiMsg, timeStamp);
            midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN, 32, LSB & 0xFF); //(int)(MSB & 0xFF));
            midircv.send(midiMsg, timeStamp);

            midiMsg.setMessage(ShortMessage.PROGRAM_CHANGE, CHAN, PC & 0XFF, 64);
            midircv.send(midiMsg, timeStamp);

            // Log the newly sent PC to compare against next send
            ////curPresetList.get(CHAN).setMSB(MSB);
            ////curPresetList.get(CHAN).setLSB(LSB);
            ////curPresetList.get(CHAN).setPC(PC);

            //System.out.println("PlayMidi: Sent MIDI Program Message: CHAN: " + (CHAN + 1) + " PC:" + PC + " MSB:" + MSB + " LSB:" + LSB);
        } catch (Exception ex) {
            System.err.println("PlayMidi Error: Sent MIDI Program Change: " + midiMsg.toString() + " CHAN: " + (CHAN + 1) + " PC:" + PC + " MSB:" + MSB + " LSB:" + LSB);
            System.err.println(ex);
            return false;
        }

        return true;
    }

    public boolean sendMidiControlChange(int CHAN, int CTRL, int VAL) {
        ShortMessage midiMsg = new ShortMessage();

        CHAN = CHAN - 1;
        if (CHAN < 0) {
            System.err.println("PlayMidi: sendMidiControlChange Error:  CHAN: " + (CHAN + 1) + " CTRL:" + CTRL + " VAL:" + VAL);
            return false;
        }

        try {
            //System.out.println("PlayMidi: sendMidiControlChange sending MIDI Control Change:  CHAN: " + (CHAN + 1) + " CTRL:" + CTRL + " VAL:" + VAL);

            // Start playing note
            long timeStamp = -1;
            midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, CTRL & 0XFF, VAL & 0XFF);

            switch (CTRL) {
                case ccVOL:
                    //System.out.println("VOL setting change requested: " + VAL);
                    //if (curPresetList.get(CHAN).getVOL() != VAL) {
                    midircv.send(midiMsg, timeStamp);

                    ////curPresetList.get(CHAN).setVOL(VAL);
                    //System.out.println("VOL setting changed! " + VAL);
                    //}
                    //else
                    //    System.out.println("VOL setting NOT changed! " + VAL);
                    break;
                case ccEXP:
                    //if (curPresetList.get(CHAN).getEXP() != VAL) {
                    midircv.send(midiMsg, timeStamp);

                    ////curPresetList.get(CHAN).setEXP(VAL);
                    //}
                    break;
                case ccREV:
                    //if (curPresetList.get(CHAN).getREV() != VAL) {
                    midircv.send(midiMsg, timeStamp);

                    ////curPresetList.get(CHAN).setREV(VAL);
                    //}
                    break;
                case ccCHO:
                    //if (curPresetList.get(CHAN).getCHO() != VAL) {
                    midircv.send(midiMsg, timeStamp);

                    ////curPresetList.get(CHAN).setCHO(VAL);
                    //}
                    break;
                case ccTRE:
                    //if (curPresetList.get(CHAN).getTRE() != VAL) {
                    midircv.send(midiMsg, timeStamp);

                    ////curPresetList.get(CHAN).setTRE(VAL);
                    //}
                    break;
                case ccMOD:
                    //if (curPresetList.get(CHAN).getMOD() != VAL) {
                    midircv.send(midiMsg, timeStamp);

                    ////curPresetList.get(CHAN).setMOD(VAL);
                    //}
                    break;
                case ccPAN:
                    //if (curPresetList.get(CHAN).getPAN() != VAL) {
                    midircv.send(midiMsg, timeStamp);

                    ////curPresetList.get(CHAN).setPAN(VAL);
                    //}
                    break;
            }
        } catch (Exception ex) {
            System.err.println("PlayMidi Error: Sent MIDI Control Change " + midiMsg.toString() + " CHAN: " + (CHAN + 1) + " CTRL:" + CTRL + " VAL:" + VAL);
            System.err.println(ex);
            return false;
        }

        //System.out.println("PlayMidi: Sent MIDI Control Message: " + midiMsg.toString() + " CHAN: " + (CHAN + 1) + " CTRL:" + CTRL + " VAL:" + VAL);
        return true;
    }

    public void sendRotaryFast() {
        sendMidiControlChange(14, 74, 127);
    }

    public void sendRotarySlow() {
        sendMidiControlChange(14, 74, 31);
    }

    public void sendRotaryOff() {
        sendMidiControlChange(14, 74, 0);
    }

    public boolean sendSysEx(byte[] SysExMsg) {

        // Example SysEx Message
        byte[] testmessage = {
                (byte) 0xF0, (byte) 0x52, (byte) 0x00,
                (byte) 0x5A, (byte) 0x50, (byte) 0xF7,
                (byte) 0xF0, (byte) 0x52, (byte) 0x00,
                (byte) 0x5A, (byte) 0x16, (byte) 0xF7
        };

        System.out.println("PlayMidi: Sending MIDI SysEx " + SysExMsg);

        long timeStamp = -1;
        ShortMessage midiMsg = new ShortMessage();
        try {
            if (midircv == null) {
                Receiver midircv = MidiSystem.getReceiver();
                //System.out.println("PlayMidi: Created getReceiver " + midircv.toString());
            }

            //  Obtain a MIDI track from the sequence  ****
            Track t = sequencer.getSequence().createTrack();
            ;

            SysexMessage sm = new SysexMessage();
            sm.setMessage(SysExMsg, SysExMsg.length);
            MidiEvent msg = new MidiEvent(sm, (long) 0);
            t.add(msg);
        } catch (Exception ex) {
            //System.err.println("### PlayMidi Error: Sent MIDI Program Change " + midiMsg.toString() + " CHAN: " + (CHAN + 1) + " PC:" + PC + MSB + " LSB:" + LSB + " MSB:");
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
            for (CHAN = 0; CHAN < 16; CHAN++) {
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, CHAN & 0XFF, allControllersoff & 0XFF, 0 & 0XFF);
                midircv.send(midiMsg, timeStamp);
            }
        } catch (Exception ex) {
            System.err.println("PlayMidi Error: Sent MIDI Program Change: All Controllers Reset:  CHAN: " + (CHAN + 1));
            System.err.println(ex);
            return false;
        }

        return true;
    }

    public boolean sendMidiPanic() {
        ShortMessage midiMsg = new ShortMessage();

        try {
            long timeStamp = -1;

            byte ccALLSoundOFF = 120;
            byte ccAllControllersOff = 121;
            byte ccAllNotesOff = 123;
            byte VAL = 0;

            for (int chanidx = 0; chanidx < 16; chanidx++) {
                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx, ccALLSoundOFF, VAL);
                midircv.send(midiMsg, timeStamp);

                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx, ccAllControllersOff, VAL);
                midircv.send(midiMsg, timeStamp);

                midiMsg.setMessage(ShortMessage.CONTROL_CHANGE, chanidx, ccAllNotesOff, VAL);
                midircv.send(midiMsg, timeStamp);

                //System.out.println("PlayMidi: PANIC Sound, Controllers, Notes off sent on channel: " + chanidx);
            }
        } catch (Exception ex) {
            System.err.println("PlayMidi Error: Sent MIDI PANIC Control Changes " + midiMsg.toString());
            System.err.println(ex);
            return false;
        }

        sendAllControllersOff();

        resetcurPresetList();

        sharedStatus.setStatusText("MIDI PANIC Sent");

        return true;
    }


    // Mute one specific Channel
    public boolean muteChannel(int CHAN) {

        CHAN = CHAN - 1;
        if (CHAN < 0) return false;

        try {
            sequencer.setTrackMute(CHAN, true);

            boolean muted = sequencer.getTrackMute(CHAN);
            if (!muted) {
                System.err.println("PlayMidi: Muting failed on Channel: " + CHAN);
                return false;        // muting failed
            }
            System.out.println("PlayMidi: Muted Channel: " + CHAN);
        }
        catch (Exception ex) {
            System.err.println("PlayMidi: Exception muting Channel: " + CHAN);
            return false;
        }

        return true;
    }

    // Unmute all potential Tracks
    public void unmuteAllTracks() {

        try {

            for (int CHAN = 0; CHAN < 16; CHAN++) {
                sequencer.setTrackMute(CHAN, false);
            }
        }
        catch (Exception ex) {
            System.err.println("PlayMidi: Exception muting all Tracks");
        }
    }

    // Unmute one specific Channel
    public boolean unmuteChannel(int CHAN) {

        CHAN = CHAN - 1;
        if (CHAN < 0) return false;

        try {
            sequencer.setTrackMute(CHAN, false);

            boolean muted = sequencer.getTrackMute(CHAN);
            if (muted) {
                System.err.println("PlayMidi: Unmute failed on Channel: " + CHAN);
                return false;        // Unmuting failed
            }
            System.out.println("PlayMidi: Unmuted Channel: " + CHAN);
        }
        catch (Exception ex) {
            System.err.println("PlayMidi: Exception Unmuting Channel: " + CHAN);
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
    public boolean muteKeyboardChannels(MidiSong midiSong) {

        boolean breturn = true;

        System.out.println("PlayMidi: MidiSong: " + midiSong.toString());
        System.out.println("PlayMidi: Muted Bass Channel: " + midiSong.getChanBass());
        System.out.println("PlayMidi: Muted Lower Channel: " + midiSong.getChanLower());
        System.out.println("PlayMidi: Muted Upper Channel: " + midiSong.getChanUpper());

        try {
            if (sequencer == null) {
                sequencer = MidiSystem.getSequencer();
                sequencer.getTransmitter().setReceiver(midircv);
            }

            if (midiSong.getChanBass() != 0) {
                sequencer.setTrackMute(midiSong.getChanBass() - 1, true);
                boolean muted = sequencer.getTrackMute(midiSong.getChanBass() - 1);
                if (!muted) {
                    System.out.println("PlayMidi: Mute failed Channel " + midiSong.getChanBass());
                    breturn = false;        // muting failed
                }
                else System.out.println("PlayMidi: Muted Channel Bass " + midiSong.getChanBass());
            }

            if (midiSong.getChanLower() != 0) {
                sequencer.setTrackMute(midiSong.getChanLower() - 1, true);
                boolean muted = sequencer.getTrackMute(midiSong.getChanLower() - 1);
                if (!muted) {
                    System.out.println("PlayMidi: Mute failed Channel " + midiSong.getChanLower());
                    breturn = false;        // muting failed
                }
                else System.out.println("PlayMidi: Muted Channel Lower " + midiSong.getChanLower());
            }

            if (midiSong.getChanUpper() != 0) {
                sequencer.setTrackMute(midiSong.getChanUpper() - 1, true);
                boolean muted = sequencer.getTrackMute(midiSong.getChanUpper() - 1);
                if (!muted) {
                    System.out.println("PlayMidi: Mute failed Channel " + midiSong.getChanUpper());
                    breturn = false;        // muting failed
                }
                else System.out.println("PlayMidi: Muted Channel Upper " + midiSong.getChanUpper());
            }

        }
        catch (Exception ex) {
            System.err.println("PlayMidi: Exception muting Channels");
            return false;
        }

        return breturn;
    }

    // Unmute All Keyboard Channels
    public boolean unmuteKeyboardChannels(MidiSong midiSong) {

        boolean breturn = true;
        boolean muted;

        System.out.println("PlayMidi: MidiSong: " + midiSong.toString());
        System.out.println("PlayMidi: Unmuted Bass Channel: " + midiSong.getChanBass());
        System.out.println("PlayMidi: Unmuted Lower Channel: " + midiSong.getChanLower());
        System.out.println("PlayMidi: Unmuted Upper Channel: " + midiSong.getChanUpper());

        try {
            if (sequencer == null) {
                sequencer = MidiSystem.getSequencer();
                sequencer.getTransmitter().setReceiver(midircv);
            }

            if (midiSong.getChanBass() != 0) {
                sequencer.setTrackMute(midiSong.getChanBass() - 1, false);
                muted = sequencer.getTrackMute(midiSong.getChanBass() - 1);

                if (muted) {
                    breturn = false;        // muting failed
                } else System.out.println("PlayMidi: Unmuted Bass Channel: " + midiSong.getChanBass());
            }

            if (midiSong.getChanLower() != 0) {
                sequencer.setTrackMute(midiSong.getChanLower() - 1, false);
                muted = sequencer.getTrackMute(midiSong.getChanLower() - 1);

                if (muted) {
                    breturn = false;        // muting failed
                } else System.out.println("PlayMidi: Unmuted Lower Channel: " + midiSong.getChanLower());
            }

            if (midiSong.getChanUpper() != 0) {
                sequencer.setTrackMute(midiSong.getChanUpper() - 1, false);
                muted = sequencer.getTrackMute(midiSong.getChanUpper() - 1);

                if (muted) {
                    breturn = false;        // muting failed
                } else System.out.println("PlayMidi: Unmuted Upper Channel: " + midiSong.getChanUpper());
            }
        }
        catch (Exception ex) {
            System.out.println("PlayMidi: Error unmuting channels!");
            System.out.println(ex);
            return false;
        }

        return breturn;
    }

    // Solo a specific Channel
    public boolean soloChannel(int CHAN) {

        CHAN = CHAN - 1;
        if (CHAN < 0) return false;

        try {
            sequencer.setTrackSolo(CHAN, true);
            System.out.println("PlayMidi: Solo CHannel: " + CHAN);
        }
        catch (Exception ex) {
            System.err.println("PlayMidi: Exception Solo-ing Channel: " + CHAN);
            return false;
        }

        return true;
    }

    // Unsolo a specific Channel
    public boolean unsoloChannel(int CHAN) {

        CHAN = CHAN - 1;
        if (CHAN < 0) return false;

        try {
            sequencer.setTrackSolo(CHAN, false);
            System.out.println("PlayMidi: Solo CHannel: " + CHAN);
        }
        catch (Exception ex) {
            System.err.println("PlayMidi: Exception removing Solo Channel: " + CHAN);
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
           curPresetList.get(chanidx).setMOD(0);
           curPresetList.get(chanidx).setPAN(0);
           curPresetList.get(chanidx).setTRE(0);
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
            System.err.println("PlayMidi Exception: Checking sequencer is running");
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

                                instrumentlist = instrumentlist.concat("Track=").concat(Integer.toString(trackNumber));
                                instrumentlist = instrumentlist.concat(" Channel=").concat(Integer.toString(sm.getChannel() + 1));
                                instrumentlist = instrumentlist.concat(" ").concat(instruments[sm.getData1()].toString()).concat("\n");

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
            System.err.println("PlayMidi: Error listing instruments: " + ex);
        }

        //System.out.println(instrumentlist);

        return instrumentlist;
    }

    // Get and parse out the Time Signature for the currently loaded Song
    private void setTimeSignature(MidiSong midiSong) {

        String timeSig = midiSong.getTimeSig();
        String[] timeSigDetails = timeSig.split("/");

        TimeSigNum = Integer.parseInt(timeSigDetails[0]);
        TimeSigDen = Integer.parseInt(timeSigDetails[1]);

        //System.out.println("MidiPlay: TimeSigNum " + TimeSigNum + ", TimeSigDen " + TimeSigDen);
    }

}
