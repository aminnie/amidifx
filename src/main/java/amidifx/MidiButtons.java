package amidifx;

import amidifx.models.MidiButton;
import amidifx.models.MidiPatch;
import amidifx.models.MidiPreset;
import amidifx.models.SharedStatus;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class MidiButtons {

    // Static variable single_instance of type PlayMidi
    private static MidiButtons single_MidiButtonsInstance = null;

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
    public static final byte ccCHO = 93;
    public static final byte ccMOD = 1;
    public static final byte ccPAN = 10;

    public static final byte ccTIM = 71;
    public static final byte ccREL = 72;
    public static final byte ccATK = 73;
    public static final byte ccBRI = 74;

    MidiButtons midiButtons;
    ArrayList<buttonMap> buttonMaps = new ArrayList<>();

    class buttonMap {
        String buttonID;
        int buttonIdx;
        int channelIdx;

        // Constructor
        public buttonMap(String buttonId, int buttonIdx, int channelIdx) {
            this.buttonID = buttonId;
            this.buttonIdx = buttonIdx;
            this.channelIdx = channelIdx;
        }
    }

    // Static method to create singleton instance of PlayMidi class
    public synchronized static MidiButtons getInstance() {
        if (single_MidiButtonsInstance == null) {
            single_MidiButtonsInstance = new MidiButtons();

            //System.out.println("MidiButtons: Creating instance MidiButtons");
        }

        return single_MidiButtonsInstance;
    }

    // Constructor
    private MidiButtons() {

        // Create Reverse Button Mapping for all Buttons in the UI
        this.createButtonMapping();
    }

    // Reverse Lookup ButtonIdx into List from buttonId
    public int lookupButtonIdx(String buttonId) {

        for (buttonMap bmap : buttonMaps) {
            if (bmap.buttonID.equals(buttonId))
                return bmap.buttonIdx;
        }
        return 0;
    }

    // Reverse Lookup ChannelIdx into List from buttonId
    public int lookupChannelIdx(String buttonId) {

        for (buttonMap bmap : buttonMaps) {
            if (bmap.buttonID.equals(buttonId))
                return bmap.channelIdx;
        }
        return 0;
    }

    // Create a list of all Buttons in UI with associated Button List Index value
    protected void createButtonMapping() {

        System.out.println("MidiButtons: Creating Button index mappings");

        SharedStatus sharedstatus = SharedStatus.getInstance();

        // Upper 1 Buttons
        buttonMaps.add(0, new buttonMap("U1-1", 0, sharedstatus.getUpper1CHAN() ));
        buttonMaps.add(0, new buttonMap("U1-2", 1, sharedstatus.getUpper1CHAN() ));
        buttonMaps.add(0, new buttonMap("U1-3", 2, sharedstatus.getUpper1CHAN() ));
        buttonMaps.add(0, new buttonMap("U1-4", 3, sharedstatus.getUpper1CHAN() ));
        buttonMaps.add(0, new buttonMap("U1-5", 4, sharedstatus.getUpper1CHAN() ));
        buttonMaps.add(0, new buttonMap("U1-6", 5, sharedstatus.getUpper1CHAN() ));

        // Upper 2 Buttons
        buttonMaps.add(0, new buttonMap("U2-1", 6, sharedstatus.getUpper2CHAN() ));
        buttonMaps.add(0, new buttonMap("U2-2", 7, sharedstatus.getUpper2CHAN() ));
        buttonMaps.add(0, new buttonMap("U2-3", 8, sharedstatus.getUpper2CHAN() ));
        buttonMaps.add(0, new buttonMap("U2-4", 9, sharedstatus.getUpper2CHAN() ));

        // Upper 3 Buttons
        buttonMaps.add(0, new buttonMap("U3-1", 10, sharedstatus.getUpper3CHAN() ));
        buttonMaps.add(0, new buttonMap("U3-2", 11, sharedstatus.getUpper3CHAN() ));
        buttonMaps.add(0, new buttonMap("U3-3", 12, sharedstatus.getUpper3CHAN() ));
        buttonMaps.add(0, new buttonMap("U3-4", 13, sharedstatus.getUpper3CHAN() ));

        // Lower 1 Buttons
        buttonMaps.add(0, new buttonMap("L1-1", 14, sharedstatus.getLower1CHAN() ));
        buttonMaps.add(0, new buttonMap("L1-2", 15, sharedstatus.getLower1CHAN() ));
        buttonMaps.add(0, new buttonMap("L1-3", 16, sharedstatus.getLower1CHAN() ));
        buttonMaps.add(0, new buttonMap("L1-4", 17, sharedstatus.getLower1CHAN() ));
        buttonMaps.add(0, new buttonMap("L1-5", 18, sharedstatus.getLower1CHAN() ));
        buttonMaps.add(0, new buttonMap("L1-6", 19, sharedstatus.getLower1CHAN() ));

        // Lower 2 Buttons
        buttonMaps.add(0, new buttonMap("L2-1", 20, sharedstatus.getLower2CHAN() ));
        buttonMaps.add(0, new buttonMap("L2-2", 21, sharedstatus.getLower2CHAN() ));
        buttonMaps.add(0, new buttonMap("L2-3", 22, sharedstatus.getLower2CHAN() ));
        buttonMaps.add(0, new buttonMap("L2-4", 23, sharedstatus.getLower2CHAN() ));

        // Bass Buttons
        buttonMaps.add(0, new buttonMap("B1-1", 24, sharedstatus.getBassCHAN() ));
        buttonMaps.add(0, new buttonMap("B1-2", 25, sharedstatus.getBassCHAN() ));
        buttonMaps.add(0, new buttonMap("B1-3", 26, sharedstatus.getBassCHAN() ));
        buttonMaps.add(0, new buttonMap("B1-4", 27, sharedstatus.getBassCHAN() ));

        // Drum Buttons
        buttonMaps.add(0, new buttonMap("D1-1", 28, sharedstatus.getDrumCHAN() ));
        buttonMaps.add(0, new buttonMap("D1-2", 29, sharedstatus.getDrumCHAN() ));
        buttonMaps.add(0, new buttonMap("D1-3", 30, sharedstatus.getDrumCHAN() ));
        buttonMaps.add(0, new buttonMap("D1-4", 31, sharedstatus.getDrumCHAN() ));
    }

    // Load specific Button Config file
    public void loadMidiButtons(String buttonFile) {

        this.buttonFile = buttonFile;

        // Need to validate this
        buttonList.clear();

        System.out.println("LoadMidiButtons: Loading buttons file:  " + buttonFile);

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
                            Integer.parseInt(buttonDetails[10]),    // msb
                            Integer.parseInt(buttonDetails[11]),    //
                            Integer.parseInt(buttonDetails[12]),    // vol
                            Integer.parseInt(buttonDetails[13]),    // exp
                            Integer.parseInt(buttonDetails[14]),    // rev
                            Integer.parseInt(buttonDetails[15]),    // cho
                            Integer.parseInt(buttonDetails[16]),    // mod
                            Integer.parseInt(buttonDetails[17]),    // tim
                            Integer.parseInt(buttonDetails[18]),    // atk
                            Integer.parseInt(buttonDetails[19]),    // rel
                            Integer.parseInt(buttonDetails[20]),    // bri
                            Integer.parseInt(buttonDetails[21]));   // pan
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
        //for (int i = 0; i < buttonList.size(); i++) {
        //    MidiButton mButton = buttonList.get(i);
        //    System.out.println(mButton.toString());
        //}

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
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getMOD()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getTIM()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getATK()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getREL()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getBRI()));
                buttonLine = buttonLine.concat(",").concat(Integer.toString(button.getPAN()).concat("\r"));
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

            int MOD = button.getMOD() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccMOD, (byte) MOD);

            int TIM = button.getTIM() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccTIM, (byte) TIM);

            int ATK = button.getATK() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccATK, (byte) ATK);

            int REL = button.getREL() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccREL, (byte) REL);

            int BRI = button.getBRI() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccBRI, (byte) BRI);

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

        midibutton.setChannelIdx(preset.getChannelIdx());
        midibutton.setChannelOutIdx(preset.getChannelOutIdx());

        midibutton.setPC(preset.getPC());
        midibutton.setLSB(preset.getLSB());
        midibutton.setMSB(preset.getMSB());

        midibutton.setVOL(preset.getVOL());
        midibutton.setEXP(preset.getEXP());
        midibutton.setREV(preset.getREV());
        midibutton.setCHO(preset.getCHO());
        midibutton.setMOD(preset.getMOD());
        midibutton.setTIM(preset.getTIM());
        midibutton.setATK(preset.getATK());
        midibutton.setREL(preset.getREL());
        midibutton.setBRI(preset.getBRI());
        midibutton.setPAN(preset.getPAN());

        midibutton.setOctaveTran(preset.getOctaveTran());
    }
}
