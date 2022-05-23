package cz.xpense;

public class TransactionDBException extends Exception {

    public TransactionDBException(String msg, Exception e) {
        super(msg, e);
    }
}