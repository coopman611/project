package aasim.ris;

import datastorage.Appointment;
import datastorage.InputValidation;
import datastorage.Order;
import datastorage.Patient;
import datastorage.PatientAlert;
import datastorage.User;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.control.ComboBox;
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

public class Administrator extends Stage {

    //Navbar
    HBox navbar = new HBox();
    Label username = new Label("Logged In as: " + App.user.getFullName());
    ImageView pfp = new ImageView(App.user.getPfp());
    Label users = new Label("Users");
    Label patients = new Label("Patients");
    Label appointments = new Label("Appointments");
    Label modalities = new Label("Modalities");
    Label patientAlerts = new Label("Patient Alerts");
    Button logOut = new Button("Log Out");
    //End Navbar

    //table
    TableView table = new TableView();
    VBox usersContainer = new VBox();
    VBox patientsContainer = new VBox();
    VBox appointmentsContainer = new VBox();
    VBox modalitiesContainer = new VBox();
    VBox patientAlertsContainer = new VBox();
    //
    //Scene
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);

    //End Scene
    private FilteredList<User> flUsers;
    private FilteredList<Patient> flPatient;
    private FilteredList<Appointment> flAppointment;

    public Administrator() {
        this.setTitle("RIS - Radiology Information System (Administrator)");
        //Navbar
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }
        });
        pfp.setPreserveRatio(true);
        pfp.setFitHeight(38);
        username.setId("navbar");
        username.setOnMouseClicked(eh -> userInfo());
        HBox navButtons = new HBox(users, patients, appointments, modalities, patientAlerts);
        navButtons.setAlignment(Pos.TOP_LEFT);
//        navButtons.setSpacing(10);
        HBox.setHgrow(navButtons, Priority.ALWAYS);
        navbar.getChildren().addAll(navButtons, username, pfp, logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);

        users.setId("navbar");
        patients.setId("navbar");
        appointments.setId("navbar");
        modalities.setId("navbar");
        patientAlerts.setId("navbar");
        //End navbar

        //Center
        Label tutorial = new Label("Select one of the buttons above to get started!");
        main.setCenter(tutorial);
        users.setOnMouseClicked(eh -> usersPageView());
        patients.setOnMouseClicked(eh -> patientsPageView());
        appointments.setOnMouseClicked(eh -> appointmentsPageView());
        modalities.setOnMouseClicked(eh -> modalitiesPageView());
        patientAlerts.setOnMouseClicked(eh -> patientAlertsPageView());

        //End Center
        //Set Scene and Structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
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

//<editor-fold defaultstate="collapsed" desc="Users Section">
    private void createTableUsers() {
        table.getColumns().clear();
        //All of the Columns
        TableColumn pfpCol = new TableColumn("PFP");
        TableColumn userIDCol = new TableColumn("User ID");
        TableColumn emailCol = new TableColumn("Email");
        TableColumn fullNameCol = new TableColumn("Full Name");
        TableColumn usernameCol = new TableColumn("Username");
        TableColumn roleCol = new TableColumn("Role");
        TableColumn enabledCol = new TableColumn("Enabled / Disabled");
        TableColumn buttonCol = new TableColumn("Update User");

        //And all of the Value setting
        pfpCol.setCellValueFactory(new PropertyValueFactory<>("pfpView"));
        userIDCol.setCellValueFactory(new PropertyValueFactory<>("userID"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("roleVal"));
        enabledCol.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        buttonCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));

        //Couldn't put all the styling
        pfpCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        userIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        emailCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        fullNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        usernameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        roleCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        enabledCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        buttonCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        table.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        //Together again
        table.getColumns().addAll(pfpCol, userIDCol, emailCol, fullNameCol, usernameCol, roleCol, enabledCol, buttonCol);
        //Add Status Update Column:
    }

    private void populateUsersTable() {
        table.getItems().clear();
        //Connect to database
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select users.user_id, users.email, users.full_name, users.username, users.enabled, users.pfp, roles.role as roleID"
                + " FROM users "
                + " INNER JOIN roles ON users.role = roles.roleID "
                + ";";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<User> list = new ArrayList<User>();

            while (rs.next()) {
                //What I receieve:  int userID, String email, String fullName, String username, int role, int enabled
                User user = new User(rs.getInt("user_id"), rs.getString("email"), rs.getString("full_name"), rs.getString("username"), 1, rs.getInt("enabled"), rs.getString("roleID"));
                try {
                    user.setPfp(new Image(new FileInputStream(App.imagePathDirectory + rs.getString("pfp"))));
                } catch (FileNotFoundException ex) {
                    user.setPfp(null);
                }
                list.add(user);
            }
            for (User z : list) {
                z.placeholder.setText("Update User");
                z.placeholder.setOnAction(eh -> updateUser(z));
            }
            flUsers = new FilteredList(FXCollections.observableList(list), p -> true);
            table.getItems().addAll(flUsers);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void usersPageView() {
        usersContainer.getChildren().clear();

        main.setCenter(usersContainer);
        createTableUsers();
        populateUsersTable();

        Button addUser = new Button("Add User");
        HBox buttonContainer = new HBox(addUser);
        buttonContainer.setSpacing(10);
        usersContainer.getChildren().addAll(table, buttonContainer);
        usersContainer.setSpacing(10);
        users.setId("selected");
        patients.setId("navbar");
        appointments.setId("navbar");
        modalities.setId("navbar");
        patientAlerts.setId("navbar");

        //
        //Searchbar Structure
        ChoiceBox<String> choiceBox = new ChoiceBox();
        TextField search = new TextField("Search Users");
        HBox searchContainer = new HBox(choiceBox, search);
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.getItems().addAll("User ID", "Full Name", "Email", "Role");
        choiceBox.setValue("User ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
            if (choiceBox.getValue().equals("User ID")) {
                flUsers.setPredicate(p -> new String(p.getUserID() + "").contains(newValue));//filter table by Appt ID
            }
            if (choiceBox.getValue().equals("Full Name")) {
                flUsers.setPredicate(p -> p.getFullName().toLowerCase().contains(newValue.toLowerCase()));//filter table by Patient Id
            }
            if (choiceBox.getValue().equals("Email")) {
                flUsers.setPredicate(p -> p.getEmail().toLowerCase().contains(newValue.toLowerCase()));//filter table by Full name
            }
            if (choiceBox.getValue().equals("Role")) {
                flUsers.setPredicate(p -> p.getRoleVal().toLowerCase().contains(newValue.toLowerCase()));//filter table by Date/Time
            }
            table.getItems().clear();
            table.getItems().addAll(flUsers);
        });
        buttonContainer.getChildren().add(searchContainer);

        //
        addUser.setOnAction(eh -> addUser());
    }

    private void addUser() {
        Stage x = new Stage();
        x.initOwner(this);
        x.setTitle("Add User");
        x.initModality(Modality.WINDOW_MODAL);
        BorderPane y = new BorderPane();
        TextField email = new TextField("Email");
        TextField name = new TextField("Full Name");
        TextField username = new TextField("username");
        TextField password = new TextField("password");
        ComboBox role = new ComboBox();
        role.setValue("Administrator");
        role.getItems().addAll("Administrator", "Referral Doctor", "Receptionist", "Technician", "Radiologist", "Biller");
        Button submit = new Button("Submit");
        submit.setId("complete");

        HBox c = new HBox(email, name);
        HBox c1 = new HBox(username, password);
        HBox c2 = new HBox(role);
        VBox center = new VBox(c, c1, c2, submit);

        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        y.setCenter(center);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));
        x.show();

        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                if (!InputValidation.validateName(name.getText())) {
                    return;
                }
                if (!InputValidation.validateEmail(email.getText())) {
                    return;
                }
                insertUserIntoDatabase(email.getText(), name.getText(), username.getText(), password.getText(), role.getValue().toString());
                usersPageView();
                x.close();

            }
        });
    }

    private void insertUserIntoDatabase(String email, String name, String username, String password, String role) {
        String sql = "INSERT INTO users(email, full_name, username, password, role) VALUES ('" + email + "','" + name + "','" + username + "','" + password + "', (SELECT roleID FROM roles WHERE role = '" + role + "'));";
        App.executeSQLStatement(sql);

    }

    private void updateUser(User z) {
        Stage x = new Stage();
        x.initOwner(this);
        x.setTitle("Update User");
        x.initModality(Modality.WINDOW_MODAL);
        BorderPane y = new BorderPane();

        Button updateUserEmail = new Button("Change User Email");
        Button updateUserPW = new Button("Change User Password");
        Button disableUser = new Button("Disable User");
        disableUser.setId("cancel");

        if (z.getEnabled() == 0) {
            disableUser.setText("Enable User");
            disableUser.setId("complete");
        }

        HBox buttonContainer = new HBox(updateUserEmail, updateUserPW, disableUser);
        buttonContainer.setSpacing(20);
        Button submit = new Button("Submit");
        //
        Label txt = new Label("Insert Value Here:");
        TextField input = new TextField("...");
        input.setPrefWidth(200);
        HBox hidden = new HBox(txt, input);
        hidden.setVisible(false);
        //
        VBox center = new VBox(buttonContainer, hidden, submit);
        center.setSpacing(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        y.setCenter(center);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));
        x.show();

        updateUserEmail.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {

                center.getChildren().remove(buttonContainer);
                hidden.setVisible(true);
                txt.setText("Email: ");
                input.setText("example@email.com");
                submit.setId("complete");
                submit.setOnAction(eh2 -> updateEmail());
            }

            private void updateEmail() {
                boolean allCool = true;
                allCool = InputValidation.validateEmail(input.getText());
                if (allCool) {
                    String sql = "UPDATE users SET email = '" + input.getText() + "' WHERE user_id = '" + z.getUserID() + "';";
                    App.executeSQLStatement(sql);
                    usersPageView();
                    x.close();
                }
            }
        });

        updateUserPW.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                center.getChildren().remove(buttonContainer);
                hidden.setVisible(true);
                txt.setText("Password: ");
                input.setText("Good passwords are long but easy to remember, try phrases with numbers and special chars mixed in.");
                submit.setId("complete");
                submit.setOnAction(eh2 -> updatePassword());
            }

            private void updatePassword() {
                String sql = "UPDATE users SET password = '" + input.getText() + "' WHERE user_id = '" + z.getUserID() + "';";
                App.executeSQLStatement(sql);
                usersPageView();
                x.close();
            }
        });

        disableUser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                center.getChildren().remove(buttonContainer);
                hidden.setVisible(true);
                txt.setText("Enter 'CONFIRM' to continue: ");
                input.setText("Are you sure?");

                submit.setId("cancel");
                if (z.getEnabled() == 0) {
                    submit.setId("complete");
                }
                submit.setOnAction(eh2 -> disableUser());
            }

            private void disableUser() {
                if (z.getUserID() != App.user.getUserID()) {
                    boolean allCool = true;
                    allCool = InputValidation.validateConfirm(input.getText());
                    if (allCool) {
                        int enabled = 0;
                        if (z.getEnabled() == 0) {
                            enabled = 1;
                        }
                        String sql = "UPDATE users SET enabled = '" + enabled + "' WHERE user_id = '" + z.getUserID() + "';";
                        App.executeSQLStatement(sql);
                        usersPageView();
                        x.close();
                    }
                } else {

                }
            }
        });
    }
//</editor-fold>
//
//<editor-fold defaultstate="collapsed" desc="Patients Section">

    private void patientsPageView() {
        patientsContainer.getChildren().clear();

        main.setCenter(patientsContainer);
        createTablePatients();
        populatePatientsTable();

        patientsContainer.getChildren().addAll(table);
        patientsContainer.setSpacing(10);
        users.setId("navbar");
        patients.setId("selected");
        appointments.setId("navbar");
        modalities.setId("navbar");
        patientAlerts.setId("navbar");

        //Searchbar Structure
        ChoiceBox<String> choiceBox = new ChoiceBox();
        TextField search = new TextField("Search Patients");
        HBox searchContainer = new HBox(choiceBox, search);
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.getItems().addAll("Patient ID", "Full Name", "Email", "Date of Birth", "Insurance");
        choiceBox.setValue("Patient ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
            if (choiceBox.getValue().equals("Patient ID")) {
                flPatient.setPredicate(p -> new String(p.getPatientID() + "").contains(newValue));//filter table by Appt ID
            } else if (choiceBox.getValue().equals("Full Name")) {
                flPatient.setPredicate(p -> p.getFullName().toLowerCase().contains(newValue.toLowerCase()));//filter table by Patient Id
            } else if (choiceBox.getValue().equals("Email")) {
                flPatient.setPredicate(p -> p.getEmail().toLowerCase().contains(newValue.toLowerCase()));//filter table by Full name
            } else if (choiceBox.getValue().equals("Date of Birth")) {
                flPatient.setPredicate(p -> p.getDob().contains(newValue));//filter table by Date/Time
            } else if (choiceBox.getValue().equals("Insurance")) {
                flPatient.setPredicate(p -> p.getInsurance().contains(newValue));//filter table by Date/Time
            }
            table.getItems().clear();
            table.getItems().addAll(flPatient);
        });
        patientsContainer.getChildren().add(searchContainer);

        //
//        addUser.setOnAction(eh -> addUser());
    }

    private void createTablePatients() {
        table.getColumns().clear();
        //All of the Columns
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn fullNameCol = new TableColumn("Full Name");
        TableColumn emailCol = new TableColumn("Email");
        TableColumn DOBCol = new TableColumn("Date of Birth");
        TableColumn insuranceCol = new TableColumn("Insurance");

        //And all of the Value setting
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        DOBCol.setCellValueFactory(new PropertyValueFactory<>("dob"));
        insuranceCol.setCellValueFactory(new PropertyValueFactory<>("insurance"));

        //Couldn't put the table
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        fullNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        emailCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        DOBCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        insuranceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));

        table.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        //back together again
        table.getColumns().addAll(patientIDCol, fullNameCol, emailCol, DOBCol, insuranceCol);
    }

    private void populatePatientsTable() {
        table.getItems().clear();
        //Connect to database
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select patients.patientID, patients.email, patients.full_name, patients.dob, patients.address, patients.insurance"
                + " FROM patients"
                + " "
                + " ;";

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

                    }

                });
            }

            flPatient = new FilteredList(FXCollections.observableList(list), p -> true);
            table.getItems().addAll(flPatient);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //</editor-fold>
//
//<editor-fold defaultstate="collapsed" desc="Appointments Section">
    private void appointmentsPageView() {
        appointmentsContainer.getChildren().clear();

        main.setCenter(appointmentsContainer);
        createTableAppointments();
        populateTableAppointments();

        appointmentsContainer.getChildren().addAll(table);
        appointmentsContainer.setSpacing(10);
        users.setId("navbar");
        patients.setId("navbar");
        appointments.setId("selected");
        modalities.setId("navbar");
        patientAlerts.setId("navbar");

        //Searchbar Structure
        ChoiceBox<String> choiceBox = new ChoiceBox();
        TextField search = new TextField("Search Appointments");
        HBox searchContainer = new HBox(choiceBox, search);
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

        appointmentsContainer.getChildren().addAll(searchContainer);
    }

    private void createTableAppointments() {
        table.getColumns().clear();
        //Vbox to hold the table
        //Allow Table to read Appointment class
        TableColumn apptIDCol = new TableColumn("Appointment ID");
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn firstNameCol = new TableColumn("Full Name");
        TableColumn timeCol = new TableColumn("Time of Appt.");
        TableColumn orderCol = new TableColumn("Orders Requested");
        TableColumn status = new TableColumn("Status");

        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptID"));
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        orderCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));

        //Set Column Widths
        apptIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.04));
        firstNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        orderCol.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
        status.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        //Add columns to table
        table.getColumns().addAll(apptIDCol, patientIDCol, firstNameCol, timeCol, orderCol, status);
    }

    private void populateTableAppointments() {
        table.getItems().clear();
        //Connect to database
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select appt_id, patient_id, patients.full_name, time, statusCode.status"
                + " FROM appointments"
                + " INNER JOIN statusCode ON appointments.statusCode = statusCode.statusID "
                + " INNER JOIN patients ON patients.patientID = appointments.patient_id"
                + " "
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

    //</editor-fold>
//
//<editor-fold defaultstate="collapsed" desc="Modalities Section">
    private void modalitiesPageView() {
        modalitiesContainer.getChildren().clear();

        main.setCenter(modalitiesContainer);
        createTableModalities();
        populateTableModalities();

        Button addModality = new Button("Add Modality");
        HBox btnContainer = new HBox(addModality);

        modalitiesContainer.getChildren().addAll(table, btnContainer);
        modalitiesContainer.setSpacing(10);
        users.setId("navbar");
        patients.setId("navbar");
        appointments.setId("navbar");
        modalities.setId("selected");
        patientAlerts.setId("navbar");

        addModality.setOnAction(eh -> addModality());
    }

    private void createTableModalities() {
        table.getColumns().clear();
        //All of the Columns
        TableColumn orderIDCol = new TableColumn("Order ID");
        TableColumn orderCol = new TableColumn("Order");
        TableColumn buttonCol = new TableColumn("Delete");

        //And all of the Value setting
        orderIDCol.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        orderCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        buttonCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));

        //Couldn't put all the styling
        orderIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        orderCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        buttonCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        //Together again
        table.getColumns().addAll(orderIDCol, orderCol, buttonCol);
        //Add Status Update Column:
    }

    private void populateTableModalities() {
        table.getItems().clear();
        //Connect to database
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select * "
                + " FROM orderCodes "
                + " "
                + " ;";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<Order> list = new ArrayList<Order>();
            while (rs.next()) {
                //What I receieve:  patientID, email, full_name, dob, address, insurance
                Order order = new Order(rs.getInt("orderID"), rs.getString("orders"));
                list.add(order);
            }

            for (Order z : list) {
                z.placeholder.setText("Delete");
                z.placeholder.setId("cancel");
                z.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        String sql = "DELETE FROM orderCodes WHERE orderID = '" + z.getOrderID() + "' ";
                        App.executeSQLStatement(sql);
                        populateTableModalities();
                    }
                });
            }

//            fl = new FilteredList(FXCollections.observableList(list), p -> true);
            table.getItems().addAll(list);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void addModality() {
        Stage x = new Stage();
        x.initOwner(this);
        x.setTitle("Add User");
        x.initModality(Modality.WINDOW_MODAL);
        BorderPane y = new BorderPane();
        Label txt = new Label("Enter the order name below. ");
        TextField order = new TextField("order");
        order.setPrefWidth(200);
        Button submit = new Button("Submit");
        submit.setId("complete");

        VBox center = new VBox(txt, order, submit);

        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        y.setCenter(center);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));
        x.show();

        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                String sql = "INSERT INTO orderCodes(orders) VALUES ('" + order.getText() + "') ;";
                App.executeSQLStatement(sql);
                populateTableModalities();
                x.close();
            }

        });
    }

    //</editor-fold>
//
    private void patientAlertsPageView() {
        patientAlertsContainer.getChildren().clear();

        main.setCenter(patientAlertsContainer);
        createTablePatientAlerts();
        populatePatientAlerts();

        Button addPatientAlert = new Button("Add Patient Alert");
        HBox btnContainer = new HBox(addPatientAlert);

        patientAlertsContainer.getChildren().addAll(table, btnContainer);
        patientAlertsContainer.setSpacing(10);
        users.setId("navbar");
        patients.setId("navbar");
        appointments.setId("navbar");
        modalities.setId("navbar");
        patientAlerts.setId("selected");
        addPatientAlert.setOnAction(eh -> addPatientAlert());
    }

    private void createTablePatientAlerts() {
        table.getColumns().clear();
        //All of the Columns
        TableColumn alertIDCol = new TableColumn("ID");
        TableColumn alertCol = new TableColumn("Alert");
        TableColumn flagsCol = new TableColumn("Flags");
        TableColumn button1Col = new TableColumn("Add Flag");
        TableColumn buttonCol = new TableColumn("Delete");

        //And all of the Value setting
        alertIDCol.setCellValueFactory(new PropertyValueFactory<>("alertID"));
        alertCol.setCellValueFactory(new PropertyValueFactory<>("alert"));
        flagsCol.setCellValueFactory(new PropertyValueFactory<>("flags"));
        button1Col.setCellValueFactory(new PropertyValueFactory<>("placeholder1"));
        buttonCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));

        //Couldn't put all the styling
        alertIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        alertCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        flagsCol.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        buttonCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        button1Col.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        //Together again
        table.getColumns().addAll(alertIDCol, alertCol, flagsCol, button1Col, buttonCol);
        //Add Status Update Column:
    }

    private void populatePatientAlerts() {
        table.getItems().clear();
        //Connect to database
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
            List<PatientAlert> list = new ArrayList<PatientAlert>();
            while (rs.next()) {
                //What I receieve:  patientID, email, full_name, dob, address, insurance
                PatientAlert pa = new PatientAlert(rs.getInt("alertID"), rs.getString("alert"), getFlagsFromDatabase(rs.getInt("alertID")));

                pa.placeholder.setText("Delete Alert");
                pa.placeholder.setId("cancel");
                pa.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        String sql = "DELETE FROM patientAlerts WHERE alertID = '" + pa.getAlertID() + "' ";
                        App.executeSQLStatement(sql);
                        sql = "DELETE FROM flags WHERE alertID = '" + pa.getAlertID() + "' ";
                        App.executeSQLStatement(sql);
                        sql = "DELETE FROM alertsPatientConnnector WHERE alertID = '" + pa.getAlertID() + "' ";
                        App.executeSQLStatement(sql);
                        populatePatientAlerts();
                    }
                });
                pa.placeholder1.setText("Add Flag");
                pa.placeholder1.setId("complete");
                pa.placeholder1.setOnAction(eh -> addFlag(pa));

                list.add(pa);
            }

//            fl = new FilteredList(FXCollections.observableList(list), p -> true);
            table.getItems().addAll(list);
            //
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void addPatientAlert() {
        Stage x = new Stage();
        x.initOwner(this);
        x.setTitle("Add Patient Alert");
        x.initModality(Modality.WINDOW_MODAL);
        BorderPane y = new BorderPane();
        Label txt = new Label("Prompt: ");
        TextField alert = new TextField("Ex: Allergic to peanuts?");
        alert.setPrefWidth(200);
        Button submit = new Button("Submit");
        submit.setId("complete");

        VBox center = new VBox(txt, alert, submit);

        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        y.setCenter(center);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));
        x.show();

        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                String sql = "INSERT INTO patientAlerts(alert) VALUES ('" + alert.getText() + "') ;";
                App.executeSQLStatement(sql);
                populatePatientAlerts();
                x.close();
            }

        });
    }
//

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

    private void addFlag(PatientAlert pa) {
        ArrayList<String> modalities = new ArrayList<String>();
        Stage x = new Stage();
        x.initOwner(this);
        x.setTitle("Add Flag");

        x.initModality(Modality.WINDOW_MODAL);
        BorderPane y = new BorderPane();
        Label txt = new Label("Select: ");

        Button submit = new Button("Submit");
        submit.setId("complete");

        ComboBox orders = populateOrdersDropdown();
        orders.setPrefWidth(100);
        HBox buttonContainer = new HBox();
        buttonContainer.setSpacing(10);
        buttonContainer.setPadding(new Insets(10));
        VBox center = new VBox(txt, orders, buttonContainer, submit);

        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10));
        y.setCenter(center);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));
        x.setHeight(200);
        x.setWidth(300);
        x.show();

        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                for (String z : modalities) {
                    String sql = "INSERT INTO flags VALUES ('" + pa.getAlertID() + "', (SELECT orderID FROM orderCodes WHERE orders = '" + z + "') )";
                    App.executeSQLStatement(sql);
                }
                populatePatientAlerts();
                x.close();
            }

        });

        orders.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                modalities.add(orders.getValue().toString());
                Button temp = new Button(orders.getValue().toString());
                buttonContainer.getChildren().add(temp);
                temp.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        if (!orders.getValue().toString().isBlank()) {
                            modalities.remove(temp.getText());
                            buttonContainer.getChildren().remove(temp);
                        }
                    }
                });
            }
        });

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

}
