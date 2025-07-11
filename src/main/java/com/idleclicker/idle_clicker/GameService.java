package com.idleclicker.idle_clicker;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final DoubleProperty currentMoney = new SimpleDoubleProperty(0.0);
    private final double dishWashValue = 0.50; // Geld pro Teller

    public DoubleProperty currentMoneyProperty() {
        return currentMoney;
    }

    public void washDish() {
        currentMoney.set(currentMoney.get() + dishWashValue);
        System.out.println("Teller gewaschen! Aktuelles Geld: " + String.format("%.2f", currentMoney.get()) + " €");
    }
}