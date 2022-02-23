/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aasim.ris;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author 14048
 */
public class ReferralDoctor extends Stage {

    //Navbar
    HBox navbar = new HBox();
    Button logOut = new Button("Log Out");
    //End Navbar

    //Scene
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);
    //End Scene

    //Center
    TableView patientsTable = new TableView();
    ChoiceBox<String> choiceBox = new ChoiceBox();
    TextField search = new TextField("Search Appointments");
    Button addAppointment = new Button("Add Appointment");
    Button refreshTable = new Button("Refresh Appointments");
    Button updateAppointment = new Button("Update Appointment");
    HBox searchContainer = new HBox(choiceBox, search);
    HBox buttonContainer = new HBox(addAppointment, refreshTable, updateAppointment, searchContainer);
//    TableView ordersTable = new TableView();
    VBox tableContainer = new VBox(patientsTable, buttonContainer);
    //End Center

    //Backend
//    FilteredList<Object> flAppointment;
    ReferralDoctor() {
        this.setTitle("RIS - Radiology Information System (Doctor)");
        //Navbar
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }
        });
        navbar.getChildren().add(logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);
        //End navbar
        //Set Center
        //Tables
        tableContainer.setSpacing(10);
        main.setCenter(tableContainer);
        createTablePatients();
//        createTableOrders();

        buttonContainer.setSpacing(10);
        //Search Bar
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.getItems().addAll("Patient Name", "Patient Game", "Patient Blame", "Patient Fame");
        choiceBox.setValue("Patient Name");
        //End Searchbar
        //End Center
        //Set Scene and Structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
    }

    private void createTablePatients() {
        //All of the Columns
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn fullNameCol = new TableColumn("Full Name");
        TableColumn addressCol = new TableColumn("Mailing Address");
        TableColumn insuranceCol = new TableColumn("Insurance Provider");
        TableColumn orderIDCol = new TableColumn("Order IDs");
        //And all of the Value setting
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        insuranceCol.setCellValueFactory(new PropertyValueFactory<>("insurance"));
        orderIDCol.setCellValueFactory(new PropertyValueFactory<>("orderIDs"));
        //Couldn't put all the styling
        patientIDCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.04));
        fullNameCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.06));
        addressCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        insuranceCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        orderIDCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.5));
        patientsTable.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        //Together again
        patientsTable.getColumns().addAll(patientIDCol, fullNameCol, addressCol, insuranceCol, orderIDCol);
    }

    private void createTableOrders() {
        //There's a column to be created, dear Lisa, dear Lisa
        TableColumn orderIDCol = new TableColumn("Order ID");
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn statusCol = new TableColumn("Status");
        TableColumn orderRequestCol = new TableColumn("Order Request");
        TableColumn orderSubmissionCol = new TableColumn("Order Submissions");
        //Also need referralDocID but no need to display that to the referral doc

        //So set the values, oh Henry, oh Henry
        orderIDCol.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderRequestCol.setCellValueFactory(new PropertyValueFactory<>("orderRequest"));
        orderSubmissionCol.setCellValueFactory(new PropertyValueFactory<>("orderSubmissions"));
        //The columns need to be styled, dear Lisa, dear Lisa
        patientIDCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.1));
        orderIDCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.1));
        statusCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.3));
        orderRequestCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        orderSubmissionCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.3));
        ordersTable.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        //So add it, oh Henry oh Henry, so add it indeed.
        ordersTable.getColumns().addAll(orderIDCol, patientIDCol, statusCol, orderRequestCol, orderSubmissionCol);

    }

    private void logOut() {
        App.user = new User();
        Stage x = new Login();
        x.show();
        x.setMaximized(true);
        this.hide();
    }
}
