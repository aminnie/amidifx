## Introduction to AMIDIFX

AMIDIFIX is a component based MIDI solution used to manage one or more MIDI Sound Modules. Hardware sound modules such as the Deebach Blackbox, Roland Integra7, or Yamaha Motif Rack ES are well suited for home or production studios. They have limited user or no user interfaces and take time to configure correctly for a new song. However with custom development, it is quite possible to use them in live music scenarios and for instance to build your own multi-keyboard solution with instant recall of all the settings for a prepared song. 

AMIDIFX is a JavaFX based solution enables a keyboard user to manage:
* Integration into the local (PC-based) MIIDI GM module, and hardware modules such as the the Deeback Blackbox via USB
* There are numerous free and commercial MIDI arrangements available that can be adapted to serve as backing tracks for live keyboard play. 
* Original or adapted MIDI SMF files to be used for:
  * Demo purposes, listening to arrangements that you have modified to use the capabilities of an external MIDI sound module
  * Live play of original MIDI file using MIDI GM sounds, 1 to 16 backing tracks
  * Play along purposes to listen and learn the tracks while playing a keyboard
* Preset Files program up to 8 presets with 16 channels each for every MIDI Song file. 
  * Every Preset defines paramaters for all 16 MIDI channels, including the following MIDI Program and Control changes: MSB, LSB, PC, VOL EXP, REV, CHO, TRE. MOD, PAN. 
  * Preset file allow layering of channels enabling multiple voices for a track. We plan to extend this to multiplex input channels into multiple output MIDI modules in the near future. 

AMIDIFIX loads easily modified Sound Module Cubase Patch files enabling the user to select, configure and test the patches for each preset channel. A selected patch can tested with a single note, or while the selected MIDI SMF file is playing. Program and Control Changes to a channel are implemented realtime, enabling you to adjust the instrument voice until it matches the arrangement best. Preset files are saved to disk along with the MIDI SMF file for future plae, and instant recall of any of the eigth presets programmed for the Song.

AMDIDFX optimizes Program and Control changes by tracking the most recent status for every channel. Only deltas of any of the Program or Control changes are sent to the sound MIDI modules to prevent unecessary trafic and poential sound glitches such as mid-note changes PC changes experienced on some sound modules.

AMIDIFX Preset definitions also includes
* Mapping any MIDI input channels to output channels, or muting selected channels
* Mapping any MIDI input channel to multiple output channels to layer sounds
* Mappping/multiplexing any MIDI input channel to one or more output channels to different sound modules (future)
* Realtime multiplexing is achieved via an external microcontroller modules that hosts multiple MIDI inputs and outputs

AMIDIFX provides a realtime keyboard or organ interface (Perform) that is used to manage multiple MIDI keyboards, including the bass, lower, upper, solo manuals for a MIDI capable organ.

You will find really great MIDI SMF files for free or sale on the internet. The idea is to manipluate your MIDI files to enable you to use them as backing tracks for real-time play. Typically the following changes are needed:
* Move the MIDI channels around to match our keyboard output channels. Typically for I put the bass pedals on channel 11, lower KBD on channel 12 & 13 (layering), and upper KBDs on channel 14, 15, & 16 if extra upper keyboards, keyboard split or layering is used
* A MIDI SMF file can be modified to by adding the following MIDI Meta Messages that is enables the MIDI Sequencer to:
  * MIDI CUE meta message 1: Trigger Preset P[1-8] changes via a callback mechanism in the MIDI Sequencer. This messages is added into the MIDI file just afer the original program changes, a fraction of a second before the first notes are played. Doing so allows MIDI play wiht the original MIDI GM sounds, or overriding them with your preset channel voice selections.
  * MIDI CUE meta message 2: Trigger Sequencer Bar Counter B[0-1] to enable realtime display or Bar Counts. This messages is added into the MIDI file just afer the original program changes, a tick or so before the first notes are played to ensure alignment.
  * I typically use MidiYodi to modify the channels to match my keyboard output channels and add the CUE messages.

## Why am I building AMIDIFX?

AMDIDFX is the 4th incarnation of a solution that started life as a simple preset controller to help manage an Integra7 that I have connected to my Roland Atelier 90 organ. The preset swicthes are syncrohonized with the organ based on a MIDI command from the organ everytime a preset in the organ is changed. The most recent version and pre-cursor to AMDIDFX consists of:
* 3 x Teensy 3.2 microcontrollers: 
  * A keyboard controller that handles channel layering, expression pedal duplication
  * A 16 channel MIDI Sequencer along with a MIDI Song Playlist
  * A controller that manages a set Hammond Drawbars (https://shop.keyboardpartner.de/epages/13705466.sf/de_DE/?ObjectPath=/Shops/13705466/Products/db9).
* Raspberry PI 3B+ that hosts a 10" touch interface, the MQTT broker that shares MIDI song and Preser files with the Teensies via Ethernet plus GPIO for realtime changes.
* The resources section for AMIDI* images (https://github.com/aminnie/amidifx/tree/master/Resources). Has been running perfectly well all this time with only minor updates and never moved off the breadboard. This time round we want to simplify the build and use the additional resources of newer microcontrollers.

AMIDIFX is a replacement solution 6-7 years later. Microcontrollers such as the Teensy 4.1 and Raspberry PI4B are significantly more powerful. Raspberry PI, an ARM based solution did not have JAVAFX support at the time, and I built the interface in JAVA Swing. I never got around to building out the functionality to manage Song and Preset files using the touch interface. Typically, I managed them offline on my laptop and uploaded the files to the Raspberry PI using VNC as a terminal solution. However, using PDFs to look up e.g. the Integra's >6000 patches and not knowing what they sound like, equalizing the channels, etc. proved to be a laborious exercise.

Earlier in 2020, Deebach (https://www.deebach.eu/#xl_xr_page_blackbox) made the new Blackbox hardware sound module available. To listen, visit their Youtube channel: https://www.youtube.com/channel/UCNsB0ht9ZPpWABu3nxK_K1Q. The module offers a rich set of sounds and control paramteres and is nicely priced. BlackBox offers two versions - one that integrates with Yamaha Tyros, and the other - well a blackbox! I decided to order a unit. The Deebach team has been very responsive and supportive, and I have to add initially engaged in a conversation to validate that I understand the unit it will require custom integration. And that is the journey we are on now.  

## What do I need to run AMIDFX?

You need the following hardware and software to run AMIDIFX:
* A host prepared to host and run JavaFX development - see https://docs.oracle.com/javafx/release-documentation.html.
  * I use a Dell 7550 Windows 19 laptop as primary development environment
  * In January 2021, I ported the solution to a single board computer (SBC) - the Seeed Odyssey: https://www.seeedstudio.com/ODYSSEY-X86J4105800-p-4445.html. This X86-based SBC has 8GB Ram, support for SATA drives and SSDs (including M.2 and NVMe), and most important an onboard ARM microcontroller that can be integrated to the X86 via USB. This RM cotroller can programmed to add low level GPIO to a solution runnign on the X86. As of 01/17 I have the X86-based JAVAFX application talking to the ARM controller and am preparing to build out the ARM-based real-time keyboard interface logic.
   * Screen with 1024 x 600 or 1280 by 800 resolution. A capacitive touch screen speeds up entry. I use this one: https://www.waveshare.com/10.1inch-hdmi-lcd-with-case.htm
* For now, the Deebach Blackbox (https://www.deebach.eu/) sound module, or any MIDI GM compatible sound module is supported. The Roland Integra7 modified patch file will be availble as an option soon.
* MIDI file manipulation software. MidiYodi (https://www.canato.se/midiyodi/) works great for manipulating channel events, inserting Preset CUE meta messages, program changes, and including movign channels around
* Note: The Seeed Odessey has proved to more than enough compute power and memory to act as a development host. I have installed JetBrains IntelliJ, the Arduino IDE, MidiYodi and several other applicatiosn on it, and while the 10" touch screen is relative small for development, the solution is performant enough to make changes to the applcation (using an attached keyboard), change MIDI files, while running AMIDIFX and the built-in sequencer! At this time I am running WIndows 10 on the SBC, but all components of this solution including the IDE can be deployed on e.g. Ubuntu should you prefer to do so.

There more to do, including:
* Channel layering and multiplexing. The onboard Seeed Oddesey ARM controller will be programmed to handle MIDI layering/multiplexes via MIDI DIN connectors, as well as external buttons and switches.
* Continue to build out the real-time organ/keyboard functionality, including adding physical buttons and rotary encoders, etc.

## Building and Running AMIDIFX

* Note: As of 12/17, I have been running AMIDIFX on a Windows 10 Laptop as well as the Seeed Oddesey SBC referred to above. The latter is intended as a standalone option that can be used with touch only.
* You may use the following for building the solution: JetBrains Intellij IDEA Community Edition (free): https://www.jetbrains.com/idea/download/#section=windows
* Download the source code from this repo, build and run the solution
* Plug in GM MIDI compatible sound module or Deebach Blackbox via USB. It will be detected on startup and be available to program, test Song and Presets configurations and live play. If no external sound module is available the system will default to the built-in Synth. 
* Download the .CSV and .MID and .DAT files into the following directory on your system: C:\amidifx\midifiles
  * Preset Files end with .CSV, and have a very specific format edit via the AMIDIFX applicaion, or editable in an editor if you understand the structure.
  * Songs List file (songs.csv) is a directory of all the Songs and their respective configurations including associated MIDI SMF and Preset files, track/channel mutes, etc.
  * Add your own MIDI files through the user interface. Don't forget to:
   * Add MIDI Cue = P[1-8] meta events to auto trigger and inject a Preset configuration into the MIDI stream. Find a place in the MIDI file following the initial channel MSB, LSB and PC changes, but before the first notes sound (often the intro symbol beats on the drum track), and insert the CUE = P1.
   * Add MIDI Cue = B[0] meta event to preset the Bar Counter with the music play. This may require carefull reviewing the MIDI file in e.g. MidiYodi to determine where the first beat starts. Initial cymbal quarter lead in often is the start of Music in a Midi file.

## AMIDIFX Example Screens

### Main/Live Performance Screen (WiP)

![Example AMIDIFX Preset screen:](https://github.com/aminnie/amidifx/blob/master/Resources/Perform.png)

### Preset Configuration for selected Song

![Example AMIDIFX Preset screen:](https://github.com/aminnie/amidifx/blob/master/Resources/Presets.png)

### Manage Song List, MIDI SMF, and Preset Files

![Example AMIDIFX Preset screen:](https://github.com/aminnie/amidifx/blob/master/Resources/Songs.png)


### Current Status: 01/17/2021
* Built out three AMDIDFX screens: 
  * Songs: Create/update new Songs with associated MIDI SMF and Preset configuration files
  * Presets: For a selected Song, define all 16 MIDI channels, including keyboard real-time play (performance mode on channels 10 - 16), and backing tracks 
  * Perform: Real-time keyboard/organ configuration in MIDI GM and Deebach Blackbox modes. Defaults o Deebach if detected on start-up
  * Java MIDI Sequencer integrated and playing in demo and demo with preset more. Backing mode channels 10 - 16 mute issue to be resolved.
* Other:
  * Effect Sliders on all screens work in realtime, and adjust the current/last channel and voice selected. 
* Next up:
  * Deeper integraton into Deebach: Complete Rotary on/off, etc.
  * Complete programming the Odessey onboard ARM controller to manage layring and multiplexing of incoming keyboard MIDI data.
 

-- More to follow  ---
