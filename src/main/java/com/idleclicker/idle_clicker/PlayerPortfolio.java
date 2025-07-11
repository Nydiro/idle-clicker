package com.idleclicker.idle_clicker;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.springframework.stereotype.Component;

@Component // Macht dies zu einer Spring-Komponente, falls es direkt injiziert werden soll
public class PlayerPortfolio {

    // Speichert die Anzahl der Anteile pro Aktiensymbol
    // ObservableMap für automatische UI-Updates, falls ein TableView direkt daran gebunden wird
    private final ObservableMap<String, IntegerProperty> ownedShares = FXCollections.observableHashMap();

    // Methode zum Hinzufügen von Anteilen
    public void addShares(String symbol, int quantity) {
        ownedShares.computeIfAbsent(symbol, k -> new SimpleIntegerProperty(0))
                   .set(ownedShares.get(symbol).get() + quantity);
        System.out.println("Portfolio aktualisiert: " + quantity + " Anteile von " + symbol + " hinzugefügt.");
    }

    // Methode zum Entfernen von Anteilen
    public void removeShares(String symbol, int quantity) {
        IntegerProperty currentQuantity = ownedShares.get(symbol);
        if (currentQuantity != null && currentQuantity.get() >= quantity) {
            currentQuantity.set(currentQuantity.get() - quantity);
            if (currentQuantity.get() == 0) {
                ownedShares.remove(symbol); // Entferne den Eintrag, wenn 0 Anteile
            }
            System.out.println("Portfolio aktualisiert: " + quantity + " Anteile von " + symbol + " entfernt.");
        } else {
            System.err.println("Fehler: Nicht genug Anteile von " + symbol + " zum Entfernen.");
        }
    }

    // Getter für die Anzahl der Anteile einer bestimmten Aktie
    public int getShares(String symbol) {
        IntegerProperty quantity = ownedShares.get(symbol);
        return (quantity != null) ? quantity.get() : 0;
    }

    // Getter für die gesamte ObservableMap (für UI-Binding)
    public ObservableMap<String, IntegerProperty> getOwnedShares() {
        return ownedShares;
    }
}