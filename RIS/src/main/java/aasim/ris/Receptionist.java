package aasim.ris;

/**
 *
 * @author 14048
 */
import datastorage.User;
import datastorage.Appointment;
import datastorage.InputValidation;
import datastorage.Patient;
import datastorage.PatientAlert;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Receptionist extends Stage {

    //<editor-fold>
    //Stage Structure
    HBox navbar = new HBox();
    Button logOut = new Button("Log Out");
    Label username = new Label("Logged In as: " + App.user.getFullName());
    ImageView pfp = new ImageView(App.user.getPfp());

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
        pfp.setPreserveRatio(true);
        pfp.setFitHeight(38);
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        username.setId("navbar");
        username.setOnMouseClicked(eh -> userInfo());
        navbar.getChildren().addAll(username, pfp, logOut);
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

    private void userInfo() {
        Stage x = new UserInfo();
        x.show();
        x.setMaximized(true);
        this.close();
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

    //On button press, open up a new stage
    private void updateAppointment(Appointment appt) {
        Stage x = new Stage();
        x.setTitle("Update Appointment");
        x.initOwner(this);
        x.setHeight(250);
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
                Text text = new Text("Insert Date: ");
                Text text1 = new Text("Insert Time (HH:MM): ");
                TextField time = new TextField("HH:MM");
//                text.setPrefWidth(100);
//                text1.setPrefWidth(150);
                Button submit = new Button("Submit");
                submit.setPrefWidth(100);
                submit.setId("complete");

                HBox hidden = new HBox(text, datePicker, text1, time, submit);
                hidden.setSpacing(15);
                container.getChildren().add(hidden);
                submit.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        //validation here
                        if (!InputValidation.validateDate(datePicker.getValue().toString())) {
                            return;
                        }
                        if (!InputValidation.validateTime(time.getText())) {
                            return;
                        }
                        //end validation
                        updateTime(datePicker.getValue().toString() + " " + time.getText(), appt.getApptID());
                        x.close();
                    }

                });
            }
        });
        updateStatus.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                container.getChildren().clear();

                ComboBox dropdown = new ComboBox();
                dropdown.getItems().addAll("Patient Did Not Show", "Appointment Scheduled", "Patient Checked In", "Patient received by Technician", "Patient Cancelled", "Faculty Cancelled");

                dropdown.setValue(appt.getStatus());
                Button submit = new Button("Submit");
                submit.setId("complete");

                HBox hidden = new HBox(dropdown, submit);
                hidden.setSpacing(15);
                container.getChildren().add(hidden);
                submit.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        //validation here
                        //end validation
                        if (!dropdown.getValue().toString().isBlank()) {
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

                Patient pat = pullPatientInfo(appt.getPatientID());
                x.close();
                updatePatient(pat);
            }
        });

        x.showAndWait();
        populateTable();
    }

    private void updatePatient(Patient z) {
        ArrayList<PatientAlert> paList = populatePaList();
        ArrayList<PatientAlert> allergies = populateAllergies(z);

        VBox container = new VBox();
        Stage x = new Stage();
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        x.setTitle("Update Patient");
        Scene scene = new Scene(container);
        x.setScene(scene);
        x.setHeight(400);
        x.setWidth(300);
        scene.getStylesheets().add("file:stylesheet.css");
        //
        Text emailText = new Text("Email: ");
        TextField email = new TextField(z.getEmail());
        HBox emailContainer = new HBox(emailText, email);

        Text addressText = new Text("Address: ");
        TextField address = new TextField(z.getAddress());
        HBox addressContainer = new HBox(addressText, address);

        Text insuranceText = new Text("Insurance: ");
        TextField insurance = new TextField(z.getInsurance());
        HBox insuranceContainer = new HBox(insuranceText, insurance);

        Button submit = new Button("Submit");
        submit.setId("complete");

        ArrayList<PatientAlert> alertsToAddForThisPatient = new ArrayList<PatientAlert>();
        ArrayList<PatientAlert> alertsToRemoveForThisPatient = new ArrayList<PatientAlert>();
        VBox patientAlertContainer = new VBox();
        for (PatientAlert a : paList) {
            Text Text = new Text(a.getAlert());
            ComboBox dropdown = new ComboBox();
            dropdown.getItems().addAll("Yes", "No");
            if (allergies.contains(a)) {
                dropdown.setValue("Yes");
            } else {
                dropdown.setValue("No");
            }
            HBox temp = new HBox(Text, dropdown);
            temp.setSpacing(10);
            temp.setPadding(new Insets(10));

            patientAlertContainer.getChildren().add(temp);

            dropdown.setOnAction(new EventHandler() {
                @Override
                public void handle(Event eh) {
                    if (dropdown.getValue().toString().equals("Yes")) {
                        alertsToAddForThisPatient.add(a);
                        alertsToRemoveForThisPatient.remove(a);
                    } else if (dropdown.getValue().toString().equals("No")) {
                        alertsToAddForThisPatient.remove(a);
                        alertsToRemoveForThisPatient.add(a);
                    }
                }
            });
        }

        ScrollPane s1 = new ScrollPane(patientAlertContainer);
        s1.setPrefHeight(200);

        container.getChildren().addAll(emailContainer, addressContainer, insuranceContainer, s1, submit);
        x.show();

        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                //Validation
                if (!InputValidation.validateEmail(email.getText())) {
                    return;
                }
                if (!InputValidation.validateAddress(address.getText())) {
                    return;
                }
                if (!InputValidation.validateInsurance(insurance.getText())) {
                    return;
                }
                //End Validation
                z.setAddress(address.getText());
                z.setEmail(email.getText());
                z.setInsurance(insurance.getText());
                String sql = "UPDATE patients SET email = '" + email.getText() + "', address = '" + address.getText() + "', insurance = '" + insurance.getText() + "';";
                App.executeSQLStatement(sql);
                for (PatientAlert a : alertsToAddForThisPatient) {
                    sql = "INSERT INTO alertsPatientConnector VALUES ( '" + z.getPatientID() + "', '" + a.getAlertID() + "');";
                    App.executeSQLStatement(sql);
                }
                for (PatientAlert a : alertsToRemoveForThisPatient) {
                    sql = "DELETE FROM alertsPatientConnector WHERE patientID = '" + z.getPatientID() + "' AND alertID = '" + a.getAlertID() + "';";
                    App.executeSQLStatement(sql);
                }
                x.close();
            }
        });
    }

    private ArrayList<PatientAlert> populatePaList() {
        ArrayList<PatientAlert> paList = new ArrayList<>();
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select patientAlerts.alertID, patientAlerts.alert "
                + " FROM patientAlerts "
                + " "
                + " ;";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {
                PatientAlert pa = new PatientAlert(rs.getInt("alertID"), rs.getString("alert"), getFlagsFromDatabase(rs.getInt("alertID")));
                paList.add(pa);
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return paList;
    }

    private ArrayList<PatientAlert> populateAllergies(Patient z) {
        ArrayList<PatientAlert> allergies = new ArrayList<>();
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select patientAlerts.alertID, patientAlerts.alert "
                + " FROM patientAlerts "
                + " INNER JOIN alertsPatientConnector ON patientAlerts.alertID = alertsPatientConnector.alertID "
                + " WHERE alertsPatientConnector.patientID = '" + z.getPatientID() + "'"
                + ";";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {
                PatientAlert pa = new PatientAlert(rs.getInt("alertID"), rs.getString("alert"), getFlagsFromDatabase(rs.getInt("alertID")));
                allergies.add(pa);
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return allergies;
    }

    private String getFlagsFromDatabase(int aInt) {

        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String val = "";
        String sql = "Select orderCodes.orders "
                + " FROM flags "
                + " INNER JOIN orderCodes ON flags.orderID = orderCodes.orderID "
                + " WHERE alertID = '" + aInt + "' "
                + ";";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<PatientAlert> list = new ArrayList<PatientAlert>();
            while (rs.next()) {
                //What I receieve:  patientID, email, full_name, dob, address, insurance
                val += rs.getString("orders") + ", ";
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return val;
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

    private void updateTime(String string, int apptID) {
        String sql = "UPDATE appointments "
                + " SET time = '" + string + "' "
                + " WHERE appt_id = '" + apptID + "';";
        App.executeSQLStatement(sql);
    }

    private void updateStatus(String status, Appointment appt) {
        String sql = "UPDATE appointments "
                + " SET statusCode = "
                + "     (SELECT statusID FROM statusCode WHERE status = '" + status + "') "
                + " WHERE appt_id = '" + appt.getApptID() + "';";
        App.executeSQLStatement(sql);

        if (status.contains("Cancelled")) {
            String sql1 = "INSERT INTO  patientOrders VALUES ('" + appt.getPatientID() + "', (SELECT orderCodeID FROM appointmentsOrdersConnector WHERE apptID = '" + appt.getApptID() + "'), '1');";
            App.executeSQLStatement(sql1);
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
            Text text = new Text("Insert Date: ");
            Text text1 = new Text("Insert Time (HH:MM): ");
            TextField time = new TextField("HH:MM");
//            text.setPrefWidth(100);
//            text1.setPrefWidth(150);

            Text tutorial = new Text("Click to remove: ");
//            tutorial.setPrefWidth(100);

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
            hiddenOrderContainer.getChildren().add(tutorial);
            check.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    if (!InputValidation.validateName(patFullName.getText())) {
                        return;
                    }
                    if (!InputValidation.validateEmail(patEmail.getText())) {
                        return;
                    }
                    pat = pullPatientInfo(patFullName.getText(), patEmail.getText());
                    if (pat != null) {
                        check.setVisible(false);
                        Text request = new Text("Orders Requested: ");
//                        request.setPrefWidth(150);
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

                    } else {
                        Alert a = new Alert(Alert.AlertType.INFORMATION);
                        a.setTitle("Error");
                        a.setHeaderText("Try Again");
                        a.setContentText("Patient not found\nPlease verify all information or contact the patient's Referral Doctor\n");
                        a.show();
                        return;
                    }
                }

            });

            submit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    if (!InputValidation.validateDate(datePicker.getValue().toString())) {
                        return;
                    }
                    if (!InputValidation.validateTime(time.getText())) {
                        return;
                    }
                    insertAppointment(pat.getPatientID(), orders, datePicker.getValue().toString() + " " + time.getText());
                }
            });

        }

        private void insertAppointment(int patientID, ArrayList<String> orders, String time) {
            String sql = "INSERT INTO appointments(patient_id, time, statusCode)"
                    + " VALUES ('" + patientID + "', '" + time + "', '1');\n";
            App.executeSQLStatement(sql);
            for (String x : orders) {
                String sql1 = "INSERT INTO appointmentsOrdersConnector(apptID, orderCodeID)"
                        + " VALUES ("
                        + " (SELECT appt_id FROM appointments WHERE patient_id = '" + patientID + "' AND time = '" + time + "') , "
                        + " (SELECT orderID FROM orderCodes WHERE orders = '" + x + "') "
                        + ");\n";

                App.executeSQLStatement(sql1);
                String sql2 = "DELETE FROM patientOrders WHERE patientID = '" + patientID + "' AND orderCodeID = (SELECT orderID FROM orderCodes WHERE orders = '" + x + "')";
                App.executeSQLStatement(sql2);
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
