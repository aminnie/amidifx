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
     * Obtains a copy of the data for the system exclusive message.
     * The returned array of bytes does not include the status byte.
     * sysexbuf[] = {0xF0, 0X80, 0x06, 0x00, 0x01, 0xF7};
     */
    public byte[] getSysexMessage(byte messagetype, byte[] data)  {

        byte[] newdata = new byte[data.length + 7];

        newdata[0] = (byte) (SYSTEM_EXCLUSIVE & 0xFF);
        newdata[1] = (byte) (0x50);                // Temporary Device ID = 80
        newdata[2] = (byte) (data.length + 7 & 0XFF);
        newdata[3] = (byte) (0X00);
        newdata[4] = (byte) (messagetype & 0XFF);
        System.arraycopy(data, 0, newdata, 5, data.length);
        newdata[newdata.length-1] = (byte) (END_OF_EXCLUSIVE & 0xFF);

        return newdata;
    }

    /**
     * Obtains a copy of the data for the system exclusive message.
     * The returned array of bytes does not include the status byte.
     * sysexbuf[] = {0xF0, 0X50, 0x14, 0x00, 0x02, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x00, 0x00, 0xF7};
     */
    public byte[] getLayerSysexMessage(byte messagetype, byte[] data)  {

        byte[] newdata = new byte[data.length + 6];

        newdata[0] = (byte) (SYSTEM_EXCLUSIVE & 0xFF);
        newdata[1] = (byte) (0x50);                // Temporary Device ID = 80
        newdata[2] = (byte) (0X14);
        newdata[3] = (byte) (0X00);
        newdata[4] = (byte) (messagetype & 0XFF);
        System.arraycopy(data, 0, newdata, 5, data.length);
        newdata[newdata.length-1] = (byte) (END_OF_EXCLUSIVE & 0xFF);

        return newdata;
    }

}

