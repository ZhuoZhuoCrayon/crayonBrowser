/*
 * Copyright 2019 caixiaoxin
 */
package demo;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author caixiaoxin
 */
public class JavaFXDemo extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/demo.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("crayon-browser");
            stage.setMaximized(true);//设置启动即最大化

            stage.getIcons().add(new Image("/resources/img/logo3.png"));
            stage.setMinWidth(800);
            stage.setMinHeight(400);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(JavaFXDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
