package amidifx.models;

public class MidiInstrument {

    private int TrackId;
    private int ChannelId;
    private String InstrumentName;

    public MidiInstrument() {
    }

    public MidiInstrument(int TrackId, int ChannelId, String InstrumentName) {
        super();
        this.TrackId = TrackId;
        this.ChannelId = ChannelId;
        this.InstrumentName = InstrumentName;
    }

    public int getTrackId() {
        return TrackId;
    }
    public void setTrackId(int TrackId) {
        this.TrackId = TrackId;
    }

    public int getChannelId() {
        return ChannelId;
    }
    public void setChannelId(int ChannelId) {
        this.ChannelId = ChannelId;
    }

    public String getInstrumentName() {
        return InstrumentName;
    }
    public void setInstrumentName(String InstrumentName) {
        this.InstrumentName = InstrumentName;
    }

    @Override
    public String toString() {
        return "Bank [TrackId=" + TrackId + ", ChannelId=" + ChannelId
                + ", InstrumentName=" + InstrumentName + "]";
    }

}

