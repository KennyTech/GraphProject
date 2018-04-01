//package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage loaderStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("loaderFXML.fxml"));
        loaderStage.setScene(new Scene(root, 600, 400));
        loaderStage.getIcons().add(new Image("graph-icon.png"));
        loaderStage.setTitle("Please wait...");
        loaderStage.show();



    }


    public static void main(String[] args) {
        launch(args);
    }
}