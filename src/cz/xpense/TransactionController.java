package cz.xpense;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.zip.DataFormatException;


public class TransactionController {

    @FXML public TextField txtAmount;
    @FXML public TextField txtNote;
    @FXML public Text lbNotification;
    @FXML public ComboBox<ObservableList<String>> comboBox;
    @FXML public DatePicker datePicker;
    private Stage stage;

    private ObservableList<String> observableTransactions = FXCollections.emptyObservableList();

    private Transaction toEditTransaction;
    private int toEditTransactionIndex = 0;
    private Text txtBalance;
    private TransactionsHandler transactionsHandler;
    private Object StringConverter;

    /** Action to perform when OK button is pressed. Fills data and handles valid inputs. */
    @FXML public void okAction() throws TransactionDBException {
        try {
            // checking if input is valid (must be a positive integer)
            int amount = Integer.parseInt(txtAmount.getText());
            if (amount < 0) {
                throw new NumberFormatException();
            }

            int categoryID = comboBox.getSelectionModel().getSelectedIndex();
            String note = txtNote.getText();

            if (toEditTransaction != null){
                observableTransactions.remove(toEditTransactionIndex);
                transactionsHandler.delete(toEditTransactionIndex);
            }

            // internal exception because of parsing is possible
            // but causes no problems to the app consistency
            LocalDate givenDate = datePicker.getValue();

            if (givenDate == null){
                throw new DataFormatException();
            }

            addNewTransaction(toEditTransactionIndex, amount, categoryID, givenDate, note);

            lbNotification.setText("");
            stage.close();
        } catch (NumberFormatException e) {
            lbNotification.setFill(Paint.valueOf("RED"));
            lbNotification.setText("Amount must be a positive integer!");
        }  catch (Exception e) {
            lbNotification.setFill(Paint.valueOf("RED"));
            lbNotification.setText("Select valid date!");
        }
    }

    /** Adds new transaction (or edited) into transactions using the data from input. */
    public void addNewTransaction(int index, int amount, int categoryID, LocalDate localDate, String note) throws TransactionDBException {
        int maxID = transactionsHandler.getAllTransactions().stream().mapToInt(Transaction::getId).max().orElse(0);
        Calendar date = toCalendar(localDate);

        Transaction newTransaction = new Transaction(maxID + 1, amount, categoryID , date, note);
        transactionsHandler.add(index, newTransaction);

        // check if we want to see this transaction based on current filters and if yes, place it to correct place
        int indexInFiltered = transactionsHandler.indexInFiltered(newTransaction);
        if (indexInFiltered != -1){
            observableTransactions.add(indexInFiltered, newTransaction.toString());
        }

        calculateBalance();
    }

    /** Transforms LocalDate to Calendar class. */
    public Calendar toCalendar(LocalDate date){
        Calendar calendar = Calendar.getInstance();
        calendar.set(date.getYear(), date.getMonthValue()-1 ,date.getDayOfMonth());
        return calendar;
    }

    /** Action to perform when cancel button is pressed. */
    @FXML public void exitAction() {
        stage.close();
    }

    /** Calculates the current balance based on filtered transactions. */
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

    public void setTransactionsHandler(TransactionsHandler transactionsHandler) {
        this.transactionsHandler = transactionsHandler;
    }

    public void setBalanceTxt(Text txtBalance) {
        this.txtBalance = txtBalance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    public void setObservableTransactions(ObservableList<String> observableTransactions) {
        this.observableTransactions = observableTransactions;
    }

    public void setToEditTransaction(Transaction toEditTransaction, int toEditTransactionIndex) {
        this.toEditTransaction = toEditTransaction;
        this.toEditTransactionIndex = toEditTransactionIndex;
    }
}
