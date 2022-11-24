package amidifx.scenes;

import amidifx.*;
import amidifx.PlayMidi;
import amidifx.models.*;
import amidifx.utils.AppConfig;
import amidifx.utils.ArduinoUtils;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/*********************************************************
 * Preset Scene
 *********************************************************/

public class PresetScene {

    // Scaling based on default 1024 x 600 resolution.
    // Anything other resolution overrides and may require additional tuning of screen layout
    final static float xscreensize = 1280;
    final static float yscreensize = 800;

    float xmul = xscreensize/1024f;
    float ymul = yscreensize/600f;

    int ifsize = 15;
    int xscene = (int)(1024 * xmul);
    int yscene = (int)(600 * ymul - 45);    // 45 = Adjustment while we are single showing Windows status bar

    int xbutton = (int)(154 * xmul);
    int ybutton = (int)(50 * ymul);

    int xtoolbarleft = (int)(225 * xmul);
    int xtitle = 200 * (int)(xmul);
    int xtoolbarright = (int)(150 * xmul);

    int xfileselect = (int)(25 * xmul);
    int yfileselect = (int)(15 * ymul);

    int xsmallbtn = (int)(75 * xmul);
    int ysmallbtn = (int)(25 * ymul);

    int xsmallestbtn = (int)(40 * xmul);
    int ysmallestbtn = (int)(10 * ymul);

    int xsonglist = (int)(200 * xmul);
    int ysonglist = (int)(575 * 1); //ymul

    int xpatchlist = (int)(180 * xmul);
    int ypatchlist = (int)(545 * 1); //xmul)

    int xpresetlist = (int)(180 * xmul);
    int ypresetlist = (int)(545 * 1); //xmul)

    int yslider = (int)(135 * xmul);

    int xstatusleft = (int)(400 * xmul);

    int xmute = (int)(50 * xmul);

    // Calculate font size based on screen dimensions. Default = 15 for 1024 * 600
    final String fsize = Integer.toString((int)(ifsize * xmul)) + "; ";
    final String fsmallsize = Integer.toString((int)(ifsize / 1.30 * xmul)) + "; ";
    final String fsizetitle = Integer.toString((int)(ifsize * xmul * 1.1)) + "; ";
    final String fsizesmall = Integer.toString((int)(ifsize * xmul * 8/10)) + "; ";

    // Button Colors
    // https://yagisanatode.com/2019/08/06/google-apps-script-hexadecimal-color-codes-for-google-docs-sheets-and-slides-standart-palette/
    final String bgpanecolor = "-fx-background-color: #000000; ";
    final String bgheadercolor = "-fx-background-color: #B2B5B1; ";
    final String bgfootercolor = "-fx-background-color: #B2B5B1; ";

    final String btnMenuOn = "-fx-background-color: #4493C0; -fx-font-size: " + fsize;
    final String btnMenuOff = "-fx-background-color: #69A8CC; -fx-font-size: " + fsize;
    final String btnMenuSaveOn = "-fx-background-color: #DB6B6B; -fx-font-size: " + fsize;

    final String btnplayOff = "-fx-background-color: #8ED072; -fx-font-size: " + fsize;
    final String btnplayOn = "-fx-background-color: #DB6B6B; -fx-font-size: " + fsize;

    final String selectcolorOff = "-fx-background-color: #69A8CC; -fx-font-size: " + fsize;
    final String selectcolorOn = "-fx-background-color: #4493C0; -fx-font-size: " + fsize;

    final String btnPresetOff = "-fx-background-color: #DBD06B;  -fx-font-size: " + fsize;
    final String btnPresetOn = "-fx-background-color: #C3B643;  -fx-font-size: " + fsize;

    final String stlInstrumentList =  "-fx-control-inner-background:#CCCCCC; -fx-font-size: " + fsize;

    final String styletext = "-fx-text-fill: black; -fx-font-size: " + fsize ;
    final String styletextred = "-fx-text-fill: red; -fx-font-size: " + fsize ;
    final String styletextwhite = "-fx-text-fill: white; -fx-font-size: " + fsize ;
    final String styletextwhitesmall = "-fx-text-fill: white; -fx-font-size: " + fsizesmall ;
    final String smallstyletext = "-fx-background-color: #69A8CC; -fx-font-size: " + fsmallsize ;
    final String styletexttitle = "-fx-font-size: " + fsizetitle;

    Scene scenePerform, scenePresets, sceneSongs, sceneHome;
    MidiPatches dopatches;
    MidiPresets dopresets;
    MidiSongs dosongs;
    MidiModules midimodules;

    AppConfig config;
    SharedStatus sharedStatus;
    ArduinoUtils arduinoUtils;

    // Main pane for the Perform scene
    private BorderPane panePresets;
    Scene scenePreset;

    Label midiLayerLabel;   // Midi Channel Layer Indicator
    Label labeleffects;     // Midi Channel Effects

    //CheckBox[] chkBoxArray; // MIDI Out Channel Layer
    //int checkIdx = 0;

    int patchIdx = 100;     // On Screen Voice Index Started
    int selpatchIdx = 0;    // Selected on Screen Voice Index

    int moduleIdx = 0;
    int presetIdx = 0;
    int channelIdx = 0;
    int channelIdxSound = 0;
    boolean flgDirtyPreset = false; // Track need to save changes Presets
    boolean bplaying = false;
    boolean btestnote = false;
    int octavetestnote =  0;

    ListView<String> presetListView;
    String songTitle = "Organ";
    String songFile = "amloop.mid";
    String presetFile = "defaultgm.pre";

    Label labelsongdetails = new Label(" ");
    Label labelstatus = new Label(" ");
    Label labelsongtitle = new Label(" ");
    Label labelmidifile = new Label(" ");
    Label labelpresetfile = new Label("  ");

    Button pstbutton1 = new Button();
    Button pstbutton2 = new Button();
    Button pstbutton3 = new Button();
    Button pstbutton4 = new Button();
    Button pstbutton5 = new Button();
    Button pstbutton6 = new Button();
    Button pstbutton7 = new Button();
    Button pstbutton8 = new Button();
    Button pstbutton9 = new Button();
    Button pstbutton10 = new Button();
    Button pstbutton11 = new Button();
    Button pstbutton12 = new Button();
    Button pstbutton13 = new Button();
    Button pstbutton14 = new Button();
    Button pstbutton15 = new Button();
    Button pstbutton16 = new Button();

    Boolean bpressed1 = false;
    Boolean bpressed2 = false;
    Boolean bpressed3 = false;
    Boolean bpressed4 = false;
    Boolean bpressed5 = false;
    Boolean bpressed6 = false;
    Boolean bpressed7 = false;
    Boolean bpressed8 = false;
    Boolean bpressed9 = false;
    Boolean bpressed10 = false;
    Boolean bpressed11 = false;
    Boolean bpressed12 = false;
    Boolean bpressed13 = false;
    Boolean bpressed14 = false;
    Boolean bpressed15 = false;
    Boolean bpressed16 = false;

    Button cfgVOLbutton;
    Button cfgEXPbutton;
    Button cfgREVbutton;
    Button cfgCHObutton;
    Button cfgMODbutton;
    Button cfgTIMbutton;
    Button cfgATKbutton;
    Button cfgRELbutton;
    Button cfgBRIbutton;
    Button cfgPANbutton;
    Button cfgOCTbutton;

    Button buttonSave = new Button();   // Presets save button - disabled/enabled when dirty
    Button buttonReload = new Button(); // Reload current Presets
    Button buttonvoice;                 // Update Channel with new Patch/Voice
    Button buttontest;

    Button buttonPresetSceneInit = new Button("");

    Button buttonApplyAllPresets;

    ComboBox presetCombo;

    Slider sliderVOL;
    Slider sliderEXP;
    Slider sliderREV;
    Slider sliderCHO;
    Slider sliderMOD;
    Slider sliderTIM;
    Slider sliderATK;
    Slider sliderREL;
    Slider sliderBRI;
    Slider sliderPAN;
    Slider sliderOCT;

    // https://professionalcomposers.com/midi-cc-list/
    public static byte ccVOL = 7;
    public static byte ccEXP = 11;
    public static byte ccREV = 91;
    public static byte ccTRE = 92;
    public static byte ccCHO = 93;
    public static byte ccMOD = 1;
    public static byte ccPAN = 10;

    public static byte ccTIM = 71;
    public static byte ccREL = 72;
    public static byte ccATK = 73;
    public static byte ccBRI = 74;

    MidiPreset midiPreset;

    ComboBox moduleCombo;
    boolean bmodulechanged = false;

    // Select Bank and Sounds Panel
    public PresetScene(Stage stage) {

        System.out.println("PresetScene: Preset Scene Starting!");

        sharedStatus = SharedStatus.getInstance();
        config = AppConfig.getInstance();
        dopatches = MidiPatches.getInstance();

        midimodules = new MidiModules();

        BorderPane borderPresets = new BorderPane();
        borderPresets.setStyle(bgpanecolor);

        scenePresets = new Scene(borderPresets, xscene, yscene);
        scenePresets.getStylesheets().clear();
        scenePresets.getStylesheets().add("style.css");

        sharedStatus.setPresetsScene(scenePresets);

        // Invisible Button to trigger updates from previous Scene
        buttonPresetSceneInit.setVisible(false);
        sharedStatus.setButtonPresetSceneInit(buttonPresetSceneInit);
        buttonPresetSceneInit.setOnAction(e -> {

            labelsongdetails.setText(sharedStatus.getSongDetails());
            buttonSave.setDisable(true);
        });

        // Create top bar navigation buttons
        Button buttonsc1 = new Button("Manual");
        buttonsc1.setStyle(btnMenuOff);
        buttonsc1.setOnAction(e -> {
            // Reload original Sound Module if changed
            if (bmodulechanged == true) {
                moduleIdx = sharedStatus.getModuleidx();

                moduleCombo.getSelectionModel().select(moduleIdx);
                Event.fireEvent(moduleCombo, new ActionEvent());

                bmodulechanged = false;
                System.out.println("Module changed to: " + sharedStatus.getModuleName(moduleIdx));
            }

            System.out.println(("Main: Changing to Organ Scene: " + sharedStatus.getPerformScene().toString()));
            stage.setScene(sharedStatus.getPerformScene());

            try {
                Thread.sleep(250);
            } catch (Exception ex) {
            }
        });

        Button buttonsc2 = new Button("Songs");
        buttonsc2.setStyle(btnMenuOff);
        buttonsc2.setOnAction(e -> {

            // Reload original Sound Module if changed
            if (bmodulechanged == true) {
                moduleIdx = sharedStatus.getModuleidx();

                moduleCombo.getSelectionModel().select(moduleIdx);
                Event.fireEvent(moduleCombo, new ActionEvent());

                bmodulechanged = false;
                System.out.println("Module changed to: " + sharedStatus.getModuleName(moduleIdx));
            }

            System.out.println(("Main: Changing to Songs Scene: " + sharedStatus.getSongsScene().toString()));
            stage.setScene(sharedStatus.getSongsScene());

            try {
                Thread.sleep(250);
            } catch (Exception ex) {
            }
        });

        Button buttonsc3 = new Button("Presets");
        buttonsc3.setStyle(btnMenuOn);
        buttonsc3.setOnAction(e -> {

            // Reload original Sound Module if changed
            if (bmodulechanged == true) {
                moduleIdx = sharedStatus.getModuleidx();

                moduleCombo.getSelectionModel().select(moduleIdx);
                Event.fireEvent(moduleCombo, new ActionEvent());

                bmodulechanged = false;
                System.out.println("Module changed to: " + sharedStatus.getModuleName(moduleIdx));
            }

            System.out.println(("Main: Changing to Presets Scene: " + sharedStatus.getPresetsScene().toString()));
            stage.setScene(sharedStatus.getPresetsScene());

            try {
                Thread.sleep(250);
            } catch (Exception ex) {
            }
        });

        Button buttonPanic = new Button("  Panic  ");
        buttonPanic.setStyle(btnMenuOff);
        buttonPanic.setOnAction(e -> {
            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiPanic();

            labelstatus.setText(" Status: MIDI Panic Sent");
        });

        Button buttonExit = new Button("  Exit  ");
        buttonExit.setStyle(btnMenuOff);
        buttonExit.setOnAction(e -> {
            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.stopMidiPlay("End Play");

            try {
                Receiver midircv = sharedStatus.getRxDevice();
                midircv.close();
            } catch (Exception ex) {
                System.out.println("Exception on receiver close");
            }
            try {
                Transmitter midixmt = sharedStatus.getTxDevice();
                midixmt.close();
            } catch (Exception ex) {
                System.out.println("Exception on transmitter close");
            }
            try {
                Sequencer midiseq = sharedStatus.getSeqDevice();
                midiseq.close();
            } catch (Exception ex) {
                System.out.println("Exception on sequencer close");
            }

            //arduinoUtils.closePort();

            System.exit(0);
        });

        // Save Presets Button
        buttonSave.setText("Save Presets");
        buttonSave.setStyle(btnMenuSaveOn);
        buttonSave.setDisable(true);
        buttonSave.setOnAction(event -> {
            if (flgDirtyPreset) {
                presetFile = sharedStatus.getPresetFile();
                boolean bsave = dopresets.saveMidiPresets(presetFile);
                if (bsave) {
                    labelstatus.setText(" Status: Song Presets saved");
                } else {
                    labelstatus.setText(" Status: Presets save error!");
                }
                buttonSave.setDisable(true);
                flgDirtyPreset = false;
            } else
                labelstatus.setText(" Status: Presets not changed. No need to save");
        });

        // Reload Presets Button
        buttonReload.setText("Reload");
        buttonReload.setStyle(btnMenuOff);
        buttonReload.setDisable(false);
        buttonReload.setOnAction(event -> {
            presetFile = sharedStatus.getPresetFile();
            if (!dopresets.loadMidiPresets(presetFile)) {
                labelstatus.setText(" Status: Error loading preset file " + presetFile);
                labelstatus.setStyle(styletextred);

                try {
                    wait(10000);
                }
                catch(Exception exception) {}
            }
            else {
                // Update the newly selected Preset MIDI Channel Voice list
                for (int idx = 0; idx < 16; idx++) {
                    midiPreset = dopresets.getPreset(presetIdx * 16 + idx);

                    String strName = Integer.toString(idx + 1).concat(":").concat(midiPreset.getPatchName());
                    presetListView.getItems().set(idx, strName);

                    //System.out.println("Main: Patch name " + strName);
                }
                channelIdx = 0;
                presetListView.getSelectionModel().select(channelIdx);
                presetListView.refresh();

                presetCombo.requestFocus();
                presetCombo.getSelectionModel().select(0);

                // Force reload of all channels
                PlayMidi playmidifile = PlayMidi.getInstance();
                playmidifile.resetcurPresetList();

                labelstatus.setText(" Status: Reloaded Presets file " + presetFile);
            }
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

        ToolBar toolbarRight = new ToolBar(buttonSave, buttonReload, buttonPanic, buttonExit);
        toolbarRight.setStyle(bgheadercolor);
        toolbarRight.setMinWidth(xtoolbarright);

        BorderPane borderPaneTop = new BorderPane();
        borderPaneTop.setStyle(bgheadercolor);

        // Assemble the Menu Bar Border Pane
        borderPaneTop.setLeft(toolbarLeft);
        borderPaneTop.setCenter(hboxTitle);
        borderPaneTop.setRight(toolbarRight);

        // *** Start Bottom Status Bar

        BorderPane borderStatus = new BorderPane();
        borderStatus.setStyle(bgheadercolor);

        labelstatus.setText(" Status: " + sharedStatus.getStatusText());
        labelstatus.setStyle(styletext);

        labelsongdetails.setText(sharedStatus.getSongDetails());
        labelsongdetails.setStyle(styletext);

        FlowPane panefiles = new FlowPane();
        panefiles.setHgap(20);
        panefiles.getChildren().add(labelsongdetails);

        VBox vboxstatusLeft = new VBox();
        vboxstatusLeft.setMinWidth(xstatusleft);
        vboxstatusLeft.getChildren().add(labelstatus);

        // Assemble the Status BorderPane View
        borderStatus.setLeft(vboxstatusLeft);
        borderStatus.setCenter(panefiles);
        //borderStatus.setRight(vboxstatusright);

        // **** Show Left Pane: MIDI Sound Bank List

        ArrayList moduleNames = new ArrayList();
        moduleNames.add(midimodules.getModuleName(0));
        moduleNames.add(midimodules.getModuleName(1));
        moduleNames.add(midimodules.getModuleName(2));

        ObservableList<String> soundbank = FXCollections.observableArrayList();
        ListView<String> banklistView = new ListView<>(soundbank);
        banklistView.setPrefWidth(xpatchlist);
        banklistView.setPrefHeight(ypatchlist);
        banklistView.setStyle("-fx-control-inner-background: #E7ECEC;");

        moduleCombo = new ComboBox(FXCollections.observableArrayList(moduleNames));
        sharedStatus.setModuleCombo(moduleCombo);
        moduleCombo.setPrefSize(xpatchlist, 20);
        moduleCombo.setStyle(selectcolorOff);
        //moduleCombo.setDisable(true);               // Do not select a module that is not loaded
        moduleCombo.getSelectionModel().select(sharedStatus.getModuleidx());
        EventHandler<ActionEvent> midxevent =
                e -> {

                    int moduleidx1 = moduleCombo.getSelectionModel().getSelectedIndex();
                    dopatches.loadMidiPatches(midimodules.getModuleFile(moduleidx1));

                    // Save ModuleIdx so we can reload it back to original when we exit the page
                    // If we temporarily changed the moduleidx, we need to reload the active module patches as we exit this page
                    // as well as disable the Voice Sound and Play Buttons as the sounds selected do not match the active sound
                    // module
                    if (moduleidx1 != sharedStatus.getModuleidx()) {

                        buttonSave.setDisable(true);
                        buttonvoice.setDisable(true);
                        buttontest.setDisable(true);

                        bmodulechanged = true;
                        System.out.println("Module changed to" + sharedStatus.getModuleName(moduleidx1));
                    } else {
                        buttonSave.setDisable(false);
                        buttonvoice.setDisable(false);
                        buttontest.setDisable(false);
                    }

                    banklistView.getItems().clear();
                    banklistView.getSelectionModel().clearSelection();

                    MidiBanks midiBanks = dopatches.getMidiBanks();
                    for (int idx = 0; idx < midiBanks.getMidiBankSize(); idx++) {
                        MidiBank midiBank = midiBanks.getMidiBank(idx);
                        String name = midiBank.getBankName();
                        banklistView.getItems().add(name);
                    }

                    channelIdx = 0;
                    banklistView.getSelectionModel().select(channelIdx);
                    banklistView.refresh();

                    labelstatus.setText(" Status: Selected Sound Module " + moduleCombo.getValue().toString());
                };
        moduleCombo.setOnAction(midxevent);
        VBox vboxModuleList = new VBox(moduleCombo);
        vboxModuleList.setStyle(styletext);

        MidiBanks midiBanks = dopatches.getMidiBanks();
        for (int idx = 0; idx < midiBanks.getMidiBankSize(); idx++) {
            MidiBank midiBank = midiBanks.getMidiBank(idx);
            String name = midiBank.getBankName();
            banklistView.getItems().add(name);
        }
        VBox vboxList = new VBox(banklistView);

        banklistView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String old_val, String new_val) -> {
                    String selectedItem = banklistView.getSelectionModel().getSelectedItem();
                    int idx = banklistView.getSelectionModel().getSelectedIndex();

                    // *** To do: Monitor to see if this is needed. Prevents an exception when switching Sound mMdules
                    if (idx != -1) {
                        patchIdx = midiBanks.getMidiBank(idx).getPatchIdx();
                        int totalpatchcnt = dopatches.getMIDIPatchSize();
                        //System.out.println("Main: PatchIdx = " + patchIdx + " " + totalpatchcnt);
                        renderVoiceButtons(patchIdx, totalpatchcnt);
                    }
                    offAllButtons();
                    pstbutton1.fire();

                    labelstatus.setText(" Status: Selected Bank " + selectedItem);
                });
        banklistView.getSelectionModel().selectFirst();
        banklistView.setStyle(styletext);

        Button buttonb = new Button("Select Voice Bank");
        buttonb.setStyle(selectcolorOff);
        buttonb.setPrefSize(xbutton, ybutton - 10);
        buttonb.setOnAction(event -> {
            ObservableList selectedIndices = banklistView.getSelectionModel().getSelectedIndices();

            for (Object o : selectedIndices) {
                System.out.println("Main: o = " + o + " (" + o.getClass() + ")");
                int idx = Integer.parseInt(o.toString());
                patchIdx = midiBanks.getMidiBank(idx).getPatchIdx();
                int totalpatchcnt = dopatches.getMIDIPatchSize();
                renderVoiceButtons(patchIdx, totalpatchcnt);
            }
        });
        VBox vboxbut = new VBox(buttonb);

        VBox vboxLeft = new VBox();
        vboxLeft.setSpacing(10);
        vboxLeft.setPadding(new Insets(5, 5, 5, 5));
        vboxLeft.getChildren().add(vboxModuleList);
        vboxLeft.getChildren().add(vboxList);
        vboxLeft.getChildren().add(vboxbut);

        // Load MIDI Default MIDI Preset file on start up
        dopresets = MidiPresets.getInstance();
        if (!dopresets.loadMidiPresets(presetFile)) {
            labelstatus.setText(" Status: Error loading preset file " + presetFile);
            labelstatus.setStyle(styletextred);

            try {
                wait(10000);
            }
            catch(Exception exception) {}
        }
        else
            System.out.println("Main Init: Loaded new Preset file " + presetFile);

        // **** Show Right Pane: MIDI Sound Bank List

        // List selected Preset channel sounds
        ObservableList<String> patchdata = FXCollections.observableArrayList();
        //ListView<String> presetListView = new ListView<String>(patchdata);
        presetListView = new ListView<>(patchdata);
        presetListView.setPrefWidth(xpatchlist);
        presetListView.setPrefHeight(ypresetlist);
        presetListView.setStyle("-fx-control-inner-background: #E7ECEC;");
        sharedStatus.setPresetListView(presetListView);

        // Preset select Combobox
        String[] ListofPresets = {"Preset 1", "Preset 2", "Preset 3", "Preset 4", "Preset 5", "Preset 6", "Preset 7", "Preset 8"};
        presetCombo = new ComboBox(FXCollections.observableArrayList(ListofPresets));
        sharedStatus.setPresetCombo(presetCombo);
        presetCombo.setPrefSize(xpatchlist, 20);
        presetCombo.setStyle(selectcolorOff);
        presetCombo.getSelectionModel().select(0);
        EventHandler<ActionEvent> pidxevent =
                e -> {
                    presetIdx = Integer.parseInt(presetCombo.getValue().toString().replaceAll("[^0-9]", ""));
                    presetIdx--;

                    // Update the newly selected Preset MIDI Channel Voice list
                    for (int idx = 0; idx < 16; idx++) {
                        midiPreset = dopresets.getPreset(presetIdx * 16 + idx);

                        String strName = Integer.toString(idx + 1).concat(":").concat(midiPreset.getPatchName());
                        presetListView.getItems().set(idx, strName);

                        //System.out.println("Main: Patch name " + strName);
                    }
                    channelIdx = 0;
                    presetListView.getSelectionModel().select(channelIdx);
                    presetListView.refresh();

                    // **** Update the MIDI Channel Out Layers
                    //String strChannelOut = dopresets.getPreset(presetIdx * 16 + channelIdx).getChannelOutIdx();
                    //for (int i = 0; i < 16; i++) {
                    //    chkBoxArray[i].setSelected(false);
                    //}

                    //String[] tokens = strChannelOut.split("\\||,");
                    //for (String token : tokens) {
                    //    int chkidx = Integer.parseInt(token);
                    //    //System.out.println("Main: Layer index " + chkidx);
                    //    if (chkidx > 0)
                    //        chkBoxArray[chkidx - 1].setSelected(true);
                    //}

                    labelstatus.setText(" Status: Selected Preset " + (presetIdx + 1));
                };
        presetCombo.setOnAction(pidxevent);
        Platform.runLater(() -> {
            presetCombo.requestFocus();
            presetCombo.getSelectionModel().select(0);

            System.out.println("Main: Run later selected Preset 0");
        });
        VBox vboxPresetList = new VBox(presetCombo);
        vboxPresetList.setStyle(styletext);

        channelIdx = 0;
        presetIdx = 0;
        for (int idx = 0; idx < 16; idx++) {
            MidiPreset midiPreset = dopresets.getPreset(presetIdx * 16 + idx);
            String name = midiPreset.getPatchName();
            //name = StringUtils.rightPad(name, 2);
            presetListView.getItems().add((idx) + ":" + name);

            //System.out.println("Main: Patch name " + name);
        }
        presetListView.getSelectionModel().select(channelIdx);

        VBox vboxList2 = new VBox(presetListView);
        // Initial populate of the Preset List View
        presetListView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String old_val, String new_val) -> {
                    String selectedItem = presetListView.getSelectionModel().getSelectedItem();
                    channelIdx = presetListView.getSelectionModel().getSelectedIndex();

                    boolean isdirty = flgDirtyPreset;

                    // Update the MIDI Channel Effects sliders to new MIDI voice channel selected
                    sliderVOL.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getVOL());
                    sliderEXP.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getEXP());
                    sliderREV.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getREV());
                    sliderCHO.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getCHO());
                    sliderTIM.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getTIM());
                    sliderATK.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getATK());
                    sliderREL.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getREL());
                    sliderBRI.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getBRI());
                    sliderMOD.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getMOD());
                    sliderPAN.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getPAN());
                    sliderOCT.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getOctaveTran());

                    // **** Update the MIDI Channel Out Layer checkboxes
                    //String strChannelOut = dopresets.getPreset(presetIdx * 16 + channelIdx).getChannelOutIdx();
                    //for (int i = 0; i < 16; i++) {
                    //    chkBoxArray[i].setSelected(false);
                    //}

                    //String[] tokens = strChannelOut.split("\\||,");
                    //for (String token : tokens)
                    //{
                    //    int chkidx = Integer.parseInt(token);
                    //    //System.out.println("Main: Layer index: " + chkidx);
                    //    if (chkidx > 0)
                    //        chkBoxArray[chkidx - 1].setSelected(true);
                    //}

                    //if (isdirty == false) {
                    //    flgDirtyPreset = false;
                    //    buttonSave.setDisable(true);
                    //}

                    //System.out.println("Main: Item selected " + selectedItem + ", Item index: " + channelIdx);
                    labelstatus.setText(" Status: CHAN Voice " + selectedItem);

                    setMidiLayerLabel(channelIdx + 1);
                });
        presetListView.getSelectionModel().selectFirst();
        presetListView.setStyle(styletext);

        // Update Voice for currently selected Channel in Preset Listview
        buttonvoice = new Button("Set Channel Voice");
        buttonvoice.setStyle(selectcolorOff);
        buttonvoice.setPrefSize(xbutton, ybutton - 10);
        buttonvoice.setOnAction(event -> {
            MidiPatch midiPatch = dopatches.getMIDIPatch(selpatchIdx);
            dopresets.setPreset(presetIdx * 16 + channelIdx, moduleIdx, midiPatch);

            String name = midiPatch.getPatchName();
            presetListView.getItems().set(channelIdx, (channelIdx + 1 + ":" + name));

            // Load the MIDI Channel Effects sliders for new MIDI voice channel selected
            sliderVOL.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getVOL());
            sliderEXP.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getEXP());
            sliderREV.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getREV());
            sliderCHO.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getCHO());
            sliderTIM.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getTIM());
            sliderATK.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getATK());
            sliderREL.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getREL());
            sliderBRI.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getBRI());
            sliderMOD.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getMOD());
            sliderPAN.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getPAN());
            sliderOCT.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getOctaveTran());

            // Apply the Voice to MIDI Channel
            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiProgramChange(channelIdx, midiPatch.getPC(), midiPatch.getLSB(), midiPatch.getMSB());

            //System.out.println("Main: Updated selected Preset and Channel Voice");
            labelstatus.setText(" Status: Applied CHAN Voice " + (channelIdx + 1) + " " + midiPatch.getPatchName());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });
        VBox vboxvoice = new VBox(buttonvoice);

        VBox vboxRight = new VBox();
        vboxRight.setSpacing(10);
        vboxRight.setSpacing(10);
        vboxRight.setPadding(new Insets(5, 5, 5, 5));
        vboxRight.getChildren().add(vboxPresetList);
        vboxRight.getChildren().add(vboxList2);
        vboxRight.getChildren().add(vboxvoice);

        // **** Show Middle Pane: Sound Fonts for selected Bank. First sound from Bank 1 1 by default
        FlowPane flowpane = new FlowPane();
        flowpane.prefHeight(ypatchlist);
        flowpane.setPadding(new Insets(5, 5, 0, 5));    // Insets(double top, double right, double bottom, double left)
        flowpane.setHgap(10);
        flowpane.setVgap(10);

        bpressed1 = false;
        pstbutton1 = new Button("Button 1");
        pstbutton1.setStyle(btnPresetOff);
        pstbutton1.setWrapText(true);
        pstbutton1.setTextAlignment(TextAlignment.CENTER);
        pstbutton1.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx);
            if (!bpressed1) {
                pstbutton1.setStyle(btnPresetOn);
            } else {
                pstbutton1.setStyle(btnPresetOff);
            }
            bpressed1 = !bpressed1;
        });
        pstbutton1.setPrefSize(xbutton, ybutton);

        bpressed2 = false;
        pstbutton2 = new Button("Button 2");
        pstbutton2.setStyle(btnPresetOff);
        pstbutton2.setWrapText(true);
        pstbutton2.setTextAlignment(TextAlignment.CENTER);
        pstbutton2.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 1);
            if (!bpressed2) {
                pstbutton2.setStyle(btnPresetOn);
            } else {
                pstbutton2.setStyle(btnPresetOff);
            }
            bpressed2 = !bpressed2;
        });
        pstbutton2.setPrefSize(xbutton, ybutton);

        bpressed3 = false;
        pstbutton3 = new Button("Button 3");
        pstbutton3.setStyle(btnPresetOff);
        pstbutton3.setWrapText(true);
        pstbutton3.setTextAlignment(TextAlignment.CENTER);
        pstbutton3.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 2);
            if (!bpressed3) {
                pstbutton3.setStyle(btnPresetOn);
            } else {
                pstbutton3.setStyle(btnPresetOff);
            }
            bpressed3 = !bpressed3;
        });
        pstbutton3.setPrefSize(xbutton, ybutton);

        bpressed4 = false;
        pstbutton4 = new Button("Button 4");
        pstbutton4.setStyle(btnPresetOff);
        pstbutton4.setWrapText(true);
        pstbutton4.setTextAlignment(TextAlignment.CENTER);
        pstbutton4.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 3);
            if (!bpressed4) {
                pstbutton4.setStyle(btnPresetOn);
            } else {
                pstbutton4.setStyle(btnPresetOff);
            }
            bpressed4 = !bpressed4;
        });
        pstbutton4.setPrefSize(xbutton, ybutton);

        bpressed5 = false;
        pstbutton5 = new Button("Button 5");
        pstbutton5.setStyle(btnPresetOff);
        pstbutton5.setWrapText(true);
        pstbutton5.setTextAlignment(TextAlignment.CENTER);
        pstbutton5.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 4);
            if (!bpressed5) {
                pstbutton5.setStyle(btnPresetOn);
            } else {
                pstbutton5.setStyle(btnPresetOff);
            }
            bpressed5 = !bpressed5;
        });
        pstbutton5.setPrefSize(xbutton, ybutton);

        bpressed6 = false;
        pstbutton6 = new Button("Button 6");
        pstbutton6.setStyle(btnPresetOff);
        pstbutton6.setWrapText(true);
        pstbutton6.setTextAlignment(TextAlignment.CENTER);
        pstbutton6.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 5);
            if (!bpressed6) {
                pstbutton6.setStyle(btnPresetOn);
            } else {
                pstbutton6.setStyle(btnPresetOff);
            }
            bpressed6 = !bpressed6;
        });
        pstbutton6.setPrefSize(xbutton, ybutton);

        bpressed7 = false;
        pstbutton7 = new Button("Button 7");
        pstbutton7.setStyle(btnPresetOff);
        pstbutton7.setWrapText(true);
        pstbutton7.setTextAlignment(TextAlignment.CENTER);
        pstbutton7.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 6);
            if (!bpressed7) {
                pstbutton7.setStyle(btnPresetOn);
            } else {
                pstbutton7.setStyle(btnPresetOff);
            }
            bpressed7 = !bpressed7;
        });
        pstbutton7.setPrefSize(xbutton, ybutton);

        bpressed8 = false;
        pstbutton8 = new Button("Button 8");
        pstbutton8.setStyle(btnPresetOff);
        pstbutton8.setWrapText(true);
        pstbutton8.setTextAlignment(TextAlignment.CENTER);
        pstbutton8.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 7);
            if (!bpressed8) {
                pstbutton8.setStyle(btnPresetOn);
            } else {
                pstbutton8.setStyle(btnPresetOff);
            }
            bpressed8 = !bpressed8;
        });
        pstbutton8.setPrefSize(xbutton, ybutton);

        bpressed9 = false;
        pstbutton9 = new Button("Button 9");
        pstbutton9.setStyle(btnPresetOff);
        pstbutton9.setWrapText(true);
        pstbutton9.setTextAlignment(TextAlignment.CENTER);
        pstbutton9.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 8);
            if (!bpressed9) {
                pstbutton9.setStyle(btnPresetOn);
            } else {
                pstbutton9.setStyle(btnPresetOff);
            }
            bpressed9 = !bpressed9;
        });
        pstbutton9.setPrefSize(xbutton, ybutton);

        bpressed10 = false;
        pstbutton10 = new Button("Button 10");
        pstbutton10.setStyle(btnPresetOff);
        pstbutton10.setWrapText(true);
        pstbutton10.setTextAlignment(TextAlignment.CENTER);
        pstbutton10.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 9);
            if (!bpressed10) {
                pstbutton10.setStyle(btnPresetOn);
            } else {
                pstbutton10.setStyle(btnPresetOff);
            }
            bpressed10 = !bpressed10;
        });
        pstbutton10.setPrefSize(xbutton, ybutton);

        bpressed11 = false;
        pstbutton11 = new Button("Button 11");
        pstbutton11.setStyle(btnPresetOff);
        pstbutton11.setWrapText(true);
        pstbutton11.setTextAlignment(TextAlignment.CENTER);
        pstbutton11.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 10);
            if (!bpressed11) {
                pstbutton11.setStyle(btnPresetOn);
            } else {
                pstbutton11.setStyle(btnPresetOff);
            }
            bpressed11 = !bpressed11;
        });
        pstbutton11.setPrefSize(xbutton, ybutton);

        bpressed12 = false;
        pstbutton12 = new Button("Button 12");
        pstbutton12.setStyle(btnPresetOff);
        pstbutton12.setWrapText(true);
        pstbutton12.setTextAlignment(TextAlignment.CENTER);
        pstbutton12.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 11);
            if (!bpressed12) {
                pstbutton12.setStyle(btnPresetOn);
            } else {
                pstbutton12.setStyle(btnPresetOff);
            }
            bpressed12 = !bpressed12;
        });
        pstbutton12.setPrefSize(xbutton, ybutton);

        bpressed13 = false;
        pstbutton13 = new Button("Button 13");
        pstbutton13.setStyle(btnPresetOff);
        pstbutton13.setWrapText(true);
        pstbutton13.setTextAlignment(TextAlignment.CENTER);
        pstbutton13.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 12);
            if (!bpressed13) {
                pstbutton13.setStyle(btnPresetOn);
            } else {
                pstbutton13.setStyle(btnPresetOff);
            }
            bpressed13 = !bpressed13;
        });
        pstbutton13.setPrefSize(xbutton, ybutton);

        bpressed14 = false;
        pstbutton14 = new Button("Button 14");
        pstbutton14.setStyle(btnPresetOff);
        pstbutton14.setWrapText(true);
        pstbutton14.setTextAlignment(TextAlignment.CENTER);
        pstbutton14.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 13);
            if (!bpressed14) {
                pstbutton14.setStyle(btnPresetOn);
            } else {
                pstbutton14.setStyle(btnPresetOff);
            }
            bpressed14 = !bpressed14;
        });
        pstbutton14.setPrefSize(xbutton, ybutton);

        bpressed15 = false;
        pstbutton15 = new Button("Button 15");
        pstbutton15.setStyle(btnPresetOff);
        pstbutton15.setWrapText(true);
        pstbutton15.setTextAlignment(TextAlignment.CENTER);
        pstbutton15.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 14);
            if (!bpressed15) {
                pstbutton15.setStyle(btnPresetOn);
            } else {
                pstbutton15.setStyle(btnPresetOff);
            }
            bpressed15 = !bpressed15;
        });
        pstbutton15.setPrefSize(xbutton, ybutton);

        bpressed16 = false;
        pstbutton16 = new Button("Button 16");
        pstbutton16.setStyle(btnPresetOff);
        pstbutton16.setWrapText(true);
        pstbutton16.setTextAlignment(TextAlignment.CENTER);
        pstbutton16.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx + 15);
            if (!bpressed16) {
                pstbutton16.setStyle(btnPresetOn);
            } else {
                pstbutton16.setStyle(btnPresetOff);
            }
            bpressed16 = !bpressed16;
        });
        pstbutton16.setPrefSize(xbutton, ybutton);

        Button btnprev = new Button("<< Previous");
        btnprev.setStyle(selectcolorOff);
        btnprev.setPrefSize(xbutton, ybutton);
        btnprev.setOnAction(e -> {
            offAllButtons();
            renderVoiceButtons(patchIdx - 16 > 0 ? (patchIdx = patchIdx - 16) : (patchIdx = 0), dopatches.getMIDIPatchSize());
        });

        channelIdxSound = sharedStatus.getDemoCHAN();
        buttontest = new Button("Demo Voice");
        buttontest.setStyle(btnplayOff);
        buttontest.setPrefSize(xbutton, ybutton);
        buttontest.setOnAction(e -> {
            try {

                // Play Demo Note
                if (!btestnote) {
                    buttontest.setText("Stop");
                    buttontest.setStyle(btnplayOn);

                    PlayMidi playmidifile = PlayMidi.getInstance();
                    MidiPatch patch = dopatches.getMIDIPatch(selpatchIdx);
                    //System.out.println("Main: Selecting patch " + patch.toString());

                    playmidifile.sendMidiProgramChange((byte) (channelIdxSound), (byte) patch.getPC(), (byte) patch.getLSB(), (byte) patch.getMSB());

                    playmidifile.sendMidiControlChange((byte) (channelIdxSound), ccVOL, (byte) sliderVOL.getValue());
                    playmidifile.sendMidiControlChange((byte) (channelIdxSound), ccEXP, (byte) sliderEXP.getValue());
                    playmidifile.sendMidiControlChange((byte) (channelIdxSound), ccREV, (byte) sliderREV.getValue());
                    playmidifile.sendMidiControlChange((byte) (channelIdxSound), ccCHO, (byte) sliderCHO.getValue());
                    playmidifile.sendMidiControlChange((byte) (channelIdxSound), ccMOD, (byte) sliderMOD.getValue());
                    playmidifile.sendMidiControlChange((byte) (channelIdxSound), ccPAN, (byte) sliderPAN.getValue());
                    playmidifile.sendMidiControlChange((byte) (channelIdxSound), ccTIM, (byte) sliderTIM.getValue());
                    playmidifile.sendMidiControlChange((byte) (channelIdxSound), ccATK, (byte) sliderATK.getValue());
                    playmidifile.sendMidiControlChange((byte) (channelIdxSound), ccREL, (byte) sliderREL.getValue());
                    playmidifile.sendMidiControlChange((byte) (channelIdxSound), ccBRI, (byte) sliderBRI.getValue());

                    // Play middle C ON octave translated.
                    // However, ensure demo note is OFF before applying a new demo note
                    if (btestnote) {
                        // Play middle C OFF octave translated
                        channelIdxSound = sharedStatus.getDemoCHAN();
                        PlayMidi playmidifileOFF = PlayMidi.getInstance();
                        playmidifileOFF.sendMidiNote((byte) (channelIdxSound), (byte) octavetestnote, false);
                        btestnote = false;
                    }

                    channelIdxSound = 0;
                    byte octave = (byte)sliderOCT.getValue();
                    int note = 60 + (byte) (octave * 13);
                    channelIdxSound = sharedStatus.getDemoCHAN();
                    PlayMidi playmidifileON = PlayMidi.getInstance();
                    playmidifileON.sendMidiNote((byte) (channelIdxSound), (byte) note, true);
                    //playmidifile.midiDemo(channelIdx, 1);

                    // Remember channel sounding so, we can turn this one off after we may have changed to another
                    //channelIdxSound = channelIdx;
                    btestnote = true;
                    octavetestnote =  note;
                }
                // Demo Note Off
                else {
                    buttontest.setText("Demo Voice");
                    buttontest.setStyle(btnplayOff);

                    // Play middle C OFF octave translated
                    byte octave = (byte)sliderOCT.getValue();
                    int note = 60 + (byte) (octave * 13);

                    channelIdxSound = sharedStatus.getDemoCHAN();
                    PlayMidi playmidifileOFF = PlayMidi.getInstance();
                    playmidifileOFF.sendMidiNote((byte) (channelIdxSound), (byte) note, false);

                    btestnote = false;

                    // Re-Apply MIDI Program Change on Upper Channel for Button Press since we used it for sound Demo
                    int CHAN = sharedStatus.getUpper1CHAN();

                    MidiPreset applypreset = dopresets.getPreset(presetIdx * 16 + CHAN);
                    dopresets.applyMidiPreset(applypreset, channelIdx);
                }

            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        Button btndemo = new Button(" Play Song");
        btndemo.setStyle(btnplayOff);
        btndemo.setPrefSize(xbutton, ybutton);
        btndemo.setOnAction(e -> {
            try {
                MidiSong midisong = sharedStatus.getCurrentSong();

                if (!bplaying) {
                    btndemo.setText(" Stop Play");
                    btndemo.setStyle(btnplayOn);

                    bplaying = true;

                    PlayMidi playmidifile = PlayMidi.getInstance();
                    if (!playmidifile.startMidiPlay(midisong, dopresets, 2)) {
                        labelstatus.setText(" Status: " + sharedStatus.getStatusText());
                    }
                    else {

                        // Disable Perform and Songs menu switch while playing
                        buttonsc1.setDisable(true);
                        buttonsc2.setDisable(true);

                        // Song Play Repeating Timer: Collects Beat Timer and Play Status every 250ms
                        Timer songPlayTimer = new Timer();
                        songPlayTimer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                // Check if Play stopped and reset Button Status
                                if (!playmidifile.isMidiRunning()) {

                                    Platform.runLater(() -> {
                                        bplaying = false;

                                        btndemo.setText("Play Song");
                                        btndemo.setStyle(btnplayOff);
                                        labelstatus.setText(" Status: Song Play Complete " + playmidifile.getSequencerTickPosition());

                                        // Enable Perform and Songs menu switch when not playing
                                        buttonsc1.setDisable(false);
                                        buttonsc2.setDisable(false);
                                    });
                                    songPlayTimer.cancel();
                                    return;
                                }

                                Platform.runLater(() -> {
                                    labelstatus.setText(" Status: Bar " + playmidifile.getSequencerBeat());
                                });

                                //System.out.println(" Main: Sequencer Bar.Beat " + playmidifile.getSequencerTickPosition());
                            }
                        }, 0, 100);

                        labelstatus.setText(" Status: Playing file " + midisong.getMidiFile());
                    }
                } else {
                    btndemo.setText("Play Song");
                    btndemo.setStyle(btnplayOff);

                    PlayMidi playmidifile = PlayMidi.getInstance();
                    playmidifile.stopMidiPlay(songFile);

                    bplaying = false;

                    // Enable Perform and Songs menu switch when not playing
                    buttonsc1.setDisable(false);
                    buttonsc2.setDisable(false);
                }
            }
            catch (Exception exception) {
                bplaying = false;

                btndemo.setText("Play Song");
                btndemo.setStyle(btnplayOff);

                // Enable Perform and Songs menu switch when not playing
                buttonsc1.setDisable(false);
                buttonsc2.setDisable(false);

                exception.printStackTrace();
            }
            //System.out.println("Main: " + readpresets.presetString(presetIdx * 16 + channelIdx));
        });

        Button btnnext = new Button("Next >>");
        btnnext.setStyle(selectcolorOff);
        btnnext.setPrefSize(xbutton, ybutton);
        btnnext.setOnAction(e -> {
            offAllButtons();
            //System.out.println("Main: patchIdx " + patchIdx + " Total Patch Size: " + dopatches.getMIDIPatchSize());
            if ((patchIdx + 16) <= dopatches.getMIDIPatchSize()) {
                renderVoiceButtons(patchIdx = patchIdx + 16, dopatches.getMIDIPatchSize());
            }
        });

        // Create VOL slider
        sliderVOL = new Slider(0, 127, 0);
        sliderVOL.setOrientation(Orientation.VERTICAL);
        sliderVOL.setShowTickLabels(true);
        sliderVOL.setShowTickMarks(true);
        sliderVOL.setMajorTickUnit(16);
        sliderVOL.setBlockIncrement(4);
        sliderVOL.setPrefHeight(yslider);
        Rotate rotateVol = new Rotate();
        sliderVOL.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateVol.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange(channelIdx, ccVOL, (byte) sliderVOL.getValue());

            dopresets.getPreset(presetIdx * 16 + channelIdx).setVOL(newValue.intValue());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " VOL: " + newValue.intValue());
        });

        // Create EXP slider
        sliderEXP = new Slider(0, 127, 0);
        sliderEXP.setOrientation(Orientation.VERTICAL);
        sliderEXP.setShowTickLabels(true);
        sliderEXP.setShowTickMarks(true);
        sliderEXP.setMajorTickUnit(16);
        sliderEXP.setBlockIncrement(4);
        sliderEXP.setPrefHeight(yslider);
        Rotate rotateExp = new Rotate();
        sliderEXP.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateExp.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange(channelIdx, ccEXP, (byte) sliderEXP.getValue());

            dopresets.getPreset(presetIdx * 16 + channelIdx).setEXP(newValue.intValue());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status; CHAN " + (channelIdx +1) + " EXP: " + newValue.intValue());
        });

        // Create REV slider
        sliderREV = new Slider(0, 127, 0);
        sliderREV.setOrientation(Orientation.VERTICAL);
        sliderREV.setShowTickLabels(true);
        sliderREV.setShowTickMarks(true);
        sliderREV.setMajorTickUnit(16);
        sliderREV.setBlockIncrement(4);
        sliderREV.setPrefHeight(yslider);
        Rotate rotateRev = new Rotate();
        sliderREV.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateRev.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange(channelIdx, ccREV, (byte) sliderREV.getValue());

            dopresets.getPreset(presetIdx * 16 + channelIdx).setREV(newValue.intValue());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " REV: " + newValue.intValue());
        });

        // Create CHO slider
        sliderCHO = new Slider(0, 127, 0);
        sliderCHO.setOrientation(Orientation.VERTICAL);
        sliderCHO.setShowTickLabels(true);
        sliderCHO.setShowTickMarks(true);
        sliderCHO.setMajorTickUnit(16);
        sliderCHO.setBlockIncrement(4);
        sliderCHO.setPrefHeight(yslider);
        Rotate rotateCho = new Rotate();
        sliderCHO.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateCho.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange(channelIdx, ccCHO, (byte) sliderCHO.getValue());

            dopresets.getPreset(presetIdx * 16 + channelIdx).setCHO(newValue.intValue());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " CHO: " + newValue.intValue());
        });

        // Create MOD slider
        sliderMOD = new Slider(0, 127, 0);
        sliderMOD.setOrientation(Orientation.VERTICAL);
        sliderMOD.setShowTickLabels(true);
        sliderMOD.setShowTickMarks(true);
        sliderMOD.setMajorTickUnit(16);
        sliderMOD.setBlockIncrement(4);
        sliderMOD.setPrefHeight(yslider);
        Rotate rotateMod = new Rotate();
        sliderMOD.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateMod.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange(channelIdx, ccMOD, (byte) sliderMOD.getValue());

            dopresets.getPreset(presetIdx * 16 + channelIdx).setMOD(newValue.intValue());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Starus: CHAN " + (channelIdx + 1) + " MOD: " + newValue.intValue());
        });

        // Create PAN slider
        sliderPAN = new Slider(0, 127, 0);
        sliderPAN.setOrientation(Orientation.VERTICAL);
        sliderPAN.setShowTickLabels(true);
        sliderPAN.setShowTickMarks(true);
        sliderPAN.setMajorTickUnit(16);
        sliderPAN.setBlockIncrement(4);
        sliderPAN.setPrefHeight(yslider);
        Rotate rotatePan = new Rotate();
        sliderPAN.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotatePan.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange(channelIdx, ccPAN, (byte) sliderPAN.getValue());

            //System.out.println("Main: Old Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());
            dopresets.getPreset(presetIdx * 16 + channelIdx).setPAN(newValue.intValue());
            //System.out.println("Main: New Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " PAN: " + newValue.intValue());
        });

        // Create TIM (Timber/Resonance) slider
        sliderTIM = new Slider(0, 127, 0);
        sliderTIM.setOrientation(Orientation.VERTICAL);
        sliderTIM.setShowTickLabels(true);
        sliderTIM.setShowTickMarks(true);
        sliderTIM.setMajorTickUnit(16);
        sliderTIM.setBlockIncrement(4);
        sliderTIM.setPrefHeight(yslider);
        Rotate rotateTim = new Rotate();
        sliderTIM.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateTim.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange(channelIdx, ccTIM, (byte) sliderTIM.getValue());

            //System.out.println("Main: Old Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());
            dopresets.getPreset(presetIdx * 16 + channelIdx).setTIM(newValue.intValue());
            //System.out.println("Main: New Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " TIM: " + newValue.intValue());
        });

        // Create ATK (Attack) slider
        sliderATK = new Slider(0, 127, 0);
        sliderATK.setOrientation(Orientation.VERTICAL);
        sliderATK.setShowTickLabels(true);
        sliderATK.setShowTickMarks(true);
        sliderATK.setMajorTickUnit(16);
        sliderATK.setBlockIncrement(4);
        sliderATK.setPrefHeight(yslider);
        Rotate rotateAtk = new Rotate();
        sliderATK.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateAtk.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange(channelIdx, ccATK, (byte) sliderATK.getValue());

            //System.out.println("Main: Old Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());
            dopresets.getPreset(presetIdx * 16 + channelIdx).setATK(newValue.intValue());
            //System.out.println("Main: New Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " ATK: " + newValue.intValue());
        });

        // Create REL (Release) slider
        sliderREL = new Slider(0, 127, 0);
        sliderREL.setOrientation(Orientation.VERTICAL);
        sliderREL.setShowTickLabels(true);
        sliderREL.setShowTickMarks(true);
        sliderREL.setMajorTickUnit(16);
        sliderREL.setBlockIncrement(4);
        sliderREL.setPrefHeight(yslider);
        Rotate rotateRel = new Rotate();
        sliderREL.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateRel.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange(channelIdx, ccREL, (byte) sliderREL.getValue());

            //System.out.println("Main: Old Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());
            dopresets.getPreset(presetIdx * 16 + channelIdx).setREL(newValue.intValue());
            //System.out.println("Main: New Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " REL: " + newValue.intValue());
        });

        // Create BRI (Brightness) slider
        sliderBRI = new Slider(0, 127, 0);
        sliderBRI.setOrientation(Orientation.VERTICAL);
        sliderBRI.setShowTickLabels(true);
        sliderBRI.setShowTickMarks(true);
        sliderBRI.setMajorTickUnit(16);
        sliderBRI.setBlockIncrement(4);
        sliderBRI.setPrefHeight(yslider);
        Rotate rotateBri = new Rotate();
        sliderBRI.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateBri.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange(channelIdx, ccBRI, (byte) sliderBRI.getValue());

            //System.out.println("Main: Old Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());
            dopresets.getPreset(presetIdx * 16 + channelIdx).setBRI(newValue.intValue());
            //System.out.println("Main: New Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " BRI: " + newValue.intValue());
        });

        // Create OCT (Octave) slider
        sliderOCT = new Slider(-2.5, 2.5, 0);
        sliderOCT.setOrientation(Orientation.VERTICAL);
        sliderOCT.setShowTickLabels(true);
        sliderOCT.setShowTickMarks(true);
        sliderOCT.setMajorTickUnit(1);
        sliderOCT.setBlockIncrement(1);
        sliderOCT.setPrefHeight(yslider);
        Rotate rotateOct = new Rotate();
        sliderOCT.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateOct.setAngle((double) newValue);

            System.out.println("Main: Old Oct Value " + dopresets.getPreset(presetIdx * 16 + channelIdx).getOctaveTran());
            dopresets.getPreset(presetIdx * 16 + channelIdx).setOctaveTran(newValue.intValue());
            System.out.println("Main: New Oct Value " + dopresets.getPreset(presetIdx * 16 + channelIdx).getOctaveTran());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " OCT: " + newValue.intValue());
        });

        flowpane.getChildren().add(pstbutton1);
        flowpane.getChildren().add(pstbutton2);
        flowpane.getChildren().add(pstbutton3);
        flowpane.getChildren().add(pstbutton4);
        flowpane.getChildren().add(pstbutton5);
        flowpane.getChildren().add(pstbutton6);
        flowpane.getChildren().add(pstbutton7);
        flowpane.getChildren().add(pstbutton8);
        flowpane.getChildren().add(pstbutton9);
        flowpane.getChildren().add(pstbutton10);
        flowpane.getChildren().add(pstbutton11);
        flowpane.getChildren().add(pstbutton12);
        flowpane.getChildren().add(pstbutton13);
        flowpane.getChildren().add(pstbutton14);
        flowpane.getChildren().add(pstbutton15);
        flowpane.getChildren().add(pstbutton16);

        flowpane.getChildren().add(btnprev);
        flowpane.getChildren().add(buttontest);
        flowpane.getChildren().add(btndemo);
        flowpane.getChildren().add(btnnext);

        // Add MIDI Channel Effects for currently selected
        VBox vboxEffects = new VBox();
        //vboxEffects.setSpacing(10);
        vboxEffects.setPadding(new Insets(0, 10, 0, 10));
        vboxEffects.setStyle(styletext);

        labeleffects = new Label("Effects: Channel " + (channelIdx + 1));
        labeleffects.setStyle(styletextwhite);

        Label vollabel = new Label("VOL");
        vollabel.setStyle(styletextwhitesmall);
        Label explabel = new Label("EXP");
        explabel.setStyle(styletextwhitesmall);
        Label revlabel = new Label("REV");
        revlabel.setStyle(styletextwhitesmall);
        Label cholabel = new Label("CHO");
        cholabel.setStyle(styletextwhitesmall);
        Label modlabel = new Label("MOD");
        modlabel.setStyle(styletextwhitesmall);
        Label timlabel = new Label("TIM");
        timlabel.setStyle(styletextwhitesmall);
        Label atklabel = new Label("ATK");
        atklabel.setStyle(styletextwhitesmall);
        Label rellabel = new Label("REL");
        rellabel.setStyle(styletextwhitesmall);
        Label brilabel = new Label("BRI");
        brilabel.setStyle(styletextwhitesmall);
        Label panlabel = new Label("PAN");
        panlabel.setStyle(styletextwhitesmall);
        Label octlabel = new Label("OCT");
        octlabel.setStyle(styletextwhitesmall);

        GridPane gridEffects = new GridPane();
        gridEffects.add(new VBox(vollabel, sliderVOL), 0, 1, 1, 1);
        gridEffects.add(new VBox(explabel, sliderEXP), 1, 1, 1, 1);
        gridEffects.add(new VBox(revlabel, sliderREV), 2, 1, 1, 1);
        gridEffects.add(new VBox(cholabel, sliderCHO), 3, 1, 1, 1);
        gridEffects.add(new VBox(modlabel, sliderMOD), 5, 1, 1, 1);
        gridEffects.add(new VBox(timlabel, sliderTIM), 6, 1, 1, 1);
        gridEffects.add(new VBox(atklabel, sliderATK), 7, 1, 1, 1);
        gridEffects.add(new VBox(rellabel, sliderREL), 8, 1, 1, 1);
        gridEffects.add(new VBox(brilabel, sliderBRI), 9, 1, 1, 1);
        gridEffects.add(new VBox(panlabel, sliderPAN), 10, 1, 1, 1);
        gridEffects.add(new VBox(octlabel, sliderOCT), 11, 1, 1, 1);

        // Effects and Slider Default or deeper Configurations
        HBox hboxEffects = new HBox();
        hboxEffects.setSpacing(5);

        cfgVOLbutton = new Button("SET");
        cfgVOLbutton.setStyle(smallstyletext);
        cfgVOLbutton.setPrefSize(xsmallestbtn, ysmallestbtn);
        cfgVOLbutton.setOnAction(e -> {
            sliderVOL.setValue(90);     // CC7 is 127 for full volume on a MIDI instrument, yet when it's used to control the fader of an audio- or instrument-track *in Logic*, 90 will hit unity gain while 127 will give +6dB of gain.
            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });

        cfgEXPbutton = new Button("SET");
        cfgEXPbutton.setStyle(smallstyletext);
        cfgEXPbutton.setPrefSize(xsmallestbtn, ysmallestbtn);
        cfgEXPbutton.setOnAction(e -> {
            sliderEXP.setValue(127);    // CC11 to land on 127, as it is designed to only turn volume down.
            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });

        cfgREVbutton = new Button("SET");
        cfgREVbutton.setStyle(smallstyletext);
        cfgREVbutton.setPrefSize(xsmallestbtn, ysmallestbtn);
        cfgREVbutton.setOnAction(e -> {
            sliderREV.setValue(16);
            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });

        cfgCHObutton = new Button("SET");
        cfgCHObutton.setStyle(smallstyletext);
        cfgCHObutton.setPrefSize(xsmallestbtn, ysmallestbtn);
        cfgCHObutton.setDisable(false);
        cfgCHObutton.setOnAction(e -> {
            sliderCHO.setValue(8);
            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });

        cfgMODbutton = new Button("SET");
        cfgMODbutton.setStyle(smallstyletext);
        cfgMODbutton.setPrefSize(xsmallestbtn, ysmallestbtn);
        cfgMODbutton.setDisable(false);
        cfgMODbutton.setOnAction(e -> {
            sliderMOD.setValue(0);
            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });

        cfgTIMbutton = new Button("SET");
        cfgTIMbutton.setStyle(smallstyletext);
        cfgTIMbutton.setPrefSize(xsmallestbtn, ysmallestbtn);
        cfgTIMbutton.setOnAction(e -> {
            sliderTIM.setValue(64);

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });

        cfgATKbutton = new Button("SET");
        cfgATKbutton.setStyle(smallstyletext);
        cfgATKbutton.setPrefSize(xsmallestbtn, ysmallestbtn);
        cfgATKbutton.setOnAction(e -> {
            sliderATK.setValue(16);

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });

        cfgRELbutton = new Button("SET");
        cfgRELbutton.setStyle(smallstyletext);
        cfgRELbutton.setPrefSize(xsmallestbtn, ysmallestbtn);
        cfgRELbutton.setOnAction(e -> {
            sliderREL.setValue(16);

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });

        cfgBRIbutton = new Button("SET");
        cfgBRIbutton.setStyle(smallstyletext);
        cfgBRIbutton.setPrefSize(xsmallestbtn, ysmallestbtn);
        cfgBRIbutton.setOnAction(e -> {
            sliderBRI.setValue(64);

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });

        cfgPANbutton = new Button("SET");
        cfgPANbutton.setStyle(smallstyletext);
        cfgPANbutton.setPrefSize(xsmallestbtn, ysmallestbtn);
        cfgPANbutton.setOnAction(e -> {
            sliderPAN.setValue(64);

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });

        cfgOCTbutton = new Button("SET");
        cfgOCTbutton.setStyle(smallstyletext);
        cfgOCTbutton.setPrefSize(xsmallestbtn, ysmallestbtn);
        cfgOCTbutton.setOnAction(e -> {
            sliderOCT.setValue(0);

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset
        });

        hboxEffects.getChildren().add(cfgVOLbutton);
        hboxEffects.getChildren().add(cfgEXPbutton);
        hboxEffects.getChildren().add(cfgREVbutton);
        hboxEffects.getChildren().add(cfgCHObutton);
        hboxEffects.getChildren().add(cfgMODbutton);
        hboxEffects.getChildren().add(cfgTIMbutton);
        hboxEffects.getChildren().add(cfgATKbutton);
        hboxEffects.getChildren().add(cfgRELbutton);
        hboxEffects.getChildren().add(cfgBRIbutton);
        hboxEffects.getChildren().add(cfgPANbutton);
        hboxEffects.getChildren().add(cfgOCTbutton);
        hboxEffects.setPadding(new Insets(5, 0, 0, 0));

        vboxEffects.getChildren().add(labeleffects);
        vboxEffects.getChildren().add(gridEffects);
        vboxEffects.getChildren().add(hboxEffects);

        // Add MIDI Channel Layering for each Channel
        VBox vboxLayers = new VBox();
        vboxLayers.setSpacing(10);

/*
        midiLayerLabel = new Label("Keyboard: Channel " + (channelIdx + 1) + " Layers");
        midiLayerLabel.setStyle(styletextwhite);
        midiLayerLabel.setDisable(!ArduinoUtils.getInstance().hasARMPort());

        GridPane gridLayers = new GridPane();
        gridLayers.setHgap(3);
        gridLayers.setVgap(3);

        String[] midiChannels = { " 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ",
                " 9 ", "10 ", "11 ", "12 ", "13 ", "14 ", "15 ", "16 "};

        chkBoxArray = new CheckBox[16];
        int x, y;
        for (checkIdx = 0; checkIdx < 16; checkIdx++) {                     // midiChannels.length
            chkBoxArray[checkIdx] = new CheckBox(midiChannels[checkIdx]);
            chkBoxArray[checkIdx].setStyle(styletextwhite);
            chkBoxArray[checkIdx].setDisable(!ArduinoUtils.getInstance().hasARMPort());
            // Add Check Box event to save changes to Preset
            EventHandler<ActionEvent> event = e -> {
                updateChannelOutIdx(channelIdx);

                buttonSave.setDisable(false);
                flgDirtyPreset = true;
            };
            chkBoxArray[checkIdx].setOnAction(event);

            if (checkIdx < 4) { x = checkIdx; y = 0; }
            else if (checkIdx < 8) { x = checkIdx - 4; y = 1; }
            else if (checkIdx < 12) { x = checkIdx - 8; y = 3; }
            else { x = checkIdx - 12; y = 5; }
            gridLayers.add(chkBoxArray[checkIdx], x, y, 1, 1);
        }
        vboxLayers.setPadding(new Insets(0, 0, 0,0));
        vboxLayers.getChildren().add(midiLayerLabel);
        //vboxLayers.getChildren().add(gridLayers);
*/

        VBox vboxApplyPreset = new VBox();
        vboxApplyPreset.setSpacing(10);

        Label labelPresets = new Label("Apply Presets");
        labelPresets.setStyle(styletextwhite);

        // Send Presets to MIDI Module Button
        Button buttonApplyPreset = new Button("Cur Channel");
        buttonApplyPreset.setPrefSize(xbutton / 1.5, ybutton);
        buttonApplyPreset.setStyle(btnplayOff);
        buttonApplyPreset.setOnAction(event -> {
            MidiPreset applypreset = dopresets.getPreset(presetIdx * 16 + channelIdx);
            dopresets.applyMidiPreset(applypreset, channelIdx);

            labelstatus.setText(" Status: MIDI sent Voice to Preset " + (presetIdx + 1) + " Channel " + (channelIdx + 1));
        });

        // Send All Presets to MIDI Module Button
        buttonApplyAllPresets = new Button("All Channels");
        buttonApplyAllPresets.setPrefSize(xbutton / 1.5, ybutton);
        buttonApplyAllPresets.setStyle(btnplayOff);
        buttonApplyAllPresets.setOnAction(event -> {
            for (int idx = 0; idx < 16; idx++) {
                MidiPreset applypreset = dopresets.getPreset(presetIdx * 16 + idx);
                dopresets.applyMidiPreset(applypreset, idx);
            }

            labelstatus.setText(" Status: MIDI sent Voices to all Preset Channels");
        });

        // Copy all Presets to Next Preset. Makes it easier to set next one up - especially when it is incremental
        Button buttonCopyPresets = new Button("Copy Next");
        buttonCopyPresets.setPrefSize(xbutton / 1.5, ybutton / 1.15);
        buttonCopyPresets.setStyle(selectcolorOff);
        buttonCopyPresets.setOnAction(event -> {
            if (presetIdx >= 7) {
                labelstatus.setText(" Status: Copy Preset " + (presetIdx + 1) + " no next Preset to copy to!");
                return;
            }

            for (int idx = 0; idx < 16; idx++) {
                MidiPreset midipreset = dopresets.getPreset(presetIdx * 16 + idx);
                dopresets.copyPreset(midipreset, presetIdx * 16 + idx, (presetIdx + 1) * 16 + idx, presetIdx + 1);
            }
            buttonSave.setDisable(false);
            flgDirtyPreset = true;

            labelstatus.setText(" Status: Preset " + (presetIdx + 1) + " copied to next " + (presetIdx + 2));
        });

        vboxApplyPreset.getChildren().add(labelPresets);
        vboxApplyPreset.getChildren().add(buttonApplyPreset);
        vboxApplyPreset.getChildren().add(buttonApplyAllPresets);

        VBox vboxCopyAll = new VBox();
        vboxCopyAll.setPadding(new Insets(27, 0, 0, 00));
        vboxCopyAll.getChildren().add(buttonCopyPresets);
        vboxApplyPreset.getChildren().add(vboxCopyAll);

        // Assemble Layers and Effects Controls
        flowpane.getChildren().add(vboxEffects);
        flowpane.getChildren().add(vboxLayers);
        flowpane.getChildren().add(vboxApplyPreset);

        // Assemble the Preset Scene BorderPane View
        borderPresets.setTop(borderPaneTop);
        borderPresets.setLeft(vboxLeft);
        borderPresets.setCenter(flowpane);
        borderPresets.setRight(vboxRight);
        borderPresets.setBottom(borderStatus);

        // Initial Patches Render
        renderVoiceButtons(patchIdx, dopatches.getMIDIPatchSize());

        // After initial render set saveButton to false if trigger during initial config, e.g. setting sliders.
        flgDirtyPreset = false;
        buttonSave.setDisable(true);

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
        //    borderPane1.setBackground(background);
        //}
        //catch(FileNotFoundException ex) {
        //    System.err.println("Background image not found! ");
        //}

    }

    public void renderVoiceButtons(int patchIdx, int totalpatchcnt) {

        int idxcnt = 0;
        String pname;

        if ((idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton1.setText(pname);
            pstbutton1.setDisable(false);
        } else {
            pstbutton1.setText("");
            pstbutton1.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton2.setText(pname);
            pstbutton2.setDisable(false);
        } else {
            pstbutton2.setText("");
            pstbutton2.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton3.setText(pname);
            pstbutton3.setDisable(false);
        } else {
            pstbutton3.setText("");
            pstbutton3.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton4.setText(pname);
            pstbutton4.setDisable(false);
        } else {
            pstbutton4.setText("");
            pstbutton4.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton5.setText(pname);
            pstbutton5.setDisable(false);
        } else {
            pstbutton5.setText("");
            pstbutton5.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton6.setText(pname);
            pstbutton6.setDisable(false);
        } else {
            pstbutton6.setText("");
            pstbutton6.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton7.setText(pname);
            pstbutton7.setDisable(false);
        } else {
            pstbutton7.setText("");
            pstbutton7.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton8.setText(pname);
            pstbutton8.setDisable(false);
        } else {
            pstbutton8.setText("");
            pstbutton8.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton9.setText(pname);
            pstbutton9.setDisable(false);
        } else {
            pstbutton9.setText("");
            pstbutton9.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton10.setText(pname);
            pstbutton10.setDisable(false);
        } else {
            pstbutton10.setText("");
            pstbutton10.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton11.setText(pname);
            pstbutton11.setDisable(false);
        } else {
            pstbutton11.setText("");
            pstbutton11.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton12.setText(pname);
            pstbutton12.setDisable(false);
        } else {
            pstbutton12.setText("");
            pstbutton12.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton13.setText(pname);
            pstbutton13.setDisable(false);
        } else {
            pstbutton13.setText("");
            pstbutton13.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton14.setText(pname);
            pstbutton14.setDisable(false);
        } else {
            pstbutton14.setText("");
            pstbutton14.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton15.setText(pname);
            pstbutton15.setDisable(false);
        } else {
            pstbutton15.setText("");
            pstbutton15.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton16.setText(pname);
            pstbutton16.setDisable(false);
        } else {
            pstbutton16.setText("");
            pstbutton16.setDisable(true);
        }

    }

    private void offAllButtons() {
        bpressed1 = false;
        pstbutton1.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed2 = false;
        pstbutton2.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed3 = false;
        pstbutton3.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed4 = false;
        pstbutton4.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed5 = false;
        pstbutton5.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed6 = false;
        pstbutton6.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed7 = false;
        pstbutton7.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed8 = false;
        pstbutton8.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed9 = false;
        pstbutton9.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed10 = false;
        pstbutton10.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed11 = false;
        pstbutton11.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed12 = false;
        pstbutton12.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed13 = false;
        pstbutton13.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed14 = false;
        pstbutton14.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed15 = false;
        pstbutton15.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
        bpressed16 = false;
        pstbutton16.setStyle("-fx-background-color: #DBD06B; -fx-font-size: " + fsize);
    }

    private void updateChannelOutIdx(int channelIdx) {
        boolean flgfirst = false;

        //for (int idx = 0; idx < 16; idx++) {
        //    if (chkBoxArray[idx].isSelected()) {
        //        if (!flgfirst) {
        //            strChannelIdxOut = Integer.toString(idx + 1);
        //            flgfirst = true;
        //        }
        //        else {
        //            strChannelIdxOut = strChannelIdxOut.concat("|").concat(Integer.toString(idx + 1));
        //        }
        //    }
        //}

        dopresets.getPreset(presetIdx * 16 + channelIdx).setChannelOutIdx(channelIdx);
        //System.out.println("Main: ChannelOutIdx " + strChannelIdxOut);

        flgDirtyPreset = true;      // Need to save updated Preset

        labelstatus.setText(" Status: MIDI Layer Out Checkboxes changes updated");
    }


    private void setMidiLayerLabel(int chidx) {
        //String strLayers = "Layers Channel: ".concat(Integer.toString(chidx));
        //midiLayerLabel.setText(strLayers);

        String strEffects = "Effects Channel: ".concat(Integer.toString(chidx));
        labeleffects.setText(strEffects);
    }

    // Patch Button Actions
    private void buttonAction(int patchIdx) {
        String strStatus = "Patch ";
        strStatus = strStatus.concat(Integer.toString(patchIdx)).concat(" - ")
                .concat(dopatches.getMIDIPatch(patchIdx).getPatchName()).concat(" PC:")
                .concat(Integer.toString((dopatches.getMIDIPatch(patchIdx).getPC()))).concat(" LSB:")
                .concat(Integer.toString((dopatches.getMIDIPatch(patchIdx).getLSB()))).concat(" MSB:")
                .concat(Integer.toString((dopatches.getMIDIPatch(patchIdx).getMSB())));

        labelstatus.setText(" Status: " + strStatus);
    }

    /** Returns the current Scene **/
    public Scene getScene() {
        return scenePresets;
    }

}