package amidifx.models;

import java.util.ArrayList;

public class MidiModules {

    private static final String MID_PATCHFILE0 = "midigm.dat";
    private static final String MID_PATCH0 = "MIDI GM (Int)";
    private static final String MID_PATCHFILE1 = "maxplus.dat";
    private static final String MID_PATCH1 = "Deebach Blackbox";

    final ArrayList moduleslist = new ArrayList<String>();
    final ArrayList modulesfiles = new ArrayList<String>();

    // Preset with two modules currently supported.
    // To do: Read from Config file
    public MidiModules() {
        moduleslist.add(MID_PATCH0);
        moduleslist.add(MID_PATCH1);

        modulesfiles.add(MID_PATCHFILE0);
        modulesfiles.add(MID_PATCHFILE1);
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

