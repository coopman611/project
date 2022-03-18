package aasim.ris;

import datastorage.User;
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

    public static User user;
    public static String fileName = "risDirectory";
    public static String imagePathDirectory = "Favicons/";

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
        //Create
        createDatabase(fileName);
        createAndPopulateTables(fileName);
        createAppointmentTable(fileName);
        createPatientTable(fileName);
        createStatusCodesTable(fileName);
        createOrderCodesTable(fileName);
        createOrdersTable(fileName);
        createImageTable(fileName);
        createDocPatientConnectorTable(fileName);
        createRadReportTable(fileName);
//        Populate
        populateTablesStatus(fileName);
        populateTablesAdmin(fileName);
//////        Duplication bug if you run these multiple times, leave commented out
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
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Functionality only does Users as of now.
    //In future, all tables to be created will be put in here
    public static void createAndPopulateTables(String fileName) {
        String sql = "CREATE TABLE users (\n"
                + "	user_id INTEGER PRIMARY KEY ,\n"
                + "	email VARCHAR(45) UNIQUE NOT NULL,\n"
                + "	full_name VARCHAR(45) NOT NULL,\n"
                + "	username VARCHAR(25) UNIQUE NOT NULL,\n"
                + "	password VARCHAR(64) NOT NULL,\n"
                + "     role TINYINT NOT NULL DEFAULT 0,\n"
                + "     pfp TEXT,\n"
                + "	enabled BIT NOT NULL DEFAULT 1\n"
                + ");";
        executeSQLStatement(sql);
        sql = "CREATE TABLE roles (\n"
                + "	roleID INTEGER PRIMARY KEY ,\n"
                + "     role VARCHAR(25),\n"
                + "	UNIQUE(roleID, role) \n"
                + ");";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('1', 'Administrator');\n";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('2', 'Receptionist');\n";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('3', 'Technician');\n";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('4', 'Radiologist');\n";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('5', 'Referral Doctor');\n";
        executeSQLStatement(sql);

        sql = "INSERT INTO roles VALUES ('6', 'Biller');\n";
        executeSQLStatement(sql);
    }

    public static void createAppointmentTable(String fileName) {
        //apptId, patientID, fullname, time, address, insurance, referral, status, order
        String sql = "CREATE TABLE appointments (\n"
                + "	appt_id INTEGER PRIMARY KEY UNIQUE,\n"
                + "	patient_id INTEGER NOT NULL,\n"
                + "	time VARCHAR(25) NOT NULL,\n"
                + "     statusCode INTEGER NOT NULL, "
                + "     UNIQUE(patient_id, time) "
                + ");";
        executeSQLStatement(sql);
        String sql1 = "CREATE TABLE appointmentsOrdersConnector ( "
                + "     apptID INTEGER,"
                + "     orderCodeID INTEGER, "
                + "     UNIQUE(apptID, orderCodeID) "
                + ");";
        executeSQLStatement(sql1);
    }

    public static void createDocPatientConnectorTable(String fileName) {
        String sql = "CREATE TABLE docPatientConnector (\n"
                + "	referralDocID INTEGER,\n"
                + "	patientID INTEGER, \n"
                + "     UNIQUE(referralDocID, patientID)"
                + ");";

        executeSQLStatement(sql);
    }

    public static void createPatientTable(String fileName) {
        String url = "jdbc:sqlite:C://sqlite/" + fileName;
        String sql = "CREATE TABLE patients (\n"
                + "	patientID INTEGER PRIMARY KEY ,\n"
                + "	email VARCHAR(45) NOT NULL,\n"
                + "	full_name VARCHAR(45) NOT NULL,\n"
                + "	dob VARCHAR(45) NOT NULL,\n"
                + "	address VARCHAR(64) NOT NULL,\n"
                + "     insurance VARCHAR(64) NOT NULL, \n"
                + "     UNIQUE(email, full_name)"
                + ");";
        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void createStatusCodesTable(String fileName) {
        String url = "jdbc:sqlite:C://sqlite/" + fileName;
        String sql = "CREATE TABLE statusCode (\n"
                + "	statusID INTEGER PRIMARY KEY,\n"
                + "	status VARCHAR(45)\n"
                + ");";
        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void createOrderCodesTable(String fileName) {
        String sql = "CREATE TABLE orderCodes (\n"
                + "	orderID INTEGER PRIMARY KEY,\n"
                + "	orders VARCHAR(45), \n"
                + "     UNIQUE(orderID, orders) "
                + ");";
        executeSQLStatement(sql);
    }

    public static void createOrdersTable(String fileName) {
        String url = "jdbc:sqlite:C://sqlite/" + fileName;
        String sql = "CREATE TABLE patientOrders (\n"
                + "	patientID INTEGER ,\n"
                + "     orderCodeID INTEGER NOT NULL ,\n"
                + "     enabled INTEGER DEFAULT 1\n" //1 = YES, 0 = FALSE
                + ");";
        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void createImageTable(String fileName) {
        String url = "jdbc:sqlite:C://sqlite/" + fileName;
        String sql = "CREATE TABLE images (\n"
                + "	imageID INTEGER PRIMARY KEY,\n"
                + "	patientID INTEGER,\n"
                + "	apptID INTEGER,\n"
                + "	image BLOB\n"
                + ");";
        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void createRadReportTable(String fileName) {
        String sql = "CREATE TABLE report(\n"
                + "apptID INTEGER UNIQUE, \n"
                + "writtenReport TEXT"
                + ");";
        executeSQLStatement(sql);
    }

    //In future, all population statements will be put in here
    public static void executeSQLStatement(String sql) {
        String url = "jdbc:sqlite:C://sqlite/" + fileName;
        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void populateTablesStatus(String fileName) {
        String sql = "INSERT INTO statusCode VALUES ('0', 'Patient Did Not Show');\n";
        String sql1 = "INSERT INTO statusCode VALUES ('1', 'Appointment Scheduled');\n";
        String sql2 = "INSERT INTO statusCode VALUES ('2', 'Patient Checked In');\n";
        String sql3 = "INSERT INTO statusCode VALUES ('3', 'Patient received by Technician');\n";
        String sql4 = "INSERT INTO statusCode VALUES ('4', 'Images Uploaded');\n";
        String sql5 = "INSERT INTO statusCode VALUES ('5', 'Radiology Report Uploaded.');\n";
        String sql6 = "INSERT INTO statusCode VALUES ('6', 'Referral Doctor Signature Completed.');\n";
        String sql7 = "INSERT INTO statusCode VALUES ('7', 'Patient Cancelled');\n";
        String sql8 = "INSERT INTO statusCode VALUES ('8', 'Faculty Cancelled');\n";

        executeSQLStatement(sql);
        executeSQLStatement(sql1);
        executeSQLStatement(sql2);
        executeSQLStatement(sql3);
        executeSQLStatement(sql4);
        executeSQLStatement(sql5);
        executeSQLStatement(sql6);
        executeSQLStatement(sql7);
        executeSQLStatement(sql8);

    }

    public static void populateTablesAdmin(String fileName) {
        String sql = "INSERT INTO users(email, full_name, username, password, role) VALUES ('god@gmail.com', 'Administrator Dave', 'admin', 'admin', '1');\n";
        executeSQLStatement(sql);
    }

}
