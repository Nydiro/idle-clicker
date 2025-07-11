package com.idleclicker.idle_clicker;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct; // Für die Initialisierung nach Spring-Boot-Start
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class StockExchangeService {

    private final List<Stock> allStocks = new ArrayList<>();
    private final Random random = new Random(); // Für spätere Preisänderungen

    @PostConstruct
    public void initializeStocks() {
        // Hier definieren wir unsere fiktiven Aktien
        allStocks.add(new Stock(
                "Tech Innovators Inc.",
                "TII",
                "Führt die Entwicklung von künstlicher Intelligenz und fortschrittlichen Robotern an.",
                100.00,
                "Technologie",
                "Hoch"
        ));
        allStocks.add(new Stock(
                "Global Energy Corp.",
                "GEC",
                "Ein führender Anbieter von traditionellen und erneuerbaren Energien weltweit.",
                50.00,
                "Energie",
                "Mittel"
        ));
        allStocks.add(new Stock(
                "Consumer Goods United",
                "CGU",
                "Produziert alltägliche Güter des täglichen Bedarfs, von Lebensmitteln bis Haushaltswaren.",
                75.00,
                "Konsumgüter",
                "Niedrig"
        ));
        allStocks.add(new Stock(
                "BioPharma Solutions",
                "BPS",
                "Forschung und Entwicklung neuer Medikamente und medizinischer Technologien.",
                120.00,
                "Gesundheit",
                "Hoch"
        ));
        allStocks.add(new Stock(
                "Universal Robotics & Automation",
                "URA",
                "Spezialisiert auf die Automatisierung von Fertigungsprozessen und Logistik.",
                80.00,
                "Industrie",
                "Mittel"
        ));

        System.out.println("Aktienmarkt initialisiert mit " + allStocks.size() + " Unternehmen.");
    }

    public List<Stock> getAllStocks() {
        // Gibt eine unveränderliche Liste zurück, um unerwünschte externe Modifikationen zu vermeiden
        return Collections.unmodifiableList(allStocks);
    }

    // --- Methoden für spätere Implementierungen (Platzhalter) ---

    // Beispiel für eine zukünftige Preisaktualisierung
    public void updateStockPrices() {
        for (Stock stock : allStocks) {
            double currentPrice = stock.getCurrentPrice();
            double changeFactor;

            // Einfache, zufällige Schwankung basierend auf Volatilität
            // Später durch komplexere Modelle und Events ersetzt
            switch (stock.getVolatility()) {
                case "Niedrig":
                    changeFactor = 1.0 + (random.nextDouble() - 0.5) * 0.02; // +/- 1%
                    break;
                case "Mittel":
                    changeFactor = 1.0 + (random.nextDouble() - 0.5) * 0.04; // +/- 2%
                    break;
                case "Hoch":
                    changeFactor = 1.0 + (random.nextDouble() - 0.5) * 0.08; // +/- 4%
                    break;
                default:
                    changeFactor = 1.0;
            }

            double newPrice = currentPrice * changeFactor;
            // Sicherstellen, dass der Preis nicht unter einen bestimmten Wert fällt
            if (newPrice < 1.0) newPrice = 1.0;
            stock.setCurrentPrice(newPrice);
        }
        // System.out.println("Aktienkurse aktualisiert."); // Nur zur Debugging-Ausgabe
    }

    // Methode zum Abrufen einer Aktie nach Symbol (für Kauf/Verkauf)
    public Stock getStockBySymbol(String symbol) {
        return allStocks.stream()
                .filter(stock -> stock.getSymbol().equalsIgnoreCase(symbol))
                .findFirst()
                .orElse(null); // Oder eine Exception werfen
    }

    // Methoden für Kauf und Verkauf kommen später im PlayerPortfolio
}