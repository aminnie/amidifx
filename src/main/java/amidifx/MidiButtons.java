package amidifx;

import amidifx.models.MidiButton;
import amidifx.models.MidiPatch;
import amidifx.models.MidiPreset;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class MidiButtons {

    // CSV file delimiter
    private static final String CSV_DELIMITER = ",";
    private static final String CSV_DIRECTORY = "C:/amidifx/midifiles/";

    String buttonFile;

    // List for holding Patch objects
    final List<MidiButton> buttonList = new ArrayList<>();

    // https://professionalcomposers.com/midi-cc-list/
    public static final byte ccVOL = 7;
    public static final byte ccEXP = 11;
    public static final byte ccREV = 91;
    public static final byte ccTRE = 92;
    public static final byte ccCHO = 93;
    public static final byte ccMOD = 1;
    public static final byte ccPAN = 10;

    MidiButtons midiButtons;
    ArrayList<buttonMap> buttonMaps = new ArrayList<>();

    class buttonMap {
        String buttonID;
        int buttonIdx;

        // Constructor
        public buttonMap(String buttonId, int buttonIdx) {
            this.buttonID = buttonId;
            this.buttonIdx = buttonIdx;
        }
    }

    // Constructor
    public MidiButtons() {

        // Create Revers Button Mapping for all Buttons in the UI
        this.createButtonMapping();
    }

    // Reverse Lookup ButtonIdx into List from buttonId
    public int lookupButtonIdx(String buttonId) {

        for (buttonMap bmap : buttonMaps) {
            if (bmap.buttonID == buttonId) return bmap.buttonIdx;
        }
        return 0;
    }

    // Create a list of all Buttons in UI with associated Button List Index value
    protected void createButtonMapping() {

        System.out.println("MidiButtons: Creating Button index mappings");

        // Upper 1 Buttons
        buttonMaps.add(0, new buttonMap("U1-1", 0));
        buttonMaps.add(0, new buttonMap("U1-2", 1));
        buttonMaps.add(0, new buttonMap("U1-3", 2));
        buttonMaps.add(0, new buttonMap("U1-4", 3));
        buttonMaps.add(0, new buttonMap("U1-5", 4));
        buttonMaps.add(0, new buttonMap("U1-6", 5));

        // Upper 2 Buttons
        buttonMaps.add(0, new buttonMap("U2-1", 6));
        buttonMaps.add(0, new buttonMap("U2-2", 7));
        buttonMaps.add(0, new buttonMap("U2-3", 8));
        buttonMaps.add(0, new buttonMap("U2-4", 9));

        // Upper 2 Buttons
        buttonMaps.add(0, new buttonMap("U3-1", 10));
        buttonMaps.add(0, new buttonMap("U3-2", 11));
        buttonMaps.add(0, new buttonMap("U3-3", 12));
        buttonMaps.add(0, new buttonMap("U3-4", 13));

        // Lower 1 Buttons
        buttonMaps.add(0, new buttonMap("L1-1", 14));
        buttonMaps.add(0, new buttonMap("L1-2", 15));
        buttonMaps.add(0, new buttonMap("L1-3", 16));
        buttonMaps.add(0, new buttonMap("L2-4", 17));

        // Lower 2 Buttons
        buttonMaps.add(0, new buttonMap("L2-1", 18));
        buttonMaps.add(0, new buttonMap("L2-2", 19));
        buttonMaps.add(0, new buttonMap("L2-3", 20));
        buttonMaps.add(0, new buttonMap("L2-4", 21));

        // Bass Buttons
        buttonMaps.add(0, new buttonMap("B1-1", 22));
        buttonMaps.add(0, new buttonMap("B1-2", 23));
        buttonMaps.add(0, new buttonMap("B1-3", 24));
        buttonMaps.add(0, new buttonMap("B1-4", 25));

        // Drum Buttons
        buttonMaps.add(0, new buttonMap("D1-1", 26));
        buttonMaps.add(0, new buttonMap("D1-2", 27));
        buttonMaps.add(0, new buttonMap("D1-3", 28));
        buttonMaps.add(0, new buttonMap("D1-4", 29));
    }

    // Load specific Button Config file
    public void makeMidiButtons(String buttonFile) {

        this.buttonFile = buttonFile;

        //midiButtons = new MidiButtons();

        // Need to validate this
        buttonList.clear();

        System.out.println("Loading MIDI Buttons using BufferedReader:  " + buttonFile);

        BufferedReader br = null;
        try {
            // To do: Read a specific Preset csv file
            br = new BufferedReader(new FileReader(CSV_DIRECTORY + buttonFile));

            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);

                // Skip any empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] buttonDetails = line.split(CSV_DELIMITER);

                if (buttonDetails.length > 0) {
                    //Save the Button and Patch details in Button object
                    // int patchId, int patchType, int PC, int MSB, int LSB, String patchName
                    MidiButton mButton = new MidiButton(
                            Integer.parseInt(buttonDetails[0]),     // index #
                            buttonDetails[1],                       // button id
                            Integer.parseInt(buttonDetails[2]),     // patch id
                            buttonDetails[3],                       // patch name
                            Integer.parseInt(buttonDetails[4]),     // layer
                            Integer.parseInt(buttonDetails[5]),     // in chan
                            buttonDetails[6],                       // out chan
                            Integer.parseInt(buttonDetails[7]),     // octave
                            Integer.parseInt(buttonDetails[8]),     // pc
                            Integer.parseInt(buttonDetails[9]),     // lsb
                            Integer.parseInt(buttonDetails[10]),     // msb
                            Integer.parseInt(buttonDetails[11]),     // mod
                            Integer.parseInt(buttonDetails[12]),    // vol
                            Integer.parseInt(buttonDetails[13]),    // exp
                            Integer.parseInt(buttonDetails[14]),    // rev
                            Integer.parseInt(buttonDetails[15]),    // cho
                            Integer.parseInt(buttonDetails[16]),    // tre
                            Integer.parseInt(buttonDetails[17]),    // mod
                            Integer.parseInt(buttonDetails[18]));   // pan
                    buttonList.add(mButton);
                }
            }

            // Print Preset Channels
            //for (int i = 0; i < buttonList.size(); i++) {
            //    MidiButton mButton = buttonList.get(i);

            //    System.out.println("*** " +  mButton.toString());
            //}
        }
        catch (Exception ee) {
            ee.printStackTrace();
        }
        finally {
            try {
                br.close();
            }
            catch (IOException ie) {
                System.out.println("*** Error occured while closing the MIDI Button BufferedReader ***");
                ie.printStackTrace();
            }
        }
    }


    // Return Button details at index number
    public MidiButton getButtonById(String buttonId, int layerIdx) {

        int buttonIdx = lookupButtonIdx(buttonId); // + layerIdx);

        //System.out.println("MIDI Button " + buttonId + ": " + buttonList.get(buttonIdx).toString());

        return buttonList.get(buttonIdx);
    }

    // Return Song details at index number
    public String getButtonFileName() {

        return buttonFile;
    }

    public int getButtonChannel(String buttonId) {

        int buttonIdx = lookupButtonIdx(buttonId);
        MidiButton midiButton = buttonList.get(buttonIdx);

        return midiButton.getChannelIdx();
    }

    // Update selected Button Voice Details based on MidiPatch provided
    public void setButton(String buttonId, int moduleidx, MidiPatch midiPatch) {

        int buttonIdx = lookupButtonIdx(buttonId);

        buttonList.get(buttonIdx).setPC(midiPatch.getPC());
        buttonList.get(buttonIdx).setLSB(midiPatch.getLSB());
        buttonList.get(buttonIdx).setMSB(midiPatch.getMSB());
        buttonList.get(buttonIdx).setModuleIdx(moduleidx);

        //presetList.get(idx).setBankIdx(midiPatch.getBankIdx());
        //presetList.get(idx).setFontIdx(midiPatch.getFontIdx());
        //buttonList.get(buttonIdx).setPatchName(midiPatch.getPatchName());

        //System.out.println("Updated MIDI Button " + buttonId + ": " + midiPatch.toString());
    }

    // List All Button details
    public void listButtons() {
        for (MidiButton mPreset : buttonList) {
            System.out.println(mPreset.toString());
        }
    }

    // Get Button details at index number
    public MidiButton getMidiButton(int buttonIdx, int layerIdx) {

        // Default Layers to zero until built out
        layerIdx = 0;

        MidiButton mButton = buttonList.get(buttonIdx + layerIdx);

        return mButton;
    }


    // Save current Button Configurations to Disk
    public boolean saveButtons(String buttonFile) {

        boolean bsaved = false;

        // Print Preset Channels
        for (int i = 0; i < buttonList.size(); i++) {
            MidiButton mButton = buttonList.get(i);

            //System.out.println(mButton.toString());
        }

        BufferedWriter bw = null;
        try {
            //Specify the file name and path here
            File file = new File(CSV_DIRECTORY + buttonFile);

            // Ensure that the fil gets created if it is not present at the specified location
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            for (MidiButton button : buttonList) {

                String buttonLine = Integer.toString(button.getButtonIdx());
                buttonLine = buttonLine.concat(",").concat(button.getButtonId());
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getPatchId()));
                buttonLine = buttonLine.concat(",").concat(button.getPatchName());
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getLayerIdx()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getChannelIdx()));
                buttonLine = buttonLine.concat(",").concat(button.getChannelOutIdx());
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getOctaveTran()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getPC()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getLSB())).concat(",").concat(Integer.toString(button.getMSB()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getModuleIdx()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getVOL())).concat(",").concat(Integer.toString(button.getEXP()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getREV())).concat(",").concat(Integer.toString(button.getCHO()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getPAN())).concat(",").concat(Integer.toString(button.getMOD()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getMOD()).concat("\r"));
                bw.write(buttonLine);

                //System.out.print("buttonline: " + buttonLine);
            }

            System.out.println("Button file " + buttonFile + " successfully written");
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            try {
                if(bw!=null) {
                    bw.close();
                    bsaved = true;
                }
            }
            catch(Exception ex) {
                System.err.println("Error in closing Buttons file " + buttonFile + " in BufferedWriter"+ex);
            }
        }
        return bsaved;
    }


    // Apply current current/selected MIDI Button
    public boolean applyMidiButton(MidiButton button, int channelIdx) {

        try {
            PlayMidi playmidifile = PlayMidi.getInstance();

            // Read channel out values and apply Preset for the first output channel in the list
            String channelIdxOut = button.getChannelOutIdx();
            //System.out.println("applyMIDIPreset Channel out: " + channelIdxOut);

            int CHANOUT;
            int idx = channelIdxOut.indexOf("|",0);

            if ( idx != -1) {
                CHANOUT = Integer.parseInt(channelIdxOut.substring(0, idx)) - 1;
                //System.out.println("applyMIDIPreset: Channel = " + CHANOUT);
            }
            else {
                CHANOUT = Integer.parseInt(channelIdxOut) - 1;
                //System.out.println("applyMIDIPreset: Channel = " + CHANOUT);
            }

            if ((CHANOUT < 0) || (CHANOUT > 15)) {
                System.err.println("applyMIDIPreset Error: Channel = " + CHANOUT);
                return false;
            }

            int PC = button.getPC() & 0xFF;
            int LSB = button.getLSB() & 0xFF;
            int MSB = button.getMSB() & 0xFF;

            playmidifile.sendMidiProgramChange(CHANOUT, PC, LSB, MSB);

            int VOL = button.getVOL() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccVOL, (byte) VOL);

            int EXP = button.getEXP() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccEXP, (byte) EXP);

            int REV = button.getREV() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccREV, (byte) REV);

            int CHO = button.getCHO() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccCHO, (byte) CHO);

            int TRE = button.getTRE() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccTRE, (byte) TRE);

            int MOD = button.getMOD() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccMOD, (byte) MOD);

            int PAN = button.getPAN() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccPAN, (byte) PAN);
        }
        catch(Exception e) {
            System.err.println("applyMIDIPreset Exception: " + button.toString());
            System.err.println(e);
            return false;
        }

        //System.out.println("applyMIDIPreset: " + preset.toString());

        return true;
    }

    public void copyPresetToMidiButton(MidiPreset preset, MidiButton midibutton) {

        midibutton.setPatchId(preset.getPatchIdx());
        midibutton.setPatchName(preset.getPatchName());

        midibutton.setPC(preset.getPC());
        midibutton.setLSB(preset.getLSB());
        midibutton.setMSB(preset.getMSB());

        midibutton.setVOL(preset.getVOL());
        midibutton.setEXP(preset.getEXP());
        midibutton.setREV(preset.getREV());
        midibutton.setCHO(preset.getCHO());
        midibutton.setMOD(preset.getMOD());
        midibutton.setTRE(preset.getTRE());
        midibutton.setPAN(preset.getPAN());
    }

}
