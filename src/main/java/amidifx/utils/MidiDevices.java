package amidifx.utils;

import amidifx.models.SharedStatus;

import javax.sound.midi.*;

public class MidiDevices {

    private static String TRANS_DEV_NAME = "javax.sound.midi.Transmitter#Microsoft MIDI Mapper";
    private static String SYNTH_DEV_NAME = "javax.sound.midi.Synthesizer#Microsoft MIDI Mapper";
    private static String SEQ_DEV_NAME = "default";

    /**
     * See {@link MidiSystem} for other classes
     */
    private static final String TRANS_PROP_KEY = "javax.sound.midi.Transmitter";
    private static final String SYNTH_PROP_KEY = "javax.sound.midi.Synthesizer";
    private static final String SEQ_PROP_KEY = "javax.sound.midi.Sequence";

    Synthesizer synthesizer;
    Sequencer sequencer;
    Receiver midircv;
    Transmitter miditrans;
    AMidiFXReceiver displayReceiver;

    AppConfig config;
    SharedStatus sharedstatus;

    private String selindevice = "default";
    private String seloutdevice = "default";

    // Layered channels out (defaulted): presetIdx, channelInIdx, (ChannelOutIdx & ModuleIdx) * 10, OctaveTran
    private byte[] channelOutStruct = {0, 13, 0, 0, 14, 0, 15, 0, 16, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,};

    private static final byte chan14idx = 4;
    private static final byte chan15idx = 6;
    private static final byte chan16idx = 8;

    private static final byte chan12idx = 4;
    private static final byte chan13idx = 6;

    private byte[] layerUpper = {0, 14, 0, 0, 14, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,};
    private byte[] layerLower = {0, 12, 0, 0, 12, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,};

    private byte lower12notestrack[] = new byte[128];
    private byte lower13notestrack[] = new byte[128];
    private byte upper14notestrack[] = new byte[128];
    private byte upper15notestrack[] = new byte[128];
    private byte upper16notestrack[] = new byte[128];

    // Track Layering requests and close out on queued basis to ensure no hung notes
    private boolean upper14layeron = false;
    private boolean upper15layeron = false;
    private boolean upper16layeron = false;
    private int uppernoteson = 0;

    private boolean lower12layeron = false;
    private boolean lower13layeron = false;
    private int lowernoteson = 0;

    // Track open/closed notes to prevent hanging:
    // 0 = no tracking or all closed; 1 = layer off request (but open notes); 2 = no open notes after current close note
    private boolean trackupper14opennotes = false;
    private boolean trackupper15opennotes = false;
    private boolean trackupper16opennotes = false;

    private boolean tracklower12opennotes = false;
    private boolean tracklower13opennotes = false;

    // Static variable single_instance of type PlayMidi
    private static MidiDevices single_MidiDevices_Instance = null;

    // Static method to create singleton instance of PlayMidi class
    public synchronized static MidiDevices getInstance() {
        if (single_MidiDevices_Instance == null) {
            single_MidiDevices_Instance = new MidiDevices();

            //System.out.println("MidiDevices: Creating instance MidiDevices");
        }

        return single_MidiDevices_Instance;
    }

    // *** Make constructor private for Singleton ***
    private MidiDevices() {
    }

    public int createMidiDevices(String selindevice, String seloutdevice) {

        createNotesTracker();

        this.selindevice = selindevice;
        this.seloutdevice = seloutdevice;

        // Load Config File Properties
        config = AppConfig.getInstance();
        config.setInDevice(selindevice);
        config.setOutDevice(seloutdevice);

        sharedstatus = SharedStatus.getInstance();

        try {
            // Get output Synth or external Sound Module
            midircv = openMidiReceiver(seloutdevice);
            if (midircv == null) {
                System.err.print("**** Error: Unable to open selected MIDI OUT device: " + seloutdevice);

                sharedstatus.setStatusText("Error opening selected MIDI OUT device");
                return -1;
            }

            // Get receiver from the synthesizer, then set it in transmitter.
            // Get a transmitter and synthesizer from their device names using system properties or defaults
            //trans.setReceiver(midircv);
            displayReceiver = new AMidiFXReceiver(midircv);
            sharedstatus.setRxDevice(displayReceiver);

            miditrans = getTransmitter();
            if (miditrans != null) {
                miditrans.setReceiver(displayReceiver); // or just "receiver"
                sharedstatus.setTxDevice(miditrans);

                System.out.println("Ready to play MIDI keyboard...");
            } else
                System.out.println("**** No MIDI keyboard connected! Connect USB MIDI keyboard proceed.");

            // Demo Play Sequencer Song in parallel with Keyboard input
            ////playDemoSequence(1);
        }
        catch (Exception e) {     //// MidiUnavailableException
            System.err.println("Error getting receiver from synthesizer");
            e.printStackTrace();
        }

        System.out.println("MIDIDevices: Device Configuration Complete");

        return 0;
    }


    // Play Song on Sequencer
    private void playDemoSequence(int replaycnt) {

        // Get default sequencer, if it exists
        sequencer = getSequencer();
        if (sequencer == null) {
            System.err.print("**** Error: Unable to open Sequencer device: " + sequencer.getDeviceInfo().getName());

            sharedstatus.setStatusText("Error: Unable to open Sequencer device: " + sequencer.getDeviceInfo().getName());
            return;
        }

        try {
            sequencer.open();
            sequencer.getTransmitter().setReceiver(midircv);
        } catch (Exception ex) {
            System.out.println("Error: Open Demo Sequencer");
        }

        for (int i = 1; i <= replaycnt; i++) {
            System.out.println("Starting Demo Sequencer Play:" + i);

            sequencer.setTempoInBPM(144.0f);

            try {
                sequencer.setSequence(getMidiInputData());
            } catch (InvalidMidiDataException e1) {
                e1.printStackTrace();
                return;
            }

            // Start demo sequence
            sleep(200);
            sequencer.start();
            while (sequencer.isRunning()) {
                sleep(1000);
            }

            // Sleep or last note is clipped
            sleep(200);

            sequencer.close();
        }

    }


    /**
     * Return a specific synthesizer object by setting the system property, otherwise the default
     */
    private Synthesizer getSynthesizer() {
        if (!SYNTH_DEV_NAME.isEmpty() || !"default".equalsIgnoreCase(SYNTH_DEV_NAME)) {
            System.setProperty(SYNTH_PROP_KEY, SYNTH_DEV_NAME);
        }

        try {
            return MidiSystem.getSynthesizer();
        } catch (MidiUnavailableException e) {
            System.err.println("Error getting synthesizer");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return a specific transmitter object by setting the system property, otherwise the default
     */
    private Transmitter getTransmitter() {

        String indevice = config.getInDevice();
        if (!indevice.isEmpty()) {
            TRANS_DEV_NAME = TRANS_PROP_KEY + "#" + indevice;
            System.out.println("TRANS_DEV_NAME set to: " + TRANS_DEV_NAME);
        }

        if (!TRANS_DEV_NAME.isEmpty() && !"default".equalsIgnoreCase(TRANS_DEV_NAME)) {
            System.setProperty(TRANS_PROP_KEY, TRANS_DEV_NAME);
        }

        try {
            return MidiSystem.getTransmitter();
        } catch (MidiUnavailableException e) {
            System.err.println("No External MIDI keyboard available! Please connect a USB keyboard.");
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * Rreturn a specific sequencer object by setting the system property, otherwise the default
     */
    private Sequencer getSequencer() {
        if (!SEQ_DEV_NAME.isEmpty()
                || !"default".equalsIgnoreCase(SEQ_DEV_NAME)) {
            System.setProperty(SEQ_PROP_KEY, SEQ_DEV_NAME);
        }

        try {
            return MidiSystem.getSequencer();
        } catch (MidiUnavailableException e) {
            System.err.println("Error getting sequencer");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Implement custom Receiver to read Keyboard input and layer/multiplex
     */
    private class AMidiFXReceiver implements Receiver {
        private Receiver receiver;
        boolean isSystemExclusiveData = false;

        public AMidiFXReceiver(Receiver receiver) {
            this.receiver = receiver;
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            //receiver.send(message, timeStamp);
            routeMessage(message, timeStamp);

            //displayMessage(message, timeStamp);
        }

        @Override
        public void close() {
            receiver.close();
        }

        // Prepare to Route and Layer incoming MIDI messages
        private void routeMessage(MidiMessage message, long timeStamp) {

            //receiver.send(message, timeStamp);

            int status = message.getStatus();

            // Do not route status and timing messages
            if ((status == 0xf8) || (status == 0xfe)) {
                receiver.send(message, timeStamp);
                return;
            }

            //System.out.printf("%d - Status: 0x%s", timeStamp, Integer.toHexString(status));

            // These statuses have MIDI channel numbers and data (except 0xf0 thru 0xff)
            // Strip channel number out of status
            int highNibble = status & 0xf0;
            switch (highNibble) {
                case 0x80:      //displayNoteOff(message);
                case 0x90:      //displayNoteOn(message);
                    layerOutMessages(message, timeStamp);
                    break;
                case 0xa0:      //displayKeyPressure(message);
                case 0xb0:      //displayControllerChange(message);
                case 0xc0:      //displayProgramChange(message);
                case 0xd0:      //displayChannelPressure(message);
                case 0xe0:      //displayPitchBend(message);
                case 0xf0:
                    //receiver.send(message, timeStamp);
                    //layerOutMessages(message, timeStamp);
                    //break;
                default:
                    // Message not recognized, but forward?
                    receiver.send(message, timeStamp);
            }
        }


        // Play MIDI Note On/Off IN messages and perform all Channel OUT layering as needed
        private void layerOutMessages(MidiMessage message, long timeStamp) {

            ShortMessage shortmessage;

            if (message.getLength() < 3 || message.getLength() % 2 == 0) {
                System.out.println("Unable to Layer/Output Bad MIDI message");
                return;
            }

            // Now dissect to determine if Layering is needed and forward in layered channels
            byte[] bytes = message.getMessage();
            int command = message.getStatus() & 0xf0;
            byte channel = (byte)(message.getStatus() & 0x0f);

            // Testing device and channel, e.g. Trellis M4
            if (channel == 0) channel = 13;

            // Do not layer any source channels below Lower Keyboard, including Bass Pedals and Drums
            if (channel < (byte)(sharedstatus.getLower1CHAN()-1)) {
                receiver.send(message, timeStamp);
                return;
            }

            //System.out.println("LayerOutMessages: " + channel + ", " + message.toString());

            // Layer Upper Channel 1
            if (channel == (byte)(sharedstatus.getUpper1CHAN()-1)) {

                // Track open notes to prevent hung notes. Close out all open notes before completing the layer off request
                // Note: Midi Note ON with Velocity = 0, is same as Note OFF
                if ((command == 0x90) && (bytes[2] != 0)) {
                    uppernoteson++;

                    try {
                        // Layer first/origin Upper Channel if not 0 (off/muted)
                        byte chan = layerUpper[chan14idx];
                        if ((chan != 0)) {
                            shortmessage = new ShortMessage();
                            shortmessage.setMessage(command, channel, byteToInt(bytes[1]), byteToInt(bytes[2]));
                            receiver.send(shortmessage, timeStamp);

                            upper14notestrack[(int)bytes[1]] = (byte)1;
                            //System.out.println("Layer Upper index[90]: " + chan + ", " + bytes[1] + ", " + bytes[2]);
                        }

                        // Layer Upper Channel + 1 if not 0 (off/muted)
                        chan = layerUpper[chan15idx];
                        if ((chan != 0) ) {
                            shortmessage = new ShortMessage();
                            shortmessage.setMessage(command, channel + 1, byteToInt(bytes[1]), byteToInt(bytes[2]));
                            receiver.send(shortmessage, timeStamp);

                            upper15notestrack[(int)bytes[1]] = (byte)1;
                            //System.out.println("Layer Upper index[90]: " + chan + ", " + bytes[1] + ", " + bytes[2]);
                        }

                        // Layer Upper Channel + 2 if not 0 (off/muted)
                        chan = layerUpper[chan16idx];
                        if ((chan != 0) ) {
                            shortmessage = new ShortMessage();
                            shortmessage.setMessage(command, channel + 2, byteToInt(bytes[1]), byteToInt(bytes[2]));
                            receiver.send(shortmessage, timeStamp);

                            upper16notestrack[(int)bytes[1]] = (byte)1;
                            //System.out.println("Layer Upper index[90]: " + chan + ", " + bytes[1] + ", " + bytes[2]);
                        }
                    }
                    catch (InvalidMidiDataException ex) {
                        System.out.print("Invalid Upper Channel Layer Message" + channel);
                        System.out.print(ex);
                    }
                }

                // Check for Upper Channel Note Off or Note On with Velocity = 0
                else if ( (command == 0x80) || ((command == 0x90) && (bytes[2] == 0)) ) {
                    uppernoteson--;

                    try {
                        // Layer first/origin Upper Channel if not 0 (off/muted)
                        shortmessage = new ShortMessage();
                        shortmessage.setMessage(command, sharedstatus.getUpper1CHAN() - 1, byteToInt(bytes[1]), byteToInt(bytes[2]));
                        receiver.send(shortmessage, timeStamp);

                        upper14notestrack[(int) bytes[1]] = (byte)0;
                        //System.out.println("Layer Upper index[80]: " + (sharedstatus.getUpper1CHAN()) + ", " + bytes[1] + ", " + bytes[2] + " L=" + trackupper14opennotes + " #=" + uppernoteson);

                        // Check if the same note on Channel 15 is on, and turn it off too
                        boolean upperlayer15enabled = sharedstatus.getUpper1KbdLayerEnabled();
                        if (upperlayer15enabled) {
                            shortmessage = new ShortMessage();
                            shortmessage.setMessage(0x90, (channel + 1), bytes[1], 0);
                            receiver.send(shortmessage, timeStamp);

                            upper15notestrack[bytes[1]] = (byte)0;
                        }

                        // Check if the same note on Channel 16 is on, and turn it off too
                        boolean upperlayer16enabled = sharedstatus.getupper2Kbdlayerenabled();
                        if (upperlayer16enabled) {
                            shortmessage = new ShortMessage();
                            shortmessage.setMessage(0x90, (channel + 2), bytes[1], 0);
                            receiver.send(shortmessage, timeStamp);

                            upper16notestrack[bytes[1]] = (byte)0;
                        }
                    }
                    catch (InvalidMidiDataException ex) {
                        System.out.println("Invalid Upper Channel Layer Message" + channel);
                        System.out.println(ex);
                    }
                }
            }

            // Layer Lower Channel 12
            else if (channel == (byte)(sharedstatus.getLower1CHAN()-1)) {

                // Track open notes to prevent hung notes. Close out all open notes before completing the layer off request
                // Note: Midi Note ON with Velocity = 0, is same as Note OFF
                if ((command == 0x90) && (bytes[2] != 0)) {
                    lowernoteson++;

                    try {
                        // Layer the first/origin Upper Channel if not 0 (off/muted)
                        byte chan = layerLower[chan12idx];
                        if ((chan != 0)) {
                            shortmessage = new ShortMessage();
                            shortmessage.setMessage(command, channel, byteToInt(bytes[1]), byteToInt(bytes[2]));
                            receiver.send(shortmessage, timeStamp);

                            lower12notestrack[(int)bytes[1]] = (byte)1;
                            //System.out.println("Layer Lower index[90]: " + chan + ", " + bytes[1] + ", " + bytes[2]);
                        }

                        // Layer Lower + 1 if not 0 (off muted)
                        chan = layerLower[chan13idx];
                        if ((chan != 0)) {
                            shortmessage = new ShortMessage();
                            shortmessage.setMessage(command, channel + 1, byteToInt(bytes[1]), byteToInt(bytes[2]));
                            receiver.send(shortmessage, timeStamp);

                            lower13notestrack[(int)bytes[1]] = (byte)1;
                            //System.out.println("Layer Lower index[90]: " + chan + ", " + bytes[1] + ", " + bytes[2]);
                        }
                    }
                    catch (InvalidMidiDataException ex) {
                        System.out.print("Invalid Upper Channel Layer Message" + channel);
                        System.out.print(ex);
                    }
                }

                // Close out Lower Channel Open Notes
                else if ( (command == 0x80) || ((command == 0x90) && (bytes[2] == 0)) ) {
                    lowernoteson--;

                    try {
                        shortmessage = new ShortMessage();
                        shortmessage.setMessage(command, channel, byteToInt(bytes[1]), byteToInt(bytes[2]));
                        receiver.send(shortmessage, timeStamp);

                        // Note Off for Channel Lower
                        lower12notestrack[(int) bytes[1]] = (byte)0;
                        //System.out.println("Layer Lower index[80]: " + channel + ", " + bytes[1] + ", " + bytes[2] + " L=" + tracklower12opennotes + " #=" + lowernoteson);

                        // Check if the same note on Channel 13 (Lower + 1) is on, and turn it off too
                        boolean lower13layerenabled = true;
                        if (lower13layerenabled) {
                            shortmessage = new ShortMessage();
                            shortmessage.setMessage(0x80, channel + 1, bytes[1], bytes[2]);
                            receiver.send(shortmessage, timeStamp);
                            lower13notestrack[bytes[1]] = (byte) 0;
                        }
                    }
                    catch (InvalidMidiDataException ex) {
                        System.out.println("Invalid Lower Channel Layer Message" + channel);
                        System.out.println(ex);
                    }
                }
                // Still fell through, send it anyway since it is likely channel Lower 2=13, Upper 2=15, Upper 3=16
                else {
                    //receiver.send(message, timeStamp);

                    System.out.println("Sent Note ON aor Note OFF that did not match: " + channel);
                    return;
                }
            }

        }
    }

    // Check if at least one MIDI OUT device is correctly installed
    public Receiver openMidiReceiver(String seloutdevice) {

        midircv = null;
        MidiDevice selectedDevice;

        // Default to MIDI GM Out device, and wait for Deebach BlackBox to override if present
        sharedstatus.setModuleidx(0);
        config.setSoundModuleIdx(0);

        System.out.println("*** openMidiReceiver " + seloutdevice + " ***");

        try {
            selectedDevice = MidiSystem.getSynthesizer();
            MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();

            if (devices.length == 0) {
                System.err.println("Error: No MIDI OUT sound modules found");
                return midircv;
            }
            else {
                boolean binit = true;

                // Loop through all devices looking to Synth or Sound Modules
                for (MidiDevice.Info dev : devices) {
                    if (MidiSystem.getMidiDevice(dev).getMaxReceivers() == 0) {
                        continue;
                    }

                    if (binit) {
                        // Default to first device and override with preferred
                        selectedDevice = MidiSystem.getMidiDevice(dev);
                        binit = false;

                        System.out.println("First MIDI OUT Device: " + dev.toString());
                    }
                    else
                        System.out.println("Found MIDI OUT Device " + dev.getName());

                    // Check for selected Device match
                    if (dev.getName().contains(seloutdevice)) {
                        selectedDevice = MidiSystem.getMidiDevice(dev);

                        // Note Deebach as more than MIDI GM with own Cubase file
                        if (selectedDevice.getDeviceInfo().getName().contains("Deebach-Blackbox")) {
                            sharedstatus.setModuleidx(1);
                            config.setSoundModuleIdx(1);
                        }

                        System.out.println("Matched preferred MIDI OUT Device: " + dev.toString());
                        break;
                    }
                }
            }

            // Open preferred MIDI OUT device
            try {
                System.out.println("MidiDevices: Closing potentially open MIDI Receiver.");
                selectedDevice.close();

                if (!selectedDevice.isOpen()) {
                    selectedDevice.open();

                    // Found output Device or Synth
                    midircv = selectedDevice.getReceiver();
                    sharedstatus.setRxDevice(midircv);

                    System.out.println("Opened MIDI OUT Device *** " + selectedDevice.getDeviceInfo().getName() + " ***");
                }

            }
            catch(Exception ex) {
                System.out.println("Error Opening MIDI OUT Device *** " + selectedDevice.getDeviceInfo().getName() + " ***");
            }

        }
        catch (MidiUnavailableException ex) {
            System.err.println("Error: Could not open MIDI OUT device: " + ex);
        }

        return midircv;
    }

    // Create a sequence and set all MIDI events
    private Sequence getMidiInputData() {
        int ticksPerQuarterNote = 4;
        Sequence seq;
        try {
            seq = new Sequence(Sequence.PPQ, ticksPerQuarterNote);
            setMidiEvents(seq.createTrack());
        }
        catch (InvalidMidiDataException e) {
            e.printStackTrace();
            return null;
        }
        return seq;
    }

    // Set MIDI events to play a short song
    private void setMidiEvents(Track track) {
        int channel = 9;
        int velocity = 64;
        int note = 35;
        int tick = 0;
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 2);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick + 4);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 7);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick + 8);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 11);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick + 12);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 17);
    }

    // Create a MIDI event and add it to the track
    private void addMidiEvent(Track track, int command, int channel, int data1, int data2, int tick) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(command, channel, data1, data2);
        }
        catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        track.add(new MidiEvent(message, tick));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int byteToInt(byte b) {
        return b & 0xff;
    }

    // Two 7-bit bytes
    private int bytesToInt(byte msb, byte lsb) {
        return byteToInt(msb) * 128 + byteToInt(lsb);
    }

    private int midiChannelToInt(MidiMessage message) {
        return (message.getStatus() & 0x0f) + 1;
    }

    // Set Channel Layering from Button in Performance Scene
    // Layers Upper 1 to Upper 2 and 3, Lower 1 to Lower 2
    // Turn layering on immediately.
    // Queue the turn off request until all Note On/Note Off combinations has been completed.
    public void layerChannel(int chan, boolean layeron) {

        // Layer Upper Keyboard Channels
        if (chan == sharedstatus.getUpper1CHAN()) {
            if (layeron) {
                layerUpper[chan14idx] = (byte)(chan & 0xFF);
                upper14layeron = true;
                uppernoteson = 0;
            }
            else {
                layerUpper[chan14idx] = (byte)(0);
                upper14layeron = false;

                trackupper14opennotes = true;            // Interim state until all notes closed out on keyboard - triggers real layer off
            }
            return;
        }
        else if (chan == sharedstatus.getUpper2CHAN()) {
            if (layeron) {
                layerUpper[chan15idx] = (byte)(chan & 0xFF);
                upper15layeron = true;
                uppernoteson = 0;
            }
            else {
                layerUpper[chan15idx] = (byte)(0);
                upper15layeron = false;

                trackupper15opennotes = true;            // Interim state until all notes closed out on keyboard - triggers real layer off
            }
            return;
        }
        else if (chan == sharedstatus.getUpper3CHAN()) {
            if (layeron) {
                layerUpper[chan16idx] = (byte) (chan & 0xFF);
                upper15layeron = true;
                uppernoteson = 0;
            }
            else {
                layerUpper[chan16idx] = (byte)(0);
                upper16layeron = false;

                trackupper16opennotes = true;            // Interim state until all notes closed out on keyboard - triggers real layer off
            }
            return;
        }

        // Layer Lower Keyboard Channels
        else if (chan == sharedstatus.getLower1CHAN()) {
            if (layeron) {
                layerLower[chan12idx] = (byte) (chan & 0xFF);
                lower12layeron = true;
                lowernoteson = 0;
            }
            else {
                layerLower[chan12idx] = (byte) (0);
                lower12layeron = false;
                //tracklower12opennotes = 1;            // Interim state until all notes closed out on keyboard - triggers real layer off
            }
            return;
        }
        else if (chan == sharedstatus.getLower2CHAN()) {
            if (layeron) {
                layerLower[chan13idx] = (byte) (chan & 0xFF);
                lower13layeron = true;
                lowernoteson = 0;
            }
            else {
                layerLower[chan13idx] = (byte) (0);
                lower13layeron = false;

                tracklower13opennotes = true;            // Interim state until all notes closed out on keyboard - triggers real layer off
            }
            return;
        }
    }

    public void createNotesTracker() {
        //http://newt.phys.unsw.edu.au/jw/notes.html

        for (int i = 0; i < 128; i++) {
            lower12notestrack[i] = 0;
        }
        for (int i = 0; i < 128; i++) {
            lower13notestrack[i] = 0;
        }

        for (int i = 0; i < 128; i++) {
            upper14notestrack[i] = 0;
        }
        for (int i = 0; i < 128; i++) {
            upper15notestrack[i] = 0;
        }
        for (int i = 0; i < 128; i++) {
            upper16notestrack[i] = 0;
        }
    }

}
