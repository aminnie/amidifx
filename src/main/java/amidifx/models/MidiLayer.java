package amidifx.models;

/** MIDILayer is a subset of the MIDI Preset data to be shared with the ARM MIDI Controller on:
 * 1. Preset selection from the AMIDIFX UI:
 * 1a: Initial selection of Preset 1: forwards all 8 * 16 layer messages to thr ARM controller
 * 1b: On subsequent preset selections, forwards a delta of the changes to the ARM controller
 * 2: From the AMDIDFX UI, the user may select layer on Upper 1 & 2 & 3, or Lower 1 & 2
 * 3: An output mapping of channel = 0 mutes the channel. Note all channels are index 1 based to enable a muting ndicator of 0
 *
 * Note: The output channel string as stored in the preset file is parsed into a byte array structure to enable quicker resolution in the ARM
 * controller during note play
*/

public class MidiLayer {
    private int presetIdx;
    private int channelIdx;
    private int channelOutIdx;
    private int octaveTran;
    private int moduleIdx;
    private int patchIdx;

    public MidiLayer(MidiPreset midipreset) {

        this.presetIdx = midipreset.getPresetIdx();
        this.channelIdx = midipreset.getChannelIdx();
        this.channelOutIdx = midipreset.getChannelOutIdx();
        this.octaveTran = midipreset.getOctaveTran();
        this.moduleIdx = midipreset.getModuleIdx();
        this.patchIdx = midipreset.getPatchIdx();
    }

    // 0,12,12,0,106,121,100,0,100,100,20,0,0,0,0,3,8,Klaus sein Sax
    public MidiLayer(int presetIdx, int channelIdx, int channelOutIdx, int octaveTran,
                      int moduleIdx, int patchIdx) {

        this.presetIdx = presetIdx;
        this.channelIdx = channelIdx;
        this.channelOutIdx = channelOutIdx;
        this.octaveTran = octaveTran;
        this.moduleIdx = moduleIdx;
        this.patchIdx = patchIdx;
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

    public int getChannelOutIdx() {
        return channelOutIdx;
    }
    public void setChannelOutIdx(int channelOutIdx) {
        this.channelOutIdx = channelOutIdx;
    }

    public int getOctaveTran() {
        return octaveTran;
    }
    public void setOctaveTran(int octaveTran) {
        this.octaveTran = octaveTran;
    }

    public int getModuleIdx() {
        return moduleIdx;
    }
    public void setModuleIdx(int moduleIdx) {
        this.moduleIdx = moduleIdx;
    }

    public int getPatchIdx() {
        return patchIdx;
    }
    public void setPatchIdx(int patchIdx) {
        this.patchIdx = patchIdx;
    }

    @Override
    public String toString() {
        return "Preset String = [presetIdx=" + presetIdx + ", channelIdx=" + channelIdx
                + ", channelOutIdx=" + channelOutIdx + ", moduleIdx=" + moduleIdx
                + ", patchIdx=" + patchIdx + "]";
    }
}
