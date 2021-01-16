package amidifx.utils;

import amidifx.models.SharedStatus;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class ArduinoUtils {

    SerialPort activePort;
    SerialPort[] ports;

    int baudRate = 115200;
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
        this.getPort();
        if (usePort != -1) {
            this.setPort(usePort);

            byte[] buffer = {'A', 'M', 'I', 'D', 'I', 'F', 'X'};
            this.writeData(buffer);
        }
        else
            System.out.println("ArduinoUtils: No COM Port for Seeeduino ARM Processor detected");
    }

    public void getPort() {

        //System.out.println("ArduinoUtils: Listing Ports");

        ports = SerialPort.getCommPorts();

        numberPort = 0;
        for (SerialPort port : ports) {
            System.out.print("ArduinoUtils Port: " + numberPort + " - " + port.getDescriptivePortName() + " ");
            System.out.println(port.getPortDescription());

            // Seeeduino is the Port the onboard COM Port
            if (port.getPortDescription().contains("Seeeduino"))
                usePort = numberPort;

            numberPort++;
        }
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

    public void closePort() {
        System.out.println("ArduinoUtils: closePort()");

        if (hasPort) activePort.closePort();
    }

    public void writeData(byte[] buffer) {
        System.out.println("ArduinoUtils: writeData()");

        if (!hasPort) return;

        long bytesToWrite = buffer.length;
        activePort.writeBytes(buffer, bytesToWrite);
    }

    public void readData() {
        System.out.println("ArduinoUtils: readData()");

        if (!hasPort) return;

    }

}
