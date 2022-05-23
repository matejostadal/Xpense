package cz.xpense;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class App extends Application {

    private static final String DB_NAME = "xpensedb";
    private static final String HOST    = "localhost:1527";
    private static TransactionDatabase transactionDB;

    public static void main(String[] args) throws SQLException, TransactionDBException {
        String connectionURL = "jdbc:derby://localhost:1527/xpensedb";
        try (Connection con = DriverManager.getConnection(connectionURL);
            TransactionDatabase transactions = new TransactionDatabase(con)) {
            transactionDB = transactions;
            launch(args);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(App.class.getResource("/PrimaryWindow.fxml"));
        Parent root = loader.load();

        // controller and stage bonding
        MainController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        setUpMainController(controller);

        // window properties
        primaryStage.setTitle("Xpense App");
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();
    }

    /** Hands data to the main controller. */
    public void setUpMainController(MainController controller) throws TransactionDBException {
        controller.setDB(transactionDB);
        controller.setTransactions(transactionDB.getAllTransactions());
        controller.prepare();
    }
}
