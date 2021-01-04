package amidifx.scenes;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.File;

public class WelcomeScene {

    Stage primaryStage;
    Scene returnScene;

    // main pane for the scene
    private BorderPane paneWelcome;
    Scene sceneWelcome;

    private Button btnStart;

    File fstyle;

    /*********************************************************
     * Creates Welcome Scene.
     *********************************************************/

    public WelcomeScene(Stage primaryStage, Scene returnScene) {

        System.out.println("AMIDIFX Welcome Scene Starting");

        try {
            Parent root = FXMLLoader.load(getClass().getResource("../amidifx.fxml"));
            System.out.println("root: " + root.toString());

            File fstyle = new File("src/amidifx/style.css");
            System.out.println("fstyle: " + fstyle.toString());

            paneWelcome = new BorderPane();
            paneWelcome.setStyle("-fx-background-color: #999999; ");

            sceneWelcome = new Scene(paneWelcome, 300, 300);
            sceneWelcome.getStylesheets().clear();
            sceneWelcome.getStylesheets().add("file:///" + fstyle.getAbsolutePath().replace("\\", "/"));

            Text txtWelcome = new Text("Welcome to AMIDIFX!");
            txtWelcome.setStyle("-fx-background-color: #999999; ");
            txtWelcome.setDisable(true);
            HBox vboxWelcome = new HBox();
            vboxWelcome.setPadding(new Insets(10, 10, 10, 10));
            vboxWelcome.getChildren().add(txtWelcome);
            vboxWelcome.setAlignment(Pos.CENTER);

            TextArea txtIntro = new TextArea("AMIDIFX integrates MIDI sound modules with keyboards enabling live play with backing tracks. " +
                    "At this time we support the Deebach BlackBox as well as MIDI GM compliant modules with more planned.");
            txtIntro.setStyle("-fx-background-color: #999999; ");
            txtIntro.setWrapText(true);
            txtIntro.setDisable(true);
            HBox vboxIntro = new HBox();
            vboxIntro.setPadding(new Insets(10, 10, 10, 10));
            vboxIntro.getChildren().add(txtIntro);
            //vboxIntro.setAlignment(Pos.CENTER);

            btnStart = new Button("Start");
            btnStart.setPrefSize(75, 30);
            btnStart.setStyle("-fx-background-color: #69a8cc; ");
            btnStart.setOnAction(e -> {
                primaryStage.setScene(returnScene);
                try {
                    Thread.sleep(600);
                } catch (Exception ex) {
                }
            });
            HBox hboxStartButton = new HBox();
            hboxStartButton.setPadding(new Insets(10, 10, 10, 10));
            hboxStartButton.getChildren().add(btnStart);
            hboxStartButton.setAlignment(Pos.CENTER);

            paneWelcome.setTop(vboxWelcome);
            paneWelcome.setCenter(vboxIntro);
            paneWelcome.setBottom(hboxStartButton);
        }
        catch (Exception ex) {
            System.out.println("WelcomeScene Exception: Unable to read Stylesheets!");
            System.out.println(ex);
        }
    }

    /** Returns the current scene **/
    public Scene getScene() {
        return sceneWelcome;
    }
}
