package aasim.ris;

import datastorage.Appointment;
import datastorage.Patient;
import datastorage.User;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.beans.binding.Bindings.format;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ReferralDoctor extends Stage {
    //Navbar

    HBox navbar = new HBox();
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
    private FilteredList flAppointment;
    private final FileChooser fileChooser = new FileChooser();

    public ReferralDoctor() {
        this.setTitle("RIS - Radiology Information System (Doctor View)");
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
        //Set Scene and Structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
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

            flAppointment = new FilteredList(FXCollections.observableList(list), p -> true);
            patientsTable.getItems().addAll(flAppointment);
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
        Label text2 = new Label("Date of Birth: ");
        DatePicker datePicker = new DatePicker();
        Label text3 = new Label("Address: ");
        TextField address = new TextField("");
        address.setPrefWidth(200);
        Label text4 = new Label("Insurance: ");
        TextField insurance = new TextField("");
        insurance.setPrefWidth(200);
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
        VBox center = new VBox(container, hiddenContainer1, hiddenContainer3, hiddenSubmit);

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
                boolean everythingCool = true;
                //validation

                //end validation
                if (everythingCool) {
                    Patient temp = checkDatabaseForPatient(name.getText(), email.getText());
                    //Temp != null (patient already exists)
                    if (temp != null) {
                        //name, dob, address, insurance
                        name.setText(temp.getFullName());
                        datePicker.setValue(LocalDate.parse(temp.getDob()));
                        address.setText(temp.getAddress());
                        insurance.setText(temp.getInsurance());
                    }

                    email.setEditable(false);
                    name.setEditable(false);
                    hiddenContainer1.setVisible(true);
                    hiddenContainer3.setVisible(true);
                    pullData.setVisible(false);
                    hiddenSubmit.setVisible(true);
                }
            }
        });

        hiddenSubmit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                insertPatientIntoDatabase(name.getText(), email.getText(), datePicker.getValue().toString(), address.getText(), insurance.getText());
                populateTable();
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
        App.executeSQLStatement(App.fileName, sql);

        int patientID = checkDatabaseForPatient(name, email).getPatientID();
        String sql1 = "INSERT INTO docPatientConnector "
                + " VALUES ('" + App.user.getUserID() + "', '" + patientID + "');";
        App.executeSQLStatement(App.fileName, sql1);

    }

    private void patientOverviewScreen(Patient z) {
        // Appointments table
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
        HBox overviewButtonContainer = new HBox(toTheLeft, newOrder, removeOrder, toTheRight);
        overviewButtonContainer.setSpacing(10);
        //End Buttons
        VBox overviewContainer = new VBox(patInfo, appointmentsTable, overviewButtonContainer);
        overviewContainer.setSpacing(10);
        main.setCenter(overviewContainer);

        //
        populateAppointmentsTable(z);
        //

        newOrder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                createNewOrder(z);
                text2.setText("Orders Requested: " + getPatOrders(z.getPatientID()));
            }

        });
        removeOrder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                removeOrder(z);
                text2.setText("Orders Requested: " + getPatOrders(z.getPatientID()));
            }

        });
        goBack.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                appointmentsTable.getColumns().clear();
                main.setCenter(tableContainer);
            }

        });
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                removePatientFromView(z);
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
                if (text.getText().equals("CONFIRM")) {
                    String sql = "DELETE FROM docPatientConnector WHERE patientID = '" + z.getPatientID() + "' AND referralDocID = '" + App.user.getUserID() + "';";
                    App.executeSQLStatement(App.fileName, sql);
                    x.close();
                    populateTable();
                    appointmentsTable.getColumns().clear();
                    main.setCenter(tableContainer);
                }
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

            flAppointment = new FilteredList(FXCollections.observableList(list), p -> true);
            appointmentsTable.getItems().addAll(flAppointment);
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
                insertNewOrder(z, dropdown.getValue().toString());
                x.close();
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
                removeOrder(z, dropdown.getValue().toString());
                x.close();
            }

        });
        x.showAndWait();
    }

    private void removeOrder(Patient z, String order) {
        String sql = "DELETE FROM patientOrders "
                + " WHERE patientID = '" + z.getPatientID() + "' AND orderCodeID = (SELECT orderCodes.orderID FROM orderCodes WHERE orderCodes.orders = '" + order + "')"
                + "     AND ROWID = (SELECT ROWID FROM patientOrders WHERE patientID = '" + z.getPatientID() + "' AND orderCodeID = (SELECT orderCodes.orderID FROM orderCodes WHERE orderCodes.orders = '" + order + "') LIMIT 1)"
                + " ;";
        App.executeSQLStatement(App.fileName, sql);
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
        App.executeSQLStatement(App.fileName, sql);
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

    private void downloadImage(Integer imgID, File directory) {

        FileOutputStream fos = null;
        try {
            String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
            String sql = "SELECT *"
                    + " FROM images"
                    + " WHERE imageID = '" + imgID + "';";
            File file = new File(directory.getAbsolutePath() + "/downloadedImage" + imgID + ".png");
            fos = new FileOutputStream(file);
            byte b[];
            Blob blob;

            try {
                Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                //
                while (rs.next()) {
                    //What I receieve:  image
                    blob = rs.getBlob("image");
                    b = blob.getBytes(1, (int) blob.length());
                    fos.write(b);
                }
                //
                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(ReferralDoctor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReferralDoctor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(ReferralDoctor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void updateStatus(int apptID) {
        String sql = "UPDATE appointments "
                + " SET statusCode = 6 "
                + " WHERE appt_id = '" + apptID + "';";
        App.executeSQLStatement(App.fileName, sql);
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
