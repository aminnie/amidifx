package amidifx.scenes;

import amidifx.*;
import amidifx.models.*;
import amidifx.utils.ArduinoUtils;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;


public class OrganScene {

    // Button Colors
    // https://yagisanatode.com/2019/08/06/google-apps-script-hexadecimal-color-codes-for-google-docs-sheets-and-slides-standart-palette/
    final String bgpanecolor = "-fx-background-color: #999999; ";
    final String bgheadercolor = "-fx-background-color: #B2B5B1; ";
    final String bgfootercolor = "-fx-background-color: #B2B5B1;";

    final String rcolorOff = "-fx-background-color: #ea9999; -fx-font-size: 15; ";
    final String rcolorOn = "-fx-background-color: #e06666; -fx-font-size: 15; ";

    final String lcolorOff = "-fx-background-color: #ffe599; -fx-font-size: 15; ";
    final String lcolorOn = "-fx-background-color: #f1c232; -fx-font-size: 15; ";

    final String dcolorOff = "-fx-background-color: #a2c4c9; -fx-font-size: 15; ";
    final String dcolorOn = "-fx-background-color: #76a5af; -fx-font-size: 15; ";

    final String bcolorOff = "-fx-background-color: #a4c2f4; -fx-font-size: 15; ";
    final String bcolorOn = "-fx-background-color: #6d9eeb; -fx-font-size: 15; ";

    final String pcolorOff = "-fx-background-color: #DBD06B; -fx-font-size: 15; ";
    final String pcolorOn = "-fx-background-color: #C3B643; -fx-font-size: 15; ";

    final String orgcolorOff = "-fx-background-color: #b7b7b7; -fx-font-size: 15; ";
    final String orgcolorOn = "-fx-background-color: #f3f3f3; -fx-font-size: 15; ";

    final String selectcolorOff = "-fx-background-color: #69a8cc; -fx-font-size: 15; ";
    final String selectcolorOn = "-fx-background-color: #4493C0; -fx-font-size: 15; ";

    static final String btnMenuOn = "-fx-background-color: #4493C0; -fx-font-size: 15; ";
    static final String btnMenuOff = "-fx-background-color: #69A8CC; -fx-font-size: 15; ";
    static final String btnMenuSaveOn = "-fx-background-color: #DB6B6B; -fx-font-size: 15; ";
    static final String btnMenuSaveOff = "-fx-background-color: #B2B5B1; -fx-font-size: 15; ";

    static final String btnplayOff = "-fx-background-color: #8ED072; -fx-font-size: 15; ";
    static final String btnplayOn = "-fx-background-color: #DB6B6B; -fx-font-size: 15; ";

    final String styletext = "-fx-font-size: 15; ";

    Stage primaryStage;
    Scene returnScene;

    SharedStatus sharedStatus;
    PlayMidi playmidifile;
    ArduinoUtils arduinoUtils;

    // Main pane for the Organ scene
    private BorderPane paneWelcome;
    Scene sceneOrgan;

    MidiPresets dopresets;
    MidiSongs dosongs;
    MidiPatches dopatches;
    MidiModules midimodules;
    MidiButtons midiButtons;

    File fstyle;

    String songTitle;
    String songFile;
    String buttonFile = "perform.csv";
    private static final String MID_DIRECTORY = "C:/amidifx/midifiles/";

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

    String lastVoiceButton;
    int lastVoiceChannel;

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

    boolean lpressed11 = false;
    boolean lpressed12 = false;
    boolean lpressed13 = false;
    boolean lpressed14 = false;

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

    Slider sliderVOL;
    Slider sliderEXP;
    Slider sliderREV;
    Slider sliderCHO;
    Slider sliderMOD;
    Slider sliderPAN;
    Slider sliderTRE;

    // https://professionalcomposers.com/midi-cc-list/
    public static byte ccVOL = 7;
    public static byte ccEXP = 11;
    public static byte ccREV = 91;
    public static byte ccTRE = 92;
    public static byte ccCHO = 93;
    public static byte ccMOD = 1;
    public static byte ccPAN = 10;


    /*********************************************************
     * Creates a Organ Scene.
     *********************************************************/

    public OrganScene(Stage primaryStage, Scene returnScene) {

        System.out.println("OrganScene: AMIDIFX Organ Scene Starting");

        try {

            // Create instance of Shared Status to report back to Scenes
            sharedStatus = SharedStatus.getInstance();

            // Prepare MIDI Player and Select Deebach Blackbox as output
            // Prepare the Channel Program and Effects tracking list
            playmidifile = PlayMidi.getInstance();
            playmidifile.initCurPresetList();

            // Get instance of Arduino Utilities
            arduinoUtils = ArduinoUtils.getInstance();

            // Load MIDI Default MIDI Preset file on start up
            dopresets = new MidiPresets();
            String presetFile = sharedStatus.getPresetFile();
            dopresets.makeMidiPresets(presetFile);
            System.out.println("OrganScene Init: Loaded new Preset file: " + presetFile);

            // Load Song List
            dosongs = new MidiSongs();
            dosongs.makeMidiSongs();
            songTitle = dosongs.getSong(0).getSongTitle();
            System.out.println("OrganScene Init: Song Title: " + songTitle);

            // Load MIDI Patch files on start up based on detected and preferred sound module
            // Load MIDI Sound Module List on start up
            midimodules = new MidiModules();

            dopatches = new MidiPatches();
            int moduleidx = sharedStatus.getModuleidx();
            String modulefile = midimodules.getModuleFile(moduleidx);
            if (!dopatches.fileExist(modulefile)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("AMIDIFX Startup Error");
                alert.setHeaderText("Module Patch file " + MID_DIRECTORY + modulefile + " not found!");
                Optional<ButtonType> result = alert.showAndWait();

                System.exit(-1);
            }
            dopatches.loadMidiPatches(modulefile);

            // Preset the Sound Bank and Voice Selection Lists on startup
            bankname = dopatches.getMidiBanks().getMidiBank(bankidx).getBankName();
            buttonSoundBank.setText(bankname);
            fontname = dopatches.getMIDIPatch(patchidx).getPatchName();
            buttonSoundFont.setText(fontname);

            // Prepare Voice Buttons Mappings by loading either Deebach or MidiGM Organ Files
            MidiButtons midiButtons = new MidiButtons();

            if (sharedStatus.getPerformFile() != null) {
                buttonFile = sharedStatus.getPerformFile();
            }
            else {
                System.err.println("### OrganScene Error: Organ file not found. Selected default: " + buttonFile);
            }
            midiButtons.makeMidiButtons(buttonFile);

            // Start Building the Scene

            Parent root = FXMLLoader.load(getClass().getResource("../amidifx.fxml"));
            //System.out.println("OrganScene root: " + root.toString());

            File fstyle = new File("src/amidifx/style.css");
            //System.out.println("OrganScene fstyle: " + fstyle.toString());

            System.out.println("OrganScene: Scene OrganScene!");

            BorderPane borderPaneOrg = new BorderPane();
            borderPaneOrg.setStyle(bgpanecolor);

            sceneOrgan = new Scene(borderPaneOrg, 1024, 600);
            sceneOrgan.getStylesheets().clear();
            sceneOrgan.getStylesheets().add("file:///" + fstyle.getAbsolutePath().replace("\\", "/"));

            sharedStatus.setOrganScene(sceneOrgan);

            labelstatusOrg = new Label(" Status: Ready");
            Label labelsongtitle = new Label("");
            Label labelstatus = new Label("");

            // Create top bar navigation buttons

            Button buttonsc1 = new Button("Perform");
            buttonsc1.setStyle(btnMenuOn);
            buttonsc1.setOnAction(e -> {
                //System.out.println(("OrganScene: Changing to Organ Scene " + sharedStatus.getOrganScene().toString()));
                primaryStage.setScene(sharedStatus.getOrganScene());
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.err.println("### OrganScene: Unable to set Organ Scene!");
                }
            });

            Button buttonsc2 = new Button("Songs");
            buttonsc2.setStyle(btnMenuOff);
            buttonsc2.setOnAction(e -> {
                //System.out.println(("OrganScene: Changing to Songs Scene " + sharedStatus.getSongsScene().toString()));
                primaryStage.setScene(sharedStatus.getSongsScene());
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.err.println("### OrganScene: Unable to set Songs Scene!");
                }
            });

            Button buttonsc3 = new Button("Presets");
            buttonsc3.setStyle(btnMenuOff);
            buttonsc3.setDisable(true);
            buttonsc3.setOnAction(e -> {
                //System.out.println(("OrganScene: Changing to Presets Scene " + sharedStatus.getPresetsScene().toString()));
                primaryStage.setScene(sharedStatus.getPresetsScene());
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.err.println("### OrganScene: Unable to set Presets Scene!");
                }
            });

            // Save Presets Button
            buttonSave = new Button("Save Voices");
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
                arduinoUtils.closePort();

                Platform.exit();
            });

            ToolBar toolbarLeft = new ToolBar(buttonsc1, buttonsc2, buttonsc3);
            toolbarLeft.setStyle(bgheadercolor);
            toolbarLeft.setMinWidth(225);

            Label lbltitle1 = new Label("AMIDIFX Sound Module Controller");
            lbltitle1.setStyle(styletext);
            HBox hboxTitle = new HBox();
            hboxTitle.setPadding(new Insets(10, 10, 10,200));
            hboxTitle.getChildren().add(lbltitle1);

            ToolBar toolbarRight = new ToolBar(buttonSave, buttonPanic, buttonExit);
            toolbarRight.setStyle(bgheadercolor);
            toolbarRight.setMinWidth(150);

            BorderPane borderPaneTop = new BorderPane();
            borderPaneTop.setStyle(bgheadercolor);

            // Assemble the Menu Bar Border Pane
            borderPaneTop.setLeft(toolbarLeft);
            borderPaneTop.setCenter(hboxTitle);
            borderPaneTop.setRight(toolbarRight);

            // Build the Song Title Selection Controls

            Button buttonSongLoad = new Button(songTitle);
            buttonSongLoad.setStyle(selectcolorOff);
            buttonSongLoad.setPrefSize(200, 25);
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

                // Enable Song Play Button
                btnplay.setDisable(false);

                labelstatusOrg.setText(" Status: Loaded Presets for " + dosongs.getSong(idxSongList).getSongTitle());
                //System.out.println("OrganScene: Loaded Presets " + dosongs.getSong(idxSongList).getPresetFile());
            });

            Button buttonSongNameLeft = new Button("<<");
            buttonSongNameLeft.setStyle(selectcolorOff);
            buttonSongNameLeft.setPrefSize(50, 25);
            buttonSongNameLeft.setOnAction(e -> {
                if (idxSongList > 0) --idxSongList;
                songTitle = dosongs.getSong(idxSongList).getSongTitle();
                songFile = dosongs.getSong(idxSongList).getMidiFile();
                buttonSongLoad.setText(songTitle);

                // New Song to be selected for Play
                buttonSongLoad.setStyle(selectcolorOff);
                btnplay.setDisable(true);

                offAllPresetButtons();

                //System.out.println("OrganScene: Previous Song " + songTitle);
            });

            Button buttonSongNameRight = new Button(">>");
            buttonSongNameRight.setStyle(selectcolorOff);
            buttonSongNameRight.setPrefSize(50, 25);
            buttonSongNameRight.setOnAction(e -> {
                if (idxSongList < (dosongs.getSongListSize() - 1)) idxSongList++;
                songTitle = dosongs.getSong(idxSongList).getSongTitle();
                songFile = dosongs.getSong(idxSongList).getMidiFile();
                buttonSongLoad.setText(songTitle);

                // New Song selected
                buttonSongLoad.setStyle(selectcolorOff);
                btnplay.setDisable(true);

                offAllPresetButtons();

                //System.out.println("OrganScene: Next Song " + songTitle);
            });

            // Assemble the Song Navigation Controls
            HBox hboxSong = new HBox();
            hboxSong.getChildren().add(buttonSongNameLeft);
            hboxSong.getChildren().add(buttonSongLoad);
            hboxSong.getChildren().add(buttonSongNameRight);

            FlowPane flowSong = new FlowPane();
            flowSong.setHgap(10);
            flowSong.setVgap(10);
            flowSong.getChildren().add(hboxSong);

            // Build the Sound Bank Selection Controls

            buttonSoundBank.setPrefSize(200, 25);
            buttonSoundBank.setStyle(selectcolorOff);
            buttonSoundBank.setAlignment(Pos.CENTER);
            buttonSoundBank.setOnAction(e -> {
                // Load the start Patch IDx for the selected Bank
                patchidx = dopatches.getMidiBanks().getMidiBank(bankidx).getPatchIdx();
                bankpatchidx = patchidx;

                // Preset the Sound Font (Voice) with the start PatchName
                fontname = dopatches.getMIDIPatch(patchidx).getPatchName();
                buttonSoundFont.setText(fontname);

                buttonSoundBank.setStyle(selectcolorOn);
                //bnewpatchselected = true;
            });

            Button buttonSoundBankLeft = new Button("<<");
            buttonSoundBankLeft.setStyle(selectcolorOff);
            buttonSoundBankLeft.setPrefSize(50, 25);
            buttonSoundBankLeft.setOnAction(e -> {
                if (bankidx > 0) --bankidx;
                bankname = dopatches.getMidiBanks().getMidiBank(bankidx).getBankName();
                buttonSoundBank.setText(bankname);

                buttonSoundBank.setStyle(selectcolorOff);
                buttonSoundFont.setStyle(selectcolorOff);
                bnewpatchselected = false;

                //System.out.println("OrganScene: Previous Bank " + bankname);
            });

            Button buttonSoundBankRight = new Button(">>");
            buttonSoundBankRight.setStyle(selectcolorOff);
            buttonSoundBankRight.setPrefSize(50, 25);
            buttonSoundBankRight.setOnAction(e -> {
                if (bankidx < dopatches.getMidiBanks().getMidiBankSize() -1 ) bankidx++;
                bankname = dopatches.getMidiBanks().getMidiBank(bankidx).getBankName();
                buttonSoundBank.setText(bankname);

                buttonSoundBank.setStyle(selectcolorOff);
                buttonSoundFont.setStyle(selectcolorOff);
                bnewpatchselected = false;

                //System.out.println("OrganScene: Previous Bank " + bankname);
            });

            // Assemble the Song Navigation Controls
            HBox hboxBank = new HBox();
            hboxBank.getChildren().add(buttonSoundBankLeft);
            hboxBank.getChildren().add(buttonSoundBank);
            hboxBank.getChildren().add(buttonSoundBankRight);

            FlowPane flowBank = new FlowPane();
            flowBank.setHgap(10);
            flowBank.setVgap(10);
            flowBank.getChildren().add(hboxBank);

            // Build the Sound Voice Selection Controls

            buttonSoundFont.setPrefSize(200, 25);
            buttonSoundFont.setStyle(selectcolorOff);
            buttonSoundFont.setAlignment(Pos.CENTER);
            buttonSoundFont.setOnAction(e -> {
                //buttonPresetLoad(dosongs.getSong(songidx).getPresetFile());

                buttonSoundFont.setStyle(selectcolorOn);
                labelstatusOrg.setText(" Status: Loaded Voice for " + fontname + ". Press Voice Button to Add.");

                bnewpatchselected = true;

                //System.out.println("OrganScene: Loaded Voice " + fontname);
            });

            Button buttonSoundFontLeft = new Button("<<");
            buttonSoundFontLeft.setStyle(selectcolorOff);
            buttonSoundFontLeft.setPrefSize(50, 25);
            buttonSoundFontLeft.setOnAction(e -> {
                if (patchidx > bankpatchidx) --patchidx;
                fontname = dopatches.getMIDIPatch(patchidx).getPatchName();
                buttonSoundFont.setText(fontname);

                ///// Get the Patch index so we can test voice
                ////patchidx = dopatches.getMIDIPatch(patchidx).getPatchId();

                //System.out.println("OrganScene: Previous Voice " + fontname);
            });

            Button buttonSoundFontRight = new Button(">>");
            buttonSoundFontRight.setStyle(selectcolorOff);
            buttonSoundFontRight.setPrefSize(50, 25);
            buttonSoundFontRight.setOnAction(e -> {
                if (patchidx < dopatches.getMIDIPatchSize() - 1) ++patchidx;
                fontname = dopatches.getMIDIPatch(patchidx).getPatchName();
                buttonSoundFont.setText(fontname);

                ///// Get the Patch index so we can test voice
                ////patchidx = dopatches.getMIDIPatch(patchidx).getPatchId();

                //System.out.println("OrganScene: Next Voice " + fontname);
            });

            // Assemble the Sound Font Navigation Controls
            HBox hboxFont = new HBox();
            hboxFont.getChildren().add(buttonSoundFontLeft);
            hboxFont.getChildren().add(buttonSoundFont);
            hboxFont.getChildren().add(buttonSoundFontRight);

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


            // *** Start Bottom Status Bar

            BorderPane borderStatusOrg = new BorderPane();
            borderStatusOrg.setStyle(bgpanecolor);

            labelstatusOrg.setText(" Status: " + sharedStatus.getStatusText());
            labelsongtitle.setText("Song: " + sharedStatus.getSongTitle());

            FlowPane panefilesOrg = new FlowPane();
            panefilesOrg.setHgap(20);
            panefilesOrg.getChildren().add(labelsongtitle);

            VBox vboxstatusLeftOrg = new VBox();
            vboxstatusLeftOrg.setMinWidth(400);
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
            btnpreset1.setPrefSize(115, 50);

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
            btnpreset2.setPrefSize(115, 50);

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
            btnpreset3.setPrefSize(115, 50);

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
            btnpreset4.setPrefSize(115, 50);

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
            btnpreset5.setPrefSize(115, 50);

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
            btnpreset6.setPrefSize(115, 50);

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
            btnpreset7.setPrefSize(115, 50);

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
            btnpreset8.setPrefSize(115, 50);

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

            GridPane gridmidcenterOrgan = new GridPane();
            gridmidcenterOrgan.setHgap(15);
            gridmidcenterOrgan.setVgap(10);

            Label blabel1 = new Label("Bass [11]");
            blabel1.setStyle("-fx-font-size:15px;");

            bleft1 = new Button(" Bass 1");
            bleft1.setId("B1-1");
            bleft1.setMinSize(120, 50);
            bleft1.setStyle(bcolorOff);
            bleft1.setWrapText(true);
            bleft1.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    bleft1.setStyle(bcolorOn);
                }
                else {
                    bleft1.setStyle(bcolorOff);
                }
                bpressed1 = !bpressed1;

                labelstatusOrg.setText(" Status: Applied Bass 1");
            });

            bleft2.setText(" Bass 2");
            bleft2.setMinSize(120, 50);
            bleft2.setId("B1-2");
            bleft2.setStyle(bcolorOff);
            bleft2.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    bleft2.setStyle(bcolorOn);
                }
                else {
                    bleft2.setStyle(bcolorOff);
                }
                bpressed2 = !bpressed2;

                labelstatusOrg.setText(" Status: Applied Bass 2");
            });

            bleft3.setText(" Bass 3");
            bleft3.setMinSize(120, 50);
            bleft3.setId("B1-3");
            bleft3.setStyle(bcolorOff);
            bleft3.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    bleft3.setStyle(bcolorOn);
                }
                else {
                    bleft3.setStyle(bcolorOff);
                }
                bpressed3 = !bpressed3;

                labelstatusOrg.setText(" Status: Applied Bass 3");
            });

            bleft4.setText(" Bass 4");
            bleft4.setId("B1-4");
            bleft4.setMinSize(120, 50);
            bleft4.setStyle(bcolorOff);
            bleft4.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    bleft4.setStyle(bcolorOn);
                }
                else {
                    bleft4.setStyle(bcolorOff);
                }
                bpressed4 = !bpressed4;

                labelstatusOrg.setText(" Status: Applied Bass 4");
            });

            gridmidcenterOrgan.add(blabel1, 0, 0, 1, 1);
            gridmidcenterOrgan.add(bleft1, 0, 1, 1, 1);
            gridmidcenterOrgan.add(bleft2, 1, 1, 1, 1);
            gridmidcenterOrgan.add(bleft3, 0, 2, 1, 1);
            gridmidcenterOrgan.add(bleft4, 1, 2, 1, 1);

            Label dlabel1 = new Label("Drums [10]");
            dlabel1.setStyle("-fx-font-size:15px;");

            // Do Beat Counter in large font
            Label lblbeatcount = new Label("Bar: 0.0");
            lblbeatcount.setStyle("-fx-font-size:15px; -fx-color:crimson");

            dleft1.setText(" Drums 1");
            dleft1.setId("D1-1");
            dleft1.setMinSize(120, 50);
            dleft1.setStyle(dcolorOff);
            dleft1.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    dleft1.setStyle(dcolorOn);
                }
                else {
                    dleft1.setStyle(dcolorOff);
                }
                dpressed1 = !dpressed1;

                labelstatusOrg.setText(" Status: Applied Drum 1");
            });

            dleft2.setText(" Drums 2");
            dleft2.setId("D1-2");
            dleft2.setMinSize(120, 50);
            dleft2.setStyle(dcolorOff);
            dleft2.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    dleft2.setStyle(dcolorOn);
                }
                else {
                    dleft2.setStyle(dcolorOff);
                }
                dpressed2 = !dpressed2;

                labelstatusOrg.setText(" Status: Applied Drum 2");
            });

            dleft3.setText(" Drums 3");
            dleft3.setId("D1-3");
            dleft3.setMinSize(120, 50);
            dleft3.setStyle(dcolorOff);
            dleft3.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    dleft3.setStyle(dcolorOn);
                }
                else {
                    dleft3.setStyle(dcolorOff);
                }
                dpressed3 = !dpressed3;

                labelstatusOrg.setText(" Status: Applied Drum 3");
            });

            dleft4.setText(" Drums 4");
            dleft4.setId("D1-4");
            dleft4.setMinSize(120, 50);
            dleft4.setStyle(dcolorOff);
            dleft4.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    dleft4.setStyle(dcolorOn);
                }
                else {
                    dleft4.setStyle(dcolorOff);
                }
                dpressed4 = !dpressed4;

                labelstatusOrg.setText(" Status: Applied Drum 4");
            });

            gridmidcenterOrgan.add(dlabel1, 0, 3, 1, 1);
            gridmidcenterOrgan.add(lblbeatcount, 1, 3, 1, 1);
            gridmidcenterOrgan.add(dleft1, 0, 4, 1, 1);
            gridmidcenterOrgan.add(dleft2, 1, 4, 1, 1);
            gridmidcenterOrgan.add(dleft3, 0, 5, 1, 1);
            gridmidcenterOrgan.add(dleft4, 1, 5, 1, 1);

            Label llabel1 = new Label("Lower 1 [12]");
            llabel1.setStyle("-fx-font-size:15px;");
            Label llabel2 = new Label("Lower 2 [13]");
            llabel2.setStyle("-fx-font-size:15px;");

            lbutton11.setText(" Lower 1-1");
            lbutton11.setId("L1-1");
            lbutton11.setMinSize(120, 50);
            lbutton11.setStyle(lcolorOff);
            lbutton11.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton11.setStyle(lcolorOn);
                }
                else {
                    lbutton11.setStyle(lcolorOff);
                }
                lpressed11 = !lpressed11;

                labelstatusOrg.setText(" Status: Applied Lower 1-1");
            });

            lbutton12.setText(" Lower 1-2");
            lbutton12.setId("L1-2");
            lbutton12.setMinSize(120, 50);
            lbutton12.setStyle(lcolorOff);
            lbutton12.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton12.setStyle(lcolorOn);
                }
                else {
                    lbutton12.setStyle(lcolorOff);
                }
                lpressed12 = !lpressed12;

                labelstatusOrg.setText(" Status: Applied Lower 1-2");
            });

            lbutton13.setText(" Lower 1-3");
            lbutton13.setId("L1-3");
            lbutton13.setMinSize(120, 50);
            lbutton13.setStyle(lcolorOff);
            lbutton13.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton13.setStyle(lcolorOn);
                }
                else {
                    lbutton13.setStyle(lcolorOff);
                }
                lpressed13 = !lpressed13;

                labelstatusOrg.setText(" Status: Applied Lower 1-3");
            });

            lbutton14.setText(" Lower 1-4");
            lbutton14.setId("L1-4");
            lbutton14.setMinSize(120, 50);
            lbutton14.setStyle(lcolorOff);
            lbutton14.setWrapText(true);
            lbutton14.setOnAction(event -> {
                offAllLower1Buttons();
                int buttonidx = midiButtons.lookupButtonIdx(lbutton13.getId());

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
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getLower1CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton14.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton14.setStyle(lcolorOn);
                }
                else {
                    lbutton14.setStyle(lcolorOff);
                }
                lpressed14 = !lpressed14;

                labelstatusOrg.setText(" Status: Applied Lower 1-4");
            });

            lbutton21 = new Button(" Lower 2-1");
            lbutton21.setId("L2-1");
            lbutton21.setMinSize(120, 50);
            lbutton21.setStyle(lcolorOff);
            lbutton21.setWrapText(true);
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
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getLower2CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton21.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton21.setStyle(lcolorOn);
                }
                else {
                    lbutton21.setStyle(lcolorOff);
                }
                lpressed21 = !lpressed21;

                labelstatusOrg.setText(" Status: Applied Lower 2-1");
            });

            lbutton22 = new Button(" Lower 2-2");
            lbutton22.setId("L2-2");
            lbutton22.setMinSize(120, 50);
            lbutton22.setStyle(lcolorOff);
            lbutton22.setWrapText(true);
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
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getLower2CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton22.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton22.setStyle(lcolorOn);
                }
                else {
                    lbutton22.setStyle(lcolorOff);
                }
                lpressed22 = !lpressed22;

                labelstatusOrg.setText(" Status: Applied Lower 2-2");
            });

            lbutton23 = new Button(" Lower 2-3");
            lbutton23.setId("L2-3");
            lbutton23.setMinSize(120, 50);
            lbutton23.setStyle(lcolorOff);
            lbutton23.setWrapText(true);
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
                    // Apply MIDI Program Change on Upper Channel for Button Press
                    int CHAN = sharedStatus.getLower2CHAN();
                    applyMidiButton(buttonidx, CHAN, midiButtons.getMidiButton(buttonidx, 0));

                    MidiButton midibutton = midiButtons.getButtonById(lbutton23.getId(), 0);
                    sliderVOL.setValue(midibutton.getVOL());
                    sliderREV.setValue(midibutton.getREV());
                    sliderCHO.setValue(midibutton.getCHO());
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton23.setStyle(lcolorOn);
                }
                else {
                    lbutton23.setStyle(lcolorOff);
                }
                lpressed23 = !lpressed23;

                labelstatusOrg.setText(" Status: Applied Lower 2-3");
            });

            lbutton24 = new Button(" Lower 2-4");
            lbutton24.setId("L2-4");
            lbutton24.setMinSize(120, 50);
            lbutton24.setStyle(lcolorOff);
            lbutton24.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    lbutton24.setStyle(lcolorOn);
                }
                else {
                    lbutton24.setStyle(lcolorOff);
                }
                lpressed24 = !lpressed24;

                labelstatusOrg.setText(" Status: Applied Lower 2-4");
            });

            gridmidcenterOrgan.add(llabel1, 3, 0, 1, 1);
            gridmidcenterOrgan.add(llabel2, 4, 0, 1, 1);

            gridmidcenterOrgan.add(lbutton11, 3, 1, 1, 1);
            gridmidcenterOrgan.add(lbutton12, 3, 2, 1, 1);
            gridmidcenterOrgan.add(lbutton13, 3, 3, 1, 1);
            gridmidcenterOrgan.add(lbutton14, 3, 4, 1, 1);
            gridmidcenterOrgan.add(lbutton21, 4, 1, 1, 1);
            gridmidcenterOrgan.add(lbutton22, 4, 2, 1, 1);
            gridmidcenterOrgan.add(lbutton23, 4, 3, 1, 1);
            gridmidcenterOrgan.add(lbutton24, 4, 4, 1, 1);

            Label rlabel1 = new Label("Upper 1 [14]");
            rlabel1.setStyle("-fx-font-size:15px;");
            Label rlabel2 = new Label("Upper 2 [15]");
            rlabel2.setStyle("-fx-font-size:15px;");
            Label rlabel3 = new Label("Upper 3 [16]");
            rlabel3.setStyle("-fx-font-size:15px;");

            rbutton11.setText(" Upper 1-1");
            rbutton11.setId("U1-1");
            rbutton11.setMinSize(120, 50);
            rbutton11.setStyle(rcolorOff);
            rbutton11.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton11.setStyle(rcolorOn);
                }
                else {
                    rbutton11.setStyle(rcolorOff);
                }
                rpressed11 = !rpressed11;

                labelstatusOrg.setText(" Status: Applied Upper 1-1");
            });

            rbutton12.setText(" Upper 1-2");
            rbutton12.setId("U1-2");
            rbutton12.setMinSize(120, 50);
            rbutton12.setStyle(rcolorOff);
            rbutton12.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton12.setStyle(rcolorOn);
                }
                else {
                    rbutton12.setStyle(rcolorOff);
                }
                rpressed12 = !rpressed12;

                labelstatusOrg.setText(" Status: Applied Upper 1-2");
            });

            rbutton13.setText(" Upper 1-3");
            rbutton13.setId("U1-3");
            rbutton13.setMinSize(120, 50);
            rbutton13.setStyle(rcolorOff);
            rbutton13.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton13.setStyle(rcolorOn);
                }
                else {
                    rbutton13.setStyle(rcolorOff);
                }
                rpressed13 = !rpressed13;

                labelstatusOrg.setText(" Status: Applied Upper 1-3");
            });

            rbutton14.setText(" Upper 1-4");
            rbutton14.setId("U1-4");
            rbutton14.setMinSize(120, 50);
            rbutton14.setStyle(rcolorOff);
            rbutton14.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton14.setStyle(rcolorOn);
                }
                else {
                    rbutton14.setStyle(rcolorOff);
                }
                rpressed14 = !rpressed14;

                lastVoiceButton = rbutton14.getId();
                lastVoiceChannel = midiButtons.getButtonChannel(rbutton14.getId());

                labelstatusOrg.setText(" Status: Applied Upper 1-4");
            });

            rbutton15.setText(" Upper 1-5");
            rbutton15.setId("U1-5");
            rbutton15.setMinSize(120, 50);
            rbutton15.setStyle(rcolorOff);
            rbutton15.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton15.setStyle(rcolorOn);
                }
                else {
                    rbutton15.setStyle(rcolorOff);
                }
                rpressed15 = !rpressed15;

                labelstatusOrg.setText(" Status: Applied Upper 1-5");
            });

            // Organ Button + Rotary On/Off and Brake
            rbutton16.setText(" Upper 1-6");
            rbutton16.setId("U1-6");
            rbutton16.setMinSize(120, 50);
            rbutton16.setStyle(orgcolorOff);
            rbutton16.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton16.setStyle(orgcolorOn);
                }
                else {
                    rbutton16.setStyle(orgcolorOff);
                }
                rpressed16 = !rpressed16;

                labelstatusOrg.setText(" Status: Applied Upper 1-6");
            });

            rbutton17.setText("Rotary Brake");
            rbutton17.setId("U1-7");
            rbutton17.setMinSize(120, 50);
            rbutton17.setStyle(orgcolorOff);
            rbutton17.setWrapText(true);
            rbutton17.setOnAction(event -> {
                //buttonPresetAction(6);

                labelstatusOrg.setText(" Status: Rotary On/Off");
                if (!rpressed17) {
                    rbutton17.setStyle(orgcolorOn);

                    labelstatusOrg.setText(" Status: Rotary Brake On");
                } else {
                    rbutton17.setStyle(orgcolorOff);

                    labelstatusOrg.setText(" Status: Rotary Brake Off");
                }
                rpressed17 = !rpressed17;
            });

            rbutton18.setText(" Rotary On");
            rbutton18.setId("U1-8");
            rbutton18.setMinSize(120, 50);
            rbutton18.setStyle(orgcolorOff);
            rbutton18.setWrapText(true);
            rbutton18.setOnAction(event -> {
                //buttonPresetAction(6);

                labelstatusOrg.setText(" Status: Rotary On/Off");
                if (!rpressed18) {
                    rbutton18.setStyle(orgcolorOn);

                    labelstatusOrg.setText(" Status: Rotary On");
                } else {
                    rbutton18.setStyle(orgcolorOff);

                    labelstatusOrg.setText(" Status: Rotary Off");
                }
                rpressed18 = !rpressed18;
            });

            // Upper 2 CHAN 15 + 16 Buttons
            rbutton21.setText(" Upper 2-1");
            rbutton21.setId("U2-1");
            rbutton21.setMinSize(120, 50);
            rbutton21.setStyle(rcolorOff);
            rbutton21.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton21.setStyle(rcolorOn);
                }
                else {
                    rbutton21.setStyle(rcolorOff);
                }
                rpressed21 = !rpressed21;

                labelstatusOrg.setText(" Status: Applied Upper 2-1");
            });

            rbutton22.setText(" Upper 2-2");
            rbutton22.setId("U2-2");
            rbutton22.setMinSize(120, 50);
            rbutton22.setStyle(rcolorOff);
            rbutton22.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton22.setStyle(rcolorOn);
                }
                else {
                    rbutton22.setStyle(rcolorOff);
                }
                rpressed22 = !rpressed22;

                labelstatusOrg.setText(" Status: Applied Upper 2-2");
            });

            rbutton23.setText(" Upper 2-3");
            rbutton23.setId("U2-3");
            rbutton23.setMinSize(120, 50);
            rbutton23.setStyle(rcolorOff);
            rbutton23.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton23.setStyle(rcolorOn);
                }
                else {
                    rbutton23.setStyle(rcolorOff);
                }
                rpressed23 = !rpressed23;

                labelstatusOrg.setText(" Status: Applied Upper 2-3");
            });

            rbutton24.setText(" Upper 2-4");
            rbutton24.setId("U2-4");
            rbutton24.setMinSize(120, 50);
            rbutton24.setStyle(rcolorOff);
            rbutton24.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton24.setStyle(rcolorOn);
                }
                else {
                    rbutton24.setStyle(rcolorOff);
                }
                rpressed24 = !rpressed24;

                labelstatusOrg.setText(" Status: Applied Upper 2-4");
            });

            rbutton31.setText(" Upper 3-1");
            rbutton31.setId("U3-1");
            rbutton31.setMinSize(120, 50);
            rbutton31.setStyle(rcolorOff);
            rbutton31.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton31.setStyle(rcolorOn);
                }
                else {
                    rbutton31.setStyle(rcolorOff);
                }
                rpressed31 = !rpressed31;

                labelstatusOrg.setText(" Status: Applied Upper 3-1");
            });

            rbutton32.setText(" Upper 3-2");
            rbutton32.setId("U3-2");
            rbutton32.setMinSize(120, 50);
            rbutton32.setStyle(rcolorOff);
            rbutton32.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton32.setStyle(rcolorOn);
                }
                else {
                    rbutton32.setStyle(rcolorOff);
                }
                rpressed32 = !rpressed32;

                labelstatusOrg.setText(" Status: Applied Upper 3-2");
            });

            rbutton33.setText(" Upper 3-3");
            rbutton33.setId("U3-3");
            rbutton33.setMinSize(120, 50);
            rbutton33.setStyle(rcolorOff);
            rbutton33.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton33.setStyle(rcolorOn);
                }
                else {
                    rbutton33.setStyle(rcolorOff);
                }
                rpressed33 = !rpressed33;

                labelstatusOrg.setText(" Status: Applied Upper 3-3");
            });

            rbutton34.setText(" Upper 3-4");
            rbutton34.setId("U3-4");
            rbutton34.setMinSize(120, 50);
            rbutton34.setStyle(rcolorOff);
            rbutton34.setWrapText(true);
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
                    sliderPAN.setValue(midibutton.getPAN());

                    rbutton34.setStyle(rcolorOn);
                }
                else {
                    rbutton34.setStyle(rcolorOff);
                }
                rpressed34 = !rpressed34;

                labelstatusOrg.setText(" Status: Applied Upper 3-4");
            });

            gridmidcenterOrgan.add(rlabel1, 6, 0, 1, 1);
            gridmidcenterOrgan.add(rlabel2, 7, 0, 1, 1);
            gridmidcenterOrgan.add(rlabel3, 8, 0, 1, 1);

            gridmidcenterOrgan.add(rbutton11, 6, 1, 1, 1);
            gridmidcenterOrgan.add(rbutton12, 6, 2, 1, 1);
            gridmidcenterOrgan.add(rbutton13, 6, 3, 1, 1);
            gridmidcenterOrgan.add(rbutton14, 6, 4, 1, 1);
            gridmidcenterOrgan.add(rbutton15, 6, 5, 1, 1);

            gridmidcenterOrgan.add(rbutton16, 6, 6, 1, 1);
            gridmidcenterOrgan.add(rbutton17, 7, 6, 1, 1);
            gridmidcenterOrgan.add(rbutton18, 8, 6, 1, 1);

            gridmidcenterOrgan.add(rbutton21, 7, 1, 1, 1);
            gridmidcenterOrgan.add(rbutton22, 7, 2, 1, 1);
            gridmidcenterOrgan.add(rbutton23, 7, 3, 1, 1);
            gridmidcenterOrgan.add(rbutton24, 7, 4, 1, 1);
            gridmidcenterOrgan.add(rbutton31, 8, 1, 1, 1);
            gridmidcenterOrgan.add(rbutton32, 8, 2, 1, 1);
            gridmidcenterOrgan.add(rbutton33, 8, 3, 1, 1);
            gridmidcenterOrgan.add(rbutton34, 8, 4, 1, 1);

            // Assemble MIDI Play Buttons

            btnplay = new Button("Play Song");
            btnplay.setDisable(true);
            btnplay.setStyle(btnplayOff);
            btnplay.setMinSize(120, 50);
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
                        if (!playmidifile.startMidiPlay(dosongs.getSong(idxSongList), dopresets, 3)) {
                            labelstatusOrg.setText(" Status: " + sharedStatus.getStatusText());
                        }
                        else {

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
                                            });
                                        songPlayTimer.cancel();
                                        return;
                                    }

                                    Platform.runLater(() -> {
                                            //labelstatusOrg.setText(" Status: Bar " + playmidifile.getSequencerBeat()));
                                            lblbeatcount.setText("Bar: " + playmidifile.getSequencerBeat());
                                    });

                                    //System.out.println("OrganScene: Sequencer Bar.Beat " + playmidifile.getSequencerTickPosition());
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

                        lblbeatcount.setText("Bar: 0.0");

                        bplaying = false;
                    }
                }
                catch (Exception exception) {
                    bplaying = false;

                    btnplay.setText("Play Song");
                    btnplay.setStyle(btnplayOff);

                    exception.printStackTrace();
                }
                //System.out.println("OrganScene: " + readpresets.presetString(presetIdx * 16 + channelIdx));
            });
            gridmidcenterOrgan.add(btnplay, 0, 6, 1, 1);

            Button btntest = new Button("Test Voice");
            btntest.setStyle(btnplayOff);
            btntest.setPrefSize(120, 50);
            btntest.setOnAction(e -> {
                try {
                    if (!btestnote) {
                        btntest.setText("Stop");
                        btntest.setStyle(btnplayOn);

                        PlayMidi playmidifile = PlayMidi.getInstance();
                        MidiPatch patch = dopatches.getMIDIPatch(patchidx);
                        //System.out.println("OrganScene: Selecting patch " + patch.toString());

                        // Note: Monitor as using CHAN 15 by default may cause unexpected behavior.
                        playmidifile.sendMidiProgramChange((byte)(lastVoiceChannel), (byte)patch.getPC(), (byte)patch.getLSB(), (byte)patch.getMSB());
                        playmidifile.sendMidiNote((byte)(lastVoiceChannel), (byte)60, true);

/*
                        playmidifile.sendMidiControlChange((byte)channelIdx, ccVOL, (byte)sliderVOL.getValue());
                        playmidifile.sendMidiControlChange((byte)channelIdx, ccEXP, (byte)sliderEXP.getValue());
                        playmidifile.sendMidiControlChange((byte)channelIdx, ccREV, (byte)sliderREV.getValue());
                        playmidifile.sendMidiControlChange((byte)channelIdx, ccCHO, (byte)sliderCHO.getValue());
                        playmidifile.sendMidiControlChange((byte)channelIdx, ccMOD, (byte)sliderMOD.getValue());
                        playmidifile.sendMidiControlChange((byte)channelIdx, ccPAN, (byte)sliderPAN.getValue());
                        playmidifile.sendMidiControlChange((byte)channelIdx, ccTRE, (byte)sliderTRE.getValue());
*/
                        btestnote = true;
                    }
                    else {
                        btntest.setText("Test Voice");
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
            gridmidcenterOrgan.add(btntest, 1, 6, 1, 1);

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

            GridPane gridEffects = new GridPane();
            gridEffects.add(new VBox(new Label("VOL"), sliderVOL), 0, 1, 1, 1);
            gridEffects.add(new VBox(new Label("REV"), sliderREV), 1, 1, 1, 1);
            gridEffects.add(new VBox(new Label("CHO"), sliderCHO), 2, 1, 1, 1);
            gridEffects.add(new VBox(new Label("MOD"), sliderMOD), 3, 1, 1, 1);
            gridEffects.add(new VBox(new Label("PAN"), sliderPAN), 4, 1, 1, 1);
            gridEffects.setHgap(25);
            gridmidcenterOrgan.add(gridEffects, 3, 5, 3, 2);
            gridmidcenterOrgan.setStyle(styletext);

            // Finalize Organ Center Panel

            BorderPane midcenterOrganPanel = new BorderPane();
            midcenterOrganPanel.setCenter(gridmidcenterOrgan);
            midcenterOrganPanel.setPadding(new Insets(10, 0, 10, 0));

            // Final assembly the Organ Center Panel

            BorderPane centerOrganPanel = new BorderPane();
            centerOrganPanel.setTop(gridTopLine);
            centerOrganPanel.setCenter(midcenterOrganPanel);
            centerOrganPanel.setBottom(presetGrid);
            centerOrganPanel.setPadding(new Insets(10, 10, 10, 10));

            // Assemble the Scene BorderPane View

            borderPaneOrg.setTop(borderPaneTop);
            //borderPaneOrg.setLeft(leftseparator);
            borderPaneOrg.setCenter(centerOrganPanel);
            //borderPaneOrg.setRight(rightseparator);
            borderPaneOrg.setBottom(labelstatusOrg);

            // Populate Midi Button Text with Patch names as read from file.
            initMidiButtonPatches(midiButtons);

            // Enable Save button only once a change has been made in UI
            buttonSave.setDisable(true);
            flgDirtyPreset = false;
        }
        catch (Exception ex) {
            System.err.println("### OrganScene Exception: Unable to read Stylesheets!");
            System.err.println(ex);
        }
    }

    void buttonPresetLoad(String presetFile) {

        dopresets.makeMidiPresets(presetFile);

        //System.out.println("OrganScene: New Song selected: Loaded new Preset file: " + presetFile);
    }

    void buttonPresetAction(int presetIdx) {

        // Apply selected Preset Program and Control Changes to MIDI output
        for (int chanidx = 0; chanidx < 16; chanidx++) {
            MidiPreset preset = dopresets.getPreset(presetIdx + chanidx);
            dopresets.applyMidiPreset(preset, chanidx);
        }

        // Apply selected Preset Program Changes to Organ Buttons
        // To do: Determine if we need to update and save the Button PC, MSB, LSB, PatchIdx, etc as well

        // Upper 1-1
        MidiPreset preset = dopresets.getPreset(presetIdx + sharedStatus.getUpper1CHAN()-1);
        rbutton11.setText(preset.getPatchName());
        offAllUpper1Buttons();
        rbutton11.setStyle(rcolorOn);

        //int buttonidx = midiButtons.lookupButtonIdx(rbutton11.getId());
        ////midiButtons.getMidiButton(buttonidx, 0).setPatchId((int)dopatches.getMIDIPatch(patchidx).getPatchId());
        //midiButtons.getMidiButton(buttonidx, 0).setPC((int)preset.getPC());
        //midiButtons.getMidiButton(buttonidx, 0).setLSB((int)preset.getLSB());
        //midiButtons.getMidiButton(buttonidx, 0).setMSB((int)preset.getMSB());

        // Upper 2-1
        preset = dopresets.getPreset(presetIdx + sharedStatus.getUpper2CHAN()-1);
        rbutton21.setText(preset.getPatchName());
        offAllUpper2Buttons();
        rbutton21.setStyle(rcolorOn);

        // Upper 3-1
        preset = dopresets.getPreset(presetIdx + sharedStatus.getUpper3CHAN()-1);
        rbutton31.setText(preset.getPatchName());
        offAllUpper3Buttons();
        rbutton31.setStyle(rcolorOn);

        // Lower 1-1
        preset = dopresets.getPreset(presetIdx + sharedStatus.getLower1CHAN());
        lbutton11.setText(preset.getPatchName());
        offAllLower1Buttons();
        lbutton11.setStyle(lcolorOn);

        // Lower 2-1
        preset = dopresets.getPreset(presetIdx + sharedStatus.getLower2CHAN());
        lbutton21.setText(preset.getPatchName());
        offAllLower2Buttons();
        lbutton21.setStyle(lcolorOn);

        // Bass 1
        preset = dopresets.getPreset(presetIdx + sharedStatus.getBassCHAN());
        bleft1.setText(preset.getPatchName());
        offAllBassButtons();
        bleft1.setStyle(bcolorOn);

        // Drum 1
        preset = dopresets.getPreset(presetIdx + sharedStatus.getDrumCHAN());
        dleft1.setText(preset.getPatchName());
        offAllDrumButtons();
        dleft1.setStyle(dcolorOn);

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

        String patchname = dopatches.getMIDIPatch(midiButton.getPatchId()).getPatchName();
        labelstatusOrg.setText(" Status: Applied Patch: " + patchname + " " +  " CHAN:" + (CHAN + 1)
                + " PC:" + midiButton.getPC() + " LSB:" + midiButton.getLSB() + " MSB:" + midiButton.getMSB());
        //System.out.println("OrganScene: applyMidiButton " + buttonidx + " applied " + midiButton.toString());
    }

    // Populate Every Midi Button Patchname on Screen
    private void initMidiButtonPatches(MidiButtons midiButtons) {

        //System.out.println("OrganScene: Initialized all Midi Button Patch Names");
        int patchid = 0;

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

    /** Returns the current Scene **/
    public Scene getScene() {
        return sceneOrgan;
    }

}
