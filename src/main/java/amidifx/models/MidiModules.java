package amidifx.models;

import java.util.ArrayList;

public class MidiModules {

    private static final String MID_PATCHFILE0 = "maxplus.dat";
    private static final String MID_PATCH0 = "Deebach Blackbox";
    private static final String MID_PATCHFILE1 = "midigm.dat";
    private static final String MID_PATCH1 = "MIDI GM (Int)";

    final ArrayList moduleslist = new ArrayList<String>();
    final ArrayList modulesfiles = new ArrayList<String>();

    // Preset with two modules currently supported.
    // To do: Read from Config file
    public MidiModules() {
        moduleslist.add(MID_PATCH0);
        moduleslist.add(MID_PATCH1);

        modulesfiles.add(MID_PATCHFILE0);
        modulesfiles.add(MID_PATCHFILE1);

        // Default to Deebach MaxPlus until not found
        //SharedStatus sharedstatus = SharedStatus.getInstance();
        //sharedstatus.setModuleidx(1);
    }

    public ArrayList getModuleslist() {
        return moduleslist;
    }

    public String getModuleName(int moduleidx) {
        return moduleslist.get(moduleidx).toString();
    }

    public String getModuleFile(int moduleidx) {
        return modulesfiles.get(moduleidx).toString();
    }
}

