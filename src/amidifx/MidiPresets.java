package amidifx;

import amidifx.models.MidiLayer;
import amidifx.models.MidiPatch;
import amidifx.models.MidiPreset;
import amidifx.utils.ArduinoUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MidiPresets {

    // CSV file delimiter
    private static final String CSV_DELIMITER = ",";
    private static final String CSV_DIRECTORY = "C:/amidifx/midifiles/";

    String presetFile;

    // List for holding Patch objects
    final List<MidiPreset> presetList = new ArrayList<>();

    // https://professionalcomposers.com/midi-cc-list/
    public static final byte ccVOL = 7;
    public static final byte ccEXP = 11;
    public static final byte ccREV = 91;
    public static final byte ccTRE = 92;
    public static final byte ccCHO = 93;
    public static final byte ccMOD = 1;
    public static final byte ccPAN = 10;


    // Load specific Preset file
    public void makeMidiPresets(String presetFile) {

        this.presetFile = presetFile;

        MidiPresets banks = new MidiPresets();

        // To do: Need to validate this
        presetList.clear();

        //System.out.println("Loading MIDI Preset using BufferedReader:  " + presetFile);

        BufferedReader br = null;
        try {
            // To do: Read a specific Preset csv file
            br = new BufferedReader(new FileReader(CSV_DIRECTORY + presetFile));

            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);

                // Skip any empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] presetDetails = line.split(CSV_DELIMITER);

                if (presetDetails.length > 0) {
                    //Save the patch details in patch object
                    // int patchId, int patchType, int PC, int MSB, int LSB, String patchName
                    MidiPreset mPreset = new MidiPreset(
                            Integer.parseInt(presetDetails[0]),
                            Integer.parseInt(presetDetails[1]),
                            presetDetails[2],
                            Integer.parseInt(presetDetails[3]),
                            Integer.parseInt(presetDetails[4]),
                            Integer.parseInt(presetDetails[5]),
                            Integer.parseInt(presetDetails[6]),
                            Integer.parseInt(presetDetails[7]),
                            Integer.parseInt(presetDetails[8]),
                            Integer.parseInt(presetDetails[9]),
                            Integer.parseInt(presetDetails[10]),
                            Integer.parseInt(presetDetails[11]),
                            Integer.parseInt(presetDetails[12]),
                            Integer.parseInt(presetDetails[13]),
                            0, //Integer.parseInt(presetDetails[13]), // TRE not saved in Preset file. Defaulted to zero
                            Integer.parseInt(presetDetails[14]),
                            Integer.parseInt(presetDetails[15]),
                            Integer.parseInt(presetDetails[16]),
                            presetDetails[17]);
                    presetList.add(mPreset);
                }
            }

            // Print Preset Channels
            //for (int i = 0; i < presetList.size(); i++) {
            //    MidiPreset mPreset = presetList.get(i);
            //    System.out.println("*** " +  mPreset.toString());
            //}
        }
        catch (Exception ee) {
            ee.printStackTrace();
        }
        finally {
            try {
                br.close();

                // Forward Preset Channel Layer Data to ARM Controller
                ArduinoUtils arduinoutils = ArduinoUtils.getInstance();
                if (arduinoutils.hasARMPort()) {
                    for (int i = 0; i < presetList.size(); i++) {
                        MidiPreset mPreset = presetList.get(i);

                        MidiLayer midilayer = new MidiLayer(mPreset);
                        arduinoutils.writeLayerSysexData((byte)2, midilayer.getChannelOut());

                        //System.out.println("Sending to ARM Controller " + midilayer.toString());
                    }
                }

            }
            catch (IOException ie) {
                System.out.println("*** Error occured while closing the MIDI Preset BufferedReader ***");
                ie.printStackTrace();
            }
        }
    }

    // Return Song details at index number
    public MidiPreset getPreset(int idx) {

        //System.out.println("MIDI Preset " + idx + ": " + midiPreset.toString());

        return presetList.get(idx);
    }

    // Return Song details at index number
    public String getPresetFileName() {

        return presetFile;
    }

    // Update selected Preset Voice Details based on MidiPatch provided
    public void setPreset(int idx, int moduleidx, MidiPatch midiPatch) {
        presetList.get(idx).setPC(midiPatch.getPC());
        presetList.get(idx).setLSB(midiPatch.getLSB());
        presetList.get(idx).setMSB(midiPatch.getMSB());
        presetList.get(idx).setModuleIdx(moduleidx);

        presetList.get(idx).setPatchIdx(midiPatch.getPatchId());
        presetList.get(idx).setPatchName(midiPatch.getPatchName());

        //System.out.println("Updated MIDI Preset " + idx + ": " + midiPatch.toString());
    }

    // List Preset details at index number
    public void getPresets() {
        for (MidiPreset mPreset : presetList) {
            System.out.println(mPreset.toString());
        }
    }

    // List Preset details at index number
    public String presetString(int idx) {
        MidiPreset mPreset = presetList.get(idx);

        return mPreset.toString();
    }

    // Save current Preset to Disk
    public boolean savePresets(String presetFle) {

        boolean bsaved = false;

        // Print Preset Channels
        //for (int i = 0; i < presetList.size(); i++) {
        //    MidiPreset mPreset = presetList.get(i);
        //    System.out.println(mPreset.toString());
        //}

        BufferedWriter bw = null;
        try {
            //Specify the file name and path here
            File file = new File(CSV_DIRECTORY + presetFle);

            // Ensure that the fil gets created if it is not present at the specified location
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            for (int idx = 0; idx < (16 * 8); idx++) {
                MidiPreset preset = getPreset(idx);

                String presetline = Integer.toString(preset.getPresetIdx());
                presetline = presetline.concat(",").concat(Integer.toString(preset.getChannelIdx()));
                presetline = presetline.concat(",").concat(preset.getChannelOutIdx());
                presetline = presetline.concat(",").concat(Integer.toString(preset.getOctaveTran()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getPC()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getLSB())).concat(",").concat(Integer.toString(preset.getMSB()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getModuleIdx()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getVOL())).concat(",").concat(Integer.toString(preset.getEXP()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getREV())).concat(",").concat(Integer.toString(preset.getCHO()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getPAN())).concat(",").concat(Integer.toString(preset.getMOD()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getBankIdx()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getFontIdx()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getPatchIdx()));
                presetline = presetline.concat(",").concat(preset.getPatchName()).concat("\r");
                bw.write(presetline);

                //System.out.print("presetline " + idx + ": " + presetline);
            }

            //System.out.println("Preset file " + presetFle + " successfully written");
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
                System.err.println("Error in closing Preset file " + presetFle + " in BufferedWriter"+ex);
            }
        }
        return bsaved;
    }


    // Apply current current/selected MIDI Preset
    public boolean applyMidiPreset(MidiPreset preset, int channelIdx) {

        try {
            PlayMidi playmidifile = PlayMidi.getInstance();

            // Read channel out values and apply Preset for the first output channel in the list
            String channelIdxOut = preset.getChannelOutIdx();
            //System.out.println("applyMIDIPreset Channel out: " + channelIdxOut);

            int CHANOUT;
            int idx = channelIdxOut.indexOf("|",0);

            if ( idx != -1) {
                CHANOUT = Integer.parseInt(channelIdxOut.substring(0, idx));
                //System.out.println("applyMIDIPreset: Channel = " + CHANOUT);
            }
            else {
                CHANOUT = Integer.parseInt(channelIdxOut);
                //System.out.println("applyMIDIPreset: Channel = " + CHANOUT);
            }

            if ((CHANOUT < 1) || (CHANOUT > 16)) {
                //System.out.println("applyMIDIPreset Error: Channel = " + CHANOUT);
                return false;
            }

            int PC = preset.getPC() & 0xFF;
            int LSB = preset.getLSB() & 0xFF;
            int MSB = preset.getMSB() & 0xFF;

            playmidifile.sendMidiProgramChange(CHANOUT, PC, LSB, MSB);

            int VOL = preset.getVOL() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccVOL, (byte) VOL);

            int EXP = preset.getEXP() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccEXP, (byte) EXP);

            int REV = preset.getREV() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccREV, (byte) REV);

            int CHO = preset.getCHO() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccCHO, (byte) CHO);

            int TRE = preset.getTRE() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccTRE, (byte) TRE);

            int MOD = preset.getMOD() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccMOD, (byte) MOD);

            int PAN = preset.getPAN() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccPAN, (byte) PAN);
        }
        catch(Exception e) {
            System.err.println("applyMIDIPreset Exception: " + preset.toString());
            System.err.println(e);
            return false;
        }

        //System.out.println("applyMIDIPreset: " + preset.toString());

        return true;
    }
}