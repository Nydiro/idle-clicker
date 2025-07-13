package com.idleclicker.idle_clicker;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@Component
public class GameController {

    // --- FXML-Elemente für den Clicker-Bereich ---
    @FXML
    private Label moneyLabel;
    @FXML
    private Label wagePerDishLabel;
    // NEU: FXML-Element für Zinssatz
    @FXML
    private Label interestRateLabel;

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
    private TableColumn<PortfolioItem, Double> portfolioProfitLossPercentColumn;
    @FXML
    private Label totalPortfolioValueLabel;
    @FXML
    private Label realizedProfitLossLabel;


    // --- Services ---
    @Autowired
    private GameService gameService;
    @Autowired
    private StockExchangeService stockExchangeService;
    @Autowired
    private PlayerPortfolio playerPortfolio;

    private Timeline priceUpdateTimeline;
    // NEU: Timeline für Zinsgutschrift
    private Timeline interestTimer;

    private Map<String, PortfolioItem> portfolioItemsMap = new HashMap<>();
    private ObservableList<PortfolioItem> observablePortfolioItems = FXCollections.observableArrayList();

    // Für Formatierung von Prozentwerten
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    // NEU: Für Zinssatz-Formatierung
    private static final DecimalFormat INTEREST_FORMAT = new DecimalFormat("0.000");


    // --- Initialisierung des Controllers ---
    @FXML
    public void initialize() {
        moneyLabel.textProperty().bind(gameService.currentMoneyProperty().asString("Geld: %.2f €"));
        wagePerDishLabel.textProperty().bind(gameService.wagePerDishProperty().asString("Einkommen pro Teller: %.2f €"));
        // NEU: Binding für Zinssatz-Label
        interestRateLabel.textProperty().bind(gameService.interestRateProperty().asString("Zinssatz: " + INTEREST_FORMAT.format(gameService.interestRateProperty().get() * 100) + "%% pro Sekunde"));


        // Setup der Aktien-Tabelle
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(data -> data.getValue().currentPriceProperty().asObject());
        sectorColumn.setCellValueFactory(new PropertyValueFactory<>("sector"));

        ObservableList<Stock> stocks = FXCollections.observableArrayList(stockExchangeService.getAllStocks());
        stockTableView.setItems(stocks);

        // Setup der Portfolio-Tabelle
        portfolioSymbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        portfolioQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        portfolioValueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        portfolioProfitLossPercentColumn.setCellValueFactory(new PropertyValueFactory<>("profitLossPercent"));
        portfolioProfitLossPercentColumn.setCellFactory(column -> new javafx.scene.control.TableCell<PortfolioItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(DECIMAL_FORMAT.format(item) + "%");
                    if (item > 0) {
                        setStyle("-fx-text-fill: green;");
                    } else if (item < 0) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });


        portfolioTableView.setItems(observablePortfolioItems);

        playerPortfolio.getOwnedShares().forEach((symbol, quantityProperty) -> {
            Stock stock = stockExchangeService.getStockBySymbol(symbol);
            if (stock != null) {
                double avgBuyPrice = playerPortfolio.getAverageBuyPrice(symbol);
                PortfolioItem item = new PortfolioItem(stock.getSymbol(), stock.getName(), quantityProperty.get(), avgBuyPrice);
                item.setCurrentValue(item.getQuantity() * stock.getCurrentPrice());
                item.updateProfitLossPercent();
                portfolioItemsMap.put(symbol, item);
                observablePortfolioItems.add(item);
            }
        });

        playerPortfolio.getOwnedShares().addListener((MapChangeListener<String, javafx.beans.property.IntegerProperty>) change -> {
            String symbol = change.getKey();
            Stock stock = stockExchangeService.getStockBySymbol(symbol);
            if (stock == null) return;

            if (change.wasAdded()) {
                if (portfolioItemsMap.get(symbol) == null) {
                    int quantity = change.getValueAdded().get();
                    double avgBuyPrice = playerPortfolio.getAverageBuyPrice(symbol);
                    PortfolioItem item = new PortfolioItem(stock.getSymbol(), stock.getName(), quantity, avgBuyPrice);
                    item.setCurrentValue(quantity * stock.getCurrentPrice());
                    item.updateProfitLossPercent();
                    portfolioItemsMap.put(symbol, item);
                    observablePortfolioItems.add(item);
                } else {
                    PortfolioItem item = portfolioItemsMap.get(symbol);
                    item.setQuantity(change.getValueAdded().get());
                    item.setAverageBuyPrice(playerPortfolio.getAverageBuyPrice(symbol));
                    item.setCurrentValue(item.getQuantity() * stock.getCurrentPrice());
                }

            } else if (change.wasRemoved()) {
                PortfolioItem itemToRemove = portfolioItemsMap.remove(symbol);
                if (itemToRemove != null) {
                    observablePortfolioItems.remove(itemToRemove);
                }
            }
        });

        realizedProfitLossLabel.textProperty().bind(playerPortfolio.realizedProfitLossProperty().asString("Realisierte G/V: %.2f €"));


        startPriceUpdateTimer();
        startInterestTimer(); // NEU: Starte den Zins-Timer
    }

    @FXML
    private void washDish() {
        gameService.washDish();
        messageLabel.setText("");
    }

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
            playerPortfolio.addShares(selectedStock.getSymbol(), quantity, selectedStock.getCurrentPrice());

            PortfolioItem item = portfolioItemsMap.get(selectedStock.getSymbol());
            if (item != null) {
                item.setQuantity(playerPortfolio.getShares(selectedStock.getSymbol()));
                item.setAverageBuyPrice(playerPortfolio.getAverageBuyPrice(selectedStock.getSymbol()));
                item.setCurrentValue(item.getQuantity() * selectedStock.getCurrentPrice());
            }

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
            double currentPrice = selectedStock.getCurrentPrice();
            double revenue = currentPrice * quantity;

            double averageBuyPriceAtSale = playerPortfolio.getAverageBuyPrice(selectedStock.getSymbol());
            playerPortfolio.removeShares(selectedStock.getSymbol(), quantity, currentPrice, averageBuyPriceAtSale);

            gameService.addMoney(revenue);

            PortfolioItem item = portfolioItemsMap.get(selectedStock.getSymbol());
            if (item != null) {
                int newQuantity = playerPortfolio.getShares(selectedStock.getSymbol());
                if (newQuantity == 0) {
                    // Der MapChangeListener handhabt das Entfernen
                } else {
                    item.setQuantity(newQuantity);
                    item.setAverageBuyPrice(playerPortfolio.getAverageBuyPrice(selectedStock.getSymbol()));
                    item.setCurrentValue(newQuantity * currentPrice);
                }
            }

            messageLabel.setText(quantity + "x " + selectedStock.getSymbol() + " verkauft für " + String.format("%.2f", revenue) + " €.");
            stockTableView.refresh();
            updateTotalPortfolioValue();

        } else {
            messageLabel.setText("Du besitzt nicht genug Anteile (" + owned + ") von " + selectedStock.getSymbol() + ".");
        }
    }

    private void startPriceUpdateTimer() {
        priceUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            stockExchangeService.updateStockPrices();
            for (PortfolioItem item : observablePortfolioItems) {
                Stock stock = stockExchangeService.getStockBySymbol(item.getSymbol());
                if (stock != null) {
                    item.setCurrentValue(item.getQuantity() * stock.getCurrentPrice());
                }
            }
            updateTotalPortfolioValue();
        }));
        priceUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        priceUpdateTimeline.play();
    }

    // NEU: Timer für Zinsgutschrift
    private void startInterestTimer() {
        interestTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            gameService.applyInterest();
        }));
        interestTimer.setCycleCount(Timeline.INDEFINITE);
        interestTimer.play();
    }


    private void updateTotalPortfolioValue() {
        double totalValue = 0.0;
        for (PortfolioItem item : observablePortfolioItems) {
            totalValue += item.getCurrentValue();
        }
        totalPortfolioValueLabel.setText("Gesamtwert: " + String.format("%.2f", totalValue) + " €");
    }
}