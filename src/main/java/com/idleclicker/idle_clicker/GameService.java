package com.idleclicker.idle_clicker;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final DoubleProperty currentMoney = new SimpleDoubleProperty(0.0); // Startkapital

    public DoubleProperty currentMoneyProperty() {
        return currentMoney;
    }

    public double getCurrentMoney() {
        return currentMoney.get();
    }

    public void washDish() {
        double earning = 0.50; // Jeder Teller bringt 0.50 €
        currentMoney.set(currentMoney.get() + earning);
        System.out.println("Teller gewaschen! Du hast jetzt: " + String.format("%.2f", currentMoney.get()) + " €");
    }

    // NEUE METHODE: Geld hinzufügen
    public void addMoney(double amount) {
        if (amount > 0) {
            currentMoney.set(currentMoney.get() + amount);
            // Optional: System.out.println("Geld hinzugefügt: " + String.format("%.2f", amount) + " €. Neues Guthaben: " + String.format("%.2f", currentMoney.get()) + " €");
        }
    }

    // NEUE METHODE: Geld ausgeben
    public void spendMoney(double amount) {
        if (amount > 0 && currentMoney.get() >= amount) {
            currentMoney.set(currentMoney.get() - amount);
            // Optional: System.out.println("Geld ausgegeben: " + String.format("%.2f", amount) + " €. Neues Guthaben: " + String.format("%.2f", currentMoney.get()) + " €");
        } else if (amount > 0) {
            System.err.println("Versuch, " + String.format("%.2f", amount) + " € auszugeben, aber nur " + String.format("%.2f", currentMoney.get()) + " € verfügbar.");
        }
    }

    // NEUE METHODE: Prüfen, ob genug Geld vorhanden ist
    public boolean canAfford(double amount) {
        return currentMoney.get() >= amount;
    }
}