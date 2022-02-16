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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Receptionist extends Stage {

    //Stage Structure
    HBox navbar = new HBox();
    Button logOut = new Button("Log Out");
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);
    //Table Structure
    TableView table = new TableView();
    TableColumn apptIDCol = new TableColumn("Appointment ID");
    TableColumn patientIDCol = new TableColumn("Patient ID");
    TableColumn firstNameCol = new TableColumn("Full Name");
    TableColumn timeCol = new TableColumn("Time of Appt.");
    TableColumn addressCol = new TableColumn("Mailing Address");
    TableColumn insuranceCol = new TableColumn("Insurance Provider");
    TableColumn referralDocCol = new TableColumn("Referral Doctor ID");
    TableColumn status = new TableColumn("Status");

    Button addAppointment = new Button("Add Appointment");
    Button refreshTable = new Button("Refresh Appointments");
    Button updateAppointment = new Button("Update Appointment");
    HBox buttonContainer = new HBox(addAppointment, refreshTable, updateAppointment);
    VBox tableContainer = new VBox(table, buttonContainer);

    //Populate the stage
    Receptionist() {
        this.setTitle("RIS- Radiology Information System (Reception)");
        //Scene structure
        //Navbar
        navbar.setAlignment(Pos.TOP_RIGHT);
        navbar.getChildren().add(logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");

        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }
        });
        addAppointment.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                addAppointment();
            }
        });
        refreshTable.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                populateTable();
            }
        });
        updateAppointment.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                updateAppointment();
            }
        });

        main.setTop(navbar);
        //End navbar
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
        //End scene

        //Putting center code here as to not clutter stuff
        loadCenter();
        //This function populates the table, making sure all NONCOMPLETED appointments are viewable
        populateTable();

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
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        insuranceCol.setCellValueFactory(new PropertyValueFactory<>("insurance"));
        referralDocCol.setCellValueFactory(new PropertyValueFactory<>("referral"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        //Set Column Widths
        apptIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        firstNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        addressCol.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        insuranceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        referralDocCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        status.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        //Add columns to table
        table.getColumns().addAll(apptIDCol, patientIDCol, firstNameCol, timeCol, addressCol, insuranceCol, referralDocCol, status);
        table.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        main.setCenter(tableContainer);
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
                Appointment appt = new Appointment(rs.getInt("appt_id"), rs.getInt("patient_id"), rs.getString("full_name"), rs.getString("time"), rs.getString("address"), rs.getString("insurance"), rs.getString("referral_doc_id"), rs.getString("statusCode"), rs.getString("patient_order"));
                list.add(appt);
            }
            table.getItems().addAll(list);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //On button press, log out
    private void logOut() {
        App.user = new User();
        Stage x = new Login();
        x.show();
        x.setMaximized(true);
        this.hide();
    }

    //On button press, open up a new stage (calls private nested class)
    private void addAppointment() {
        Stage x = new AddAppointment();
        x.setTitle("Add Appointment");
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        x.showAndWait();
        populateTable();
    }

    //On button press, open up a new stage (calls private nested class)
    private void updateAppointment() {
        Stage x = new UpdateAppointment();
        x.setTitle("Update Appointment");
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        x.showAndWait();
        populateTable();
    }

    /* 
    // Private Nested Classes Below.
    //
    //
    //
     */
    //Private Nested Class 1
    //For the Add Appointment
    private class AddAppointment extends Stage {

//    Labels
        private Label patID = new Label("Patient ID");
        private Label patName = new Label("Patient's Full Name");
        private Label patAddress = new Label("Patient's Mailing Address");
        private Label patDate = new Label("Appointment Date (YYYY-MM-DD HHMM)");
        private Label patInsurance = new Label("Insurance Provider");
        private Label patDoc = new Label("Referral Doctor");
        private Label patOrder = new Label("Orders Requested (Separated by ',')");

//    Text Boxes
        private TextField patIDText = new TextField("PatientID");
        private TextField patNameText = new TextField("PatientsFullName");
        private TextField patAddressText = new TextField("PatientsMailingAddress");
        private TextField patDateText = new TextField("YYYY-MM-DD HH:MM");
        private TextField patInsuranceText = new TextField("Insurance Provider");
        private TextField patDocText = new TextField("Referral Doctor");
        private TextField patOrderText = new TextField("Order Requested");

//    Add stuff to HBoxes
        HBox placeholder1 = new HBox(patID, patIDText, patName, patNameText, patAddress, patAddressText);
        HBox placeholder2 = new HBox(patDate, patDateText, patInsurance, patInsuranceText);
        HBox placeholder3 = new HBox(patDoc, patDocText, patOrder, patOrderText);

//    Button
        Button submit = new Button("Verify and Submit");

//    VBox
        VBox localmain = new VBox(placeholder1, placeholder2, placeholder3, submit);
        Scene temp = new Scene(localmain);

        public AddAppointment() {
            localmain.setPadding(new Insets(10));
            localmain.setSpacing(10);
            patDateText.setPrefWidth(300);
            patDateText.setMaxWidth(300);
            placeholder1.setSpacing(10);
            placeholder2.setSpacing(10);
            placeholder3.setSpacing(10);

            submit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
//                In the future, change to verify patient information before adding directly to the database
                    boolean everythingCool = true;
                    int id = -1;
                    try {
                        id = Integer.parseInt(patIDText.getText());
                    } catch (NumberFormatException abc) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Patient ID");
                        alert.setContentText("Please input a valid number for Patient ID.");
                        alert.showAndWait();
                    }

                    String name = patNameText.getText();
                    String address = patAddressText.getText();
                    String date = patDateText.getText();
                    String insurance = patInsuranceText.getText();
                    String doc = patDocText.getText();
                    String order = patOrderText.getText();

                    if (!name.matches("[a-zA-Z ]+") || !insurance.matches("[a-zA-Z ]+") || !doc.matches("[a-zA-Z ]+")) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Inputs");
                        alert.setContentText("Please check your inputs for Name, Insurance, and Referral Doctor. Numbers and Special Characters are not allowed.");
                        alert.showAndWait();
                    }

                    if (!address.matches("\\p{Alnum}*")) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Inputs");
                        alert.setContentText("Please check your inputs for the Address. Special Characters are not allowed.");
                        alert.showAndWait();
                    }
                    try {
                        Timestamp temp = Timestamp.valueOf(date + ":00");
                    } catch (IllegalArgumentException axd) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Inputs");
                        alert.setContentText("Please input a proper date.");
                        alert.showAndWait();
                    }

                    int statusCode = 0;
                    if (everythingCool) {
                        addAppt(id, name, address, date, insurance, doc, order, statusCode);
                    }
                }

            });
            temp.getStylesheets().add("file:stylesheet.css");

            this.setScene(temp);
        }

        private void addAppt(int id, String name, String address, String date, String insurance, String doc, String order, int statusCode) {
            String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
            String sql = "INSERT INTO appointments(patient_id, full_name, time, address, insurance, referral_doc_id, patient_order, statusCode) VALUES('" + id + "','" + name + "','" + date + "', '" + address + "', '" + insurance + "', '" + doc + "', '" + order + "', '" + statusCode + "');";
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

    //Private Nested Class 2
    //Update appointment
    private class UpdateAppointment extends Stage {

//    Labels
        private Label apptID = new Label("Appointment ID");
        private Label patID = new Label("Patient ID");
        private Label patName = new Label("Patient's Full Name");
        private Label patAddress = new Label("Patient's Mailing Address");
        private Label patDate = new Label("Appointment Date (YYYY-MM-DD HHMM)");
        private Label patInsurance = new Label("Insurance Provided");
        private Label patDoc = new Label("Referral Doctor");
        private Label patOrder = new Label("Orders Requested (Separated by ',')");
        private Label statusCode = new Label("Status Code: ");

//    Text Boxes
        private TextField apptIDText = new TextField("");
        private TextField patIDText = new TextField("");
        private TextField patNameText = new TextField("");
        private TextField patAddressText = new TextField("");
        private TextField patDateText = new TextField("");
        private TextField patInsuranceText = new TextField("");
        private TextField patDocText = new TextField("");
        private TextField patOrderText = new TextField("");
        private TextField statusCodeText = new TextField("");

//    Button
        Button submit = new Button("Pull Data");
        Button update = new Button("Update");

//    Add stuff to HBoxes
        HBox placeholder1 = new HBox(apptID, apptIDText, patID, patIDText, submit);
        HBox placeholder2 = new HBox(patName, patNameText, patAddress, patAddressText);
        HBox placeholder3 = new HBox(patDate, patDateText, patInsurance, patInsuranceText);
        HBox placeholder4 = new HBox(patDoc, patDocText, patOrder, patOrderText);
//    VBox
        VBox localmain = new VBox(placeholder1);
        Scene temp = new Scene(localmain);

        public UpdateAppointment() {
            localmain.setPadding(new Insets(10));
            localmain.setSpacing(10);

            patNameText.setPrefWidth(150);
            patAddressText.setPrefWidth(150);
            patDateText.setPrefWidth(150);
            patInsuranceText.setPrefWidth(150);
            patDocText.setPrefWidth(150);
            patOrderText.setPrefWidth(150);
            patNameText.setMaxWidth(150);
            patAddressText.setMaxWidth(150);
            patDateText.setMaxWidth(150);
            patInsuranceText.setMaxWidth(150);
            patDocText.setMaxWidth(150);
            patOrderText.setMaxWidth(150);

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
                    if (!patAddressText.getText().matches("^[A-Za-z0-9 ]*$")) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Inputs");
                        alert.setContentText("Please input a proper address.");
                        alert.showAndWait();
                    }

                    if (!patInsuranceText.getText().matches("^[A-Za-z ]*$")) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Inputs");
                        alert.setContentText("Please input a proper insurance.");
                        alert.showAndWait();
                    }

                    if (!patDocText.getText().matches("^[A-Za-z .]*$")) {
                        everythingCool = false;
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Improper Inputs");
                        alert.setContentText("Please input a proper doctor.");
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
            try {
                Appointment appt = null;
                Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                //

                while (rs.next()) {
                    //What I receieve:  apptId, patientID, fullname, time, address, insurance, referral, status, order
                    appt = new Appointment(rs.getInt("appt_id"), rs.getInt("patient_id"), rs.getString("full_name"), rs.getString("time"), rs.getString("address"), rs.getString("insurance"), rs.getString("referral_doc_id"), rs.getString("statusCode"), rs.getString("patient_order"));
                }
                //
                rs.close();
                stmt.close();
                conn.close();

                apptIDText.setEditable(false);
                patIDText.setEditable(false);
                placeholder1.getChildren().remove(submit);
                placeholder1.getChildren().addAll(statusCode, statusCodeText);
                patNameText.setText(appt.getFullname());
                patAddressText.setText(appt.getAddress());
                patDateText.setText(appt.getTime());
                statusCodeText.setText(appt.getStatus());
                patInsuranceText.setText(appt.getInsurance());
                patDocText.setText(appt.getReferral());
                patOrderText.setText(appt.getOrder());
                localmain.getChildren().addAll(placeholder2, placeholder3, placeholder4, update);
                this.setHeight(300);
                this.setWidth(1000);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        private void updateAppt() {
            String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
            String sql = "UPDATE appointments SET full_name = '" + patNameText.getText() + "', time = '" + patDateText.getText() + "', address = '" + patAddressText.getText() + "', insurance = '" + patInsuranceText.getText() + "', referral_doc_id = '" + patDocText.getText() + "', statusCode = '" + statusCodeText.getText() + "', patient_order = '" + patOrderText.getText() + "' WHERE appt_id = '" + apptIDText.getText() + "' AND patient_id = '" + patIDText.getText() + "';";
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
