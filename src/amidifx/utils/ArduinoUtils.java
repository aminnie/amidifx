package amidifx.utils;

//import com.fazecast.jSerialComm.*;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class ArduinoUtils {

    SerialPort activePort;
    SerialPort[] ports;

    public void listPorts() {

        //System.out.println("ArduinoUtils: Listing Ports");

        ports = SerialPort.getCommPorts();

        int i = 0;
        for (SerialPort port : ports) {
            System.out.print("ArduinoUtils Port: " + i + ". " + port.getDescriptivePortName() + " ");
            System.out.println(port.getPortDescription());
            i++;
        }
    }

    public void setPort(int portIndex) {

        System.out.println("ArduinoUtils: Setting Port: " + portIndex);

        activePort = ports[portIndex];

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
    }

    public void writeData() {
        System.out.println("ArduinoUtils: writeData()");

        byte[] buffer = {'A', 'B', 'C'};
        long bytesToWrite = 3;

        activePort.writeBytes(buffer, bytesToWrite);
    }

    public void readData() {
    }

}
