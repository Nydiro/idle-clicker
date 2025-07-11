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
    private final IntegerProperty quantity; // Anzahl der besessenen Anteile
    private final DoubleProperty currentValue; // Aktueller Gesamtwert dieser Position

    public PortfolioItem(String symbol, String name, int quantity, double initialPrice) {
        this.symbol = new SimpleStringProperty(symbol);
        this.name = new SimpleStringProperty(name);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.currentValue = new SimpleDoubleProperty(quantity * initialPrice); // Initialwert setzen
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

    // Setter für die Menge und den Wert, die von der Logik (GameController) aktualisiert werden
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public void setCurrentValue(double value) {
        this.currentValue.set(value);
    }
}