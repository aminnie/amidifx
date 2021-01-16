package amidifx.utils;

//https://alvinalexander.com/java/jwarehouse/openjdk-8/jdk/src/share/classes/javax/sound/midi/SysexMessage.java.shtml

/**
 * A SysexMessage object represents a MIDI system exclusive message.
 *
 * When a system exclusive message is read from a MIDI file, it always has
 * a defined length.  Data from a system exclusive message from a MIDI file
 * should be stored in the data array of a SysexMessage as
 * follows: the system exclusive message status byte (0xF0 or 0xF7), all
 * message data bytes, and finally the end-of-exclusive flag (0xF7).
 * The length reported by the SysexMessage object is therefore
 * the length of the system exclusive data plus two: one byte for the status
 * byte and one for the end-of-exclusive flag.
 *
 * As dictated by the Standard MIDI Files specification, two status byte values are legal
 * for a SysexMessage read from a MIDI file:
 *  0xF0: System Exclusive message (same as in MIDI wire protocol)
 *  0xF7: Special System Exclusive message
 *
 * When Java Sound is used to handle system exclusive data that is being received
 * using MIDI wire protocol, it should place the data in one or more
 * SysexMessages.  In this case, the length of the system exclusive data
 * is not known in advance; the end of the system exclusive data is marked by an
 * end-of-exclusive flag (0xF7) in the MIDI wire byte stream.
 *  0xF0: System Exclusive message (same as in MIDI wire protocol)
 *  0xF7: End of Exclusive (EOX)
 *
 * The first SysexMessage object containing data for a particular system
 * exclusive message should have the status value 0xF0.  If this message contains all
 * the system exclusive data
 * for the message, it should end with the status byte 0xF7 (EOX).
 * Otherwise, additional system exclusive data should be sent in one or more
 * SysexMessages with a status value of 0xF7.  The SysexMessage
 * containing the last of the data for the system exclusive message should end with the
 * value 0xF7 (EOX) to mark the end of the system exclusive message.
 *
 * If system exclusive data from SysexMessages objects is being transmitted
 * using MIDI wire protocol, only the initial 0xF0 status byte, the system exclusive
 * data itself, and the final 0xF7 (EOX) byte should be propagated; any 0xF7 status
 * bytes used to indicate that a SysexMessage contains continuing system
 * exclusive data should not be propagated via MIDI wire protocol.
 */

public class SysexUtils {

    /**
     * Status byte for System Exclusive message (0xF0, or 240).
     * Status byte for Special System Exclusive message (0xF7, or 247), which is use in MIDI files.
     */
    public static final int SYSTEM_EXCLUSIVE = 0xF0; // 240
    public static final int SPECIAL_SYSTEM_EXCLUSIVE = 0xF7; // 247
    public static final int END_OF_EXCLUSIVE = 0xF7; // 247

    // Instance variables
    protected byte[] data = null;

    /**
     * Sets the data for the system exclusive message. The first byte of the data array must be a valid system
     * exclusive status byte (0xF0 or 0xF7).
     */
    public byte[]  setSysexMessage(byte[] data, int length)  {

        byte[] newData = new byte[length+2];

        newData[0] = (byte) (SYSTEM_EXCLUSIVE & 0xFF);
        System.arraycopy(data, 0, newData, 1, newData.length);
        data[length] = (byte) (END_OF_EXCLUSIVE & 0xFF);

        return newData;
    }

    /**
     * Obtains a copy of the data for the system exclusive message.
     * The returned array of bytes does not include the status byte.
     */
    public byte[] getSysexMessage(byte[] data) {

        byte[] newData = new byte[data.length - 1];
        System.arraycopy(data, 1, newData, 0, (data.length - 1));

        return newData;
    }

}
