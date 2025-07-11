package com.idleclicker.idle_clicker;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class IdleClickerApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        // Initialisiert den Spring ApplicationContext
        // und registriert diese JavaFX Application Instanz als Spring Bean.
        // Launcher.class ist die Klasse mit @SpringBootApplication
        applicationContext = new SpringApplicationBuilder(Launcher.class)
                .sources(IdleClickerApplication.class) // Füge diese Klasse als Spring-Quelle hinzu
                .initializers((ApplicationContextInitializer<GenericApplicationContext>) applicationContext -> {
                    applicationContext.registerBean(Application.class, () -> this);
                })
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Dies ist die Hauptmethode für deine JavaFX UI.
        // Aktuell zeigt sie nur ein leeres Fenster.
        stage.setTitle("Idle Clicker Game");
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();
    }

    @Override
    public void stop() {
        // Schließt den Spring ApplicationContext, wenn die JavaFX-App beendet wird.
        applicationContext.close();
        // Platform.exit() ist wichtig, um den JavaFX-Thread sauber zu beenden
        Platform.exit();
    }

    public static void main(String[] args) {
        // Startet die JavaFX-Anwendung
        // Diese Methode ist der wahre Einstiegspunkt deines Programms
        launch(args);
    }
}