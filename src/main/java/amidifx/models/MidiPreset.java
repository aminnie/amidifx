package amidifx.models;

public class MidiPreset {
    private int presetIdx;
    private int channelIdx;
    private String channelOutIdx;
    private int octaveTran;
    private int PC;
    private int LSB;
    private int MSB;
    private int moduleIdx;
    private int VOL;
    private int EXP;
    private int REV;
    private int CHO;
    private int PAN;
    private int MOD;
    private int TRE;
    private int bankIdx;
    private int fontIdx;
    private int patchIdx;
    private String patchName;

    public MidiPreset() {
        this.setPresetIdx(0);
        this.setChannelOutIdx("0");
        this.setBankIdx(0);
        this.setPC(0);
        this.setLSB(0);
        this.setMSB(0);
        this.setVOL(0);
        this.setEXP(0);
        this.setREV(0);
        this.setCHO(0);
        this.setTRE(0);
        this.setMOD(0);
        this.setPAN(0);
    }

    // 0,12,12,0,106,121,100,0,100,100,20,0,0,0,0,3,8,Klaus sein Sax
    public MidiPreset(int presetIdx, int channelIdx, String channelOutIdx, int octaveTran,
                      int PC, int LSB, int MSB, int moduleIdx,
                      int VOL, int EXP, int REV, int CHO, int PAN,int MOD, int TRE,
                      int bankIdx, int fontIdx, int patchIdx, String patchName) {
        super();
        this.presetIdx = presetIdx;
        this.channelIdx = channelIdx;
        this.channelOutIdx = channelOutIdx;
        this.octaveTran = octaveTran;
        this.PC = PC;
        this.LSB = LSB;
        this.MSB = MSB;
        this.moduleIdx = moduleIdx;
        this.VOL = VOL;
        this.EXP = EXP;
        this.REV = REV;
        this.CHO = CHO;
        this.PAN = PAN;
        this.MOD = MOD;
        this.TRE = TRE;
        this.bankIdx = bankIdx;
        this.fontIdx = fontIdx;
        this.patchIdx = patchIdx;
        this.patchName = patchName;
    }

    public int getPresetIdx() {
        return presetIdx;
    }
    public void setPresetIdx(int presetIdx) {
        this.presetIdx = presetIdx;
    }

    public int getChannelIdx() {
        return channelIdx;
    }
    public void setChannelIdx(int channelIdx) {
        this.channelIdx = channelIdx;
    }

    public String getChannelOutIdx() {
        return channelOutIdx;
    }
    public void setChannelOutIdx(String channelOutIdx) {
        this.channelOutIdx = channelOutIdx;
    }

    public int getOctaveTran() {
        return octaveTran;
    }
    public void setOctaveTran(int octaveTran) {
        this.octaveTran = octaveTran;
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

    public int getModuleIdx() {
        return moduleIdx;
    }
    public void setModuleIdx(int moduleIdx) {
        this.moduleIdx = moduleIdx;
    }

    public int getVOL() {
        return VOL;
    }
    public void setVOL(int VOL) {
        this.VOL = VOL;
    }

    public int getEXP() {
        return EXP;
    }
    public void setEXP(int EXP) {
        this.EXP = EXP;
    }

    public int getREV() {
        return REV;
    }
    public void setREV(int REV) {
        this.REV = REV;
    }

    public int getCHO() {
        return CHO;
    }
    public void setCHO(int CHO) {
        this.CHO = CHO;
    }

    public int getMOD() {
        return MOD;
    }
    public void setMOD(int MOD) {
        this.MOD = MOD;
    }

    public int getPAN() {
        return PAN;
    }
    public void setPAN(int PAN) {
        this.PAN = PAN;
    }

    public int getTRE() {
        return TRE;
    }
    public void setTRE(int TRE) {
        this.TRE = TRE;
    }

    public int getBankIdx() {
        return bankIdx;
    }
    public void setBankIdx(int bankIdx) {
        this.bankIdx = bankIdx;
    }

    public int getFontIdx() {
        return fontIdx;
    }
    public void setFontIdx(int fontIdx) {
        this.fontIdx = fontIdx;
    }

    public int getPatchIdx() {
        return patchIdx;
    }
    public void setPatchIdx(int patchIdx) {
        this.patchIdx = patchIdx;
    }

    public String getPatchName() {
        return patchName;
    }
    public void setPatchName(String patchName) {
        this.patchName = patchName;
    }

    @Override
    public String toString() {
        return "Preset String = [presetIdx=" + presetIdx + ", channelIdx=" + channelIdx
                + ", channelOutIdx=" + channelOutIdx + ", moduleIdx=" + moduleIdx
                + ", PC=" + PC + ", LSB=" + LSB + ", MSB=" + MSB
                + ", VOL=" + VOL + ", EXP=" + EXP + ", REV=" + REV
                + ", CHO=" + CHO + ", PAN=" + PAN + ", MOD=" + MOD + ", TRE=" + TRE
                + ", bankIdx=" + bankIdx + ", fontIdx=" + fontIdx
                + ", patchIdx=" + patchIdx + ", patchName=" + patchName + "]";
    }
}
