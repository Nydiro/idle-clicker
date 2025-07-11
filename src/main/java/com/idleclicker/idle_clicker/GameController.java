package com.idleclicker.idle_clicker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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

    // --- Services ---
    @Autowired
    private GameService gameService;
    @Autowired
    private StockExchangeService stockExchangeService;
    @Autowired
    private PlayerPortfolio playerPortfolio; // Wird jetzt auch direkt hier benötigt

    // --- Initialisierung des Controllers ---
    @FXML
    public void initialize() {
        // Binding für die Geldanzeige
        moneyLabel.textProperty().bind(gameService.currentMoneyProperty().asString("Geld: %.2f €"));

        // Setup der Aktien-Tabelle
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        // WICHTIG: Binding für den Preis, damit Änderungen automatisch aktualisiert werden
        priceColumn.setCellValueFactory(data -> data.getValue().currentPriceProperty().asObject());
        sectorColumn.setCellValueFactory(new PropertyValueFactory<>("sector"));
        volatilityColumn.setCellValueFactory(new PropertyValueFactory<>("volatility"));

        // Setze die Daten für die Tabelle
        ObservableList<Stock> stocks = FXCollections.observableArrayList(stockExchangeService.getAllStocks());
        stockTableView.setItems(stocks);

        // TODO: Später hier einen Timer für regelmäßige Preis-Updates starten
        // stockExchangeService.updateStockPrices(); // Beispielaufruf
    }

    // --- Event-Handler für den Clicker-Bereich ---
    @FXML
    private void washDish() {
        gameService.washDish();
    }

    // --- Event-Handler für den Aktienmarkt-Bereich (Platzhalter für Kauf/Verkauf) ---
    @FXML
    private void buyStock() {
        Stock selectedStock = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedStock != null) {
            System.out.println("Aktie kaufen: " + selectedStock.getName());
            // TODO: Implementiere Kauf-Logik hier
            // gameService.spendMoney(...)
            // playerPortfolio.addShares(...)
        } else {
            System.out.println("Bitte eine Aktie zum Kaufen auswählen.");
        }
    }

    @FXML
    private void sellStock() {
        Stock selectedStock = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedStock != null) {
            System.out.println("Aktie verkaufen: " + selectedStock.getName());
            // TODO: Implementiere Verkauf-Logik hier
            // gameService.addMoney(...)
            // playerPortfolio.removeShares(...)
        } else {
            System.out.println("Bitte eine Aktie zum Verkaufen auswählen.");
        }
    }

    // --- Diese Methoden und Variablen werden HIER NICHT MEHR BENÖTIGT,
    // da kein Szenenwechsel mehr stattfindet und die Logik integriert wird ---
    // private IdleClickerApplication application;
    // public void setApplication(IdleClickerApplication application) { ... }
    // private void openExchange() { ... }
}