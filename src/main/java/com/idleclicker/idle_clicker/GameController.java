package com.idleclicker.idle_clicker;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component // Macht diesen Controller zu einer Spring-Komponente
public class GameController {

    @FXML
    private Label moneyLabel; // Verknüpft mit fx:id="moneyLabel" in FXML

    @Autowired
    private GameService gameService; // Spring injiziert unseren GameService

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    @FXML
    public void initialize() {
        // Bindet das Text-Property des Labels an das currentMoney-Property des GameService
        // Wenn sich currentMoney ändert, aktualisiert sich das Label automatisch.
        moneyLabel.textProperty().bind(gameService.currentMoneyProperty().asString("Geld: %.2f €"));
    }

    @FXML
    private void washDish() {
        gameService.washDish();
    }
}