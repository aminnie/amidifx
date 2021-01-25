package amidifx.utils;

import amidifx.models.MidiSong;
import java.util.Comparator;

public class SongNameSorter implements Comparator<MidiSong> {
    @Override
    public int compare(MidiSong o1, MidiSong o2) {
        return o2.getSongTitle().compareToIgnoreCase(o1.getSongTitle());
    }
}
