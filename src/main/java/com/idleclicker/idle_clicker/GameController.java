package com.idleclicker.idle_clicker;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GameController {

    // --- FXML-Elemente für den Clicker-Bereich ---
    @FXML
    private Label moneyLabel;

    // --- FXML-Elemente für den Aktienmarkt-Bereich ---
    @FXML
    private TableView<Stock> stockTableView;
    @FXML
    private TableColumn<Stock, String> symbolColumn;
    @FXML
    private TableColumn<Stock, String> nameColumn;
    @FXML
    private TableColumn<Stock, Double> priceColumn;
    @FXML
    private TableColumn<Stock, String> sectorColumn;
    @FXML
    private TableColumn<Stock, String> volatilityColumn;
    @FXML
    private TableColumn<Stock, Integer> ownedSharesColumn; // NEUE SPALTE VERKNÜPFUNG
    @FXML
    private TextField quantityTextField; // NEUES TEXTFELD VERKNÜPFUNG
    @FXML
    private Label messageLabel; // Für Fehlermeldungen/Status

    // --- Services ---
    @Autowired
    private GameService gameService;
    @Autowired
    private StockExchangeService stockExchangeService;
    @Autowired
    private PlayerPortfolio playerPortfolio;

    private Timeline priceUpdateTimeline; // Für automatische Preisupdates

    // --- Initialisierung des Controllers ---
    @FXML
    public void initialize() {
        // Binding für die Geldanzeige
        moneyLabel.textProperty().bind(gameService.currentMoneyProperty().asString("Geld: %.2f €"));

        // Setup der Aktien-Tabelle
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(data -> data.getValue().currentPriceProperty().asObject());
        sectorColumn.setCellValueFactory(new PropertyValueFactory<>("sector"));
        volatilityColumn.setCellValueFactory(new PropertyValueFactory<>("volatility"));

        // Binding für die "Deine Anteile"-Spalte
        // Hier wird es etwas komplexer, da wir die Menge aus dem PlayerPortfolio holen müssen
        ownedSharesColumn.setCellValueFactory(data -> {
            String symbol = data.getValue().getSymbol();
            // Die IntegerProperty aus der ownedShares-Map des PlayerPortfolio zurückgeben.
            // computeIfAbsent stellt sicher, dass eine Property erstellt wird, wenn die Aktie noch nicht im Portfolio ist.
            return playerPortfolio.getOwnedShares().computeIfAbsent(symbol, k -> new javafx.beans.property.SimpleIntegerProperty(0)).asObject();
        });


        // Setze die Daten für die Tabelle
        ObservableList<Stock> stocks = FXCollections.observableArrayList(stockExchangeService.getAllStocks());
        stockTableView.setItems(stocks);

        // Starte den Timer für regelmäßige Preis-Updates
        startPriceUpdateTimer();
    }

    // --- Event-Handler für den Clicker-Bereich ---
    @FXML
    private void washDish() {
        gameService.washDish();
        messageLabel.setText(""); // Fehlermeldung löschen
    }

    // --- Event-Handler für den Aktienmarkt-Bereich ---
    @FXML
    private void buyStock() {
        Stock selectedStock = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            messageLabel.setText("Bitte eine Aktie zum Kaufen auswählen.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityTextField.getText());
            if (quantity <= 0) {
                messageLabel.setText("Menge muss größer als 0 sein.");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Ungültige Menge. Bitte eine Zahl eingeben.");
            return;
        }

        double totalCost = selectedStock.getCurrentPrice() * quantity;

        if (gameService.canAfford(totalCost)) {
            gameService.spendMoney(totalCost);
            playerPortfolio.addShares(selectedStock.getSymbol(), quantity);
            messageLabel.setText(quantity + "x " + selectedStock.getSymbol() + " gekauft für " + String.format("%.2f", totalCost) + " €.");
            // Aktualisiere die Tabelle, um die neuen Anteile anzuzeigen (obwohl Binding helfen sollte)
            stockTableView.refresh();
        } else {
            messageLabel.setText("Nicht genug Geld, um " + quantity + "x " + selectedStock.getSymbol() + " zu kaufen.");
        }
    }

    @FXML
    private void sellStock() {
        Stock selectedStock = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            messageLabel.setText("Bitte eine Aktie zum Verkaufen auswählen.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityTextField.getText());
            if (quantity <= 0) {
                messageLabel.setText("Menge muss größer als 0 sein.");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Ungültige Menge. Bitte eine Zahl eingeben.");
            return;
        }

        int owned = playerPortfolio.getShares(selectedStock.getSymbol());
        if (owned >= quantity) {
            double revenue = selectedStock.getCurrentPrice() * quantity;
            gameService.addMoney(revenue);
            playerPortfolio.removeShares(selectedStock.getSymbol(), quantity);
            messageLabel.setText(quantity + "x " + selectedStock.getSymbol() + " verkauft für " + String.format("%.2f", revenue) + " €.");
            // Aktualisiere die Tabelle
            stockTableView.refresh();
        } else {
            messageLabel.setText("Du besitzt nicht genug Anteile (" + owned + ") von " + selectedStock.getSymbol() + ".");
        }
    }

    // --- Methode für automatische Preisupdates ---
    private void startPriceUpdateTimer() {
        // Aktualisiere die Preise jede Sekunde
        priceUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            stockExchangeService.updateStockPrices();
            // Die TableView aktualisiert sich automatisch, da wir DoubleProperty/IntegerProperty verwenden.
            // stockTableView.refresh(); // Dies ist bei Properties in der Regel nicht nötig, kann aber bei komplexeren Szenarien helfen
        }));
        priceUpdateTimeline.setCycleCount(Timeline.INDEFINITE); // Läuft unendlich
        priceUpdateTimeline.play();
    }
}