package cz.xpense;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TransactionDatabase implements AutoCloseable {

    private final PreparedStatement getTransactionByIDStmt;
    private final PreparedStatement getAllTransactionsStmt;
    private final PreparedStatement deleteTransactionStmt;
    private final PreparedStatement insertTransactionStmt;

    public TransactionDatabase(Connection con) throws TransactionDBException {
        try {
            getTransactionByIDStmt = con.prepareStatement("SELECT * FROM transactions WHERE (transaction_id = ?)");
            deleteTransactionStmt = con.prepareStatement("DELETE FROM transactions WHERE (transaction_id = ?)");
            getAllTransactionsStmt = con.prepareStatement("SELECT * FROM transactions");
            insertTransactionStmt    = con.prepareStatement("INSERT INTO transactions (transaction_id, amount, category_id, date, note) VALUES (?, ?, ?, ?, ?)");
        } catch (SQLException e) {
            throw new TransactionDBException("Unable to initialize prepared statements.", e);
        }
    }

    /** Deletes transaction with given id from the database. */
    public void deleteTransaction(int id) throws TransactionDBException {
        try {
            deleteTransactionStmt.setInt(1, id);
            deleteTransactionStmt.executeUpdate();
        } catch (SQLException e) {
            throw new TransactionDBException("Unable to delete transaction.", e);
        }
    }

    /** Adds new transaction into the database. */
    public void createTransaction(int id, int total, int categoryID, String date, String note) throws TransactionDBException {
        try {
            insertTransactionStmt.setInt(1, id);
            insertTransactionStmt.setInt(2, total);
            insertTransactionStmt.setInt(3, categoryID);
            insertTransactionStmt.setString(4, date);
            insertTransactionStmt.setString(5, note);
            insertTransactionStmt.executeUpdate();
        } catch (SQLException e) {
            throw new TransactionDBException("Unable to create new transaction.", e);
        }
    }

    /** Returns all transactions. */
    public ArrayList<Transaction> getAllTransactions() throws TransactionDBException {
        ArrayList<Transaction> transactions = new ArrayList<>();
        try {
            try (ResultSet results = getAllTransactionsStmt.executeQuery()) {
                while (results.next()){
                    transactions.add(new Transaction(
                            results.getInt("transaction_id"),
                            results.getInt("amount"),
                            results.getInt("category_id"),
                            transformToDate(results.getString("date")),
                            results.getString("note")));
                }
            }
        } catch (SQLException | ParseException e){
            throw new TransactionDBException("Unable to find receipt's items.", e);
        }
        return transactions;
    }

    public Calendar transformToDate(String d) throws ParseException {
        String dateFormat = "dd/MM/yyyy";
        Date wantedDate =  new SimpleDateFormat(dateFormat).parse(d);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(wantedDate);

        return calendar;
    }


    @Override
    public void close() {
        try {
            getTransactionByIDStmt.close();
            getAllTransactionsStmt.close();
            deleteTransactionStmt.close();
            insertTransactionStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
