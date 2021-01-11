package amidifx.utils;

import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
//import jssc.SerialNativeInterface;
import jssc.SerialPortList;

import java.io.IOException;

// https://github.com/reapzor/FiloFirmata
// https://forum.arduino.cc/index.php?topic=281994.0

public class Firmata {

    IODevice device;

    public boolean initFirmataDevice() {


        String[] portNames;
        portNames = SerialPortList.getPortNames();
        for (String portName : portNames) {
            System.out.println(portName);
        }

        System.out.println("Firmata initFirmataDevice: Starting");

        device = new FirmataDevice("/dev/ttyUSB0");

        try {
            device.start();

            Thread.sleep(500);
            if (!device.isReady()) {
                System.out.println("Firmata initFirmataDevice: Device not ready!");
                return false;
            }
        }
        catch (IOException | InterruptedException e) {
            System.out.println("Firmata initFirmataDevice Error: Device start failed!");
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public boolean setFirmataPin(int pin) {

        try {
            device.getPin(pin).setMode(Pin.Mode.OUTPUT);
        }
        catch (IllegalArgumentException | IOException e) {
            System.out.println("Firmata setFirmataPin Error: unable to set Pin 4 to output");
            e.printStackTrace();
            return false;
        }
        System.out.println("Firmata setFirmataPin: Pin " + pin + " set to OUTPUT mode.");

        try {
            for (int i = 0; i < 1000; i++) {
                device.getPin(pin).setValue(1);
                System.out.println("Firmata setFirmataPin: Pin " + pin + " set to 1.");
                Thread.sleep(500);
                device.getPin(pin).setValue(0);
                System.out.println("Firmata setFirmataPin: Pin " + pin + " set to 0.");
                Thread.sleep(500);
            }
        } catch (IllegalStateException | IOException | InterruptedException e) {
            System.err.println("Firmata setFirmataPin Error: Device pin " + pin + " test.");
            e.printStackTrace();
            return false;
        }
        System.out.println("Firmata setFirmataPin: Pin " + pin + " set to logic HIGH.");

        return true;
    }

}
