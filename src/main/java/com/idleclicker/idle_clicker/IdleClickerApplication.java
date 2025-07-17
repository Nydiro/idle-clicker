package com.idleclicker.idle_clicker;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.io.IOException;

public class IdleClickerApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(Launcher.class)
                .sources(IdleClickerApplication.class)
                .initializers((ApplicationContextInitializer<GenericApplicationContext>) applicationContext -> {
                    applicationContext.registerBean(Application.class, () -> this);
                })
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        fxmlLoader.setControllerFactory(applicationContext::getBean);

        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load(), 1200, 700);
            // NEU: CSS-Datei zur Szene hinzufügen
            scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Fehler beim Laden der FXML-Datei: main-view.fxml");
            throw e;
        }

        stage.setTitle("Idle Clicker Game");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}