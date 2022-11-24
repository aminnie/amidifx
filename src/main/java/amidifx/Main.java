package amidifx;

import amidifx.models.MidiModules;
import amidifx.models.MidiPreset;
import amidifx.models.MidiSong;
import amidifx.models.SharedStatus;
import amidifx.scenes.HomeScene;
import amidifx.scenes.PresetScene;
import amidifx.utils.AppConfig;
import amidifx.utils.ArduinoUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import static amidifx.utils.Logger.logSystemToFile;

@SuppressWarnings("CanBeFinal")
public class Main extends Application {

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
    PlayMidi playmidifile;
    MidiPreset midiPreset;

    Label midiLayerLabel;   // Midi Channel Layer Indicator
    Label labeleffects;     // Midi Channel Effects
    //CheckBox[] chkBoxArray; // MIDI Out Channel Layer

    private TableView tableSongList;
    private ObservableList songData;
    private Text statusSong;

    boolean bplaying = false;

    Button midichooser;
    Button presetchooser;

    boolean deleteconfirmed = false;

    ListView<String> presetListView;
    String songTitle = "Organ";
    String songFile = "amloop.mid";
    String presetFile = "defaultgm.pre";
    Label labelstatus = new Label(" ");

    Label labelsongtitle = new Label(" ");
    Label labelmidifile = new Label(" ");
    Label labelpresetfile = new Label("  ");

    Image applicationIcon;

    AppConfig config;
    SharedStatus sharedStatus;
    ArduinoUtils arduinoUtils;

    @Override
    public void start(Stage stage) throws Exception {

        ////Parent root = FXMLLoader.load(getClass().getResource("/amidifx.fxml"));
        stage.setTitle("AMIDIFX");
        stage.initStyle(StageStyle.UNDECORATED);

        applicationIcon = new Image(getClass().getResourceAsStream("/music-48.png"));
        stage.getIcons().add(applicationIcon);

        //Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        //stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 3);
        //stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 3);

        // Create single instance of Shared Status to report back to Scenes
        sharedStatus = SharedStatus.getInstance();

        // Load single instance of XML App Config
        config = AppConfig.getInstance();

        dopresets = MidiPresets.getInstance();

        // Prepare single instance ofPlay Midi
        PlayMidi playmidifile = PlayMidi.getInstance();

        // Prepare single instance of Arduino Interface
        arduinoUtils = ArduinoUtils.getInstance();

        // Load MIDI Sound Module List on start up
        midimodules = new MidiModules();

        // Load Song List. If not exists, abort load AMIDIFX
        dosongs = MidiSongs.getInstance();
        if (!dosongs.fileExist(sharedStatus.getSongList())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("AMIDIFX Startup Error");
            alert.setHeaderText("Song Index file " + sharedStatus.getCFGDirectory() + "songs.sng not found!");
            Optional<ButtonType> result = alert.showAndWait();

            System.exit(-1);
        }
        sharedStatus.setDoSongs(dosongs);

        // Load MIDI Patch files on start up based on AppConfig set sound module
        int moduleidx = config.getSoundModuleIdx();
        sharedStatus.setModuleidx(moduleidx);

        dopatches = MidiPatches.getInstance();
        moduleidx = sharedStatus.getModuleidx();
        String modulefile = midimodules.getModuleFile(moduleidx);
        if (!dopatches.fileExist(modulefile)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("AMIDIFX Startup Error");
            alert.setHeaderText("Module Patch file " + sharedStatus.getCFGDirectory() + modulefile + " not found!");
            Optional<ButtonType> result = alert.showAndWait();

            System.exit(-1);
        }
        dopatches.loadMidiPatches(modulefile);

        // *** Prepare the Home Organ, Song and Preset Screens

        // Song Scene
        sceneSongs = new Scene(createSongScene(stage), xscene, yscene);
        sceneSongs.getStylesheets().clear();
        sceneSongs.getStylesheets().add("style.css");
        sharedStatus.setSongsScene(sceneSongs);

        // Preset Scene
        PresetScene presetScene = new PresetScene(stage);
        sharedStatus.setPresetsScene(presetScene.getScene());

        // Device Configuration Home Scene
        HomeScene homeScene = new HomeScene(stage);
        sharedStatus.setHomeScene(homeScene.getScene());

        // Switch to Device Configuration (Home) Screen for start up
        stage.setScene(homeScene.getScene());

        stage.show();
    }


    /*********************************************************
     * Song Scene
     *********************************************************/

    TextField txtSongTitle = new TextField("");
    TextField txtSmfFile = new TextField("");
    TextField txtPresetFile = new TextField("");
    TextField txtPresetSaveAsFile = new TextField("");
    TextField txtBass = new TextField("2");
    TextField txtLower = new TextField("3");
    TextField txtUpper = new TextField("4");
    TextField txtTimeSig = new TextField("4/4");
    TextArea txtInstrumentList = new TextArea("Play Song to retrieve MIDI Instruments.\n\nMIDI Sequencer mutes by Track not Channel number!");

    Label lblCurSongMidiModule = new Label("MIDI GM");
    int cursongmoduleidx = 0;

    Button buttonupdate = new Button(" Save Song ");

    Button buttonedit = new Button("  Edit ");
    Button buttonnew = new Button("  New ");
    Button buttondelete = new Button("  Delete ");

    int idxSongList = 0;        // Currently selected Song in List

    boolean bnewSong = false;

    RadioButton radioOriginal = new RadioButton("Original    ");
    RadioButton radioPresets;
    RadioButton radioLive;

    public BorderPane createSongScene(Stage stage) {

        System.out.println("Main: Scene SongScene!");

        BorderPane borderPaneSng = new BorderPane();
        borderPaneSng.setStyle(bgpanecolor);

        Label labelstatusSng = new Label(" Status: Ready");
        labelstatusSng.setStyle(styletext);

        FileChooser fileChooserPreset = new FileChooser();
        FileChooser fileChooserMidi = new FileChooser();

        // *** Create Top Navigation Bar

        Button buttonsc1 = new Button("Manual");
        buttonsc1.setStyle(btnMenuOff);
        buttonsc1.setOnAction(e -> {
            System.out.println(("Main: Changing to Organ Scene " + sharedStatus.getPerformScene().toString()));
            stage.setScene(sharedStatus.getPerformScene());
            try {
                Thread.sleep(250);
            } catch (Exception ex) {
                System.err.println("### Main: Unable to set Perform Scene!");
            }
        });

        Button buttonsc2 = new Button("Songs");
        buttonsc2.setStyle(btnMenuOn);
        //buttonsc2.setOnAction(e -> stage.setScene(sceneSongs));
        buttonsc2.setOnAction(e -> {
            System.out.println(("Main: Changing to Songs Scene: " + sharedStatus.getSongsScene().toString()));
            stage.setScene(sharedStatus.getSongsScene());
            try {
                Thread.sleep(250);
            } catch (Exception ex) {
                System.err.println("### Main: Unable to set Songs Scene!");
            }
        });

        Button buttonsc3 = new Button("Presets");
        buttonsc3.setStyle(btnMenuOff);
        //buttonsc3.setOnAction(e -> stage.setScene(scenePresets));
        buttonsc3.setDisable(true);
        buttonsc3.setOnAction(e -> {
            System.out.println(("Main: Changing to Presets Scene " + sharedStatus.getPresetsScene().toString()));
            stage.setScene(sharedStatus.getPresetsScene());
            try {
                Thread.sleep(250);
            } catch (Exception ex) {
                System.err.println("### Main: Unable to set Presets Scene!");
            }
        });

        Button buttonPanic = new Button("  Panic  ");
        buttonPanic.setStyle(btnMenuOff);
        buttonPanic.setOnAction(e -> {
            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiPanic();

            labelstatusSng.setText(" Status: MIDI Panic Sent");
        });

        Button buttonExit = new Button("  Exit  ");
        buttonExit.setStyle(btnMenuOff);
        buttonExit.setOnAction(e -> {
            PlayMidi playmidifile = PlayMidi.getInstance();
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

        // Reload Presets Button
        Button buttonReload = new Button("Reload");
        buttonReload.setStyle(btnMenuOff);
        buttonReload.setDisable(false);
        buttonReload.setOnAction(event -> {
/*
            //dopresets = new MidiPresets();
            presetFile = sharedStatus.getPresetFile();
            dopresets.makeMidiPresets(presetFile);

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

            //presetCombo.requestFocus();
            //presetCombo.getSelectionModel().select(0);

            // Force reload of all channels
            playmidifile = PlayMidi.getInstance();
            playmidifile.resetcurPresetList();

            labelstatus.setText(" Status: Reloaded Presets file " + presetFile);
*/
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

        //Label labelicon = new Label();
        //ImageView view = new ImageView(applicationIcon);
        //labelicon.setGraphic(view);
        //labelicon.setMaxSize(10, 10);
        //hboxTitle.getChildren().add(view);

        ToolBar toolbarRight = new ToolBar(buttonupdate, buttonReload, buttonPanic, buttonExit);
        toolbarRight.setStyle(bgheadercolor);
        toolbarRight.setMinWidth(xtoolbarright);

        BorderPane borderPaneTopSng = new BorderPane();
        borderPaneTopSng.setStyle(bgheadercolor);

        // Assemble the Menu Bar Border Pane
        borderPaneTopSng.setLeft(toolbarLeft);
        borderPaneTopSng.setCenter(hboxTitle);
        borderPaneTopSng.setRight(toolbarRight);

        // *** Start Bottom Status Bar

        BorderPane borderStatusSng = new BorderPane();
        borderStatusSng.setStyle(bgheadercolor);

        labelstatus.setText(" Status: " + sharedStatus.getStatusText());
        labelstatus.setStyle(styletextwhite);
        labelsongtitle.setText("Song: " + sharedStatus.getSongTitle());
        labelsongtitle.setStyle(styletextwhite);
        labelmidifile.setText("   Midi: " + sharedStatus.getMidiFile());
        labelmidifile.setStyle(styletextwhite);
        labelpresetfile.setText("   Preset: " + sharedStatus.getPresetFile());
        labelpresetfile.setStyle(styletextwhite);

        FlowPane panefilesSng = new FlowPane();
        panefilesSng.setHgap(20);
        panefilesSng.getChildren().add(labelsongtitle);
        panefilesSng.getChildren().add(labelmidifile);
        panefilesSng.getChildren().add(labelpresetfile);

        VBox vboxstatusLeftSng = new VBox();
        vboxstatusLeftSng.setMinWidth(xstatusleft);
        vboxstatusLeftSng.getChildren().add(labelstatusSng);

        // Assemble Status BorderPane View
        borderStatusSng.setLeft(vboxstatusLeftSng);
        borderStatusSng.setCenter(panefilesSng);
        borderStatusSng.setStyle(bgheadercolor);

        // Show Left Pane: MIDI Song List
        presetchooser = new Button("...");
        midichooser = new Button("...");

        ObservableList<String> songdata = FXCollections.observableArrayList();
        ListView<String> songlistView = new ListView<>(songdata);
        songlistView.setPrefWidth(xsonglist);
        songlistView.setPrefHeight(ysonglist);
        songlistView.setStyle("-fx-control-inner-background: #E7ECEC;");

        ArrayList<MidiSong> midiSongs = dosongs.getSongs();
        for (int idx = 0; idx < dosongs.getSongListSize(); idx++) {
            MidiSong midiSong = dosongs.getSong(idx);
            String name = midiSong.getSongTitle();
            songlistView.getItems().add(name);
        }
        Platform.runLater(() -> {
            songlistView.requestFocus();

            MidiSong midiSong = dosongs.getSong(0);
            txtSongTitle.setText(midiSong.getSongTitle());
            txtSmfFile.setText(midiSong.getMidiFile());
            txtPresetFile.setText(midiSong.getPresetFile());
            txtBass.setText(Integer.toString(midiSong.getTrackBass()));
            txtLower.setText(Integer.toString(midiSong.getTrackLower()));
            txtUpper.setText(Integer.toString(midiSong.getTrackUpper()));
            txtTimeSig.setText(midiSong.getTimeSig());

            sharedStatus.setSongTitle(midiSong.getSongTitle());
            sharedStatus.setMidiFile(midiSong.getMidiFile());
            sharedStatus.setPresetFile(midiSong.getPresetFile());

            // Preset Time Signature for correct Bar Time Display
            sharedStatus.setTimeSig(midiSong.getTimeSig());

            songFile = txtSmfFile.getText();

        });
        VBox vboxList = new VBox(songlistView);

        songlistView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String old_val, String new_val) -> {
                    String selectedItem = songlistView.getSelectionModel().getSelectedItem();
                    idxSongList = songlistView.getSelectionModel().getSelectedIndex();

                    MidiSong midiSong = dosongs.getSong(idxSongList);
                    sharedStatus.setCurrentSong(midiSong);

                    txtSongTitle.setText(midiSong.getSongTitle());
                    txtSmfFile.setText(midiSong.getMidiFile());
                    txtPresetFile.setText(midiSong.getPresetFile());
                    txtPresetSaveAsFile.setText("");

                    txtBass.setText(Integer.toString(midiSong.getTrackBass()));
                    txtLower.setText(Integer.toString(midiSong.getTrackLower()));
                    txtUpper.setText(Integer.toString(midiSong.getTrackUpper()));
                    txtTimeSig.setText(midiSong.getTimeSig());

                    MidiModules midimodule = new MidiModules();
                    cursongmoduleidx = midiSong.getModuleIdx();
                    lblCurSongMidiModule.setText(midimodule.getModuleName(cursongmoduleidx));

                    sharedStatus.setSongTitle(midiSong.getSongTitle());
                    sharedStatus.setMidiFile(midiSong.getMidiFile());
                    sharedStatus.setPresetFile(midiSong.getPresetFile());

                    // Preset Time Signature for correct Bar Time Display
                    sharedStatus.setTimeSig(midiSong.getTimeSig());

                    // Enable Preset File Save As text entry
                    txtSongTitle.setDisable(true);
                    txtPresetFile.setDisable(true);
                    txtSmfFile.setDisable(true);
                    txtBass.setDisable(true);
                    txtLower.setDisable(true);
                    txtUpper.setDisable(true);
                    txtTimeSig.setDisable(true);
                    txtPresetSaveAsFile.setDisable(true);
                    presetchooser.setDisable(true);
                    midichooser.setDisable(true);

                    txtInstrumentList.setText("Play Song to retrieve Song MIDI Instruments.\nSequencer mutes by Track not Channel Number!\nUse number 0 for Tracks not too mute.");

                    songTitle = midiSong.getSongTitle();
                    songFile = midiSong.getMidiFile();

                    buttonedit.setDisable(false);
                    buttonnew.setDisable(false);
                    buttondelete.setDisable(false);

                    buttonupdate.setDisable(true);

                    labelstatusSng.setText(" Status: Selected song " + songTitle);
                    labelstatusSng.setStyle(styletext);
                });
        songlistView.getSelectionModel().selectFirst();
        songlistView.setStyle(styletext);

        Button buttonpreset = new Button("Edit Song Presets");
        buttonpreset.setStyle(selectcolorOff);
        buttonpreset.setPrefSize(xbutton, ybutton);
        buttonpreset.setOnAction(event -> {

            // Only allow edits on connect sound module
            if (cursongmoduleidx != sharedStatus.getModuleidx()) {
                MidiModules midimodule = new MidiModules();
                labelstatusSng.setText(" Status: To edit Presets, connect module " + midimodule.getModuleName(cursongmoduleidx));
                labelstatusSng.setStyle(styletextred);

                System.out.println("To edit Presets, connect sound module " + midimodule.getModuleName(cursongmoduleidx));
                return;
            }

            // For newly selected Song, change to the first Preset and 16 Channels
            // Abort if error loading Preset file
            presetFile = txtPresetFile.getText();
            if (!dopresets.loadMidiPresets(presetFile))  {
                labelstatusSng.setText(" Status: Error loading preset file " + presetFile);
                labelstatusSng.setStyle(styletextred);

                System.out.println("Fatal Error: Unable to load preset file " + presetFile);
                try {
                    wait(10000);
                }
                catch(Exception exception) {}
                System.exit(-1);
            }

            for (int idx = 0; idx < 16; idx++) {
                midiPreset = dopresets.getPreset(idx);

                String strName = Integer.toString(idx + 1).concat(":").concat(midiPreset.getPatchName());
                //presetListView.getItems().set(idx, strName);
                sharedStatus.getPresetListView().getItems().set(idx, strName);
            }
            sharedStatus.getPresetCombo().getSelectionModel().select(0);
            sharedStatus.getPresetListView().refresh();

            // Switch to Presets Scene, and initialize appropriately, e.g. ensure Song Details is updated and Save Button is off
            Button buttonPresetSceneInit = sharedStatus.getButtonPresetSceneInit();
            buttonPresetSceneInit.fire();

            System.out.println("Main: Changing to Presets Scene " + sharedStatus.getPresetsScene().toString());
            stage.setScene(sharedStatus.getPresetsScene());

            try {
                Thread.sleep(250);
            } catch (Exception ex) {
            }

        });
        VBox vboxbutpreset = new VBox(buttonpreset);

        VBox vboxLeftS = new VBox();
        vboxLeftS.setSpacing(10);
        vboxLeftS.setPadding(new Insets(10, 5, 5,5));
        vboxLeftS.getChildren().add(vboxList);
        vboxLeftS.getChildren().add(vboxbutpreset);

        presetchooser.setPrefSize(xfileselect, yfileselect);
        presetchooser.setStyle("-fx-font-size: 15; ");
        presetchooser.setDisable(true);
        presetchooser.setOnAction(e -> {
            fileChooserPreset.setInitialDirectory(new File(sharedStatus.getMIDDirectory()));
            fileChooserPreset.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Preset Files", "*.pre")
            );
            File selectedFile = fileChooserPreset.showOpenDialog(stage);
            if (selectedFile != null)
                txtPresetFile.setText(selectedFile.getName());
        });

        midichooser.setPrefSize(xfileselect, yfileselect);
        midichooser.setStyle("-fx-font-size: 15; ");
        midichooser.setDisable(true);
        midichooser.setOnAction(e -> {
            fileChooserMidi.setInitialDirectory(new File(sharedStatus.getMIDDirectory()));
            fileChooserMidi.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("MIDI Files", "*.mid")
            );
            File selectedFile = fileChooserMidi.showOpenDialog(stage);
            if (selectedFile != null)
                txtSmfFile.setText(selectedFile.getName());
        });

        // Song Details view, input and edit screen
        Label lblsong = new Label("Song Title:");
        lblsong.setStyle(styletextwhite);
        txtSongTitle.setStyle(styletext);
        //**txtSongTitle.setStyle("-fx-opacity: 0.75;");
        txtSongTitle.setDisable(true);
        txtSongTitle.setMaxWidth(250);
        txtSongTitle.textProperty().addListener(event -> {
            String regexsong = "^[A-Za-z0-9 ]{3,25}$";
            txtSongTitle.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                        (txtSongTitle.getText().isEmpty() ||
                            !txtSongTitle.getText().matches(regexsong))
            );
        });
        txtSongTitle.setMinHeight(30.0);
        txtSongTitle.setPromptText("Enter song title (required).");
        txtSongTitle.setPrefColumnCount(20);
        txtSongTitle.setTooltip(new Tooltip(
                "Item name (5 to 25 Alphanum chars length)"));

        Label lblpreset = new Label("Preset File:");
        lblpreset.setStyle(styletextwhite);
        txtPresetFile.setStyle(styletext);
        //**txtPresetFile.setStyle("-fx-opacity: 0.75;");
        txtPresetFile.setDisable(true);
        txtPresetFile.textProperty().addListener(event -> {
            String regexfile = "^[A-Za-z0-9]{3,12}[.]{1}[A-Za-z]{3}$";
            txtPresetFile.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                        (txtPresetFile.getText().isEmpty() ||
                            !txtPresetFile.getText().matches(regexfile))
            );
        });
        txtPresetFile.setMinHeight(30.0);
        txtPresetFile.setPromptText("Enter Preset file (required).");
        txtPresetFile.setPrefColumnCount(10);
        txtPresetFile.setTooltip(new Tooltip(
                "Item name (Name in 3-12 Alphanum format)"));

        Label lblsmf = new Label("MIDI File:");
        lblsmf.setStyle(styletextwhite);
        txtSmfFile.setStyle(styletext);
        //**txtSmfFile.setStyle("-fx-opacity: 0.75;");
        txtSmfFile.setDisable(true);
        txtSmfFile.textProperty().addListener(event -> {
            String regexfile = "^[A-Za-z0-9]{3,12}[.]{1}[A-Za-z]{3}$";
            txtSmfFile.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                        (txtSmfFile.getText().isEmpty() ||
                            !txtSmfFile.getText().matches(regexfile))
            );
        });
        txtSmfFile.setMinHeight(30.0);
        txtSmfFile.setPromptText("Enter MIDI file (required).");
        txtSmfFile.setPrefColumnCount(10);
        txtSmfFile.setTooltip(new Tooltip(
                "Item name (Name in 3-12 Alphanum format)"));

        Label lblpresetsaveas = new Label("Preset As:");
        lblpresetsaveas.setStyle(styletextwhite);
        txtPresetSaveAsFile.setStyle(styletext);
        //**txtPresetSaveAsFile.setStyle("-fx-opacity: 0.75;");
        txtPresetSaveAsFile.setDisable(true);
        //**txtPresetSaveAsFile.setEditable(false);
        //**txtPresetSaveAsFile.setMouseTransparent(true);
        //**txtPresetSaveAsFile.setFocusTraversable(false);
        txtPresetSaveAsFile.textProperty().addListener(event -> {
            String regexfile = "^[A-Za-z0-9]{3,12}[.]{1}[A-Za-z]{3}$";
            txtPresetSaveAsFile.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                    (txtPresetSaveAsFile.getText().isEmpty() ||
                            !txtPresetSaveAsFile.getText().matches(regexfile) ||
                            (txtPresetSaveAsFile.getText().equals(txtPresetFile.getText()))
                    )
            );
        });
        txtPresetSaveAsFile.setMinHeight(30.0);
        txtPresetSaveAsFile.setPromptText("New Preset File (required for New)");
        txtPresetSaveAsFile.setPrefColumnCount(10);
        txtPresetSaveAsFile.setTooltip(new Tooltip(
                "Save as Preset name (Name in 3-12 Alphanum format)"));

        Label lblCurMidiModule = new Label("Module:");
        lblCurMidiModule.setStyle(styletextwhite);
        lblCurSongMidiModule.setStyle(styletextwhite);

        buttonupdate.setDisable(true);
        buttonupdate.setStyle(btnMenuSaveOn);
        //BooleanBinding booleanBind = txtSongTitle.textProperty().isEmpty()
        //        .or(txtPresetFile.textProperty().isEmpty())
        //        .or(txtSmfFile.textProperty().isEmpty())
        //        .or(txtBass.textProperty().isEmpty())
        //        .or(txtLower.textProperty().isEmpty())
        //        .or(txtUpper.textProperty().isEmpty());
        //buttonupdate.disableProperty().bind(booleanBind);
        buttonupdate.setOnAction(event -> {
            ObservableList selectedIndices = songlistView.getSelectionModel().getSelectedIndices();

            for(Object o : selectedIndices) {
                //System.out.println("Main: o = " + o + " (" + o.getClass() + ")");
                int idx = Integer.parseInt(o.toString());

                // Proceed to updated Song List save only if both files exist
                int fileexistcnt = 0;

                File mfile = new File(sharedStatus.getMIDDirectory() + txtSmfFile.getText());
                if (!mfile.exists()) {
                    labelstatusSng.setText(" Status: MIDI file " + txtSmfFile.getText() + " does not exist!");
                    fileexistcnt++;
                }
                mfile = new File(sharedStatus.getMIDDirectory() + txtPresetFile.getText());
                if (!mfile.exists()) {
                    labelstatusSng.setText(" Status: Preset file " + txtPresetFile.getText() + " does not exist!");
                    fileexistcnt++;
                }

                // Save the Songs file if valid entries for MIDI and Preset files
                if (fileexistcnt == 0) {

                    if (bnewSong) {
                        System.out.println("Main: Creating new midi files for " + txtSmfFile.getText());

                        // Never overwrite defaultgm.pre. System needs to boot.
                        if (txtPresetSaveAsFile.getText().equals("defaultgm.pre")) {
                            System.err.println("### Main Error: Required file defaultgm.pre does not exist.");
                            return;
                        }

                        // Create a new MIDI Preset file based on source Preset file selected
                        if (!dosongs.copyFile(txtPresetFile.getText(), txtPresetSaveAsFile.getText(), true))
                            return;
                        System.out.println("Main: Created new preset file " + txtPresetSaveAsFile.getText() + " based on " + txtPresetFile.getText());

                        MidiSong midiSong = new MidiSong();
                        //midiSong.setSongId(dosongs.sizeSongs());
                        midiSong.setSongTitle(txtSongTitle.getText());
                        midiSong.setMidiFile(txtSmfFile.getText());
                        midiSong.setPresetFile(txtPresetSaveAsFile.getText());
                        midiSong.setTrackBass(Integer.parseInt(txtBass.getText()));
                        midiSong.setTrackLower(Integer.parseInt(txtLower.getText()));
                        midiSong.setTrackUpper(Integer.parseInt(txtUpper.getText()));
                        midiSong.setModuleIdx(sharedStatus.getModuleidx());
                        midiSong.setTimeSig(txtTimeSig.getText());

                        // Preset Time Signature for correct Bar Time Display
                        sharedStatus.setTimeSig(txtTimeSig.getText());

                        midiSongs.add(midiSong);
                        songlistView.getItems().add(midiSong.getSongTitle());

                        if (!dosongs.saveSongs(false)) {
                            labelstatusSng.setText(" Status: Error Saving New Song " + txtSongTitle.getText() + ". Check Song List File!");
                            labelstatusSng.setStyle(styletextred);
                            return;
                        }
                        songlistView.refresh();

                        bnewSong = false;

                        // Disable Preset File Save As text entry after save
                        txtPresetSaveAsFile.setDisable(true);
                        //**txtPresetSaveAsFile.setMouseTransparent(true);
                        //**txtPresetSaveAsFile.setFocusTraversable(false);

                        labelstatusSng.setText(" Status: Saved New Song " + txtSongTitle.getText());
                    }
                    else {
                        MidiSong midiSong = dosongs.getSong(idx);
                        midiSong.setSongTitle(txtSongTitle.getText());
                        midiSong.setMidiFile(txtSmfFile.getText());
                        midiSong.setPresetFile(txtPresetFile.getText());
                        midiSong.setTrackBass(Integer.parseInt(txtBass.getText()));
                        midiSong.setTrackLower(Integer.parseInt(txtLower.getText()));
                        midiSong.setTrackUpper(Integer.parseInt(txtUpper.getText()));
                        midiSong.setModuleIdx(sharedStatus.getModuleidx());
                        midiSong.setTimeSig(txtTimeSig.getText());
                        midiSong.setSongType(0);

                        midiSongs.set(idx, midiSong);
                        songlistView.getItems().set(idx, midiSong.getSongTitle());
                        songlistView.refresh();
                        songlistView.scrollTo(songlistView.getItems().size());

                        dosongs.saveSongs(false);

                        labelstatusSng.setText(" Status: Updated Song " +  txtSongTitle.getText());
                    }
                }
            }

            // Prevent repeat saves or new entries
            buttonupdate.setDisable(true);

            // Default the "Saved As" new file name after save
            txtPresetSaveAsFile.setPromptText("New Preset File (required for New)");

        });

        // Get Song Bass, Lower and Upper Channel override from User
        Label lblSongChannels = new Label("Mute MIDI Track Numbers:");
        lblSongChannels.setStyle(styletextwhite);

        Label lblBass = new Label("Bass:  ");
        lblBass.setStyle(styletextwhite);
        txtBass = new TextField(Integer.toString(sharedStatus.getBassCHAN()));
        txtBass.setStyle(styletext);
        txtBass.setDisable(true);
        txtBass.setMaxWidth(xmute);
        txtBass.textProperty().addListener(event -> {
            String regexfile = "^[0-9]{1,2}$";
            txtBass.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                    (txtBass.getText().isEmpty() ||
                            !txtBass.getText().matches(regexfile)
                    ));
        });

        Label lblLower = new Label("   Lower:  ");
        lblLower.setStyle(styletextwhite);
        txtLower = new TextField(Integer.toString(sharedStatus.getLower1CHAN()));
        txtLower.setStyle(styletext);
        txtLower.setDisable(true);
        txtLower.setMaxWidth(xmute);
        txtLower.textProperty().addListener(event -> {
            String regexfile = "^[0-9]{1,2}$";
            txtLower.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                    (txtLower.getText().isEmpty() ||
                            !txtLower.getText().matches(regexfile)
                    ));
        });

        Label lblUpper = new Label("   Upper:  ");
        lblUpper.setStyle(styletextwhite);
        txtUpper = new TextField(Integer.toString(sharedStatus.getUpper1CHAN()));
        txtUpper.setStyle(styletext);
        txtUpper.setDisable(true);
        txtUpper.setMaxWidth(xmute);
        txtUpper.textProperty().addListener(event -> {
            String regexfile = "^[0-9]{1,2}$";
            txtUpper.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                    (txtUpper.getText().isEmpty() ||
                            !txtUpper.getText().matches(regexfile)
                    ));
        });

        Label lblTimeSig = new Label(" Signature: ");
        lblTimeSig.setStyle(styletextwhite);
        txtTimeSig.setStyle(styletext);
        txtTimeSig.setDisable(true);
        txtTimeSig.setMaxWidth(xmute);
        txtTimeSig.textProperty().addListener(event -> {
            String regexfile = "^[0-9]{1}/[0-9]{1}$";
            txtTimeSig.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                    (txtTimeSig.getText().isEmpty() ||
                            !txtTimeSig.getText().matches(regexfile)
                    ));
        });

        HBox hboxChan = new HBox();
        hboxChan.setPadding(new Insets(0, 10, 00, 0));
        hboxChan.getChildren().add(lblBass);
        hboxChan.getChildren().add(txtBass);
        hboxChan.getChildren().add(lblLower);
        hboxChan.getChildren().add(txtLower);
        hboxChan.getChildren().add(lblUpper);
        hboxChan.getChildren().add(txtUpper);
        hboxChan.getChildren().add(lblTimeSig);
        hboxChan.getChildren().add(txtTimeSig);

        // Song New/Update, Delete and Save Buttons
        buttonedit.setText("Edit");
        buttonedit.setStyle(selectcolorOff);
        buttonedit.setPrefSize(xsmallbtn, ysmallbtn);
        buttonedit.setOnAction(event -> {
            ObservableList selectedIndices = songlistView.getSelectionModel().getSelectedIndices();

            for(Object o : selectedIndices) {
                System.out.println("Main: o = " + o + " (" + o.getClass() + ")");
                int idx = Integer.parseInt(o.toString());

                MidiSong midiSong = midiSongs.get(idx);
                String songtitle = midiSong.getSongTitle();

                // Never edit initial default organ preset files.
                if ((idx == 0) || (idx == 1)|| (idx == 2)) {
                    labelstatusSng.setText(" Status: Editing Preset Song " + midiSong.getSongTitle() + " files not allowed");
                    labelstatusSng.setStyle(styletextred);

                    buttonedit.setDisable(true);
                    buttondelete.setDisable(true);

                    return;
                }
            }

            // Enable Preset File Save As text entry
            txtSongTitle.setDisable(false);
            txtPresetFile.setDisable(false);
            txtSmfFile.setDisable(false);
            txtBass.setDisable(false);
            txtLower.setDisable(false);
            txtUpper.setDisable(false);
            txtTimeSig.setDisable(false);
            txtPresetSaveAsFile.setDisable(true);
            presetchooser.setDisable(false);
            midichooser.setDisable(false);

            buttonedit.setDisable(true);
            buttonnew.setDisable(true);
            buttondelete.setDisable(true);
            buttonupdate.setDisable(false);

            labelstatusSng.setText(" Status: Editing details for Song " + txtSongTitle.getText());
        });

        // Song New/Update, Delete and Save Buttons

        // New Song
        buttonnew.setText("New");
        buttonnew.setStyle(selectcolorOff);
        buttonnew.setPrefSize(xsmallbtn, ysmallbtn);
        buttonnew.setOnAction(event -> {

            MidiSong midiSong = dosongs.getSong(0);
            txtSongTitle.setText("New"); //midiSong.getSongTitle());
            txtSmfFile.setText(midiSong.getMidiFile());
            txtPresetFile.setText(midiSong.getPresetFile());

            bnewSong = true;

            // Enable Preset File Save As text entry
            //**txtPresetSaveAsFile.setEditable(true);
            //**txtPresetSaveAsFile.setMouseTransparent(false);
            //**txtPresetSaveAsFile.setFocusTraversable(true);
            txtPresetSaveAsFile.setDisable(false);

            // Enable Preset File Save As text entry
            txtSongTitle.setDisable(false);
            txtPresetFile.setDisable(false);
            txtSmfFile.setDisable(false);
            txtBass.setDisable(false);
            txtLower.setDisable(false);
            txtUpper.setDisable(false);
            txtTimeSig.setDisable(false);
            presetchooser.setDisable(false);
            midichooser.setDisable(false);

            txtInstrumentList.setText("Play Song to retrieve MIDI Instrument List.\n Sequencer mutes by Track not Channel Number!");

            buttonedit.setDisable(true);
            buttonnew.setDisable(true);
            buttondelete.setDisable(true);
            buttonupdate.setDisable(false);

            lblCurSongMidiModule.setText(sharedStatus.getModuleName(sharedStatus.getModuleidx()));

            labelstatusSng.setText(" Status: Creating New Song " + txtSongTitle.getText());
        });

        // Delete Song
        buttondelete.setText("Delete");
        buttondelete.setStyle(selectcolorOff);
        buttondelete.setPrefSize(xsmallbtn, ysmallbtn);
        buttondelete.setOnAction(event -> {
            ObservableList selectedIndices = songlistView.getSelectionModel().getSelectedIndices();

            buttonedit.setDisable(true);
            buttonnew.setDisable(true);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete selected Song");
            alert.setContentText("Proceed with delete?");
            ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(noButton, okButton);
            alert.showAndWait().ifPresent(type -> {
                if (type == okButton) {
                    deleteconfirmed = true;
                    labelstatusSng.setText(" Status: Deleting current Song");
                }
                else {
                    deleteconfirmed = false;
                    labelstatusSng.setText(" Status: Abort delete current Song");
                    return;
                }
            });

            if (deleteconfirmed) {
                for(Object o : selectedIndices) {
                    System.out.println("Main: o = " + o + " (" + o.getClass() + ")");
                    int idx = Integer.parseInt(o.toString());

                    MidiSong midiSong = midiSongs.get(idx);
                    String songtitle = midiSong.getSongTitle();

                    // Never delete the initial default organ preset files (type 1)
                    if (midiSong.getSongType() == 1) {
                        labelstatusSng.setText(" Status: Delete not allowed for file type of Song " + songtitle);
                        labelstatusSng.setStyle(styletextred);

                        // Enable user save of UI deleted Song form List
                        buttonupdate.setDisable(true);
                        buttonedit.setDisable(true);
                        buttondelete.setDisable(true);

                        return;
                    }

                    midiSongs.remove(idx);

                    songlistView.getItems().remove(idx);
                    songlistView.refresh();

                    labelstatusSng.setText(" Status: Deleted Song " + songtitle);

                    // Enable user save of UI deleted Song form List
                    buttonupdate.setDisable(false);
                }
            }
        });

        // Prepare for Demo Play with options, defaulting to demo play
        radioOriginal.setSelected(true);
        radioOriginal.setStyle(styletextwhite);

        Button buttondemo = new Button("Play Song");
        buttondemo.setStyle(btnplayOff);
        buttondemo.setPrefSize(xsmallbtn * 2, ysmallbtn);
        buttondemo.setOnAction(e -> {
            try {
                // Only play Song if the selected moduleidx is the same as moduleidx on the Song
                if (sharedStatus.getModuleidx() != dosongs.getSong(idxSongList).getModuleIdx()) {
                    bplaying = false;

                    labelstatusSng.setText(" Status: To play this Song, connect module " + sharedStatus.getModuleName(dosongs.getSong(idxSongList).getModuleIdx()));
                    labelstatusSng.setStyle(styletextred);

                    return;
                }
                else
                    labelstatusSng.setStyle(styletext);

                if (!bplaying) {
                    bplaying = true;
                    boolean bsettimer = false;

                    // Reload the Preset file for current Song in case it has changed
                    presetFile = sharedStatus.getPresetFile();
                    if (!dopresets.loadMidiPresets(presetFile)) {
                        labelstatusSng.setText(" Status: Error loading preset file " + presetFile);
                        labelstatusSng.setStyle(styletextred);

                        try {
                            wait(10000);
                        }
                        catch(Exception exception) {}
                        System.exit(-1);
                    }

                    buttondemo.setText("Stop Play");
                    buttondemo.setStyle(btnplayOn);

                    playmidifile = PlayMidi.getInstance();
                    if (radioOriginal.isSelected()) {
                        if (!playmidifile.startMidiPlay(dosongs.getSong(idxSongList), dopresets, 1)) {
                            labelstatusSng.setText(" Status: " + sharedStatus.getStatusText());
                            bplaying = false;
                        } else {
                            bsettimer = true;
                            labelstatusSng.setText(" Status: Original demo play " + txtSmfFile.getText());
                        }
                    }
                    else if (radioPresets.isSelected()) {
                        if (!playmidifile.startMidiPlay(dosongs.getSong(idxSongList), dopresets, 2)) {
                            labelstatusSng.setText(" Status: " + sharedStatus.getStatusText());
                            bplaying = false;
                        } else {
                            bsettimer = true;
                            labelstatusSng.setText(" Status: Preset demo play " + txtSmfFile.getText());
                        }
                    }
                    else if (radioLive.isSelected()) {
                        // Start playing and apply then channel mote. Channel mute requires data to be loaded
                        // into the sequencer first: https://docs.oracle.com/javase/tutorial/sound/MIDI-seq-adv.html
                        playmidifile.startMidiPlay(dosongs.getSong(idxSongList), dopresets, 3);

                        bsettimer = true;

                        labelstatusSng.setText(" Status: Live preset play " + txtSmfFile.getText());
                    }
                    else {
                        labelstatusSng.setText(" Status: Invalid Live play option!");
                    }

                    // Song Play Repeating Timer: Collects Beat Timer and Play Status every 250ms
                    if (bsettimer) {
                        Timer songPlayTimer = new Timer();
                        songPlayTimer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                // Check if Play stopped and reset Button Status
                                if (!playmidifile.isMidiRunning()) {

                                    Platform.runLater(() -> {
                                        bplaying = false;

                                        buttondemo.setText("Play Song");
                                        buttondemo.setStyle(btnplayOff);
                                        labelstatusSng.setText(" Status: Song Play Complete " + playmidifile.getSequencerTickPosition());
                                    });
                                    songPlayTimer.cancel();
                                    return;
                                }

                                Platform.runLater(() -> {
                                    labelstatusSng.setText(" Status: Bar " + playmidifile.getSequencerBeat());
                                });

                                //System.out.println("Main: Sequencer Bar.Beat " + playmidifile.getSequencerTickPosition());
                            }
                        }, 0, 100);
                    }

                }
                else {
                    buttondemo.setText("Play Song");
                    buttondemo.setStyle(btnplayOn);

                    playmidifile.stopMidiPlay(txtSmfFile.getText());

                    txtInstrumentList.setText(sharedStatus.getInstruments());

                    bplaying = false;

                    labelstatusSng.setText(" Status: Stopped Playing file " + txtSmfFile.getText());
                }
            }
            catch (Exception exception) {
                bplaying = false;

                buttondemo.setText("Play Song");
                buttondemo.setStyle(btnplayOn);

                exception.printStackTrace();
            }

            //System.out.println("Main: " + dopresets.presetString(presetIdx * 16 + channelIdx));
        });

        txtInstrumentList.setMaxWidth(450 * xmul);
        txtInstrumentList.setMaxHeight(185 * ymul);
        txtInstrumentList.setStyle(stlInstrumentList);

        VBox vboxInstrumentList = new VBox();
        VBox.setMargin(vboxInstrumentList, new Insets(10, 50, 10,10));
        vboxInstrumentList.getChildren().add(txtInstrumentList);

        // Midi Song gridPane layout
        VBox songvboxS = new VBox();

        GridPane songgrid = new GridPane();
        songgrid.setAlignment(Pos.TOP_LEFT);
        songgrid.setHgap(10);
        songgrid.setVgap(10);
        songgrid.setPadding(new Insets(10, 25, 10, 10));
        songgrid.add(lblsong, 1, 1);
        songgrid.add(txtSongTitle, 2, 1);
        songgrid.add(lblsmf, 1, 2);
        songgrid.add(txtSmfFile, 2, 2);
        songgrid.add(midichooser, 3, 2);
        songgrid.add(lblpreset, 1, 3);
        songgrid.add(txtPresetFile, 2, 3);
        songgrid.add(presetchooser, 3, 3);
        songgrid.add(lblpresetsaveas, 1, 4);
        songgrid.add(txtPresetSaveAsFile, 2, 4);
        songgrid.add(lblCurMidiModule, 1, 5);
        songgrid.add(lblCurSongMidiModule, 2, 5);

        songgrid.add(lblSongChannels, 1, 6, 2, 1);
        songgrid.add(hboxChan, 1, 7, 3, 1);

        songvboxS.getChildren().add(songgrid);

        HBox songbuttons = new HBox();
        songbuttons.setPadding(new Insets(10, 50, 10,25));

        HBox songbuttons1 = new HBox();
        songbuttons1.setPadding(new Insets(0, 10, 0,10));
        songbuttons1.getChildren().add(buttonedit);
        HBox songbuttons2 = new HBox();
        songbuttons2.setPadding(new Insets(0, 10, 0,10));
        songbuttons2.getChildren().add(buttonnew);
        HBox songbuttons3 = new HBox();
        songbuttons3.setPadding(new Insets(0, 10, 0,10));
        songbuttons3.getChildren().add(buttondelete);
        HBox songbuttons4 = new HBox();
        songbuttons4.setPadding(new Insets(0, 10, 0,10));
        songbuttons4.getChildren().add(buttondemo);

        // Do the Demo style radio buttons

        Label labelDemoType = new Label("Sequencer Mode:  ");
        labelDemoType.setStyle(styletextwhite);

        ToggleGroup radioDemoTypeGroup = new ToggleGroup();
        radioOriginal.setSelected(true);
        radioPresets = new RadioButton("With Presets    ");
        radioPresets.setStyle(styletextwhite);
        radioLive = new RadioButton("Backing Only");
        radioLive.setStyle(styletextwhite);
        radioOriginal.setToggleGroup(radioDemoTypeGroup);
        radioPresets.setToggleGroup(radioDemoTypeGroup);
        radioLive.setToggleGroup(radioDemoTypeGroup);

        HBox hboxPlayMode = new HBox(labelDemoType, radioOriginal, radioPresets, radioLive);
        hboxPlayMode.setPadding(new Insets(10, 0, 0,10));

        songbuttons.getChildren().addAll(songbuttons1, songbuttons2, songbuttons3, songbuttons4);
        songvboxS.getChildren().addAll(songbuttons, hboxPlayMode, vboxInstrumentList);

        HBox boxstatussong = new HBox();
        boxstatussong.getChildren().add(labelstatusSng);
        boxstatussong.setStyle(bgheadercolor);

        // Add Song, Bank and Font Select to Top Line - Not in use yet
        VBox vboxRight = new VBox();

        Button cbutton1 = new Button("Mute");

        GridPane gridChannels = new GridPane();
        gridChannels.setHgap(5);
        gridChannels.setVgap(10);
        gridChannels.add(cbutton1, 0, 0, 1, 1);
        vboxRight.getChildren().add(gridChannels);
        vboxRight.setPadding(new Insets(20, 10, 10,20 ));

        // Assemble the BorderPane View
        borderPaneSng.setTop(borderPaneTopSng);
        borderPaneSng.setLeft(vboxLeftS);
        borderPaneSng.setCenter(songvboxS);
        //borderPaneSng.setRight(vboxRight);
        //borderPaneS.setBottom(borderStatusS);
        borderPaneSng.setBottom(boxstatussong);

        // Prepare background Image
        //try {
        //    FileInputStream input = new FileInputStream(sharedStatus.getCFGDirectory() + "backimage.png");
        //    Image image = new Image(input);
        //    BackgroundImage backgroundimage = new BackgroundImage(image,
        //            BackgroundRepeat.NO_REPEAT,
        //            BackgroundRepeat.NO_REPEAT,
        //            BackgroundPosition.DEFAULT,
        //            BackgroundSize.DEFAULT);
        //
        //    // Create and set background
        //    Background background = new Background(backgroundimage);
        //    borderPaneSng.setBackground(background);
        //}
        //catch(FileNotFoundException ex) {
        //    System.err.println("Background image not found! ");
        //}

        return borderPaneSng;
    }

    public static void main(String[] args) {

        // Log System Out and Err to a file
        logSystemToFile();

        AppConfig appconfig = AppConfig.getInstance();
        if (!appconfig.getApplicationLock()) {
            System.err.println("Exiting as instance of AMIDIFX is already running!");

            System.exit(-1);
        }
        launch(args);
    }
}
