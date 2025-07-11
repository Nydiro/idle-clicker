package com.idleclicker.idle_clicker;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Stock {

    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty symbol = new SimpleStringProperty(); // Kürzel, z.B. "TII"
    private final StringProperty description = new SimpleStringProperty(); // Kurze Beschreibung des Unternehmens
    private final DoubleProperty currentPrice = new SimpleDoubleProperty();
    private final DoubleProperty initialPrice = new SimpleDoubleProperty(); // Startpreis (für Performance-Tracking)
    private final StringProperty sector = new SimpleStringProperty(); // Branche, z.B. "Technologie"
    private final StringProperty volatility = new SimpleStringProperty(); // Z.B. "Niedrig", "Mittel", "Hoch"

    // Konstruktor
    public Stock(String name, String symbol, String description, double initialPrice, String sector, String volatility) {
        this.name.set(name);
        this.symbol.set(symbol);
        this.description.set(description);
        this.currentPrice.set(initialPrice); // Initialpreis ist auch der Startpreis
        this.initialPrice.set(initialPrice);
        this.sector.set(sector);
        this.volatility.set(volatility);
    }

    // JavaFX Properties (für automatische UI-Updates)
    public StringProperty nameProperty() { return name; }
    public StringProperty symbolProperty() { return symbol; }
    public StringProperty descriptionProperty() { return description; }
    public DoubleProperty currentPriceProperty() { return currentPrice; }
    public DoubleProperty initialPriceProperty() { return initialPrice; }
    public StringProperty sectorProperty() { return sector; }
    public StringProperty volatilityProperty() { return volatility; }

    // Getter (optional, wenn du nur mit Properties arbeitest, aber oft nützlich)
    public String getName() { return name.get(); }
    public String getSymbol() { return symbol.get(); }
    public String getDescription() { return description.get(); }
    public double getCurrentPrice() { return currentPrice.get(); }
    public double getInitialPrice() { return initialPrice.get(); }
    public String getSector() { return sector.get(); }
    public String getVolatility() { return volatility.get(); }

    // Setter (für currentPrice, da dieser sich ändern wird)
    public void setCurrentPrice(double price) {
        this.currentPrice.set(price);
    }
}