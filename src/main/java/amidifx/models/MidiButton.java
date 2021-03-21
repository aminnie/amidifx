package amidifx.models;

public class MidiButton {

    private int buttonIdx;
    private String buttonId;
    private int patchId;
    private String patchName;
    private int layerIdx;
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
    private int MOD;
    private int TIM;
    private int ATK;
    private int REL;
    private int BRI;
    private int PAN;

    public MidiButton() {
        this.setButtonIdx(0);
        this.setButtonId("B0-0");
        this.setPatchId(0);
        this.setPatchName("No Voice");
        this.setLayerIdx(0);
        this.setChannelIdx(0);
        this.setChannelOutIdx("0");
        this.setOctaveTran(0);
        this.setPC(0);
        this.setLSB(0);
        this.setMSB(0);
        this.setVOL(90);
        this.setEXP(127);
        this.setREV(20);
        this.setCHO(10);
        this.setMOD(0);
        this.setTIM(0);
        this.setATK(0);
        this.setREL(0);
        this.setBRI(64);
        this.setPAN(64);
    }

    // ButtonIdx + ButtonId + 0,12,12,0,106,121,100,0,100,100,20,0,0,0,0,3,8,Klaus sein Sax
    public MidiButton(int buttonIdx, String buttonId, int patchId, String patchName, int layerIdx,
                      int channelIdx, String channelOutIdx, int octaveTran,
                      int PC, int LSB, int MSB, int moduleIdx,
                      int VOL, int EXP, int REV, int CHO, int MOD, int TIM, int ATK, int REL, int BRI, int PAN) {
        super();

        this.buttonIdx = buttonIdx;
        this.buttonId = buttonId;
        this.patchId = patchId;
        this.patchName = patchName;
        this.layerIdx = layerIdx;
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
        this.MOD = MOD;
        this.TIM = TIM;
        this.ATK = ATK;
        this.REL = REL;
        this.BRI = BRI;
        this.PAN = PAN;
    }

    public int getButtonIdx() {
        return buttonIdx;
    }

    public void setButtonIdx(int buttonIdx) {
        this.buttonIdx = buttonIdx;
    }

    public String getButtonId() {
        return buttonId;
    }

    public void setButtonId(String buttonId) {
        this.buttonId = buttonId;
    }

    public int getPatchId() {
        return patchId;
    }

    public void setPatchId(int patchId) {
        this.patchId = patchId;
    }

    public String getPatchName() {
        return patchName;
    }

    public void setPatchName(String patchName) {
        this.patchName = patchName;
    }

    public int getLayerIdx() {
        return layerIdx;
    }

    public void setLayerIdx(int layerIdx) {
        this.layerIdx = layerIdx;
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

    public int getTIM() {
        return TIM;
    }
    public void setTIM(int TIM) {
        this.TIM = TIM;
    }

    public int getATK() {
        return ATK;
    }
    public void setATK(int ATK) {
        this.ATK = ATK;
    }

    public int getREL() {
        return REL;
    }
    public void setREL(int REL) {
        this.REL = REL;
    }

    public int getBRI() {
        return BRI;
    }
    public void setBRI(int BRI) {
        this.BRI = BRI;
    }

    public int getPAN() { return PAN; }
    public void setPAN(int PAN) { this.PAN = PAN; }

    @Override
    public String toString() {
        return "Preset String = [buttonIdx=" + buttonIdx + ", buttonId=" + buttonId
                + ", patchId=" + patchId + ", patchName=" + patchName
                + ", layerIdx" + layerIdx +  ", channelIdx=" + channelIdx
                + ", channelOutIdx=" + channelOutIdx + ", moduleIdx=" + moduleIdx
                + ", octaveTran=" + octaveTran
                + ", PC=" + PC + ", LSB=" + LSB + ", MSB=" + MSB
                + ", VOL=" + VOL + ", EXP=" + EXP
                + ", REV=" + REV + ", CHO=" + CHO
                + ", MOD=" + MOD +  ", TIM" + TIM
                + ", ATK=" + ATK +  ", REL" + REL
                + ", BRI" + BRI + ", PAN=" + PAN+ "]";
    }
}

