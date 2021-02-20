package amidifx;

import amidifx.models.MidiBank;
import amidifx.models.MidiPatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MidiPatches {

    // Static variable single_instance of type PlayMidi
    private static MidiPatches single_MidiPatchesInstance = null;

    // CSV file delimiter
    private static final String CSV_DELIMITER = ",";
    private static final String MID_DIRECTORY = "C:/amidifx/midifiles/";

    //private static final String MID_PATCHFILE = "maxplus.dat";
    private static final String MID_PATCHFILE = "midigm.dat";

    // List for holding Patch objects
    final List<MidiPatch> patchList = new ArrayList<>();
    final MidiBanks banks = new MidiBanks();

    // Static method to create singleton instance of PlayMidi class
    public synchronized static MidiPatches getInstance() {
        if (single_MidiPatchesInstance == null) {
            single_MidiPatchesInstance = new MidiPatches();

            System.out.println("PlayMidi: Creating instance MidiPatches");
        }

        return single_MidiPatchesInstance;
    }

    // *** Make constructor private for Singleton ***
    private MidiPatches() {

    }

    public boolean loadMidiPatches(String patchfile) {

        boolean returnstate = true;

        int bankId = 0;

        // Clear the Patch and Bank Lists before loading updated Patch and Bank List
        patchList.clear();
        banks.clearMidiBankList();

        BufferedReader br = null;
        try {
            System.out.println("Reading Patchfile: " + patchfile);

            // Read the csv file
            br = new BufferedReader(new FileReader(MID_DIRECTORY + patchfile));

            int patchIdx = 0;
            String line;

            while ((line = br.readLine()) != null) {

                // Skip any empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] patchDetails = line.split(CSV_DELIMITER);

                // Save TYpe 2 = New Bank into separate structure
                if (Integer.parseInt(patchDetails[0]) != 3) {
                    // Create entry for new Patch Bank
                    // int bankId, String bankName, int patchIdx, int patchCnt
                    if (Integer.parseInt(patchDetails[0]) == 2) {
                        MidiBank mBank = new MidiBank(bankId++, patchDetails[1], patchIdx, 0);
                        banks.addMidiBank(mBank);
                    }
                    continue;
                }

                if (patchDetails.length > 0) {
                    //Save the patch details in patch object, format:
                    // int patchId, int patchType, int PC, int LSB, int MSB, String patchName
                    MidiPatch mPatch = new MidiPatch(patchIdx++,
                            Integer.parseInt(patchDetails[0]),
                            Integer.parseInt(patchDetails[1]),
                            Integer.parseInt(patchDetails[2]),
                            Integer.parseInt(patchDetails[3]),
                            patchDetails[4]);
                    patchList.add(mPatch);
                }
            }

            // Print Patch List
            //listMidiPatches();

            // Print Bank list
            //banks.listMidiBanks();

            }
        catch (Exception ex) {
            System.out.println("Exception occured while reading Patch file");
            ex.printStackTrace();

            returnstate = false;
        }
        finally {
            try {
                br.close();
            }
            catch (IOException ie) {
                System.out.println("Error occured while closing the BufferedReader");
                ie.printStackTrace();
            }
        }

        return returnstate;
    }

    // Return full Patch at index number
    public MidiPatch getMIDIPatch(int idx) {

        return patchList.get(idx);
    }

    public int getMIDIPatchSize() {
        return patchList.size();
    }

    public MidiBanks getMidiBanks() {
        return banks;
    }

    public boolean fileExist(String patchfile) {

        File f = new File(MID_DIRECTORY + patchfile);
        if (!f.exists()) {
            System.out.println("MIDIPatches: Error Patchong File does not exist: " + patchfile);
            return false;
        }
        return true;
    }

    // Print Patch List list
    public void listMidiPatches() {

        // Print Patch List
        for (MidiPatch e : patchList) {
            System.out.println(e.getPatchId() + "   " + e.getPatchType() + "   "
                    + e.getPC() + "   "+ e.getLSB() + "   "+ e.getMSB() + "   "
                    + e.getPatchName());
        }
    }

}
