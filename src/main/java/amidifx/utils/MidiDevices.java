package amidifx.utils;

import amidifx.models.SharedStatus;

import javax.sound.midi.*;

public class MidiDevices {

    private static String TRANS_DEV_NAME = "javax.sound.midi.Transmitter#Microsoft MIDI Mapper";
    private static String SYNTH_DEV_NAME = "javax.sound.midi.Synthesizer#Microsoft MIDI Mapper";
    private static String SEQ_DEV_NAME = "default";

    /** See {@link MidiSystem} for other classes */
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

    // Static variable single_instance of type PlayMidi
    private static MidiDevices single_MidiDevices_Instance = null;

    // Static method to create singleton instance of PlayMidi class
    public synchronized static MidiDevices getInstance() {
        if (single_MidiDevices_Instance == null) {
            single_MidiDevices_Instance = new MidiDevices();

            System.out.println("PlayMidi: Creating instance StatusBar");
        }

        return single_MidiDevices_Instance;
    }

    // *** Make constructor private for Singleton ***
    private MidiDevices() { }

    public int createMidiDevices(String selindevice, String seloutdevice) {

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
                System.err.print("Error: Unable to open selected MIDI OUT device: " + seloutdevice);

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

                System.out.println("Ready to play your USB keyboard...");
            }
            else
                System.out.println("No musical keyboard connected! Please connect USB keyboard proceed.");

            // Get default sequencer, if it exists
            sequencer = getSequencer();
            if (sequencer == null) {
                System.err.print("Error: Unable to open Sequencer device: " + sequencer.getDeviceInfo().getName());

                sharedstatus.setStatusText("Error: Unable to open Sequencer device: " + sequencer.getDeviceInfo().getName());
                return -2;
            }

            sequencer.open();
            sequencer.getTransmitter().setReceiver(midircv);

            // Demo Play Sequencer Song in parallel with Keyboard input
            playDemoSequence(1);

            sequencer.close();
        }
        catch (Exception e) {     //// MidiUnavailableException
            System.err.println("Error getting receiver from synthesizer");
            e.printStackTrace();
        }

        return 0;
    }

    // Play Song on Sequencer
    private void playDemoSequence(int replaycnt) {

        for (int i = 1; i <= replaycnt; i++) {
            System.out.println("Starting Demo Sequencer Play:" + i);

            sequencer.setTempoInBPM(144.0f);

            try {
                sequencer.setSequence(getMidiInputData());
            }
            catch (InvalidMidiDataException e1) {
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
        }
    }


    /**
     * Return a specific synthesizer object by setting the system property, otherwise the default
     */
    private Synthesizer getSynthesizer() {
        if (! SYNTH_DEV_NAME.isEmpty() || ! "default".equalsIgnoreCase(SYNTH_DEV_NAME)) {
            System.setProperty(SYNTH_PROP_KEY, SYNTH_DEV_NAME);
        }

        try {
            return MidiSystem.getSynthesizer();
        }
        catch (MidiUnavailableException e) {
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

        if (! TRANS_DEV_NAME.isEmpty() && ! "default".equalsIgnoreCase(TRANS_DEV_NAME)) {
            System.setProperty(TRANS_PROP_KEY, TRANS_DEV_NAME);
        }

        try {
            return MidiSystem.getTransmitter();
        }
        catch (MidiUnavailableException e) {
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
        }
        catch (MidiUnavailableException e) {
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
            if (( status == 0xf8 ) || ( status == 0xfe )) {
                receiver.send(message, timeStamp);
                return;
            }

            //System.out.printf("%d - Status: 0x%s", timeStamp, Integer.toHexString(status));

            // These statuses have MIDI channel numbers and data (except 0xf0 thru 0xff)
            // Strip channel number out of status
            int leftNibble = status & 0xf0;
            switch (leftNibble) {
                case 0x80: //displayNoteOff(message);
                case 0x90: //displayNoteOn(message);
                    receiver.send(message, timeStamp);
                    //layerOutMessages(message, timeStamp);
                    break;
                case 0xa0: //displayKeyPressure(message);
                case 0xb0: //displayControllerChange(message);
                case 0xc0: //displayProgramChange(message);
                case 0xd0: //displayChannelPressure(message);
                case 0xe0: //displayPitchBend(message);
                case 0xf0:
                    receiver.send(message, timeStamp);
                    break;
                default:
                    // Not recognized, but forward
                    receiver.send(message, timeStamp);
            }
        }

        // Play Keyboard IN messages and perform all Channel OUT layering as needed
        private void layerOutMessages(MidiMessage message, long timeStamp) {

            ShortMessage shortmessage;

            if (message.getLength() < 3 || message.getLength() % 2 == 0) {
                System.out.println("Unable to Layer/Output Bad MIDI message");
                return;
            }

            // Now dissect to determine if Layering is needed and forward in layered channels
            byte[] bytes = message.getMessage();
            int command = message.getStatus() & 0xf0;
            int channel = message.getStatus() & 0x0f;

            try {
                // Layer the first/origin Channel
                int chan = channelOutStruct[2];
                if (chan != 0) {
                    shortmessage = new ShortMessage();
                    shortmessage.setMessage(command, chan - 1, byteToInt(bytes[1]) + 4, byteToInt(bytes[2]));
                    receiver.send(shortmessage, timeStamp);

                    //System.out.println("Layer Channel index[0]: " + chan);
                }

                // Lookup and layer the remaining up to 9 channels until a 0 out is found
                int startidx = 4;
                int idx = 0;

                chan = channelOutStruct[startidx];
                if ((chan < 0) || (chan > 16)) return;

                while ((chan != 0) && (idx < 10)) {
                    shortmessage = new ShortMessage();
                    shortmessage.setMessage(command, chan - 1, byteToInt(bytes[1]), byteToInt(bytes[2]));
                    receiver.send(shortmessage, timeStamp);

                    // Read next channel mapping
                    int offsetidx = startidx + (idx++ * 2);
                    if (offsetidx > (channelOutStruct.length - 1))
                        break;

                    chan = channelOutStruct[offsetidx];
                    if ((chan <= 0) || (chan > 16)) return;

                    //System.out.println("Layer Channel index[" + idx + "]: " + chan);
                }
            }
            catch (InvalidMidiDataException ex) {
                System.out.print("Invalid Channel Layer Message" + channel);
                System.out.print(ex);
            }
        }

    }

    // Check if at least one MIDI OUT device is correctly installed
    public Receiver openMidiReceiver(String seloutdevice) {

        midircv = null;
        MidiDevice selectedDevice;

        // Default to MIDI GM Out device, and wait for Deeback to override if present
        sharedstatus.setModuleidx(0);
        config.setSoundModuleIdx(0);

        System.out.println("** openMidiReceiver " + seloutdevice + " **");

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
                    } else
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

    // Set MIDI events to play "Mary Had a Little Lamb"
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

}