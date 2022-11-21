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
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/*********************************************************
 * Perform/Manual Scene
 *********************************************************/

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
    int yleftright = (int)(35 * ymul);

    int xbtnleftright = (int)(190 * xmul);
    int ybtnleftright = (int)(35 * ymul);

    int xbtnpreset = (int)(120 * xmul);
    int ybtnpreset = (int)(50 * ymul);

    int xvoicebtn = (int)(130 * xmul);
    int yvoicebtn = (int)(50 * ymul);

    int xlayerbtn = xvoicebtn;
    int ylayerbtn = (int)(35 * ymul);

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
    final String lrpressedOff = "-fx-background-color: #CBE9BE; -fx-font-size: " + fsize ;

    final String styletext = "-fx-text-fill: black; -fx-font-size: " + fsize ;
    final String styletextwhite = "-fx-text-fill: white; -fx-font-size: " + fsize ;
    final String styletextwhitesmall = "-fx-text-fill: white; -fx-font-size: " + fsizesmall ;
    final String styletextred = "-fx-text-fill: red; -fx-font-size: " + fsize ;
    final String styletexttitle = "-fx-font-size: " + fsizetitle;

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
    int lastVoiceChannel = 0;
    int lastVoiceChannelSound = 0;

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
    Button bass1 = new Button();
    Button bass2 = new Button();
    Button bass3 = new Button();
    Button bass4 = new Button();

    boolean bpressed1 = false;
    boolean bpressed2 = false;
    boolean bpressed3 = false;
    boolean bpressed4 = false;

    // Tracking Drum Buttons
    Button drum1 = new Button();
    Button drum2 = new Button();
    Button drum3 = new Button();
    Button drum4 = new Button();

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

    Button b1layerbtn;
    boolean b1pressed = true;

    boolean octaveflg = true;

    Slider sliderVOL;
    Slider sliderEXP;
    Slider sliderREV;
    Slider sliderCHO;
    Slider sliderMOD;
    Slider sliderBRI;
    Slider sliderPAN;
    Slider sliderOCT;
    int rotvalue = 0;

    // https://professionalcomposers.com/midi-cc-list/
    public static byte ccVOL = 7;
    public static byte ccEXP = 11;
    public static byte ccREV = 91;
    public static byte ccCHO = 93;
    public static byte ccMOD = 1;
    public static byte ccPAN = 10;

    public static byte ccTIM = 71;
    public static byte ccREL = 72;
    public static byte ccATK = 73;
    public static byte ccBRI = 74;

    /*********************************************************
     * Creates a Perform Scene.
     *********************************************************/

    //public PerformScene(Stage primaryStage, Scene returnScene) {
    public PerformScene(Stage primaryStage) {

        System.out.println("PerformScene: AMIDIFX Perform Scene Starting");

        try {
            // Create instance of Shared Status to report back to Scenes
            sharedStatus = SharedStatus.getInstance();
            lastVoiceChannel = sharedStatus.getDemoCHAN();
            lastVoiceChannelSound = lastVoiceChannel;

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

            AppConfig config = AppConfig.getInstance();

            // To Do: Generalize the first two Songs in the Song List and ensure cannot be deleted
            if (config.getSoundModuleIdx() == 1) {
                sharedStatus.setPresetFile(config.getPresetFileName(1));
                idxSongList = 1;
            }
            else if (config.getSoundModuleIdx() == 2) {
                sharedStatus.setPresetFile(config.getPresetFileName(2));
                idxSongList = 2;
            }
            else {
                sharedStatus.setPresetFile(config.getPresetFileName(0));
                idxSongList = 0;
            }

            // Get instance of Arduino Utilities
            arduinoUtils = ArduinoUtils.getInstance();

            // Get instance of MidiDevices Configuration
            MidiDevices mididevices = MidiDevices.getInstance();

            // Load MIDI Default MIDI Preset file on start up
            dopresets = MidiPresets.getInstance();
            presetFile = sharedStatus.getPresetFile();
            if (!dopresets.loadMidiPresets(presetFile)) {
                labelstatusOrg.setText(" Status: Error loading preset file " + presetFile);
                labelstatusOrg.setStyle(styletextred);

                System.err.println("PerformScene Init: Error loading Preset file: " + presetFile);
                try {
                    wait(10000);
                }
                catch(Exception exception) {}
            }
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

            // Load the MIDI Button definitions
            if (!midiButtons.loadMidiButtons(buttonFile)) {
                labelstatusOrg.setText(" Status: Error loading Button PRF file " + buttonFile);
                labelstatusOrg.setStyle(styletextred);

                System.err.println("PerformScene: Error loading PRF File " + buttonFile);
                Thread.sleep(2500);
                labelstatusOrg.setStyle(styletext);
            }
            System.out.println("PerformScene: Successfully loaded PRF File " + buttonFile);

            labelstatusOrg.setText(" Status: Loaded Button PRF file " + buttonFile);
            labelstatusOrg.setStyle(styletext);

            // Create top bar navigation buttons

            Button buttonsc1 = new Button("Manual");
            buttonsc1.setStyle(btnMenuOn);
            buttonsc1.setOnAction(e -> {
                // Reset Channel layering to default every time we switch Scenes to avoid unwanted layering
                defaultChannelLayering();

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
                // Reset Channel layering to default every time we switch Scenes to avoid unwanted layering
                defaultChannelLayering();

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
                // Reset Channel Layering to default every time we switch Scenes to avoid unwanted Layering while working the Presets
                defaultChannelLayering();

                // Only allow Preset edits on correct, connected sound module
                if (sharedStatus.getModuleidx() != dosongs.getSong(idxSongList).getModuleIdx()) {

                    MidiModules midimodule = new MidiModules();
                    labelstatusOrg.setText(" Status: To edit Presets, connect module " + midimodule.getModuleName(dosongs.getSong(idxSongList).getModuleIdx()));
                    return;
                }

                // For newly selected Song, change to the first Preset and 16 Channels
                presetFile = sharedStatus.getPresetFile();
                if (!dopresets.loadMidiPresets(presetFile)) {
                    labelstatusOrg.setText(" Status: Error loading preset file " + presetFile);
                    labelstatusOrg.setStyle(styletextred);

                    System.err.println("PerformScene Init: Error loading Preset file: " + presetFile);
                    try {
                        wait(10000);
                    }
                    catch(Exception exception) {}
                    labelstatusOrg.setStyle(styletext);
                }
                System.out.println("PerformScene Init: Loaded new Preset file: " + presetFile);

                for (int idx = 0; idx < 16; idx++) {
                    MidiPreset midiPreset = new MidiPreset();
                    midiPreset = dopresets.getPreset(idx);

                    String strName = Integer.toString(idx + 1).concat(":").concat(midiPreset.getPatchName());
                    sharedStatus.getPresetListView().getItems().set(idx, strName);
                }
                sharedStatus.getPresetCombo().getSelectionModel().select(0);
                sharedStatus.getPresetListView().refresh();

                // Switch to Presets Scene, and initialize appropriately, e.g. ensure Song Details is updated and Save Button is off
                Button buttonPresetSceneInit = sharedStatus.getButtonPresetSceneInit();
                buttonPresetSceneInit.fire();

                //System.out.println(("PerformScene: Changing to Presets Scene " + sharedStatus.getPresetsScene().toString()));
                primaryStage.setScene(sharedStatus.getPresetsScene());
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.err.println("PerformScene: Unable to set Presets Scene!");
                }
            });

            // Save Performance Button
            buttonSave = new Button("Save Manual");
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

            // Middle Song Click Button
            Button buttonSongLoad = new Button(songTitle);
            buttonSongLoad.setStyle(selectcolorOff);
            buttonSongLoad.setPrefSize(xbtnleftright, ybtnleftright);
            buttonSongLoad.setAlignment(Pos.CENTER);
            buttonSongLoad.setOnAction(e -> {

                // Check if Song Module setting is the same as connected Module
                if (sharedStatus.getModuleidx() != dosongs.getSong(idxSongList).getModuleIdx()) {
                    labelstatusOrg.setText(" Status: Unable to play. Preset configured for module " + sharedStatus.getModuleName(dosongs.getSong(idxSongList).getModuleIdx()));
                    labelstatusOrg.setStyle(styletextred);

                    return;
                }

                buttonPresetLoad(dosongs.getSong(idxSongList).getPresetFile());

                buttonSongLoad.setStyle(selectcolorOn);

                sharedStatus.setPresetFile(dosongs.getSong(idxSongList).getPresetFile());
                sharedStatus.setMidiFile(dosongs.getSong(idxSongList).getMidiFile());
                sharedStatus.setSongTitle(dosongs.getSong(idxSongList).getSongTitle());
                sharedStatus.setStatusText("Selected Preset File " + presetFile);

                // Preset Time Signature for correct Bar Time Display
                sharedStatus.setTimeSig(dosongs.getSong(idxSongList).getTimeSig());

                MidiSong midiSong = dosongs.getSong(idxSongList);
                sharedStatus.setCurrentSong(midiSong);

                // Check if Sound Module matches the active Module, enable/disable Presets and Autoload Preset 0
                boolean modulematch = disablePresetButtons(sharedStatus.getModuleidx(), dosongs.getSong(idxSongList).getModuleIdx());
                if (modulematch) {
                    ////dopresets.loadMidiPresets(sharedStatus.getPresetFile());
                    if (!dopresets.loadMidiPresets(sharedStatus.getPresetFile())) {
                        labelstatusOrg.setText(" Status: Error loading preset file " + sharedStatus.getPresetFile());
                        labelstatusOrg.setStyle(styletextred);

                        try {
                            wait(10000);
                        }
                        catch(Exception exception) {}
                        labelstatusOrg.setStyle(styletext);
                    }
                    else {
                        buttonPresetAction(0);
                        btnpreset1.setStyle(pcolorOn);
                        labelstatusOrg.setText(" Status1: Presets loaded for " + dosongs.getSong(idxSongList).getSongTitle());
                        labelstatusOrg.setStyle(styletext);
                    }

                    // Enable Song Play, Backing and Edit Presets Buttons
                    btnplay.setDisable(false);
                    btnbacking.setDisable(false);
                    buttonsc3.setDisable(false);

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
                // Decrement to previous Song and turn to end of list when index goes below 0
                if (--idxSongList < 0) idxSongList = dosongs.getSongListSize() - 1;

                songTitle = dosongs.getSong(idxSongList).getSongTitle();
                songFile = dosongs.getSong(idxSongList).getMidiFile();
                buttonSongLoad.setText(songTitle);

                // New Song to be selected for Play
                buttonSongLoad.setStyle(selectcolorOff);
                btnplay.setDisable(true);
                buttonsc3.setDisable(true);

                offAllPresetButtons();

                labelstatusOrg.setText(" Status: Click Song to select.");
                labelstatusOrg.setStyle(styletext);
                //System.out.println("PerformScene: Previous Song " + songTitle);
            });

            Button buttonSongNameRight = new Button(">>");
            buttonSongNameRight.setStyle(selectcolorOff);
            buttonSongNameRight.setPrefSize(xleftright, yleftright);
            buttonSongNameRight.setOnAction(e -> {
                // Increment to next Song and turn to first in list when index goes below 0
                if (++idxSongList > dosongs.getSongListSize() - 1) idxSongList = 0;

                songTitle = dosongs.getSong(idxSongList).getSongTitle();
                songFile = dosongs.getSong(idxSongList).getMidiFile();
                buttonSongLoad.setText(songTitle);

                // New Song selected
                buttonSongLoad.setStyle(selectcolorOff);
                btnplay.setDisable(true);
                buttonsc3.setDisable(true);

                offAllPresetButtons();

                labelstatusOrg.setText(" Status: Click Song to select.");
                labelstatusOrg.setStyle(styletext);
                //System.out.println("PerformScene: Next Song " + songTitle);
            });

            // Assemble the Song Navigation Controls
            HBox hboxSong = new HBox();
            hboxSong.setSpacing(5);
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

                labelsynth.setText("Module: " + config.getOutDevice());

                buttonSoundBank.setStyle(selectcolorOn);

                labelstatusOrg.setText(" Status: Next select new Voice");
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

                labelstatusOrg.setText(" Status: Click Sound Bank to select.");
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

                labelstatusOrg.setText(" Status: Click Sound Bank to select.");
                //System.out.println("PerformScene: Previous Bank " + bankname);
            });

            // Assemble the Song Navigation Controls
            HBox hboxBank = new HBox();
            hboxBank.setSpacing(5);
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

                if (!bnewpatchselected) {
                    buttonSoundFont.setStyle(selectcolorOn);
                    bnewpatchselected = true;

                    labelstatusOrg.setText(" Status: Selected " + fontname + ". Click on Voice Button to add, or unclick to cancel.");
                    //System.out.println("PerformScene: Loaded Voice " + fontname);
                }
                else {
                    buttonSoundFont.setStyle(selectcolorOff);
                    bnewpatchselected = false;

                    labelstatusOrg.setText(" Status: Voice " + fontname + " cleared.");
                }
            });

            Button buttonSoundFontLeft = new Button("<<");
            buttonSoundFontLeft.setStyle(selectcolorOff);
            buttonSoundFontLeft.setPrefSize(xleftright, yleftright);
            buttonSoundFontLeft.setOnAction(e -> {
                if (patchidx > bankpatchidx) --patchidx;
                fontname = dopatches.getMIDIPatch(patchidx).getPatchName();
                buttonSoundFont.setText(fontname);

                labelstatusOrg.setText(" Status: Click Voice to select.");
                //System.out.println("PerformScene: Previous Voice " + fontname);
            });

            Button buttonSoundFontRight = new Button(">>");
            buttonSoundFontRight.setStyle(selectcolorOff);
            buttonSoundFontRight.setPrefSize(xleftright, yleftright);
            buttonSoundFontRight.setOnAction(e -> {
                if (patchidx < dopatches.getMIDIPatchSize() - 1) ++patchidx;

                fontname = dopatches.getMIDIPatch(patchidx).getPatchName();
                buttonSoundFont.setText(fontname);

                labelstatusOrg.setText(" Status: Click Voice to select.");
                //System.out.println("PerformScene: Next Voice " + fontname);
            });

            // Assemble the Sound Font Navigation Controls
            HBox hboxFont = new HBox();
            hboxFont.setSpacing(2);
            hboxFont.getChildren().add(buttonSoundFontLeft);
            hboxFont.getChildren().add(buttonSoundFont);
            hboxFont.getChildren().add(buttonSoundFontRight);

            // Voice Test Button
            Button btntest = new Button("Demo");
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

                        // Remember last voice channel that sounded, so that this one is remembered for turn off
                        lastVoiceChannelSound = lastVoiceChannel;
                        btestnote = true;
                    }
                    else {
                        btntest.setText("Demo");
                        btntest.setStyle(btnplayOff);

                        PlayMidi playmidifile = PlayMidi.getInstance();
                        playmidifile.sendMidiNote((byte)lastVoiceChannelSound, (byte)60, false);

                        btestnote = false;

                        // Re-Apply MIDI Program Change on Upper Channel for Button Press since we used it for sound Demo
                        int CHAN = sharedStatus.getUpper1CHAN();
                        int buttonidx = midiButtons.lookupButtonIdx(rbutton11.getId());
                        applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    }
                    labelstatusOrg.setText(" Status: ");
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
            gridTopLine.setVgap(20);
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

            b1layerbtn = new Button("Bass [" + (sharedStatus.getBassCHAN() + 1) + "]");
            b1layerbtn.setStyle(lrpressedOn);
            b1layerbtn.setMaxSize(xlayerbtn, ylayerbtn);
            b1layerbtn.setMinSize(xlayerbtn, ylayerbtn);
            //b1layerbtn.setDisable(!arduinoUtils.hasARMPort());
            b1layerbtn.setOnAction(event -> {
                if (b1pressed == false) {
                    mididevices.layerChannel(sharedStatus.getBassCHAN(), true);

                    b1layerbtn.setStyle(lrpressedOn);
                    b1pressed = true;

                    labelstatusOrg.setText(" Status: Layer Bass On");
                }
                else {
                    mididevices.layerChannel(sharedStatus.getBassCHAN(), false);

                    b1layerbtn.setStyle(lrpressedOff);
                    b1pressed = false;

                    labelstatusOrg.setText(" Status: Layer Bass Off");
                }
            });
            mididevices.layerChannel(sharedStatus.getBassCHAN(), true);

            bass1 = new Button(" Bass 1");
            bass1.setId("B1-1");
            bass1.setMaxSize(xvoicebtn, yvoicebtn);
            bass1.setMinSize(xvoicebtn, yvoicebtn);
            bass1.setStyle(bcolorOff);
            bass1.setWrapText(true);
            bass1.setWrapText(true);
            bass1.setTextAlignment(TextAlignment.CENTER);
            bass1.setOnAction(event -> {
                offAllBassButtons();
                int buttonidx = midiButtons.lookupButtonIdx(bass1.getId());

                lastVoiceButton = bass1.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(bass1.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    bass1.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

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

                    MidiButton midibutton = midiButtons.getButtonById(bass1.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    bass1.setStyle(bcolorOn);
                }
                else {
                    bass1.setStyle(bcolorOff);
                }
                bpressed1 = !bpressed1;

                labelstatusOrg.setText(" Status: Applied B1. Use Sliders to adjust B1 Effects.");
            });

            bass2.setText(" Bass 2");
            bass2.setMaxSize(xvoicebtn, yvoicebtn);
            bass2.setMinSize(xvoicebtn, yvoicebtn);
            bass2.setId("B1-2");
            bass2.setStyle(bcolorOff);
            bass2.setWrapText(true);
            bass2.setTextAlignment(TextAlignment.CENTER);
            bass2.setOnAction(event -> {
                offAllBassButtons();
                int buttonidx = midiButtons.lookupButtonIdx(bass2.getId());

                lastVoiceButton = bass2.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(bass2.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    bass2.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

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

                    MidiButton midibutton = midiButtons.getButtonById(bass2.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    bass2.setStyle(bcolorOn);
                }
                else {
                    bass2.setStyle(bcolorOff);
                }
                bpressed2 = !bpressed2;

                labelstatusOrg.setText(" Status: Applied B2. Use Sliders to adjust B2 Effects.");
            });

            bass3.setText(" Bass 3");
            bass3.setMaxSize(xvoicebtn, yvoicebtn);
            bass3.setMinSize(xvoicebtn, yvoicebtn);
            bass3.setId("B1-3");
            bass3.setStyle(bcolorOff);
            bass3.setWrapText(true);
            bass3.setTextAlignment(TextAlignment.CENTER);
            bass3.setOnAction(event -> {
                offAllBassButtons();
                int buttonidx = midiButtons.lookupButtonIdx(bass3.getId());

                lastVoiceButton = bass3.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(bass3.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    bass3.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

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

                    MidiButton midibutton = midiButtons.getButtonById(bass3.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    bass3.setStyle(bcolorOn);
                }
                else {
                    bass3.setStyle(bcolorOff);
                }
                bpressed3 = !bpressed3;

                labelstatusOrg.setText(" Status: Applied B3. Use Sliders to adjust B3 Effects.");
            });
            
            bass4.setText(" Bass 4");
            bass4.setId("B1-4");
            bass4.setMaxSize(xvoicebtn, yvoicebtn);
            bass4.setMinSize(xvoicebtn, yvoicebtn);
            bass4.setStyle(bcolorOff);
            bass4.setWrapText(true);
            bass4.setTextAlignment(TextAlignment.CENTER);
            bass4.setOnAction(event -> {
                offAllBassButtons();
                int buttonidx = midiButtons.lookupButtonIdx(bass4.getId());

                lastVoiceButton = bass4.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(bass4.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    bass4.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

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

                    MidiButton midibutton = midiButtons.getButtonById(bass4.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    bass4.setStyle(bcolorOn);
                }
                else {
                    bass4.setStyle(bcolorOff);
                }
                bpressed4 = !bpressed4;

                labelstatusOrg.setText(" Status: Applied B4. Use Sliders to adjust B4 Effects.");
            });

            gridmidcenterPerform.add(b1layerbtn, 0, 0, 1, 1);
            gridmidcenterPerform.add(bass1, 0, 1, 1, 1);
            gridmidcenterPerform.add(bass2, 1, 1, 1, 1);
            gridmidcenterPerform.add(bass3, 0, 2, 1, 1);
            gridmidcenterPerform.add(bass4, 1, 2, 1, 1);

            Button d1layerbtn = new Button("Drums [" + (sharedStatus.getDrumCHAN() + 1) + "]");
            d1layerbtn.setStyle(styletext);
            d1layerbtn.setStyle(lrpressedOn);
            d1layerbtn.setMaxSize(xlayerbtn, ylayerbtn);
            d1layerbtn.setMinSize(xlayerbtn, ylayerbtn);

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

            drum1.setText(" Drums 1");
            drum1.setId("D1-1");
            drum1.setMaxSize(xvoicebtn, yvoicebtn);
            drum1.setMinSize(xvoicebtn, yvoicebtn);
            drum1.setStyle(dcolorOff);
            drum1.setWrapText(true);
            drum1.setTextAlignment(TextAlignment.CENTER);
            drum1.setOnAction(event -> {
                offAllDrumButtons();
                int buttonidx = midiButtons.lookupButtonIdx(drum1.getId());

                lastVoiceButton = drum1.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(drum1.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    drum1.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

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

                    MidiButton midibutton = midiButtons.getButtonById(drum1.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(0);

                    drum1.setStyle(dcolorOn);
                }
                else {
                    drum1.setStyle(dcolorOff);
                }
                dpressed1 = !dpressed1;

                labelstatusOrg.setText(" Status: Applied D1. Use Sliders to adjust D1 Effects.");
            });

            drum2.setText(" Drums 2");
            drum2.setId("D1-2");
            drum2.setMaxSize(xvoicebtn, yvoicebtn);
            drum2.setMinSize(xvoicebtn, yvoicebtn);
            drum2.setStyle(dcolorOff);
            drum2.setWrapText(true);
            drum2.setTextAlignment(TextAlignment.CENTER);
            drum2.setOnAction(event -> {
                offAllDrumButtons();
                int buttonidx = midiButtons.lookupButtonIdx(drum2.getId());

                lastVoiceButton = drum2.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(drum2.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    drum2.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

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

                    MidiButton midibutton = midiButtons.getButtonById(drum2.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(0);

                    drum2.setStyle(dcolorOn);
                }
                else {
                    drum2.setStyle(dcolorOff);
                }
                dpressed2 = !dpressed2;

                labelstatusOrg.setText(" Status: Applied D2. Use Sliders to adjust D2 Effects.");
            });

            drum3.setText(" Drums 3");
            drum3.setId("D1-3");
            drum3.setMaxSize(xvoicebtn, yvoicebtn);
            drum3.setMinSize(xvoicebtn, yvoicebtn);
            drum3.setStyle(dcolorOff);
            drum3.setWrapText(true);
            drum3.setTextAlignment(TextAlignment.CENTER);
            drum3.setOnAction(event -> {
                offAllDrumButtons();
                int buttonidx = midiButtons.lookupButtonIdx(drum3.getId());

                lastVoiceButton = drum3.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(drum3.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    drum3.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

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

                    MidiButton midibutton = midiButtons.getButtonById(drum3.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(0);

                    drum3.setStyle(dcolorOn);
                }
                else {
                    drum3.setStyle(dcolorOff);
                }
                dpressed3 = !dpressed3;

                labelstatusOrg.setText(" Status: Applied D3. Use Sliders to adjust D3 Effects.");
            });

            drum4.setText(" Drums 4");
            drum4.setId("D1-4");
            drum4.setMaxSize(xvoicebtn, yvoicebtn);
            drum4.setMinSize(xvoicebtn, yvoicebtn);
            drum4.setStyle(dcolorOff);
            drum4.setWrapText(true);
            drum4.setTextAlignment(TextAlignment.CENTER);
            drum4.setOnAction(event -> {
                offAllDrumButtons();

                lastVoiceButton = drum4.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(drum4.getId());

                int buttonidx = midiButtons.lookupButtonIdx(drum4.getId());

                // New Voice available to be programmed into this button
                if (bnewpatchselected) {
                    drum4.setText(dopatches.getMIDIPatch(patchidx).getPatchName());

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

                    MidiButton midibutton = midiButtons.getButtonById(drum4.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderMOD.setValue(midibutton.getMOD());
                    sliderPAN.setValue(midibutton.getPAN());
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(0);

                    drum4.setStyle(dcolorOn);
                }
                else {
                    drum4.setStyle(dcolorOff);
                }
                dpressed4 = !dpressed4;

                labelstatusOrg.setText(" Status: Applied D4. Use Sliders to adjust D4 Effects.");
            });

            gridmidcenterPerform.add(d1layerbtn, 0, 3, 1, 1);
            gridmidcenterPerform.add(lblbeatcount, 1, 3, 1, 1);
            gridmidcenterPerform.add(drum1, 0, 4, 1, 1);
            gridmidcenterPerform.add(drum2, 1, 4, 1, 1);
            gridmidcenterPerform.add(drum3, 0, 5, 1, 1);
            gridmidcenterPerform.add(drum4, 1, 5, 1, 1);

            // Lower Buttons

            l1layerbtn = new Button("Lower 1 [" + (sharedStatus.getLower1CHAN() + 1)+ "]   ");       // Lefthand Layering Buttons
            l1layerbtn.setStyle(lrpressedOn);
            l1layerbtn.setMaxSize(xlayerbtn, ylayerbtn);
            l1layerbtn.setMinSize(xlayerbtn, ylayerbtn);
            //l1layerbtn.setDisable(!arduinoUtils.hasARMPort());
            l1layerbtn.setOnAction(event -> {
                if (l1pressed == false) {
                    mididevices.layerChannel(sharedStatus.getLower1CHAN(), true);

                    l1layerbtn.setStyle(lrpressedOn);
                    l1pressed = true;

                    labelstatusOrg.setText(" Status: Layer Lower 1 On");
                }
                else {
                    mididevices.layerChannel(sharedStatus.getLower1CHAN(), false);

                    l1layerbtn.setStyle(lrpressedOff);
                    l1pressed = false;

                    labelstatusOrg.setText(" Status: Layer Lower 1 Off");
                }
                ////arduinoUtils.lefthandLayerSysexData(l1pressed, l2pressed);
            });
            mididevices.layerChannel(sharedStatus.getLower1CHAN(), true);

            l2layerbtn = new Button("Lower 2 [" + (sharedStatus.getLower2CHAN() + 1) + "]   ");
            l2layerbtn.setStyle(lrpressedOff);
            l2layerbtn.setMaxSize(xlayerbtn, ylayerbtn);
            l2layerbtn.setMinSize(xlayerbtn, ylayerbtn);
            l2layerbtn.setDisable(!sharedStatus.getlower1Kbdlayerenabled());
            //l2layerbtn.setDisable(!arduinoUtils.hasARMPort());
            l2layerbtn.setOnAction(event -> {
                if (l2pressed == false) {
                    mididevices.layerChannel(sharedStatus.getLower2CHAN(), true);

                    l2layerbtn.setText("L Lower 1 [" + (sharedStatus.getLower2CHAN() + 1) + "]   ");
                    l2layerbtn.setStyle(lrpressedOn);
                    l2pressed = true;

                    labelstatusOrg.setText(" Status: Layer Lower 2 On");
                }
                else {
                    mididevices.layerChannel(sharedStatus.getLower2CHAN(), false);

                    l2layerbtn.setText("Lower 2 [" + (sharedStatus.getLower2CHAN() + 1) + "]   ");
                    l2layerbtn.setStyle(lrpressedOff);
                    l2pressed = false;

                    labelstatusOrg.setText(" Status: Layer Lower 2 Off");
                }
                ////arduinoUtils.lefthandLayerSysexData(l1pressed, l2pressed);
            });
            mididevices.layerChannel(sharedStatus.getLower2CHAN(), false);

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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    lbutton11.setStyle(lcolorOn);
                }
                else {
                    lbutton11.setStyle(lcolorOff);
                }
                lpressed11 = !lpressed11;

                labelstatusOrg.setText(" Status: Applied L1-1. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    lbutton12.setStyle(lcolorOn);
                }
                else {
                    lbutton12.setStyle(lcolorOff);
                }
                lpressed12 = !lpressed12;

                labelstatusOrg.setText(" Status: Applied L1-2. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    lbutton13.setStyle(lcolorOn);
                }
                else {
                    lbutton13.setStyle(lcolorOff);
                }
                lpressed13 = !lpressed13;

                labelstatusOrg.setText(" Status: Applied L1-3. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    lbutton14.setStyle(lcolorOn);
                }
                else {
                    lbutton14.setStyle(lcolorOff);
                }
                lpressed14 = !lpressed14;

                labelstatusOrg.setText(" Status: Applied L1-4. Use Sliders to adjust Effects.");
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
                lbutton16.setDisable(true);
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
                lbutton16.setDisable(false);
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    lbutton21.setStyle(lcolorOn);
                }
                else {
                    lbutton21.setStyle(lcolorOff);
                }
                lpressed21 = !lpressed21;

                labelstatusOrg.setText(" Status: Applied L2-1. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    lbutton22.setStyle(lcolorOn);
                }
                else {
                    lbutton22.setStyle(lcolorOff);
                }
                lpressed22 = !lpressed22;

                labelstatusOrg.setText(" Status: Applied L2-2. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    lbutton23.setStyle(lcolorOn);
                }
                else {
                    lbutton23.setStyle(lcolorOff);
                }
                lpressed23 = !lpressed23;

                labelstatusOrg.setText(" Status: Applied L2-3. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    lbutton24.setStyle(lcolorOn);
                }
                else {
                    lbutton24.setStyle(lcolorOff);
                }
                lpressed24 = !lpressed24;

                labelstatusOrg.setText(" Status: Applied L2-4. Use Sliders to adjust Effects.");
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

            r1layerbtn = new Button("Upper 1 ["+(sharedStatus.getUpper1CHAN() + 1) +"]   ");       // Righthand Layering Buttons
            r1layerbtn.setStyle(lrpressedOn);
            r1layerbtn.setMaxSize(xlayerbtn, ylayerbtn);
            r1layerbtn.setMinSize(xlayerbtn, ylayerbtn);
            //r1layerbtn.setDisable(!arduinoUtils.hasARMPort());
            r1layerbtn.setOnAction(event -> {
                if (r1pressed == false) {
                    mididevices.layerChannel(sharedStatus.getUpper1CHAN(), true);

                    r1layerbtn.setStyle(lrpressedOn);
                    r1pressed = true;

                    labelstatusOrg.setText(" Status: Layer Upper 1 On");
                }
                else {
                    mididevices.layerChannel(sharedStatus.getUpper1CHAN(), false);

                    r1layerbtn.setStyle(lrpressedOff);
                    r1pressed = false;

                    labelstatusOrg.setText(" Status: Layer Upper 1 Off");
                }
                ////arduinoUtils.righthandLayerSysexData(r1pressed, r2pressed, r3pressed);
            });
            mididevices.layerChannel(sharedStatus.getUpper1CHAN(), true);

            r2layerbtn = new Button("Upper 2 [" + (sharedStatus.getUpper2CHAN() + 1) + "]   ");
            r2layerbtn.setStyle(lrpressedOff);
            r2layerbtn.setMaxSize(xlayerbtn, ylayerbtn);
            r2layerbtn.setMinSize(xlayerbtn, ylayerbtn);
            r2layerbtn.setDisable(!sharedStatus.getUpper1KbdLayerEnabled());
            //r2layerbtn.setDisable(!arduinoUtils.hasARMPort());
            r2layerbtn.setOnAction(event -> {
                if (r2pressed == false) {
                    mididevices.layerChannel(sharedStatus.getUpper2CHAN(), true);

                    r2layerbtn.setText("L Upper 1 [" + (sharedStatus.getUpper2CHAN() + 1) + "]   ");
                    r2layerbtn.setStyle(lrpressedOn);
                    r2pressed = true;

                    labelstatusOrg.setText(" Status: Layer Upper 2 On");
                }
                else {
                    mididevices.layerChannel(sharedStatus.getUpper2CHAN(), false);

                    r2layerbtn.setText("Upper 2 [" + (sharedStatus.getUpper2CHAN() + 1) + "]   ");
                    r2layerbtn.setStyle(lrpressedOff);
                    r2pressed = false;

                    labelstatusOrg.setText(" Status: Layer Upper 2 Off");
                }
                ////arduinoUtils.righthandLayerSysexData(r1pressed, r2pressed, r3pressed);
            });
            mididevices.layerChannel(sharedStatus.getUpper2CHAN(), false);

            r3layerbtn = new Button("Upper 3 [" + (sharedStatus.getUpper3CHAN() + 1) + "]   ");
            r3layerbtn.setStyle(lrpressedOff);
            r3layerbtn.setMaxSize(xlayerbtn, ylayerbtn);
            r3layerbtn.setMinSize(xlayerbtn, ylayerbtn);
            r3layerbtn.setDisable(!sharedStatus.getupper2Kbdlayerenabled());
            //r3layerbtn.setDisable(!arduinoUtils.hasARMPort());
            r3layerbtn.setOnAction(event -> {
                if (r3pressed == false) {
                    mididevices.layerChannel(sharedStatus.getUpper3CHAN(), true);

                    r3layerbtn.setText("L Upper 1 [" + (sharedStatus.getUpper3CHAN() + 1) + "]   ");
                    r3layerbtn.setStyle(lrpressedOn);
                    r3pressed = true;

                    labelstatusOrg.setText(" Status: Layer Upper 3 On");
                }
                else {
                    mididevices.layerChannel(sharedStatus.getUpper3CHAN(), false);

                    r3layerbtn.setText("Upper 3 [" + (sharedStatus.getUpper3CHAN() + 1) + "]   ");
                    r3layerbtn.setStyle(lrpressedOff);
                    r3pressed = false;

                    labelstatusOrg.setText(" Status: Layer Upper 3 Off");
                }
                ////arduinoUtils.righthandLayerSysexData(r1pressed, r2pressed, r3pressed);

            });
            mididevices.layerChannel(sharedStatus.getUpper3CHAN(), false);

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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton11.setStyle(rcolorOn);
                }
                else {
                    rbutton11.setStyle(rcolorOff);
                }
                rpressed11 = !rpressed11;

                labelstatusOrg.setText(" Status: Applied U1-1. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton12.setStyle(rcolorOn);
                }
                else {
                    rbutton12.setStyle(rcolorOff);
                }
                rpressed12 = !rpressed12;

                labelstatusOrg.setText(" Status: Applied U1-2. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton13.setStyle(rcolorOn);
                }
                else {
                    rbutton13.setStyle(rcolorOff);
                }
                rpressed13 = !rpressed13;

                labelstatusOrg.setText(" Status: Applied U1-3. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton14.setStyle(rcolorOn);
                }
                else {
                    rbutton14.setStyle(rcolorOff);
                }
                rpressed14 = !rpressed14;

                lastVoiceButton = rbutton14.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton14.getId());

                labelstatusOrg.setText(" Status: Applied U1-4. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton15.setStyle(rcolorOn);
                }
                else {
                    rbutton15.setStyle(rcolorOff);
                }
                rpressed15 = !rpressed15;

                labelstatusOrg.setText(" Status: Applied U1-5. Use Sliders to adjust Effects.");
            });

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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton16.setStyle(orgcolorOn);
                }
                else {
                    rbutton16.setStyle(orgcolorOff);
                }
                rpressed16 = !rpressed16;

                labelstatusOrg.setText(" Status: Applied U1-6. Use Sliders to adjust Effects.");
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
                rbutton18.setDisable(true);
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
                rbutton18.setDisable(false);
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton21.setStyle(rcolorOn);
                }
                else {
                    rbutton21.setStyle(rcolorOff);
                }
                rpressed21 = !rpressed21;

                labelstatusOrg.setText(" Status: Applied U2-1. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton22.setStyle(rcolorOn);
                }
                else {
                    rbutton22.setStyle(rcolorOff);
                }
                rpressed22 = !rpressed22;

                labelstatusOrg.setText(" Status: Applied U2-2. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton23.setStyle(rcolorOn);
                }
                else {
                    rbutton23.setStyle(rcolorOff);
                }
                rpressed23 = !rpressed23;

                labelstatusOrg.setText(" Status: Applied U2-3. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton24.setStyle(rcolorOn);
                }
                else {
                    rbutton24.setStyle(rcolorOff);
                }
                rpressed24 = !rpressed24;

                labelstatusOrg.setText(" Status: Applied U2-4. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton31.setStyle(rcolorOn);
                }
                else {
                    rbutton31.setStyle(rcolorOff);
                }
                rpressed31 = !rpressed31;

                labelstatusOrg.setText(" Status: Applied U3-1. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton32.setStyle(rcolorOn);
                }
                else {
                    rbutton32.setStyle(rcolorOff);
                }
                rpressed32 = !rpressed32;

                labelstatusOrg.setText(" Status: Applied U3-2. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton33.setStyle(rcolorOn);
                }
                else {
                    rbutton33.setStyle(rcolorOff);
                }
                rpressed33 = !rpressed33;

                labelstatusOrg.setText(" Status: Applied U3-3. Use Sliders to adjust Effects.");
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
                    sliderBRI.setValue(midibutton.getBRI());
                    sliderOCT.setValue(midibutton.getOctaveTran());

                    rbutton34.setStyle(rcolorOn);
                }
                else {
                    rbutton34.setStyle(rcolorOff);
                }
                rpressed34 = !rpressed34;

                labelstatusOrg.setText(" Status: Applied U3-4. Use Sliders to adjust Effects.");
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
                            // Disable Songs and Preset menu switch while playing
                            buttonsc2.setDisable(true);
                            buttonsc3.setDisable(true);

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

                        // Enable Songs and Preset menu switch once stopped playing
                        buttonsc2.setDisable(false);
                        buttonsc3.setDisable(false);
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
                        playmidifile.unmuteKeyboardTracks(dosongs.getSong(idxSongList));
                    }
                }
                else {
                    btnbacking.setText("Backing");
                    btnbacking.setStyle(btnplayOff);
                    playmode = 3;

                    PlayMidi playmidifile = PlayMidi.getInstance();
                    if (playmidifile.isMidiRunning()) {
                        playmidifile.muteKeyboardTracks(dosongs.getSong(idxSongList));
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

            // Create BRI (Brightness) slider
            sliderBRI = new Slider(0, 127, 0);
            sliderBRI.setOrientation(Orientation.VERTICAL);
            sliderBRI.setShowTickLabels(true);
            sliderBRI.setShowTickMarks(true);
            sliderBRI.setMajorTickUnit(16);
            sliderBRI.setBlockIncrement(8);
            Rotate rotateBri = new Rotate();
            sliderBRI.valueProperty().addListener((observable, oldValue, newValue) -> {
                //Setting the angle for the rotation
                rotateBri.setAngle((double) newValue);

                PlayMidi playmidifile = PlayMidi.getInstance();
                playmidifile.sendMidiControlChange((byte) lastVoiceChannel, ccBRI, (byte) sliderBRI.getValue());

                midiButtons.getButtonById(lastVoiceButton, 0).setBRI((int)sliderBRI.getValue());

                buttonSave.setDisable(false);
                flgDirtyPreset = true;      // Need to save updated Preset

                if (lastVoiceButton != null)
                    labelstatusOrg.setText(" Status: Button " + lastVoiceButton + " BRI= " + newValue.intValue());
            });
            sliderBRI.setValue(midiButtons.getButtonById(lastVoiceButton, 0).getBRI());

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

            // Create OCT slider
            octaveflg = true;
            sliderOCT = new Slider(-2.5, 2.5, 0);
            sliderOCT.setOrientation(Orientation.VERTICAL);
            sliderOCT.setShowTickLabels(true);
            sliderOCT.setShowTickMarks(true);
            sliderOCT.setMajorTickUnit(1);
            sliderOCT.setBlockIncrement(1);
            Rotate rotateOct = new Rotate();
            sliderOCT.valueProperty().addListener((observable, oldValue, newValue) -> {
                //Setting the angle for the rotation
                rotateOct.setAngle((double) newValue);

                midiButtons.getButtonById(lastVoiceButton, 0).setOctaveTran((int)sliderOCT.getValue());
                octaveflg = mididevices.setOctaveCHAN(lastVoiceChannel, (byte)sliderOCT.getValue());

                buttonSave.setDisable(false);
                flgDirtyPreset = true;      // Need to save updated Preset

                if (lastVoiceButton != null)
                    labelstatusOrg.setText(" Status: Button " + lastVoiceButton + " OCT= " + newValue.intValue());
            });
            // Only Update slider if no open notes!
            if (octaveflg) {
                sliderOCT.setValue(midiButtons.getButtonById(lastVoiceButton, 0).getOctaveTran());
            }
            else {
                labelstatusOrg.setText(" Status: Octave change only allowed with all notes off");
            }

            Label vollabel = new Label("VOL");
            vollabel.setStyle(styletextwhitesmall);
            Label revlabel = new Label("REV");
            revlabel.setStyle(styletextwhitesmall);
            Label cholabel = new Label("CHO");
            cholabel.setStyle(styletextwhitesmall);
            Label modlabel = new Label("MOD");
            modlabel.setStyle(styletextwhitesmall);
            Label brilabel = new Label("BRI");
            brilabel.setStyle(styletextwhitesmall);
            Label panlabel = new Label("PAN");
            panlabel.setStyle(styletextwhitesmall);
            Label octlabel = new Label("OCT");
            octlabel.setStyle(styletextwhitesmall);

            GridPane gridEffects = new GridPane();
            gridEffects.add(new VBox(vollabel, sliderVOL), 0, 1, 1, 1);
            gridEffects.add(new VBox(revlabel, sliderREV), 1, 1, 1, 1);
            gridEffects.add(new VBox(cholabel, sliderCHO), 2, 1, 1, 1);
            gridEffects.add(new VBox(modlabel, sliderMOD), 3, 1, 1, 1);
            gridEffects.add(new VBox(brilabel, sliderBRI), 4, 1, 1, 1);
            gridEffects.add(new VBox(panlabel, sliderPAN), 5, 1, 1, 1);
            gridEffects.add(new VBox(octlabel, sliderOCT), 6, 1, 1, 1);
            gridEffects.setHgap(2);
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
            labelstatusOrg.setMinWidth(700 * ymul);

            labelsynth = new Label("Module: " + sharedStatus.getModuleName(config.getSoundModuleIdx()));
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
                drum1.setVisible(false);
                drum2.setVisible(false);
                drum3.setVisible(false);
                drum4.setVisible(false);

                // Disable Organ Rotary Buttons in MIDI GM Mode
                lbutton15.setVisible(false);
                lbutton16.setVisible(false);
                rbutton17.setVisible(false);
                rbutton18.setVisible(false);
            }
            else
                drum1.fire();
                bass1.fire();
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

        if (!dopresets.loadMidiPresets(presetFile)) {
            labelstatusOrg.setText(" Status: Error loading preset file " + presetFile);
            labelstatusOrg.setStyle(styletextred);

            System.err.println("PerformScene Init: Error loading Preset file: " + presetFile);
            try {
                wait(10000);
            }
            catch(Exception exception) {}
        }
        System.out.println("PerformScene Init: Loaded new Preset file: " + presetFile);

        defaultChannelLayering();

        //System.out.println("OrganScene: New Song selected: Loaded new Preset file: " + presetFile);
    }

    void defaultChannelLayering() {

        // Reset Channel layering to default every time new Song is selected
        MidiDevices mididevices = MidiDevices.getInstance();
        mididevices.initlayerChannels();

        b1layerbtn.setStyle(lrpressedOn);

        l1layerbtn.setStyle(lrpressedOn);
        l2layerbtn.setStyle(lrpressedOff);

        r1layerbtn.setStyle(lrpressedOn);
        r2layerbtn.setStyle(lrpressedOff);
        r3layerbtn.setStyle(lrpressedOff);

        //System.out.println("OrganScene: New Song selected: Loaded new Preset file: " + presetFile);
    }

    void buttonPresetAction(int presetIdx) {

        try {

            // Reload Preset file if changed, e.g. in Preset Scene
            if (sharedStatus.getPresetReload() == true) {
                if (!dopresets.loadMidiPresets(presetFile)) {
                    labelstatusOrg.setText(" Status: Error loading preset file " + presetFile);
                    labelstatusOrg.setStyle(styletextred);

                    System.err.println("PerformScene Init: Error loading Preset file: " + presetFile);
                    try {
                        wait(10000);
                    }
                    catch(Exception exception) {}
                }
                System.out.println("PerformScene Init: Loaded new Preset file: " + presetFile);

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
            MidiPreset preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getUpper1CHAN());
            int buttonidx = midiButtons.lookupButtonIdx(rbutton11.getId());
            MidiButton midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            rbutton11.setText(preset.getPatchName());
            offAllUpper1Buttons();
            rbutton11.setStyle(rcolorOn);

            // Upper 2-1
            preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getUpper2CHAN());
            buttonidx = midiButtons.lookupButtonIdx(rbutton21.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            rbutton21.setText(preset.getPatchName());
            offAllUpper2Buttons();
            rbutton21.setStyle(rcolorOn);

            // Upper 3-1
            preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getUpper3CHAN());
            buttonidx = midiButtons.lookupButtonIdx(rbutton31.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            rbutton31.setText(preset.getPatchName());
            offAllUpper3Buttons();
            rbutton31.setStyle(rcolorOn);

            // Lower 1-1
            preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getLower1CHAN());
            buttonidx = midiButtons.lookupButtonIdx(lbutton11.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            lbutton11.setText(preset.getPatchName());
            offAllLower1Buttons();
            lbutton11.setStyle(lcolorOn);

            // Lower 2-1
            preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getLower2CHAN());
            buttonidx = midiButtons.lookupButtonIdx(lbutton21.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            lbutton21.setText(preset.getPatchName());
            offAllLower2Buttons();
            lbutton21.setStyle(lcolorOn);

            // Bass 1
            preset = dopresets.getPreset(presetIdx * 16  + sharedStatus.getBassCHAN());
            buttonidx = midiButtons.lookupButtonIdx(bass1.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            bass1.setText(preset.getPatchName());
            offAllBassButtons();
            bass1.setStyle(bcolorOn);

            // Drum 1
            preset = dopresets.getPreset(presetIdx * 16 + sharedStatus.getDrumCHAN());
            buttonidx = midiButtons.lookupButtonIdx(drum1.getId());
            midibutton = midiButtons.getMidiButton(buttonidx, 0);
            midiButtons.copyPresetToMidiButton(preset, midibutton);

            drum1.setText(preset.getPatchName());
            offAllDrumButtons();
            drum1.setStyle(dcolorOn);
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
        bass1.setStyle(bcolorOff);
        bpressed2 = false;
        bass2.setStyle(bcolorOff);
        bpressed3 = false;
        bass3.setStyle(bcolorOff);
        bpressed4 = false;
        bass4.setStyle(bcolorOff);
    }

    private void offAllDrumButtons() {
        dpressed1 = false;
        drum1.setStyle(dcolorOff);
        dpressed2 = false;
        drum2.setStyle(dcolorOff);
        dpressed3 = false;
        drum3.setStyle(dcolorOff);
        dpressed4 = false;
        drum4.setStyle(dcolorOff);
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

        // Default Brightness for now - until in Organ PRF file and on screen
        for (int chan = 0; chan < 15; chan++) {
            playmidifile.sendMidiProgramChange(chan, ccEXP, 127, 0);
            playmidifile.sendMidiProgramChange(chan, ccBRI, 64, 0);
            playmidifile.sendMidiProgramChange(chan, ccTIM, 64, 0);
            playmidifile.sendMidiProgramChange(chan, ccATK, 0, 0);
            playmidifile.sendMidiProgramChange(chan, ccREL, 0, 0);
        }

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

            patchid = midiButtons.getButtonById(bass1.getId(), 0).getPatchId();
            bass1.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(bass2.getId(), 0).getPatchId();
            bass2.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(bass3.getId(), 0).getPatchId();
            bass3.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(bass4.getId(), 0).getPatchId();
            bass4.setText(dopatches.getMIDIPatch(patchid).getPatchName());

            patchid = midiButtons.getButtonById(drum1.getId(), 0).getPatchId();
            drum1.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(drum2.getId(), 0).getPatchId();
            drum2.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(drum3.getId(), 0).getPatchId();
            drum3.setText(dopatches.getMIDIPatch(patchid).getPatchName());
            patchid = midiButtons.getButtonById(drum4.getId(), 0).getPatchId();
            drum4.setText(dopatches.getMIDIPatch(patchid).getPatchName());

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
