package cz.xpense;

public enum Category {

    DEFAULT(false),
    GROCERIES(true),
    RENT(true),
    SALARY(false),
    TRANSPORTATION(true),
    TRAVEL(true),
    POCKET_MONEY(false),
    HOBBY(true),
    PART_TIME_JOB(false),
    CULTURE(true),
    LUNCH(true),
    COFFEE(true),
    SNACKING(true),
    PRESENT(true),
    SCHOLARSHIP(false),
    GYM(true);


    // for developers only, other categories might be added later

    private final Boolean isExpense;

    Category(boolean isExpense) {
        this.isExpense = isExpense;
    }

    public Boolean isExpense() {
        return isExpense;
    }
}
