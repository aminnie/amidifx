package amidifx.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {

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
    private AppConfig() { }

    public boolean loadProperties() {

        System.out.println("Loading Properties from disk: " + configPath);

        try {
            configProps.loadFromXML(new FileInputStream(configPath));

            // get the property value and print it out
            System.out.println("Config: In device is " +  configProps.getProperty("indevice"));
            System.out.println("Config: Out device is " +  configProps.getProperty("outdevice"));
        }
        catch (FileNotFoundException ex) {
            System.out.println("File not found exception: " + configPath);
            return false;
        }
        catch (IOException ex) {
            System.out.println("File read exception: " + configPath);
            return false;
        }

        return true;
    }

    public boolean saveProperties() {

        try {
            configProps.storeToXML(new FileOutputStream(configPath), "Saved to XML file");

            // get the property value and print it out
            System.out.println("Saving Properties to disk: " + configPath);
            System.out.println("Config: App Name " +  configProps.getProperty("appname"));
            System.out.println("Config: App Version " +  configProps.getProperty("appversion"));
            System.out.println("Config: App Date " +  configProps.getProperty("appdate"));
            System.out.println("Config: In device is " +  configProps.getProperty("indevice"));
            System.out.println("Config: Out device is " +  configProps.getProperty("outdevice"));


        }
        catch (IOException ex) {
            System.out.println("Config File write failed: " + configPath);
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

        System.out.println("Property indevice set to:" + configProps.getProperty("indevice"));
    }

    // Get selected Out Midi device - Sound Module
    public String getOutDevice() {
        return configProps.getProperty("outdevice");
    }

    // Set selected Midi Out device - Keyboard
    public void setOutDevice(String outdevice) {
        configProps.setProperty("outdevice", outdevice);

        System.out.println("Property outdevice set to:" + configProps.getProperty("outdevice"));
    }

    // Get selected Out Midi device - Sound Module
    public String getAppName() {
        return configProps.getProperty("appname");
    }

    // Get selected Out Midi device - Sound Module
    public String getSoundModule(int idx) {
        String strmodule;

        switch (idx) {
            case 1:
                strmodule = configProps.getProperty("sndmodfil1");
            default:
                strmodule = configProps.getProperty("sndmodfil0");
        }

        return strmodule;
    }

    public void setSoundModuleIdx(int moduleidx) {
        configProps.setProperty("moduleidx", Integer.toString(moduleidx));

        System.out.println("Property moduleidx set to:" + configProps.getProperty("moduleidx"));
    }

    public int getSoundModuleIdx() {
        Integer moduleidx = 0;

        try {
            String moduleidxstr = configProps.getProperty("moduleidx");
            moduleidx = new Integer(moduleidxstr);
        }
        catch (Exception ex) {
            System.err.println("Error read moduleidx from AppConfig file");
            System.err.println(ex);
        }

        return moduleidx;
    }

    public String getSongsFile() {
        return configProps.getProperty("songsfile");
    }

}

