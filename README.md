## Introduction to AMIDIFX

Last Update: 28 March 2021

Most recent updates:
* Added Octave Transpose to every voice on Upper and Lower Manual and every Presets Channel/Voice
* Added Layering modes for Lower 1 + 2 and Upper 1 + 2 + 3 Keyboards
* Added MIDI Expression Pedal support to Upper, Lower and Bass channels via input CHAN 16
* Added additional Midi CC controllers (VOL, EXP, REV, CHO, MOD, TIM, ATK, REL, TIM, PAN)
* Added Sound Module Support. AMDIFIX Supports MIDI GM, Deebach BlackBox and Roland Integra 7
* Added USB MIDI Keyboard IN support
* Added home screen for MIDI IN and OUT Device Selects

For a download of the latest Windows X64 build, please contact the author at: a_minnie@hotmail.com

AMIDIFIX is a component based MIDI solution intended to manage one or more MIDI Sound Modules. Hardware sound modules such as the Deebach BlackBox (https://www.deebach.eu/), Roland Integra7, or Yamaha Motif Rack ES are well suited for studio applications. However, wiith no or limited user onboard interfaces they require much configuration for realtime performances. With custom development, it is quite possible to use them in live music scenarios, and/or for instance assemble your own multi-keyboard solution with instant recall of all the settings for a pre-configured song. 

AMIDIFX is a JavaFX based solution enables a musician to:
* Integrate your keyboards into software MIDI GM or hardware sound modules such as the the Deeback Blackbox via USB or MIDI DIN
* Create backing tracks for real-time play and/or practice - as a keyboard or some other instrument player.
* Store pre-configured Song and Preset configurations for instant recall during a live performance

There are numerous free and commercial MIDI arrangements available that can be adapted to serve as backing tracks for live keyboard play: 
* There original or adapted MIDI SMF files can be used for:
  * Demo purposes, listening to arrangements that you have modified to use the capabilities of an external MIDI sound module
  * Live play of original MIDI file using MIDI GM sounds with 1 to 16 backing tracks
  * Play along purposes to listen and learn the tracks while playing a keyboard
* Preset Files program up to 8 presets with 16 channels each for every MIDI Song file. 
  * Every Preset defines parameters for all 16 MIDI channels, including the following MIDI Program and Control changes: MSB, LSB, PC, VOL, EXP, REV, CHO, MOD, PAN. 
  * Preset files allow layering of channels enabling multiple voices on a track. 
   * We plan to extend this to multiplex input channels into multiple output MIDI modules in future. 

AMIDIFIX loads easily modified Sound Module Cubase Patch files enabling the user to select, configure and test the patches for each Preset Channel. A selected Patch can tested with a single note, or while a selected MIDI SMF file is playing. Program and Control Changes to a channel are implemented realtime, enabling you to adjust the instrument voice until it matches the arrangement best. Preset files are saved to disk along with the MIDI SMF file for future play and instant recall of any of the eigth Presets programmed for the Song.

AMDIDFX optimizes Program and Control changes by tracking the most recent status for every channel. Only deltas of any of the Program or Control changes are sent to the sound MIDI modules to prevent unecessary trafic and poential sound glitches such as mid-note changes PC changes experienced on some sound modules.

AMIDIFX provides a realtime keyboard or organ interface (Perform) that is used to manage multiple MIDI keyboards, including Bass, Lower 1 + 2, Upper 1 + 2 + 3 for a MIDI capable organ.

You will find really great MIDI SMF files for free or sale on the internet. The idea is to manipluate your MIDI files enabling you to use them as backing tracks for real-time play. Typically the following changes are needed:
* Move the MIDI channels around to match our keyboard input MIDI channels. Typically I have the Bass pedals on channel 11, Lower KBD on channel 12 (& 13 for layering), and Upper keyboards on channel 14 (& 15,& 16 for layering) if extra keyboards, keyboard split or layering is used
* A MIDI SMF file can be modified to by adding the following MIDI Meta CUE Messages enables the build-in AMIDIFX MIDI Sequencer to:
  * MIDI CUE meta message P[1-8]: Trigger Preset P[1-8] changes via a callback mechanism in the MIDI Sequencer. This messages is added into the MIDI file just afer the original program changes, a fraction of a second before the first notes are played. Doing so allows MIDI play wiht the original MIDI GM sounds, or overriding them with your preset channel voice selections.
  * MIDI CUE meta message B[0-1]: Trigger Sequencer Bar Counter B[0-1] to enable a realtime Bar Coint display.
  * The MIDI CUE messages is added into the MIDI file just afer the original program changes, a number of ticks before the first notes are played to ensure alignment of the Bar counter with Bar quarter note ticks, and to allow for the MIDI Preset settings to be applied to the sound module before play starts.
  * I find MidiYodi a quick and easy tool to modify the channels to match my keyboard output channels, and add the CUE messages.

## Why am I building AMIDIFX?

AMDIDFX is the 4th incarnation of a solution that started life as a simple preset controller to help manage an Integra7 that I have connected to my Roland Atelier 90S Console organ. The preset swicthes are syncrohonized with the organ based on a MIDI command from the organ everytime a preset in the organ is changed. The most recent version and pre-cursor to AMDIDFX consists of:
* 3 x Teensy 3.2 microcontrollers: 
  * A keyboard controller that handles channel layering, expression pedal duplication
  * A 16 channel MIDI Sequencer along with a MIDI Song Playlist
  * A controller that manages a set Hammond Drawbars (https://shop.keyboardpartner.de/epages/13705466.sf/de_DE/?ObjectPath=/Shops/13705466/Products/db9).
* Raspberry PI 3B+ that hosts a 10" touch interface, the MQTT broker that shares MIDI song and Preser files with the Teensies via Ethernet plus GPIO for realtime changes.
* The resources section for AMIDI* images (https://github.com/aminnie/amidifx/tree/master/Resources). Has been running perfectly well all this time with only minor updates and never moved off the breadboard.

AMIDIFX is a replacement solution 6-7 years later. This time round we want to simplify the build and use the additional resources of newer microcontrollers. Additionally microcontrollers such as the Teensy 4.1 are significantly more powerful. The Raspberry PI, an ARM based solution did not have JAVAFX support at the time I built the solution, and I used JAVA Swing for the user interface. I never got around to building out the functionality to manage Song and Preset files using the touch interface. Typically, I managed them offline on my laptop and uploaded the files to the Raspberry PI using a terminal solution. However, using PDFs to look up e.g. the Integra's >6000 patches and not knowing what they sound like, equalizing the channels, etc. proved to be a laborious exercise.

Earlier in 2020, Deebach (https://www.deebach.eu/#xl_xr_page_blackbox) made the new Blackbox hardware sound module available. To listen, visit their Youtube channel: https://www.youtube.com/channel/UCNsB0ht9ZPpWABu3nxK_K1Q. This module offers a rich set of sounds and control paramteres at a very reasonable price. BlackBox offers two versions, the Blockbox-Y that integrates with the Yamaha Tyros, and the other - well a black box! I decided to order the BlackBox. The Deebach team has been very responsive and supportive, and initially engaged in a conversation to validate that I understand the BlackBox unit will require custom integration into my equipment. And that is the journey we are on now.

## What do I need to run AMIDFX?

You need the following hardware and software to run AMIDIFX:
* A Windows 10 (for now) 64-bit host prepared to host and run JavaFX development - see https://docs.oracle.com/javafx/release-documentation.html.
  * I use a Dell 7550 Windows 10 laptop as primary development environment
  * Of course a Windows 10 Tablet provides a nicely integrated solution to host AMIDIFX if you are looking a standalone solution or comfortable with hardware. See here for example: https://www.amazon.com/Windows-Fusion5-Ultra-Tablet-Cameras/dp/B07W6QYX8G/ref=sr_1_3?dchild=1&keywords=fusion+windows+tablet&qid=1612633174&sr=8-3

* In January 2021, I ported the solution to a single board computer (SBC) - the Seeed Odyssey: https://www.seeedstudio.com/ODYSSEY-X86J4105800-p-4445.html.
  * This X86-based SBC has 8GB RAM, support for SATA drives and SSDs (including M.2 and NVMe), as well as onboard ARM microcontroller that is integrated to the X86 via USB. This ARM controller can programmed to add low level GPIO to a AMIDIFX running on the X86. The JavaFX application forwards MIDI layering configurations to the ARM controller to manage MIDI keyboard layering, muting, etc.
  * A Screen with 1024 x 600 or better a 1280 by 800 resolution - capacitive touch preferably. I use this one: https://www.waveshare.com/10.1inch-hdmi-lcd-with-case.htm
  * A MIDI Interface DIN Board. This board provides the MIDI DIN IN and OUT connectivity to the Odyssey ARM controller via the one/two sets of Serial GPIO pins. Most MIDI interface boards will work. For example, I use the midibox.org (http://www.ucapps.de/) dual MIDI channel MBHP_MIDI_IO board. Note: This board is not needed at this time, as the MIDI keyboard can be connected directly to the AMIDIFX host via USB, or a DIN to USB converter such as: https://www.amazon.com/gp/product/B08HMWJWDW/ref=ppx_yo_dt_b_asin_title_o04_s00?ie=UTF8&psc=1

* At this time, the Deebach Blackbox (https://www.deebach.eu/) sound module, Roland Integra 7, or a MIDI GM compatible sound module is supported

* MIDI file manipulation software. MidiYodi (https://www.canato.se/midiyodi/) works great for manipulating channel events, inserting Preset and Bar Counter CUE meta messages, program changes, and moving channels around to map to you keyboard preferences. I use CHAN 11 for Bass, CHAN 12 + 13 for Lower, and CHAN 14 + 15 + 16 for Upper.

* Note: The Seeed Odessey has enough compute power and memory to act as a development host for AMIDIFX! I have installed JetBrains IntelliJ, the Arduino IDE, MidiYodi and several other applications on it, and while the 10" touch screen is relatively small for development, the solution is performant enough to make changes to the applcation (using an attached keyboard), change MIDI files, while running AMIDIFX and the built-in sequencer! At this time I am running WIndows 10 on the SBC, but all components of this solution including the IDE can be deployed on e.g. Ubuntu should you prefer to do so.

My setup: Seed Odyssey X86, Waveshare 10.1" Touch Screen, and midibox IO module with 2 In / 2 Out DIN MIDI ports:

![Example AMIDIFX Setup:](https://github.com/aminnie/amidifx/blob/master/Resources/other/AMIDIDX01182021.jpg)


## Building and Running AMIDIFX

* I have been running AMIDIFX on a Windows 10 64-bit Laptop as well as the Seeed Oddesey SBC referred to above. The latter is intended as a standalone option that can be used with touch only.
* You may use the following for building the solution: JetBrains Intellij IDEA Community Edition (free): https://www.jetbrains.com/idea/download/#section=windows
* Download the source code from this repo, build and run the solution
* Plug in GM MIDI compatible sound module or Deebach Blackbox via USB. It will be detected on startup and be available to program, test Song and Presets configurations and live play. If no external sound module is available the system will default to the built-in Synth. 
* Download the .PRE and .MID and files into the following directory on your system: C:\amidifx\midifiles
  * Preset Files end with .PRE, and have a very specific format edit via the AMIDIFX application, or editable in an editor if you understand the structure.
  * Songs List file (songs.sng) is a directory of all the Songs and their respective configurations including associated MIDI SMF and Preset files, track/channel mutes, etc.
  * Add your own MIDI files through the user interface. Don't forget to:
   * Add MIDI Cue = P[1-8] meta events to auto trigger and inject a Preset configuration into the MIDI stream. Find a place in the MIDI file following the initial channel MSB, LSB and PC changes, but before the first notes sound (often the intro symbol beats on the drum track), and insert the CUE = P1.
   * Add MIDI Cue = B[0-1] meta event to preset the Bar Counter with the music play. This may require a carefully reviewing the MIDI file in e.g. MidiYodi to determine where the first beat starts. Initial quarter lead in often is the start of Music in a Midi file.
  

## AMIDIFX Screens and User Guide

### MIDI Input Keyboard and Output Sound Module

![Example AMIDIFX Home screen:](https://github.com/aminnie/amidifx/blob/master/Resources/other/Home1.png)

How to Use the Home screen:
* Select MIDI Keyboard from detected Input Devices
* Select MIDI Output Sound Module from detected Sound Modules
* Click on Configure button and test MIDI keyboard to sound output
* Click on Perform to proceed to live play screen
* Selected input and output devices are saved to a configuration file and available as the default on next startup

### Main/Live Performance Screen

![Example AMIDIFX Perform screen:](https://github.com/aminnie/amidifx/blob/master/Resources/other/Perform1.png)

How to Use the Performance screen:
* Navigation buttons: Select Song, Select Bank and Select Voice
  * Each control consists of three separate buttons left << and >> navigate, and a text area that is used to select the current option.
  * Selecting a Bank, resets the Bank Voices to the first vocie in the Bank
  * Navigating and selecting a voice makes it available to applied to any of the Upper, Lower, Bass, or Drum soft buttons
  * Selecting a song enables / disables preset buttons and other functionality depending on Song and the sound module if was configured for
* Voice Buttons:
  * Click on a Voice button to register a new Bank and Voice on the soft button
  * Future clicks on the voice button will function like any other mechanical push button and forward the program change to the Deebach sound module.
  * Drum Channel is disabled (not shown) for MIDI GM sound modules
* Layer Buttons:
  * Layers Upper 2 and/or 3 on to Upper 1
  * Layers Lower 2 on to Lower 1
  * Also used to turn the sound on Upper 1 or Lower 1 on and off
* Effect Sliders:
  * The VOL, REV, CHO, MOD, BRI, PAN effect sliders applies to the last selected voice button in take effect in realtime just like a mechanical slider.
  * See status line for last voice selected and impacted
* Play Song:
  * Clicking on the Play button initiates play of the selected MIDI Song. The button remains active until the song ends or you click on play stop
* Backing/PlayAlong Button: This button has two modes
   * Play Along with plays up to and all 16 channels in Song Midi files.
   * Backing which mutes the MIDI tracks that contain the Upper 1, Lower 1, and Bass MIDI channels.
   * Until further notice the external keyboard MIDI channels should be configured to: Drums = 10, Bass - 11, Lower = 12, Upper = 14. Channels 1 through 9 and selected higher can be used for the backing tracks.
* Presets:
  * Every preset file can be programmed with 8 presets and 16 channels each. See the Preset Screen
  * Once a song is selected, the associated file configured in the Songs screen is loaded.
  * If the P1-8 Meta Cues have been configured in the MIDI file, playng the Song file will automatically trigger a preset load. Alternatively, you can click on a Preset button to activate the sounds configured.
  * The system is configured to track the voice and effects on every MIDI channel. When a Preset is applied, only the deltas are forwarded to the Deebach module to avoid redundant traffic and potential sound glitches. To force a Preset load, or clear the controllers, click on the Panic button.
  * If a MIDI Song plays with 'strange sounds', then the Preset file associated with the Song in the SOng screen has not been configured correctly in the Presets screen.
* Upper 1 and Lower 1 Rotary Buttons
  * Enables Rotary on and off. When turned on reverts to last Fast/Slow setting
  * Rotary Fast and Slow buttons go through a time sequence to resemble rotary spin up and slow down after button is pressed
* Save Perform button
  * Saves the current state of the voices, not unlike powering down a keyboard and having the same setting present when you turn it on again.
* Bar Counter:
  * Activated by placing a Metadate CUE B0 (lead in bar) or B1 in the midi file, just before the first note is played. A good placement tick will result in a accurate quarter note beat count. 

### Preset Configuration for selected Song

![Example AMIDIFX Songs screen:](https://github.com/aminnie/amidifx/blob/master/Resources/other/Songs1.png)

How to Use Song screen:
* Song List:
  * This is a scrollable list of all Song files loaded in the system
  * Scrolling the list updates the Song Details controls on the screen
  * Note: For the moment, a new Song added to the list will show up at the end of the list, and not supported in place - to correct this in future. When  you restart AMIDIFX the new song will appear in the sorted list.
* Edit Preset File button
  * This button switches the Preset screen with current song selection as the Preset file to edit
  * Edit Preset will only allow preset edits if the song presets was created with current active sound module 
* Song Input Controls: Any Song is created with
  * A Song Name (up to 25 characters)
  * A MIDI file that you select from the file system. In 8.3 format with every file extension .mid
  * Track Mutes:
   * Find the MIDI tracks that contain the Bass, Upper and Lower MIDI channels. MIDI Tracks abd Channels are not the same! Start playing the Song and stop and see the tracks detected to assist with Track Mute input a swell as Preset configurations. Ideally change the channels to the the Drum (10) and Bass (11), Lower (12), Upper (13) MIDI channels usng e.g. MidiYodi or equvalent software.
  * Signature:
   * At this time the system support 3/4, 4/4 and 6/8 time signatures. 
  * A Preset file that you can copy form an existing during the New Song operation. In 8.3 format with the extension .csv (yes Excel text file)
  * Note: All Songs and Preset files are contained in the folder: c:\amidifx\midifiles. Any new MIDI files should be added to this direct. Please take care with the file names and working with directory as overwriting the wrong files will cuase issues, and may leave the system inoperable. It is a good idea to keep a recent backup of the midifiles folder somewhere else incase you need to restore to a good state.
* New Button
  * Creates a new Song with details to be entered. Has basic validation input validation.
* Edit Button
  * Enables update of Song parameters
* Delete Button
  * Deletes the song from the list, but not the file system.
* Save Song Button
  * Use this Button to save a new Song or Updates or Deletes to the master Song List fle.
* Sequencer Mode Radio Buttons
  * Original; Plays the MIDI file without auto selecting Preset 1, even if coded as Metadata Cue P1 in the MIDI file
  * With Presets: Plays all 16 channels auto selecting Preset 1 when Song play is initiated. If Song play sounds off, proceed to update Presets 1 (at least in the Presets screen)
  * Backing: Upon initiating Song Play, auto selects Preset 1, and mutes the Bass, Lower, and Upper tracks configured on the Song. 
* MIDI Sound Module Indicator
  * Backing: Song Presets are coded with the current active sound module. This information is stored in the Song metadata, and in future the Song can only be played or presets updated with the same sound module connected 


### Manage Song List, MIDI SMF, and Preset Files

![Example AMIDIFX Presets screen:](https://github.com/aminnie/amidifx/blob/master/Resources/other/Presets2.png)

How to use the Preset configuration screen:
* Bank List:
  * The available sounds Bans are listed in the list on the left. If the BlackBox is connected all banks are Deebach. Otherwise the system defaults to standard MIDI GM and the built in synth.
  * Clicking on a voice bank loads the voices for this bank into the 16 voice buttons in the center of the screen
* Voice Buttons
  * 16 Voice Buttons for the last selected Bank is shown with a << left and >> right buttons to scroll.
  * The Sound Voice button can be used to play a single note in the voice button last selected.
* Play Song button
  * Initiates Song play in Preset 0 auto select mode enabling you to lsten to voice and effect changes (in realtime using the appropriate buttons)
* Effect Sliders
  * Operates in realtime and is set individually for each of the 16 MIDI channels on a Preset.
  * The buttons below the sliders can be used to default each slider as a starting point to tune a voice
  * Note: The REV, CHO and MOD slider will be developed into a pop-up with additional parameter settings in future.

* Preset Channels: How do I program a new voice into a channel?
  * Do this: 1) Select voice bank, 2) then proceed to select a Voice from this Bank, then 3) click on any of Channels 1 through 16. The voice has not registered on the Channel yet - in case you clicked on the wrong channel. Once you have your preferred channel, then 4) proceed to click the Set Voice Button.
  
* Select Voice Button:
  * Programs the selected Voice into a CHannel, and forward the PC, MSB and LSB to the sound module. If your keyboard is connected the channel in the sound module it should sound.
* Effect Sliders (again): Once you have the voice programmed into a channel continue to adjust the VOL etc. effects on this CHannel. All Effects are saved with its respective MIDI channel and every MIDI channel can be configured differently.
* Preset 1 through 1 selector:
  * At the top of the Preset List, the Preset 1 (default) dropdown can used to select different Preset to configure all 16 channels for.
* Copy Next Button:
  * Configuring all 8 presets and 16 channels is quite a task, especially if you are looking for incremental changes on just one or a few channels from one Preset to another.
  * This button takes the current Preset selected and copies it in full to the next Preset, including all program and effects configured.
  * After Copy Next, select the next Preset from the dropdown. It should be identical to the previous. Now proceed to update the channel you need to with new voices and effects  * * Apply Channel or All Channels Buttons:
  * Send the selected channel or all channel program and effect changes to the sound module if oy want to test in realtime.
* Apply Preset Buttons:
  * Cur Channel: Sends current channel Voice Patch and Effects to the MIDI sound module
  * All Channels: Sends Voice Patch and Effects for all 16 channels to the MIDI sound module
* Save Button:
  * Save the open Preset file. DO NOT forget to save your updates, otherwise you will have to redo all work since the last save.
* Reload Button:
  * If you realize that you have misconfigured the voice or effects in file, the Reload button will abort the current edit and reload the last saved version of the Preset file.
* Layers Option:
  * Every MIDI channel can be mapped to up to 10 other channels to layer additional MIDI sounds onto it.
  * This feature relies on an external custom controller currently in development and testing. The idea is that you connect your keyboard/organ to this device and the output of this controller to the sound module. The controller is updated with layers associated with each channel. Inbound keyboard notes are mapped real-time to configured output channels - up to 16 of them. Additionally external VOL control changes form the organ or MIDI volume pedal is mapped to all Keyboard Bass, Lower 1+2, Upper 1+2+3 channels as well as all layers - just like the expression pedal on an Organ. The Seeed Oddesey shown above is already integrated in this manner and I am working on the ARM controller source code. This external ARM controller will be connected to hardware drawbars as well as additional rotary encoders and buttons
 
### Current Status: 03/23/2021

Next up:
  * Updating the Song screen to list all active tracks and channels along with a track mute buttons for an easy play along mode
  * AMIDIFX was developed in JavaFX. It is portable to numerous operating systems and devices, including IOS, Android, Linux, etc.
  * Adding a USB based controller hardware with real buttons and rotary switches, and of course one or more sets of drawbars
  * More testing, and usability improvements based on user feedback
 
-- More to follow  ---
