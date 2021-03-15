package amidifx.utils;

import amidifx.models.SharedStatus;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class ArduinoUtils {

    SerialPort activePort;
    SerialPort[] ports;

    int baudRate = 57600; //115200;
    int numberPort = 0;
    int usePort = -1;
    boolean hasPort = false;

    // Static variable single_instance of type PlayMidi
    private static ArduinoUtils single_ArduinoInstance = null;

    // Create instance of Shared Status to report back to Scenes
    final SharedStatus sharedStatus = SharedStatus.getInstance();

    // Static method to create singleton instance of PlayMidi class
    public synchronized static ArduinoUtils getInstance()
    {
        if (single_ArduinoInstance == null) {
            single_ArduinoInstance = new ArduinoUtils();

            System.out.println("PlayMidi: Creating instance ArduinoUtils");
        }

        return single_ArduinoInstance;
    }


    // *** Make constructor private for Singleton ***
    // Initialize Serial Port to Arduino Controller
    private ArduinoUtils() {

        this.hasPort = false;

        // Ignore HW interface based on Config setting
        AppConfig appconfig = AppConfig.getInstance();
        if (!appconfig.getUSBHardeware()) {
            usePort = -1;
            System.out.println("ArduinoUtils Config: External MIDI Hardware ignored");
            return;
        }

        usePort = this.getPort();

        if (usePort != -1) {
            this.setPort(usePort);

            byte[] buffer = {'A', 'M', 'I', 'D', 'I', 'F', 'X'};

            //this.writeData(buffer);
            writeSysexData((byte)0, buffer);

        }
        else
            System.out.println("ArduinoUtils: No COM Port for Seeeduino ARM Processor detected");
    }


    public int getPort() {

        int getport = -1;

        //System.out.println("ArduinoUtils: Listing Ports");

        ports = SerialPort.getCommPorts();

        numberPort = 0;
        for (SerialPort port : ports) {
            System.out.print("ArduinoUtils Port: " + numberPort + " - " + port.getDescriptivePortName() + " ");
            System.out.println(port.getPortDescription());

            // Seeeduino is the Port the onboard COM Port
            if (port.getPortDescription().contains("Seeeduino")) {
                getport = numberPort;
                break;
            }

            // Otherwise assume first USB Serial Port for remote Arduino controller
            else if (port.getPortDescription().contains("USB Serial")) {
                getport = numberPort;
            }

            numberPort++;
        }

        return getport;
    }


    public boolean setPort(int portIndex) {

        System.out.println("ArduinoUtils: Setting Port: " + portIndex);

        if (portIndex > (numberPort - 1)) {
            System.err.println("ArduinoUtils: Port not available: " + portIndex);
            return false;
        }
        hasPort = true;

        activePort = ports[portIndex];
        activePort.setBaudRate(baudRate);

        if (activePort.openPort())
            System.out.println("ArduinoUtils: " + activePort.getPortDescription() + " port opened.");

        activePort.addDataListener(new SerialPortDataListener() {

            @Override
            public void serialEvent(SerialPortEvent event) {
                int size = event.getSerialPort().bytesAvailable();
                byte[] buffer = new byte[size];
                event.getSerialPort().readBytes(buffer, size);

                for(byte b:buffer)
                    System.out.print((char)b);
            }

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }
        });

        return true;
    }

    // Check if an ARM Port was found
    public boolean hasARMPort() {
        return hasPort;
    }


    public void closePort() {
        System.out.println("ArduinoUtils: closePort()");

        if (hasPort) activePort.closePort();
    }


    public boolean writeData(byte[] buffer) {

        System.out.println("ArduinoUtils: writeData()");

        if (!hasPort) return false;

        long bytesToWrite = buffer.length;
        activePort.writeBytes(buffer, bytesToWrite);

        return true;
    }


    // Creates and writes Sysex Message buffer that starts with 0xF0 and ends with 0xF7
    public boolean writeSysexData(byte messagetype, byte[] messagebuffer) {

        //System.out.println("ArduinoUtils: writeSysexData() Message Tyoe: " + messagetype);

        if (!hasPort) return false;

        SysexUtils sysex = new SysexUtils();
        byte[] sysexbuffer = sysex.getSysexMessage(messagetype, messagebuffer);

        long bytesToWrite = sysexbuffer.length;
        activePort.writeBytes(sysexbuffer, bytesToWrite);

        return true;
    }


    // Creates and writes Keyoard Layering Sysex Message buffer that starts with 0xF0 and ends with 0xF7
    public boolean writeLayerSysexData(byte messagetype, byte[] messagebuffer) {

        //System.out.println("ArduinoUtils: writeLayerSysexData() Message Tyoe: " + messagetype);

        if (!hasPort) return false;

        SysexUtils sysex = new SysexUtils();
        byte[] sysexbuffer = sysex.getLayerSysexMessage(messagetype, messagebuffer);

        long bytesToWrite = sysexbuffer.length;
        activePort.writeBytes(sysexbuffer, bytesToWrite);

        return true;
    }


    // Creates and writes Keyoard Layering Sysex Message buffer that starts with 0xF0 and ends with 0xF7
    public boolean lefthandLayerSysexData(boolean l1pressed, boolean l2pressed) {

        SysexUtils sysex = new SysexUtils();
        byte[] sysexbuffer = sysex.getlefthandLayerSysexMessage(l1pressed, l2pressed);

        long bytesToWrite = sysexbuffer.length;
        activePort.writeBytes(sysexbuffer, bytesToWrite);

        return true;
    }

    // Creates and writes Keyoard Layering Sysex Message buffer that starts with 0xF0 and ends with 0xF7
    public boolean righthandLayerSysexData(boolean r1pressed, boolean r2pressed, boolean r3pressed) {

        SysexUtils sysex = new SysexUtils();
        byte[] sysexbuffer = sysex.getrighthandLayerSysexMessage(r1pressed, r2pressed, r3pressed);

        long bytesToWrite = sysexbuffer.length;
        activePort.writeBytes(sysexbuffer, bytesToWrite);

        return true;
    }

    public void readData() {
        System.out.println("ArduinoUtils: readData()");

        if (!hasPort) return;
    }

}
