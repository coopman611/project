package aasim.ris;

import datastorage.Appointment;
import datastorage.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

public class Rad extends Stage {
    //navbar

    HBox navbar = new HBox();
    Label username = new Label("Logged In as: " + App.user.getFullName());
    ImageView pfp = new ImageView(App.user.getPfp());

    Button logOut = new Button("Log Out");

    //end navbar
    //table
    TableView appointmentsTable = new TableView();
    VBox tableContainer = new VBox(appointmentsTable);

    //scene
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);

    //end scene
    private FilteredList<Appointment> flAppointment;
    ChoiceBox<String> choiceBox = new ChoiceBox();
    TextField search = new TextField("Search Appointments");
    HBox searchContainer = new HBox(choiceBox, search);

    private final FileChooser fileChooser = new FileChooser();

    public Rad() {
        this.setTitle("RIS - Radiology Information System (Radiologist)");
        //navbar
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
        navbar.getChildren().addAll(username, pfp, logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);
        //end navbar

        //center
        main.setCenter(tableContainer);
        createTableAppointments();
        populateTable();
        //end center

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

        //set scene and structure
        scene.getStylesheets().add("file:stylesheet.css");
        this.setScene(scene);
    }

    private void createTableAppointments() {
        //columns of table
        TableColumn patientIDCol = new TableColumn("Patient ID");
        TableColumn fullNameCol = new TableColumn("Full Name");
        TableColumn timeCol = new TableColumn("Time");
        TableColumn orderIDCol = new TableColumn("Orders Requested");
        TableColumn statusCol = new TableColumn("Appointment Status");
        TableColumn reportCol = new TableColumn("Radiologist Report");

        //all of the value settings
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        orderIDCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        reportCol.setCellValueFactory(new PropertyValueFactory<>("placeholder"));

        //Couldn't put all the styling
        patientIDCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.04));
        fullNameCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.06));
        timeCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.2));
        orderIDCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.3));
        reportCol.prefWidthProperty().bind(appointmentsTable.widthProperty().multiply(0.3));
        appointmentsTable.setStyle("-fx-background-color: #25A18E; -fx-text-fill: WHITE; ");
        //Together again
        appointmentsTable.getColumns().addAll(patientIDCol, fullNameCol, timeCol, orderIDCol, statusCol, reportCol);
        //Add Status Update Column:
    }

    private void populateTable() {
        appointmentsTable.getItems().clear();
        //connects to database

        String sql = "Select appt_id, patient_id, patients.full_name, time, statusCode.status"
                + " FROM appointments"
                + " INNER JOIN statusCode ON appointments.statusCode = statusCode.statusID"
                + " INNER JOIN patients ON appointments.patient_id = patients.patientID"
                + " WHERE statusCode = 4"
                + " ORDER BY time ASC;";
        try {
            Connection conn = DriverManager.getConnection(App.url);
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
                z.placeholder.setText("Create Report");
                z.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        radPageTwo(z.getPatientID(), z.getApptID(), z.getFullName(), z.getOrder());
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

        String sql = "Select orderCodes.orders "
                + " FROM appointmentsOrdersConnector "
                + " INNER JOIN orderCodes ON appointmentsOrdersConnector.orderCodeID = orderCodes.orderID "
                + " WHERE apptID = '" + aInt + "';";

        String value = "";
        try {
            Connection conn = DriverManager.getConnection(App.url);
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

    private void radOne() {
        populateTable();
        main.setCenter(tableContainer);
    }

    private void radPageTwo(int patID, int apptId, String fullname, String order) {
        VBox container = new VBox();
        container.setSpacing(10);
        container.setAlignment(Pos.CENTER);
        Label patInfo = new Label("Patient: " + fullname + "\t Order/s Requested: " + order + "\n");
        Label imgInfo = new Label("Images Uploaded: " + fullname + "\t Order/s Requested: " + order);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png")
        //                 new FileChooser.ExtensionFilter("HTML Files", "*.htm")
        );
//        Button complete = new Button("Fulfill Order");
//        complete.setId("complete");
        Button cancel = new Button("Go Back");
        cancel.setId("cancel");
        Button addReport = new Button("Upload Report");
        HBox buttonContainer = new HBox(cancel, addReport);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(25);
        container.getChildren().addAll(patInfo, buttonContainer);
        main.setCenter(container);
        //Set Size of Every button in buttonContainer
        cancel.setPrefSize(200, 100);
        addReport.setPrefSize(200, 100);
        //
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                populateTable();
                main.setCenter(tableContainer);
            }
        });

        addReport.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                openFile(patID, apptId);

            }
        });

    }

    private void openFile(int patID, int apptId) {

        Stage x = new Stage();
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        x.setMaximized(true);
        BorderPane y = new BorderPane();
        Label label = new Label("Report");
        Button confirm = new Button("Confirm");
        confirm.setId("complete");
        // Button viewImg = new Button("View Image");
        //viewImg.setId("View Image");

        VBox imgContainer = new VBox();
        ArrayList<Pair> list = retrieveUploadedImages(apptId);
        ArrayList<HBox> hbox = new ArrayList<HBox>();
        int counter = 0;
        int hboxCounter = 0;
        if (list.isEmpty()) {
            System.out.println("Error, image list is empty");
        } else {
            for (int i = 0; i < (list.size() / 2) + 1; i++) {
                hbox.add(new HBox());
            }
            for (Pair i : list) {
                if (counter > 2) {
                    counter++;
                    hboxCounter++;
                }
                ImageView temp = new ImageView(i.getImg());
                temp.setPreserveRatio(true);
                temp.setFitHeight(300);
                Button download = new Button("Download");
                VBox tempBox = new VBox(temp, download);
                tempBox.setId("borderOnHover");
                tempBox.setSpacing(5);
                tempBox.setAlignment(Pos.CENTER);
                tempBox.setPadding(new Insets(10));
                hbox.get(hboxCounter).getChildren().addAll(tempBox);
                download.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        File selectedDirectory = directoryChooser.showDialog(x);
                        downloadImage(i, selectedDirectory);
                    }

                });
                counter++;
            }
        }
        for (HBox i : hbox) {
            imgContainer.getChildren().add(i);
        }
        imgContainer.setSpacing(10);
        imgContainer.setPadding(new Insets(10));
        ScrollPane s1 = new ScrollPane();
        s1.setContent(imgContainer);

        TextArea reportText = new TextArea();
        reportText.getText();

        Button cancel = new Button("Cancel");
        cancel.setId("cancel");
        HBox btnContainer = new HBox(cancel, confirm, s1);
        btnContainer.setSpacing(25);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(new Scene(y));

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                x.close();
            }
        });
        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                addReportToDatabase(reportText.getText(), apptId);
                updateAppointmentStatus(patID, apptId);
                x.close();
                populateTable();
                main.setCenter(tableContainer);
            }
        });

        VBox container = new VBox(s1, label, reportText, btnContainer);
        y.setCenter(container);
        x.show();
    }

    private void addReportToDatabase(String report, int apptId) {
        String sql = "INSERT INTO report (apptID, writtenreport) VALUES ('" + apptId + "', '" + report + "');";
        App.executeSQLStatement(sql);
    }

    private ArrayList<Pair> retrieveUploadedImages(int apptId) {
        //Connect to database
        ArrayList<Pair> list = new ArrayList<Pair>();


        String sql = "SELECT *"
                + " FROM images"
                + " WHERE apptID = '" + apptId + "'"
                + " ORDER BY imageID DESC;";

        try {
            Connection conn = DriverManager.getConnection(App.url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            while (rs.next()) {
                //What I receieve:  image
                Pair pair = new Pair(new Image(rs.getBinaryStream("image")), rs.getInt("imageID"));
                pair.fis = rs.getBinaryStream("image");
                list.add(pair);
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

        String sql = "UPDATE appointments"
                + " SET statusCode = 5"
                + " WHERE appt_id = '" + apptId + "';";
        try {
            Connection conn = DriverManager.getConnection(App.url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void downloadImage(Pair img, File selectedDirectory) {
        try {
            String mimeType = URLConnection.guessContentTypeFromStream(img.fis);
            System.out.print(mimeType);
            mimeType = mimeType.replace("image/", "");
            File outputFile = new File(selectedDirectory.getPath() + "/" + img.imgID + "." + mimeType);
            FileUtils.copyInputStreamToFile(img.fis, outputFile);
        } catch (IOException ex) {
            Logger.getLogger(ReferralDoctor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private class Pair {

        Image img;
        Integer imgID;
        InputStream fis;

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
