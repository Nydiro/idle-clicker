package com.idleclicker.idle_clicker;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// Diese Klasse repräsentiert eine einzelne Zeile im Portfolio des Spielers.
public class PortfolioItem {
    private final StringProperty symbol;
    private final StringProperty name;
    private final IntegerProperty quantity;
    private final DoubleProperty currentValue;
    // NEUE PROPERTY für den durchschnittlichen Kaufpreis
    private final DoubleProperty averageBuyPrice;
    // NEUE PROPERTY für den prozentualen Gewinn/Verlust
    private final DoubleProperty profitLossPercent;

    public PortfolioItem(String symbol, String name, int quantity, double averageBuyPrice) {
        this.symbol = new SimpleStringProperty(symbol);
        this.name = new SimpleStringProperty(name);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.averageBuyPrice = new SimpleDoubleProperty(averageBuyPrice); // Initialwert setzen
        this.currentValue = new SimpleDoubleProperty(quantity * averageBuyPrice); // Initialwert basierend auf Kaufpreis
        this.profitLossPercent = new SimpleDoubleProperty(0.0); // Initialisierung
        updateProfitLossPercent(); // Initialberechnung
    }

    // Getter für JavaFX TableView
    public String getSymbol() {
        return symbol.get();
    }

    public StringProperty symbolProperty() {
        return symbol;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public double getCurrentValue() {
        return currentValue.get();
    }

    public DoubleProperty currentValueProperty() {
        return currentValue;
    }

    // NEUER GETTER und PROPERTY für durchschnittlichen Kaufpreis
    public double getAverageBuyPrice() {
        return averageBuyPrice.get();
    }

    public DoubleProperty averageBuyPriceProperty() {
        return averageBuyPrice;
    }

    // NEUER GETTER und PROPERTY für prozentualen Gewinn/Verlust
    public double getProfitLossPercent() {
        return profitLossPercent.get();
    }

    public DoubleProperty profitLossPercentProperty() {
        return profitLossPercent;
    }


    // Setter für die Menge und den Wert, die von der Logik (GameController) aktualisiert werden
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public void setCurrentValue(double value) {
        this.currentValue.set(value);
        updateProfitLossPercent(); // Neu: Prozentsatz aktualisieren, wenn sich der Wert ändert
    }

    // NEUER SETTER für den durchschnittlichen Kaufpreis
    public void setAverageBuyPrice(double price) {
        this.averageBuyPrice.set(price);
        updateProfitLossPercent(); // Neu: Prozentsatz aktualisieren, wenn sich der Kaufpreis ändert
    }

    // NEUE METHODE: Berechnet und aktualisiert den prozentualen Gewinn/Verlust
    public void updateProfitLossPercent() {
        if (averageBuyPrice.get() > 0 && quantity.get() > 0) { // Nur berechnen, wenn Aktien vorhanden sind und Kaufpreis > 0
            double currentTotalValue = currentValue.get();
            double originalTotalCost = averageBuyPrice.get() * quantity.get();
            double percent = ((currentTotalValue - originalTotalCost) / originalTotalCost) * 100.0;
            profitLossPercent.set(percent);
        } else {
            profitLossPercent.set(0.0); // Wenn keine Aktien (oder kein Kaufpreis), dann 0%
        }
    }
}