package aasim.ris;

import datastorage.Appointment;
import datastorage.InputValidation;
import datastorage.Patient;
import datastorage.PatientAlert;
import datastorage.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ReferralDoctor extends Stage {
    //Navbar

    HBox navbar = new HBox();
    Label username = new Label("Logged In as: " + App.user.getFullName());
    ImageView pfp = new ImageView(App.user.getPfp());

    Button logOut = new Button("Log Out");

    //End Navbar
    //table
    TableView patientsTable = new TableView();
    TableView appointmentsTable = new TableView();
    HBox buttonContainer = new HBox();
    VBox tableContainer = new VBox(patientsTable, buttonContainer);
    //
    //Scene
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);

    //End Scene
    private FilteredList<Patient> flPatient;
    ChoiceBox<String> choiceBox = new ChoiceBox();
    TextField search = new TextField("Search Patients");
    HBox searchContainer = new HBox(choiceBox, search);

    ArrayList<PatientAlert> paList = new ArrayList<PatientAlert>(); //GeneralOverview 
    ArrayList<PatientAlert> allergies = new ArrayList<PatientAlert>(); //Specific to the Patient

    public ReferralDoctor() {
        this.setTitle("RIS - Radiology Information System (Doctor View)");
        //Navbar
        pfp.setPreserveRatio(true);
        pfp.setFitHeight(38);
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }
        });
        username.setId("navbar");
        username.setOnMouseClicked(eh -> userInfo());
        navbar.getChildren().addAll(username, pfp, logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);
        //End navbar

        //Center
        tableContainer.setSpacing(10);
        main.setCenter(tableContainer);
        createTablePatients();
        populateTable();
        //Button Container
        buttonContainer.setSpacing(10);
        buttonContainer.setPadding(new Insets(5));

        Button addPatient = new Button("Add Patient");
        addPatient.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                addPatient();
            }

        });
        buttonContainer.getChildren().addAll(addPatient);
        //End Button Container
        //End Center
        //Searchbar Structure
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.setValue("Patient ID");
        choiceBox.getItems().addAll("Patient ID", "Full Name", "Email", "Date of Birth", "Insurance");
        choiceBox.setValue("Appointment ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
            if (choiceBox.getValue().equals("Patient ID")) {
                flPatient.setPredicate(p -> new String(p.getPatientID() + "").contains(newValue));//filter table by Appt ID
            }
            if (choiceBox.getValue().equals("Full Name")) {
                flPatient.setPredicate(p -> p.getFullName().toLowerCase().contains(newValue.toLowerCase()));//filter table by Patient Id
            }
            if (choiceBox.getValue().equals("Email")) {
                flPatient.setPredicate(p -> p.getEmail().toLowerCase().contains(newValue.toLowerCase()));//filter table by Full name
            }
            if (choiceBox.getValue().equals("Date of Birth")) {
                flPatient.setPredicate(p -> p.getDob().contains(newValue));//filter table by Date/Time
            }
            if (choiceBox.getValue().equals("Insurance")) {
                flPatient.setPredicate(p -> p.getInsurance().contains(newValue));//filter table by Date/Time
            }
            patientsTable.getItems().clear();
            patientsTable.getItems().addAll(flPatient);
        });
        buttonContainer.getChildren().add(searchContainer);
        //End Searchbar Structure

        //Set Scene and Structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);

        populatePaList();
    }

    private void createTablePatients() {
        //All of the Columns
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn fullNameCol = new TableColumn("Full Name");
        TableColumn emailCol = new TableColumn("Email");
        TableColumn DOBCol = new TableColumn("Date of Birth");
        TableColumn updateStatusCol = new TableColumn("View Patient Overview");

        //And all of the Value setting
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        DOBCol.setCellValueFactory(new PropertyValueFactory<>("dob"));
        updateStatusCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));

        //Couldn't put the table
        patientIDCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.05));
        fullNameCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.1));
        emailCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        DOBCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.1));
        updateStatusCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.5));

        patientsTable.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        //back together again
        patientsTable.getColumns().addAll(patientIDCol, fullNameCol, emailCol, DOBCol, updateStatusCol);
    }

    private void populateTable() {
        patientsTable.getItems().clear();
        //Connect to database
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select docPatientConnector.patientID, patients.email, patients.full_name, patients.dob, patients.address, patients.insurance"
                + " FROM docPatientConnector"
                + " INNER JOIN patients ON docPatientConnector.patientID = patients.patientID"
                + " WHERE docPatientConnector.referralDocID = '" + App.user.getUserID() + "';";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<Patient> list = new ArrayList<Patient>();
            while (rs.next()) {
                //What I receieve:  patientID, email, full_name, dob, address, insurance
                Patient pat = new Patient(rs.getInt("patientID"), rs.getString("email"), rs.getString("full_name"), rs.getString("dob"), rs.getString("address"), rs.getString("insurance"));
                list.add(pat);
            }

            for (Patient z : list) {
                z.placeholder.setText("Patient Overview");
                z.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        patientOverviewScreen(z);
                    }

                });
            }

            flPatient = new FilteredList(FXCollections.observableList(list), p -> true);
            patientsTable.getItems().addAll(flPatient);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void logOut() {
        App.user = new User();
        Stage x = new Login();
        x.show();
        x.setMaximized(true);
        this.close();
    }

    private void userInfo() {
        Stage x = new UserInfo();
        x.show();
        x.setMaximized(true);
        this.close();
    }

    private void addPatient() {

        Stage x = new Stage();
        x.initOwner(this);
        x.setTitle("Add Patient");
        x.initModality(Modality.WINDOW_MODAL);
        BorderPane y = new BorderPane();
        Label text = new Label("Email: ");
        TextField email = new TextField();
        email.setPrefWidth(250);
        Label text1 = new Label("Full Name: ");
        TextField name = new TextField("");
        name.setPrefWidth(150);
        Button pullData = new Button("Check for Patient");
        HBox container = new HBox(text, email, text1, name, pullData);
        //Hidden Containers
        Label text2 = new Label("Date of Birth: \n(Press Enter)");
        DatePicker datePicker = new DatePicker();
        Label text3 = new Label("Address: ");
        TextField address = new TextField("");
        address.setPrefWidth(200);
        Label text4 = new Label("Insurance: ");
        TextField insurance = new TextField("");
        insurance.setPrefWidth(200);
        //
        ArrayList<PatientAlert> alertsToAddForThisPatient = new ArrayList<PatientAlert>();
        VBox patientAlertContainer = new VBox();
        for (PatientAlert z : paList) {
            Label label = new Label(z.getAlert());
            ComboBox dropdown = new ComboBox();
            dropdown.getItems().addAll("Yes", "No");
            dropdown.setValue("No");

            HBox temp = new HBox(label, dropdown);
            temp.setSpacing(10);
            temp.setPadding(new Insets(10));

            patientAlertContainer.getChildren().add(temp);

            dropdown.setOnAction(new EventHandler() {
                @Override
                public void handle(Event eh) {
                    if (dropdown.getValue().toString().equals("Yes")) {
                        alertsToAddForThisPatient.add(z);
                    } else if (dropdown.getValue().toString().equals("No")) {
                        alertsToAddForThisPatient.remove(z);
                    }
                }
            });

        }
        //
        ScrollPane s1 = new ScrollPane(patientAlertContainer);
        s1.setPrefHeight(200);
        s1.setVisible(false);

        HBox hiddenContainer1 = new HBox(text2, datePicker, text3, address);
//        HBox hiddenContainer2 = new HBox();
        HBox hiddenContainer3 = new HBox(text4, insurance);
        Button hiddenSubmit = new Button("Create New Patient");

        text1.setPrefWidth(100);
        text2.setPrefWidth(100);
        text3.setPrefWidth(100);
        text4.setPrefWidth(100);
        hiddenContainer1.setSpacing(10);
        hiddenContainer1.setPadding(new Insets(10));

        hiddenContainer3.setSpacing(10);
        hiddenContainer3.setPadding(new Insets(10));

        hiddenSubmit.setPadding(new Insets(10));

        hiddenContainer1.setVisible(false);
        hiddenContainer3.setVisible(false);
        hiddenSubmit.setVisible(false);
        //End Hidden Containers
        Label text5 = new Label("Patient ID: ");
        text5.setPrefWidth(150);
        VBox center = new VBox(container, hiddenContainer1, hiddenContainer3, s1, hiddenSubmit);

        container.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        y.setCenter(center);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));
        x.show();

        datePicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                LocalDate date = datePicker.getValue();
            }
        });

        pullData.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                //validation
                if (!InputValidation.validateName(name.getText())) {
                    return;
                }
                if (!InputValidation.validateEmail(email.getText())) {
                    return;
                }
                //end validation
                Patient temp = checkDatabaseForPatient(name.getText(), email.getText());
                //Temp != null (patient already exists)
                if (temp != null) {
                    //name, dob, address, insurance
                    name.setText(temp.getFullName());
                    name.setEditable(false);
                    datePicker.setValue(LocalDate.parse(temp.getDob()));
                    datePicker.setEditable(false);

                    address.setText(temp.getAddress());
                    address.setEditable(false);
                    insurance.setText(temp.getInsurance());
                    insurance.setEditable(false);
                }

                s1.setVisible(true);
                email.setEditable(false);
                name.setEditable(false);
                hiddenContainer1.setVisible(true);
                hiddenContainer3.setVisible(true);
                pullData.setVisible(false);
                hiddenSubmit.setVisible(true);
            }
        });

        hiddenSubmit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                //Validation
                if (!InputValidation.validateDate(datePicker.getValue().toString())) {
                    return;
                }
                if (!InputValidation.validateAddress(address.getText())) {
                    return;
                }

                if (!InputValidation.validateInsurance(insurance.getText())) {
                    return;
                }
                //End Validation
                insertPatientIntoDatabase(name.getText(), email.getText(), datePicker.getValue().toString(), address.getText(), insurance.getText());
                populateTable();
                for (PatientAlert z : alertsToAddForThisPatient) {
                    String sql = "INSERT INTO alertsPatientConnector VALUES ( (SELECT patientID FROM patients WHERE full_name = '" + name.getText() + "' AND email = '" + email.getText() + "') , '" + z.getAlertID() + "');";
                    App.executeSQLStatement(sql);
                }
                x.close();
            }

        });

    }

    private Patient checkDatabaseForPatient(String name, String email) {
        Patient temp = null;

        String sql = "Select * "
                + " FROM patients"
                + " WHERE email = '" + email + "' AND full_name = '" + name + "';";
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

    private void insertPatientIntoDatabase(String name, String email, String dob, String address, String insurance) {
        String sql = "INSERT INTO patients(full_name, email, dob, address, insurance) "
                + " VALUES ('" + name + "','" + email + "','" + dob + "','" + address + "','" + insurance + "');";
        App.executeSQLStatement(sql);

        int patientID = checkDatabaseForPatient(name, email).getPatientID();
        String sql1 = "INSERT INTO docPatientConnector "
                + " VALUES ('" + App.user.getUserID() + "', '" + patientID + "');";
        App.executeSQLStatement(sql1);

    }

    private void patientOverviewScreen(Patient z) {
        populateAllergies(z);
        // Appointments table
        appointmentsTable.getColumns().clear();
        //Patient Info
        Label text = new Label("Name: " + z.getFullName() + "\t\t Email: " + z.getEmail() + "\t\t Date of Birth: " + z.getDob());
        Label text1 = new Label("Address: " + z.getAddress() + "\t\t Insurance Provider: " + z.getInsurance() + " ");
        Label text2 = new Label("Orders Requested: " + getPatOrders(z.getPatientID()));
        VBox patInfo = new VBox(text, text1, text2);
        patInfo.setAlignment(Pos.CENTER);

        //Create appointments table columns
        TableColumn apptIDCol = new TableColumn("Appt. ID");
        TableColumn timeCol = new TableColumn("Time");
        TableColumn ordersCol = new TableColumn("Orders");
        TableColumn statusCol = new TableColumn("Status");
        TableColumn updateStatusCol = new TableColumn("View Appointment Orders");
        //Set tableColumn getters
        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptID"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        ordersCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        updateStatusCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));
        //Set tableColumn sizes
        apptIDCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.05));
        timeCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.1));
        ordersCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.2));
        statusCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.1));
        updateStatusCol.prefWidthProperty().bind(patientsTable.widthProperty().multiply(0.5));
        appointmentsTable.getColumns().addAll(apptIDCol, timeCol, ordersCol, statusCol, updateStatusCol);
        //Buttons
        Button goBack = new Button("Go Back");
        Button delete = new Button("Remove Patient from View");
        Button updatePatientInformation = new Button("Update Patient Information");
        Button newOrder = new Button("New Order");
        Button removeOrder = new Button("Remove Order");
        goBack.setId("cancel");
        delete.setId("cancel");
        newOrder.setId("complete");
        HBox toTheLeft = new HBox(goBack);
        HBox.setHgrow(toTheLeft, Priority.ALWAYS);
        HBox toTheRight = new HBox(delete);
        HBox.setHgrow(toTheRight, Priority.ALWAYS);
        toTheRight.setAlignment(Pos.CENTER_RIGHT);
        HBox overviewButtonContainer = new HBox(toTheLeft, newOrder, removeOrder, updatePatientInformation, toTheRight);
        overviewButtonContainer.setSpacing(10);
        //End Buttons
//
//
        VBox patientAlertContainer = new VBox();
        for (PatientAlert i : allergies) {
            Label label = new Label(i.getAlert());
            ComboBox dropdown = new ComboBox();
            dropdown.getItems().addAll("Yes");
            dropdown.setValue("Yes");
            dropdown.setEditable(false);
            HBox temp = new HBox(label, dropdown);
            temp.setSpacing(10);
            temp.setPadding(new Insets(10));

            patientAlertContainer.getChildren().add(temp);

//            dropdown.setOnAction(new EventHandler() {
//                @Override
//                public void handle(Event eh) {
//                    if (dropdown.getValue().toString().equals("Yes")) {
//                        String sql = "INSERT INTO alertsPatientConnector VALUES ( '" + z.getPatientID() + "' , '" + i.getAlertID() + "');";
//                        App.executeSQLStatement(sql);
//                    } else if (dropdown.getValue().toString().equals("No")) {
//                        String sql = "DELETE FROM alertsPatientConnector WHERE patientID = '" + z.getPatientID() + "' AND alertID = '" + i.getAlertID() + "';";
//                        App.executeSQLStatement(sql);
//                    }
//                }
//            });
        }
        ScrollPane s1 = new ScrollPane(patientAlertContainer);
        s1.setPrefHeight(200);
        s1.setVisible(true);
//
        VBox overviewContainer = new VBox(patInfo, appointmentsTable, overviewButtonContainer, s1);
        overviewContainer.setSpacing(10);
        main.setCenter(overviewContainer);
        //
        populateAppointmentsTable(z);
        //

        newOrder.setOnAction((ActionEvent e) -> {
            createNewOrder(z);
            text2.setText("Orders Requested: " + getPatOrders(z.getPatientID()));
        });
        removeOrder.setOnAction((ActionEvent e) -> {
            removeOrder(z);
            text2.setText("Orders Requested: " + getPatOrders(z.getPatientID()));
        });
        goBack.setOnAction((ActionEvent e) -> {
            appointmentsTable.getColumns().clear();
            main.setCenter(tableContainer);
        });
        delete.setOnAction((ActionEvent e) -> {
            removePatientFromView(z);
        });
        updatePatientInformation.setOnAction((ActionEvent e) -> {
            updatePatient(z);
        });

    }

    private void updatePatient(Patient z) {
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
        Label emailLabel = new Label("Email: ");
        TextField email = new TextField(z.getEmail());
        HBox emailContainer = new HBox(emailLabel, email);

        Label addressLabel = new Label("Address: ");
        TextField address = new TextField(z.getAddress());
        HBox addressContainer = new HBox(addressLabel, address);

        Label insuranceLabel = new Label("Insurance: ");
        TextField insurance = new TextField(z.getInsurance());
        HBox insuranceContainer = new HBox(insuranceLabel, insurance);

        Button submit = new Button("Submit");
        submit.setId("complete");

        ArrayList<PatientAlert> alertsToAddForThisPatient = new ArrayList<PatientAlert>();
        ArrayList<PatientAlert> alertsToRemoveForThisPatient = new ArrayList<PatientAlert>();
        VBox patientAlertContainer = new VBox();
        for (PatientAlert a : paList) {
            Label label = new Label(a.getAlert());
            ComboBox dropdown = new ComboBox();
            dropdown.getItems().addAll("Yes", "No");
            if (allergies.contains(a)) {
                dropdown.setValue("Yes");
            } else {
                dropdown.setValue("No");
            }
            HBox temp = new HBox(label, dropdown);
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
                appointmentsTable.getColumns().clear();
                patientOverviewScreen(z);
            }
        });
    }

    private void removePatientFromView(Patient z) {
        Stage x = new Stage();
        x.initOwner(this);
        x.setTitle("Add Order");
        x.initModality(Modality.WINDOW_MODAL);
        Label text1 = new Label("Are you sure? Type 'CONFIRM' without quotes to continue ");
        TextField text = new TextField("whyyyy");
        Button killIt = new Button("Confirm Delete");
        killIt.setId("cancel");
        VBox cont = new VBox(text1, text, killIt);
        cont.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(cont));
        killIt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (!InputValidation.validateConfirm(text.getText())) {
                    return;
                }
                String sql = "DELETE FROM docPatientConnector WHERE patientID = '" + z.getPatientID() + "' AND referralDocID = '" + App.user.getUserID() + "';";
                App.executeSQLStatement(sql);
                x.close();
                populateTable();
                appointmentsTable.getColumns().clear();
                main.setCenter(tableContainer);

            }
        });

        x.showAndWait();

    }

    private void populateAppointmentsTable(Patient pat) {
        appointmentsTable.getItems().clear();
        //Connect to database
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select appointments.appt_id, appointments.time, statusCode.status"
                + " FROM appointments"
                + " INNER JOIN statusCode ON appointments.statusCode = statusCode.statusID "
                + " WHERE patient_id = '" + pat.getPatientID() + "' AND statusCode < 7"
                + " ORDER BY time ASC;";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<Appointment> list = new ArrayList<Appointment>();
            while (rs.next()) {
                //What I receieve:  
                Appointment temp = new Appointment(rs.getInt("appt_id"), pat.getPatientID(), rs.getString("time"), rs.getString("status"), getPatOrders(pat.getPatientID(), rs.getInt("appt_id")));
                list.add(temp);
            }

            for (Appointment x : list) {
                if (x.getStatus().contains(".")) {
                    x.placeholder.setText("View Radiology Report");
                    if (!x.getStatus().contains("Signature")) {
                        x.placeholder.setId("complete");
                    }
                    x.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            viewRadiologyReport(pat, x);
                        }
                    });

                } else {
                    x.placeholder.setText("Radiology Report not created yet");
                    x.placeholder.setId("cancel");
                }
            }

            appointmentsTable.getItems().addAll(list);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getPatOrders(int patientID) {
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select orderCodes.orders "
                + " FROM patientOrders "
                + " INNER JOIN orderCodes ON patientOrders.orderCodeID = orderCodes.orderID "
                + " WHERE patientID = '" + patientID + "';";
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

    private void createNewOrder(Patient z) {
        Stage x = new Stage();
        x.initOwner(this);
        x.setTitle("Add Order");
        x.initModality(Modality.WINDOW_MODAL);
        BorderPane y = new BorderPane();
        Label text1 = new Label("Order: ");
        text1.setPrefWidth(200);

        ComboBox dropdown = populateOrdersDropdown();
        Button insertOrder = new Button("Create New Order");
        HBox container = new HBox(text1, dropdown, insertOrder);
        //End Hidden Containers
        VBox center = new VBox(container);

        container.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        y.setCenter(center);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));

        insertOrder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (!dropdown.getValue().toString().isBlank()) {
                    //
                    for (PatientAlert z : allergies) {
                        if (z.getFlags().contains(dropdown.getValue().toString())) {
                            Alert a = new Alert(Alert.AlertType.INFORMATION);
                            a.setTitle("Patient Alert");
                            a.setHeaderText(dropdown.getValue().toString());
                            a.setContentText("Patient is allergic to procedure. \n");
                            a.show();
                            return;
                        }
                    }
                    //
                    insertNewOrder(z, dropdown.getValue().toString());
                    x.close();
                }
            }
        });
        x.showAndWait();

    }

    private void removeOrder(Patient z) {
        Stage x = new Stage();
        x.initOwner(this);
        x.setTitle("Remove Order");
        x.initModality(Modality.WINDOW_MODAL);
        BorderPane y = new BorderPane();
        Label text1 = new Label("Order to remove: ");
        text1.setPrefWidth(150);
        ComboBox dropdown = populateOrdersDropdown();
        Button insertOrder = new Button("Remove Order");
        HBox container = new HBox(text1, dropdown, insertOrder);
        Label confirmTxt = new Label("Type 'CONFIRM' to continue");
        TextField confirm = new TextField();
        HBox cont = new HBox(confirmTxt, confirm);
        //End Hidden Containers
        VBox center = new VBox(container, cont);

        container.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        y.setCenter(center);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));

        insertOrder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (!dropdown.getValue().toString().isBlank()) {
                    removeOrder(z, dropdown.getValue().toString());
                    x.close();
                }
            }

        });
        x.showAndWait();
    }

    private void removeOrder(Patient z, String order) {
        String sql = "DELETE FROM patientOrders "
                + " WHERE patientID = '" + z.getPatientID() + "' AND orderCodeID = (SELECT orderCodes.orderID FROM orderCodes WHERE orderCodes.orders = '" + order + "')"
                + "     AND ROWID = (SELECT ROWID FROM patientOrders WHERE patientID = '" + z.getPatientID() + "' AND orderCodeID = (SELECT orderCodes.orderID FROM orderCodes WHERE orderCodes.orders = '" + order + "') LIMIT 1)"
                + " ;";
        App.executeSQLStatement(sql);
    }

    private ComboBox populateOrdersDropdown() {
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select orders "
                + " FROM orderCodes;";
        ComboBox dropdown = new ComboBox();
        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {
                dropdown.getItems().add(rs.getString("orders"));
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dropdown;
    }

    private void insertNewOrder(Patient z, String x) {
        String sql = "INSERT INTO  patientOrders VALUES ('" + z.getPatientID() + "', (SELECT orderID FROM orderCodes WHERE orders = '" + x + "'), '1');";
        App.executeSQLStatement(sql);
    }

    private void viewRadiologyReport(Patient z, Appointment appt) {
        Stage x = new Stage();
        x.initOwner(this);
        x.setTitle("View Radiology Report");
        x.initModality(Modality.WINDOW_MODAL);
        x.setMaximized(true);
        BorderPane y = new BorderPane();

        Button confirm = new Button("Confirm Read Receipt");
        confirm.setId("complete");
        //
        VBox imgContainer = new VBox();
        ArrayList<Pair> list = retrieveUploadedImages(appt.getApptID());
        if (list.isEmpty()) {
            System.out.println("Error, image list is empty");
        } else {
            int counter = 0;
            for (Pair i : list) {
                ImageView temp = new ImageView(i.getImg());
                temp.setFitHeight(300);
//                Button download = new Button("Download");
                imgContainer.getChildren().addAll(temp);

//                download.setOnAction(new EventHandler<ActionEvent>() {
//                    @Override
//                    public void handle(ActionEvent e) {
//                        DirectoryChooser directoryChooser = new DirectoryChooser();
//                        File selectedDirectory = directoryChooser.showDialog(x);
//
//                        downloadImage(i.getImgID(), selectedDirectory);
//                    }
//
//                });
                counter++;
            }
        }
        ScrollPane s1 = new ScrollPane();
        s1.setContent(imgContainer);
        //
        Label radiologyReport = new Label();
        radiologyReport.setText("Radiology Report: \n" + getRadiologyReport(appt.getApptID()) + "\n\n");
        HBox container = new HBox(s1);
        VBox center = new VBox(container, radiologyReport, confirm);
        container.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        y.setCenter(center);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));

        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                updateStatus(appt.getApptID());
                x.close();
                populateAppointmentsTable(z);
            }

        });
        x.showAndWait();
    }

    private ArrayList<Pair> retrieveUploadedImages(int apptId) {
        //Connect to database
        ArrayList<Pair> list = new ArrayList<Pair>();

        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "SELECT *"
                + " FROM images"
                + " WHERE apptID = '" + apptId + "'"
                + " ORDER BY imageID DESC;";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            while (rs.next()) {
                //What I receieve:  image
                list.add(new Pair(new Image(rs.getBinaryStream("image")), rs.getInt("imageID")));
//                System.out.println(rs.getBinaryStream("image"));
            }
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    private String getRadiologyReport(int apptID) {
        String value = "";
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "SELECT writtenReport "
                + " FROM report"
                + " WHERE apptID = '" + apptID + "';";
        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            while (rs.next()) {
                //What I receieve:  text
                value = rs.getString("writtenReport");
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

    private void updateStatus(int apptID) {
        String sql = "UPDATE appointments "
                + " SET statusCode = 6 "
                + " WHERE appt_id = '" + apptID + "';";
        App.executeSQLStatement(sql);
    }

    private void populatePaList() {
        paList.clear();
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

    private void populateAllergies(Patient z) {
        allergies.clear();
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
    }

    private class Pair {

        Image img;
        Integer imgID;

        public Pair(Image img, Integer imgID) {
            this.img = img;
            this.imgID = imgID;
        }

        public Image getImg() {
            return img;
        }

        public void setImg(Image img) {
            this.img = img;
        }

        public Integer getImgID() {
            return imgID;
        }

        public void setImgID(Integer imgID) {
            this.imgID = imgID;
        }

    }

}
