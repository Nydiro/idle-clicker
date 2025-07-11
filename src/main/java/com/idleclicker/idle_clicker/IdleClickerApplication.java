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
import java.net.URL; // Import für URL

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
    public void start(Stage stage) throws IOException {
        // NEUE GEÄNDERTE ZEILE: Laden über den Thread-Kontext-ClassLoader
        URL fxmlLocation = Thread.currentThread().getContextClassLoader().getResource("com/idleclicker/idle_clicker/main-view.fxml");

        if (fxmlLocation == null) {
            System.err.println("Fehler: main-view.fxml nicht gefunden im Classpath unter com/idleclicker/idle_clicker/");
            throw new IOException("FXML-Datei nicht gefunden: com/idleclicker/idle_clicker/main-view.fxml");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        fxmlLoader.setControllerFactory(applicationContext::getBean);

        Parent root = fxmlLoader.load();

        stage.setTitle("Idle Clicker Game: Tellerwäscher");
        stage.setScene(new Scene(root, 800, 600));
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