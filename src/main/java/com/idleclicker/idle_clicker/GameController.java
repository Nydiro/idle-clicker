package com.idleclicker.idle_clicker;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

import java.util.HashMap;
import java.util.Map;

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
    private TableColumn<Stock, Integer> ownedSharesColumn;
    @FXML
    private TextField quantityTextField;
    @FXML
    private Label messageLabel;

    // --- FXML-Elemente für den Portfolio-Bereich ---
    @FXML
    private TableView<PortfolioItem> portfolioTableView;
    @FXML
    private TableColumn<PortfolioItem, String> portfolioSymbolColumn;
    @FXML
    private TableColumn<PortfolioItem, Integer> portfolioQuantityColumn;
    @FXML
    private TableColumn<PortfolioItem, Double> portfolioValueColumn;
    @FXML
    private Label totalPortfolioValueLabel;


    // --- Services ---
    @Autowired
    private GameService gameService;
    @Autowired
    private StockExchangeService stockExchangeService;
    @Autowired
    private PlayerPortfolio playerPortfolio;

    private Timeline priceUpdateTimeline;

    // Map, um schnell auf PortfolioItem-Objekte zugreifen zu können, basierend auf dem Aktiensymbol
    private Map<String, PortfolioItem> portfolioItemsMap = new HashMap<>();
    // ObservableList für die Portfolio-Tabelle
    private ObservableList<PortfolioItem> observablePortfolioItems = FXCollections.observableArrayList();


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
        ownedSharesColumn.setCellValueFactory(data ->
            playerPortfolio.getOwnedShares().computeIfAbsent(
                data.getValue().getSymbol(), k -> new javafx.beans.property.SimpleIntegerProperty(0)
            ).asObject()
        );

        // Setze die Daten für die Aktien-Tabelle
        ObservableList<Stock> stocks = FXCollections.observableArrayList(stockExchangeService.getAllStocks());
        stockTableView.setItems(stocks);

        // Setup der Portfolio-Tabelle
        portfolioSymbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        portfolioQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        portfolioValueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));

        // Setze die Daten für die Portfolio-Tabelle
        portfolioTableView.setItems(observablePortfolioItems);

        // Initialisiere das Portfolio, falls der Spieler bereits Aktien besitzt (z.B. aus einem Savegame)
        playerPortfolio.getOwnedShares().forEach((symbol, quantityProperty) -> {
            Stock stock = stockExchangeService.getStockBySymbol(symbol);
            if (stock != null) {
                PortfolioItem item = new PortfolioItem(stock.getSymbol(), stock.getName(), quantityProperty.get(), stock.getCurrentPrice());
                portfolioItemsMap.put(symbol, item);
                observablePortfolioItems.add(item);
            }
        });

        // Listener für Änderungen im PlayerPortfolio, um die Portfolio-Tabelle zu aktualisieren
        playerPortfolio.getOwnedShares().addListener((javafx.collections.MapChangeListener<String, javafx.beans.property.IntegerProperty>) change -> {
            if (change.wasAdded()) {
                String symbol = change.getKey();
                int quantity = change.getValueAdded().get();
                Stock stock = stockExchangeService.getStockBySymbol(symbol);
                if (stock != null) {
                    PortfolioItem item = new PortfolioItem(stock.getSymbol(), stock.getName(), quantity, stock.getCurrentPrice());
                    portfolioItemsMap.put(symbol, item);
                    observablePortfolioItems.add(item);
                }
            } else if (change.wasRemoved()) {
                String symbol = change.getKey();
                PortfolioItem itemToRemove = portfolioItemsMap.remove(symbol);
                if (itemToRemove != null) {
                    observablePortfolioItems.remove(itemToRemove);
                }
            }
            // Bei Mengenänderungen (innerhalb eines bestehenden Items) muss der Wert in der Timeline aktualisiert werden.
            // Der quantityProperty Listener im PortfolioItem kümmert sich bereits darum, wenn seine Menge gesetzt wird.
            // Die Aktualisierung des Wertes geschieht primär über die Preis-Updates.
        });


        // Starte den Timer für regelmäßige Preis-Updates
        startPriceUpdateTimer();
    }

    // --- Event-Handler für den Clicker-Bereich ---
    @FXML
    private void washDish() {
        gameService.washDish();
        messageLabel.setText("");
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

            // Aktualisiere oder füge PortfolioItem hinzu
            PortfolioItem item = portfolioItemsMap.get(selectedStock.getSymbol());
            if (item == null) {
                item = new PortfolioItem(selectedStock.getSymbol(), selectedStock.getName(), 0, selectedStock.getCurrentPrice());
                portfolioItemsMap.put(selectedStock.getSymbol(), item);
                observablePortfolioItems.add(item);
            }
            // Holen der tatsächlichen Menge aus dem PlayerPortfolio, falls es Updates gab (z.B. bei Mehrfachkäufen)
            item.setQuantity(playerPortfolio.getShares(selectedStock.getSymbol()));
            item.setCurrentValue(item.getQuantity() * selectedStock.getCurrentPrice());

            messageLabel.setText(quantity + "x " + selectedStock.getSymbol() + " gekauft für " + String.format("%.2f", totalCost) + " €.");
            stockTableView.refresh();
            updateTotalPortfolioValue();

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

            // Aktualisiere oder entferne PortfolioItem
            PortfolioItem item = portfolioItemsMap.get(selectedStock.getSymbol());
            if (item != null) {
                int newQuantity = playerPortfolio.getShares(selectedStock.getSymbol());
                item.setQuantity(newQuantity);
                item.setCurrentValue(newQuantity * selectedStock.getCurrentPrice());
                if (newQuantity == 0) { // Wenn Menge 0 ist, aus der Tabelle entfernen
                    portfolioItemsMap.remove(selectedStock.getSymbol());
                    observablePortfolioItems.remove(item);
                }
            }

            messageLabel.setText(quantity + "x " + selectedStock.getSymbol() + " verkauft für " + String.format("%.2f", revenue) + " €.");
            stockTableView.refresh();
            updateTotalPortfolioValue();
        } else {
            messageLabel.setText("Du besitzt nicht genug Anteile (" + owned + ") von " + selectedStock.getSymbol() + ".");
        }
    }

    // --- Methode für automatische Preisupdates ---
    private void startPriceUpdateTimer() {
        priceUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            stockExchangeService.updateStockPrices();
            // Aktualisiere die Werte in der Portfolio-Tabelle, da sich die Preise geändert haben könnten
            for (PortfolioItem item : observablePortfolioItems) {
                Stock stock = stockExchangeService.getStockBySymbol(item.getSymbol());
                if (stock != null) {
                    item.setCurrentValue(item.getQuantity() * stock.getCurrentPrice());
                }
            }
            updateTotalPortfolioValue(); // Gesamtwert aktualisieren
        }));
        priceUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        priceUpdateTimeline.play();
    }

    // NEUE METHODE: Berechnet und aktualisiert den Gesamtwert des Portfolios
    private void updateTotalPortfolioValue() {
        double totalValue = 0.0;
        for (PortfolioItem item : observablePortfolioItems) {
            totalValue += item.getCurrentValue();
        }
        totalPortfolioValueLabel.setText("Gesamtwert: " + String.format("%.2f", totalValue) + " €");
    }
}