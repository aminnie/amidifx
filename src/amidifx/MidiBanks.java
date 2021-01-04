package amidifx;

import amidifx.models.MidiBank;

import java.util.ArrayList;
import java.util.List;

public class MidiBanks {

    // CSV file delimiter
    private static final String CSV_DELIMITER = ",";

    // List for holding Patch objects
    final List<MidiBank> bankList = new ArrayList<>();

    // Return Bank list index for added entry
    public int addMidiBank(MidiBank mBank) {

        // Add next entry into MIDI Bank List
        int bankIdx = bankList.size();
        mBank.setBankId(bankIdx);
        bankList.add(mBank);

        return bankIdx;
    }

    public void clearMidiBankList() {
        bankList.clear();
    }

    // Return full patch at index number
    public MidiBank getMidiBank(int idx) {

        return bankList.get(idx);
    }

    // Return Bank list size
    public int getMidiBankSize() {
        return bankList.size();
    }

    // Print Bank list
    public void listMidiBanks() {

        for (MidiBank e : bankList) {
            System.out.println(e.getBankId() + "   " + e.getBankName() + "   "
                    + e.getPatchIdx() + "   " + e.getPatchCnt());
        }
    }

}

