import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoaderFXML {
    @FXML private StackPane loaderSpane;
    @FXML private ImageView iv;

    public void initialize() {
        new loaderScreen().start();
    }
    class loaderScreen extends Thread {
        @Override
        public void run() {
                try {
                    //sleeps for 7.5seconds
                    Thread.sleep(7500);
                    //you can't alter contents of UI thread on another thread so post the processing on the thread
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Parent root = null;
                            try {
                                root = FXMLLoader.load(getClass().getResource("sample.fxml"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Stage mainStage = new Stage();
                            mainStage.setTitle("LINE GRAPHER v1.0");
                            mainStage.getIcons().add(new Image("graph-icon.png"));
                            mainStage.setScene(new Scene(root, 800, 600));
                            mainStage.show();

                            loaderSpane.getScene().getWindow().hide();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }
}
