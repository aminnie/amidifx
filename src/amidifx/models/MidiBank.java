package amidifx.models;

// 2,A-Piano

// Save the Bank Names and start index into the Batches array
public class MidiBank {
    private int bankId;
    private String bankName;
    private int patchIdx;
    private int patchCnt;

    public MidiBank(int bankId, String bankName, int patchIdx, int patchCnt) {
        super();
        this.bankId = bankId;
        this.bankName = bankName;
        this.patchIdx = patchIdx;
        this.patchCnt = patchCnt;
    }

    public int getBankId() {
        return bankId;
    }
    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public String getBankName() {
        return bankName;
    }
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public int getPatchIdx() {
        return patchIdx;
    }
    public void setPatchIdx(int patchIdx) {
        this.patchIdx = patchIdx;
    }

    public int getPatchCnt() {
        return patchCnt;
    }
    public void sePatchCnt(int patchCnt) {
        this.patchCnt = patchCnt;
    }

    @Override
    public String toString() {
        return "Bank [bankId=" + bankId + ", bankName=" + bankName
                + ", patchIdx=" + patchIdx + ", patchCnt=" + patchCnt + "]";
    }

}
