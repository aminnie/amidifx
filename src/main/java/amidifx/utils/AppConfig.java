package amidifx.utils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Properties;

public class AppConfig {

    private static final String APP_DIRECTORY = "C:/amidifx/";
    private static final String CFG_DIRECTORY = "C:/amidifx/config/";

    Properties configProps = new Properties();

    //String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    //String configPath = rootPath + "AppConfig.xml";
    String configPath = CFG_DIRECTORY + "AppConfig.xml";

    // Static variable single_instance of type PlayMidi
    private static AppConfig single_AppConfig_Instance = null;

    // Static method to create singleton instance of PlayMidi class
    public synchronized static AppConfig getInstance() {
        if (single_AppConfig_Instance == null) {
            single_AppConfig_Instance = new AppConfig();

            System.out.println("AppConfig: Creating single instance AppConfig");
        }

        return single_AppConfig_Instance;
    }

    // *** Make constructor private for Singleton ***
    private AppConfig() {

        if (!this.loadProperties()) {
            System.err.println("AppConfig: Failed to load AppConfig file!");
            System.exit(-1);
        }
    }

    private boolean loadProperties() {

        System.out.println("AppConfig: Loading Properties from: " + configPath);

        try {
            configProps.loadFromXML(new FileInputStream(configPath));

            // get the property value and print it out
            System.out.println("AppConfig: In device is " +  configProps.getProperty("indevice"));
            System.out.println("AppConfig: Out device is " +  configProps.getProperty("outdevice"));
        }
        catch (FileNotFoundException ex) {
            System.err.println("AppConfig: File not found exception: " + configPath);
            return false;
        }
        catch (IOException ex) {
            System.err.println("AppConfig: File read exception: " + configPath);
            return false;
        }

        return true;
    }

    public boolean saveProperties() {

        try {
            configProps.storeToXML(new FileOutputStream(configPath), "Config XML file save");

            // get the property value and print it out
            System.out.println("AppConfig: Saving Properties to: " + configPath);
            System.out.println("AppConfig: In device is " +  configProps.getProperty("indevice"));
            System.out.println("AppConfig: Out device is " +  configProps.getProperty("outdevice"));

        }
        catch (IOException ex) {
            System.err.println("AppConfig: File write failed: " + configPath);
            return false;
        }

        return true;
    }

    // Get selected In Midi device - Keyboard
    public String getInDevice() {
        return configProps.getProperty("indevice");
    }

    // Set selected Midi In device - Keyboard
    public void setInDevice(String indevice) {
        configProps.setProperty("indevice", indevice);

        System.out.println("AppConfig: Property indevice set to " + configProps.getProperty("indevice"));
    }

    // Get selected Out Midi device - Sound Module
    public String getOutDevice() {
        return configProps.getProperty("outdevice");
    }

    // Set selected Midi Out device - Keyboard
    public void setOutDevice(String outdevice) {
        configProps.setProperty("outdevice", outdevice);

        System.out.println("AppConfig: Property outdevice set to " + configProps.getProperty("outdevice"));
    }

    // Get selected Out Midi device - Sound Module
    public String getAppName() {
        return configProps.getProperty("appname");
    }

    // Check if USB Hardware Controller is connected
    public boolean getUSBHardeware() {

        if (configProps.getProperty("usbhardware").equals("true"))
            return true;
        else
            return false;
    }

    // Check if Master Volume is sourced from Organ or MidiCC based Volume pedal
    public String getMidiCCVol() {
        return configProps.getProperty("midiccvol");
    }

    // Get selected Out Midi device - Sound Module
    public String getSoundModule(int idx) {
        String strmodule;

        switch (idx) {
            case 1:
                strmodule = configProps.getProperty("sndmodfil1");
            case 2:
                strmodule = configProps.getProperty("sndmodfil2");
            default:
                // Default to Midi GM file
                strmodule = configProps.getProperty("sndmodfil0");
        }

        return strmodule;
    }

    public void setControllerTitle(String controllertitle) {
        configProps.setProperty("controllertitle", controllertitle);
    }

    public String getControllerTitle() {
        return configProps.getProperty("controllertitle");
    }

    public void setSoundModuleIdx(int moduleidx) {
        configProps.setProperty("moduleidx", Integer.toString(moduleidx));

        System.out.println("AppConfig: Property moduleidx set to " + configProps.getProperty("moduleidx"));
    }

    public int getSoundModuleIdx() {
        Integer moduleidx = 0;

        try {
            String moduleidxstr = configProps.getProperty("moduleidx");
            moduleidx = new Integer(moduleidxstr);

            // Ignore incorrect moduleidx values from AppConfig. Default to MIDIGM is so
            if ((moduleidx < -1) || (moduleidx > 2)) moduleidx = 0;
        }
        catch (Exception ex) {
            System.err.println("AppConfig: Error read moduleidx from AppConfig file");
            System.err.println(ex);
        }

        return moduleidx;
    }

    public String getSongsFile() {
        return configProps.getProperty("songsfile");
    }

    public byte getExpressionChannel() {
        Byte expchannelidx = 0;

        try {
            String channelidxstr = configProps.getProperty("midiexpchan");
            expchannelidx = new Byte(channelidxstr);

            // Ignore incorrect values: 0 = not used, 1 - 16 = valid channels
            if ((expchannelidx < -1) || (expchannelidx > 16)) expchannelidx = 0;
        }
        catch (Exception ex) {
            System.err.println("AppConfig: Error read MIDI Expression CHannel from AppConfig file");
            System.err.println(ex);
        }

        return (byte)(expchannelidx - 1);
    }

    public byte getSoloChannel() {
        byte channelidx = 0;

        try {
            String channelidxstr = configProps.getProperty("midisolo");
            channelidx = new Byte(channelidxstr);

            // Ignore incorrect values: 0 = not used, 1 - 16 = valid channels
            if ((channelidx < -1) || (channelidx > 16)) channelidx = 0;
        }
        catch (Exception ex) {
            System.err.println("AppConfig: Error read MIDI Expression CHannel from AppConfig file");
            System.err.println(ex);
        }

        return (byte)(channelidx - 1);
    }

    public byte getBassChannel() {
        Byte channelidx = 0;

        try {
            String channelidxstr = configProps.getProperty("midibass");
            channelidx = new Byte(channelidxstr);

            // Ignore incorrect values: 0 = not used, 1 - 16 = valid channels
            if ((channelidx < -1) || (channelidx > 16)) channelidx = 0;
        }
        catch (Exception ex) {
            System.err.println("AppConfig: Error read MIDI Expression CHannel from AppConfig file");
            System.err.println(ex);
        }

        return (byte)(channelidx - 1);
    }

    public byte getDrumChannel() {
        Byte channelidx = 0;

        try {
            String channelidxstr = configProps.getProperty("mididrums");
            channelidx = new Byte(channelidxstr);

            // Ignore incorrect values: 0 = not used, 1 - 16 = valid channels
            if ((channelidx < -1) || (channelidx > 16)) channelidx = 0;
        }
        catch (Exception ex) {
            System.err.println("AppConfig: Error read MIDI Drum CHannel from AppConfig file");
            System.err.println(ex);
        }

        return (byte)(channelidx - 1);
    }

    public byte getUpper1Channel() {
        Byte channelidx = 0;

        try {
            String channelidxstr = configProps.getProperty("midiupper1");
            channelidx = new Byte(channelidxstr);

            // Ignore incorrect values: 0 = not used, 1 - 16 = valid channels
            if ((channelidx < -1) || (channelidx > 16)) channelidx = 0;
        }
        catch (Exception ex) {
            System.err.println("AppConfig: Error read MIDI Upper 1 CHannel from AppConfig file");
            System.err.println(ex);
        }

        return (byte)(channelidx - 1);
    }

    public byte getUpper2Channel() {
        Byte channelidx = 0;

        try {
            String channelidxstr = configProps.getProperty("midiupper2");
            channelidx = new Byte(channelidxstr);

            // Ignore incorrect values: 0 = not used, 1 - 16 = valid channels
            if ((channelidx < -1) || (channelidx > 16)) channelidx = 0;
        }
        catch (Exception ex) {
            System.err.println("AppConfig: Error read MIDI Upper 2 CHannel from AppConfig file");
            System.err.println(ex);
        }

        return (byte)(channelidx - 1);
    }

    public byte getUpper3Channel() {
        Byte channelidx = 0;

        try {
            String channelidxstr = configProps.getProperty("midiupper3");
            channelidx = new Byte(channelidxstr);

            // Ignore incorrect values: 0 = not used, 1 - 16 = valid channels
            if ((channelidx < -1) || (channelidx > 16)) channelidx = 0;
        }
        catch (Exception ex) {
            System.err.println("AppConfig: Error read MIDI Upper 3 CHannel from AppConfig file");
            System.err.println(ex);
        }

        return (byte)(channelidx - 1);
    }

    public byte getLower1Channel() {
        Byte channelidx = 0;

        try {
            String channelidxstr = configProps.getProperty("midilower1");
            channelidx = new Byte(channelidxstr);

            // Ignore incorrect values: 0 = not used, 1 - 16 = valid channels
            if ((channelidx < -1) || (channelidx > 16)) channelidx = 0;
        }
        catch (Exception ex) {
            System.err.println("AppConfig: Error read MIDI Lower 1 CHannel from AppConfig file");
            System.err.println(ex);
        }

        return (byte)(channelidx - 1);
    }

    public byte getLower2Channel() {
        Byte channelidx = 0;

        try {
            String channelidxstr = configProps.getProperty("midilower2");
            channelidx = new Byte(channelidxstr);

            // Ignore incorrect values: 0 = not used, 1 - 16 = valid channels
            if ((channelidx < -1) || (channelidx > 16)) channelidx = 0;
        }
        catch (Exception ex) {
            System.err.println("AppConfig: Error read MIDI Lower 2 CHannel from AppConfig file");
            System.err.println(ex);
        }

        return (byte)(channelidx - 1);
    }

    public boolean getApplicationLock() {

        try {
            RandomAccessFile randomFile = new RandomAccessFile(APP_DIRECTORY + "applock.txt","rw");

            FileChannel channel = randomFile.getChannel();

            if (channel.tryLock() == null) {
                System.out.println("AppConfig: AMIDIFX already Running...");
                return false;
            }

            System.out.println("AppConfig: AMIDIFX application locked.");
        }
        catch (Exception ex) {
            System.err.println("AppConfig: AMDIDIFX Unable to obtain Application lock error");
            System.err.println(ex.toString());
        }

        return true;
    }
}

