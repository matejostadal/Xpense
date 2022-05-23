package cz.xpense;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Transaction {

    private final int id;
    private int amount;     // money spent/earned
    private Category category;
    private Calendar date;  // date of the transaction creation
    private String note;

    public Transaction(int id, int amount, int categoryID, Calendar date, String note){
        this.id = id;
        this.amount = amount;
        this.category = Category.values()[categoryID];
        this.date = date;
        this.note = note;
    }

    /** Converts calendar date to a string in a specified format. */
    public String transformToString(Calendar cal) {
        SimpleDateFormat wantedFormat = new SimpleDateFormat("dd/MM/yyyy");
        return wantedFormat.format(cal.getTime());
    }

    @Override
    public String toString(){
        String sign = "+";
        if (category.isExpense()) {
            sign = "-";
        }
        return "[" + sign + amount +" CZK] \t" + " - { " + category.name() + " } \t DATE: " + transformToString(date);
    }

    public String getDetails() {
        String sign = "+";
        if (category.isExpense()) {
            sign = "-";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[" + sign + amount +" CZK] \t\t\t\t" + "CATEGORY: " + category.name());
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("DATE:\t" + transformToString(date));
        sb.append(System.lineSeparator());
        sb.append("NOTE:\t" + note);
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("(TRANSACTION ID:\t" +  id + ")");

        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isExpense(){
        return category.isExpense();
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
