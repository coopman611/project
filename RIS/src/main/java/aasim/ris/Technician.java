package aasim.ris;

import datastorage.Appointment;
import datastorage.User;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.InputStream;

public class Technician extends Stage {
//stage structure

    HBox navbar = new HBox();
    Button logOut = new Button("Log Out");
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);
    //table structure
    TableView table = new TableView();
    TableColumn apptIDCol = new TableColumn("Appointment ID");
    TableColumn patientIDCol = new TableColumn("Patient ID");
    TableColumn fullNameCol = new TableColumn("Full Name");
    TableColumn timeCol = new TableColumn("Time of Appointment");
    TableColumn orderCol = new TableColumn("Patient Order");
    TableColumn fulOrder = new TableColumn("Fullfilled Order");
    TableColumn status = new TableColumn("Status");
    //search bar
    FilteredList<Appointment> flAppointment;
    ChoiceBox<String> choiceBox = new ChoiceBox();
    TextField search = new TextField("Search Appointments");

    //buttons
    Button refreshTable = new Button("Refresh Appointments");
    //containers
    HBox searchContainer = new HBox(choiceBox, search);
    HBox buttonContainer = new HBox(refreshTable, searchContainer);
    VBox tableContainer = new VBox(table, buttonContainer);

    //populate the stage
    Technician() {
        this.setTitle("RIS-Radiology Information System(Technician)");
        //navbar
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        navbar.getChildren().add(logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);
        //end navbar

        loadCenter();

        //buttons
        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }
        });
        refreshTable.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                updateAppointment();
            }
        });
        //Searchbar Structure
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.getItems().addAll("Appointment ID", "Patient ID", "Full Name", "Date/Time", "Address", "Insurance", "Referral Doctor", "Status");
        choiceBox.setValue("Appointment ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
            if (choiceBox.getValue().equals("Appointment ID")) {
                flAppointment.setPredicate(p -> new String(p.getApptId() + "").contains(newValue));//filter table by Full name
            }
            if (choiceBox.getValue().equals("Patient ID")) {
                flAppointment.setPredicate(p -> new String(p.getPatientID() + "").contains(newValue));//filter table by Full name
            }
            if (choiceBox.getValue().equals("Full Name")) {
                flAppointment.setPredicate(p -> p.getFullname().contains(newValue));//filter table by Full name
            }
            if (choiceBox.getValue().equals("Date/Time")) {
                flAppointment.setPredicate(p -> p.getTime().contains(newValue));//filter table by Full name
            }

            if (choiceBox.getValue().equals("Status")) {
                flAppointment.setPredicate(p -> p.getStatus().contains(newValue));//filter table by Full name
            }
            table.getItems().clear();
            table.getItems().addAll(flAppointment);
        });
        //End Searchbar Structure
        //Scene Structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
        //End scene

        //This function populates the table, making sure all NONCOMPLETED appointments are viewable
        populateTable();

    }
    //Clear the table, query the database, populate table based on results.
    //DOES NOT SHOW 'COMPLETED' (statusCode = 1) APPOINTMENTS. 

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
                Appointment appt = new Appointment(rs.getInt("appt_id"), rs.getInt("patient_id"), rs.getString("full_name"), rs.getString("time"), rs.getString("statusCode"), rs.getString("patient_order"), rs.getBinaryStream("fulOrder"));
                list.add(appt);
            }
            flAppointment = new FilteredList(FXCollections.observableList(list), p -> true);
            table.getItems().addAll(flAppointment);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    //Add stuff to the center, and make it look good.
    private void loadCenter() {
        //Vbox to hold the table
        tableContainer.setAlignment(Pos.TOP_CENTER);
        tableContainer.setPadding(new Insets(20, 10, 10, 10));
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.setSpacing(10);
        //Allow Table to read Appointment class
        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptId"));
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        orderCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        fulOrder.setCellValueFactory(new PropertyValueFactory<>("Fullfilled Order"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        //Set Column Widths
        apptIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        fullNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        orderCol.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
        fulOrder.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        status.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        //Add columns to table
        table.getColumns().addAll(apptIDCol, patientIDCol, fullNameCol, timeCol, orderCol, fulOrder, status);
        table.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        main.setCenter(tableContainer);

    }

    private void updateAppointment() {
        Stage x = new UpdateAppointment();
        x.setTitle("Update Appointment");
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        x.showAndWait();
        populateTable();
    }

    private void logOut() {
        // TODO Auto-generated method stub
        App.user = new User();
        Stage x = new Login();
        x.show();
        x.setMaximized(true);
        this.hide();

    }

    /* 
    // Private Nested Classes Below.
    //
    //
    //
     */
    //Private Nested Class 1
    //For the Add Appointment
    //Private Nested Class 2
    //Update appointment
    private class UpdateAppointment extends Stage {
//<editor-fold>
        //Class Variables
//    Labels

        private Label apptID = new Label("Appointment ID");
        private Label patID = new Label("Patient ID");
        private Label patName = new Label("Patient's Full Name");
        private Label patDate = new Label("Appointment Date (YYYY-MM-DD HHMM)");
        private Label patOrder = new Label("Orders Requested (Separated by ',')");
        private Label fulOrder = new Label("Fulfilled Orders");
        private Label statusCode = new Label("Status Code: ");

//    Text Boxes
        private TextField apptIDText = new TextField("");
        private TextField patIDText = new TextField("");
        private TextField patNameText = new TextField("");
        private TextField patAddressText = new TextField("");
        private TextField patDateText = new TextField("");
        private TextField patOrderText = new TextField("");
        private TextField fulOrderText = new TextField("");
        private TextField statusCodeText = new TextField("");

//    Button
        Button submit = new Button("Pull Data");
        Button update = new Button("Update");

//    Add stuff to HBoxes
        HBox placeholder1 = new HBox(apptID, apptIDText, patID, patIDText, submit);
        HBox placeholder2 = new HBox(patName, patNameText);
        HBox placeholder3 = new HBox(patDate, patDateText);
        HBox placeholder4 = new HBox(patOrder, patOrderText);
//    VBox
        VBox localmain = new VBox(placeholder1);
        Scene temp = new Scene(localmain);
//</editor-fold>

        public UpdateAppointment() {
            localmain.setPadding(new Insets(10));
            localmain.setSpacing(10);
            patNameText.setPrefWidth(150);
            patDateText.setPrefWidth(150);
            patOrderText.setPrefWidth(150);
            fulOrder.setPrefWidth(150);
            patNameText.setMaxWidth(150);
            patDateText.setMaxWidth(150);
            patOrderText.setMaxWidth(150);
            fulOrder.setMaxWidth(150);
            placeholder1.setSpacing(10);
            placeholder2.setSpacing(10);
            placeholder3.setSpacing(10);
            placeholder4.setSpacing(10);
            submit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    boolean everythingCool = true;
                    int patID = -1, apptID = -1;
                    try {
                        apptID = Integer.parseInt(apptIDText.getText());
                    } catch (NumberFormatException abc) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Appointment ID");
                        alert.setContentText("Please input a valid number for Patient ID.");
                        alert.showAndWait();
                    }

                    try {
                        patID = Integer.parseInt(patIDText.getText());
                    } catch (NumberFormatException abc) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Appointment ID");
                        alert.setContentText("Please input a valid number for Patient ID.");
                        alert.showAndWait();
                    }
                    if (everythingCool) {
                        pullData(apptID, patID);
                    }
                }

            });
            update.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    boolean everythingCool = true;

                    if (!patNameText.getText().matches("^[A-Za-z ]*$+")) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Inputs");
                        alert.setContentText("Please input a proper name.");
                        alert.showAndWait();
                    }

                    if (!patOrderText.getText().matches("^[A-Za-z0-9 ,]*$")) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Inputs");
                        alert.setContentText("Please input a proper order.");
                        alert.showAndWait();
                    }
                    if (!statusCodeText.getText().matches("[0-9]+")) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Inputs");
                        alert.setContentText("Please input a proper statuscode.");
                        alert.showAndWait();
                    }

                    try {
                        Timestamp temp = Timestamp.valueOf(patDateText.getText() + ":00");
                    } catch (IllegalArgumentException axd) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Inputs");
                        alert.setContentText("Please input a proper date.");
                        alert.showAndWait();
                    }
                    if (everythingCool) {
                        updateAppt();
                    }
                }
            });
            temp.getStylesheets().add("file:stylesheet.css");
            this.setScene(temp);
        }

        private void pullData(int apptID, int patID) {
            //Connect to database
            String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
            String sql = "Select * FROM appointments WHERE appt_id = '" + apptID + "' AND patient_id = '" + patID + "';";
            boolean everythingCool2ElectricBoogaloo = false;
            try {
                Appointment appt = null;
                Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                //

                while (rs.next()) {
                    //What I receieve:  apptId, patientID, fullname, time, address, insurance, referral, status, order
                    everythingCool2ElectricBoogaloo = true;
                    appt = new Appointment(rs.getInt("appt_id"), rs.getInt("patient_id"), rs.getString("full_name"), rs.getString("time"), rs.getString("statusCode"), rs.getString("patient_order"), rs.getBinaryStream("fulfilledOrder"));
                }
                //
                rs.close();
                stmt.close();
                conn.close();
                if (everythingCool2ElectricBoogaloo) {
                    apptIDText.setEditable(false);
                    patIDText.setEditable(false);
                    placeholder1.getChildren().remove(submit);
                    placeholder1.getChildren().addAll(statusCode, statusCodeText);
                    patNameText.setText(appt.getFullname());
                    patDateText.setText(appt.getTime());
                    statusCodeText.setText(appt.getStatus());
                    patOrderText.setText(appt.getOrder());
                    localmain.getChildren().addAll(placeholder2, placeholder3, placeholder4, update);
                    this.setHeight(300);
                    this.setWidth(1000);
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        private void updateAppt() {
            String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
            String sql = "UPDATE appointments SET full_name = '" + patNameText.getText() + "', time = '" + patDateText.getText() + "', statusCode = '" + statusCodeText.getText() + "', patient_order = '" + patOrderText.getText() + "Patient Fulfilled Order" + fulOrder + "' WHERE appt_id = '" + apptIDText.getText() + "' AND patient_id = '" + patIDText.getText() + "';";
            try {
                Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
                stmt.close();
                conn.close();
                this.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    //Private Nested Class 3
    //Remove Appointment
}
