package com.idleclicker.idle_clicker;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.springframework.stereotype.Component;

import java.util.HashMap; // Jetzt verwendet
import java.util.Map;     // Jetzt verwendet

@Component
public class PlayerPortfolio {

    // Speichert die Anzahl der besessenen Anteile pro Aktiensymbol
    private final ObservableMap<String, IntegerProperty> ownedShares = FXCollections.observableHashMap();
    // NEU: Speichert den durchschnittlichen Kaufpreis pro Aktiensymbol
    private final Map<String, Double> averageBuyPrices = new HashMap<>();
    // NEU: Speichert die realisierten Gewinne/Verluste
    private final DoubleProperty realizedProfitLoss = new SimpleDoubleProperty(0.0);


    public ObservableMap<String, IntegerProperty> getOwnedShares() {
        return ownedShares;
    }

    // NEUER GETTER für realisierte Gewinne/Verluste
    public DoubleProperty realizedProfitLossProperty() {
        return realizedProfitLoss;
    }

    // NEUER GETTER: Gibt die Anzahl der Anteile für ein bestimmtes Symbol zurück
    public int getShares(String symbol) {
        return ownedShares.getOrDefault(symbol, new SimpleIntegerProperty(0)).get();
    }

    // NEUER GETTER: Gibt den durchschnittlichen Kaufpreis für ein Symbol zurück
    public double getAverageBuyPrice(String symbol) {
        return averageBuyPrices.getOrDefault(symbol, 0.0);
    }

    // Überarbeitete Methode zum Hinzufügen von Anteilen (jetzt mit Kaufpreis)
    public void addShares(String symbol, int quantity, double buyPrice) {
        // Ermittle die aktuelle Menge und den aktuellen Gesamtwert der Position
        int currentQuantity = getShares(symbol);
        double currentTotalCost = averageBuyPrices.getOrDefault(symbol, 0.0) * currentQuantity;

        // Berechne den neuen Gesamtwert und die neue Gesamtmenge
        double newTotalCost = currentTotalCost + (buyPrice * quantity);
        int newQuantity = currentQuantity + quantity;

        // Aktualisiere den durchschnittlichen Kaufpreis
        averageBuyPrices.put(symbol, newTotalCost / newQuantity);

        // Aktualisiere die Menge
        ownedShares.computeIfAbsent(symbol, k -> new SimpleIntegerProperty(0)).set(newQuantity);
    }

    // Überarbeitete Methode zum Entfernen von Anteilen (jetzt mit Verkaufspreis und durchschnittlichem Kaufpreis)
    public void removeShares(String symbol, int quantity, double sellPrice, double currentAverageBuyPrice) {
        if (getShares(symbol) >= quantity) {
            // Berechne den Gewinn/Verlust aus diesem Verkauf
            double profitOrLoss = (sellPrice - currentAverageBuyPrice) * quantity;
            realizedProfitLoss.set(realizedProfitLoss.get() + profitOrLoss);

            // Aktualisiere die Menge der Anteile
            int newQuantity = getShares(symbol) - quantity;
            if (newQuantity <= 0) {
                ownedShares.remove(symbol);
                averageBuyPrices.remove(symbol); // Entferne auch den durchschnittlichen Kaufpreis, wenn keine Aktien mehr gehalten werden
            } else {
                ownedShares.get(symbol).set(newQuantity);
                // Der durchschnittliche Kaufpreis bleibt unverändert, wenn nur teilweise verkauft wird.
            }
        }
    }
}