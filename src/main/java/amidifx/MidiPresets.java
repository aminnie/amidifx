package amidifx;

import amidifx.models.MidiLayer;
import amidifx.models.MidiPatch;
import amidifx.models.MidiPreset;
import amidifx.models.SharedStatus;
import amidifx.utils.ArduinoUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MidiPresets {

    // Static variable single_instance of type PlayMidi
    private static MidiPresets single_MidiPresetsInstance = null;

    // CSV file delimiter
    private static final String CSV_DELIMITER = ",";

    String presetFile;

    SharedStatus sharedStatus;

    // List for holding Patch objects
    final List<MidiPreset> presetList = new ArrayList<>();

    // https://professionalcomposers.com/midi-cc-list/
    public static final byte ccVOL = 7;
    public static final byte ccEXP = 11;
    public static final byte ccREV = 91;
    public static final byte ccROT = 74;
    public static final byte ccCHO = 93;
    public static final byte ccMOD = 1;
    public static final byte ccPAN = 10;
    public static final byte ccGP1 = 80;
    public static final byte ccGP2 = 81;

    public static final byte ccTIM = 71;
    public static final byte ccREL = 72;
    public static final byte ccATK = 73;
    public static final byte ccBRI = 74;

    // Static method to create singleton instance of PlayMidi class
    public synchronized static MidiPresets getInstance() {
        if (single_MidiPresetsInstance == null) {
            single_MidiPresetsInstance = new MidiPresets();

            System.out.println("PlayMidi: Creating instance MidiPresets");
        }

        return single_MidiPresetsInstance;
    }

    // *** Make constructor private for Singleton ***
    private MidiPresets() {

    }

    // Load specific Preset file
    public void makeMidiPresets(String presetFile) {

        this.presetFile = presetFile;

        MidiPresets banks = new MidiPresets();

        // Create instance of Shared Status to report back to Scenes
        sharedStatus = SharedStatus.getInstance();

        // To do: Need to validate this
        presetList.clear();

        //System.out.println("Loading MIDI Preset using BufferedReader:  " + presetFile);

        BufferedReader br = null;
        try {
            // To do: Read a specific Preset csv file
            br = new BufferedReader(new FileReader(sharedStatus.getMIDDirectory() + presetFile));

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
                            Integer.parseInt(presetDetails[8]),     // VOL
                            Integer.parseInt(presetDetails[9]),     // EXP
                            Integer.parseInt(presetDetails[10]),    // REV
                            Integer.parseInt(presetDetails[11]),    // CHO
                            Integer.parseInt(presetDetails[12]),    // TIM
                            Integer.parseInt(presetDetails[13]),    // ATK
                            Integer.parseInt(presetDetails[14]),    // REL
                            Integer.parseInt(presetDetails[15]),    // BRI
                            Integer.parseInt(presetDetails[16]),    // PAN
                            Integer.parseInt(presetDetails[17]),    // MOD
                            Integer.parseInt(presetDetails[18]),
                            Integer.parseInt(presetDetails[19]),
                            Integer.parseInt(presetDetails[20]),
                            presetDetails[21]);
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

                // Reload presets on screens such as Perform it has changed
                sharedStatus.setPresetReload(false);

            }
            catch (IOException ie) {
                System.out.println("*** Error occured while closing the MIDI Preset BufferedReader ***");
                ie.printStackTrace();
            }
        }
    }

    // Return Preset details at index number
    public MidiPreset getPreset(int idx) {

        //System.out.println("MIDI Preset " + idx + ": " + midiPreset.toString());

        return presetList.get(idx);
    }

    // Return Preset details at index number
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

    // Copy provided Preset Channel to the next Preset same Channel
    public void copyPreset(MidiPreset midipreset, int presetIdx, int nextpresetIdx) {
        presetList.get(nextpresetIdx).setPresetIdx(nextpresetIdx);
        presetList.get(nextpresetIdx).setChannelIdx(midipreset.getChannelIdx());;
        presetList.get(nextpresetIdx).setChannelOutIdx(midipreset.getChannelOutIdx());;
        presetList.get(nextpresetIdx).setOctaveTran(midipreset.getOctaveTran());
        presetList.get(nextpresetIdx).setPC(midipreset.getPC());
        presetList.get(nextpresetIdx).setLSB(midipreset.getLSB());
        presetList.get(nextpresetIdx).setMSB(midipreset.getMSB());
        presetList.get(nextpresetIdx).setModuleIdx(midipreset.getModuleIdx());

        presetList.get(nextpresetIdx).setVOL(midipreset.getVOL());
        presetList.get(nextpresetIdx).setEXP(midipreset.getEXP());
        presetList.get(nextpresetIdx).setREV(midipreset.getREV());
        presetList.get(nextpresetIdx).setCHO(midipreset.getCHO());
        presetList.get(nextpresetIdx).setTIM(midipreset.getTIM());
        presetList.get(nextpresetIdx).setATK(midipreset.getATK());
        presetList.get(nextpresetIdx).setREL(midipreset.getREL());
        presetList.get(nextpresetIdx).setBRI(midipreset.getBRI());
        presetList.get(nextpresetIdx).setPAN(midipreset.getPAN());
        presetList.get(nextpresetIdx).setMOD(midipreset.getMOD());

        presetList.get(nextpresetIdx).setBankIdx(midipreset.getBankIdx());
        presetList.get(nextpresetIdx).setFontIdx(midipreset.getFontIdx());
        presetList.get(nextpresetIdx).setPatchIdx(midipreset.getPatchIdx());
        presetList.get(nextpresetIdx).setPatchName(midipreset.getPatchName());

        //System.out.println("Copied Preset " + (presetIdx + 1) + " to " + (presetList.get(nextpresetIdx).getPresetIdx() + 1));
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

        // Create instance of Shared Status to report back to Scenes
        sharedStatus = SharedStatus.getInstance();

        // Print Preset Channels
        //for (int i = 0; i < presetList.size(); i++) {
        //    MidiPreset mPreset = presetList.get(i);
        //    System.out.println(mPreset.toString());
        //}

        BufferedWriter bw = null;
        try {
            //Specify the file name and path here
            File file = new File(sharedStatus.getMIDDirectory() + presetFle);

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
                presetline = presetline.concat(",").concat(Integer.toString(preset.getTIM())).concat(",").concat(Integer.toString(preset.getATK()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getREL())).concat(",").concat(Integer.toString(preset.getBRI()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getPAN())).concat(",").concat(Integer.toString(preset.getMOD()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getBankIdx()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getFontIdx()));
                presetline = presetline.concat(",").concat(Integer.toString(preset.getPatchIdx()));
                presetline = presetline.concat(",").concat(preset.getPatchName()).concat("\r");
                bw.write(presetline);

                //System.out.println("presetline " + idx + ": " + presetline);
            }

            // Reload presets on screens such as Perform it has changed
            sharedStatus.setPresetReload(true);

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

            sharedStatus.setOctaveCHAN(CHANOUT - 1, (byte)(preset.getOctaveTran() & 0XFF));

            int VOL = preset.getVOL() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccVOL, (byte) VOL);

            int EXP = preset.getEXP() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccEXP, (byte) EXP);

            int REV = preset.getREV() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccREV, (byte) REV);

            int CHO = preset.getCHO() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccCHO, (byte) CHO);

            int MOD = preset.getMOD() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccMOD, (byte) MOD);

            int TIM = preset.getTIM() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccTIM, (byte) TIM);

            int ATK = preset.getATK() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccATK, (byte) ATK);

            int REL = preset.getREL() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccREL, (byte) REL);

            int BRI = preset.getBRI() & 0x7F;
            playmidifile.sendMidiControlChange((byte) CHANOUT, ccBRI, (byte) BRI);

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