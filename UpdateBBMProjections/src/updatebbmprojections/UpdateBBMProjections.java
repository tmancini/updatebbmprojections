package updatebbmprojections;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UpdateBBMProjections extends Application {

  Label status;
  TextField usernameField;
  PasswordField passwordField;
  Button updateButton;
  String spFileName = System.getenv().get("USERPROFILE") + "\\Downloads\\" + "FantasyPros_2016_Projections_P.csv";
  String rpFileName = System.getenv().get("USERPROFILE") + "\\Downloads\\" + "FantasyPros_2016_Projections_RP.csv";
  String hitterFileName = System.getenv().get("USERPROFILE") + "\\Downloads\\" + "FantasyPros_2016_Projections_H.csv";

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    File file = new File("UpdateBBMProjections.log");
    FileOutputStream fos = new FileOutputStream(file);
    PrintStream ps = new PrintStream(fos);
    System.setErr(ps);

    status = new Label("");

    updateButton = new Button("Update Projections");
    updateButton.setOnAction(event -> {
      if(usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
        new Alert(AlertType.ERROR, "Username and password are required!", ButtonType.OK).showAndWait();
      }
      else {
        new Thread(getTask()).start();
      }
    });

    VBox root = new VBox(10);
    root.setPadding(new Insets(5));
    usernameField = new TextField();
    usernameField.setPromptText("Username");
    passwordField = new PasswordField();
    passwordField.setPromptText("Password");

    root.getChildren().addAll(
        new Label(
            "This program updates BBM projections from http://www.fantasypros.com.\nEnter your BBM username and password.  Then click 'Update Projections'."),
        usernameField, passwordField, status, updateButton);
    Scene scene = new Scene(root, 450, 200);

    primaryStage.setTitle("Update Baseball Monster Custom Projections");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private Task<Boolean> getTask() {
    Task<Boolean> task = new Task<Boolean>() {
      @Override
      public Boolean call() throws InterruptedException {
        try {
          updateButton.setDisable(true);
          WebHelper webHelper = new WebHelper(usernameField.getText(), passwordField.getText());
          updateMessage("Downloading SP file...");
          webHelper.downloadExportFile(spFileName, 'S');
          updateMessage("Downloading RP file...");
          webHelper.downloadExportFile(rpFileName, 'R');
          updateMessage("Downloading hitter file...");
          webHelper.downloadExportFile(hitterFileName, 'H');
          updateMessage("Importing projections...");
          webHelper.importFile(hitterFileName, spFileName, rpFileName);
        }
        catch(Exception e) {
          e.printStackTrace();
          return false;
        }
        return true;
      }
    };
    status.textProperty().bind(task.messageProperty());
    task.setOnSucceeded(e -> {
      updateButton.setDisable(false);
      status.textProperty().unbind();
      status.setText(task.getValue() ? "Done!" : "Failed see log.");
      if(!task.getValue()) {
        try {
          Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + "UpdateBBMProjections.log");
        }
        catch(Exception e1) {
          e1.printStackTrace();
        }
      }
    });
    return task;
  }
}
