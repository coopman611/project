package aasim.ris;

/**
 *
 * @author 14048
 */
import datastorage.User;
import datastorage.Appointment;
import datastorage.Patient;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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

public class Receptionist extends Stage {

    //<editor-fold>
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
    TableColumn orderCol = new TableColumn("Orders Requested");
    TableColumn status = new TableColumn("Status");
    TableColumn updateAppt = new TableColumn("Update Appointment");
    //Search Bar
    FilteredList<Appointment> flAppointment;
    ChoiceBox<String> choiceBox = new ChoiceBox();
    TextField search = new TextField("Search Appointments");

    //Buttons
    Button addAppointment = new Button("Add Appointment");
    Button refreshTable = new Button("Refresh Appointments");
    //Containers
    HBox searchContainer = new HBox(choiceBox, search);
    HBox buttonContainer = new HBox(addAppointment, refreshTable, searchContainer);
    VBox tableContainer = new VBox(table, buttonContainer);
//</editor-fold>
    //Populate the stage

    Receptionist() {
        this.setTitle("RIS- Radiology Information System (Reception)");
        //Navbar
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        navbar.getChildren().add(logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);
        //End navbar

        //Putting center code here as to not clutter stuff
        loadCenter();

        //Buttons
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

        //Searchbar Structure
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.getItems().addAll("Appointment ID", "Patient ID", "Full Name", "Date/Time", "Status");
        choiceBox.setValue("Appointment ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
            if (choiceBox.getValue().equals("Appointment ID")) {
                flAppointment.setPredicate(p -> new String(p.getApptID() + "").contains(newValue));//filter table by Appt ID
            }
            if (choiceBox.getValue().equals("Patient ID")) {
                flAppointment.setPredicate(p -> new String(p.getPatientID() + "").contains(newValue));//filter table by Patient Id
            }
            if (choiceBox.getValue().equals("Full Name")) {
                flAppointment.setPredicate(p -> p.getFullName().toLowerCase().contains(newValue.toLowerCase()));//filter table by Full name
            }
            if (choiceBox.getValue().equals("Date/Time")) {
                flAppointment.setPredicate(p -> p.getTime().contains(newValue));//filter table by Date/Time
            }
            if (choiceBox.getValue().equals("Status")) {
                flAppointment.setPredicate(p -> p.getStatus().toLowerCase().contains(newValue.toLowerCase()));//filter table by Status
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

    //Add stuff to the center, and make it look good.
    private void loadCenter() {
        //Vbox to hold the table
        tableContainer.setAlignment(Pos.TOP_CENTER);
        tableContainer.setPadding(new Insets(20, 10, 10, 10));
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.setSpacing(10);
        //Allow Table to read Appointment class
        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptID"));
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        orderCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        updateAppt.setCellValueFactory(new PropertyValueFactory<>("placeholder"));

        //Set Column Widths
        apptIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.04));
        firstNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        orderCol.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
        updateAppt.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        status.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        //Add columns to table
        table.getColumns().addAll(apptIDCol, patientIDCol, firstNameCol, timeCol, orderCol, status, updateAppt);
        table.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        main.setCenter(tableContainer);
    }

    //Clear the table, query the database, populate table based on results.
    //DOES NOT SHOW 'COMPLETED' (statusCode = 4) APPOINTMENTS. 
    private void populateTable() {
        table.getItems().clear();
        //Connect to database
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select appt_id, patient_id, patients.full_name, time, statusCode.status"
                + " FROM appointments"
                + " INNER JOIN statusCode ON appointments.statusCode = statusCode.statusID "
                + " INNER JOIN patients ON patients.patientID = appointments.patient_id"
                + " WHERE statusCode < 3"
                + " ORDER BY time ASC;";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<Appointment> list = new ArrayList<Appointment>();

            while (rs.next()) {
                //What I receieve:  apptId, patientID, fullname, time, address, insurance, referral, status, order
                Appointment appt = new Appointment(rs.getInt("appt_id"), rs.getInt("patient_id"), rs.getString("time"), rs.getString("status"), getPatOrders(rs.getInt("patient_id"), rs.getInt("appt_id")));
                appt.setFullName(rs.getString("full_name"));
                list.add(appt);
            }

            for (Appointment x : list) {
                x.placeholder.setText("Update Appointment");
                x.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        updateAppointment(x);
                    }
                });
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

    private String getPatOrders(int patientID, int aInt) {
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select orderCodes.orders "
                + " FROM appointmentsOrdersConnector "
                + " INNER JOIN orderCodes ON appointmentsOrdersConnector.orderCodeID = orderCodes.orderID "
                + " WHERE apptID = '" + aInt + "';";

        String value = "";
        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {

                value += rs.getString("orders") + ", ";
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return value;
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
    private void updateAppointment(Appointment appt) {
        Stage x = new Stage();
        x.setTitle("Update Appointment");
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        //
        Button updateTime = new Button("Reschedule Appointment");
        Button updateStatus = new Button("Change Appointment Status");
        Button updatePatient = new Button("Update Patient Information");
        HBox display = new HBox(updateTime, updateStatus, updatePatient);
        display.setAlignment(Pos.CENTER);
        display.setSpacing(15);
        //

        //
        VBox container = new VBox(display);
        Scene scene = new Scene(container);
        x.setScene(scene);
        scene.getStylesheets().add("file:stylesheet.css");
        //
        updateTime.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                container.getChildren().clear();

                DatePicker datePicker = new DatePicker();
                Label text = new Label("Insert Date: ");
                Label text1 = new Label("Insert Time (HH:MM): ");
                TextField time = new TextField("HH:MM");
                text.setPrefWidth(100);
                text1.setPrefWidth(150);
                Button submit = new Button("Submit");
                submit.setPrefWidth(100);
                submit.setId("complete");

                HBox hidden = new HBox(text, datePicker, text1, time, submit);
                hidden.setSpacing(15);
                container.getChildren().add(hidden);
                submit.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        boolean everythingCool = true;
                        //validation here
                        //end validation
                        if (everythingCool) {
                            updateTime(datePicker.getValue().toString() + " " + time.getText(), appt.getApptID());
                            x.close();
                        }
                    }

                });
            }
        });
        updateStatus.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                container.getChildren().clear();

                ComboBox dropdown = new ComboBox();
                dropdown.getItems().addAll("Patient Did Not Show", "Appointment Scheduled", "Patient Checked In", "Patient recieved by Technician", "Patient Cancelled", "Faculty Cancelled");

                dropdown.setValue(appt.getStatus());
                Button submit = new Button("Submit");
                submit.setId("complete");

                HBox hidden = new HBox(dropdown, submit);
                hidden.setSpacing(15);
                container.getChildren().add(hidden);
                submit.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        boolean everythingCool = true;
                        //validation here
                        //end validation
                        if (everythingCool) {
                            updateStatus(dropdown.getValue().toString(), appt);
                            x.close();
                        }
                    }
                });
            }
        });
        updatePatient.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                container.getChildren().clear();
                Patient pat = pullPatientInfo(appt.getPatientID());
                Label txtName = new Label("Patient Name: ");
                Label name = new Label(pat.getFullName());

                Label txtEmail = new Label("Patient Email: ");
                TextField email = new TextField(pat.getEmail());

                Label txtAddress = new Label("Patient Address: ");
                TextField address = new TextField(pat.getAddress());

                Label txtInsurance = new Label("Patient Insurance: ");
                TextField insurance = new TextField(pat.getInsurance());

                Button submit = new Button("Submit");
                submit.setId("complete");

                HBox hidden = new HBox(txtName, name, txtEmail, email);
                HBox hidden1 = new HBox(txtAddress, address, txtInsurance, insurance);

                hidden.setSpacing(15);
                container.getChildren().addAll(hidden, hidden1, submit);
                container.setSpacing(15);
                submit.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        boolean everythingCool = true;
                        //validation here
                        //end validation
                        if (everythingCool) {
                            updatePatient(new Patient(pat.getPatientID(), email.getText(), pat.getFullName(), pat.getDob(), address.getText(), insurance.getText()));
                            x.close();
                        }
                    }

                });
            }
        });

        x.showAndWait();
        populateTable();
    }

    private Patient pullPatientInfo(int patID) {
        Patient temp = null;

        String sql = "Select * "
                + " FROM patients"
                + " WHERE patientID = '" + patID + "';";
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            while (rs.next()) {
                //What I receieve:  patientID, email, full_name, dob, address, insurance
                temp = new Patient(rs.getInt("patientID"), rs.getString("email"), rs.getString("full_name"), rs.getString("dob"), rs.getString("address"), rs.getString("insurance"));
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return temp;
    }

    private void updatePatient(Patient patient) {
        String sql = "UPDATE patients "
                + " SET email = '" + patient.getEmail() + "', address = '" + patient.getAddress() + "', insurance = '" + patient.getInsurance() + "' "
                + " WHERE patientID = '" + patient.getPatientID() + "';";
        App.executeSQLStatement(App.fileName, sql);
    }

    private void updateTime(String string, int apptID) {
        String sql = "UPDATE appointments "
                + " SET time = '" + string + "' "
                + " WHERE appt_id = '" + apptID + "';";
        App.executeSQLStatement(App.fileName, sql);
    }

    private void updateStatus(String status, Appointment appt) {
        String sql = "UPDATE appointments "
                + " SET statusCode = "
                + "     (SELECT statusID FROM statusCode WHERE status = '" + status + "') "
                + " WHERE appt_id = '" + appt.getApptID() + "';";
        App.executeSQLStatement(App.fileName, sql);

        if (status.contains("Cancelled")) {
            String sql1 = "INSERT INTO  patientOrders VALUES ('" + appt.getPatientID() + "', (SELECT orderCodeID FROM appointmentsOrdersConnector WHERE apptID = '" + appt.getApptID() + "'), '1');";
            App.executeSQLStatement(App.fileName, sql1);
        }
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

        Patient pat = null;
        ArrayList<String> orders = new ArrayList<String>();
        DatePicker datePicker = new DatePicker();

        //Class Variables
        AddAppointment() {
            TextField patFullName = new TextField("Full Name");
            TextField patEmail = new TextField("Email");
            Button check = new Button("Pull Patient Information");
            //time && order
            Label text = new Label("Insert Date: ");
            Label text1 = new Label("Insert Time (HH:MM): ");
            TextField time = new TextField("HH:MM");
            text.setPrefWidth(100);
            text1.setPrefWidth(150);

            Label tutorial = new Label("Click to remove: ");
            tutorial.setPrefWidth(100);

            Button submit = new Button("Submit");
            submit.setId("complete");
            //
            HBox initialContainer = new HBox(patFullName, patEmail, check);
            initialContainer.setSpacing(10);
            HBox hiddenContainer = new HBox(text, datePicker, text1, time);
            hiddenContainer.setSpacing(10);
            HBox hiddenOrderContainer = new HBox();
            hiddenOrderContainer.setSpacing(10);

            HBox hiddenContainer1 = new HBox(submit);
            hiddenContainer1.setSpacing(10);
            VBox container = new VBox(initialContainer, hiddenContainer, hiddenOrderContainer, hiddenContainer1);
            container.setAlignment(Pos.CENTER);
            initialContainer.setAlignment(Pos.CENTER);
            hiddenContainer.setAlignment(Pos.CENTER);
            hiddenOrderContainer.setAlignment(Pos.CENTER);
            hiddenContainer1.setAlignment(Pos.CENTER);
            container.setPadding(new Insets(10));
            initialContainer.setPadding(new Insets(10));
            hiddenContainer.setPadding(new Insets(10));
            hiddenOrderContainer.setPadding(new Insets(10));
            hiddenContainer1.setPadding(new Insets(10));
            hiddenContainer.setVisible(false);
            hiddenOrderContainer.setVisible(false);
            hiddenContainer1.setVisible(false);
            Scene newScene = new Scene(container);
            newScene.getStylesheets().add("file:stylesheet.css");
            this.setScene(newScene);

            check.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    pat = pullPatientInfo(patFullName.getText(), patEmail.getText());
                    if (pat != null) {
                        check.setVisible(false);
                        Label request = new Label("Orders Requested: ");
                        request.setPrefWidth(150);
                        ComboBox dropdown = getPatOrders(pat.getPatientID());
                        dropdown.setPrefWidth(100);

                        hiddenContainer.getChildren().addAll(request, dropdown);
                        hiddenContainer.setVisible(true);
                        hiddenOrderContainer.setVisible(true);
                        hiddenContainer1.setVisible(true);

                        dropdown.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                                orders.add(dropdown.getValue().toString());
                                Button temp = new Button(dropdown.getValue().toString());
                                hiddenOrderContainer.getChildren().add(temp);
                                temp.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent t) {
                                        if (!dropdown.getValue().toString().isBlank()) {
                                            orders.remove(temp.getText());
                                            hiddenOrderContainer.getChildren().remove(temp);
                                        }
                                    }
                                });
                            }
                        });

                    }
                }

            });

            submit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    boolean everythingCool = true;
                    if (everythingCool) {
                        insertAppointment(pat.getPatientID(), orders, datePicker.getValue().toString() + " " + time.getText());

                    }
                }
            });

        }

        private void insertAppointment(int patientID, ArrayList<String> orders, String time) {
            String sql = "INSERT INTO appointments(patient_id, time, statusCode)"
                    + " VALUES ('" + patientID + "', '" + time + "', '1');\n";
            App.executeSQLStatement(App.fileName, sql);
            for (String x : orders) {
                String sql1 = "INSERT INTO appointmentsOrdersConnector(apptID, orderCodeID)"
                        + " VALUES ("
                        + " (SELECT appt_id FROM appointments WHERE patient_id = '" + patientID + "' AND time = '" + time + "') , "
                        + " (SELECT orderID FROM orderCodes WHERE orders = '" + x + "') "
                        + ");\n";

                App.executeSQLStatement(App.fileName, sql1);
                String sql2 = "DELETE FROM patientOrders WHERE patientID = '" + patientID + "' AND orderCodeID = (SELECT orderID FROM orderCodes WHERE orders = '" + x + "')";
                App.executeSQLStatement(App.fileName, sql2);
            }
            this.close();
        }

        private ComboBox getPatOrders(int patientID) {
            String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
            String sql = "Select orderCodes.orders "
                    + " FROM patientOrders "
                    + " INNER JOIN orderCodes ON patientOrders.orderCodeID = orderCodes.orderID "
                    + " WHERE patientID = '" + patientID + "';";
            ComboBox value = new ComboBox();
            try {
                Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                //

                while (rs.next()) {
                    value.getItems().add(rs.getString("orders"));
                }
                //
                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return value;
        }

        private Patient pullPatientInfo(String patFullName, String patEmail) {
            Patient temp = null;

            String sql = "Select * "
                    + " FROM patients"
                    + " WHERE email = '" + patEmail + "' AND full_name = '" + patFullName + "';";
            String url = "jdbc:sqlite:C://sqlite/" + App.fileName;

            try {
                Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                //
                while (rs.next()) {
                    //What I receieve:  patientID, email, full_name, dob, address, insurance
                    temp = new Patient(rs.getInt("patientID"), rs.getString("email"), rs.getString("full_name"), rs.getString("dob"), rs.getString("address"), rs.getString("insurance"));
                }
                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return temp;
        }
    }
}
