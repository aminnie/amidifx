package amidifx.scenes;

import amidifx.*;
import amidifx.models.*;
import amidifx.utils.AppConfig;
import amidifx.utils.ArduinoUtils;
import amidifx.utils.MidiDevices;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;
import java.io.File;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;


public class PerformScene {

    // Scaling based on default 1024 x 600 resolution.
    // Anything other resolution overrides and may require additional tuning of screen layout
    final static float xscreensize = 1280;
    final static float yscreensize = 800;

    float xmul = xscreensize/1024f;
    float ymul = yscreensize/600f;

    int ifsize = 15;
    int xscene = (int)(1024 * xmul);
    int yscene = (int)(600 * ymul - 45);    // 45 = Adjustment while we are single showing Windows status bar

    int xtoolbarleft = (int)(225 * xmul);
    int xtitle = (int)(200 * xmul);
    int xtoolbarright = (int)(150 * xmul);

    int xleftright = (int)(50 * xmul);
    int yleftright = (int)(25 * ymul);

    int xbtnleftright = (int)(190 * xmul);
    int ybtnleftright = (int)(25 * ymul);

    int xbtnpreset = (int)(120 * xmul);
    int ybtnpreset = (int)(50 * ymul);

    int xvoicebtn = (int)(130 * xmul);
    int yvoicebtn = (int)(50 * ymul);

    int xstatusleft = (int)(400 * xmul);

    // Calculate font size based on screen dimensions. Default = 15 for 1024 * 600
    final String fsize = Integer.toString((int)(ifsize * xmul)) + "; ";
    final String fsizetitle = Integer.toString((int)(ifsize * xmul * 1.1)) + "; ";
    final String fsizesmall = Integer.toString((int)(ifsize * xmul * 8/10)) + "; ";

    // Button Colors
    // https://yagisanatode.com/2019/08/06/google-apps-script-hexadecimal-color-codes-for-google-docs-sheets-and-slides-standart-palette/
    final String bgpanecolor = "-fx-background-color: #000000; ";
    final String bgheadercolor = "-fx-background-color: #B2B5B1; ";
    final String bgfootercolor = "-fx-background-color: #B2B5B1;";

    final String rcolorOff = "-fx-background-color: #ea9999; -fx-font-size: " + fsize ;
    final String rcolorOn = "-fx-background-color: #e06666; -fx-font-size: " + fsize ;

    final String lcolorOff = "-fx-background-color: #ffe599; -fx-font-size: " + fsize ;
    final String lcolorOn = "-fx-background-color: #f1c232; -fx-font-size: " + fsize ;

    final String dcolorOff = "-fx-background-color: #a2c4c9; -fx-font-size: " + fsize ;
    final String dcolorOn = "-fx-background-color: #76a5af; -fx-font-size: " + fsize ;

    final String bcolorOff = "-fx-background-color: #a4c2f4; -fx-font-size: " + fsize ;
    final String bcolorOn = "-fx-background-color: #6d9eeb; -fx-font-size: " + fsize ;

    final String pcolorOff = "-fx-background-color: #DBD06B; -fx-font-size: " + fsize ;
    final String pcolorOn = "-fx-background-color: #C3B643; -fx-font-size: " + fsize ;

    final String orgcolorOff = "-fx-background-color: #b7b7b7; -fx-font-size: " + fsize ;
    final String orgcolorOn = "-fx-background-color: #f3f3f3; -fx-font-size: " + fsize ;

    final String selectcolorOff = "-fx-background-color: #69a8cc; -fx-font-size: " + fsize ;
    final String selectcolorOn = "-fx-background-color: #4493C0; -fx-font-size: " + fsize ;

    final String btnMenuOn = "-fx-background-color: #4493C0; -fx-font-size: " + fsize ;
    final String btnMenuOff = "-fx-background-color: #69A8CC; -fx-font-size: " + fsize ;
    final String btnMenuSaveOn = "-fx-background-color: #DB6B6B; -fx-font-size: " + fsize ;
    final String btnMenuSaveOff = "-fx-background-color: #B2B5B1; -fx-font-size: " + fsize ;

    final String btnplayOff = "-fx-background-color: #8ED072; -fx-font-size: " + fsize ;
    final String btnplayOn = "-fx-background-color: #DB6B6B; -fx-font-size: " + fsize ;

    final String lrpressedOn = "-fx-background-color: #8ED072; -fx-font-size: " + fsize ;
    final String lrpressedOff = "-fx-background-color: #DB6B6B; -fx-font-size: " + fsize ;

    final String styletext = "-fx-text-fill: black; -fx-font-size: " + fsize ;
    final String styletextwhite = "-fx-text-fill: white; -fx-font-size: " + fsize ;
    final String styletextwhitesmall = "-fx-text-fill: white; -fx-font-size: " + fsizesmall ;
    final String styletextred = "-fx-text-fill: red; -fx-font-size: " + fsize ;
    final String styletexttitle = "-fx-font-size: " + fsizetitle;

    Stage primaryStage;
    Scene returnScene;

    SharedStatus sharedStatus;
    PlayMidi playmidifile;
    ArduinoUtils arduinoUtils;

    // Main pane for the Perform scene
    private BorderPane paneWelcome;
    Scene scenePerform;

    MidiPresets dopresets;
    MidiSongs dosongs;
    MidiPatches dopatches;
    MidiModules midimodules;
    MidiButtons midiButtons;

    File fstyle;

    String songTitle;
    String songFile;
    String presetFile;

    private String performgm = "midigm.prf";
    String buttonFile = performgm;

    Button buttonSave;
    boolean flgDirtyPreset = false;

    int idxSongList = 0;
    int bankidx = 0;
    String bankname;
    String fontname;
    int patchidx = 0;
    int bankpatchidx = 0;
    boolean bnewpatchselected = false;

    boolean bplaying = false;
    boolean btestnote = false;

    Label labelstatusOrg;
    Label labelsynth;

    // Midi Play Button
    Button btnplay;

    Button buttonSoundBank = new Button();
    Button buttonSoundFont = new Button();

    Button btnpreset1 = new Button();
    Button btnpreset2 = new Button();
    Button btnpreset3 = new Button();
    Button btnpreset4 = new Button();
    Button btnpreset5 = new Button();
    Button btnpreset6 = new Button();
    Button btnpreset7 = new Button();
    Button btnpreset8 = new Button();

    // Tracking Preset Buttons
    boolean ppressed1 = false;
    boolean ppressed2 = false;
    boolean ppressed3 = false;
    boolean ppressed4 = false;
    boolean ppressed5 = false;
    boolean ppressed6 = false;
    boolean ppressed7 = false;
    boolean ppressed8 = false;

    String lastVoiceButton = "U1-1";
    int lastVoiceChannel = 1;

    // Tracking Upper1 Buttons
    Button rbutton11 = new Button();
    Button rbutton12 = new Button();
    Button rbutton13 = new Button();
    Button rbutton14 = new Button();
    Button rbutton15 = new Button();
    Button rbutton16 = new Button();
    Button rbutton17 = new Button();
    Button rbutton18 = new Button();

    boolean rpressed11 = false;
    boolean rpressed12 = false;
    boolean rpressed13 = false;
    boolean rpressed14 = false;
    boolean rpressed15 = false;

    boolean rpressed16 = false;
    boolean rpressed17 = false;
    boolean rpressed18 = false;

    // Tracking Upper2 Buttons
    Button rbutton21 = new Button();
    Button rbutton22 = new Button();
    Button rbutton23 = new Button();
    Button rbutton24 = new Button();

    boolean rpressed21 = false;
    boolean rpressed22 = false;
    boolean rpressed23 = false;
    boolean rpressed24 = false;

    // Tracking Upper3 Buttons
    Button rbutton31 = new Button();
    Button rbutton32 = new Button();
    Button rbutton33 = new Button();
    Button rbutton34 = new Button();

    boolean rpressed31 = false;
    boolean rpressed32 = false;
    boolean rpressed33 = false;
    boolean rpressed34 = false;

    // Tracking Lower Buttons
    Button lbutton11 = new Button();
    Button lbutton12 = new Button();
    Button lbutton13 = new Button();
    Button lbutton14 = new Button();
    Button lbutton15 = new Button();
    Button lbutton16 = new Button();

    boolean lpressed11 = false;
    boolean lpressed12 = false;
    boolean lpressed13 = false;
    boolean lpressed14 = false;
    boolean lpressed15 = false;
    boolean lpressed16 = false;

    Button lbutton21 = new Button();
    Button lbutton22 = new Button();
    Button lbutton23 = new Button();
    Button lbutton24 = new Button();

    boolean lpressed21 = false;
    boolean lpressed22 = false;
    boolean lpressed23 = false;
    boolean lpressed24 = false;

    // Tracking Bass Buttons
    Button bleft1 = new Button();
    Button bleft2 = new Button();
    Button bleft3 = new Button();
    Button bleft4 = new Button();

    boolean bpressed1 = false;
    boolean bpressed2 = false;
    boolean bpressed3 = false;
    boolean bpressed4 = false;

    // Tracking Drum Buttons
    Button dleft1 = new Button();
    Button dleft2 = new Button();
    Button dleft3 = new Button();
    Button dleft4 = new Button();

    boolean dpressed1 = false;
    boolean dpressed2 = false;
    boolean dpressed3 = false;
    boolean dpressed4 = false;

    Button btnbacking;      // Backing mode Play configuration Button
    int playmode = 3;       // 2 = Play ALong, 3 = Backing mode

    Button r1layerbtn;       // Righthand Layering Buttons
    Button r2layerbtn;
    Button r3layerbtn;

    boolean r1pressed = true;
    boolean r2pressed = false;
    boolean r3pressed = false;

    Button l1layerbtn;       // Lefthand Layering Buttons
    Button l2layerbtn;

    boolean l1pressed = true;
    boolean l2pressed = false;

    Slider sliderVOL;
    Slider sliderEXP;
    Slider sliderREV;
    Slider sliderCHO;
    Slider sliderMOD;
    Slider sliderPAN;
    Slider sliderROT;
    int rotvalue = 0;

    // https://professionalcomposers.com/midi-cc-list/
    public static byte ccVOL = 7;
    public static byte ccEXP = 11;
    public static byte ccROT = 74;
    public static byte ccREV = 91;
    public static byte ccCHO = 93;
    public static byte ccMOD = 1;
    public static byte ccPAN = 10;


    /*********************************************************
     * Creates a Perform Scene.
     *********************************************************/

    //public PerformScene(Stage primaryStage, Scene returnScene) {
    public PerformScene(Stage primaryStage) {

        System.out.println("PerformScene: AMIDIFX Perform Scene Starting");

        try {
            // Create instance of Shared Status to report back to Scenes
            sharedStatus = SharedStatus.getInstance();

            AppConfig config = AppConfig.getInstance();

            // To Do: Generalize the first two Songs in the Song List and ensure cannot be deleted
            if (config.getSoundModuleIdx() == 1) {
                sharedStatus.setPresetFile("defaultdb.pre");
                idxSongList = 0;
            }
            else if (config.getSoundModuleIdx() == 2) {
                sharedStatus.setPresetFile("defaultin.pre");
                idxSongList = 2;
            }
            else {
                sharedStatus.setPresetFile("defaultgm.pre");
                idxSongList = 1;
            }

            // Get instance of Arduino Utilities
            arduinoUtils = ArduinoUtils.getInstance();

            // Load MIDI Default MIDI Preset file on start up
            dopresets = MidiPresets.getInstance();
            presetFile = sharedStatus.getPresetFile();
            dopresets.makeMidiPresets(presetFile);
            System.out.println("PerformScene Init: Loaded new Preset file: " + presetFile);

            // Load Song List
            dosongs = sharedStatus.getDoSongs();
            songTitle = dosongs.getSong(idxSongList).getSongTitle();
            System.out.println("PerformScene Init: Song Title: " + songTitle);

            // Load MIDI Patch files on start up based on detected and preferred sound module
            // Load MIDI Sound Module List on start up
            midimodules = new MidiModules();

            dopatches = MidiPatches.getInstance();
            int moduleidx = config.getSoundModuleIdx();
            String modulefile = midimodules.getModuleFile(moduleidx);
            if (!dopatches.fileExist(modulefile)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("AMIDIFX Perform Scene Error");
                alert.setHeaderText("Module Patch file " + sharedStatus.getCFGDirectory() + modulefile + " not found!");
                Optional<ButtonType> result = alert.showAndWait();

                System.exit(-1);
            }
            dopatches.loadMidiPatches(modulefile);

            // Preset the Sound Bank and Voice Selection Lists on startup
            bankname = dopatches.getMidiBanks().getMidiBank(bankidx).getBankName();
            buttonSoundBank.setText(bankname);
            fontname = dopatches.getMIDIPatch(patchidx).getPatchName();
            buttonSoundFont.setText(fontname);

            // Prepare MIDI Player and Select Deebach Blackbox as output
            // Prepare the Channel Program and Effects tracking list
            playmidifile = PlayMidi.getInstance();
            playmidifile.initCurPresetList();

            // Prepare Voice Buttons Mappings by loading either Deebach or MidiGM Organ Files
            midiButtons = MidiButtons.getInstance();

            if (sharedStatus.getPerformFile() != null) {
                buttonFile = sharedStatus.getPerformFile();
            }
            else {
                System.err.println("### PerformScene Error: Perform file not found. Selected default: " + buttonFile);
            }
            midiButtons.loadMidiButtons(buttonFile);

            // Start Building the Scene

            System.out.println("PerformScene: Scene PerformScene!");

            BorderPane borderPaneOrg = new BorderPane();
            borderPaneOrg.setStyle(bgpanecolor);

            scenePerform = new Scene(borderPaneOrg, xscene, yscene);
            scenePerform.getStylesheets().clear();
            scenePerform.getStylesheets().add("style.css");

            sharedStatus.setPerformScene(scenePerform);

            labelstatusOrg = new Label(" Status: Ready");
            labelstatusOrg.setStyle(styletext);
            Label labelsongtitle = new Label("");
            Label labelstatus = new Label("");

            // Create top bar navigation buttons

            Button buttonsc1 = new Button("Perform");
            buttonsc1.setStyle(btnMenuOn);
            buttonsc1.setOnAction(e -> {
                //System.out.println(("PerformScene: Changing to Perform Scene " + sharedStatus.getPerformScene().toString()));
                primaryStage.setScene(sharedStatus.getPerformScene());
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.err.println("PerformScene: Unable to set Perform Scene!");
                }
            });

            Button buttonsc2 = new Button("Songs");
            buttonsc2.setStyle(btnMenuOff);
            buttonsc2.setOnAction(e -> {
                //System.out.println(("PerformScene: Changing to Song Scene " + sharedStatus.getSongScene().toString()));
                primaryStage.setScene(sharedStatus.getSongsScene());
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.err.println("PerformScene: Unable to set Songs Scene!");
                }
            });

            Button buttonsc3 = new Button("Presets");
            buttonsc3.setStyle(btnMenuOff);
            buttonsc3.setDisable(true);
            buttonsc3.setOnAction(e -> {
                //System.out.println(("PerformScene: Changing to Presets Scene " + sharedStatus.getPresetsScene().toString()));
                primaryStage.setScene(sharedStatus.getPresetsScene());
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.err.println("PerformScene: Unable to set Presets Scene!");
                }
            });

            // Save Performance Button
            buttonSave = new Button("Save Perform");
            buttonSave.setStyle(btnMenuSaveOn);
            buttonSave.setDisable(true);
            buttonSave.setOnAction(event -> {
                if (flgDirtyPreset) {

                    boolean bsave = midiButtons.saveButtons(buttonFile);
                    if (bsave) {
                        labelstatusOrg.setText(" Status: Button Voices saved");
                    } else {
                        labelstatusOrg.setText(" Status: Button Voices save error!");
                    }
                    buttonSave.setDisable(true);
                    flgDirtyPreset = false;
                }
                else
                    labelstatusOrg.setText(" Status: Voices not changed. No need to save");
            });

            Button buttonPanic = new Button("  Panic  ");
            buttonPanic.setStyle(btnMenuOff);
            buttonPanic.setOnAction(e -> {
                PlayMidi playmidifile = PlayMidi.getInstance();
                playmidifile.sendMidiPanic();

                labelstatusOrg.setText(" Status: MIDI Panic Sent");
            });

            Button buttonExit = new Button("  Exit  ");
            buttonExit.setStyle(btnMenuOff);
            buttonExit.setOnAction(e -> {
                playmidifile.stopMidiPlay("End Play");

                try {
                    Receiver midircv = sharedStatus.getRxDevice();
                    midircv.close();
                }
                catch (Exception ex) {
                    System.out.println("Exception on receiver close");
                }
                try {
                    Transmitter midixmt = sharedStatus.getTxDevice();
                    midixmt.close();
                }
                catch (Exception ex) {
                    System.out.println("Exception on transmitter close");
                }
                try {
                    Sequencer midiseq = sharedStatus.getSeqDevice();
                    midiseq.close();
                }
                catch (Exception ex) {
                    System.out.println("Exception on sequencer close");
                }

                //arduinoUtils.closePort();

                System.exit(0);
            });

            ToolBar toolbarLeft = new ToolBar(buttonsc1, buttonsc2, buttonsc3);
            toolbarLeft.setStyle(bgheadercolor);
            toolbarLeft.setMinWidth(xtoolbarleft);

            // Header Text
            DropShadow ds = new DropShadow();
            ds.setOffsetY(3.0f);
            ds.setColor(Color.color(0.4f, 0.4f, 0.4f));

            Text lbltitle1 = new Text();
            lbltitle1.setEffect(ds);
            lbltitle1.setCache(true);
            lbltitle1.setX(10.0f);
            lbltitle1.setY(270.0f);
            lbltitle1.setFill(Color.BLACK);
            lbltitle1.setText(config.getControllerTitle());
            lbltitle1.setFont(Font.font(null, FontWeight.SEMI_BOLD, 20));

            HBox hboxTitle = new HBox();
            hboxTitle.setPadding(new Insets(10, 10, 10, xtitle));
            hboxTitle.getChildren().add(lbltitle1);

            ToolBar toolbarRight = new ToolBar(buttonSave, buttonPanic, buttonExit);
            toolbarRight.setStyle(bgheadercolor);
            toolbarRight.setMinWidth(xtoolbarright);

            BorderPane borderPaneTop = new BorderPane();
            borderPaneTop.setStyle(bgheadercolor);

            // Assemble the Menu Bar Border Pane
            borderPaneTop.setLeft(toolbarLeft);
            borderPaneTop.setCenter(hboxTitle);
            borderPaneTop.setRight(toolbarRight);

            // Build the Song Title Selection Controls
            Button buttonSongLoad = new Button(songTitle);
            buttonSongLoad.setStyle(selectcolorOff);
            buttonSongLoad.setPrefSize(xbtnleftright, ybtnleftright);
            buttonSongLoad.setAlignment(Pos.CENTER);
            buttonSongLoad.setOnAction(e -> {
                buttonPresetLoad(dosongs.getSong(idxSongList).getPresetFile());

                buttonSongLoad.setStyle(selectcolorOn);

                sharedStatus.setPresetFile(dosongs.getSong(idxSongList).getPresetFile());
                sharedStatus.setMidiFile(dosongs.getSong(idxSongList).getMidiFile());
                sharedStatus.setSongTitle(dosongs.getSong(idxSongList).getSongTitle());
                sharedStatus.setStatusText("Selected Preset File " + presetFile);

                // Preset Time Signature for correct Bar Time Display
                sharedStatus.setTimeSig(dosongs.getSong(idxSongList).getTimeSig());

                // Check if Sound Module matches the active Module, enable/disable Presets and Autoload Preset 0
                boolean modulematch = disablePresetButtons(sharedStatus.getModuleidx(), dosongs.getSong(idxSongList).getModuleIdx());
                if (modulematch) {
                    ////dopresets.makeMidiPresets(sharedStatus.getPresetFile());
                    ////buttonPresetAction(0);
                    ////btnpreset1.setStyle(pcolorOn);
                    labelstatusOrg.setText(" Status: Loaded Presets for " + dosongs.getSong(idxSongList).getSongTitle());
                    labelstatusOrg.setStyle(styletext);

                    // Enable Song Play Button
                    btnplay.setDisable(false);
                    btnbacking.setDisable(false);
                }
                else {
                    labelstatusOrg.setText(" Status: Connect Module "
                            + sharedStatus.getModuleName(dosongs.getSong(idxSongList).getModuleIdx())
                            + " for Song " + dosongs.getSong(idxSongList).getSongTitle());
                    labelstatusOrg.setStyle(styletextred);

                    // Enable Song Play Button
                    btnplay.setDisable(true);
                    btnbacking.setDisable(true);
                }

            });

            Button buttonSongNameLeft = new Button("<<");
            buttonSongNameLeft.setStyle(selectcolorOff);
            buttonSongNameLeft.setPrefSize(xleftright, yleftright);
            buttonSongNameLeft.setOnAction(e -> {

                if (idxSongList > 0) --idxSongList;
                songTitle = dosongs.getSong(idxSongList).getSongTitle();
                songFile = dosongs.getSong(idxSongList).getMidiFile();
                buttonSongLoad.setText(songTitle);

                // New Song to be selected for Play
                buttonSongLoad.setStyle(selectcolorOff);
                btnplay.setDisable(true);

                offAllPresetButtons();

                //System.out.println("PerformScene: Previous Song " + songTitle);
            });

            Button buttonSongNameRight = new Button(">>");
            buttonSongNameRight.setStyle(selectcolorOff);
            buttonSongNameRight.setPrefSize(xleftright, yleftright);
            buttonSongNameRight.setOnAction(e -> {

                if (idxSongList < (dosongs.getSongListSize() - 1)) idxSongList++;
                songTitle = dosongs.getSong(idxSongList).getSongTitle();
                songFile = dosongs.getSong(idxSongList).getMidiFile();
                buttonSongLoad.setText(songTitle);

                // New Song selected
                buttonSongLoad.setStyle(selectcolorOff);
                btnplay.setDisable(true);

                offAllPresetButtons();

                //System.out.println("PerformScene: Next Song " + songTitle);
            });

            // Assemble the Song Navigation Controls
            HBox hboxSong = new HBox();
            hboxSong.setSpacing(2);
            hboxSong.getChildren().add(buttonSongNameLeft);
            hboxSong.getChildren().add(buttonSongLoad);
            hboxSong.getChildren().add(buttonSongNameRight);

            FlowPane flowSong = new FlowPane();
            flowSong.setHgap(10);
            flowSong.setVgap(10);
            flowSong.getChildren().add(hboxSong);

            // Build the Sound Bank Selection Controls

            buttonSoundBank.setPrefSize(xbtnleftright, ybtnleftright);
            buttonSoundBank.setStyle(selectcolorOff);
            buttonSoundBank.setAlignment(Pos.CENTER);
            buttonSoundBank.setOnAction(e -> {
                // Load the start Patch IDx for the selected Bank
                patchidx = dopatches.getMidiBanks().getMidiBank(bankidx).getPatchIdx();
                bankpatchidx = patchidx;

                // Preset the Sound Font (Voice) with the start PatchName
                fontname = dopatches.getMIDIPatch(patchidx).getPatchName();
                buttonSoundFont.setText(fontname);

                labelsynth.setText(config.getOutDevice());

                buttonSoundBank.setStyle(selectcolorOn);
            });

            Button buttonSoundBankLeft = new Button("<<");
            buttonSoundBankLeft.setStyle(selectcolorOff);
            buttonSoundBankLeft.setPrefSize(xleftright, yleftright);
            buttonSoundBankLeft.setOnAction(e -> {
                if (bankidx > 0) --bankidx;
                bankname = dopatches.getMidiBanks().getMidiBank(bankidx).getBankName();
                buttonSoundBank.setText(bankname);

                buttonSoundBank.setStyle(selectcolorOff);
                buttonSoundFont.setStyle(selectcolorOff);
                bnewpatchselected = false;

                //System.out.println("PerformScene: Previous Bank " + bankname);
            });

            Button buttonSoundBankRight = new Button(">>");
            buttonSoundBankRight.setStyle(selectcolorOff);
            buttonSoundBankRight.setPrefSize(xleftright, yleftright);
            buttonSoundBankRight.setOnAction(e -> {
                if (bankidx < dopatches.getMidiBanks().getMidiBankSize() -1 ) bankidx++;
                bankname = dopatches.getMidiBanks().getMidiBank(bankidx).getBankName();
                buttonSoundBank.setText(bankname);

                buttonSoundBank.setStyle(selectcolorOff);
                buttonSoundFont.setStyle(selectcolorOff);
                bnewpatchselected = false;

                //System.out.println("PerformScene: Previous Bank " + bankname);
            });

            // Assemble the Song Navigation Controls
            HBox hboxBank = new HBox();
            hboxBank.setSpacing(2);
            hboxBank.getChildren().add(buttonSoundBankLeft);
            hboxBank.getChildren().add(buttonSoundBank);
            hboxBank.getChildren().add(buttonSoundBankRight);

            FlowPane flowBank = new FlowPane();
            flowBank.setHgap(10);
            flowBank.setVgap(10);
            flowBank.getChildren().add(hboxBank);

            // Build the Sound Voice Selection Controls

            buttonSoundFont.setPrefSize(xbtnleftright, ybtnleftright);
            buttonSoundFont.setStyle(selectcolorOff);
            buttonSoundFont.setAlignment(Pos.CENTER);
            buttonSoundFont.setOnAction(e -> {
                //buttonPresetLoad(dosongs.getSong(songidx).getPresetFile());

                buttonSoundFont.setStyle(selectcolorOn);
                labelstatusOrg.setText(" Status: Loaded Voice for " + fontname + ". Press Voice Button to Add.");

                bnewpatchselected = true;

                //System.out.println("PerformScene: Loaded Voice " + fontname);
            });

            Button buttonSoundFontLeft = new Button("<<");
            buttonSoundFontLeft.setStyle(selectcolorOff);
            buttonSoundFontLeft.setPrefSize(xleftright, yleftright);
            buttonSoundFontLeft.setOnAction(e -> {
                if (patchidx > bankpatchidx) --patchidx;
                fontname = dopatches.getMIDIPatch(patchidx).getPatchName();
                buttonSoundFont.setText(fontname);

                ///// Get the Patch index so we can test voice
                ////patchidx = dopatches.getMIDIPatch(patchidx).getPatchId();

                //System.out.println("PerformScene: Previous Voice " + fontname);
            });

            Button buttonSoundFontRight = new Button(">>");
            buttonSoundFontRight.setStyle(selectcolorOff);
            buttonSoundFontRight.setPrefSize(xleftright, yleftright);
            buttonSoundFontRight.setOnAction(e -> {
                if (patchidx < dopatches.getMIDIPatchSize() - 1) ++patchidx;

                fontname = dopatches.getMIDIPatch(patchidx).getPatchName();
                buttonSoundFont.setText(fontname);

                ///// Get the Patch index so we can test voice
                ////patchidx = dopatches.getMIDIPatch(patchidx).getPatchId();

                //System.out.println("PerformScene: Next Voice " + fontname);
            });

            // Assemble the Sound Font Navigation Controls
            HBox hboxFont = new HBox();
            hboxFont.setSpacing(2);
            hboxFont.getChildren().add(buttonSoundFontLeft);
            hboxFont.getChildren().add(buttonSoundFont);
            hboxFont.getChildren().add(buttonSoundFontRight);

            // Voice Test Button
            Button btntest = new Button("Sound");
            btntest.setStyle(btnplayOff);
            btntest.setPrefSize(xbtnleftright / 2 - 10, ybtnleftright);
            btntest.setOnAction(e -> {
                try {

                    if (!btestnote) {
                        btntest.setText("Stop");
                        btntest.setStyle(btnplayOn);

                        PlayMidi playmidifile = PlayMidi.getInstance();
                        MidiPatch patch = dopatches.getMIDIPatch(patchidx);
                        //System.out.println("PerformScene: Selecting patch " + patch.toString());

                        // Note: Monitor as using CHAN 15 by default may cause unexpected behavior.
                        playmidifile.sendMidiProgramChange((byte)(lastVoiceChannel), (byte)patch.getPC(), (byte)patch.getLSB(), (byte)patch.getMSB());
                        playmidifile.sendMidiNote((byte)(lastVoiceChannel), (byte)60, true);

                        playmidifile.sendMidiControlChange((byte)lastVoiceChannel, ccVOL, (byte)sliderVOL.getValue());
                        //playmidifile.sendMidiControlChange((byte)lastVoiceChannel, ccEXP, (byte)sliderEXP.getValue());
                        playmidifile.sendMidiControlChange((byte)lastVoiceChannel, ccREV, (byte)sliderREV.getValue());
                        playmidifile.sendMidiControlChange((byte)lastVoiceChannel, ccCHO, (byte)sliderCHO.getValue());
                        playmidifile.sendMidiControlChange((byte)lastVoiceChannel, ccMOD, (byte)sliderMOD.getValue());
                        playmidifile.sendMidiControlChange((byte)lastVoiceChannel, ccPAN, (byte)sliderPAN.getValue());
                        //playmidifile.sendMidiControlChange((byte)lastVoiceChannel, ccTRE, (byte)sliderTRE.getValue());

                        btestnote = true;
                    }
                    else {
                        btntest.setText("Sound");
                        btntest.setStyle(btnplayOff);

                        PlayMidi playmidifile = PlayMidi.getInstance();
                        playmidifile.sendMidiNote((byte)lastVoiceChannel, (byte)60, false);

                        btestnote = false;
                    }
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            });

            FlowPane flowFont = new FlowPane();
            flowFont.setHgap(10);
            flowFont.setVgap(10);
            flowFont.getChildren().add(hboxFont);

            // Add Song, Bank and Font Select to Top Line
            GridPane gridTopLine = new GridPane();
            gridTopLine.setHgap(10);
            gridTopLine.setVgap(10);
            gridTopLine.add(flowSong, 0, 0, 1, 1);
            gridTopLine.add(flowBank, 1, 0, 1, 1);
            gridTopLine.add(flowFont, 2, 0, 1, 1);
            gridTopLine.add(btntest, 3, 0, 1, 1);

            // *** Start Bottom Status Bar

            BorderPane borderStatusOrg = new BorderPane();
            borderStatusOrg.setStyle(bgpanecolor);

            labelstatusOrg.setText(" Status: " + sharedStatus.getStatusText());
            labelsongtitle.setText("Song: " + sharedStatus.getSongTitle());
            labelsongtitle.setStyle(styletext);

            FlowPane panefilesOrg = new FlowPane();
            panefilesOrg.setHgap(20);
            panefilesOrg.getChildren().add(labelsongtitle);

            VBox vboxstatusLeftOrg = new VBox();
            vboxstatusLeftOrg.setMinWidth(xstatusleft);
            vboxstatusLeftOrg.getChildren().add(labelstatus);

            // Assemble the Status Bar BorderPane View
            borderStatusOrg.setLeft(vboxstatusLeftOrg);
            borderStatusOrg.setCenter(panefilesOrg);

            // *** End of Bottom Status Bar

            // Preset Buttons in Middle/Center Flowpane

            ppressed1 = false;
            btnpreset1 = new Button("Preset 1");
            btnpreset1.setStyle(pcolorOff);
            btnpreset1.setOnAction(event -> {
                offAllPresetButtons();
                buttonPresetAction(0);
                labelstatusOrg.setText(" Status: Applying Preset 1");
                if (!ppressed1) {
                    btnpreset1.setStyle(pcolorOn);
                } else {
                    btnpreset1.setStyle(pcolorOff);
                }
                ppressed1 = !ppressed1;
            });
            btnpreset1.setPrefSize(xbtnpreset, ybtnpreset);

            ppressed2 = false;
            btnpreset2 = new Button("Preset 2");
            btnpreset2.setStyle(pcolorOff);
            btnpreset2.setOnAction(event -> {
                offAllPresetButtons();
                buttonPresetAction(1);
                labelstatusOrg.setText(" Status: Applying Preset 2");
                if (!ppressed2) {
                    btnpreset2.setStyle(pcolorOn);
                } else {
                    btnpreset2.setStyle(pcolorOff);
                }
                ppressed2 = !ppressed2;
            });
            btnpreset2.setPrefSize(xbtnpreset, ybtnpreset);

            ppressed3 = false;
            btnpreset3 = new Button("Preset 3");
            btnpreset3.setStyle(pcolorOff);
            btnpreset3.setOnAction(event -> {
                offAllPresetButtons();
                buttonPresetAction(2);
                labelstatusOrg.setText(" Status: Applying Preset 3");
                if (!ppressed3) {
                    btnpreset3.setStyle(pcolorOn);
                } else {
                    btnpreset3.setStyle(pcolorOff);
                }
                ppressed3 = !ppressed3;
            });
            btnpreset3.setPrefSize(xbtnpreset, ybtnpreset);

            ppressed4 = false;
            btnpreset4 = new Button("Preset 4");
            btnpreset4.setStyle(pcolorOff);
            btnpreset4.setOnAction(event -> {
                offAllPresetButtons();
                buttonPresetAction(3);
                labelstatusOrg.setText(" Status: Applying Preset 4");
                if (!ppressed4) {
                    btnpreset4.setStyle(pcolorOn);
                } else {
                    btnpreset4.setStyle(pcolorOff);
                }
                ppressed4 = !ppressed4;
            });
            btnpreset4.setPrefSize(xbtnpreset, ybtnpreset);

            ppressed5 = false;
            btnpreset5 = new Button("Preset 5");
            btnpreset5.setStyle(pcolorOff);
            btnpreset5.setOnAction(event -> {
                offAllPresetButtons();
                buttonPresetAction(4);
                labelstatusOrg.setText(" Status: Applying Preset 5");
                if (!ppressed5) {
                    btnpreset5.setStyle(pcolorOn);
                } else {
                    btnpreset5.setStyle(pcolorOff);
                }
                ppressed5 = !ppressed5;
            });
            btnpreset5.setPrefSize(xbtnpreset, ybtnpreset);

            ppressed6 = false;
            btnpreset6 = new Button("Preset 6");
            btnpreset6.setStyle(pcolorOff);
            btnpreset6.setOnAction(event -> {
                offAllPresetButtons();
                buttonPresetAction(5);
                labelstatusOrg.setText(" Status: Applying Preset 6");
                if (!ppressed6) {
                    btnpreset6.setStyle(pcolorOn);
                } else {
                    btnpreset6.setStyle(pcolorOff);
                }
                ppressed6 = !ppressed6;
            });
            btnpreset6.setPrefSize(xbtnpreset, ybtnpreset);

            ppressed7 = false;
            btnpreset7 = new Button("Preset 7");
            btnpreset7.setStyle(pcolorOff);
            btnpreset7.setOnAction(event -> {
                offAllPresetButtons();
                buttonPresetAction(6);
                labelstatusOrg.setText(" Status: Applying Preset 7");
                if (!ppressed7) {
                    btnpreset7.setStyle(pcolorOn);
                } else {
                    btnpreset7.setStyle(pcolorOff);
                }
                ppressed7 = !ppressed7;
            });
            btnpreset7.setPrefSize(xbtnpreset, ybtnpreset);

            ppressed8 = false;
            btnpreset8 = new Button("Preset 8");
            btnpreset8.setStyle(pcolorOff);
            btnpreset8.setOnAction(event -> {
                offAllPresetButtons();
                buttonPresetAction(7);
                labelstatusOrg.setText(" Status: Applying Preset 8");
                if (!ppressed8) {
                    btnpreset8.setStyle(pcolorOn);
                } else {
                    btnpreset8.setStyle(pcolorOff);
                }
                ppressed8 = !ppressed8;
            });
            btnpreset8.setPrefSize(xbtnpreset, ybtnpreset);

            GridPane presetGrid = new GridPane();
            presetGrid.setHgap(10);
            presetGrid.setVgap(10);
            presetGrid.add(btnpreset1 , 0, 0, 1, 1);
            presetGrid.add(btnpreset2 , 1, 0, 1, 1);
            presetGrid.add(btnpreset3 , 2, 0, 1, 1);
            presetGrid.add(btnpreset4 , 3, 0, 1, 1);
            presetGrid.add(btnpreset5 , 4, 0, 1, 1);
            presetGrid.add(btnpreset6 , 5, 0, 1, 1);
            presetGrid.add(btnpreset7 , 6, 0, 1, 1);
            presetGrid.add(btnpreset8 , 7, 0, 1, 1);

            // Assemble Keyboard Voice Panels

            GridPane gridmidcenterPerform = new GridPane();
            gridmidcenterPerform.setHgap(15);
            gridmidcenterPerform.setVgap(10);

            Button b1layerbtn = new Button("Bass [11]");
            b1layerbtn.setStyle(lrpressedOn);
            b1layerbtn.setMaxWidth(xvoicebtn);

            bleft1 = new Button(" Bass 1");
            bleft1.setId("B1-1");
            bleft1.setMaxSize(xvoicebtn, yvoicebtn);
            bleft1.setMinSize(xvoicebtn, yvoicebtn);
            bleft1.setStyle(bcolorOff);
            bleft1.setWrapText(true);
            bleft1.setWrapText(true);
            bleft1.setTextAlignment(TextAlignment.CENTER);
            bleft1.setOnAction(event -> {
                offAllBassButtons();
                int buttonidx = midiButtons.lookupButtonIdx(bleft1.getId());

                lastVoiceButton = bleft1.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(bleft1.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    bleft1.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!bpressed1) {
                    // Apply MIDI Program Change on Bass Channel for Button Press
                    int CHAN = sharedStatus.getBassCHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(bleft1.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    bleft1.setStyle(bcolorOn);
                }
                else {
                    bleft1.setStyle(bcolorOff);
                }
                bpressed1 = !bpressed1;

                labelstatusOrg.setText(" Status: Applied B1");
            });

            bleft2.setText(" Bass 2");
            bleft2.setMaxSize(xvoicebtn, yvoicebtn);
            bleft2.setMinSize(xvoicebtn, yvoicebtn);
            bleft2.setId("B1-2");
            bleft2.setStyle(bcolorOff);
            bleft2.setWrapText(true);
            bleft2.setTextAlignment(TextAlignment.CENTER);
            bleft2.setOnAction(event -> {
                offAllBassButtons();
                int buttonidx = midiButtons.lookupButtonIdx(bleft2.getId());

                lastVoiceButton = bleft2.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(bleft2.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    bleft2.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!bpressed2) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getBassCHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(bleft2.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    bleft2.setStyle(bcolorOn);
                }
                else {
                    bleft2.setStyle(bcolorOff);
                }
                bpressed2 = !bpressed2;

                labelstatusOrg.setText(" Status: Applied B2");
            });

            bleft3.setText(" Bass 3");
            bleft3.setMaxSize(xvoicebtn, yvoicebtn);
            bleft3.setMinSize(xvoicebtn, yvoicebtn);
            bleft3.setId("B1-3");
            bleft3.setStyle(bcolorOff);
            bleft3.setWrapText(true);
            bleft3.setTextAlignment(TextAlignment.CENTER);
            bleft3.setOnAction(event -> {
                offAllBassButtons();
                int buttonidx = midiButtons.lookupButtonIdx(bleft3.getId());

                lastVoiceButton = bleft3.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(bleft3.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    bleft3.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!bpressed3) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getBassCHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(bleft3.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    bleft3.setStyle(bcolorOn);
                }
                else {
                    bleft3.setStyle(bcolorOff);
                }
                bpressed3 = !bpressed3;

                labelstatusOrg.setText(" Status: Applied B3");
            });
            
            bleft4.setText(" Bass 4");
            bleft4.setId("B1-4");
            bleft4.setMaxSize(xvoicebtn, yvoicebtn);
            bleft4.setMinSize(xvoicebtn, yvoicebtn);
            bleft4.setStyle(bcolorOff);
            bleft4.setWrapText(true);
            bleft4.setTextAlignment(TextAlignment.CENTER);
            bleft4.setOnAction(event -> {
                offAllBassButtons();
                int buttonidx = midiButtons.lookupButtonIdx(bleft4.getId());

                lastVoiceButton = bleft4.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(bleft4.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    bleft4.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!bpressed4) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getBassCHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(bleft4.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    bleft4.setStyle(bcolorOn);
                }
                else {
                    bleft4.setStyle(bcolorOff);
                }
                bpressed4 = !bpressed4;

                labelstatusOrg.setText(" Status: Applied B4");
            });

            gridmidcenterPerform.add(b1layerbtn, 0, 0, 1, 1);
            gridmidcenterPerform.add(bleft1, 0, 1, 1, 1);
            gridmidcenterPerform.add(bleft2, 1, 1, 1, 1);
            gridmidcenterPerform.add(bleft3, 0, 2, 1, 1);
            gridmidcenterPerform.add(bleft4, 1, 2, 1, 1);

            Button d1layerbtn = new Button("Drums [10]");
            d1layerbtn.setStyle(styletext);
            d1layerbtn.setStyle(lrpressedOn);
            d1layerbtn.setMaxWidth(xvoicebtn);
            d1layerbtn.setMaxHeight(yvoicebtn / 2);

            // Do Beat Counter in large font
            DropShadow ds1 = new DropShadow();
            ds1.setOffsetY(3.0f);
            ds1.setColor(Color.color(0.4f, 0.4f, 0.4f));

            Text lblbeatcount = new Text();
            lblbeatcount.setEffect(ds1);
            lblbeatcount.setCache(true);
            lblbeatcount.setX(10.0f);
            lblbeatcount.setY(270.0f);
            lblbeatcount.setFill(Color.RED);
            lblbeatcount.setText("  Bar: 0.0");
            lblbeatcount.setFont(Font.font(null, FontWeight.BOLD, 20));

            dleft1.setText(" Drums 1");
            dleft1.setId("D1-1");
            dleft1.setMaxSize(xvoicebtn, yvoicebtn);
            dleft1.setMinSize(xvoicebtn, yvoicebtn);
            dleft1.setStyle(dcolorOff);
            dleft1.setWrapText(true);
            dleft1.setTextAlignment(TextAlignment.CENTER);
            dleft1.setOnAction(event -> {
                offAllDrumButtons();
                int buttonidx = midiButtons.lookupButtonIdx(dleft1.getId());

                lastVoiceButton = dleft1.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(dleft1.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    dleft1.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!dpressed1) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getDrumCHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(dleft1.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    dleft1.setStyle(dcolorOn);
                }
                else {
                    dleft1.setStyle(dcolorOff);
                }
                dpressed1 = !dpressed1;

                labelstatusOrg.setText(" Status: Applied D1");
            });

            dleft2.setText(" Drums 2");
            dleft2.setId("D1-2");
            dleft2.setMaxSize(xvoicebtn, yvoicebtn);
            dleft2.setMinSize(xvoicebtn, yvoicebtn);
            dleft2.setStyle(dcolorOff);
            dleft2.setWrapText(true);
            dleft2.setTextAlignment(TextAlignment.CENTER);
            dleft2.setOnAction(event -> {
                offAllDrumButtons();
                int buttonidx = midiButtons.lookupButtonIdx(dleft2.getId());

                lastVoiceButton = dleft2.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(dleft2.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    dleft2.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!dpressed2) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getDrumCHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(dleft2.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    dleft2.setStyle(dcolorOn);
                }
                else {
                    dleft2.setStyle(dcolorOff);
                }
                dpressed2 = !dpressed2;

                labelstatusOrg.setText(" Status: Applied D2");
            });

            dleft3.setText(" Drums 3");
            dleft3.setId("D1-3");
            dleft3.setMaxSize(xvoicebtn, yvoicebtn);
            dleft3.setMinSize(xvoicebtn, yvoicebtn);
            dleft3.setStyle(dcolorOff);
            dleft3.setWrapText(true);
            dleft3.setTextAlignment(TextAlignment.CENTER);
            dleft3.setOnAction(event -> {
                offAllDrumButtons();
                int buttonidx = midiButtons.lookupButtonIdx(dleft3.getId());

                lastVoiceButton = dleft3.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(dleft3.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    dleft3.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!dpressed3) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getDrumCHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(dleft3.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    dleft3.setStyle(dcolorOn);
                }
                else {
                    dleft3.setStyle(dcolorOff);
                }
                dpressed3 = !dpressed3;

                labelstatusOrg.setText(" Status: Applied D3");
            });

            dleft4.setText(" Drums 4");
            dleft4.setId("D1-4");
            dleft4.setMaxSize(xvoicebtn, yvoicebtn);
            dleft4.setMinSize(xvoicebtn, yvoicebtn);
            dleft4.setStyle(dcolorOff);
            dleft4.setWrapText(true);
            dleft4.setTextAlignment(TextAlignment.CENTER);
            dleft4.setOnAction(event -> {
                offAllDrumButtons();

                lastVoiceButton = dleft4.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(dleft4.getId());

                int buttonidx = midiButtons.lookupButtonIdx(dleft4.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    dleft4.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!dpressed4) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getDrumCHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(dleft4.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    dleft4.setStyle(dcolorOn);
                }
                else {
                    dleft4.setStyle(dcolorOff);
                }
                dpressed4 = !dpressed4;

                labelstatusOrg.setText(" Status: Applied D4");
            });

            gridmidcenterPerform.add(d1layerbtn, 0, 3, 1, 1);
            gridmidcenterPerform.add(lblbeatcount, 1, 3, 1, 1);
            gridmidcenterPerform.add(dleft1, 0, 4, 1, 1);
            gridmidcenterPerform.add(dleft2, 1, 4, 1, 1);
            gridmidcenterPerform.add(dleft3, 0, 5, 1, 1);
            gridmidcenterPerform.add(dleft4, 1, 5, 1, 1);

            // Lower Buttons

            l1layerbtn = new Button("Lower 1 [12]   ");       // Lefthand Layering Buttons
            l1layerbtn.setStyle(lrpressedOn);
            l1layerbtn.setMaxWidth(xvoicebtn);
            //l1layerbtn.setDisable(!arduinoUtils.hasARMPort());
            l1layerbtn.setOnAction(event -> {
                if (l1pressed == false) {
                    MidiDevices mididevices = MidiDevices.getInstance();
                    mididevices.layerChannel(12, true);

                    l1layerbtn.setStyle(lrpressedOn);
                    l1pressed = true;

                    labelstatusOrg.setText(" Status: Layer Lower 1 On");
                }
                else {
                    MidiDevices mididevices = MidiDevices.getInstance();
                    mididevices.layerChannel(12, false);

                    l1layerbtn.setStyle(lrpressedOff);
                    l1pressed = false;

                    labelstatusOrg.setText(" Status: Layer Lower 1 Off");
                }
                ////arduinoUtils.lefthandLayerSysexData(l1pressed, l2pressed);
            });

            l2layerbtn = new Button("Lower 2 [13]   ");
            l2layerbtn.setStyle(lrpressedOff);
            l2layerbtn.setMaxWidth(xvoicebtn);
            l2layerbtn.setDisable(!sharedStatus.getlower1Kbdlayerenabled());
            //l2layerbtn.setDisable(!arduinoUtils.hasARMPort());
            l2layerbtn.setOnAction(event -> {
                if (l2pressed == false) {
                    MidiDevices mididevices = MidiDevices.getInstance();
                    mididevices.layerChannel(13, true);

                    l2layerbtn.setStyle(lrpressedOn);
                    l2pressed = true;

                    labelstatusOrg.setText(" Status: Layer Lower 2 On");
                }
                else {
                    MidiDevices mididevices = MidiDevices.getInstance();
                    mididevices.layerChannel(13, false);

                    l2layerbtn.setStyle(lrpressedOff);
                    l2pressed = false;

                    labelstatusOrg.setText(" Status: Layer Lower 2 Off");
                }
                ////arduinoUtils.lefthandLayerSysexData(l1pressed, l2pressed);
            });

            lbutton11.setText(" Lower 1-1");
            lbutton11.setId("L1-1");
            lbutton11.setMaxSize(xvoicebtn, yvoicebtn);
            lbutton11.setMinSize(xvoicebtn, yvoicebtn);
            lbutton11.setStyle(lcolorOff);
            lbutton11.setWrapText(true);
            lbutton11.setTextAlignment(TextAlignment.CENTER);
            lbutton11.setOnAction(event -> {
                offAllLower1Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(lbutton11.getId());

                lastVoiceButton = lbutton11.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(lbutton11.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    lbutton11.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!lpressed11) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getLower1CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton11.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton11.setStyle(lcolorOn);
                }
                else {
                    lbutton11.setStyle(lcolorOff);
                }
                lpressed11 = !lpressed11;

                labelstatusOrg.setText(" Status: Applied L1-1");
            });

            lbutton12.setText(" Lower 1-2");
            lbutton12.setId("L1-2");
            lbutton12.setMaxSize(xvoicebtn, yvoicebtn);
            lbutton12.setMinSize(xvoicebtn, yvoicebtn);
            lbutton12.setStyle(lcolorOff);
            lbutton12.setWrapText(true);
            lbutton12.setTextAlignment(TextAlignment.CENTER);
            lbutton12.setOnAction(event -> {
                offAllLower1Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(lbutton12.getId());

                lastVoiceButton = lbutton12.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(lbutton12.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    lbutton12.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!lpressed12) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getLower1CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton12.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton12.setStyle(lcolorOn);
                }
                else {
                    lbutton12.setStyle(lcolorOff);
                }
                lpressed12 = !lpressed12;

                labelstatusOrg.setText(" Status: Applied L1-2");
            });

            lbutton13.setText(" Lower 1-3");
            lbutton13.setId("L1-3");
            lbutton13.setMaxSize(xvoicebtn, yvoicebtn);
            lbutton13.setMinSize(xvoicebtn, yvoicebtn);
            lbutton13.setStyle(lcolorOff);
            lbutton13.setWrapText(true);
            lbutton13.setTextAlignment(TextAlignment.CENTER);
            lbutton13.setOnAction(event -> {
                offAllLower1Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(lbutton13.getId());

                lastVoiceButton = lbutton13.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(lbutton13.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    lbutton13.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!lpressed13) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getLower1CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton13.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton13.setStyle(lcolorOn);
                }
                else {
                    lbutton13.setStyle(lcolorOff);
                }
                lpressed13 = !lpressed13;

                labelstatusOrg.setText(" Status: Applied L1-3");
            });

            lbutton14.setText(" Lower 1-4");
            lbutton14.setId("L1-4");
            lbutton14.setMaxSize(xvoicebtn, yvoicebtn);
            lbutton14.setMinSize(xvoicebtn, yvoicebtn);
            lbutton14.setStyle(lcolorOff);
            lbutton14.setWrapText(true);
            lbutton14.setTextAlignment(TextAlignment.CENTER);
            lbutton14.setOnAction(event -> {
                offAllLower1Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(lbutton14.getId());

                lastVoiceButton = lbutton14.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(lbutton14.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    lbutton14.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!lpressed14) {
                    // Apply MIDI Program Change on Lower Channel for Button Press
                    int CHAN = sharedStatus.getLower1CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton14.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton14.setStyle(lcolorOn);
                }
                else {
                    lbutton14.setStyle(lcolorOff);
                }
                lpressed14 = !lpressed14;

                labelstatusOrg.setText(" Status: Applied L1-4");
            });

            //playmidifile.sendRotaryOn(false);
            lbutton15.setText("Lower 1 Rotary Off");
            lbutton15.setId("L1-5");
            lbutton15.setMaxSize(xvoicebtn, yvoicebtn);
            lbutton15.setMinSize(xvoicebtn, yvoicebtn);
            lbutton15.setStyle(orgcolorOff);
            lbutton15.setWrapText(true);
            lbutton15.setTextAlignment(TextAlignment.CENTER);
            lbutton15.setOnAction(event -> {

                labelstatusOrg.setText(" Status: Lower 1 Rotary On/Off");
                if (!lpressed15) {
                    lbutton15.setStyle(orgcolorOn);
                    lbutton15.setText("Lower 1 Rotary On");

                    int channel = sharedStatus.getLower1CHAN();
                    playmidifile.sendLowerRotaryOn(channel, true);

                    labelstatusOrg.setText(" Status: Lower 1 Rotary On");
                } else {
                    lbutton15.setStyle(orgcolorOff);
                    lbutton15.setText("Lower 1 Rotary Off");

                    int channel = sharedStatus.getLower1CHAN();
                    playmidifile.sendLowerRotaryOn(channel, false);

                    labelstatusOrg.setText(" Status: Lower 1 Rotary Off");
                }
                lpressed15 = !lpressed15;
            });

            //playmidifile.sendRotaryFast(false);
            lbutton16.setText(" Lower 1 Rotary Slow");
            lbutton16.setId("L1-6");
            lbutton16.setMaxSize(xvoicebtn, yvoicebtn);
            lbutton16.setMinSize(xvoicebtn, yvoicebtn);
            lbutton16.setStyle(orgcolorOff);
            lbutton16.setWrapText(true);
            lbutton16.setTextAlignment(TextAlignment.CENTER);
            lbutton16.setOnAction(event -> {

                labelstatusOrg.setText(" Status: Lower 1 Rotary On/Off");
                if (!lpressed16) {
                    lbutton16.setStyle(orgcolorOn);
                    lbutton16.setText(" Lower 1 Rotary Fast");

                    labelstatusOrg.setText(" Status: Lower 1 Rotary Fast");

                    lbutton16.setDisable(true);
                    int channel = sharedStatus.getLower1CHAN();
                    playmidifile.sendLowerRotaryFast(channel,true);
                    lbutton16.setDisable(false);
                }
                else {
                    lbutton16.setStyle(orgcolorOff);
                    lbutton16.setText(" Lower 1 Rotary Slow");

                    labelstatusOrg.setText(" Status: Lower 1 Rotary Slow");

                    lbutton16.setDisable(true);
                    int channel = sharedStatus.getLower1CHAN();
                    playmidifile.sendLowerRotaryFast(channel,false);
                    lbutton16.setDisable(false);
                }
                lpressed16 = !lpressed16;
            });

            lbutton21 = new Button(" Lower 2-1");
            lbutton21.setId("L2-1");
            lbutton21.setMaxSize(xvoicebtn, yvoicebtn);
            lbutton21.setMinSize(xvoicebtn, yvoicebtn);
            lbutton21.setStyle(lcolorOff);
            lbutton21.setWrapText(true);
            lbutton21.setTextAlignment(TextAlignment.CENTER);
            lbutton21.setOnAction(event -> {
                offAllLower2Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(lbutton21.getId());

                lastVoiceButton = lbutton21.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(lbutton21.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    lbutton21.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!lpressed21) {
                    // Apply MIDI Program Change on Lower Channel for Button Press
                    int CHAN = sharedStatus.getLower2CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton21.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton21.setStyle(lcolorOn);
                }
                else {
                    lbutton21.setStyle(lcolorOff);
                }
                lpressed21 = !lpressed21;

                labelstatusOrg.setText(" Status: Applied L2-1");
            });

            lbutton22 = new Button(" Lower 2-2");
            lbutton22.setId("L2-2");
            lbutton22.setMaxSize(xvoicebtn, yvoicebtn);
            lbutton22.setMinSize(xvoicebtn, yvoicebtn);
            lbutton22.setStyle(lcolorOff);
            lbutton22.setWrapText(true);
            lbutton22.setTextAlignment(TextAlignment.CENTER);
            lbutton22.setOnAction(event -> {
                offAllLower2Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(lbutton22.getId());

                lastVoiceButton = lbutton22.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(lbutton22.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    lbutton22.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!lpressed22) {
                    // Apply MIDI Program Change on Lower Channel for Button Press
                    int CHAN = sharedStatus.getLower2CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton22.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton22.setStyle(lcolorOn);
                }
                else {
                    lbutton22.setStyle(lcolorOff);
                }
                lpressed22 = !lpressed22;

                labelstatusOrg.setText(" Status: Applied L2-2");
            });

            lbutton23 = new Button(" Lower 2-3");
            lbutton23.setId("L2-3");
            lbutton23.setMaxSize(xvoicebtn, yvoicebtn);
            lbutton23.setMinSize(xvoicebtn, yvoicebtn);
            lbutton23.setStyle(lcolorOff);
            lbutton23.setWrapText(true);
            lbutton23.setTextAlignment(TextAlignment.CENTER);
            lbutton23.setOnAction(event -> {
                offAllLower2Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(lbutton23.getId());

                lastVoiceButton = lbutton23.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(lbutton23.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    lbutton23.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!lpressed23) {
                    // Apply MIDI Program Change on Lower Channel for Button Press
                    int CHAN = sharedStatus.getLower2CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton23.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton23.setStyle(lcolorOn);
                }
                else {
                    lbutton23.setStyle(lcolorOff);
                }
                lpressed23 = !lpressed23;

                labelstatusOrg.setText(" Status: Applied L2-3");
            });

            lbutton24 = new Button(" Lower 2-4");
            lbutton24.setId("L2-4");
            lbutton24.setMaxSize(xvoicebtn, yvoicebtn);
            lbutton24.setMinSize(xvoicebtn, yvoicebtn);
            lbutton24.setStyle(lcolorOff);
            lbutton24.setWrapText(true);
            lbutton24.setTextAlignment(TextAlignment.CENTER);
            lbutton24.setOnAction(event -> {
                offAllLower2Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(lbutton24.getId());

                lastVoiceButton = lbutton24.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(lbutton24.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    lbutton24.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;
                }

                if (!lpressed24) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getLower2CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton24.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton24.setStyle(lcolorOn);
                }
                else {
                    lbutton24.setStyle(lcolorOff);
                }
                lpressed24 = !lpressed24;

                labelstatusOrg.setText(" Status: Applied L2-4");
            });

            gridmidcenterPerform.add(l1layerbtn, 3, 0, 1, 1);
            gridmidcenterPerform.add(l2layerbtn, 4, 0, 1, 1);

            gridmidcenterPerform.add(lbutton11, 3, 1, 1, 1);
            gridmidcenterPerform.add(lbutton12, 3, 2, 1, 1);
            gridmidcenterPerform.add(lbutton13, 3, 3, 1, 1);
            gridmidcenterPerform.add(lbutton14, 3, 4, 1, 1);
            gridmidcenterPerform.add(lbutton21, 4, 1, 1, 1);
            gridmidcenterPerform.add(lbutton22, 4, 2, 1, 1);
            gridmidcenterPerform.add(lbutton23, 4, 3, 1, 1);
            gridmidcenterPerform.add(lbutton24, 4, 4, 1, 1);

            // Upper Buttons

            r1layerbtn = new Button("Upper 1 [14]   ");       // Righthand Layering Buttons
            r1layerbtn.setStyle(lrpressedOn);
            r1layerbtn.setMaxWidth(xvoicebtn);
            //r1layerbtn.setDisable(!arduinoUtils.hasARMPort());
            r1layerbtn.setOnAction(event -> {
                if (r1pressed == false) {
                    MidiDevices mididevices = MidiDevices.getInstance();
                    mididevices.layerChannel(14, true);

                    r1layerbtn.setStyle(lrpressedOn);
                    r1pressed = true;

                    labelstatusOrg.setText(" Status: Layer Upper 1 On");
                }
                else {
                    MidiDevices mididevices = MidiDevices.getInstance();
                    mididevices.layerChannel(14, false);

                    r1layerbtn.setStyle(lrpressedOff);
                    r1pressed = false;

                    labelstatusOrg.setText(" Status: Layer Upper 1 Off");
                }
                ////arduinoUtils.righthandLayerSysexData(r1pressed, r2pressed, r3pressed);
            });

            r2layerbtn = new Button("Upper 2 [15]   ");
            r2layerbtn.setStyle(lrpressedOff);
            r2layerbtn.setMaxWidth(xvoicebtn);
            r2layerbtn.setDisable(!sharedStatus.getUpper1KbdLayerEnabled());
            //r2layerbtn.setDisable(!arduinoUtils.hasARMPort());
            r2layerbtn.setOnAction(event -> {
                if (r2pressed == false) {
                    MidiDevices mididevices = MidiDevices.getInstance();
                    mididevices.layerChannel(15, true);

                    r2layerbtn.setStyle(lrpressedOn);
                    r2pressed = true;

                    labelstatusOrg.setText(" Status: Layer Upper 2 On");
                }
                else {
                    MidiDevices mididevices = MidiDevices.getInstance();
                    mididevices.layerChannel(15, false);

                    r2layerbtn.setStyle(lrpressedOff);
                    r2pressed = false;

                    labelstatusOrg.setText(" Status: Layer Upper 2 Off");
                }
                ////arduinoUtils.righthandLayerSysexData(r1pressed, r2pressed, r3pressed);
            });

            r3layerbtn = new Button("Upper 3 [16]   ");
            r3layerbtn.setStyle(lrpressedOff);
            r3layerbtn.setMaxWidth(xvoicebtn);
            r3layerbtn.setDisable(!sharedStatus.getupper2Kbdlayerenabled());
            //r3layerbtn.setDisable(!arduinoUtils.hasARMPort());
            r3layerbtn.setOnAction(event -> {
                if (r3pressed == false) {
                    MidiDevices mididevices = MidiDevices.getInstance();
                    mididevices.layerChannel(16, true);

                    r3layerbtn.setStyle(lrpressedOn);
                    r3pressed = true;

                    labelstatusOrg.setText(" Status: Layer Upper 3 On");
                }
                else {
                    MidiDevices mididevices = MidiDevices.getInstance();
                    mididevices.layerChannel(16, false);

                    r3layerbtn.setStyle(lrpressedOff);
                    r3pressed = false;

                    labelstatusOrg.setText(" Status: Layer Upper 3 Off");
                }
                ////arduinoUtils.righthandLayerSysexData(r1pressed, r2pressed, r3pressed);

            });

            rbutton11.setText(" Upper 1-1");
            rbutton11.setId("U1-1");
            rbutton11.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton11.setMinSize(xvoicebtn, yvoicebtn);
            rbutton11.setStyle(rcolorOff);
            rbutton11.setWrapText(true);
            rbutton11.setTextAlignment(TextAlignment.CENTER);
            rbutton11.setOnAction(event -> {
                offAllUpper1Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton11.getId());

                lastVoiceButton = rbutton11.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton11.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton11.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed11) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper1CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton11.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton11.setStyle(rcolorOn);
                }
                else {
                    rbutton11.setStyle(rcolorOff);
                }
                rpressed11 = !rpressed11;

                labelstatusOrg.setText(" Status: Applied U1-1");
            });

            rbutton12.setText(" Upper 1-2");
            rbutton12.setId("U1-2");
            rbutton12.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton12.setMinSize(xvoicebtn, yvoicebtn);
            rbutton12.setStyle(rcolorOff);
            rbutton12.setWrapText(true);
            rbutton12.setTextAlignment(TextAlignment.CENTER);
            rbutton12.setOnAction(event -> {
                offAllUpper1Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton12.getId());

                lastVoiceButton = rbutton12.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton12.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton12.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed12) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper1CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton12.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton12.setStyle(rcolorOn);
                }
                else {
                    rbutton12.setStyle(rcolorOff);
                }
                rpressed12 = !rpressed12;

                labelstatusOrg.setText(" Status: Applied U1-2");
            });

            rbutton13.setText(" Upper 1-3");
            rbutton13.setId("U1-3");
            rbutton13.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton13.setMinSize(xvoicebtn, yvoicebtn);
            rbutton13.setStyle(rcolorOff);
            rbutton13.setWrapText(true);
            rbutton13.setTextAlignment(TextAlignment.CENTER);
            rbutton13.setOnAction(event -> {
                offAllUpper1Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton13.getId());

                lastVoiceButton = rbutton13.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton13.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton13.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed13) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper1CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton13.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton13.setStyle(rcolorOn);
                }
                else {
                    rbutton13.setStyle(rcolorOff);
                }
                rpressed13 = !rpressed13;

                labelstatusOrg.setText(" Status: Applied U1-3");
            });

            rbutton14.setText(" Upper 1-4");
            rbutton14.setId("U1-4");
            rbutton14.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton14.setMinSize(xvoicebtn, yvoicebtn);
            rbutton14.setStyle(rcolorOff);
            rbutton14.setWrapText(true);
            rbutton14.setTextAlignment(TextAlignment.CENTER);
            rbutton14.setOnAction(event -> {
                offAllUpper1Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton14.getId());

                lastVoiceButton = rbutton14.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton14.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton14.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed14) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper1CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton14.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton14.setStyle(rcolorOn);
                }
                else {
                    rbutton14.setStyle(rcolorOff);
                }
                rpressed14 = !rpressed14;

                lastVoiceButton = rbutton14.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton14.getId());

                labelstatusOrg.setText(" Status: Applied U1-4");
            });

            rbutton15.setText(" Upper 1-5");
            rbutton15.setId("U1-5");
            rbutton15.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton15.setMinSize(xvoicebtn, yvoicebtn);
            rbutton15.setStyle(rcolorOff);
            rbutton15.setWrapText(true);
            rbutton15.setTextAlignment(TextAlignment.CENTER);
            rbutton15.setOnAction(event -> {
                offAllUpper1Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton15.getId());

                lastVoiceButton = rbutton15.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton15.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton15.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed15) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper1CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton15.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton15.setStyle(rcolorOn);
                }
                else {
                    rbutton15.setStyle(rcolorOff);
                }
                rpressed15 = !rpressed15;

                labelstatusOrg.setText(" Status: Applied U1-5");
            });

            // Organ Button + Rotary On/Off and Brake
            rbutton16.setText(" Upper 1-6");
            rbutton16.setId("U1-6");
            rbutton16.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton16.setMinSize(xvoicebtn, yvoicebtn);
            rbutton16.setStyle(orgcolorOff);
            rbutton16.setWrapText(true);
            rbutton16.setTextAlignment(TextAlignment.CENTER);
            rbutton16.setOnAction(event -> {
                offAllUpper1Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton16.getId());

                lastVoiceButton = rbutton16.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton16.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton16.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed16) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper1CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton16.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton16.setStyle(orgcolorOn);
                }
                else {
                    rbutton16.setStyle(orgcolorOff);
                }
                rpressed16 = !rpressed16;

                labelstatusOrg.setText(" Status: Applied U1-6");
            });

            //playmidifile.sendRotaryOn(false);
            rbutton17.setText("Upper 1 Rotary Off");
            rbutton17.setId("U1-7");
            rbutton17.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton17.setMinSize(xvoicebtn, yvoicebtn);
            rbutton17.setStyle(orgcolorOff);
            rbutton17.setWrapText(true);
            rbutton17.setTextAlignment(TextAlignment.CENTER);
            rbutton17.setOnAction(event -> {

                labelstatusOrg.setText(" Status: Upper 1 Rotary On/Off");
                if (!rpressed17) {
                    rbutton17.setStyle(orgcolorOn);
                    rbutton17.setText("Upper 1 Rotary On");

                    int channel = sharedStatus.getUpper1CHAN();
                    playmidifile.sendUpperRotaryOn(channel, true);

                    labelstatusOrg.setText(" Status: Upper 1 Rotary On");
                } else {
                    rbutton17.setStyle(orgcolorOff);
                    rbutton17.setText("Upper 1 Rotary Off");

                    int channel = sharedStatus.getUpper1CHAN();
                    playmidifile.sendUpperRotaryOn(channel, false);

                    labelstatusOrg.setText(" Status: Upper 1 Rotary Off");
                }
                rpressed17 = !rpressed17;
            });

            //playmidifile.sendRotaryFast(false);
            rbutton18.setText(" Upper 1 Rotary Slow");
            rbutton18.setId("U1-8");
            rbutton18.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton18.setMinSize(xvoicebtn, yvoicebtn);
            rbutton18.setStyle(orgcolorOff);
            rbutton18.setWrapText(true);
            rbutton18.setTextAlignment(TextAlignment.CENTER);
            rbutton18.setOnAction(event -> {

                labelstatusOrg.setText(" Status: Upper 1 Rotary On/Off");
                if (!rpressed18) {
                    rbutton18.setStyle(orgcolorOn);
                    rbutton18.setText(" Upper 1 Rotary Fast");

                    labelstatusOrg.setText(" Status: Upper 1 Rotary Fast");

                    rbutton18.setDisable(true);
                    int channel = sharedStatus.getUpper1CHAN();
                    playmidifile.sendUpperRotaryFast(channel,true);
                    rbutton18.setDisable(false);
                }
                else {
                    rbutton18.setStyle(orgcolorOff);
                    rbutton18.setText(" Upper 1 Rotary Slow");

                    labelstatusOrg.setText(" Status: Upper 1 Rotary Slow");

                    rbutton18.setDisable(true);
                    int channel = sharedStatus.getUpper1CHAN();
                    playmidifile.sendUpperRotaryFast(channel,false);
                    rbutton18.setDisable(false);
                }
                rpressed18 = !rpressed18;
            });

            // Upper 2 CHAN 15 + 16 Buttons
            rbutton21.setText(" Upper 2-1");
            rbutton21.setId("U2-1");
            rbutton21.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton21.setMinSize(xvoicebtn, yvoicebtn);
            rbutton21.setStyle(rcolorOff);
            rbutton21.setWrapText(true);
            rbutton21.setTextAlignment(TextAlignment.CENTER);
            rbutton21.setOnAction(event -> {
                offAllUpper2Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton21.getId());

                lastVoiceButton = rbutton21.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton21.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton21.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed21) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper2CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton21.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton21.setStyle(rcolorOn);
                }
                else {
                    rbutton21.setStyle(rcolorOff);
                }
                rpressed21 = !rpressed21;

                labelstatusOrg.setText(" Status: Applied U2-1");
            });

            rbutton22.setText(" Upper 2-2");
            rbutton22.setId("U2-2");
            rbutton22.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton22.setMinSize(xvoicebtn, yvoicebtn);
            rbutton22.setStyle(rcolorOff);
            rbutton22.setWrapText(true);
            rbutton22.setTextAlignment(TextAlignment.CENTER);
            rbutton22.setOnAction(event -> {
                offAllUpper2Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton22.getId());

                lastVoiceButton = rbutton22.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton22.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton22.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed22) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper2CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton22.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton22.setStyle(rcolorOn);
                }
                else {
                    rbutton22.setStyle(rcolorOff);
                }
                rpressed22 = !rpressed22;

                labelstatusOrg.setText(" Status: Applied U2-2");
            });

            rbutton23.setText(" Upper 2-3");
            rbutton23.setId("U2-3");
            rbutton23.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton23.setMinSize(xvoicebtn, yvoicebtn);
            rbutton23.setStyle(rcolorOff);
            rbutton23.setWrapText(true);
            rbutton23.setTextAlignment(TextAlignment.CENTER);
            rbutton23.setOnAction(event -> {
                offAllUpper2Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton23.getId());

                lastVoiceButton = rbutton23.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton23.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton23.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed23) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper2CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton23.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton23.setStyle(rcolorOn);
                }
                else {
                    rbutton23.setStyle(rcolorOff);
                }
                rpressed23 = !rpressed23;

                labelstatusOrg.setText(" Status: Applied U2-3");
            });

            rbutton24.setText(" Upper 2-4");
            rbutton24.setId("U2-4");
            rbutton24.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton24.setMinSize(xvoicebtn, yvoicebtn);
            rbutton24.setStyle(rcolorOff);
            rbutton24.setWrapText(true);
            rbutton24.setTextAlignment(TextAlignment.CENTER);
            rbutton24.setOnAction(event -> {
                offAllUpper2Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton24.getId());

                lastVoiceButton = rbutton24.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton24.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton24.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed24) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper2CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton24.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton24.setStyle(rcolorOn);
                }
                else {
                    rbutton24.setStyle(rcolorOff);
                }
                rpressed24 = !rpressed24;

                labelstatusOrg.setText(" Status: Applied U2-4");
            });

            rbutton31.setText(" Upper 3-1");
            rbutton31.setId("U3-1");
            rbutton31.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton31.setMinSize(xvoicebtn, yvoicebtn);
            rbutton31.setStyle(rcolorOff);
            rbutton31.setWrapText(true);
            rbutton31.setTextAlignment(TextAlignment.CENTER);
            rbutton31.setOnAction(event -> {
                offAllUpper3Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton31.getId());

                lastVoiceButton = rbutton31.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton31.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton31.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed31) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper3CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton31.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton31.setStyle(rcolorOn);
                }
                else {
                    rbutton31.setStyle(rcolorOff);
                }
                rpressed31 = !rpressed31;

                labelstatusOrg.setText(" Status: Applied U3-1");
            });

            rbutton32.setText(" Upper 3-2");
            rbutton32.setId("U3-2");
            rbutton32.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton32.setMinSize(xvoicebtn, yvoicebtn);
            rbutton32.setStyle(rcolorOff);
            rbutton32.setWrapText(true);
            rbutton32.setTextAlignment(TextAlignment.CENTER);
            rbutton32.setOnAction(event -> {
                offAllUpper3Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton32.getId());

                lastVoiceButton = rbutton32.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton32.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton32.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed32) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper3CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton32.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton32.setStyle(rcolorOn);
                }
                else {
                    rbutton32.setStyle(rcolorOff);
                }
                rpressed32 = !rpressed32;

                labelstatusOrg.setText(" Status: Applied U3-2");
            });

            rbutton33.setText(" Upper 3-3");
            rbutton33.setId("U3-3");
            rbutton33.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton33.setMinSize(xvoicebtn, yvoicebtn);
            rbutton33.setStyle(rcolorOff);
            rbutton33.setWrapText(true);
            rbutton33.setTextAlignment(TextAlignment.CENTER);
            rbutton33.setOnAction(event -> {
                offAllUpper3Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton33.getId());

                lastVoiceButton = rbutton33.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton33.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton33.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed33) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper3CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton33.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton33.setStyle(rcolorOn);
                }
                else {
                    rbutton33.setStyle(rcolorOff);
                }
                rpressed33 = !rpressed33;

                labelstatusOrg.setText(" Status: Applied U3-3");
            });

            rbutton34.setText(" Upper 3-4");
            rbutton34.setId("U3-4");
            rbutton34.setMaxSize(xvoicebtn, yvoicebtn);
            rbutton34.setMinSize(xvoicebtn, yvoicebtn);
            rbutton34.setStyle(rcolorOff);
            rbutton34.setWrapText(true);
            rbutton34.setTextAlignment(TextAlignment.CENTER);
            rbutton34.setOnAction(event -> {
                offAllUpper3Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(rbutton34.getId());

                lastVoiceButton = rbutton34.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton34.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    rbutton34.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

                    // Save the Patch Changes to the Perform file
                    midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
                    midiButtons.getMidiButton(buttonidx, 0).setPC((int)dopatches.getMIDIPatch(patchidx).getPC());
                    midiButtons.getMidiButton(buttonidx, 0).setLSB((int)dopatches.getMIDIPatch(patchidx).getLSB());
                    midiButtons.getMidiButton(buttonidx, 0).setMSB((int)dopatches.getMIDIPatch(patchidx).getMSB());

                    buttonSoundFont.setStyle(selectcolorOff);

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    bnewpatchselected = false;
                }

                if (!rpressed34) {
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getUpper3CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(rbutton34.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton34.setStyle(rcolorOn);
                }
                else {
                    rbutton34.setStyle(rcolorOff);
                }
                rpressed34 = !rpressed34;

                labelstatusOrg.setText(" Status: Applied U3-4");
            });

            gridmidcenterPerform.add(r1layerbtn, 6, 0, 1, 1);
            gridmidcenterPerform.add(r2layerbtn, 7, 0, 1, 1);
            gridmidcenterPerform.add(r3layerbtn, 8, 0, 1, 1);

            gridmidcenterPerform.add(rbutton11, 6, 1, 1, 1);
            gridmidcenterPerform.add(rbutton12, 6, 2, 1, 1);
            gridmidcenterPerform.add(rbutton13, 6, 3, 1, 1);
            gridmidcenterPerform.add(rbutton14, 6, 4, 1, 1);
            gridmidcenterPerform.add(rbutton15, 6, 5, 1, 1);
            gridmidcenterPerform.add(rbutton16, 6, 6, 1, 1);

            gridmidcenterPerform.add(rbutton21, 7, 1, 1, 1);
            gridmidcenterPerform.add(rbutton22, 7, 2, 1, 1);
            gridmidcenterPerform.add(rbutton23, 7, 3, 1, 1);
            gridmidcenterPerform.add(rbutton24, 7, 4, 1, 1);
            gridmidcenterPerform.add(lbutton15, 7, 5, 1, 1);
            gridmidcenterPerform.add(lbutton16, 7, 6, 1, 1);

            gridmidcenterPerform.add(rbutton31, 8, 1, 1, 1);
            gridmidcenterPerform.add(rbutton32, 8, 2, 1, 1);
            gridmidcenterPerform.add(rbutton33, 8, 3, 1, 1);
            gridmidcenterPerform.add(rbutton34, 8, 4, 1, 1);
            gridmidcenterPerform.add(rbutton17, 8, 5, 1, 1);
            gridmidcenterPerform.add(rbutton18, 8, 6, 1, 1);

            // Assemble MIDI Play Buttons

            btnplay = new Button("Play Song");
            btnplay.setDisable(true);
            btnplay.setStyle(btnplayOff);
            btnplay.setMaxSize(xvoicebtn, yvoicebtn);
            btnplay.setOnAction(e -> {
                try {
                    if (songFile == null) {
                        labelstatus.setText(" Status: Select MIDI Song to play!");
                        return;
                    }

                    if (!bplaying) {
                        btnplay.setText("Stop Play");
                        btnplay.setStyle(btnplayOn);

                        bplaying = true;

                        PlayMidi playmidifile = PlayMidi.getInstance();
                        if (!playmidifile.startMidiPlay(dosongs.getSong(idxSongList), dopresets, playmode)) {
                            labelstatusOrg.setText(" Status: " + sharedStatus.getStatusText());
                        }
                        else {
                            // Disable Songs menu switch while playing
                            buttonsc2.setDisable(true);

                            // Song Play Repeating Timer: Collects Beat Timer and Play Status every 250ms
                            Timer songPlayTimer = new Timer();
                            songPlayTimer.scheduleAtFixedRate(new TimerTask(){
                                @Override
                                public void run(){
                                    // Check if Play stopped and reset Button Status
                                    if (!playmidifile.isMidiRunning()) {

                                        Platform.runLater(() -> {

                                                //PlayMidi playmidifile = PlayMidi.getInstance();
                                                //playmidifile.stopMidiPlay(songFile);
                                                //lblbeatcount.setText("Bar: 0.0");
                                                bplaying = false;

                                                btnplay.setText("Play Song");
                                                btnplay.setStyle(btnplayOff);
                                                labelstatusOrg.setText(" Status: Song Play Ended");

                                            // Enable Songs menu switch once stopped playing
                                            buttonsc2.setDisable(false);

                                        });
                                        songPlayTimer.cancel();
                                        return;
                                    }

                                    Platform.runLater(() -> {
                                            //labelstatusOrg.setText(" Status: Bar " + playmidifile.getSequencerBeat()));
                                            lblbeatcount.setText("  Bar: " + playmidifile.getSequencerBeat());
                                    });

                                    //System.out.println("PerformScene: Sequencer Bar.Beat " + playmidifile.getSequencerTickPosition());
                                }
                            }, 0, 100);

                            labelstatusOrg.setText(" Status: Playing " + songTitle);
                        }
                    }
                    else {
                        btnplay.setText("Play Song");
                        btnplay.setStyle(btnplayOff);

                        PlayMidi playmidifile = PlayMidi.getInstance();
                        playmidifile.stopMidiPlay(songFile);

                        lblbeatcount.setText("  Bar: 0.0");

                        bplaying = false;

                        // Enable Songs menu switch once stopped playing
                        buttonsc2.setDisable(false);
                    }
                }
                catch (Exception exception) {
                    bplaying = false;

                    btnplay.setText("Play Song");
                    btnplay.setStyle(btnplayOff);

                    exception.printStackTrace();
                }
                //System.out.println("PerformScene: " + readpresets.presetString(presetIdx * 16 + channelIdx));
            });
            gridmidcenterPerform.add(btnplay, 0, 6, 1, 1);

            // Backing Mode Button
            // Mode 1 = Original, 2 = Play Along, 3 = Backing
            playmode = 3;
            btnbacking = new Button("Backing");
            btnbacking.setDisable(true);
            btnbacking.setStyle(btnplayOff);
            btnbacking.setMaxSize(xvoicebtn, yvoicebtn);
            btnbacking.setOnAction(e -> {
                if (playmode == 3) {
                    btnbacking.setText("Play Along");
                    btnbacking.setStyle(btnplayOn);
                    playmode = 2;

                    PlayMidi playmidifile = PlayMidi.getInstance();
                    if (playmidifile.isMidiRunning()) {
                        playmidifile.unmuteKeyboardChannels(dosongs.getSong(idxSongList));
                    }
                }
                else {
                    btnbacking.setText("Backing");
                    btnbacking.setStyle(btnplayOff);
                    playmode = 3;

                    PlayMidi playmidifile = PlayMidi.getInstance();
                    if (playmidifile.isMidiRunning()) {
                        playmidifile.muteKeyboardChannels(dosongs.getSong(idxSongList));
                    }
                }
            });
            gridmidcenterPerform.add(btnbacking, 1, 6, 1, 1);


            // Add VOL, REV and CHO Sliders. Show for the most recent selected Voice Button

            // Create VOL slider
            sliderVOL = new Slider(0, 127, 0);
            sliderVOL.setOrientation(Orientation.VERTICAL);
            sliderVOL.setShowTickLabels(true);
            sliderVOL.setShowTickMarks(true);
            sliderVOL.setMajorTickUnit(16);
            sliderVOL.setBlockIncrement(8);
            Rotate rotateVol = new Rotate();
            sliderVOL.valueProperty().addListener((observable, oldValue, newValue) -> {
                //Setting the angle for the rotation
                rotateVol.setAngle((double) newValue);

                if (lastVoiceButton != null) {
                    PlayMidi playmidifile = PlayMidi.getInstance();
                    playmidifile.sendMidiControlChange((byte) lastVoiceChannel, ccVOL, (byte) sliderVOL.getValue());

                    midiButtons.getButtonById(lastVoiceButton, 0).setVOL((int) sliderVOL.getValue());

                    buttonSave.setDisable(false);
                    flgDirtyPreset = true;      // Need to save updated Preset

                    labelstatusOrg.setText(" Status: Button " + lastVoiceButton + " VOL= " + newValue.intValue());
                }
            });
            sliderVOL.setValue(midiButtons.getButtonById(lastVoiceButton, 0).getVOL());

            // Create REV slider
            sliderREV = new Slider(0, 127, 0);
            sliderREV.setOrientation(Orientation.VERTICAL);
            sliderREV.setShowTickLabels(true);
            sliderREV.setShowTickMarks(true);
            sliderREV.setMajorTickUnit(16);
            sliderREV.setBlockIncrement(8);
            Rotate rotateRev = new Rotate();
            sliderREV.valueProperty().addListener((observable, oldValue, newValue) -> {
                //Setting the angle for the rotation
                rotateRev.setAngle((double) newValue);

                PlayMidi playmidifile = PlayMidi.getInstance();
                playmidifile.sendMidiControlChange((byte) lastVoiceChannel, ccREV, (byte) sliderREV.getValue());

                midiButtons.getButtonById(lastVoiceButton, 0).setREV((int)sliderREV.getValue());

                buttonSave.setDisable(false);
                flgDirtyPreset = true;      // Need to save updated Preset

                if (lastVoiceButton != null)
                    labelstatusOrg.setText(" Status: Button " + lastVoiceButton + " REV= " + newValue.intValue());
            });
            sliderREV.setValue(midiButtons.getButtonById(lastVoiceButton, 0).getREV());

            // Create CHO slider
            sliderCHO = new Slider(0, 127, 0);
            sliderCHO.setOrientation(Orientation.VERTICAL);
            sliderCHO.setShowTickLabels(true);
            sliderCHO.setShowTickMarks(true);
            sliderCHO.setMajorTickUnit(16);
            sliderCHO.setBlockIncrement(8);
            Rotate rotateCho = new Rotate();
            sliderCHO.valueProperty().addListener((observable, oldValue, newValue) -> {
                //Setting the angle for the rotation
                rotateCho.setAngle((double) newValue);

                PlayMidi playmidifile = PlayMidi.getInstance();
                playmidifile.sendMidiControlChange((byte) lastVoiceChannel, ccCHO, (byte) sliderCHO.getValue());

                midiButtons.getButtonById(lastVoiceButton, 0).setCHO((int)sliderCHO.getValue());

                buttonSave.setDisable(false);
                flgDirtyPreset = true;      // Need to save updated Preset

                if (lastVoiceButton != null)
                    labelstatusOrg.setText(" Status: Button " + lastVoiceButton + " CHO= " + newValue.intValue());
            });
            sliderCHO.setValue(midiButtons.getButtonById(lastVoiceButton, 0).getCHO());

            // Create MOD slider
            sliderMOD = new Slider(0, 127, 0);
            sliderMOD.setOrientation(Orientation.VERTICAL);
            sliderMOD.setShowTickLabels(true);
            sliderMOD.setShowTickMarks(true);
            sliderMOD.setMajorTickUnit(16);
            sliderMOD.setBlockIncrement(8);
            Rotate rotateMod = new Rotate();
            sliderMOD.valueProperty().addListener((observable, oldValue, newValue) -> {
                //Setting the angle for the rotation
                rotateMod.setAngle((double) newValue);

                PlayMidi playmidifile = PlayMidi.getInstance();
                playmidifile.sendMidiControlChange((byte) lastVoiceChannel, ccMOD, (byte) sliderMOD.getValue());

                midiButtons.getButtonById(lastVoiceButton, 0).setMOD((int)sliderMOD.getValue());

                buttonSave.setDisable(false);
                flgDirtyPreset = true;      // Need to save updated Preset

                if (lastVoiceButton != null)
                    labelstatusOrg.setText(" Status: Button " + lastVoiceButton + " MOD= " + newValue.intValue());
            });
            sliderMOD.setValue(midiButtons.getButtonById(lastVoiceButton, 0).getMOD());

/*
            // Create ROT slider
            sliderROT = new Slider(0, 7, 0);
            sliderROT.setOrientation(Orientation.VERTICAL);
            sliderROT.setShowTickLabels(true);
            sliderROT.setShowTickMarks(true);
            sliderROT.setMajorTickUnit(1);
            sliderROT.setBlockIncrement(1);
            Rotate rotateRot = new Rotate();
            sliderROT.valueProperty().addListener((observable, oldValue, newValue) -> {
                //Setting the angle for the rotation
                rotateRot.setAngle((double) newValue);

                // Rotary Slider CC74 value range = 0 - 7. Ignore duplicate values
                if (rotvalue == (int)sliderROT.getValue()) return;

                rotvalue = (int)sliderROT.getValue();

                PlayMidi playmidifile = PlayMidi.getInstance();
                playmidifile.sendMidiControlChange((byte) lastVoiceChannel, ccROT, (byte)rotvalue);

                midiButtons.getButtonById(lastVoiceButton, 0).setROT(rotvalue);

                buttonSave.setDisable(false);
                flgDirtyPreset = true;      // Need to save updated Preset

                if (lastVoiceButton != null)
                    labelstatusOrg.setText(" Status: Button " + lastVoiceButton + " ROT= " + (byte)rotvalue);
            });
            sliderROT.setValue(midiButtons.getButtonById(lastVoiceButton, 0).getROT());
*/
            // Create PAN slider
            sliderPAN = new Slider(0, 127, 0);
            sliderPAN.setOrientation(Orientation.VERTICAL);
            sliderPAN.setShowTickLabels(true);
            sliderPAN.setShowTickMarks(true);
            sliderPAN.setMajorTickUnit(16);
            sliderPAN.setBlockIncrement(8);
            Rotate rotatePan = new Rotate();
            sliderPAN.valueProperty().addListener((observable, oldValue, newValue) -> {
                //Setting the angle for the rotation
                rotatePan.setAngle((double) newValue);

                PlayMidi playmidifile = PlayMidi.getInstance();
                playmidifile.sendMidiControlChange((byte) lastVoiceChannel, ccPAN, (byte) sliderPAN.getValue());

                midiButtons.getButtonById(lastVoiceButton, 0).setPAN((int)sliderPAN.getValue());

                buttonSave.setDisable(false);
                flgDirtyPreset = true;      // Need to save updated Preset

                if (lastVoiceButton != null)
                    labelstatusOrg.setText(" Status: Button " + lastVoiceButton + " PAN= " + newValue.intValue());
            });
            sliderPAN.setValue(midiButtons.getButtonById(lastVoiceButton, 0).getPAN());

            Label vollabel = new Label("VOL");
            vollabel.setStyle(styletextwhitesmall);
            Label revlabel = new Label("REV");
            revlabel.setStyle(styletextwhitesmall);
            Label cholabel = new Label("CHO");
            cholabel.setStyle(styletextwhitesmall);
            Label modlabel = new Label("MOD");
            modlabel.setStyle(styletextwhitesmall);
            Label panlabel = new Label("PAN");
            panlabel.setStyle(styletextwhitesmall);

            GridPane gridEffects = new GridPane();
            gridEffects.add(new VBox(vollabel, sliderVOL), 0, 1, 1, 1);
            gridEffects.add(new VBox(revlabel, sliderREV), 1, 1, 1, 1);
            gridEffects.add(new VBox(cholabel, sliderCHO), 2, 1, 1, 1);
            gridEffects.add(new VBox(modlabel, sliderMOD), 3, 1, 1, 1);
            gridEffects.add(new VBox(panlabel, sliderPAN), 4, 1, 1, 1);
            //gridEffects.add(new VBox(new Label("ROT"), sliderROT), 5, 1, 1, 1);
            gridEffects.setHgap(10);
            gridmidcenterPerform.add(gridEffects, 3, 5, 3, 2);
            gridmidcenterPerform.setStyle(styletext);

            // Finalize Perform Center Panel

            BorderPane midcenterPerformPanel = new BorderPane();
            midcenterPerformPanel.setCenter(gridmidcenterPerform);
            midcenterPerformPanel.setPadding(new Insets(10, 0, 10, 0));

            // Final assembly the Perform Center Panel

            BorderPane centerPerformPanel = new BorderPane();
            centerPerformPanel.setTop(gridTopLine);
            centerPerformPanel.setCenter(midcenterPerformPanel);
            centerPerformPanel.setBottom(presetGrid);
            centerPerformPanel.setPadding(new Insets(10, 10, 10, 10));

            // Assemble Status Bar
            HBox hboxstatus = new HBox();
            hboxstatus.getChildren().add(labelstatusOrg);
            labelstatusOrg.setMinWidth(820 * ymul);

            //labelsynth = new Label("Module");
            labelsynth = new Label(sharedStatus.getModuleName(config.getSoundModuleIdx()));
            labelsynth.setTextAlignment(TextAlignment.JUSTIFY);
            labelsynth.setStyle(styletext);
            hboxstatus.getChildren().add(labelsynth);
            hboxstatus.setStyle(bgheadercolor);

            // Assemble the Scene BorderPane View

            borderPaneOrg.setTop(borderPaneTop);
            //borderPaneOrg.setLeft(leftseparator);
            borderPaneOrg.setCenter(centerPerformPanel);
            //borderPaneOrg.setRight(rightseparator);
            borderPaneOrg.setBottom(hboxstatus);

            // Populate Midi Button Text with Patch names as read from file.
            initMidiButtonPatches(midiButtons);

            // Disable Preset Buttons on startup
            disablePresetButtons(0, -1);

            // In MIDI GM Mode: Preset all Sounds and Wire up the Sliders to default to Upper 1-1
            if (moduleidx == 0) {
                d1layerbtn.setVisible(false);
                dleft1.setVisible(false);
                dleft2.setVisible(false);
                dleft3.setVisible(false);
                dleft4.setVisible(false);

                // Disable Organ Rotary Buttons in MIDI GM Mode
                lbutton15.setVisible(false);
                lbutton16.setVisible(false);
                rbutton17.setVisible(false);
                rbutton18.setVisible(false);
            }
            else
                dleft1.fire();
                bleft1.fire();
                lbutton21.fire();
                lbutton11.fire();
                rbutton31.fire();
                rbutton21.fire();
                rbutton11.fire();

            // Enable Save button only once a change has been made in UI
            buttonSave.setDisable(true);
            flgDirtyPreset = false;

            // Prepare background Image
            //try {
            //    FileInputStream input = new FileInputStream(sharedStatus.getCFGDirectory() + "backimage.png");
            //    Image image = new Image(input);
            //    BackgroundImage backgroundimage = new BackgroundImage(image,
            //            BackgroundRepeat.NO_REPEAT,
            //            BackgroundRepeat.NO_REPEAT,
            //            BackgroundPosition.DEFAULT,
            //            BackgroundSize.DEFAULT);

            //    // Create and set background
            //    Background background = new Background(backgroundimage);
            //    centerPerformPanel.setBackground(background);
            //}
            //catch(FileNotFoundException ex) {
            //    System.err.println("Background image not found! ");
            //}

        }
        catch (Exception ex) {
            System.err.println("### PerformScene Exception: Unable to read Stylesheets!");
            System.err.println(ex);
        }
    }

    void buttonPresetLoad(String presetFile) {

        dopresets.makeMidiPresets(presetFile);

        //System.out.println("OrganScene: New Song selected: Loaded new Preset file: " + presetFile);
    }

    void buttonPresetAction(int presetIdx) {

        try {

            // Reload Preset file if changed, e.g. in Preset Scene
            if (sharedStatus.getPresetReload() == true) {
                dopresets.makeMidiPresets(presetFile);
            }

            // Apply selected Preset Program and Control Changes to MIDI output
            // Get this done before we catch before we catch up with UI
            for (int chanidx = 0; chanidx < 16; chanidx++) {
                MidiPreset preset = dopresets.getPreset(presetIdx * 16 + chanidx);
                dopresets.applyMidiPreset(preset, chanidx);
            }

            // Apply selected Preset Program Changes to Organ Buttons
            // Set Voide Name on the Button, and copy Preset details & effects in Button's MidiButton object

            // Upper 1-1
            MidiPreset preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getUpper1CHAN() - 1);
            int buttonidx = midiButtons.lookupButtonIdx(rbutton11.getId());
            MidiButton midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            rbutton11.setText(preset.getPatchName());
            offAllUpper1Buttons();
            rbutton11.setStyle(rcolorOn);

            // Upper 2-1
            preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getUpper2CHAN() - 1);
            buttonidx = midiButtons.lookupButtonIdx(rbutton21.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            rbutton21.setText(preset.getPatchName());
            offAllUpper2Buttons();
            rbutton21.setStyle(rcolorOn);

            // Upper 3-1
            preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getUpper3CHAN() - 1);
            buttonidx = midiButtons.lookupButtonIdx(rbutton31.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            rbutton31.setText(preset.getPatchName());
            offAllUpper3Buttons();
            rbutton31.setStyle(rcolorOn);

            // Lower 1-1
            preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getLower1CHAN() - 1);
            buttonidx = midiButtons.lookupButtonIdx(lbutton11.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            lbutton11.setText(preset.getPatchName());
            offAllLower1Buttons();
            lbutton11.setStyle(lcolorOn);

            // Lower 2-1
            preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getLower2CHAN() - 1);
            buttonidx = midiButtons.lookupButtonIdx(lbutton21.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            lbutton21.setText(preset.getPatchName());
            offAllLower2Buttons();
            lbutton21.setStyle(lcolorOn);

            // Bass 1
            preset = dopresets.getPreset(presetIdx * 16  + sharedStatus.getBassCHAN() - 1);
            buttonidx = midiButtons.lookupButtonIdx(bleft1.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            bleft1.setText(preset.getPatchName());
            offAllBassButtons();
            bleft1.setStyle(bcolorOn);

            // Drum 1
            preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getDrumCHAN() - 1);
            buttonidx = midiButtons.lookupButtonIdx(dleft1.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            dleft1.setText(preset.getPatchName());
            offAllDrumButtons();
            dleft1.setStyle(dcolorOn);
        }
        catch (Exception ex) {
            System.out.println("OrganScene buttonPresetAction: Exception occurred");
            System.out.println(ex);
        }
    }

    private void offAllPresetButtons() {

        ppressed1 = false;
        btnpreset1.setStyle(pcolorOff);
        ppressed2 = false;
        btnpreset2.setStyle(pcolorOff);
        ppressed3 = false;
        btnpreset3.setStyle(pcolorOff);
        ppressed4 = false;
        btnpreset4.setStyle(pcolorOff);
        ppressed5 = false;
        btnpreset5.setStyle(pcolorOff);
        ppressed6 = false;
        btnpreset6.setStyle(pcolorOff);
        ppressed7 = false;
        btnpreset7.setStyle(pcolorOff);
        ppressed8 = false;
        btnpreset8.setStyle(pcolorOff);
    }

    private void offAllUpper1Buttons() {
        rpressed11 = false;
        rbutton11.setStyle(rcolorOff);
        rpressed12 = false;
        rbutton12.setStyle(rcolorOff);
        rpressed13 = false;
        rbutton13.setStyle(rcolorOff);
        rpressed14 = false;
        rbutton14.setStyle(rcolorOff);
        rpressed15 = false;
        rbutton15.setStyle(rcolorOff);
        rpressed16 = false;
        rbutton16.setStyle(orgcolorOff);
        rpressed17 = false;
        rbutton17.setStyle(orgcolorOff);
        rpressed18 = false;
        rbutton18.setStyle(orgcolorOff);
    }

    private void offAllUpper2Buttons() {
        rpressed21 = false;
        rbutton21.setStyle(rcolorOff);
        rpressed22 = false;
        rbutton22.setStyle(rcolorOff);
        rpressed23 = false;
        rbutton23.setStyle(rcolorOff);
        rpressed24 = false;
        rbutton24.setStyle(rcolorOff);
    }

    private void offAllUpper3Buttons() {
        rpressed31 = false;
        rbutton31.setStyle(rcolorOff);
        rpressed32 = false;
        rbutton32.setStyle(rcolorOff);
        rpressed33 = false;
        rbutton33.setStyle(rcolorOff);
        rpressed34 = false;
        rbutton34.setStyle(rcolorOff);
    }

    private void offAllLower1Buttons() {
        lpressed11 = false;
        lbutton11.setStyle(lcolorOff);
        lpressed12 = false;
        lbutton12.setStyle(lcolorOff);
        lpressed13 = false;
        lbutton13.setStyle(lcolorOff);
        lpressed14 = false;
        lbutton14.setStyle(lcolorOff);
        lpressed15 = false;
        lbutton15.setStyle(orgcolorOff);
        lpressed16 = false;
        lbutton16.setStyle(orgcolorOff);
    }

    private void offAllLower2Buttons() {
        lpressed21 = false;
        lbutton21.setStyle(lcolorOff);
        lpressed22 = false;
        lbutton22.setStyle(lcolorOff);
        lpressed23 = false;
        lbutton23.setStyle(lcolorOff);
        lpressed24 = false;
        lbutton24.setStyle(lcolorOff);
    }

    private void offAllBassButtons() {
        bpressed1 = false;
        bleft1.setStyle(bcolorOff);
        bpressed2 = false;
        bleft2.setStyle(bcolorOff);
        bpressed3 = false;
        bleft3.setStyle(bcolorOff);
        bpressed4 = false;
        bleft4.setStyle(bcolorOff);
    }

    private void offAllDrumButtons() {
        dpressed1 = false;
        dleft1.setStyle(dcolorOff);
        dpressed2 = false;
        dleft2.setStyle(dcolorOff);
        dpressed3 = false;
        dleft3.setStyle(dcolorOff);
        dpressed4 = false;
        dleft4.setStyle(dcolorOff);
    }

    /** Save and Apply MIDI Patch for Button just pressed **/

    private void applyMidiButton(int buttonidx, int CHAN, MidiButton midiButton) {

        int PC = midiButton.getPC();
        int LSB = midiButton.getLSB();
        int MSB = midiButton.getMSB();
        playmidifile.sendMidiProgramChange(CHAN, PC, LSB, MSB);

        labelstatusOrg.setText(" Status: Applied Patch: " + midiButton.getPatchName() + " " +  " CHAN:" + (CHAN + 1)
                + " PC:" + midiButton.getPC() + " LSB:" + midiButton.getLSB() + " MSB:" + midiButton.getMSB());

        //System.out.println("OrganScene: applyMidiButton " + buttonidx + " applied " + midiButton.toString());
    }

    // Populate Every Midi Button Patchname on Screen
    private void initMidiButtonPatches(MidiButtons midiButtons) {

        //System.out.println("OrganScene: Initialized all Midi Button Patch Names");
        int patchid = 0;

        try {
            patchid = midiButtons.getButtonById(rbutton11.getId(), 0).getPatchId();
            rbutton11.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(rbutton12.getId(), 0).getPatchId();
            rbutton12.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(rbutton13.getId(), 0).getPatchId();
            rbutton13.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(rbutton14.getId(), 0).getPatchId();
            rbutton14.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(rbutton15.getId(), 0).getPatchId();
            rbutton15.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(rbutton16.getId(), 0).getPatchId();
            rbutton16.setText(dopatches.getMIDIPatch(patchid).getPatchName());

            patchid = midiButtons.getButtonById(rbutton21.getId(), 0).getPatchId();
            rbutton21.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(rbutton22.getId(), 0).getPatchId();
            rbutton22.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(rbutton23.getId(), 0).getPatchId();
            rbutton23.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(rbutton24.getId(), 0).getPatchId();
            rbutton24.setText(dopatches.getMIDIPatch(patchid).getPatchName());

            patchid = midiButtons.getButtonById(rbutton31.getId(), 0).getPatchId();
            rbutton31.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(rbutton32.getId(), 0).getPatchId();
            rbutton32.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(rbutton33.getId(), 0).getPatchId();
            rbutton33.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(rbutton34.getId(), 0).getPatchId();
            rbutton34.setText(dopatches.getMIDIPatch(patchid).getPatchName());

            patchid = midiButtons.getButtonById(lbutton11.getId(), 0).getPatchId();
            lbutton11.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(lbutton12.getId(), 0).getPatchId();
            lbutton12.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(lbutton13.getId(), 0).getPatchId();
            lbutton13.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(lbutton14.getId(), 0).getPatchId();
            lbutton14.setText(dopatches.getMIDIPatch(patchid).getPatchName());

            patchid = midiButtons.getButtonById(lbutton21.getId(), 0).getPatchId();
            lbutton21.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(lbutton22.getId(), 0).getPatchId();
            lbutton22.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(lbutton23.getId(), 0).getPatchId();
            lbutton23.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(lbutton24.getId(), 0).getPatchId();
            lbutton24.setText(dopatches.getMIDIPatch(patchid).getPatchName());

            patchid = midiButtons.getButtonById(bleft1.getId(), 0).getPatchId();
            bleft1.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(bleft2.getId(), 0).getPatchId();
            bleft2.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(bleft3.getId(), 0).getPatchId();
            bleft3.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(bleft4.getId(), 0).getPatchId();
            bleft4.setText(dopatches.getMIDIPatch(patchid).getPatchName());

            patchid = midiButtons.getButtonById(dleft1.getId(), 0).getPatchId();
            dleft1.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(dleft2.getId(), 0).getPatchId();
            dleft2.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(dleft3.getId(), 0).getPatchId();
            dleft3.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(dleft4.getId(), 0).getPatchId();
            dleft4.setText(dopatches.getMIDIPatch(patchid).getPatchName());
        }
        catch (Exception ex) {
            sharedStatus.setStatusText("Error reading MIDI Buttons File: " + buttonFile.toString());

            System.err.println("initMidiButtonPatches: Error reading MIDI Buttons File: " + buttonFile.toString());
            System.err.println(ex);
        }
    }


    // Set Preset Buttons On/Off based current Sound Module and the Module the Song Presets was coded with
    // to avoid loading presets for a module not connected and invalid program changes
    // Note that Deebach can handle both own and GM sounds, but Patch Names may not be correct for Midi GM
    private boolean disablePresetButtons(int moduleidx, int songmoduleidx) {
        boolean bon = false;

        if (moduleidx != songmoduleidx)
            bon = true;

        // If Deebach connected, then do both Deebach and MidiGM
        if (moduleidx == 1)
            bon = false;

        btnpreset1.setDisable(bon);
        btnpreset2.setDisable(bon);
        btnpreset3.setDisable(bon);
        btnpreset4.setDisable(bon);
        btnpreset5.setDisable(bon);
        btnpreset6.setDisable(bon);
        btnpreset7.setDisable(bon);
        btnpreset8.setDisable(bon);

        return (!bon);
    }

    /** Returns the current Scene **/
    public Scene getScene() {
        return scenePerform;
    }

}
