package cz.xpense;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class MainController {

    private final TransactionsHandler transactionsHandler = new TransactionsHandler();  // for easier work with transactions and database

    @FXML public Text txtBalance;
    private Stage primaryStage;

    @FXML public ComboBox<ObservableList<String>> categoryFilter;
    @FXML public ComboBox<ObservableList<String>> typeFilter;
    @FXML public ComboBox<ObservableList<String>> monthFilter;

    private int filteredCategory = 0;
    private int filteredType = 0;
    private int filteredMonth = 0;

    // view part in window
    @FXML private ListView<String> listTransactions;
    private ObservableList<String> observableTransactions = FXCollections.observableArrayList();

    /** Closing the window */
    @FXML public void exitAction() {
        primaryStage.close();
    }

    /** Makes a dialog window for the user to edit a transaction. */
    @FXML public void onEditAction() throws IOException {
        int clickedIndex = listTransactions.getSelectionModel().getSelectedIndex();
        if (clickedIndex != -1){
            createTransactionWindow(true);
        } else {
            constructDialog("Warning!", "You have to choose a transaction first.");
        }
    }

    /** Makes a dialog window for the user to create a new transaction.*/
    @FXML public void onAddAction() throws IOException {
        createTransactionWindow(false);
    }

    /** Creates a new window for editing or adding. */
    public void createTransactionWindow(boolean mode) throws IOException{
        FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/TransactionWindow.fxml"));
        Parent root = loader.load();

        // controller and stage bonding
        Stage stage = new Stage();
        TransactionController controller = loader.getController();
        controller.setStage(stage);

        setUpTransactionController(controller, mode);

        // window properties setup
        stage.initOwner(this.primaryStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.centerOnScreen();
        if (mode){
            stage.setTitle("Edit transaction dialog");
        } else {
            stage.setTitle("Add transaction dialog");
        }
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    /** Gives all necessary data to the controller, based on the mode (edit/add). */
    public void setUpTransactionController(TransactionController controller, boolean mode) {
        controller.setTransactionsHandler(transactionsHandler);
        controller.setObservableTransactions(observableTransactions);

        // setting of default values
        controller.setBalanceTxt(txtBalance);
        controller.datePicker.setValue(toLocalDate(Calendar.getInstance()));

        if (mode) {
            // getting the selected transaction
            int clickedIndex = listTransactions.getSelectionModel().getSelectedIndex();
            Transaction clickedTransaction = transactionsHandler.getFilteredTransactions().get(clickedIndex);

            // filling amount input zone
            controller.txtAmount.setText(String.valueOf(clickedTransaction.getAmount()));

            // filling category input
            controller.comboBox.getSelectionModel().select(clickedTransaction.getCategory().ordinal());

            // filling note input zone
            controller.txtNote.setText(clickedTransaction.getNote());

            // filling date
            Calendar transactionDate = clickedTransaction.getDate();
            controller.datePicker.setValue(toLocalDate(transactionDate));

            controller.setToEditTransaction(clickedTransaction, clickedIndex);
        }
    }

    /** Action to perform when delete button is pressed. Deletes the selected transaction. */
    @FXML public void onDeleteAction() throws TransactionDBException {
        int indexOfSelected = listTransactions.getSelectionModel().getSelectedIndex();
        if (indexOfSelected != -1) {
            // if something is clicked on, remove the transaction
            transactionsHandler.delete(indexOfSelected);
            observableTransactions.remove(indexOfSelected);
        }
        calculateBalance();
    }

    /** Action to perform when details button is pressed. Opens a dialog with all data regarding the selected transaction. */
    @FXML public void onDetails() {
        int clickedIndex = listTransactions.getSelectionModel().getSelectedIndex();
        if (clickedIndex != -1){
            constructDialog("Details of transaction: ", transactionsHandler.getFilteredTransactions().get(clickedIndex).getDetails());
        } else {
            constructDialog("Warning!", "You have to choose a transaction first.");
        }
    }

    /** Calculates the current balance and corrects the text. */
    public void calculateBalance(){
        int totalBalance = 0;
        for (Transaction transaction : transactionsHandler.getFilteredTransactions()){
            if (transaction.isExpense()){
                totalBalance -= transaction.getAmount();
            } else {
                totalBalance += transaction.getAmount();
            }
        }

        String color = "BLACK";
        if (totalBalance < 0){
            color = "RED";
        } else if (totalBalance > 0){
            color = "GREEN";
        }

        txtBalance.setFill(Paint.valueOf(color));
        txtBalance.setText("CURRENT BALANCE: " + totalBalance);
    }

    /** Constructs a dialog window with given text and title. */
    public void constructDialog(String windowTitle, String text){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(windowTitle);
        ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.setContentText(text);
        dialog.getDialogPane().getButtonTypes().add(type);
        dialog.showAndWait();
    }

    /** Sets all necessary data regarding transaction list. */
    public void setTransactions(ArrayList<Transaction> transactions) {
        transactionsHandler.setAllTransactions(transactions);
        transactionsHandler.setFilteredTransactions(transactions);

        transactionsHandler.sortTransactions();

        fillObservableList();
    }

    /** Everything we need to do as initialization is done here. */
    public void prepare() {
        calculateBalance();

        categoryFilter.setOnAction((actionEvent -> {
            filteredCategory = categoryFilter.getSelectionModel().getSelectedIndex();
            filterTransactions();
        }));

        typeFilter.setOnAction((actionEvent -> {
            filteredType = typeFilter.getSelectionModel().getSelectedIndex();
            filterTransactions();
        }));

        monthFilter.setOnAction((actionEvent -> {
            filteredMonth = monthFilter.getSelectionModel().getSelectedIndex();
            filterTransactions();
        }));
    }

    /** Filters the transactions and view when filter is changed and recalculates balance. */
    private void filterTransactions() {
        transactionsHandler.filterTransactions(filteredMonth, filteredType, filteredCategory);
        fillObservableList();

        calculateBalance();
    }

    /** Changes the observable list based on currently filtered transactions. */
    void fillObservableList(){
        List<String> stringTransactions = new LinkedList<>();

        for (Transaction t : transactionsHandler.getFilteredTransactions()){
            stringTransactions.add(t.toString());
        }

        observableTransactions = FXCollections.observableList(stringTransactions);
        listTransactions.setItems(observableTransactions);
    }

    /** Transforms Calendar to LocalDate class. */
    public static LocalDate toLocalDate(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        TimeZone tz = calendar.getTimeZone();
        ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
        return LocalDate.ofInstant(calendar.toInstant(), zid);
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void setDB(TransactionDatabase transactionDatabase) {
        transactionsHandler.setTransactionDatabase(transactionDatabase);
    }

}
