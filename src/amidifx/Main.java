package amidifx;

import amidifx.models.*;
import amidifx.scenes.OrganScene;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import static amidifx.utils.Logger.logSystemToFile;

@SuppressWarnings("CanBeFinal")
public class Main extends Application {

    // Button Colors
    // https://yagisanatode.com/2019/08/06/google-apps-script-hexadecimal-color-codes-for-google-docs-sheets-and-slides-standart-palette/
    final String bgpanecolor = "#999999";
    final String bgheadercolor = "#B2B5B1;";
    final String bgfootercolor = "#B2B5B1;";

    static final String btnMenuOn = "-fx-background-color: #4493C0; -fx-font-size: 15; ";
    static final String btnMenuOff = "-fx-background-color: #69A8CC; -fx-font-size: 15; ";
    static final String btnMenuSaveOn = "-fx-background-color: #DB6B6B; -fx-font-size: 15; ";

    static final String btnplayOff = "-fx-background-color: #8ED072; -fx-font-size: 15; ";
    static final String btnplayOn = "-fx-background-color: #DB6B6B; -fx-font-size: 15; ";

    final String selectcolorOff = "-fx-background-color: #69A8CC; -fx-font-size: 15; ";
    final String selectcolorOn = "-fx-background-color: #4493C0; -fx-font-size: 15; ";

    final String btnPresetOff = "-fx-background-color: #DBD06B;  -fx-font-size: 15; -fx-text";
    final String btnPresetOn = "-fx-background-color: #C3B643;  -fx-font-size: 15; ";

    final String styletext = "-fx-font-size: 15; ";

    Scene sceneOrgan, scenePresets, sceneSongs, sceneMonitor;
    MidiPatches dopatches;
    MidiPresets dopresets;
    MidiSongs dosongs;
    MidiModules midimodules;

    Label midiLayerLabel;   // Midi Channel Layer Indicator
    CheckBox[] chkBoxArray; // MIDI Out Channel Layer

    int patchIdx = 100;     // On Screen Voice Index Started
    int selpatchIdx = 0;    // Selected on Screen Voice Index
    int songIdx = 0;
    int moduleIdx = 0;
    int presetIdx = 0;
    int channelIdx = 0;
    int checkIdx = 0;

    private TableView tableSongList;
    private ObservableList songData;
    private Text statusSong;

    boolean flgDirtyPreset = false; // Track need to save changes Presets
    boolean bplaying = false;
    boolean btestnote = false;

    ListView<String> presetListView;
    String songTitle = "Organ";
    String songFile = "amloop.mid";
    String presetFile = "default.csv";
    Label labelstatus = new Label(" ");
    Label labelsongtitle = new Label(" ");
    Label labelmidifile = new Label(" ");
    Label labelpresetfile = new Label("  ");
    Label labelstatusSng = new Label(" ");

    SharedStatus sharedStatus;

    private static final String MID_DIRECTORY = "C:/amidifx/midifiles/";

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("amidifx.fxml"));
        stage.setTitle("AMIDIFX");
        stage.initStyle(StageStyle.UNDECORATED);

        // Create instance of Shared Status to report back to Scenes
        sharedStatus = SharedStatus.getInstance();

        // Prepare MIDI Player and Select Deebach Blackbox as output
        playmidifile = PlayMidi.getInstance();
        if (!playmidifile.selectMidiDevice()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("AMIDIFX Startup Error");
            alert.setHeaderText("PlayMidi Error: No MIDI Device installed. Please check!");
            Optional<ButtonType> result = alert.showAndWait();

            System.exit(-1);
        }
        else {
            sharedStatus.setStatusText("PlayMidi: MIDI Device is " + sharedStatus.getSynth());
        }

        // Prepare the Channel Program and Effects tracking list
        playmidifile.initCurPresetList();

        // Load MIDI Sound Module List on start up
        midimodules = new MidiModules();

        // Load Song List. If not exists, abort load AMIDIFX
        dosongs = new MidiSongs();
        if (!dosongs.fileExist("songs.csv")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("AMIDIFX Startup Error");
            alert.setHeaderText("Song Index file " + MID_DIRECTORY + "songs.csv not found!");
            Optional<ButtonType> result = alert.showAndWait();

            System.exit(-1);
        }
        dosongs.makeMidiSongs();

        // Load MIDI Patch files on start up based on detected and preferred sound module
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

        // *** Prepare the Organ, Song and Preset Screens
        File fstyle = new File("src/amidifx/style.css");

        sceneSongs = new Scene(createSongScene(stage), 1024, 600);
        sceneSongs.getStylesheets().clear();
        sceneSongs.getStylesheets().add("file:///" + fstyle.getAbsolutePath().replace("\\", "/"));
        sharedStatus.setSongsScene(sceneSongs);

        scenePresets = new Scene(createPresetScene(stage), 1024, 600);
        scenePresets.getStylesheets().clear();
        scenePresets.getStylesheets().add("file:///" + fstyle.getAbsolutePath().replace("\\", "/"));
        sharedStatus.setPresetsScene(scenePresets);

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 3);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 3);

        //WelcomeScene welcomeScene = new WelcomeScene(stage, sceneSongs);
        //stage.setScene(welcomeScene.getScene());

        OrganScene organScene = new OrganScene(stage, sceneSongs);
        stage.setScene(organScene.getScene());

        //stage.setScene(sceneSongs);
        stage.show();
    }


    /*********************************************************
     * Song Scene
     *********************************************************/

    TextField txtSongTitle = new TextField("");
    TextField txtSmfFile = new TextField("");
    TextField txtPresetFile = new TextField("");
    TextField txtPresetSaveAsFile = new TextField("");
    TextField txtBass = new TextField("11");
    TextField txtLower = new TextField("12");
    TextField txtUpper = new TextField("14");
    TextField txtTimeSig = new TextField("4/4");
    TextArea txtInstrumentList = new TextArea("Play Song to update Song MIDI Track Instruments");

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
        borderPaneSng.setStyle("-fx-background-color: " + bgpanecolor);

        Label labelstatusSng = new Label(" Status: Ready");

        FileChooser fileChooserPreset = new FileChooser();
        FileChooser fileChooserMidi = new FileChooser();

        // *** Create Top Navigation Bar

        Button buttonsc1 = new Button("Perform");
        buttonsc1.setStyle(btnMenuOff);
        //buttonsc1.setOnAction(e -> stage.setScene(sceneOrgan));
        buttonsc1.setOnAction(e -> {
            System.out.println(("Main: Changing to Organ Scene: " + sharedStatus.getOrganScene().toString()));
            stage.setScene(sharedStatus.getOrganScene());
            try {
                Thread.sleep(250);
            } catch (Exception ex) {
                System.err.println("### nMain: Unable to set Organ Scene!");
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
            System.out.println(("Main: Changing to Presets Scene: " + sharedStatus.getPresetsScene().toString()));
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
            playmidifile.stopMidiPlay("End Play");
            Platform.exit();
        });

        ToolBar toolbarLeft = new ToolBar(buttonsc1, buttonsc2, buttonsc3);
        toolbarLeft.setStyle("-fx-background-color: #B2B5B1; ");
        toolbarLeft.setMinWidth(225);

        Label lbltitle1 = new Label("AMIDIFX Sound Module Controller");
        lbltitle1.setStyle(styletext);
        HBox hboxTitle = new HBox();
        hboxTitle.setPadding(new Insets(10, 10, 10,200));
        hboxTitle.getChildren().add(lbltitle1);

        ToolBar toolbarRight = new ToolBar(buttonupdate, buttonPanic, buttonExit);
        toolbarRight.setStyle("-fx-background-color: " + bgheadercolor);
        toolbarRight.setMinWidth(150);

        BorderPane borderPaneTopSng = new BorderPane();
        borderPaneTopSng.setStyle("-fx-background-color: #B2B5B1; ");

        // Assemble the Menu Bar Border Pane
        borderPaneTopSng.setLeft(toolbarLeft);
        borderPaneTopSng.setCenter(hboxTitle);
        borderPaneTopSng.setRight(toolbarRight);

        // *** Start Bottom Status Bar

        BorderPane borderStatusSng = new BorderPane();
        borderStatusSng.setStyle("-fx-background-color: #999999; "); //#B2B5B1; ");

        labelstatus.setText(" Status: " + sharedStatus.getStatusText());
        labelsongtitle.setText("Song: " + sharedStatus.getSongTitle());
        labelmidifile.setText("   Midi: " + sharedStatus.getMidiFile());
        labelpresetfile.setText("   Preset: " + sharedStatus.getPresetFile());

        FlowPane panefilesSng = new FlowPane();
        panefilesSng.setHgap(20);
        panefilesSng.getChildren().add(labelsongtitle);
        panefilesSng.getChildren().add(labelmidifile);
        panefilesSng.getChildren().add(labelpresetfile);

        VBox vboxstatusLeftSng = new VBox();
        vboxstatusLeftSng.setMinWidth(400);
        vboxstatusLeftSng.getChildren().add(labelstatusSng);

        // Assemble Status BorderPane View
        borderStatusSng.setLeft(vboxstatusLeftSng);
        borderStatusSng.setCenter(panefilesSng);

        // Show Left Pane: MIDI Song List

        ObservableList<String> songdata = FXCollections.observableArrayList();
        ListView<String> songlistView = new ListView<>(songdata);
        songlistView.setPrefWidth(200);
        songlistView.setPrefHeight(550);
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
            txtBass.setText(Integer.toString(midiSong.getChanBass()));
            txtLower.setText(Integer.toString(midiSong.getChanLower()));
            txtUpper.setText(Integer.toString(midiSong.getChanUpper()));
            txtTimeSig.setText(midiSong.getTimeSig());

            songFile = txtSmfFile.getText();

        });
        VBox vboxList = new VBox(songlistView);

        songlistView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String old_val, String new_val) -> {
                    String selectedItem = songlistView.getSelectionModel().getSelectedItem();
                    idxSongList = songlistView.getSelectionModel().getSelectedIndex();

                    MidiSong midiSong = dosongs.getSong(idxSongList);
                    txtSongTitle.setText(midiSong.getSongTitle());
                    txtSmfFile.setText(midiSong.getMidiFile());
                    txtPresetFile.setText(midiSong.getPresetFile());
                    txtBass.setText(Integer.toString(midiSong.getChanBass()));
                    txtLower.setText(Integer.toString(midiSong.getChanLower()));
                    txtUpper.setText(Integer.toString(midiSong.getChanUpper()));
                    txtTimeSig.setText(midiSong.getTimeSig());

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
                    txtInstrumentList.setText("Play Song to update Song MIDI Track Instruments");

                    songTitle = midiSong.getSongTitle();
                    songFile = midiSong.getMidiFile();

                    buttonedit.setDisable(false);
                    buttonnew.setDisable(false);
                    buttondelete.setDisable(false);

                    buttonupdate.setDisable(true);

                    labelstatusSng.setText(" Status: Selected song " + songTitle);
                });
        songlistView.getSelectionModel().selectFirst();
        songlistView.setStyle(styletext);

        Button buttonpreset = new Button("Edit Presets");
        buttonpreset.setStyle(selectcolorOff);
        ////String songFile = "amloop.mid";
        buttonpreset.setPrefSize(150, 50);
        buttonpreset.setOnAction(event -> {

            presetFile = txtPresetFile.getText();
            dopresets.makeMidiPresets(presetFile);

            // For newly selected Song, change to the first Preset and 16 Channels
            for (int idx = 0; idx < 16; idx++) {
                midiPreset = dopresets.getPreset(idx);

                String strName = Integer.toString(idx + 1).concat(":").concat(midiPreset.getPatchName());
                presetListView.getItems().set(idx, strName);
            }
            presetCombo.getSelectionModel().select(0);
            presetListView.refresh();

            // Save latest Song details to shared status instance and prepare to display in Preset Scene
            sharedStatus.setPresetFile(txtPresetFile.getText());
            sharedStatus.setMidiFile(txtSmfFile.getText());
            sharedStatus.setSongTitle(txtSongTitle.getText());
            sharedStatus.setStatusText("Editing Preset File " + presetFile);

            // Prepare Status Bar for Edit Presets Scene!
            labelstatus.setText(" Status: Editing Presets for " + sharedStatus.getPresetFile());
            labelsongtitle.setText("Song: " + sharedStatus.getSongTitle());
            labelmidifile.setText("Midi: " + sharedStatus.getMidiFile());
            labelpresetfile.setText("Preset: " + sharedStatus.getPresetFile());

            stage.setScene(scenePresets);
        });
        VBox vboxbutpreset = new VBox(buttonpreset);

        VBox vboxLeftS = new VBox();
        vboxLeftS.setSpacing(10);
        vboxLeftS.setPadding(new Insets(10, 5, 5,5));
        vboxLeftS.getChildren().add(vboxList);
        vboxLeftS.getChildren().add(vboxbutpreset);

        Button presetchooser = new Button("...");
        presetchooser.setPrefSize(25, 15);
        presetchooser.setStyle("-fx-font-size: 15; ");
        presetchooser.setOnAction(e -> {
            fileChooserPreset.setInitialDirectory(new File(MID_DIRECTORY));
            fileChooserPreset.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Preset Files", "*.csv")
            );
            File selectedFile = fileChooserPreset.showOpenDialog(stage);
            if (selectedFile != null)
                txtPresetFile.setText(selectedFile.getName());
        });

        Button midichooser = new Button("...");
        midichooser.setPrefSize(25, 15);
        midichooser.setStyle("-fx-font-size: 15; ");
        midichooser.setOnAction(e -> {
            fileChooserMidi.setInitialDirectory(new File(MID_DIRECTORY));
            fileChooserMidi.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("MIDI Files", "*.mid")
            );
            File selectedFile = fileChooserMidi.showOpenDialog(stage);
            if (selectedFile != null)
                txtSmfFile.setText(selectedFile.getName());
        });

        // Song Details view, input and edit screen
        Label lblsong = new Label("Song Title:");
        lblsong.setStyle(styletext);
        txtSongTitle.setStyle(styletext);
        txtSongTitle = new TextField();
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
        txtSongTitle.setStyle("-fx-control-inner-background: #E7ECEC;");

        Label lblpreset = new Label("Preset File:");
        lblpreset.setStyle(styletext);
        txtPresetFile.setStyle(styletext);
        txtPresetFile = new TextField();
        txtPresetFile.setDisable(true);
        txtPresetFile.textProperty().addListener(event -> {
            String regexfile = "^[A-Za-z0-9]{3,8}[.]{1}[A-Za-z]{3}$";
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
                "Item name (Name in 8.3 Alphanum format)"));
        txtPresetFile.setStyle("-fx-control-inner-background: #E7ECEC;");

        Label lblsmf = new Label("MIDI File:");
        lblsmf.setStyle(styletext);
        txtSmfFile.setStyle(styletext);
        txtSmfFile = new TextField();
        txtSmfFile.setDisable(true);
        txtSmfFile.textProperty().addListener(event -> {
            String regexfile = "^[A-Za-z0-9]{3,8}[.]{1}[A-Za-z]{3}$";
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
                "Item name (Name in 8.3 Alphanum format)"));
        txtSmfFile.setStyle("-fx-control-inner-background: #E7ECEC;");

        Label lblpresetsaveas = new Label("Preset As:");
        lblpresetsaveas.setStyle(styletext);
        txtPresetSaveAsFile.setStyle(styletext);
        txtPresetSaveAsFile.setDisable(true);
        txtPresetSaveAsFile.setEditable(false);
        txtPresetSaveAsFile.setMouseTransparent(true);
        txtPresetSaveAsFile.setFocusTraversable(false);
        txtPresetSaveAsFile.textProperty().addListener(event -> {
            String regexfile = "^[A-Za-z0-9]{3,8}[.]{1}[A-Za-z]{3}$";
            txtPresetSaveAsFile.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                    (txtPresetSaveAsFile.getText().isEmpty() ||
                            !txtPresetSaveAsFile.getText().matches(regexfile))
            );
        });
        txtPresetSaveAsFile.setMinHeight(30.0);
        txtPresetSaveAsFile.setPromptText("New Preset File (required for New)");
        txtPresetSaveAsFile.setPrefColumnCount(10);
        txtPresetSaveAsFile.setTooltip(new Tooltip(
                "Save as Preset name (Name in 8.3 Alphanum format)"));
        txtPresetSaveAsFile.setStyle("-fx-control-inner-background: #E7ECEC;");

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

                File mfile = new File(MID_DIRECTORY + txtSmfFile.getText());
                if (!mfile.exists()) {
                    labelstatusSng.setText(" Status: MIDI file " + txtSmfFile.getText() + " does not exist!");
                    fileexistcnt++;
                }
                mfile = new File(MID_DIRECTORY + txtPresetFile.getText());
                if (!mfile.exists()) {
                    labelstatusSng.setText(" Status: Preset file " + txtPresetFile.getText() + " does not exist!");
                    fileexistcnt++;
                }

                // Save the Songs file if valid entries for MIDI and Preset files
                if (fileexistcnt == 0) {

                    if (bnewSong) {
                        System.out.println("Main: Creating new midi files for: " + txtSmfFile.getText());

                        // Never overwrite default.csv. System needs to boot.
                        if (txtPresetSaveAsFile.getText().equals("default.csv")) {
                            System.err.println("### Main Error: Required file default.csv does not exist. Aborting start up");
                            return;
                        }

                        // Create a new MIDI Preset file based on source Preset file selected
                        if (!dosongs.copyFile(txtPresetFile.getText(), txtPresetSaveAsFile.getText(), true))
                            return;
                        System.out.println("Main: Created new preset file: " + txtPresetSaveAsFile.getText() + " based on " + txtPresetFile.getText());

                        MidiSong midiSong = new MidiSong();
                        midiSong.setSongId(dosongs.sizeSongs());
                        midiSong.setSongTitle(txtSongTitle.getText());
                        midiSong.setMidiFile(txtSmfFile.getText());
                        midiSong.setPresetFile(txtPresetSaveAsFile.getText());
                        midiSong.setChanBass(Integer.parseInt(txtBass.getText()));
                        midiSong.setChanLower(Integer.parseInt(txtLower.getText()));
                        midiSong.setChanUpper(Integer.parseInt(txtUpper.getText()));
                        midiSong.setTimeSig(txtTimeSig.getText());

                        // Preset Time Signature for correct Bar Time Display
                        sharedStatus.setTimeSig(txtTimeSig.getText());

                        midiSongs.add(midiSong);
                        songlistView.getItems().add(midiSong.getSongTitle());
                        songlistView.refresh();

                        dosongs.saveSongs();

                        bnewSong = false;

                        // Disable Preset File Save As text entry after save
                        txtPresetSaveAsFile.setDisable(true);
                        txtPresetSaveAsFile.setMouseTransparent(true);
                        txtPresetSaveAsFile.setFocusTraversable(false);

                        labelstatusSng.setText(" Status: Saved New Song " + txtSongTitle.getText() + " -" + songIdx);
                    }
                    else {
                        songIdx = dosongs.getMidiSong(idx).getSongId();

                        MidiSong midiSong = dosongs.getSong(idx);
                        midiSong.setSongTitle(txtSongTitle.getText());
                        midiSong.setMidiFile(txtSmfFile.getText());
                        midiSong.setPresetFile(txtPresetFile.getText());
                        midiSong.setChanBass(Integer.parseInt(txtBass.getText()));
                        midiSong.setChanLower(Integer.parseInt(txtLower.getText()));
                        midiSong.setChanUpper(Integer.parseInt(txtUpper.getText()));
                        midiSong.setTimeSig(txtTimeSig.getText());

                        midiSongs.set(idx, midiSong);
                        songlistView.getItems().set(idx, midiSong.getSongTitle());
                        songlistView.refresh();

                        dosongs.saveSongs();

                        labelstatusSng.setText(" Status: Updated Song " +  txtSongTitle.getText() + " (" + songIdx + ")");
                    }
                }
            }

            buttonupdate.setDisable(true);
        });

        // Get Song Bass, Lower and Upper Channel override from User
        Label lblSongChannels = new Label("MIDI Track Number Override for Mute:");
        lblSongChannels.setStyle(styletext);

        Label lblBass = new Label("Bass:  ");
        lblBass.setStyle(styletext);
        txtBass.setStyle(styletext);
        txtBass = new TextField(Integer.toString(sharedStatus.getBassCHAN()));
        txtBass.setDisable(true);
        txtBass.setMaxWidth(50);
        txtBass.textProperty().addListener(event -> {
            String regexfile = "^[0-9]{1,2}$";
            txtBass.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                    (txtBass.getText().isEmpty() ||
                            !txtBass.getText().matches(regexfile)
                    ));
        });

        Label lblLower = new Label("    Lower:  ");
        lblLower.setStyle(styletext);
        txtLower.setStyle(styletext);
        txtLower = new TextField(Integer.toString(sharedStatus.getLowerCHAN()));
        txtLower.setDisable(true);
        txtLower.setMaxWidth(50);
        txtLower.textProperty().addListener(event -> {
            String regexfile = "^[0-9]{1,2}$";
            txtLower.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                    (txtLower.getText().isEmpty() ||
                            !txtLower.getText().matches(regexfile)
                    ));
        });

        Label lblUpper = new Label("    Upper:  ");
        lblUpper.setStyle(styletext);
        txtUpper.setStyle(styletext);
        txtUpper = new TextField(Integer.toString(sharedStatus.getUpper1CHAN()));
        txtUpper.setDisable(true);
        txtUpper.setMaxWidth(50);
        txtUpper.textProperty().addListener(event -> {
            String regexfile = "^[0-9]{1,2}$";
            txtUpper.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("error"),
                    (txtUpper.getText().isEmpty() ||
                            !txtUpper.getText().matches(regexfile)
                    ));
        });

        Label lblTimeSig = new Label("    Time:  ");
        lblTimeSig.setStyle(styletext);
        txtTimeSig.setStyle(styletext);
        txtTimeSig.setDisable(true);
        txtTimeSig.setMaxWidth(50);
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
        buttonedit.setPrefSize(75, 25);
        buttonedit.setOnAction(event -> {

            // Enable Preset File Save As text entry
            txtSongTitle.setDisable(false);
            txtPresetFile.setDisable(false);
            txtSmfFile.setDisable(false);
            txtBass.setDisable(false);
            txtLower.setDisable(false);
            txtUpper.setDisable(false);
            txtTimeSig.setDisable(false);
            txtPresetSaveAsFile.setDisable(true);

            buttonedit.setDisable(true);
            buttonnew.setDisable(true);
            buttondelete.setDisable(true);
            buttonupdate.setDisable(false);

            labelstatusSng.setText(" Status: Editing details for Song " + txtSongTitle.getText());
        });

        // Song New/Update, Delete and Save Buttons
        buttonnew.setText("New");
        buttonnew.setStyle(selectcolorOff);
        buttonnew.setPrefSize(75, 25);
        buttonnew.setOnAction(event -> {

            MidiSong midiSong = dosongs.getSong(0);
            txtSongTitle.setText("New"); //midiSong.getSongTitle());
            txtSmfFile.setText(midiSong.getMidiFile());
            txtPresetFile.setText(midiSong.getPresetFile());

            bnewSong = true;

            // Enable Preset File Save As text entry
            txtPresetSaveAsFile.setEditable(true);
            txtPresetSaveAsFile.setMouseTransparent(false);
            txtPresetSaveAsFile.setFocusTraversable(true);
            txtPresetSaveAsFile.setDisable(false);

            // Enable Preset File Save As text entry
            txtSongTitle.setDisable(false);
            txtPresetFile.setDisable(false);
            txtSmfFile.setDisable(false);
            txtBass.setDisable(false);
            txtLower.setDisable(false);
            txtUpper.setDisable(false);
            txtTimeSig.setDisable(false);
            txtInstrumentList.setText("Play Song to update Song MIDI Track Instruments");

            buttonedit.setDisable(true);
            buttonnew.setDisable(true);
            buttondelete.setDisable(true);
            buttonupdate.setDisable(false);

            labelstatusSng.setText(" Status: Creating New Song " + txtSongTitle.getText());
        });

        buttondelete.setText("Delete");
        buttondelete.setStyle(selectcolorOff);
        buttondelete.setPrefSize(75, 25);
        buttondelete.setOnAction(event -> {
            ObservableList selectedIndices = songlistView.getSelectionModel().getSelectedIndices();

            buttonedit.setDisable(true);
            buttonnew.setDisable(true);


            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete selected Song");
            alert.setContentText("Proceed with delete?");
            ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(okButton, noButton);
            alert.showAndWait().ifPresent(type -> {
                if (type == ButtonType.NO) {
                    labelstatusSng.setText(" Status: Deleting current Song");
                }
            });

            for(Object o : selectedIndices) {
                System.out.println("Main: o = " + o + " (" + o.getClass() + ")");
                int idx = Integer.parseInt(o.toString());

                MidiSong midiSong = midiSongs.get(idx);
                String songtitle = midiSong.getSongTitle();

                // Never delete the initial default organ preset file.
                if (idx == 0) {
                    labelstatusSng.setText(" Status: Deleting Song " + songtitle + " not allowed");
                    return;
                }

                midiSongs.remove(idx);

                songlistView.getItems().remove(idx);
                songlistView.refresh();

                labelstatusSng.setText(" Status: Deleted Song " + songtitle);
            }
        });

        // Prepare for Demo Play with options, defaulting to demo play
        radioOriginal.setSelected(true);
        radioOriginal.setStyle(styletext);

        Button buttondemo = new Button("Play Song");
        buttondemo.setStyle(btnplayOff);
        buttondemo.setPrefSize(150, 25);
        buttondemo.setOnAction(e -> {
            //PlayMidi playmidifile = new PlayMidi();
            try {
                if (!bplaying) {
                    bplaying = true;
                    boolean bsettimer = false;

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

                buttondemo.setText("Demo");
                buttondemo.setStyle("-fx-background-color: #8ED072; ");

                exception.printStackTrace();
            }

            //System.out.println("Main: " + dopresets.presetString(presetIdx * 16 + channelIdx));
        });

        txtInstrumentList.setMaxWidth(500);
        txtInstrumentList.setMaxHeight(130);
        txtInstrumentList.setStyle("-fx-control-inner-background:#CCCCCC;");

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

        songgrid.add(lblSongChannels, 1, 5, 2, 1);
        songgrid.add(hboxChan, 1, 6, 3, 1);

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
        labelDemoType.setStyle(styletext);

        ToggleGroup radioDemoTypeGroup = new ToggleGroup();
        radioOriginal.setSelected(true);
        radioPresets = new RadioButton("With Presets    ");
        radioPresets.setStyle(styletext);
        radioLive = new RadioButton("Backing Only");
        radioLive.setStyle(styletext);
        radioOriginal.setToggleGroup(radioDemoTypeGroup);
        radioPresets.setToggleGroup(radioDemoTypeGroup);
        radioLive.setToggleGroup(radioDemoTypeGroup);

        HBox hboxPlayMode = new HBox(labelDemoType, radioOriginal, radioPresets, radioLive);
        hboxPlayMode.setPadding(new Insets(10, 0, 0,10));

        songbuttons.getChildren().addAll(songbuttons1, songbuttons2, songbuttons3, songbuttons4);
        songvboxS.getChildren().addAll(songbuttons, hboxPlayMode, vboxInstrumentList);

        // Assemble the BorderPane View
        borderPaneSng.setTop(borderPaneTopSng);
        borderPaneSng.setLeft(vboxLeftS);
        borderPaneSng.setCenter(songvboxS);
        //borderPane1.setRight(vboxRight);
        //borderPaneS.setBottom(borderStatusS);
        borderPaneSng.setBottom(labelstatusSng);

        return borderPaneSng;
    }

    /*********************************************************
     * Preset Scene
     *********************************************************/

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

    Button buttonSave = new Button();   // Presets save button - disabled/enabled when dirty

    ComboBox presetCombo;

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

    MidiPreset midiPreset;
    PlayMidi playmidifile;

    // Select Bank and Sounds Panel
    public BorderPane createPresetScene(Stage stage) {

        System.out.println("Main: Scene PresetScene!");

        BorderPane borderPane1 = new BorderPane();
        borderPane1.setStyle("-fx-background-color: " + bgpanecolor);

        // Create top bar navigation buttons
        Button buttonsc1 = new Button("Perform");
        buttonsc1.setStyle(btnMenuOff);
        //buttonsc1.setOnAction(e -> stage.setScene(sceneOrgan));
        buttonsc1.setOnAction(e -> {
            System.out.println(("Main: Changing to Organ Scene: " + sharedStatus.getOrganScene().toString()));
            stage.setScene(sharedStatus.getOrganScene());
            try {
                Thread.sleep(250);
            } catch (Exception ex) { }
        });

        Button buttonsc2 = new Button("Songs");
        buttonsc2.setStyle(btnMenuOff);
        //buttonsc2.setOnAction(e -> stage.setScene(sceneSongs));
        buttonsc2.setOnAction(e -> {
            System.out.println(("Main: Changing to Songs Scene: " + sharedStatus.getSongsScene().toString()));
            stage.setScene(sharedStatus.getSongsScene());
            try {
                Thread.sleep(250);
            } catch (Exception ex) { }
        });

        Button buttonsc3 = new Button("Presets");
        buttonsc3.setStyle(btnMenuOn);
        //buttonsc3.setOnAction(e -> stage.setScene(scenePresets));
        buttonsc3.setOnAction(e -> {
            System.out.println(("Main: Changing to Presets Scene: " + sharedStatus.getPresetsScene().toString()));
            stage.setScene(sharedStatus.getPresetsScene());
            try {
                Thread.sleep(250);
            } catch (Exception ex) { }
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
            playmidifile.stopMidiPlay("End Play");
            Platform.exit();
        });

        // Save Presets Button
        buttonSave.setText("Save Presets");
        buttonSave.setStyle(btnMenuSaveOn);
        buttonSave.setDisable(true);
        buttonSave.setOnAction(event -> {
            if (flgDirtyPreset) {
                boolean bsave = dopresets.savePresets(presetFile);
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

        ToolBar toolbarLeft = new ToolBar(buttonsc1, buttonsc2, buttonsc3);
        toolbarLeft.setStyle("-fx-background-color:" + bgheadercolor);
        toolbarLeft.setMinWidth(225);

        Label lbltitle1 = new Label("AMIDIFX Sound Module Controller");
        lbltitle1.setStyle(styletext);
        HBox hboxTitle = new HBox();
        hboxTitle.setPadding(new Insets(10, 10, 10,200));
        hboxTitle.getChildren().add(lbltitle1);

        ToolBar toolbarRight = new ToolBar(buttonSave, buttonPanic, buttonExit);
        toolbarRight.setStyle("-fx-background-color: " + bgheadercolor);
        toolbarRight.setMinWidth(150);

        BorderPane borderPaneTop = new BorderPane();
        borderPaneTop.setStyle("-fx-background-color: " + bgheadercolor);

        // Assemble the Menu Bar Border Pane
        borderPaneTop.setLeft(toolbarLeft);
        borderPaneTop.setCenter(hboxTitle);
        borderPaneTop.setRight(toolbarRight);

        // *** Start Bottom Status Bar

        BorderPane borderStatus = new BorderPane();
        borderStatus.setStyle("-fx-background-color: #999999; "); //#B2B5B1; ");

        labelstatus.setText(" Status: " + sharedStatus.getStatusText());
        labelsongtitle.setText(" Song: " + sharedStatus.getSongTitle());
        labelmidifile.setText("   Midi: " + sharedStatus.getMidiFile());
        labelpresetfile.setText("   Preset: " + sharedStatus.getPresetFile());

        FlowPane panefiles = new FlowPane();
        panefiles.setHgap(20);
        panefiles.getChildren().add(labelsongtitle);
        panefiles.getChildren().add(labelmidifile);
        panefiles.getChildren().add(labelpresetfile);

        VBox vboxstatusLeft = new VBox();
        vboxstatusLeft.setMinWidth(400);
        vboxstatusLeft.getChildren().add(labelstatus);

        // Assemble the Status BorderPane View
        borderStatus.setLeft(vboxstatusLeft);
        borderStatus.setCenter(panefiles);
        //borderPane2.setRight(vboxstatusright);

        // **** Show Left Pane: MIDI Sound Bank List

        ArrayList moduleNames = new ArrayList();
        moduleNames.add(midimodules.getModuleName(0));
        moduleNames.add(midimodules.getModuleName(1));
        int moduleidx = sharedStatus.getModuleidx();

        ObservableList<String> soundbank = FXCollections.observableArrayList();
        ListView<String> banklistView = new ListView<>(soundbank);
        banklistView.setPrefWidth(175);
        banklistView.setPrefHeight(550);
        banklistView.setStyle("-fx-control-inner-background: #E7ECEC;");

        ComboBox moduleCombo = new ComboBox(FXCollections.observableArrayList(moduleNames));
        moduleCombo.setPrefSize(175, 20);
        moduleCombo.getSelectionModel().select(moduleidx);
        EventHandler<ActionEvent> midxevent =
                e -> {
                    int moduleidx1 = moduleCombo.getSelectionModel().getSelectedIndex();
                    dopatches.loadMidiPatches(midimodules.getModuleFile(moduleidx1));
                    sharedStatus.setModuleidx(moduleidx1);

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

                    labelstatus.setText(" Status: Selected Bank " + selectedItem);
                });
        banklistView.getSelectionModel().selectFirst();
        banklistView.setStyle(styletext);

        Button buttonb = new Button("Select Bank");
        buttonb.setStyle(selectcolorOff);
        buttonb.setPrefSize(150, 50);
        buttonb.setOnAction(event -> {
            ObservableList selectedIndices = banklistView.getSelectionModel().getSelectedIndices();

            for(Object o : selectedIndices) {
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
        vboxLeft.setPadding(new Insets(10, 5, 5,5));
        vboxLeft.getChildren().add(vboxModuleList);
        vboxLeft.getChildren().add(vboxList);
        vboxLeft.getChildren().add(vboxbut);

        // Load MIDI Default MIDI Preset file on start up
        dopresets = new MidiPresets();
        dopresets.makeMidiPresets(presetFile);
        System.out.println("Main Init: Loaded new Preset file: " + presetFile);

        // **** Show Right Pane: MIDI Sound Bank List

        // List selected Preset channel sounds
        ObservableList<String> patchdata = FXCollections.observableArrayList();
        //ListView<String> presetListView = new ListView<String>(patchdata);
        presetListView = new ListView<>(patchdata);
        presetListView.setPrefWidth(175);
        presetListView.setPrefHeight(550);
        presetListView.setStyle("-fx-control-inner-background: #E7ECEC;");

        // Preset select Combobox
        String[] weekDays = { "Preset 1", "Preset 2", "Preset 3", "Preset 4", "Preset 5", "Preset 6", "Preset 7", "Preset 8"};
        presetCombo = new ComboBox(FXCollections.observableArrayList(weekDays));
        presetCombo.setPrefSize(175, 20);
        presetCombo.getSelectionModel().select(0);
        EventHandler<ActionEvent> pidxevent =
                e -> {
                    presetIdx = Integer.parseInt(presetCombo.getValue().toString().replaceAll("[^0-9]", ""));
                    presetIdx--;

                    String pfile = dopresets.getPresetFileName();
                    labelsongtitle.setText("Song: " + songTitle);
                    labelmidifile.setText("MIDI: " + songFile);
                    labelpresetfile.setText("Preset: " + pfile);
                    //System.out.println("Main: Selected Preset File: " + pfile);
                    //System.out.println("Main: Selected Preset: " + presetIdx);

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
                    String strChannelOut = dopresets.getPreset(presetIdx * 16 + channelIdx).getChannelOutIdx();
                    for (int i = 0; i < 16; i++) {
                        chkBoxArray[i].setSelected(false);
                    }

                    String[] tokens = strChannelOut.split("\\||,");
                    for (String token : tokens) {
                        int chkidx = Integer.parseInt(token);
                        //System.out.println("Main: Layer index " + chkidx);
                        if (chkidx > 0)
                            chkBoxArray[chkidx - 1].setSelected(true);
                    }

                    labelstatus.setText(" Status: Selected Preset " + (presetIdx + 1));
                };
        presetCombo.setOnAction(pidxevent);
        Platform.runLater(() -> {
            presetCombo.requestFocus();
            presetCombo.getSelectionModel().select(0);

            System.out.println("Main: Run later selected Preset 0");
        });
        VBox vboxPresetList = new VBox(presetCombo);

        channelIdx = 0;
        presetIdx = 0;
        for (int idx = 0; idx < 16; idx++) {
            MidiPreset midiPreset = dopresets.getPreset(presetIdx * 16 + idx);
            String name = midiPreset.getPatchName();
            //name = StringUtils.rightPad(name, 2);
            presetListView.getItems().add((idx+1) + ":" + name);

            //System.out.println("Main: Patch name " + name);
        }
        presetListView.getSelectionModel().select(channelIdx);

        VBox vboxList2 = new VBox(presetListView);
        // Initial populate of the Preset List View
        presetListView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String old_val, String new_val) -> {
                    String selectedItem = presetListView.getSelectionModel().getSelectedItem();
                    channelIdx = presetListView.getSelectionModel().getSelectedIndex();

                    // Update the MIDI Channel Effects sliders to new MIDI voice channel selected
                    sliderVOL.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getVOL());
                    sliderEXP.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getEXP());
                    sliderREV.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getREV());
                    sliderCHO.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getCHO());
                    sliderMOD.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getMOD());
                    sliderPAN.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getPAN());
                    sliderTRE.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getTRE());

                    // **** Update the MIDI Channel Out Layer checkboxes
                    String strChannelOut = dopresets.getPreset(presetIdx * 16 + channelIdx).getChannelOutIdx();
                    for (int i = 0; i < 16; i++) {
                        chkBoxArray[i].setSelected(false);
                    }

                    String[] tokens = strChannelOut.split("\\||,");
                    for (String token : tokens)
                    {
                        int chkidx = Integer.parseInt(token);
                        //System.out.println("Main: Layer index: " + chkidx);
                        if (chkidx > 0)
                            chkBoxArray[chkidx - 1].setSelected(true);
                    }

                    //System.out.println("Main: Item selected " + selectedItem + ", Item index: " + channelIdx);
                    labelstatus.setText(" Status: CHAN Voice "  + selectedItem);

                    setMidiLayerLabel(channelIdx + 1);
                });
        presetListView.getSelectionModel().selectFirst();
        presetListView.setStyle(styletext);

        // Update Voice for currently selected Channel in Preset Listview
        Button buttonvoice = new Button("Update Channel");
        buttonvoice.setStyle(selectcolorOff);
        buttonvoice.setPrefSize(150, 50);
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
            sliderMOD.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getMOD());
            sliderPAN.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getPAN());
            sliderTRE.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getTRE());

            // Apply the Voice to MIDI Channel
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
        vboxRight.setPadding(new Insets(10, 5, 5,5));
        vboxRight.getChildren().add(vboxPresetList);
        vboxRight.getChildren().add(vboxList2);
        vboxRight.getChildren().add(vboxvoice);

        // **** Show Middle Pane: Sound Fonts for selected Bank. First sound from Bank 1 1 by default
        FlowPane flowpane = new FlowPane();
        flowpane.setPadding(new Insets(10, 5, 0,10));    // Insets(double top, double right, double bottom, double left)
        flowpane.setHgap(10);
        flowpane.setVgap(10);

        bpressed1 = false;
        pstbutton1 = new Button("Button 1");
        pstbutton1.setStyle(btnPresetOff);
        pstbutton1.setWrapText(true);
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
        pstbutton1.setPrefSize(150, 50);

        bpressed2 = false;
        pstbutton2 = new Button("Button 2");
        pstbutton2.setStyle(btnPresetOff);
        pstbutton2.setWrapText(true);
        pstbutton2.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+1);
            if (!bpressed2) {
                pstbutton2.setStyle(btnPresetOn);
            } else {
                pstbutton2.setStyle(btnPresetOff);
            }
            bpressed2 = !bpressed2;
        });
        pstbutton2.setPrefSize(150, 50);

        bpressed3 = false;
        pstbutton3 = new Button("Button 3");
        pstbutton3.setStyle(btnPresetOff);
        pstbutton3.setWrapText(true);
        pstbutton3.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+2);
            if (!bpressed3) {
                pstbutton3.setStyle(btnPresetOn);
            } else {
                pstbutton3.setStyle(btnPresetOff);
            }
            bpressed3 = !bpressed3;
        });
        pstbutton3.setPrefSize(150, 50);

        bpressed4 = false;
        pstbutton4 = new Button("Button 4");
        pstbutton4.setStyle(btnPresetOff);
        pstbutton4.setWrapText(true);
        pstbutton4.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+3);
            if (!bpressed4) {
                pstbutton4.setStyle(btnPresetOn);
            } else {
                pstbutton4.setStyle(btnPresetOff);
            }
            bpressed4 = !bpressed4;
        });
        pstbutton4.setPrefSize(150, 50);

        bpressed5 = false;
        pstbutton5 = new Button("Button 5");
        pstbutton5.setStyle(btnPresetOff);
        pstbutton5.setWrapText(true);
        pstbutton5.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+4);
            if (!bpressed5) {
                pstbutton5.setStyle(btnPresetOn);
            } else {
                pstbutton5.setStyle(btnPresetOff);
            }
            bpressed5 = !bpressed5;
        });
        pstbutton5.setPrefSize(150, 50);

        bpressed6 = false;
        pstbutton6 = new Button("Button 6");
        pstbutton6.setStyle(btnPresetOff);
        pstbutton6.setWrapText(true);
        pstbutton6.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+5);
            if (!bpressed6) {
                pstbutton6.setStyle(btnPresetOn);
            } else {
                pstbutton6.setStyle(btnPresetOff);
            }
            bpressed6 = !bpressed6;
        });
        pstbutton6.setPrefSize(150, 50);

        bpressed7 = false;
        pstbutton7 = new Button("Button 7");
        pstbutton7.setStyle(btnPresetOff);
        pstbutton7.setWrapText(true);
        pstbutton7.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+6);
            if (!bpressed7) {
                pstbutton7.setStyle(btnPresetOn);
            } else {
                pstbutton7.setStyle(btnPresetOff);
            }
            bpressed7 = !bpressed7;
        });
        pstbutton7.setPrefSize(150, 50);

        bpressed8 = false;
        pstbutton8 = new Button("Button 8");
        pstbutton8.setStyle(btnPresetOff);
        pstbutton8.setWrapText(true);
        pstbutton8.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+7);
            if (!bpressed8) {
                pstbutton8.setStyle(btnPresetOn);
            } else {
                pstbutton8.setStyle(btnPresetOff);
            }
            bpressed8 = !bpressed8;
        });
        pstbutton8.setPrefSize(150, 50);

        bpressed9 = false;
        pstbutton9 = new Button("Button 9");
        pstbutton9.setStyle(btnPresetOff);
        pstbutton9.setWrapText(true);
        pstbutton9.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+8);
            if (!bpressed9) {
                pstbutton9.setStyle(btnPresetOn);
            } else {
                pstbutton9.setStyle(btnPresetOff);
            }
            bpressed9 = !bpressed9;
        });
        pstbutton9.setPrefSize(150, 50);

        bpressed10 = false;
        pstbutton10 = new Button("Button 10");
        pstbutton10.setStyle(btnPresetOff);
        pstbutton10.setWrapText(true);
        pstbutton10.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+9);
            if (!bpressed10) {
                pstbutton10.setStyle(btnPresetOn);
            } else {
                pstbutton10.setStyle(btnPresetOff);
            }
            bpressed10 = !bpressed10;
        });
        pstbutton10.setPrefSize(150, 50);

        bpressed11 = false;
        pstbutton11 = new Button("Button 11");
        pstbutton11.setStyle(btnPresetOff);
        pstbutton11.setWrapText(true);
        pstbutton11.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+10);
            if (!bpressed11) {
                pstbutton11.setStyle(btnPresetOn);
            } else {
                pstbutton11.setStyle(btnPresetOff);
            }
            bpressed11 = !bpressed11;
        });
        pstbutton11.setPrefSize(150, 50);

        bpressed12 = false;
        pstbutton12 = new Button("Button 12");
        pstbutton12.setStyle(btnPresetOff);
        pstbutton12.setWrapText(true);
        pstbutton12.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+11);
            if (!bpressed12) {
                pstbutton12.setStyle(btnPresetOn);
            } else {
                pstbutton12.setStyle(btnPresetOff);
            }
            bpressed12 = !bpressed12;
        });
        pstbutton12.setPrefSize(150, 50);

        bpressed13 = false;
        pstbutton13 = new Button("Button 13");
        pstbutton13.setStyle(btnPresetOff);
        pstbutton13.setWrapText(true);
        pstbutton13.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+12);
            if (!bpressed13) {
                pstbutton13.setStyle(btnPresetOn);
            } else {
                pstbutton13.setStyle(btnPresetOff);
            }
            bpressed13 = !bpressed13;
        });
        pstbutton13.setPrefSize(150, 50);

        bpressed14 = false;
        pstbutton14 = new Button("Button 14");
        pstbutton14.setStyle(btnPresetOff);
        pstbutton14.setWrapText(true);
        pstbutton14.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+13);
            if (!bpressed14) {
                pstbutton14.setStyle(btnPresetOn);
            } else {
                pstbutton14.setStyle(btnPresetOff);
            }
            bpressed14 = !bpressed14;
        });
        pstbutton14.setPrefSize(150, 50);

        bpressed15 = false;
        pstbutton15 = new Button("Button 15");
        pstbutton15.setStyle(btnPresetOff);
        pstbutton15.setWrapText(true);
        pstbutton15.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+14);
            if (!bpressed15) {
                pstbutton15.setStyle(btnPresetOn);
            } else {
                pstbutton15.setStyle(btnPresetOff);
            }
            bpressed15 = !bpressed15;
        });
        pstbutton15.setPrefSize(150, 50);

        bpressed16 = false;
        pstbutton16 = new Button("Button 16");
        pstbutton16.setStyle(btnPresetOff);
        pstbutton16.setWrapText(true);
        pstbutton16.setOnAction(event -> {
            offAllButtons();
            buttonAction(selpatchIdx = patchIdx+15);
            if (!bpressed16) {
                pstbutton16.setStyle(btnPresetOn);
            } else {
                pstbutton16.setStyle(btnPresetOff);
            }
            bpressed16 = !bpressed16;
        });
        pstbutton16.setPrefSize(150, 50);

        Button btnprev = new Button("<< Previous");
        btnprev.setStyle(selectcolorOff);
        btnprev.setPrefSize(150, 50);
        btnprev.setOnAction(e -> {
            offAllButtons();
            renderVoiceButtons(patchIdx - 16 > 0 ? (patchIdx = patchIdx - 16) : (patchIdx = 0), dopatches.getMIDIPatchSize());
        });

        Button btntest = new Button("Test Voice");
        btntest.setStyle(btnplayOff);
        btntest.setPrefSize(150, 50);
        btntest.setOnAction(e -> {
            try {
                if (!btestnote) {
                    btntest.setText("Stop");
                    btntest.setStyle(btnplayOn);

                    PlayMidi playmidifile = PlayMidi.getInstance();
                    MidiPatch patch = dopatches.getMIDIPatch(selpatchIdx);
                    //System.out.println("Main: Selecting patch " + patch.toString());

                    playmidifile.sendMidiProgramChange((byte)(channelIdx+1), (byte)patch.getPC(), (byte)patch.getLSB(), (byte)patch.getMSB());

                    playmidifile.sendMidiControlChange((byte)(channelIdx+1), ccVOL, (byte)sliderVOL.getValue());
                    playmidifile.sendMidiControlChange((byte)(channelIdx+1), ccEXP, (byte)sliderEXP.getValue());
                    playmidifile.sendMidiControlChange((byte)(channelIdx+1), ccREV, (byte)sliderREV.getValue());
                    playmidifile.sendMidiControlChange((byte)(channelIdx+1), ccCHO, (byte)sliderCHO.getValue());
                    playmidifile.sendMidiControlChange((byte)(channelIdx+1), ccMOD, (byte)sliderMOD.getValue());
                    playmidifile.sendMidiControlChange((byte)(channelIdx+1), ccPAN, (byte)sliderPAN.getValue());
                    playmidifile.sendMidiControlChange((byte)(channelIdx+1), ccTRE, (byte)sliderTRE.getValue());
                    playmidifile.sendMidiNote((byte)(channelIdx+1), (byte)60, true);

                    btestnote = true;
                }
                else {
                    btntest.setText("Test Note");
                    btntest.setStyle(btnplayOff);

                    PlayMidi playmidifile = PlayMidi.getInstance();
                    playmidifile.sendMidiNote((byte)1, (byte)60, false);

                    btestnote = false;
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        Button btndemo = new Button(" Play Song");
        btndemo.setStyle(btnplayOff);
        btndemo.setPrefSize(150, 50);
        btndemo.setOnAction(e -> {
            try {
                if (!bplaying) {
                    btndemo.setText(" Stop Play");
                    btndemo.setStyle(btnplayOn);

                    bplaying = true;

                    PlayMidi playmidifile = PlayMidi.getInstance();
                    if (!playmidifile.startMidiPlay(dosongs.getSong(idxSongList), dopresets, 1)) {
                        labelstatus.setText(" Status: " + sharedStatus.getStatusText());
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
                                        bplaying = false;

                                        btndemo.setText("Play Song");
                                        btndemo.setStyle(btnplayOff);
                                        labelstatus.setText(" Status: Song Play Complete " + playmidifile.getSequencerTickPosition());
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

                        labelstatus.setText(" Status: Playing file " + txtSmfFile.getText());
                    }
                }
                else {
                    btndemo.setText("Play Song");
                    btndemo.setStyle(btnplayOff);

                    PlayMidi playmidifile = PlayMidi.getInstance();
                    playmidifile.stopMidiPlay(songFile);

                    bplaying = false;
                }
            }
            catch (Exception exception) {
                bplaying = false;

                btndemo.setText("Play Song");
                btndemo.setStyle(btnplayOff);

                exception.printStackTrace();
            }
            //System.out.println("Main: " + readpresets.presetString(presetIdx * 16 + channelIdx));
        });

        Button btnnext = new Button("Next >>");
        btnnext.setStyle(selectcolorOff);
        btnnext.setPrefSize(150, 50);
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
        Rotate rotateVol = new Rotate();
        sliderVOL.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateVol.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange((byte)( channelIdx +1), ccVOL, (byte) sliderVOL.getValue());

            dopresets.getPreset(presetIdx * 16 + channelIdx).setVOL(newValue.intValue());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " VOL: " + newValue.intValue());
        });
        sliderVOL.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getVOL());

        // Create EXP slider
        sliderEXP = new Slider(0, 127, 0);
        sliderEXP.setOrientation(Orientation.VERTICAL);
        sliderEXP.setShowTickLabels(true);
        sliderEXP.setShowTickMarks(true);
        sliderEXP.setMajorTickUnit(16);
        sliderEXP.setBlockIncrement(4);
        Rotate rotateExp = new Rotate();
        sliderEXP.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateExp.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange((byte)(channelIdx + 1), ccEXP, (byte) sliderEXP.getValue());

            dopresets.getPreset(presetIdx * 16 + channelIdx).setEXP(newValue.intValue());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status; CHAN " + (channelIdx + 1) + " EXP: " + newValue.intValue());
        });
        sliderEXP.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getEXP());

        // Create REV slider
        sliderREV = new Slider(0, 127, 0);
        sliderREV.setOrientation(Orientation.VERTICAL);
        sliderREV.setShowTickLabels(true);
        sliderREV.setShowTickMarks(true);
        sliderREV.setMajorTickUnit(16);
        sliderREV.setBlockIncrement(4);
        Rotate rotateRev = new Rotate();
        sliderREV.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateRev.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange((byte)( channelIdx + 1), ccREV, (byte) sliderREV.getValue());

            dopresets.getPreset(presetIdx * 16 + channelIdx).setREV(newValue.intValue());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " REV: " + newValue.intValue());
        });
        sliderREV.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getREV());

        // Create CHO slider
        sliderCHO = new Slider(0, 127, 0);
        sliderCHO.setOrientation(Orientation.VERTICAL);
        sliderCHO.setShowTickLabels(true);
        sliderCHO.setShowTickMarks(true);
        sliderCHO.setMajorTickUnit(16);
        sliderCHO.setBlockIncrement(4);
        Rotate rotateCho = new Rotate();
        sliderCHO.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateCho.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange((byte)( channelIdx + 1), ccCHO, (byte) sliderCHO.getValue());

            dopresets.getPreset(presetIdx * 16 + channelIdx).setCHO(newValue.intValue());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " CHO: " + newValue.intValue());
        });
        sliderCHO.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getCHO());

        // Create MOD slider
        sliderMOD = new Slider(0, 127, 0);
        sliderMOD.setOrientation(Orientation.VERTICAL);
        sliderMOD.setShowTickLabels(true);
        sliderMOD.setShowTickMarks(true);
        sliderMOD.setMajorTickUnit(16);
        sliderMOD.setBlockIncrement(4);
        Rotate rotateMod = new Rotate();
        sliderMOD.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateMod.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange((byte)( channelIdx + 1), ccMOD, (byte) sliderMOD.getValue());

            dopresets.getPreset(presetIdx * 16 + channelIdx).setMOD(newValue.intValue());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Starus: CHAN " + (channelIdx + 1) + " MOD: " + newValue.intValue());
        });
        sliderMOD.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getMOD());

        // Create PAN slider
        sliderPAN = new Slider(0, 127, 0);
        sliderPAN.setOrientation(Orientation.VERTICAL);
        sliderPAN.setShowTickLabels(true);
        sliderPAN.setShowTickMarks(true);
        sliderPAN.setMajorTickUnit(16);
        sliderPAN.setBlockIncrement(4);
        Rotate rotatePan = new Rotate();
        sliderPAN.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotatePan.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange((byte)( channelIdx + 1), ccPAN, (byte) sliderPAN.getValue());

            //System.out.println("Main: Old Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());
            dopresets.getPreset(presetIdx * 16 + channelIdx).setPAN(newValue.intValue());
            //System.out.println("Main: New Pan Value " + readpresets.getPreset(presetIdx * 16 + channelIdx).getPAN());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: CHAN " + (channelIdx + 1) + " PAN: " + newValue.intValue());
        });
        sliderPAN.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getPAN());

        // Create PAN slider
        sliderTRE = new Slider(0, 127, 0);
        sliderTRE.setOrientation(Orientation.VERTICAL);
        sliderTRE.setShowTickLabels(true);
        sliderTRE.setShowTickMarks(true);
        sliderTRE.setMajorTickUnit(16);
        sliderTRE.setBlockIncrement(4);
        Rotate rotateTre = new Rotate();
        sliderTRE.valueProperty().addListener((observable, oldValue, newValue) -> {
            //Setting the angle for the rotation
            rotateTre.setAngle((double) newValue);

            PlayMidi playmidifile = PlayMidi.getInstance();
            playmidifile.sendMidiControlChange((byte) (channelIdx + 1), ccTRE, (byte) sliderTRE.getValue());

            dopresets.getPreset(presetIdx * 16 + channelIdx).setTRE(newValue.intValue());

            buttonSave.setDisable(false);
            flgDirtyPreset = true;      // Need to save updated Preset

            labelstatus.setText(" Status: Channel " + (channelIdx + 1) + " TRE: " + newValue.intValue());
        });
        sliderTRE.setValue(dopresets.getPreset(presetIdx * 16 + channelIdx).getTRE());

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
        flowpane.getChildren().add(btntest);
        flowpane.getChildren().add(btndemo);
        flowpane.getChildren().add(btnnext);

        FlowPane paneEffects = new FlowPane();
        paneEffects.getChildren().add(new VBox(new Label(" VOL"), sliderVOL));
        paneEffects.getChildren().add(new VBox(new Label(" EXP"), sliderEXP));
        paneEffects.getChildren().add(new VBox(new Label(" REV"), sliderREV));
        paneEffects.getChildren().add(new VBox(new Label(" CHO"), sliderCHO));
        paneEffects.getChildren().add(new VBox(new Label(" TRE"), sliderTRE));
        paneEffects.getChildren().add(new VBox(new Label(" MOD"), sliderMOD));
        paneEffects.getChildren().add(new VBox(new Label(" PAN"), sliderPAN));
        flowpane.getChildren().add(paneEffects);
        flowpane.setStyle(styletext);

        // Add MIDI Channel Layering for each Channel
        VBox vboxLayers = new VBox();
        vboxLayers.setSpacing(10);

        GridPane gridLayers = new GridPane();
        gridLayers.setHgap(3);
        gridLayers.setVgap(3);

        String[] midiChannels = { " 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ",
                " 9 ", "10 ", "11 ", "12 ", "13 ", "14 ", "15 ", "16 "};

        midiLayerLabel = new Label("MIDI Channel " + (channelIdx + 1) + " Layers");
        midiLayerLabel.setStyle(styletext);

        chkBoxArray = new CheckBox[16];
        int x, y;
        for (checkIdx = 0; checkIdx < 16; checkIdx++) {                     // midiChannels.length
            chkBoxArray[checkIdx] = new CheckBox(midiChannels[checkIdx]);
            chkBoxArray[checkIdx].setStyle(styletext);
            // Add Check Box event to save changes to Preset
            EventHandler<ActionEvent> event = e -> {
                updateChannelOutIdx(channelIdx);

                buttonSave.setDisable(false);
                flgDirtyPreset = true;
            };
            chkBoxArray[checkIdx].setOnAction(event);

            if (checkIdx < 4) { x = checkIdx; y = 0; }
            else if (checkIdx < 8) { x = checkIdx - 4; y = 1; }
            else if (checkIdx < 12) { x = checkIdx - 8; y = 2; }
            else { x = checkIdx - 12; y = 3; }
            gridLayers.add(chkBoxArray[checkIdx], x, y, 1, 1);

            //chkBox.setIndeterminate(true);
        }

        // Send Presets to MIDI Button
        Button buttonApplyPreset = new Button("Apply Preset");
        buttonApplyPreset.setPrefSize(150, 25);
        buttonApplyPreset.setStyle(btnplayOff);
        buttonApplyPreset.setOnAction(event -> {
            MidiPreset applypreset = dopresets.getPreset(channelIdx);
            dopresets.applyMidiPreset(applypreset, channelIdx+1);

            labelstatus.setText(" Status: Preset CHAN " + (channelIdx + 1) + " MIDI sent");
        });

        vboxLayers.setPadding(new Insets(0, 0, 0,0));
        vboxLayers.getChildren().add(midiLayerLabel);
        vboxLayers.getChildren().add(gridLayers);
        vboxLayers.getChildren().add(buttonApplyPreset);
        flowpane.getChildren().add(vboxLayers);

        // Assemble the Preset Scene BorderPane View
        borderPane1.setTop(borderPaneTop);
        borderPane1.setLeft(vboxLeft);
        borderPane1.setCenter(flowpane);
        borderPane1.setRight(vboxRight);
        borderPane1.setBottom(borderStatus);

        // Initial Patches Render
        renderVoiceButtons(patchIdx, dopatches.getMIDIPatchSize());

        return borderPane1;
    }

    public void renderVoiceButtons(int patchIdx, int totalpatchcnt) {

        int idxcnt = 0;
        String pname;

        if ((idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton1.setText(pname);
            pstbutton1.setDisable(false);
        }
        else {
            pstbutton1.setText("");
            pstbutton1.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton2.setText(pname);
            pstbutton2.setDisable(false);
        }
        else {
            pstbutton2.setText("");
            pstbutton2.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton3.setText(pname);
            pstbutton3.setDisable(false);
        }
        else {
            pstbutton3.setText("");
            pstbutton3.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton4.setText(pname);
            pstbutton4.setDisable(false);
        }
        else {
            pstbutton4.setText("");
            pstbutton4.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton5.setText(pname);
            pstbutton5.setDisable(false);
        }
        else {
            pstbutton5.setText("");
            pstbutton5.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx + idxcnt).getPatchName();
            pstbutton6.setText(pname);
            pstbutton6.setDisable(false);
        }
        else {
            pstbutton6.setText("");
            pstbutton6.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx+idxcnt).getPatchName();
            pstbutton7.setText(pname);
            pstbutton7.setDisable(false);
        }
        else {
            pstbutton7.setText("");
            pstbutton7.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx+idxcnt).getPatchName();
            pstbutton8.setText(pname);
            pstbutton8.setDisable(false);
        }
        else {
            pstbutton8.setText("");
            pstbutton8.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx+idxcnt).getPatchName();
            pstbutton9.setText(pname);
            pstbutton9.setDisable(false);
        }
        else {
            pstbutton9.setText("");
            pstbutton9.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx+idxcnt).getPatchName();
            pstbutton10.setText(pname);
            pstbutton10.setDisable(false);
        }
        else {
            pstbutton10.setText("");
            pstbutton10.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx+idxcnt).getPatchName();
            pstbutton11.setText(pname);
            pstbutton11.setDisable(false);
        }
        else {
            pstbutton11.setText("");
            pstbutton11.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx+idxcnt).getPatchName();
            pstbutton12.setText(pname);
            pstbutton12.setDisable(false);
        }
        else {
            pstbutton12.setText("");
            pstbutton12.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx+idxcnt).getPatchName();
            pstbutton13.setText(pname);
            pstbutton13.setDisable(false);
        }
        else {
            pstbutton13.setText("");
            pstbutton13.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx+idxcnt).getPatchName();
            pstbutton14.setText(pname);
            pstbutton14.setDisable(false);
        }
        else {
            pstbutton14.setText("");
            pstbutton14.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx+idxcnt).getPatchName();
            pstbutton15.setText(pname);
            pstbutton15.setDisable(false);
        }
        else {
            pstbutton15.setText("");
            pstbutton15.setDisable(true);
        }

        if ((++idxcnt + patchIdx) < totalpatchcnt) {
            pname = dopatches.getMIDIPatch(patchIdx+idxcnt).getPatchName();
            pstbutton16.setText(pname);
            pstbutton16.setDisable(false);
        }
        else {
            pstbutton16.setText("");
            pstbutton16.setDisable(true);
        }

    }

    private void offAllButtons() {
        bpressed1 = false;
        pstbutton1.setStyle("-fx-background-color: #DBD06B; ");
        bpressed2 = false;
        pstbutton2.setStyle("-fx-background-color: #DBD06B; ");
        bpressed3 = false;
        pstbutton3.setStyle("-fx-background-color: #DBD06B; ");
        bpressed4 = false;
        pstbutton4.setStyle("-fx-background-color: #DBD06B; ");
        bpressed5 = false;
        pstbutton5.setStyle("-fx-background-color: #DBD06B; ");
        bpressed6 = false;
        pstbutton6.setStyle("-fx-background-color: #DBD06B; ");
        bpressed7 = false;
        pstbutton7.setStyle("-fx-background-color: #DBD06B; ");
        bpressed8 = false;
        pstbutton8.setStyle("-fx-background-color: #DBD06B; ");
        bpressed9 = false;
        pstbutton9.setStyle("-fx-background-color: #DBD06B; ");
        bpressed10 = false;
        pstbutton10.setStyle("-fx-background-color: #DBD06B; ");
        bpressed11 = false;
        pstbutton11.setStyle("-fx-background-color: #DBD06B; ");
        bpressed12 = false;
        pstbutton12.setStyle("-fx-background-color: #DBD06B; ");
        bpressed13 = false;
        pstbutton13.setStyle("-fx-background-color: #DBD06B; ");
        bpressed14 = false;
        pstbutton14.setStyle("-fx-background-color: #DBD06B; ");
        bpressed15 = false;
        pstbutton15.setStyle("-fx-background-color: #DBD06B; ");
        bpressed16 = false;
        pstbutton16.setStyle("-fx-background-color: #DBD06B; ");
    }

    private void updateChannelOutIdx(int channelIdx) {
        String strChannelIdxOut = "";
        boolean flgfirst = false;

        for (int idx = 0; idx < 16; idx++) {
            if (chkBoxArray[idx].isSelected()) {
                if (!flgfirst) {
                    strChannelIdxOut = Integer.toString(idx + 1);
                    flgfirst = true;
                }
                else {
                    strChannelIdxOut = strChannelIdxOut.concat("|").concat(Integer.toString(idx + 1));
                }
            }
        }
        dopresets.getPreset(presetIdx * 16 + channelIdx).setChannelOutIdx(strChannelIdxOut);
        //System.out.println("Main: ChannelOutIdx " + strChannelIdxOut);

        flgDirtyPreset = true;      // Need to save updated Preset

        labelstatus.setText(" Status: MIDI Layer Out Checkboxes changes updated");
    }


    private void setMidiLayerLabel(int chidx) {
        String strLabel = "MIDI Out Layers Channel: ".concat(Integer.toString(chidx));

        midiLayerLabel.setText(strLabel);
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

    public static void main(String[] args) {

        // Log System Out and Err to a file
        logSystemToFile();

        launch(args);
    }
}
