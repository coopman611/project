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
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Rad extends Stage{
	//navbar
	HBox navbar = new HBox();
	Button logOut = new Button("Log Out");
	
	//end navbar
	//table
	TableView appointmentsTable = new TableView();
	VBox tableContainer = new VBox (appointmentsTable);
	
	//scene
	BorderPane main = new BorderPane();
	Scene scene = new Scene (main);
	
	//end scene
	private FilteredList flAppointment;
	private final FileChooser fileChooser = new FileChooser();
	
	public Rad() {
		this.setTitle("RIS - Radiology Information System (Radiologist");
		//navbar
		navbar.setAlignment(Pos.TOP_RIGHT);
		logOut.setPrefHeight(30);
		logOut.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent e) {
				logOut();
			}
		});
		navbar.getChildren().add(logOut);
		navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
		main.setTop(navbar);
		//end navbar
		
		//center
		main.setCenter(tableContainer);
		createTableAppointments();
		populateTable();
		//end center
		//set scene and structure
		scene.getStylesheets().add("file:stylesheet.css");
		this.setScene(scene);
	}
	
	private void createTableAppointments() {
		//columns of table
		TableColumn patientIDCol=new TableColumn("Patient ID");
		TableColumn fullNameCol = new TableColumn ("Full Name");
		TableColumn timeCol = new TableColumn ("Time");
		TableColumn orderIDCol = new TableColumn("Orders Requested");
		TableColumn statusCol = new TableColumn ("Appoointment Status");
		TableColumn reportCol = new TableColumn("Radiologist Report");
		
		//all of the value settings
		patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
		fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullname"));
	    timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
	    orderIDCol.setCellValueFactory(new PropertyValueFactory<>("order"));
	    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
	    reportCol.setCellValueFactory(new PropertyValueFactory<>("updateAppt"));
	    
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
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select appt_id, patient_id, full_name, time, address, insurance, referral_doc_id, statusCode.status, patient_order"
                + " FROM appointments"
                + " INNER JOIN statusCode ON appointments.statusCode = statusCode.statusID"
                + " WHERE statusCode != 4"
                + " ORDER BY time DESC;";
        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //
            List<Appointment> list = new ArrayList<Appointment>();

            while (rs.next()) {
                //What I receieve:  apptId, patientID, fullname, time, address, insurance, referral, status, order
                Appointment appt = new Appointment(rs.getInt("appt_id"), rs.getInt("patient_id"), rs.getString("full_name"), rs.getString("time"), rs.getString("address"), rs.getString("insurance"), rs.getString("referral_doc_id"), rs.getString("status"), rs.getString("patient_order"));
                list.add(appt);
            }
            for (Appointment z : list) {
                z.updateAppt.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        radPageTwo(z.getPatientID(), z.getApptId(), z.getFullname(), z.getOrder(), z.getReferral(), z.getStatus(), sql);
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
	
    private void logOut() {
        App.user = new User();
        Stage x = new Login();
        x.show();
        x.setMaximized(true);
        this.close();
    }
    
    private void radOne() {
        populateTable();
        main.setCenter(tableContainer);
    }
 
    private void radPageTwo(int patID, int apptId, String fullname, String order, String referral, String status, String report) {
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
        Button addReport = new Button("Upload Report");
        HBox buttonContainer = new HBox(cancel, addReport, complete);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(25);
        container.getChildren().addAll(patInfo, buttonContainer);
        main.setCenter(container);
        //Set Size of Every button in buttonContainer
        complete.setPrefSize(200, 100);
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
                openFile(report, patID, apptId,order, referral);

            }
        });

        complete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                completeOrder(patID, apptId);
            }
        });

    }
    
 
    private void openFile(String report,int patID, int apptId, String order, String referral) {
            
            
                Stage x = new Stage();
                x.initOwner(this);
                x.initModality(Modality.WINDOW_MODAL);
                x.setMaximized(true);
                BorderPane y = new BorderPane();
                Label label = new Label ("Report");
                Button confirm = new Button("Confirm");
                confirm.setId("complete");
                
                TextArea reportText = new TextArea();
                reportText.getText();

                Button cancel = new Button("Cancel");
                cancel.setId("cancel");
                HBox btnContainer = new HBox(cancel, reportText, confirm);
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
                        addReportToDatabase( report, apptId, patID, order, referral);
                        x.close();
                    }
                });
                
                VBox container = new VBox(label, btnContainer);
                y.setCenter(container);
                x.show();
    }
    
    private void addReportToDatabase(String report,int patID, int apptId, String order, String referral) {
    	String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
		 String sql = "INSERT INTO report (apptID, orderID, referralDocId, writtenreport) VALUES (?, ?, ?, ?);";
		 try {
		     Connection conn = DriverManager.getConnection(url);
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     pstmt.setInt(1, apptId);
		     pstmt.setString(2, order);
		     pstmt.setString(3, referral);
		     pstmt.setString(4, report);
		     pstmt.executeUpdate();
		     pstmt.close();
		     conn.close();
		 } catch (SQLException e) {
		     System.out.println(e.getMessage());
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
                radOne();
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









