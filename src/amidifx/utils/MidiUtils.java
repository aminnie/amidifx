package amidifx.utils;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class MidiUtils {

    // http://www.docjar.com/html/api/com/sun/media/sound/MidiUtils.java.html
    // https://openmidiproject.osdn.jp/index_en.html

    // https://stackoverflow.com/questions/13169941/using-the-midi-tick-position-to-control-movement-of-a-graphic-marker-in-a-sequen/13169972#13169972

    // https://jan.newmarch.name/LinuxSound/MIDI/JavaSound/

    // https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/MidiSystem.html
    // https://www.geeksforgeeks.org/java-midi/
    // https://stackoverflow.com/questions/58469147/javax-sound-midi-example-of-how-include-meta-event-t
    // https://docs.oracle.com/javase/tutorial/sound/MIDI-messages.html
    // https://www.javatips.net/api/javax.sound.midi.shortmessage
    // https://gist.github.com/tkojitu/1751867
    // https://stackoverflow.com/questions/55228242/java-midi-controllereventlistener-how-to-change-the-instrument/58469198#58469198

    // https://github.com/DerekCook/CoreMidi4J
    // https://alvinalexander.com/java/jwarehouse/openjdk-8/jdk/src/share/classes/javax/sound/midi/SysexMessage.java.shtml
    // http://www.automatic-pilot.com/midifile.html

    // https://www.codota.com/code/java/classes/javax.sound.midi.Sequencer
    // http://ungrid.unal.edu.co/Java8/tutorial/sound/MIDI-seq-adv.html

    // http://www.docjar.com/html/api/com/sun/media/sound/MidiUtils.java.html


/*

    http://docs.oracle.com/javase/tutorial/sound/MIDI-seq-adv.html

    There are a couple different things that may be of use: tools for synchronizing with other devices and special event listeners.
    The following (about the slave) looks particularly promising: Sequencer has an inner class called Sequencer.SyncMode.
    A SyncMode object represents one of the ways in which a MIDI sequencer's notion of time can be synchronized with a master or slave device.
    If the sequencer is being synchronized to a master, the sequencer revises its current time in response to certain MIDI messages from the master.
    If the sequencer has a slave, the sequencer similarly sends MIDI messages to control the slave's timing.
    If you write a "MidiSlaveDevice" that's sole job is to send triggers to your redraw, you could have the solution to your desired scenario.

    https://alvinalexander.com/java/jwarehouse/openjdk-8/jdk/src/share/classes/com/sun/media/sound/RealTimeSequencer.java.shtml

*/


}
