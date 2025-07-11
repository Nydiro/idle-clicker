package com.idleclicker.idle_clicker;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final DoubleProperty currentMoney = new SimpleDoubleProperty(50.0); // Startkapital auf 50.0 € geändert
    private final DoubleProperty wagePerDish = new SimpleDoubleProperty(0.50); // Start-Einkommen pro Teller

    public DoubleProperty currentMoneyProperty() {
        return currentMoney;
    }

    public double getCurrentMoney() {
        return currentMoney.get();
    }

    public DoubleProperty wagePerDishProperty() {
        return wagePerDish;
    }

    public void washDish() {
        currentMoney.set(currentMoney.get() + wagePerDish.get());
        System.out.println("Teller gewaschen! Du hast jetzt: " + String.format("%.2f", currentMoney.get()) + " €");

        if (currentMoney.get() >= 10.00 && wagePerDish.get() < 1.00) {
            increaseWage(0.50);
        } else if (currentMoney.get() >= 50.00 && wagePerDish.get() < 2.00) {
            increaseWage(1.00);
        }
    }

    public void increaseWage(double amount) {
        if (amount > 0) {
            wagePerDish.set(wagePerDish.get() + amount);
            System.out.println("GEHALTSERHÖHUNG! Dein Einkommen pro Teller ist jetzt: " + String.format("%.2f", wagePerDish.get()) + " €");
        }
    }

    public void addMoney(double amount) {
        if (amount > 0) {
            currentMoney.set(currentMoney.get() + amount);
        }
    }

    public void spendMoney(double amount) {
        if (amount > 0 && currentMoney.get() >= amount) {
            currentMoney.set(currentMoney.get() - amount);
        } else if (amount > 0) {
            System.err.println("Versuch, " + String.format("%.2f", amount) + " € auszugeben, aber nur " + String.format("%.2f", currentMoney.get()) + " € verfügbar.");
        }
    }

    public boolean canAfford(double amount) {
        return currentMoney.get() >= amount;
    }
}