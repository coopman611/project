/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aasim.ris;

/**
 *
 * @author 14048
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Receptionist extends Stage {

    //Stage Structure
    HBox navbar = new HBox();
    Button logOut = new Button("Log Out");
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);
    //Table Structure
    VBox tableContainer = new VBox();
    TableView table = new TableView();
    TableColumn patientIDCol = new TableColumn("Patient ID");
    TableColumn firstNameCol = new TableColumn("Full Name");
    TableColumn timeCol = new TableColumn("Time of Appt.");
    TableColumn addressCol = new TableColumn("Mailing Address");
    TableColumn insuranceCol = new TableColumn("Insurance Provider");
    TableColumn referralDocCol = new TableColumn("Referral Doctor ID");
    TableColumn status = new TableColumn("Status");

    //
    Receptionist() {
        this.setTitle("RIS- Radiology Information System (Reception)");
        //Scene structure
        //Navbar
        navbar.setAlignment(Pos.TOP_RIGHT);
        navbar.getChildren().add(logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        logOut.setStyle("-fx-background-color: #9FFFCB; -fx-padding:10;");

        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }

        });
        main.setTop(navbar);
        //End navbar
        this.setScene(scene);
        //End scene

        //Putting center code here as to not clutter stuff
        loadCenter();
        //This function populates the table, making sure all NONCOMPLETED appointments are viewable
        populateTable();
        
    }

    private void loadCenter() {
        //Vbox to hold the table
        tableContainer.setAlignment(Pos.TOP_CENTER);
        tableContainer.setPadding(new Insets(20, 10, 10, 10));
        //Allow Table to read Appointment class
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        insuranceCol.setCellValueFactory(new PropertyValueFactory<>("insurance"));
        referralDocCol.setCellValueFactory(new PropertyValueFactory<>("referral"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        //Set Column Widths
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        firstNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        addressCol.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
        insuranceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        referralDocCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        status.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        //Add columns to table
        table.getColumns().addAll(patientIDCol, firstNameCol, timeCol, addressCol, insuranceCol, referralDocCol, status);
        table.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        tableContainer.getChildren().addAll(table);
        main.setCenter(tableContainer);
    }

    private void populateTable() {
        table.getItems().clear();
        //Connect to database
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select * FROM appointments WHERE statusCode != 1 ORDER BY time DESC;";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<Appointment> list = new ArrayList<Appointment>();
            while (rs.next()) {
                //What I receieve:  apptId, patientID, fullname, time, address, insurance, referral, status, order
                Appointment appt = new Appointment(rs.getInt("appt_id"), rs.getInt("patient_id"), rs.getString("full_name"), rs.getString("time"), rs.getString("address"), rs.getString("insurance"), rs.getString("referral_doc_id"), rs.getString("statusCode"), rs.getString("patient_order"));
                list.add(appt);
            }
            table.getItems().addAll(list);
            //
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void logOut() {
        App.user = new User();
        Stage x = new Login();
        x.show();
        x.setMaximized(true);
        this.hide();
    }
}
