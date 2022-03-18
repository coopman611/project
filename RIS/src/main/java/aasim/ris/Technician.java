package aasim.ris;

import datastorage.Appointment;
import datastorage.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Technician extends Stage {
    //Navbar

    HBox navbar = new HBox();
    Label username = new Label("Logged In as: " + App.user.getFullName());
    Button logOut = new Button("Log Out");

    //End Navbar
    //table
    TableView appointmentsTable = new TableView();
    VBox tableContainer = new VBox(appointmentsTable);
    //
    //Scene
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);

    //End Scene
    private FilteredList<Appointment> flAppointment;
    ChoiceBox<String> choiceBox = new ChoiceBox();
    TextField search = new TextField("Search Appointments");
    HBox searchContainer = new HBox(choiceBox, search);

    private final FileChooser fileChooser = new FileChooser();

    public Technician() {
        this.setTitle("RIS - Radiology Information System (Technician)");
        //Navbar
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
        navbar.getChildren().addAll(username, logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);
        //End navbar

        //Center
        main.setCenter(tableContainer);
        createTableAppointments();
        populateTable();
        //End Center
        //Searchbar Structure
        tableContainer.getChildren().add(searchContainer);
        searchContainer.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        choiceBox.setPrefHeight(40);
        search.setPrefHeight(40);
        choiceBox.getItems().addAll("Patient ID", "Full Name", "Date/Time", "Order", "Status");
        choiceBox.setValue("Patient ID");
        search.textProperty().addListener((obs, oldValue, newValue) -> {
            if (choiceBox.getValue().equals("Patient ID")) {
                flAppointment.setPredicate(p -> new String(p.getPatientID() + "").contains(newValue));//filter table by Patient Id
            } else if (choiceBox.getValue().equals("Full Name")) {
                flAppointment.setPredicate(p -> p.getFullName().toLowerCase().contains(newValue.toLowerCase()));//filter table by Full name
            } else if (choiceBox.getValue().equals("Date/Time")) {
                flAppointment.setPredicate(p -> p.getTime().contains(newValue));//filter table by Date/Time
            } else if (choiceBox.getValue().equals("Order")) {
                flAppointment.setPredicate(p -> p.getOrder().toLowerCase().contains(newValue.toLowerCase()));//filter table by Date/Time
            } else if (choiceBox.getValue().equals("Status")) {
                flAppointment.setPredicate(p -> p.getStatus().toLowerCase().contains(newValue.toLowerCase()));//filter table by Status
            }
            appointmentsTable.getItems().clear();
            appointmentsTable.getItems().addAll(flAppointment);
        });
        //End Searchbar Structure

        //Set Scene and Structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
    }

    private void createTableAppointments() {
        //All of the Columns
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn fullNameCol = new TableColumn("Full Name");
        TableColumn timeCol = new TableColumn("Time");
        TableColumn orderIDCol = new TableColumn("Orders Requested");
        TableColumn statusCol = new TableColumn("Appointment Status");
        TableColumn updateStatusCol = new TableColumn("Open Appointment");

        //And all of the Value setting
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        orderIDCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        updateStatusCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));

        //Couldn't put all the styling
        patientIDCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.04));
        fullNameCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.06));
        timeCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.2));
        orderIDCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.3));
        appointmentsTable.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        //Together again
        appointmentsTable.getColumns().addAll(patientIDCol, fullNameCol, timeCol, orderIDCol, statusCol, updateStatusCol);
        //Add Status Update Column:
    }

    private void populateTable() {
        appointmentsTable.getItems().clear();
        //Connect to database
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select appt_id, patient_id, patients.full_name, time, statusCode.status"
                + " FROM appointments"
                + " INNER JOIN statusCode ON appointments.statusCode = statusCode.statusID"
                + " INNER JOIN patients ON appointments.patient_id = patients.patientID"
                + " WHERE statusCode < 4"
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
            for (Appointment z : list) {
                z.placeholder.setText("Check In");
                z.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        techPageTwo(z.getPatientID(), z.getApptID(), z.getFullName(), z.getOrder());
                    }
                });
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

    private void techPageOne() {
        populateTable();
        main.setCenter(tableContainer);
    }

    private void techPageTwo(int patID, int apptId, String fullname, String order) {
        VBox container = new VBox();
        container.setSpacing(10);
        container.setAlignment(Pos.CENTER);
        Label patInfo = new Label("Patient: " + fullname + "\t Order/s Requested: " + order + "\n");
        Label imgInfo = new Label("Images Uploaded: " + fullname + "\t Order/s Requested: " + order);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png")
        //                 new FileChooser.ExtensionFilter("HTML Files", "*.htm")
        );
        Button complete = new Button("Fulfill Order");
        complete.setId("complete");
        Button cancel = new Button("Go Back");
        cancel.setId("cancel");
        Button addImg = new Button("Upload Image");
        HBox buttonContainer = new HBox(cancel, addImg, complete);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(25);
        container.getChildren().addAll(patInfo, buttonContainer);
        main.setCenter(container);
        //Set Size of Every button in buttonContainer
        complete.setPrefSize(200, 100);
        cancel.setPrefSize(200, 100);
        addImg.setPrefSize(200, 100);
        //
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                populateTable();
                main.setCenter(tableContainer);
            }
        });

        addImg.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                openFile(patID, apptId);

            }
        });

        complete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                completeOrder(patID, apptId);
            }
        });

    }

    private void openFile(int patID, int apptId) {
        File file = fileChooser.showOpenDialog(this);
        if (file != null) {
            try {
                Image img = new Image(new FileInputStream(file));
                Stage x = new Stage();
                x.initOwner(this);
                x.initModality(Modality.WINDOW_MODAL);
                x.setMaximized(true);
                BorderPane y = new BorderPane();
                Label label = new Label("You are uploading the image: " + file.getName());
                Button confirm = new Button("Confirm");
                confirm.setId("complete");

                Button cancel = new Button("Cancel");
                cancel.setId("cancel");
                HBox btnContainer = new HBox(cancel, confirm);
                btnContainer.setSpacing(25);
                ScrollPane s1 = new ScrollPane(new ImageView(img));
                s1.setPrefHeight(1000);
                VBox container = new VBox(label, s1, btnContainer);
                y.setCenter(container);
                y.getStylesheets().add("file:stylesheet.css");
                x.setScene(new Scene(y));
                x.show();

                cancel.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        x.close();
                    }
                });
                confirm.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        addImgToDatabase(file, patID, apptId);
                        x.close();
                    }
                });
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Technician.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void addImgToDatabase(File file, int patID, int apptId) {
        try {
            FileInputStream temp = new FileInputStream(file);

            String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
            String sql = "INSERT INTO images (patientID, apptID, image) VALUES (?, ?, ?);";
            try {
                Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, patID);
                pstmt.setInt(2, apptId);
                pstmt.setBinaryStream(3, temp, (int) file.length());
                pstmt.executeUpdate();
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Technician.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void completeOrder(int patID, int apptId) {
        Stage x = new Stage();
        x.initOwner(this);
        x.setMaximized(true);
        x.initModality(Modality.WINDOW_MODAL);
        BorderPane y = new BorderPane();
        Label label = new Label("You are uploading the images: ");
        //Images
        VBox imgContainer = new VBox();
        ArrayList<Image> list = retrieveUploadedImages(patID, apptId);
        if (list.isEmpty()) {
            System.out.println("Error, image list is empty");
        } else {
            for (Image i : list) {
                ImageView temp = new ImageView(i);
                imgContainer.getChildren().add(temp);
            }
        }
        ScrollPane s1 = new ScrollPane();
        s1.setContent(imgContainer);
        //End Images
        Button confirm = new Button("Confirm");
        confirm.setId("complete");
        Button cancel = new Button("Cancel");
        cancel.setId("cancel");
        HBox btnContainer = new HBox(cancel, confirm);
        btnContainer.setSpacing(25);
        VBox container = new VBox(label, s1, btnContainer);
        y.setCenter(container);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));
        x.show();

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                x.close();
            }
        });

        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                updateAppointmentStatus(patID, apptId);
                x.close();
                techPageOne();
            }

        });
    }

    private ArrayList<Image> retrieveUploadedImages(int patID, int apptId) {
        //Connect to database
        ArrayList<Image> list = new ArrayList<Image>();

        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "SELECT *"
                + " FROM images"
                + " WHERE patientID = '" + patID + "' AND apptID = '" + apptId + "'"
                + " ORDER BY imageID DESC;";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            while (rs.next()) {
                //What I receieve:  image
                list.add(new Image(rs.getBinaryStream("image")));
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

    private void updateAppointmentStatus(int patID, int apptId) {
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "UPDATE appointments"
                + " SET statusCode = 4"
                + " WHERE appt_id = '" + apptId + "';";
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
}
