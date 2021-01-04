package amidifx.models;

// https://www.bome.com/forums/7/856/cubase-parse-patch-file/

// 3,0,121,8,Deebach Grand1
public class MidiPatch {
    private int patchId;
    private int patchType;
    private int PC;
    private int LSB;
    private int MSB;
    private String patchName;

    public MidiPatch(int patchId, int patchType, int PC, int LSB, int MSB, String patchName) {
        super();
        this.patchId = patchId;
        this.patchType = patchType;
        this.PC = PC;
        this.LSB = LSB;
        this.MSB = MSB;
        this.patchName = patchName;
    }

    public int getPatchId() {
        return patchId;
    }
    public void setPatchId(int patchId) {
        this.patchId = patchId;
    }

    public int getPatchType() {
        return patchType;
    }
    public void setPatchType(int patchType) {
        this.patchType = patchType;
    }

    public int getPC() {
        return PC;
    }
    public void setPC(int PC) {
        this.PC = PC;
    }

    public int getLSB() {
        return LSB;
    }
    public void setLSB(int LSB) {
        this.LSB = LSB;
    }

    public int getMSB() {
        return MSB;
    }
    public void setMSB(int MSB) {
        this.MSB = MSB;
    }

    public String getPatchName() {
        return patchName;
    }
    public void setPatchName(String patchName) {
        this.patchName = patchName;
    }

    @Override
    public String toString() {
        return "Patch [patchId=" + patchId + ", patchType=" + patchType
                +  ", PC=" + PC + ", LSB=" + LSB + ", MSB=" + MSB
                + ", patchName=" + patchName + "]";
    }
}
