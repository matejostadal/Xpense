package cz.xpense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.stream.Stream;

public class TransactionsHandler {

    private ArrayList<Transaction> allTransactions;
    private ArrayList<Transaction> filteredTransactions;
    private TransactionDatabase transactionDatabase;

    private int lastFilteredMonth;
    private int lastFilteredType;
    private int lastFilteredCategory;


    /** Filters the transactions based on the current filter. Saves new filtered transactions and returns them.*/
    public ArrayList<Transaction> filterTransactions(int filteredMonth, int filteredType, int filteredCategory) {
        // remember last filter in case we need it
        lastFilteredMonth = filteredMonth;
        lastFilteredType = filteredType;
        lastFilteredCategory = filteredCategory;

        Stream<Transaction> transactionStream = allTransactions.stream();

        // filter by month
        if (filteredMonth != 0){
            int wantedMonth = filteredMonth - 1;
            transactionStream = transactionStream.filter(t -> t.getDate().get(Calendar.MONTH) == wantedMonth);
        }

        // filter by type
        if (filteredType == 1){
            transactionStream = transactionStream.filter(Transaction::isExpense);
        } else if (filteredType == 2){
            transactionStream = transactionStream.filter(t -> !t.isExpense());
        }

        // filter by category
        if (filteredCategory != 0) {
            Category wantedCategory = Category.values()[filteredCategory];
            transactionStream = transactionStream.filter(t -> t.getCategory() == wantedCategory);
        }

        filteredTransactions = new ArrayList<>(transactionStream.toList());

        // sorting at the end to keep the app easy to use
        sortTransactions();

        return filteredTransactions;
    }

    /** Sorts transactions by their date. */
    public void sortTransactions(){
        filteredTransactions =
                new ArrayList<>(filteredTransactions
                        .stream()
                        .sorted(Comparator.comparing(Transaction::getDate).reversed())
                        .toList());
    }


    /** Handles deleting of transaction in transactions and database. */
    public void delete(int indexInFiltered) throws TransactionDBException {
        Transaction deletedTransaction = filteredTransactions.get(indexInFiltered);

        allTransactions.remove(deletedTransaction);
        filteredTransactions = filterTransactions(lastFilteredMonth, lastFilteredType, lastFilteredCategory);

        transactionDatabase.deleteTransaction(deletedTransaction.getId());
    }

    /** Handles adding of the transaction into transactions and database. */
    public void add(int indexInFiltered, Transaction transaction) throws TransactionDBException {
        allTransactions.add(indexInFiltered, transaction);
        filteredTransactions = filterTransactions(lastFilteredMonth, lastFilteredType, lastFilteredCategory);

        Calendar date = transaction.getDate();
        String dateString = transformDateToString(date);

        transactionDatabase.createTransaction(transaction.getId(), transaction.getAmount(), transaction.getCategory().ordinal(), dateString, transaction.getNote());

        // sorting at the end to keep the app easy to use
        sortTransactions();
    }

    /** Transforms date into wanted string format. */
    public String transformDateToString(Calendar cal) {
        SimpleDateFormat wantedFormat = new SimpleDateFormat("dd/MM/yyyy");
        return wantedFormat.format(cal.getTime());
    }

    /** Predicate that returns true if transaction is in filtered transactions. */
    public int indexInFiltered(Transaction newTransaction) {
        return filteredTransactions.indexOf(newTransaction);
    }


    public void setAllTransactions(ArrayList<Transaction> allTransactions) {
        this.allTransactions = allTransactions;
    }

    public void setFilteredTransactions(ArrayList<Transaction> filteredTransactions) {
        this.filteredTransactions = filteredTransactions;
    }

    public ArrayList<Transaction> getAllTransactions() {
        return allTransactions;
    }

    public ArrayList<Transaction> getFilteredTransactions() {
        return filteredTransactions;
    }

    public void setTransactionDatabase(TransactionDatabase transactionDatabase) {
        this.transactionDatabase = transactionDatabase;
    }
}
