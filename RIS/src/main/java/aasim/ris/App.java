package aasim.ris;

import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JavaFX App
 */
public class App extends Application {

    public static User user = new User();
    public static String fileName = "risDirectory";

    @Override
    public void start(Stage stage) {

        //Creating, Editing, Adding stuff to the scene
        //Add stuff to the Stage
        stage = new Login();
        stage.setMaximized(true);
        stage.show();
        //

    }

    public static void main(String[] args) {
//        createDatabase(fileName);
//        createUserTable(fileName);
//        createAppointmentTable(fileName);
//        populateTables(fileName);
//        populateTables2(fileName);
        launch();
    }

    //Create a database
    public static void createDatabase(String fileName) {
        String url = "jdbc:sqlite:C:/sqlite/" + fileName;
        try {
            Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Functionality only does Users as of now.
    //In future, all tables to be created will be put in here
    public static void createUserTable(String fileName) {
        String url = "jdbc:sqlite:C://sqlite/" + fileName;
        String sql = "CREATE TABLE users (\n"
                + "	user_id INTEGER PRIMARY KEY ,\n"
                + "	email VARCHAR(45) UNIQUE NOT NULL,\n"
                + "	full_name VARCHAR(45) NOT NULL,\n"
                + "	username VARCHAR(25) UNIQUE NOT NULL,\n"
                + "	password VARCHAR(64) NOT NULL,\n"
                + "     role TINYINT NOT NULL DEFAULT 0,\n"
                + "	enabled BIT NOT NULL DEFAULT 1\n"
                + ");";
        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void createAppointmentTable(String fileName) {
        String url = "jdbc:sqlite:C://sqlite/" + fileName;
        //apptId, patientID, fullname, time, address, insurance, referral, status, order
        String sql = "CREATE TABLE appointments (\n"
                + "	appt_id INTEGER PRIMARY KEY ,\n"
                + "	patient_id INTEGER UNIQUE NOT NULL,\n"
                + "	full_name VARCHAR(45) NOT NULL,\n"
                + "	time VARCHAR(25) NOT NULL,\n"
                + "	address VARCHAR(64) NOT NULL,\n"
                + "     insurance VARCHAR(64) NOT NULL,\n"
                + "     referral_doc_id VARCHAR(64) NOT NULL,\n"
                + "     statusCode INTEGER NOT NULL,\n" //0 inprogress, 1 complete
                + "	patient_order VARCHAR(45)\n"
                + ");";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    //Functionality only populates Users for now.
    //In future, all population statements will be put in here
    public static void populateTables(String fileName) {
        String url = "jdbc:sqlite:C://sqlite/" + fileName;
        String sql = "INSERT INTO users(email, full_name, username, password, role) VALUES ('exampleemail@gmail.com', 'Sadie Greenhorn', 'receptionist', 'receptionist', '2');\n";
        try {

            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void populateTables2(String fileName) {
        String url = "jdbc:sqlite:C://sqlite/" + fileName;
        String sql = "INSERT INTO appointments(patient_id, full_name, time, address, insurance, referral_doc_id, patient_order, statusCode) VALUES('0','Loki Barnes','2022-02-24 18:00', '3012 Popcorn Avenue, Texas Illinois', 'PeachTree Healthcare', '0', 'xray', '0');";
        try {

            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
//INSERT INTO appointments(patient_id, full_name, time, address, insurance, referral_doc_id, patient_order, statusCode) VALUES('0','Loki Barnes','2022-02-24 18:00', '3012 Popcorn Avenue, Texas Illinois', 'PeachTree Healthcare', '0', 'xray', '0');"
