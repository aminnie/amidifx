## Introduction to AMIDIFX

AMIDIFIX is a component based MIDI solution used to manage one or more MIDI Sound Modules. Hardware sound modules such as the Roland Integra7, Yamaha Motif Rack ES, and the new Deebach Blackbox are well suited for home or production studios. They have limited user or no user interfaces and take time to configure correctly for a new song. However with custom development, it is quite possible to use them in live music scenarios and for instance to build your own multi-keyboard solution with instant recall of all the settings for a prepared song. 

AMIDIFX is a JavaFX based solution enables a keyboard user to manage:
- MIDI SMF files to be used for demo purposes, live play backing tracks, or play along purposes. There are many free and commercial MIDI arrangements available that can be adapted for to serve as backing tracks while you play. 
- Preset Files that is used to program up to 8 presets for every MIDI Song file. Every Preset defines the following paramaters for all 16 MIDI channels, including the following MIDI Program and Control changes: MSB, LSB, PC, VOL EXP, REV, CHO, TRE. MOD, PAN. Preset file allow layering of channels, and I plan to extend this to multiplexing into multiple output MIDI modules in the near future. 

AMIDIFIX loads modified Cubase Patch files into memory enabling the user to select, configure and test the patches for each channel. A selected patch can tested with a single note, or the current selected MIDI SMF file. Control Changes can be applies in realtime to adjust the instrument until it matches your need best. Preset files are saved to disk along with the MIDI SMF file for future plae, and instant recall of any of the eigth presets programmed for the Song.

AMDIDFX optimizes Program and Control changes by tracking the most recent status for every channel. Only deltas of any of the Program or Control changes are sent to the sound modules to prevent sound glitches such as mid-note changes PC changes experienced on sound modules.

AMIDIFX Preset definitions also includes
- Mapping MIDI input channels to output channels
- Mapping a MIDI input channel to multiple output channels to layer sounds
- Mappping/multiplexing any MIDI input channel to one or more output channels to different sound modules
- Realtime multiplexing is achieved via an external microcontroller modules that hosts multiple MIDI inputs and outputs

AMIDIFX provides a realtime keyboard or organ interface that is used to handle multiple MIDI keyboards or for example the lower, upper, solo manuals and bass pedals for a MIDI capable organ.

You will find really great MIDI SMF files for free or sale on the internet. The idea is to manipluate your MIDI files to enable you to use them as backing tracks for real-time play. Typically the following changes are needed:
- Move the MIDI channels around so that you are able to disable channels your would like to play live on your keyboard(s). Typically for I put the bass pedals on channel 11, lower KBD on channel 12, and upper KBDs on channel 13, 14, 15 if extra upper keyboards, keyboard split or layering is used
- Add MIDI Meta Messages: Use the CUE meta message to trigger AMIDIFX Preset P[1-8] changes via a callback mechanism.

## Why am I building AMIDIFX?

AMDIDFX is the 4th incarnation of a solution that started life as a simple preset controller to help manage an Integra7 that I have connected to my Roland Atelier 90 organ. The preset swicthes are syncrohonized with the organ based on a MIDI command from the organ everytime a preset in the organ is changed. The most recent version and pre-cursor to AMDIDFX consists of:
* 3 x Teensy 3.2 microcontrollers: A keyboard controller that handles channel layering, expression pedal duplication, and a set of Hammond Drawbars.
* Raspberry PI 3B+ that hosts a 10" touch interface, the MQTT broker that shares MIDI song and Preser files with the Teensies via Ethernet plus GPIO for realtime changes.
* The resources section for AMIDI* images (https://github.com/aminnie/amidifx/tree/main/AMIDIFX/Resources). Has been running perfectly well all this time with only minor updates and never moved off the breadboard. This time round we want to simplify the build and use the additional resources of newer microcontrollers.

AMIDIFX is a replacement solution 6-7 years later. Microcontrollers such as the Teensy 4.1 and Raspberry PI4B are significantly more powerful. Raspberry PI, an ARM based solution did not have JAVAFX support at the time, and I built the interface in JAVA Swing. I never got around to building out the functionality to manage Song and Preset files using the touch interface. Typically, I managed them offline on my laptop and uploaded the files to the Raspberry PI using a terminal solution. However, using PDFs to look up e.g. the Integra's >6000 patches and not know what they sound like, equalizing the channels proved to be a laborious exercise.

Earlier in 2020, Deebach (https://www.deebach.eu/#xl_xr_page_blackbox) made the new BlackBox hardware sound module available. To listen visit their Youtube channel: https://www.youtube.com/channel/UCNsB0ht9ZPpWABu3nxK_K1Q. It is a great module with rich sounds and nicely priced. BlackBox offers two versions - one that integrates with Yamaha Tyros, and the other - well a blackbox! I decided to order a unit. The Deebach team has been very responsive and supportive, and I have to add even cautios in initially selling the unit to me wanting verify that I understand it will require custom integration. And that is the journey we are on now.  

## What do I need to run AMIDFX?

You need the following hardware and software to run AMIDIFX:
* A host prepared to host and run JavaFX development - see https://docs.oracle.com/javafx/release-documentation.html.
  * As of today I am running the solution on a Dell 7550 laptop, but the intention is to run this on a 4GB Raspberry Pi 4B or better (if needed) SBC. 
  * Screen with 1024 x 600 resolution minimum. A capacitive touch screen speeds up entry. I use this one: https://www.waveshare.com/10.1inch-hdmi-lcd-with-case.htm
* For now, the Deebach Blackbox (https://www.deebach.eu/) sound module, or any MIDI GM compatible sound module. The Roland Integra7 modified patch file will be availble as an option soon.
* MIDI file manipulation software. MidiYodi (https://www.canato.se/midiyodi/) works great for manipulating channel events, inserting Preset CUE meta messages, program changes, and including movign channels around

There is much more to do, including:
* Optimizing the Java MIDI Sound API (https://docs.oracle.com/javase/tutorial/sound/overview-MIDI.html) currently integrated into AMIDIFX.
* Channel layering and multiplexing. May require sperate a microcontoller that duplicates/multiplexes an inbound MIDI channel on specified output channels.
* Continue to build out the real-time organ/keyboard functionality, including adding physical buttons and rotary encoders, etc.
* Refactoring the solution to support multiple controllers and/or FXML.

## Building and Running AMIDIFX

* Note: As of 12/29, I have been running AMIDIFX on a Windows 10 Laptop and have not ported it to the Raspberry Pi yet.
* You may use the following for building the solution: JetBrains Intellij IDEA Community Edition (free): https://www.jetbrains.com/idea/download/#section=windows
* Download the source code from this repo, build and run the solution
* Plug in GM MIDI compatible sound module or Deebach Blackbox via USB. It will be detected on startup and be available to program and test Presets. If no external synth is available the system will default to the built-in Synth. 
* Download the .CSV and .MID and .DAT files into the following directory on your system: C:\amidifx\midifiles
  * Preset Files end with .CSV, and have a very specific format, but editable once you understand it (to do: add more details)
  * Songs file is a directory of all the Songs, associated MIDI SMF and Preset files
  * Add your own MIDI files through the user interface. Don't forget to:
   * Add MIDI Cue = P[1-8] meta events to auto trigger and inject a Preset configuration into the MIDI stream. Find a place in the MIDI file following the initial channel MSB, LSB and PC changes, but before the first notes sound (often the intro symbol beats on the drum track), and insert the CUE = P1.
   * Add MIDI Cue = B[0] meta event to preset the Bar Counter with the music play. This may require carefull reviewing the MIDI file in e.g. MidiYodi to determine where the first beat starts. Initial cymbal quarter lead in often is the start of Music in a Midi file.

## AMIDIFX Example Screens

### Main/Live Performance Screen (WiP)

![Example AMIDIFX Preset screen:](https://github.com/aminnie/amidifx/blob/main/AMIDIFX/Resources/Perform.png)

### Preset Configuration for selected Song

![Example AMIDIFX Preset screen:](https://github.com/aminnie/amidifx/blob/main/AMIDIFX/Resources/Presets.png)

### Manage Song List, MIDI SMF, and Preset Files

![Example AMIDIFX Preset screen:](https://github.com/aminnie/amidifx/blob/main/AMIDIFX/Resources/Songs.png)



### Current Status: 12/31/2020
* Built out initial three AMDIDFX screens: 
 * Songs: Create/update new Songs with associated MIDI SMF and Preset configuration files
 * Presets: For a selected Song, define all 16 MIDI channels, including keyboard real-time play (performance mode on channels 10 - 16), and backing tracks
 * Perform: Real-time keyboard/organ configuration in MIDI GM and Deebach Blackbox modes. Defaults o Deebach if detected on start-up
 * Java MIDI Sequencer integrated and playing in demo and demo with preset more. Backing mode channels 10 - 16 mute issue to be resolved.
* Other:
 * Effect Sliders on all screens work in realtime, and adjust the last channel and voice selected. 
* Next up:
 * More testing of all three screens
 * Deeper integraton into Deebach: Complete Rotary on/off, etc.
 * Determine if MIDI Java Library can be used for Layering or if an external Microcontroller will be used
* More testing and adjustments.
 * Disable Channel 10 MIDI drums on Screen when in MIDI GM Perform mode.
* Determine if we will be using the Raspberry PI4 for the standalone implementation or a more powerful Intel-based SBC, e.g. Seeed Odessey (https://www.seeedstudio.com/ODYSSEY-c-1492.html)
 



-- More to follow  ---
