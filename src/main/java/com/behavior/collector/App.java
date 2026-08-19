package com.behavior.collector;

import com.behavior.collector.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Main JavaFX Application class.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Behavioral Data Collector - Final Year Biometric Project");

        MainView mainView = new MainView(primaryStage);
        Scene scene = new Scene(mainView.buildUI(), 900, 700);

        // Load custom stylesheet
        URL cssUrl = getClass().getResource("/styles/main.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // Attach mouse, keyboard & window listeners to the scene
        mainView.attachEventHandlers(scene);

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(840);
        primaryStage.setMinHeight(640);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
