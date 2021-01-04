package amidifx;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

import amidifx.models.MidiSong;
import amidifx.utils.SongNameSorter;

public class MidiSongs {

    // CSV file delimiter
    private static final String CSV_DELIMITER = ",";
    private static final String MID_DIRECTORY = "C:/amidifx/midifiles/";
    private static final String songFile = "songs.csv";

    // List for holding Patch objects - https://edencoding.com/force-refresh-scene/
    final ArrayList<MidiSong> songList = new ArrayList<>();

    public void makeMidiSongs() {

        MidiSongs banks = new MidiSongs();

        BufferedReader br = null;
        try {
            // Read the csv file
            br = new BufferedReader(new FileReader(MID_DIRECTORY + songFile));

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
                            Integer.parseInt((songDetails[0])),
                            songDetails[1],
                            songDetails[2],
                            songDetails[3],
                            Integer.parseInt((songDetails[4])),
                            Integer.parseInt((songDetails[5])),
                            Integer.parseInt((songDetails[6])),
                            songDetails[7]);
                    songList.add(mSong);
                }
            }

            // Print Patch List
            //for (MidiSong e : songList) {
            //    System.out.println(e.getSongId() + "   " + e.getSongTitle() + "   "
            //            + e.getMidiFile() + "   " + e.getPresetFile() + "   "
            //            + e.getTimeSig());
            //}

        }
        catch (Exception ex) {
            System.err.println("### MidiSongs: Error Reading Songs from file " + songFile);
            ex.printStackTrace();
        }
        finally {
            try {
                br.close();
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
    public void saveSongs() {

        // Ensure that Song List is saved in alphabetical order in case new New Songs has been added
        songList.sort(new SongNameSorter().reversed());

        // Print Song List
        //for (MidiSong msong : songList) {
        //    System.out.println(msong.toString());
        //}

        BufferedWriter bw = null;
        try {
            //Specify the file name and path here
            File file = new File(MID_DIRECTORY + "songs.csv");

            // Ensure that the fil gets created if it is not present at the specified location
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            for (int idx = 0; idx < songList.size(); idx++) {
                MidiSong song = getSong(idx);

                String songline = Integer.toString(song.getSongId());
                songline = songline.concat(",").concat(song.getSongTitle());
                songline = songline.concat(",").concat(song.getPresetFile());
                songline = songline.concat(",").concat(song.getMidiFile());
                songline = songline.concat(",").concat(Integer.toString(song.getChanBass()));
                songline = songline.concat(",").concat(Integer.toString(song.getChanLower()));
                songline = songline.concat(",").concat(Integer.toString(song.getChanUpper()));
                songline = songline.concat(",").concat(song.getTimeSig()).concat("\r");
                bw.write(songline);

                //System.out.print("songline " + idx + ": " + songline);
            }

            System.out.println("Song file written Successfully");
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            try {
                if(bw!=null)
                    bw.close();
            }
            catch(Exception ex) {
                System.err.println("Error in closing the Song file BufferedWriter"+ex);
            }
        }
    }

    public static boolean copyFile(String source, String dest, boolean bdestdel) {

        File sourceFile = new File(MID_DIRECTORY + source);
        File destFile = new File(MID_DIRECTORY + dest);

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

        File f = new File(MID_DIRECTORY + songfile);
        if (!f.exists()) {
            System.out.println("MIDISongs: Error Song File does not exist: " + songfile);
            return false;
        }
        return true;
    }

}

