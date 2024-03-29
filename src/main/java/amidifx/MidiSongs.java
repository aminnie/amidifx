package amidifx;

import amidifx.models.MidiSong;
import amidifx.models.SharedStatus;
import amidifx.utils.SongNameSorter;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class MidiSongs {

    // Static variable single_instance of type PlayMidi
    private static MidiSongs single_MidiSongsInstance = null;

    // CSV file delimiter
    private static final String CSV_DELIMITER = ",";

    SharedStatus sharedStatus;

    // List for holding Patch objects - https://edencoding.com/force-refresh-scene/
    final ArrayList<MidiSong> songList = new ArrayList<>();

    // Static method to create singleton instance of PlayMidi class
    public synchronized static MidiSongs getInstance() {
        if (single_MidiSongsInstance == null) {
            single_MidiSongsInstance = new MidiSongs();

            System.out.println("MidiSongs: Creating instance MidiSongs");
        }

        return single_MidiSongsInstance;
    }

    // *** Make constructor private for Singleton ***
    private MidiSongs() {

        // Preload the Song List
        loadMidiSongs();
    }

    public void loadMidiSongs() {

        // Create instance of Shared Status to report back to Scenes
        sharedStatus = SharedStatus.getInstance();

        System.out.println("MidiSongs: Loading Song List from file " + sharedStatus.getSongList());

        BufferedReader br = null;
        try {
            // Read the csv file
            br = new BufferedReader(new FileReader(sharedStatus.getMIDDirectory() + sharedStatus.getSongList()));

            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);

                // Skip any empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] songDetails = line.split(CSV_DELIMITER);

                if (songDetails.length > 0) {
                    // Save the patch details in patch object
                    // int patchId, int patchType, int PC, int MSB, int LSB, String patchName
                    MidiSong mSong = new MidiSong(
                            Integer.parseInt(songDetails[0]),
                            songDetails[1],
                            songDetails[2],
                            songDetails[3],
                            Integer.parseInt(songDetails[4]),
                            Integer.parseInt(songDetails[5]),
                            Integer.parseInt(songDetails[6]),
                            Integer.parseInt(songDetails[7]),
                            songDetails[8]);
                    songList.add(mSong);
                }
            }

            // Print Patch List
            //for (MidiSong e : songList) {
            //    System.out.println(e.getSongId() + "   " + e.getSongTitle() + "   "
            //            + e.getMidiFile() + "   " + e.getPresetFile() + "   "
            //            + e.getModuleIdx() + "    "  + e.getTimeSig());
            //}

        }
        catch (Exception ex) {
            System.err.println("### MidiSongs: Error Reading Songs from file " + sharedStatus.getSongList());
            ex.printStackTrace();
        }
        finally {
            try {
                br.close();

                songList.sort(new SongNameSorter().reversed());
            }
            catch (IOException ie) {
                System.out.println("Error occured while closing the MIDI Song BufferedReader");
                ie.printStackTrace();
            }
        }
    }

    // Return Song at index number
    public MidiSong getSong(int idx) {

        return songList.get(idx);
    }

    // Return Song at index number
    public void addSong(MidiSong song) {
        songList.add(song);
    }

    // List Song List
    public ArrayList<MidiSong> getSongs() {
        //for (MidiSong mSong : songList) {
        //    System.out.println(mSong.toString());
        //}

        return songList;
    }

    // Return full patch at index number
    public MidiSong getMidiSong(int idx) {

        return songList.get(idx);
    }

    // Return Bank list size
    public int getSongListSize() {
        return songList.size();
    }

    // Return Song at index number
    public int sizeSongs() {
        return songList.size();
    }


    // Save current Song List to Disk
    public boolean saveSongs(boolean sort) {

        boolean statusreturn = true;

        // Ensure that Song List is saved in alphabetical order in case new New Songs has been added
        if (sort)
            songList.sort(new SongNameSorter().reversed());

        // Print Song List
        //for (MidiSong msong : songList) {
        //    System.out.println(msong.toString());
        //}

        BufferedWriter bw = null;
        try {
            //Specify the file name and path here
            File file = new File(sharedStatus.getMIDDirectory() + sharedStatus.getSongList());

            // Ensure that the fil gets created if it is not present at the specified location
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            for (int idx = 0; idx < songList.size(); idx++) {
                MidiSong song = getSong(idx);

                String songline = Integer.toString(song.getSongType());
                songline = songline.concat(",").concat(song.getSongTitle());
                songline = songline.concat(",").concat(song.getPresetFile());
                songline = songline.concat(",").concat(song.getMidiFile());
                songline = songline.concat(",").concat(Integer.toString(song.getTrackBass()));
                songline = songline.concat(",").concat(Integer.toString(song.getTrackLower()));
                songline = songline.concat(",").concat(Integer.toString(song.getTrackUpper()));
                songline = songline.concat(",").concat(Integer.toString(song.getModuleIdx()));
                songline = songline.concat(",").concat(song.getTimeSig()).concat("\r");
                bw.write(songline);

                // Reload presets on screens such as Perform it has changed
                sharedStatus.setSongReload(true);

                System.out.print("songline " + idx + ": " + songline);
            }

            System.out.println("Song file written successfully");
        }
        catch (IOException ioe) {
            System.err.println("Unable to Save the Updated Song List");
            ioe.printStackTrace();

            sharedStatus.setStatusText(" Status: Unable to Save the Updated Song List!");
            statusreturn = false;
        }
        finally {
            try {
                if(bw!=null)
                    bw.close();
            }
            catch(Exception ex) {
                System.err.println("Error in closing the Song file BufferedWriter"+ex);
                statusreturn = false;
            }
        }

        return statusreturn;
    }

    public static boolean copyFile(String source, String dest, boolean bdestdel) {

        SharedStatus sharedstatus = SharedStatus.getInstance();

        File sourceFile = new File(sharedstatus.getMIDDirectory() + source);
        File destFile = new File(sharedstatus.getMIDDirectory() + dest);

        if (bdestdel && destFile.exists()) {
            destFile.delete();
        }

        try {
            Files.copy(sourceFile.toPath(), destFile.toPath());

            System.out.println("copyFile: Copied file " + source + " to " + dest);
        }
        catch (Exception ex) {
            System.out.println("copyFile Exception: Copy file " + source + " to " + dest);
            System.out.println(ex);
            return false;
        }
        return true;
    }

    public boolean fileExist(String songfile) {

        SharedStatus sharedstatus = SharedStatus.getInstance();

        File f = new File(sharedstatus.getMIDDirectory() + songfile);
        if (!f.exists()) {
            System.out.println("MIDISongs: Error Song File does not exist: " + songfile);
            return false;
        }
        return true;
    }

}

