package com.idleclicker.idle_clicker;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.io.IOException;
import java.net.URL;

public class IdleClickerApplication extends Application {

    private ConfigurableApplicationContext applicationContext;
    private Stage primaryStage; // Wir behalten diese Referenz, falls wir sie später brauchen, z.B. für Fenstergröße

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
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Idle Clicker Game: Tellerwäscher to Tycoon"); // Neuer, passenderer Titel
        primaryStage.setWidth(1000); // Fensterbreite anpassen, da mehr Inhalt
        primaryStage.setHeight(700); // Fensterhöhe anpassen

        try {
            // Lade die Hauptansicht, die jetzt alle Bereiche enthält
            URL fxmlLocation = Thread.currentThread().getContextClassLoader().getResource("com/idleclicker/idle_clicker/main-view.fxml");
            if (fxmlLocation == null) {
                throw new IOException("FXML-Datei nicht gefunden: com/idleclicker/idle_clicker/main-view.fxml");
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            fxmlLoader.setControllerFactory(applicationContext::getBean); // Spring soll den Controller instanziieren

            Parent root = fxmlLoader.load();
            primaryStage.setScene(new Scene(root, primaryStage.getWidth(), primaryStage.getHeight()));
            primaryStage.show();

            // Hier können wir optional direkt auf den GameController zugreifen, falls nötig
            // GameController gameController = fxmlLoader.getController();

        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Hauptansicht: " + e.getMessage());
            e.printStackTrace();
            Platform.exit(); // Beende die Anwendung bei einem kritischen Fehler
        }
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Die Methoden loadView(), showMainView() und showExchangeView() werden entfernt,
    // da der Szenenwechsel nicht mehr benötigt wird.
}