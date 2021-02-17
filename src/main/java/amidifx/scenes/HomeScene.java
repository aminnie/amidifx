package amidifx.scenes;

import amidifx.models.SharedStatus;
import amidifx.utils.AppConfig;
import amidifx.utils.MidiDevices;
import amidifx.utils.MidiUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

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

    int xtoolbarleft = (int)(225 * xmul);
    int xtitle = (int)(200 * xmul);
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

    private static final String IMG_DIRECTORY = "C:/amidifx/config/";

    SharedStatus sharedStatus;

    AppConfig config;

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

    public HomeScene(Stage primaryStage, Scene returnScene) {

        System.out.println("AMIDIFX Home Scene Starting");

        try {
            // Create instance of Shared Status to report back to Scenes
            sharedStatus = SharedStatus.getInstance();

            // Load Config File Properties and preset IN and OUT devices to last config
            config = AppConfig.getInstance();
            if (!config.loadProperties()) {
                System.err.println("Failed to load AppConfig file!");
                System.exit(-1);
            }

            sharedStatus.setSelInDevice(config.getInDevice());
            sharedStatus.setSelOutDevice(config.getOutDevice());

            // Initialize Input and Output Device Lists
            MidiUtils midiutils = new MidiUtils();
            midiutils.loadMidiDevices();

            List<MidiUtils.StatusMidiDevice> inlist = midiutils.listInDevices();
            for (MidiUtils.StatusMidiDevice statusdevice : inlist) {
                comboInDevice.getItems().add(statusdevice.getDevice());

                System.out.println("MIDI In:" + statusdevice.getDevice());
            }

            List<MidiUtils.StatusMidiDevice> outlist = midiutils.listOutDevices();
            for (MidiUtils.StatusMidiDevice statusdevice : outlist) {
                comboOutDevice.getItems().add(statusdevice.getDevice());

                System.out.println("MIDI Out:" + statusdevice.getDevice());
            }

            // Start Building the Scene

            paneHome = new BorderPane();
            paneHome.setStyle("-fx-background-color: #999999; ");

            sceneHome = new Scene(paneHome, xscene, yscene);
            sceneHome.getStylesheets().clear();
            sceneHome.getStylesheets().add("style.css");

            sharedStatus.setOrganScene(sceneHome);

            // Create top bar navigation buttons

            System.out.println("HomeScene: Scene HomeScene!");

            labelstatusOrg = new Label(" Status: Ready");
            labelstatusOrg.setStyle(styletext);

            // Create top bar navigation buttons

            Button buttonsc1 = new Button("Perform");
            buttonsc1.setStyle(btnMenuOff);
            buttonsc1.setDisable(true);
            buttonsc1.setOnAction(e -> {
                //System.out.println(("OrganScene: Changing to Organ Scene " + sharedStatus.getOrganScene().toString()));
                primaryStage.setScene(sharedStatus.getOrganScene());
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.err.println("### HomeScene: Unable to set Organ Scene!");
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

                if (!config.saveProperties()) {
                    labelstatusOrg.setText(" Status: Application config save failed!");
                    System.err.println("Failed to save AppConfig file!");
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

                Platform.exit();
            });

            ToolBar toolbarLeft = new ToolBar(buttonsc1, buttonsc2, buttonsc3);
            toolbarLeft.setStyle(bgheadercolor);
            toolbarLeft.setMinWidth(xtoolbarleft);

            Label lbltitle1 = new Label("AMIDIFX Sound Module Controller");
            lbltitle1.setStyle(styletext);
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
            lbltopspacer.setPrefSize(300, 50);

            String introline1 = "Welcome to AMIDIFX!";
            String introline2 = "AMIDIFX integrates MIDI sound modules with keyboards enabling live play with backing tracks.";
            String introline3 = "At this time we support the Deebach BlackBox as well as MIDI GM compliant modules with more planned.";
            String introlines = introline1 + System.getProperty("line.separator") + System.getProperty("line.separator") + introline2 + System.getProperty("line.separator") + introline3;
            TextArea txtIntro = new TextArea(introlines);
            txtIntro.setStyle("-fx-background-color: #999999; ");
            txtIntro.setPrefSize(300, 200);
            txtIntro.setWrapText(true);
            txtIntro.setDisable(true);
            HBox vboxIntro = new HBox();
            vboxIntro.setPadding(new Insets(10, 10, 10, 10));
            vboxIntro.getChildren().add(txtIntro);

            lblindevice = new Label(sharedStatus.getSelInDevice());
            lblindevice.setStyle(styletextwhite + "; -fx-font-style:italic");
            VBox vboxindevice = new VBox();
            vboxindevice.getChildren().add(lblindevice);
            vboxindevice.setPadding(new Insets(5, 0, 5, 0));

            lbloutdevice = new Label(sharedStatus.getSelOutDevice());
            lbloutdevice.setStyle(styletextwhite + "; -fx-font-style:italic");
            VBox vboxoutdevice = new VBox();
            vboxoutdevice.getChildren().add(lbloutdevice);
            vboxoutdevice.setPadding(new Insets(5, 0, 5, 0));

            // Proceed to setup MIDI IN, OUT and SYNTH
            MidiDevices mididevices = MidiDevices.getInstance();
            Button btnConfig = new Button("Configure");
            btnConfig.setStyle(btnplayOff);
            btnConfig.setOnAction(e -> {
                int result = mididevices.createMidiDevices(config.getInDevice(), config.getOutDevice());
                if (result == -1) {
                    labelstatusOrg.setText(" Error creating MIDI OUT Device: " + config.getOutDevice());
                }
                else if (result == -2) {
                    labelstatusOrg.setText(" Error creating MIDI Sequencer!");
                }
                else {
                    btnStart.setDisable(false);

                    labelstatusOrg.setText(" Status: Ready to Play!");
                }
            });
            // Proceed to setup MIDI IN, OUT and SYNTH
            btnStart = new Button("To Perform");
            btnStart.setStyle(selectcolorOn);
            btnStart.setDisable(true);
            btnStart.setOnAction(e -> {
                primaryStage.setScene(returnScene);
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
            comboInDevice.setOnAction((event) -> {
                int selectedIndex = comboInDevice.getSelectionModel().getSelectedIndex();
                Object selectedItem = comboInDevice.getSelectionModel().getSelectedItem();
                lblindevice.setText(comboInDevice.getValue().toString());
                lblindevice.setStyle(styletextred);

                config.setInDevice(comboInDevice.getValue().toString());
                sharedStatus.setSelInDevice(comboInDevice.getValue().toString());

                selindevice = comboInDevice.getValue().toString();

                buttonSave.setDisable(false);
                System.out.println("MIDI In:" + comboInDevice.getValue().toString());

                //System.out.println("Selection made: [" + selectedIndex + "] " + selectedItem);
                //System.out.println("   ComboBox.getValue(): " + comboInDevice.getValue());
            });
            Label lblinselect = new Label("Select MIDI IN:");
            lblinselect.setStyle(styletextwhite);

            VBox vboxcomboindevice = new VBox();
            vboxcomboindevice.setPadding(new Insets(20, 10, 10, 10));
            vboxcomboindevice.getChildren().add(lblinselect);
            vboxcomboindevice.getChildren().add(comboInDevice);
            vboxcomboindevice.getChildren().add(vboxindevice);

            comboOutDevice.setPrefWidth(300);
            comboOutDevice.setPadding(new Insets(5, 0, 5, 0));
            comboOutDevice.setOnAction((event) -> {
                int selectedIndex = comboOutDevice.getSelectionModel().getSelectedIndex();
                Object selectedItem = comboOutDevice.getSelectionModel().getSelectedItem();
                lbloutdevice.setText(comboOutDevice.getValue().toString());
                lbloutdevice.setStyle(styletextred);

                config.setOutDevice(comboOutDevice.getValue().toString());
                sharedStatus.setSelOutDevice(comboOutDevice.getValue().toString());

                seloutdevice = comboOutDevice.getValue().toString();

                seloutdevice = comboOutDevice.getValue().toString();
                buttonSave.setDisable(false);
                System.out.println("MIDI Out:" + comboOutDevice.getValue().toString());

                //System.out.println("Selection made: [" + selectedIndex + "] " + selectedItem);
                //System.out.println("   ComboBox.getValue(): " + comboOutDevice.getValue());
            });
            Label lbloutselect = new Label("Select MIDI OUT:");
            lbloutselect.setStyle(styletextwhite);

            VBox vboxcombooutdevice = new VBox();
            vboxcombooutdevice.setPadding(new Insets(20, 10, 10, 10));
            vboxcombooutdevice.getChildren().add(lbloutselect);
            vboxcombooutdevice.getChildren().add(comboOutDevice);
            vboxcombooutdevice.getChildren().add(vboxoutdevice);

            VBox vboxMidCenter = new VBox();
            vboxMidCenter.setPadding(new Insets(10, 10, 10, 10));
            vboxMidCenter.getChildren().add(lbltopspacer);
            vboxMidCenter.getChildren().add(vboxIntro);
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
            labelstatusOrg.setMinWidth(820 * ymul);
            Label labelsynth = new Label(config.getOutDevice());
            labelsynth.setTextAlignment(TextAlignment.JUSTIFY);
            labelsynth.setStyle(styletext);
            hboxstatus.getChildren().add(labelsynth);

            // Prepare background Image
            try {
                FileInputStream input = new FileInputStream(IMG_DIRECTORY + "backimage.png");
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
