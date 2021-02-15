package amidifx.utils;

import amidifx.models.SharedStatus;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;

public class MidiUtils {

    //private String selindevice = "2- Seaboard RISE 49";
    //private String seloutdevice = "Deebach-Blackbox";
    private String selindevice = "default";
    private String seloutdevice = "default";

    final List<StatusMidiDevice> InDeviceList = new ArrayList<>();
    final List<StatusMidiDevice> OutDeviceList = new ArrayList<>();

    public class StatusMidiDevice {
        boolean isactive;
        MidiDevice device;

        StatusMidiDevice(MidiDevice device, boolean isactive) {
            this.device = device;
            this.isactive = isactive;

            System.out.println("Adding MIDI Device: " + toString());
        }

        public String getDevice() {
            return device.getDeviceInfo().getName();
        }

        @Override
        public String toString() {
            String devicestring = "Device Status Active:" + isactive + " Device:" + device.getDeviceInfo().toString();
            return devicestring;
        }
    }

    // List all Midi Devices detected
    public void loadMidiDevices() {
        MidiDevice.Info[] deviceInfo = MidiSystem.getMidiDeviceInfo();
        if (deviceInfo.length == 0) {
            System.out.println("No MIDI devices found");
            return;
        }

        for (MidiDevice.Info info : deviceInfo) {
            System.out.println("**********************");
            System.out.println("Device name: " + info.getName());
            System.out.println("Description: " + info.getDescription());
            System.out.println("Vendor: " + info.getVendor());
            System.out.println("Version: " + info.getVersion());

            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                addDeviceType(device, false);

                System.out.println("Maximum receivers: " + maxToString(device.getMaxReceivers()));
                System.out.println("Maximum transmitters: " + maxToString(device.getMaxTransmitters()));
            }
            catch (MidiUnavailableException e) {
                System.out.println("Can't get MIDI device");
                e.printStackTrace();
            }
        }
    }

    /*
     * Add MIDI In and Out Devices to respective lists for future lookup
     * Flag (override) named 1x IN and 1 x Out Device as active
     */
    private void addDeviceType(MidiDevice device, boolean isactive) {

        // Create instance of Shared Status to report back to Scenes
        SharedStatus sharedStatus = SharedStatus.getInstance();
        selindevice = sharedStatus.getSelInDevice();
        seloutdevice = sharedStatus.getSelOutDevice();

        if (device instanceof Sequencer) {
            System.out.println("This is a sequencer");
            InDeviceList.add(new StatusMidiDevice(device, false));
        }
        else if (device instanceof Synthesizer) {
            System.out.println("This is a synthesizer");
            OutDeviceList.add(new StatusMidiDevice(device, false));
        }
        else {
            System.out.print("This is a MIDI port ");
            if (device.getMaxReceivers() != 0) {
                System.out.println("IN ");

                //boolean isactive = false;
                if ( device.getDeviceInfo().getName().contains(selindevice) ) {
                    isactive = true;
                }
                InDeviceList.add(new StatusMidiDevice(device, isactive));
            }
            if (device.getMaxTransmitters() != 0) {
                System.out.println("OUT ");

                //boolean isactive = false;
                if ( device.getDeviceInfo().getName().contains(seloutdevice) ) {
                    isactive = true;
                }
                OutDeviceList.add(new StatusMidiDevice(device, isactive));
            }
        }
    }

    private String maxToString(int max) {
        return max == -1 ? "Unlimited" : String.valueOf(max);
    }

    public List<StatusMidiDevice> listInDevices() {
        System.out.println("**********************");
        for (StatusMidiDevice statusdevice : InDeviceList ) {
            System.out.println("MIDI In:" + statusdevice.toString());
        }

        return InDeviceList;
    }

    public List<StatusMidiDevice> listOutDevices() {
        System.out.println("**********************");
        for (StatusMidiDevice statusdevice : OutDeviceList ) {
            System.out.println("MIDI Out:" + statusdevice.toString());
        }

        return OutDeviceList;
    }

}
