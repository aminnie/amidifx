## Introduction to AMIDIFX

Last Update: 06 February 2021

AMIDIFIX is a component based MIDI solution used to manage one or more MIDI Sound Modules. Hardware sound modules such as the Deebach BlackBox, Roland Integra7, or Yamaha Motif Rack ES are well suited for home or production studios. They have limited user or no user interfaces and take time to configure correctly for a new song. However with custom development, it is quite possible to use them in live music scenarios and for instance to build your own multi-keyboard solution with instant recall of all the settings for a prepared song. 

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
  * In January 2021, I ported the solution to a single board computer (SBC) - the Seeed Odyssey: https://www.seeedstudio.com/ODYSSEY-X86J4105800-p-4445.html. This X86-based SBC has 8GB Ram, support for SATA drives and SSDs (including M.2 and NVMe), and most important an onboard ARM microcontroller that can be integrated to the X86 via USB. This ARM cotroller can programmed to add low level GPIO to a solution running on the X86. The JavaFX application will be forwarding MIDI layering configurations to the ARM controller to manage keyboard layering and muting.
   * Screen with 1024 x 600 or 1280 by 800 resolution - capactive touch. I use this one: https://www.waveshare.com/10.1inch-hdmi-lcd-with-case.htm
* At this time, the Deebach Blackbox (https://www.deebach.eu/) sound module, or any MIDI GM compatible sound module is supported. The Roland Integra7 modified patch file will be availble as an option soon.
* A MIDI Interface DIN Board. This board provides the MIDI DIN IN and OUT connectivty to the Odyssey ARM controler via the one/two sets of Serial GPIO pins. Most MIDI interface boards will work. For example, I use the midibox.org (http://www.ucapps.de/) dual MIDI channel MBHP_MIDI_IO board.
* MIDI file manipulation software. MidiYodi (https://www.canato.se/midiyodi/) works great for manipulating channel events, inserting Preset and Bar Counter CUE meta messages, program changes, and moving channels around to map to you keyboard preferences. I use CHAN 11 for Bass, CHAN 12 + 13 for Lower, and CHAN 14 + 15 + 16 for Upper.
* Note: The Seeed Odessey has proved to more than enough compute power and memory to act as a development host. I have installed JetBrains IntelliJ, the Arduino IDE, MidiYodi and several other applicatiosn on it, and while the 10" touch screen is relative small for development, the solution is performant enough to make changes to the applcation (using an attached keyboard), change MIDI files, while running AMIDIFX and the built-in sequencer! At this time I am running WIndows 10 on the SBC, but all components of this solution including the IDE can be deployed on e.g. Ubuntu should you prefer to do so.

In process development includes:
* Channel layering and multiplexing. The onboard Seeed Oddesey ARM controller will be programmed to handle MIDI layering/multiplexes via MIDI DIN connectors, as well as external buttons and switches.

Current setup with Seed Odyssey, Waveshare 10.1" Touch Screen, and midibox IO module with 2 In / 2 Out DIN MIDI ports:

![Example AMIDIFX Setup:](https://github.com/aminnie/amidifx/blob/master/Resources/other/AMIDIDX01182021.jpg)

## Building and Running AMIDIFX

* I have been running AMIDIFX on a Windows 10 Laptop as well as the Seeed Oddesey SBC referred to above. The latter is intended as a standalone option that can be used with touch only.
* You may use the following for building the solution: JetBrains Intellij IDEA Community Edition (free): https://www.jetbrains.com/idea/download/#section=windows
* Download the source code from this repo, build and run the solution
* Plug in GM MIDI compatible sound module or Deebach Blackbox via USB. It will be detected on startup and be available to program, test Song and Presets configurations and live play. If no external sound module is available the system will default to the built-in Synth. 
* Download the .CSV and .MID and .DAT files into the following directory on your system: C:\amidifx\midifiles
  * Preset Files end with .CSV, and have a very specific format edit via the AMIDIFX applicaion, or editable in an editor if you understand the structure.
  * Songs List file (songs.csv) is a directory of all the Songs and their respective configurations including associated MIDI SMF and Preset files, track/channel mutes, etc.
  * Add your own MIDI files through the user interface. Don't forget to:
   * Add MIDI Cue = P[1-8] meta events to auto trigger and inject a Preset configuration into the MIDI stream. Find a place in the MIDI file following the initial channel MSB, LSB and PC changes, but before the first notes sound (often the intro symbol beats on the drum track), and insert the CUE = P1.
   * Add MIDI Cue = B[0-1] meta event to preset the Bar Counter with the music play. This may require a carefully reviewing the MIDI file in e.g. MidiYodi to determine where the first beat starts. Initial quarter lead in often is the start of Music in a Midi file.

## AMIDIFX Example Screens

### Main/Live Performance Screen (WiP)

![Example AMIDIFX Preset screen:](https://github.com/aminnie/amidifx/blob/master/Resources/other/Perform.png)

Usage Notes:
* Navigation buttons: Select Song, Select Bank and Select Voice
 * Each control consists of three seperate buttons left << and >> navigate, and a text area that is used to select the current option.
 * Selecting a Bank, resets the Bacnk Voices to the first vocie in the Bank
 * Navigating and selecting a voice makes it available to applied to any of the Upper, Lower, Bass, or Drum soft buttons
* Voice Buttons:
 * Click on a Voice button to register a new Bank and Voice on the soft button
 * Future clicks on the voice button will function like any other mechanical push button and forward the program change to the Deebach sound module.
* Effect sliders:
 * The VOL, REV, CHO, MOD, PAN effect sliders applies to the last selected voice button in take effect in realtime just like a mechanical slider.
* Play Song:
 * Clicking on the Play button initiates play of the selected MIDI Song. The button remains active until the song ends or you click on play stop
 * Next to the Play Song button: This buttons has two modes
  * Play Along with plays up to and all 16 channels in Song Midi files.
  * Backing mutes the MIDI tracks that contains contains the Upper 1, Lower 1, and Bass MIDI channels. Until further notice the external keyboard MIDI channels should be configured to: Drums = 10, Bass - 11, Lower = 12, Upper = 14. UpperNote that the tracks to be muted. CHannels 1 through 9 and selected higher can be used for the backing tracks.
* Presets:
 * Every preset file can be programmed with 8 presets and 16 channels each. See the Preset Screen
 * Once a song is selected, the associated file configured in the Songs screen is loaded.
 * If the P1-8 Meta Cues have been configured in the MIDI file, playng the Song file will automaticall trigger a preset load. ALternatively, you can click on a Preset button to activate the sounds configured.
  * The system is configured to track the voice and effects on every MIDI channel. When a Preset is applied, only the deltas are forwarded to the Deebach module to avoid redundant traffic and potential sound glicthes. To force a Preset load, or clear the controllers, click on the Panic button.
  * If a MIDI Song plays with 'strange sounds', then the Preset file associated with the Song in the SOng screen has not been configured correctly in the Presets screen.
* Save Perform buttoon
 * Saves the current state of the voices, not unlike powering down a keyboard and having the same setting present when you turn it on again.
* Bar Counter:
 * I sactivated by placing a Metadate CUE B0 (lead in bar) or B1 in the midi file, just before the first note is played. A good placement tick will result in a acurate o qyarter note bar count. 
* Other Notes:
 * The Hammond section is to be built out in future and the rotary buttons have not been implemented yet. The plan is to support soft and physical drawbar controllers in the future
 * You will notice that the Lower 1, Upper 1, 2 and 3 labels are in effect disbled buttons. These buttons activate once it seens an external ARM controller that is used to manage keyboard layering configured in the Preset screen - see below for more.
 * Please connect the Deebach controller to the USB port on your computer before start AMIDIFX. If not, AMIDIFX will select the built in software synth.

### Preset Configuration for selected Song

![Example AMIDIFX Preset screen:](https://github.com/aminnie/amidifx/blob/master/Resources/other/Songs.png)

* Song List:
 * This is a scrollable list of all Song files loaded in the system
 * Scrolling the list updates the Song Details controls on the screen
 * Note: For the moment, a new Song added to the list will show up at the end of the list, and not supported in place - to correct this in future. When  you restart AMIDIFX the new song will appear in the sorted list.
* Edit Preset File button
 * This button switches the Preset screen with current song selection as the Preset file to edit
* Song Input Controls: Any Song is created with
 * A Song Name (up to 25 chaaracters
 * A MIDI file that you select from the file system. In 8.3 format with every file extension .mid
 * Track Mutes:
  * FInd the MIDI tracks that contain the Bass, Upper and Lower MIDI channels. MIDI Tracks abd Channels are not the same! Start playing the Song and stop and see the tracks detected to assist with Track Mute input a swell as Preset configurations. Ideally change the channels to the the Drum (10) and Bass (11), Lower (12), Upper (13) MIDI channels usng e.g. MidiYodi or equvalent software.
 * Signature:
  * At this time the system support 3/4 and 4/4. To be extended to correctly count 6/8, etc. 
 * A Preset file that you can copy form an existing during the New Song operation. In 8.3 format with the extension .csv (yes Excel text file)
  * Note: All Songs and Preset files are contained in the folder: c:\amidifx\midifiles. Any new MIDI files should be added to this direct. Please take care with the file names and working with directory as overwriting the wrong files will cuase issues, and may leave the system inoperable. It is a good idea to keep a recent backup of the midifiles folder somewhere else incase you need to restore to a good state.
 * New Button
  * Creates a new Song with details to be entered. Has basic validation input validation.
 * Edit Button
  * Enables update of Song paramaters
 * Delete Button
  * Deletes the song from the list, but not the file system.
 * Save Song Button
  * Use this Button to save a new Song or Updates or Deletes to the master Song List fle.
 * Sequencer Mode Radio Buttons
  * Original; Plays the MIDI file without auto selecting Preset 1, even if coded as Metadata Cue P1 in the MIDI file
  * With Presets: Plays all 16 channels auto selecting Preset 1 when Song play is initiated. If Song play sounds off, proceed to update Presets 1 (at least in the Presets screen)
  * Backking: Upon initiating Song Play, auto selects Preset 1, and mutes the Bass, Lower, and Upper tracks configured on the Song. 

### Manage Song List, MIDI SMF, and Preset Files

![Example AMIDIFX Preset screen:](https://github.com/aminnie/amidifx/blob/master/Resources/other/Presets.png)

This is where I started out! We have nearly 1400 sounds 'locked' up in the Deebakc BlacckBox. We need a way to get to them and make them available for realtime organ keuboard play! 
 * Bank List:
  * The available sounds Bans are listed in the list on the left. If the BlackBox is connected all banks are Deebach. Otherwise the system defaults to standard MIDI GM and the built in synth.
  * Clicking on a voice bank loads the voices for this bank into the 16 voice buttons in the center of the screen
 * Voice Buttons
  * 16 Voice Buttons for the last selected Bank is shown with a << left and >> right buttons to scroll.
  * The Sound Voice button can be used to play a single note in the voice button last selected.
* Play Song button
 * Initiates Song play in Preset 0 autoselect mode enabling you to lsten to voice and effect changes (in realtime using the appropriate buttons)
* Effect Sliders:
 * Operates in realtime and is set individually for each of the 16 MIDI channels on a Preset.
 * The buttons below the sliders can be used to defualt each slider as a starting point to tune a voice
 * NoteL The REV, CHO and MOD slider will be developed into a pop-up with additional paramter settings in future.
* Preset Channels: How do I program a new voice into a channel?
 * Do this: 1) Select voice bank, 2) then proceed to select a Voice from this Bank, then 3) click on any of Channels 1 through 16. The voice has not registered on the Channel yet - in case you clicked on the wrong channel. Once you have yoru preferred channel, then 4) proceed to click on the Set Voice Button.
* Select Voice Button:
 * Programs the selected Voice into a CHannel, and forward the PC, MSB and LSB to the sound module. If you keyboard is connected the channel in the sound module it should sound.
 * Effect sLiders (again): Once you have the vocie programmed into a channelm continue to adjust the VOL etc. effects on this CHannel. All Effects are saved with its respective MIDI channel and every MIDI channel can be configured differently.
* Preset 1 through 1 selector:
 * At the top of the Preset List the, Preset 1 (default) dropdown can used to select different Preset to configure all 16 channels for.
* Copy Next Button:
 * Configuring all 8 presets and 16 channels is quite a task, especially if you are looking for incremental changes on just one or a few channels from one Preset to another.
 * This button takes the current Preset selected and copies it in full to the next Preset, including all program and effects configured.
 * After Copy Next, select the next Preset from the dropdown. It should be identical to the previous. Now proceed to update the channel you need to with new voices ad effecs.
* Apply Channel or All Channels Buttons:
 * Send the selected channel or all channel program abd effect changes to the sound module if oy want to test in realtime.
* Save Button:
 * Save the open Preset file. DO NOT forget to save your updates, otherwise you will have to redo all work since the last save.
* Reload Button:
 * If you realize you have misconfigered the voice or effetcs in file, the Reload buttons will abort the current edit and reload the Preset file base don the last save.
* Layers Option:
 * Every MIDI channel can be mapped to up to 10 other channels to layer additional MIDI sounds onto it.
 * This feature reiies on an external custom controller currently in development and testing. The idea is that you connect your keyboard/organ to this device and the output of this controller to the sound module. The controller is updated with layers associated with each channel. Inbound keyboard notes are mapped real-time to configured output channels - up to 16 of them. Additioanlly external VOL control changes form the organ or MIDI volume pedal is mapped to all Keyboard Bass, Lower 1+2, Upper 1+2+3 channels as well as all layers - just like the expression pedal on an Organ. The Seeed Oddesey shown above is already integrated in this manner and I am workinng on the ARM controller source code. This extenrnal ARM controller will be connected to hardware drawbars as well as additioal rotary encoders and buttons.

 
### Current Status: 01/31/2021
** Next up:
  * Building out the ARM controller
  * Deeper integraton into Deebach: Complete rotary on/off, drawbar integration, etc.
  * More testing and usability inprovements
** AMIDIFX was developed in JavaFX. It is portable to numerous operating systems and devices, including IOS, Andriod, Linux, etc!
** The AMIDIFx keyboard controller can run on sinngle board computer  (such as the Seeed above), or you laptop/tablet. The exteral ARM controller us accessed over USB, and it is possible to connect this controller to a host via a second USB port if no GPIO is availavble. A basic Windows 10 host (PC/Laptop/Tablet) with 4GB of RAM and a touch screen will do for the current build.

 
-- More to follow  ---
