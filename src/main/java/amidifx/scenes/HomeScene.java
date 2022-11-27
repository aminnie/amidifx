package amidifx.scenes;

import amidifx.MidiPatches;
import amidifx.MidiPresets;
import amidifx.models.MidiModules;
import amidifx.models.SharedStatus;
import amidifx.utils.AppConfig;
import amidifx.utils.MidiDevices;
import amidifx.utils.MidiUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

public class HomeScene {

    Stage primaryStage;
    Scene returnScene;

    // Scaling based on default 1024 x 600 resolution.
    // Anything other resolution overrides and may require additional tuning of screen layout
    final static float xscreensize = 1280;
    final static float yscreensize = 800;

    float xmul = xscreensize/1024f;
    float ymul = yscreensize/600f;

    int ifsize = 15;
    int xscene = (int)(1024 * xmul);
    int yscene = (int)(600 * ymul - 45);    // 45 = Adjustment while we are single showing Windows status bar

    int xtoolbarleft = (int)(150 * xmul);
    int xtitle = (int)(100 * xmul);
    int xtoolbarright = (int)(150 * xmul);

    int xleftright = (int)(50 * xmul);
    int yleftright = (int)(25 * ymul);

    int xbtnleftright = (int)(190 * xmul);
    int ybtnleftright = (int)(25 * ymul);

    int xbtnpreset = (int)(115 * xmul);
    int ybtnpreset = (int)(50 * ymul);

    int xvoicebtn = (int)(120 * xmul);
    int yvoicebtn = (int)(50 * ymul);

    int xstatusleft = (int)(400 * xmul);

    // Calculate font size based on screen dimensions. Default = 15 for 1024 * 600
    final String fsize = Integer.toString((int)(ifsize * xmul)) + "; ";
    final String fsizetitle = Integer.toString((int)(ifsize * xmul * 1.1)) + "; ";

    // Button Colors
    // https://yagisanatode.com/2019/08/06/google-apps-script-hexadecimal-color-codes-for-google-docs-sheets-and-slides-standart-palette/
    final String bgpanecolor = "-fx-background-color: #999999; ";
    final String bgheadercolor = "-fx-background-color: #B2B5B1; ";
    final String bgfootercolor = "-fx-background-color: #B2B5B1;";

    final String selectcolorOff = "-fx-background-color: #69a8cc; -fx-font-size: " + fsize ;
    final String selectcolorOn = "-fx-background-color: #4493C0; -fx-font-size: " + fsize ;

    final String btnMenuOn = "-fx-background-color: #4493C0; -fx-font-size: " + fsize ;
    final String btnMenuOff = "-fx-background-color: #69A8CC; -fx-font-size: " + fsize ;
    final String btnMenuSaveOn = "-fx-background-color: #DB6B6B; -fx-font-size: " + fsize ;
    final String btnMenuSaveOff = "-fx-background-color: #B2B5B1; -fx-font-size: " + fsize ;

    final String btnplayOff = "-fx-background-color: #8ED072; -fx-font-size: " + fsize ;
    final String btnplayOn = "-fx-background-color: #DB6B6B; -fx-font-size: " + fsize ;

    final String styletext = "-fx-font-size: " + fsize ;
    final String styletextwhite = "-fx-text-fill: white; -fx-font-size: " + fsize ;
    final String styletextred = "-fx-text-fill: red; -fx-font-size: " + fsize ;
    final String styletextgreen = "-fx-text-fill: #8ED072; -fx-font-size: " + fsize ;
    final String styletexttitle = "-fx-font-size: " + fsizetitle;

    SharedStatus sharedStatus;

    AppConfig appconfig;

    MidiPatches dopatches;
    MidiPresets dopresets;
    MidiModules midimodules;

    //private String selindevice = "2- Seaboard RISE 49";
    //private String seloutdevice = "Deebach-Blackbox";
    private String selindevice = "default";
    private String seloutdevice = "default";

    Label lblindevice;
    Label lbloutdevice;

    Label labelstatusOrg;

    // main pane for the scene
    private BorderPane paneHome;
    private Scene sceneHome;

    private Button btnStart;
    Button buttonSave = new Button();

    ComboBox comboOutDevice = new ComboBox();
    ComboBox comboInDevice = new ComboBox();

    File fstyle;

    /*********************************************************
     * Creates Welcome Scene.
     *********************************************************/

    //public HomeScene(Stage primaryStage, Scene returnScene) {
    public HomeScene(Stage primaryStage) {

        System.out.println("AMIDIFX Home Scene Starting");

        try {
            // Create instance of Shared Status to report back to Scenes
            sharedStatus = SharedStatus.getInstance();

            // Load Config File Properties and preset IN and OUT devices to last config
            appconfig = AppConfig.getInstance();
            sharedStatus.setSelInDevice(appconfig.getInDevice());
            sharedStatus.setSelOutDevice(appconfig.getOutDevice());

            sharedStatus.setDrumCHAN(appconfig.getDrumChannel());
            sharedStatus.setSoloCHAN(appconfig.getSoloChannel());
            sharedStatus.setBassCHAN(appconfig.getBassChannel());
            sharedStatus.setLower1CHAN(appconfig.getLower1Channel());
            sharedStatus.setLower2CHAN(appconfig.getLower2Channel());
            sharedStatus.setUpper1CHAN(appconfig.getUpper1Channel());
            sharedStatus.setUpper2CHAN(appconfig.getUpper2Channel());
            sharedStatus.setUpper3CHAN(appconfig.getUpper3Channel());

            // Default the Demo Channel to Upper1
            sharedStatus.setDemoCHAN(appconfig.getUpper1Channel());

            // Initialize Input and Output Device Lists
            MidiUtils midiutils = new MidiUtils();
            midiutils.loadMidiDevices();

            boolean fselindeviceok = false;
            List<MidiUtils.StatusMidiDevice> inlist = midiutils.listInDevices();
            for (MidiUtils.StatusMidiDevice statusdevice : inlist) {
                comboInDevice.getItems().add(statusdevice.getDevice());

                if (statusdevice.getDevice().equals(appconfig.getInDevice())) fselindeviceok = true;

                System.out.println("MIDI In:" + statusdevice.getDevice());
            }

            boolean fseloutdeviceok = false;
            List<MidiUtils.StatusMidiDevice> outlist = midiutils.listOutDevices();
            for (MidiUtils.StatusMidiDevice statusdevice : outlist) {
                comboOutDevice.getItems().add(statusdevice.getDevice());

                if (statusdevice.getDevice().equals(appconfig.getOutDevice())) fseloutdeviceok = true;

                System.out.println("MIDI Out:" + statusdevice.getDevice());
            }

            // Start Building the Scene

            paneHome = new BorderPane();
            paneHome.setStyle("-fx-background-color: #999999; ");

            sceneHome = new Scene(paneHome, xscene, yscene);
            sceneHome.getStylesheets().clear();
            sceneHome.getStylesheets().add("style.css");

            sharedStatus.setHomeScene(sceneHome);

            // Create top bar navigation buttons

            System.out.println("HomeScene: Scene HomeScene!");

            labelstatusOrg = new Label(" Status: Select MIDI IN and OUT, click Configure.");

            labelstatusOrg.setStyle(styletext);

            // Create top bar navigation buttons

            Button buttonsc1 = new Button("Manual");
            buttonsc1.setStyle(btnMenuOff);
            buttonsc1.setDisable(true);
            buttonsc1.setOnAction(e -> {
                //System.out.println(("OrganScene: Changing to Organ Scene " + sharedStatus.getOrganScene().toString()));
                primaryStage.setScene(sharedStatus.getPerformScene());
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.err.println("### HomeScene: Unable to set Perform Scene!");
                }
            });

            Button buttonsc2 = new Button("Songs");
            buttonsc2.setStyle(btnMenuOff);
            buttonsc2.setDisable(true);
            buttonsc2.setOnAction(e -> {
                //System.out.println(("OrganScene: Changing to Songs Scene " + sharedStatus.getSongsScene().toString()));
                primaryStage.setScene(sharedStatus.getSongsScene());
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.err.println("### OrganScene: Unable to set Songs Scene!");
                }
            });

            Button buttonsc4 = new Button("Config");
            buttonsc4.setStyle(btnMenuOff);
            buttonsc4.setDisable(true);
            buttonsc4.setOnAction(e -> {

                System.out.println(("PerformScene: Changing to Config Scene " + sharedStatus.getHomeScene().toString()));
                primaryStage.setScene(sharedStatus.getHomeScene());
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.err.println("PerformScene: Unable to set Config Scene!");
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

            // Save Performance Button
            buttonSave.setText("Save Config");
            buttonSave.setStyle(btnMenuSaveOn);
            buttonSave.setDisable(true);
            buttonSave.setOnAction(event -> {

                if (!appconfig.saveProperties()) {
                    labelstatusOrg.setText(" Status: Application config save failed!");
                    System.err.println("HomeScene Error: Failed to save AppConfig file!");

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("AMIDIFX Error!");
                    alert.setHeaderText("Failed to save AppConfig file");
                    Optional<ButtonType> result = alert.showAndWait();

                    System.exit(-1);
                }
                else {
                    labelstatusOrg.setText(" Status: Application config saved!");
                    System.out.println("Application config file saved!");

                    lblindevice.setStyle(styletextwhite);
                    lbloutdevice.setStyle(styletextwhite);
                }

                buttonSave.setDisable(true);
            });

            Button buttonExit = new Button("  Exit  ");
            buttonExit.setStyle(btnMenuOff);
            buttonExit.setOnAction(e -> {

                try {
                    Receiver midircv = sharedStatus.getRxDevice();
                    midircv.close();
                    Transmitter miditrans = sharedStatus.getTxDevice();
                    miditrans.close();
                    Sequencer midiseq = sharedStatus.getSeqDevice();
                    midiseq.close();
                }
                catch (Exception ex) {
                    System.out.println("Info: Exiting: No receiver set yet");
                }

                Platform.exit();
            });

            ToolBar toolbarLeft = new ToolBar(buttonsc1, buttonsc2, buttonsc3, buttonsc4);
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
            lbltitle1.setText(appconfig.getControllerTitle());
            lbltitle1.setFont(Font.font(null, FontWeight.SEMI_BOLD, 20));

            HBox hboxTitle = new HBox();
            hboxTitle.setPadding(new Insets(10, 10, 10, xtitle));
            hboxTitle.getChildren().add(lbltitle1);

            ToolBar toolbarRight = new ToolBar(buttonSave, buttonExit);
            toolbarRight.setStyle(bgheadercolor);
            toolbarRight.setMinWidth(xtoolbarright);

            // Assemble the Menu Bar Border Pane

            BorderPane borderPaneTop = new BorderPane();
            borderPaneTop.setStyle(bgheadercolor);

            borderPaneTop.setLeft(toolbarLeft);
            borderPaneTop.setCenter(hboxTitle);
            borderPaneTop.setRight(toolbarRight);

            Label lbltopspacer = new Label("");
            lbltopspacer.setMaxHeight(50);

            String introline1 = "Welcome to AMIDIFX!";
            String introline2 = "AMIDIFX integrates MIDI keyboards, organs and sound modules enabling live play with backing tracks.\n";
            String introline3 = "AMIDIFX supsorts: Deebach BlackBox, Roland Integra 7, and MIDI GM compliant modules.\n";
            String introlines = introline1 + System.getProperty("line.separator") +
                    System.getProperty("line.separator") +
                    introline2 + System.getProperty("line.separator") +
                    introline3;
            TextArea txtIntro = new TextArea(introlines);
            txtIntro.setStyle("-fx-background-color: #CCCCCC; -fx-opacity: 0.7;");
            txtIntro.setPrefSize(300, 250);
            txtIntro.setWrapText(true);
            txtIntro.setEditable(false);

            String keyboards0 = "Keyboard MIDI Channels:";
            String keyboards1 = "       Solo: Channel " +  (sharedStatus.getSoloCHAN() + 1);
            String keyboards2 = "       Bass: Channel " +  (sharedStatus.getBassCHAN() + 1);
            String keyboards3 = "  Lower 1: Channel " +  (sharedStatus.getLower1CHAN() + 1);
            String keyboards4 = "  Upper 1: Channel " + (sharedStatus.getUpper1CHAN() + 1);
            String keyboards5 = "  Lower 2: Channel " +  (sharedStatus.getLower2CHAN() + 1);
            String keyboards6 = "  Upper 2: Channel " + (sharedStatus.getUpper2CHAN() + 1);
            String keyboards7 = "  Upper 3: Channel " + (sharedStatus.getUpper3CHAN() + 1);
            String keyboards8 = "    Drums: Channel " + (sharedStatus.getDrumCHAN() + 1);
            String keyboards9 = "        Expr: Channel " + (sharedStatus.getExpressionCHAN() + 1);
            String keyboards = keyboards0 + System.getProperty("line.separator") + System.getProperty("line.separator") +
                    keyboards1 + System.getProperty("line.separator") +
                    keyboards2 + System.getProperty("line.separator") +
                    keyboards3 + System.getProperty("line.separator") +
                    keyboards4 + System.getProperty("line.separator") +
                    keyboards5 + System.getProperty("line.separator") +
                    keyboards6 + System.getProperty("line.separator") +
                    keyboards7 + System.getProperty("line.separator") +
                    keyboards8 + System.getProperty("line.separator") +
                    keyboards9;
            TextArea txtKeyboard = new TextArea(keyboards);
            txtKeyboard.setStyle("-fx-background-color: #CACACA; -fx-opacity: 0.7;");
            txtKeyboard.setPrefSize(200, 250);
            txtKeyboard.setWrapText(true);
            txtKeyboard.setEditable(false);

            Label lblspace = new Label();
            lblspace.setPrefSize(720, 250);

            GridPane gridIntroKeyboard = new GridPane();
            gridIntroKeyboard.setPadding(new Insets(10, 10, 10, 10));
            gridIntroKeyboard.add(txtIntro,  0, 0, 1, 1);
            gridIntroKeyboard.add(lblspace, 1, 0, 1, 1);
            gridIntroKeyboard.add(txtKeyboard, 2, 0, 1, 1);

            lblindevice = new Label(sharedStatus.getSelInDevice());
            if (fselindeviceok == true)
                lblindevice.setStyle(styletextgreen + "; -fx-font-style:italic");
            else
                lblindevice.setStyle(styletextred + "; -fx-font-style:italic");
            VBox vboxindevice = new VBox();
            vboxindevice.getChildren().add(lblindevice);
            vboxindevice.setPadding(new Insets(5, 0, 5, 0));

            lbloutdevice = new Label(sharedStatus.getSelOutDevice());
            if (fseloutdeviceok == true)
                lbloutdevice.setStyle(styletextgreen + "; -fx-font-style:italic");
            else
                lbloutdevice.setStyle(styletextred + "; -fx-font-style:italic");
            VBox vboxoutdevice = new VBox();
            vboxoutdevice.getChildren().add(lbloutdevice);
            vboxoutdevice.setPadding(new Insets(5, 0, 5, 0));

            // Proceed to setup MIDI IN, OUT and SYNTH
            MidiDevices mididevices = MidiDevices.getInstance();
            Button btnConfig = new Button("Configure");
            btnConfig.setStyle(btnplayOff);
            btnConfig.setOnAction(e -> {

                int result = mididevices.createMidiDevices(appconfig.getInDevice(), appconfig.getOutDevice());
                if (result == -1) {
                    labelstatusOrg.setText(" Error creating MIDI OUT Device: " + appconfig.getOutDevice());
                    labelstatusOrg.setStyle(styletextred);
                }
                else if (result == -2) {
                    labelstatusOrg.setText(" Error creating MIDI Sequencer!");
                    labelstatusOrg.setStyle(styletextred);
                }
                else {
                    btnStart.setDisable(false);

                    // Save the updated Devices and load the associated Patch file
                    appconfig.saveProperties();

                    midimodules = new MidiModules();
                    dopatches = MidiPatches.getInstance();
                    String modulefile = midimodules.getModuleFile(appconfig.getSoundModuleIdx());
                    if (!dopatches.fileExist(modulefile)) {
                        System.err.println("HomeScene: Module Patch File "+ sharedStatus.getCFGDirectory() + modulefile + " not found!");

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("AMIDIFX Device Config Error");
                        alert.setHeaderText("Module Patch file " + sharedStatus.getCFGDirectory() + modulefile + " not found!");
                        Optional<ButtonType> presult = alert.showAndWait();

                        System.exit(-1);
                    }
                    dopatches.loadMidiPatches(modulefile);

                    labelstatusOrg.setText(" Status: Click Manual to continue.");
                    labelstatusOrg.setStyle(styletext);
                }
            });

            // Proceed to setup MIDI IN, OUT and SYNTH
            btnStart = new Button("  Manual  ");
            btnStart.setStyle(selectcolorOn);
            btnStart.setDisable(true);
            btnStart.setOnAction(e -> {

                System.out.println("HomeScene: Switching to Perform Scene");

                PerformScene performScene = new PerformScene(primaryStage);
                sharedStatus.setPerformScene(performScene.getScene());

                primaryStage.setScene(sharedStatus.getPerformScene());
                try {

                    Thread.sleep(600);
                }
                catch (Exception ex) {
                }

            });

            HBox hboxbtnstart = new HBox();
            hboxbtnstart.setSpacing(20);
            hboxbtnstart.getChildren().add(btnConfig);
            hboxbtnstart.getChildren().add(btnStart);

            comboInDevice.setPrefWidth(300);
            comboInDevice.setPadding(new Insets(5, 0, 5, 0));
            comboInDevice.setVisibleRowCount(3);
            comboInDevice.setOnAction((event) -> {
                int selectedIndex = comboInDevice.getSelectionModel().getSelectedIndex();
                Object selectedItem = comboInDevice.getSelectionModel().getSelectedItem();
                lblindevice.setText(comboInDevice.getValue().toString());
                lblindevice.setStyle(styletextgreen);

                appconfig.setInDevice(comboInDevice.getValue().toString());
                sharedStatus.setSelInDevice(comboInDevice.getValue().toString());

                selindevice = comboInDevice.getValue().toString();

                buttonSave.setDisable(false);
                System.out.println("MIDI In:" + comboInDevice.getValue().toString());

                //System.out.println("Selection made: [" + selectedIndex + "] " + selectedItem);
                //System.out.println("   ComboBox.getValue(): " + comboInDevice.getValue());
            });
            Label lblinselect = new Label("MIDI IN:");
            lblinselect.setStyle(styletextwhite);

            VBox vboxcomboindevice = new VBox();
            vboxcomboindevice.setPadding(new Insets(20, 10, 10, 10));
            vboxcomboindevice.getChildren().add(lblinselect);
            vboxcomboindevice.getChildren().add(comboInDevice);
            vboxcomboindevice.getChildren().add(vboxindevice);

            comboOutDevice.setPrefWidth(300);
            comboOutDevice.setPadding(new Insets(5, 0, 5, 0));
            comboOutDevice.setVisibleRowCount(3);
            comboOutDevice.setOnAction((event) -> {
                int selectedIndex = comboOutDevice.getSelectionModel().getSelectedIndex();
                Object selectedItem = comboOutDevice.getSelectionModel().getSelectedItem();
                lbloutdevice.setText(comboOutDevice.getValue().toString());
                lbloutdevice.setStyle(styletextgreen);

                appconfig.setOutDevice(comboOutDevice.getValue().toString());
                sharedStatus.setSelOutDevice(comboOutDevice.getValue().toString());

                seloutdevice = comboOutDevice.getValue().toString();

                // If selected Device is Deebach prepare to load the Deebach patches
                if (seloutdevice.contains("Deebach")) {
                    sharedStatus.setModuleidx(1);
                    appconfig.setSoundModuleIdx(1);
                    appconfig.saveProperties();

                    dopatches.loadMidiPatches(midimodules.getModuleFile(1));
                }

                // If selected Device is Integra, prepare to load the Integra7 patches
                if (seloutdevice.contains("Integra")) {
                    sharedStatus.setModuleidx(2);
                    appconfig.setSoundModuleIdx(2);
                    appconfig.saveProperties();

                    dopatches.loadMidiPatches(midimodules.getModuleFile(2));
                }

                buttonSave.setDisable(false);
                System.out.println("MIDI Out:" + comboOutDevice.getValue().toString());

                //System.out.println("Selection made: [" + selectedIndex + "] " + selectedItem);
                //System.out.println("   ComboBox.getValue(): " + comboOutDevice.getValue());
            });

            Label lbloutselect = new Label("MIDI OUT:");
            lbloutselect.setStyle(styletextwhite);

            VBox vboxcombooutdevice = new VBox();
            vboxcombooutdevice.setPadding(new Insets(20, 10, 10, 10));
            vboxcombooutdevice.getChildren().add(lbloutselect);
            vboxcombooutdevice.getChildren().add(comboOutDevice);
            vboxcombooutdevice.getChildren().add(vboxoutdevice);

            VBox vboxMidCenter = new VBox();
            vboxMidCenter.setPadding(new Insets(10, 10, 10, 10));
            vboxMidCenter.getChildren().add(lbltopspacer);
            vboxMidCenter.getChildren().add(gridIntroKeyboard);
            vboxMidCenter.getChildren().add(vboxcomboindevice);
            vboxMidCenter.getChildren().add(vboxcombooutdevice);
            vboxMidCenter.getChildren().add(hboxbtnstart);
            //vboxMidCenter.setAlignment(Pos.CENTER);

            // Final assembly the Home Center Panel

            BorderPane centerHomePanel = new BorderPane();
            centerHomePanel.setPadding(new Insets(10, 10, 10, 10));
            centerHomePanel.setCenter(vboxMidCenter);

            // Assemble Status Bar
            HBox hboxstatus = new HBox();
            hboxstatus.getChildren().add(labelstatusOrg);
            labelstatusOrg.setMinWidth(700 * ymul);
            Label labelsynth = new Label("Module: " + appconfig.getOutDevice());
            labelsynth.setTextAlignment(TextAlignment.JUSTIFY);
            labelsynth.setStyle(styletext);
            hboxstatus.getChildren().add(labelsynth);
            hboxstatus.setStyle(bgheadercolor);

            // Prepare background Image
            try {
                FileInputStream input = new FileInputStream(sharedStatus.getCFGDirectory() + "backimage.png");
                Image image = new Image(input);
                BackgroundImage backgroundimage = new BackgroundImage(image,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.DEFAULT,
                        BackgroundSize.DEFAULT);

                // Create and set background
                Background background = new Background(backgroundimage);
                centerHomePanel.setBackground(background);
            }
            catch(FileNotFoundException ex) {
                System.err.println("Background image not found! ");
            }

            // Assemble the Scene BorderPane View

            paneHome.setTop(borderPaneTop);
            //paneHome.setLeft(leftseparator);
            paneHome.setCenter(centerHomePanel);
            //paneHome.setRight(rightseparator);
            paneHome.setBottom(hboxstatus);
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
    }

    /** Returns the current scene **/
    public Scene getScene() {
        return sceneHome;
    }

}
