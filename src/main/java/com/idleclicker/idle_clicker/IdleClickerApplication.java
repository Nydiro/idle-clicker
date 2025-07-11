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

import java.io.IOException; // Dieser Import ist notwendig

public class IdleClickerApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        // Initialisiert den Spring ApplicationContext
        // und registriert diese JavaFX Application Instanz als Spring Bean.
        applicationContext = new SpringApplicationBuilder(Launcher.class) // Nutze Launcher.class
                .sources(IdleClickerApplication.class)
                .initializers((ApplicationContextInitializer<GenericApplicationContext>) applicationContext -> {
                    applicationContext.registerBean(Application.class, () -> this);
                })
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml")); // Korrekter Pfad
        fxmlLoader.setControllerFactory(applicationContext::getBean); // Wichtig für Spring @Autowired

        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load(), 1200, 700); // Angepasste Größe
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Fehler beim Laden der FXML-Datei: main-view.fxml");
            throw e; // Wirf die Exception weiter, um den Fehler anzuzeigen
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