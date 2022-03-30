package aasim.ris;

/**
 *
 * @author 14048
 */
import datastorage.User;
import datastorage.Appointment;
import datastorage.InputValidation;
import datastorage.Order;
import datastorage.Patient;
import datastorage.Payment;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
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
import javafx.scene.control.ScrollPane;
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

public class Billing extends Stage {

    //<editor-fold>
    //Stage Structure
    HBox navbar = new HBox();
    Button logOut = new Button("Log Out");
    BorderPane main = new BorderPane();
    Scene scene = new Scene(main);
    //Table Structure
    TableView table = new TableView();
   
    //Search Bar
    FilteredList<Appointment> flAppointment;
    ChoiceBox<String> choiceBox = new ChoiceBox();
    TextField search = new TextField("Search Appointments");

    //Buttons
    Button addBilling = new Button("Add Bill");
    Button refreshTable = new Button("Refresh Appointments");
    //Containers
    HBox searchContainer = new HBox(choiceBox, search);
    HBox buttonContainer = new HBox(addBilling, refreshTable, searchContainer);
    VBox tableContainer = new VBox(table, buttonContainer);
    
//</editor-fold>
    ArrayList<Order> varName = new ArrayList<>();
    //Populate the stage

    Billing() {
        this.setTitle("RIS- Radiology Information System (Billing)");
        //Navbar
        navbar.setAlignment(Pos.TOP_RIGHT);
        logOut.setPrefHeight(30);
        navbar.getChildren().add(logOut);
        navbar.setStyle("-fx-background-color: #2f4f4f; -fx-spacing: 15;");
        main.setTop(navbar);
        //End navbar

        //Putting center code here as to not clutter stuff
        loadCenter();
        varName = populateOrders();
        //Buttons
        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logOut();
            }
        });
//        addAppointment.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent e) {
//                addAppointment();
//            }
//        });
        addBilling.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                addBilling();
            }
        });
        refreshTable.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                populateTable();
            }
        });
//         check.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent e) {
//                billWindow();
//            }
//        });

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
        table.getColumns().clear();
        //Vbox to hold the table
        tableContainer.setAlignment(Pos.TOP_CENTER);
        tableContainer.setPadding(new Insets(20, 10, 10, 10));
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.setSpacing(10);
         TableColumn apptIDCol = new TableColumn("Appointment ID");
    TableColumn patientIDCol = new TableColumn("Patient ID");
    TableColumn firstNameCol = new TableColumn("Full Name");
    TableColumn timeCol = new TableColumn("Time of Appt.");
    TableColumn orderCol = new TableColumn("Orders Requested");
    TableColumn status = new TableColumn("Status");
    TableColumn updateAppt = new TableColumn("Update Billing");
    TableColumn totalCost = new TableColumn("Total Cost:");
    TableColumn makePayment = new TableColumn("Make Payment:");
        //Allow Table to read Appointment class
        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptID"));
        patientIDCol.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        orderCol.setCellValueFactory(new PropertyValueFactory<>("order"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        updateAppt.setCellValueFactory(new PropertyValueFactory<>("placeholder"));
        totalCost.setCellValueFactory(new PropertyValueFactory<>("total"));

        makePayment.setCellValueFactory(new PropertyValueFactory<>("button"));
        //Set Column Widths
        apptIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        patientIDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.04));
        firstNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.06));
        orderCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        updateAppt.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        status.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        totalCost.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        
        //Add columns to table
        table.getColumns().addAll(apptIDCol, patientIDCol, firstNameCol, timeCol, orderCol,totalCost, status, updateAppt,makePayment);
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
                appt.setTotal(calculateTotalCost(appt));
                appt.button.setOnAction(eh -> makePayment(appt) );
                list.add(appt);
            }

            for (Appointment x : list) {
                x.placeholder.setText("View Bill");
                x.placeholder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        viewBill(x);
                        
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
//    private void addAppointment() {
//        Stage x = new AddAppointment();
//        x.setTitle("Add Appointment");
//        x.initOwner(this);
//        x.initModality(Modality.WINDOW_MODAL);
//        x.showAndWait();
//        populateTable();
//    }
    //On button press, open up a new stage (calls private nested class)

    private void addBilling() {
        Stage x = new AddBilling();
        x.setTitle("Add Billing");
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        x.showAndWait();
        populateTable();
    }

    private float calculateTotalCost(Appointment appt) 
            {
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "Select orderCodes.cost "
                + " FROM appointmentsOrdersConnector "
                + " INNER JOIN orderCodes ON appointmentsOrdersConnector.orderCodeID = orderCodes.orderID "
                + " WHERE apptID = '" + appt.getApptID() + "';";

        float value = 0;
        try{
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {

                value += rs.getFloat("cost");
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
    
        
//        float value = -1;
//        return value;
      private void viewBill(Appointment appt) {
           Stage x = new Stage();
        x.setTitle("View Bill");
        x.setMaximized(true);
//        x.initOwner(this);
//        x.initModality(Modality.WINDOW_MODAL);
//        x.show();
       BorderPane bp = new BorderPane();
       Scene sc = new Scene(bp);
       x.setScene(sc);
       sc.getStylesheets().add("file:stylesheet.css");
// code goes here
//header
HBox header = new HBox();
Label patientName = new Label(appt.getFullName());
Label patientEmail = new Label(getEmail(appt.getPatientID()));
Label patientAddress = new Label(getAddress(appt.getPatientID()));
Label patientInsurance = new Label(getInsurance(appt.getPatientID()));
header.getChildren().addAll(patientName,patientEmail,patientAddress,patientInsurance );
bp.setTop(header);
//end header
//center
float paybox=0;
VBox center = new VBox();
ScrollPane sp = new ScrollPane(center);
String order[]=appt.getOrder().split(",");
for(int i=0; i < order.length-1;i++){
    Label tempOrder = new Label(order[i].trim());
    Label tempCost = new Label("Hello");
    for( Order a : varName){
      if(a.getOrder().equals(order[i].trim()) ){
          tempCost.setText(a.getCost()+"" ); 
          paybox+=a.getCost();
        }   
    }
    HBox he = new HBox(tempOrder, tempCost);

    center.getChildren().add(he);
}
ArrayList <Payment> payment = populatePayment(appt.getApptID());
for (Payment p:payment){
    Label tempPayment = new Label(p.getTime()+" "+ p.getPayment());
     center.getChildren().add(tempPayment);
     paybox-=p.getPayment();
}
bp.setCenter(sp);
//end center
//footer
HBox footer = new HBox();
Label blank = new Label("Total Bill Remaining: ");
Label tc = new Label(""+paybox);
footer.getChildren().addAll(blank,tc);
bp.setBottom(footer);
//end footer
x.show();
    }  

    private String getAddress(int patientID)  {
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "SELECT address FROM patients WHERE patientID = '"+patientID+"';";


        String value = "";
        try{
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {

                value += rs.getString("address");
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

    private String getEmail(int patientID) {
        
       
     String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "SELECT email FROM patients WHERE patientID = '"+patientID+"';";


        String value = "";
        try{
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {

                value += rs.getString("email");
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

    private String getInsurance(int patientID) {
         String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "SELECT insurance FROM patients WHERE patientID = '"+patientID+"';";


        String value = "";
        try{
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {

                value += rs.getString("insurance");
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

    private ArrayList<Order> populateOrders() {
        String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "SELECT * FROM orderCodes;";



        ArrayList<Order> value = new ArrayList <>();
        try{
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {
            Order order = new Order ( rs.getInt("orderID"),rs.getString("orders"));
            order.setCost(rs.getFloat("cost"));
               // value += rs.getS("insurance");
               value.add(order);
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

    private ArrayList<Payment> populatePayment(int apptID) {
        
            String url = "jdbc:sqlite:C://sqlite/" + App.fileName;
        String sql = "SELECT * FROM patientPayments WHERE apptID ='"+apptID+"';";



        ArrayList<Payment> value = new ArrayList <>();
        try{
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //

            while (rs.next()) {
            Payment payment = new Payment ( rs.getInt("apptID"),rs.getString("time"),rs.getFloat("patientPayment"));
            
               // value += rs.getS("insurance");
               value.add(payment);
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

    private void makePayment(Appointment appt) {
        Stage x = new Stage();
        VBox container = new VBox();
        Scene scene = new Scene(container);
        scene.getStylesheets().add("file:stylesheet.css");
        x.setScene(scene);

HBox hello = new HBox();
Label enterpay = new Label ("Enter Payment Here");
TextField ep = new TextField ( );
ComboBox dropdown = new ComboBox();
dropdown.getItems().addAll("Patient", "Insurance");
dropdown.setValue("Patient");
Button b = new Button("Submit");
hello.getChildren().addAll(enterpay, ep, dropdown,b);
container.getChildren().addAll(hello);
x.show();
b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                if (!InputValidation.validatePayment(ep.getText())){
                    
                return;
                
                }
                String sql = "";
                if (dropdown.getValue().toString().equals("Patient")) {
                  sql = "INSERT INTO patientPayments(apptID, time, patientPayment, byPatient) VALUES ('"+appt.getApptID()+"', '"+LocalDate.now() +"' , '"+ep.getText()+"', '1' )";  
                }else{
                   sql = "INSERT INTO patientPayments(apptID, time, patientPayment, byPatient) VALUES ('"+appt.getApptID()+"', '"+LocalDate.now() +"' , '"+ep.getText()+"', '0' )"; 
                }
            App.executeSQLStatement(sql); 
            x.close();
                //sql = "INSERT INTO patientPayments(apptID, time, patientPayment, byPatient) VALUES ('"+appt.getApptID()+"', '"+LocalDate.now() +"' , '"+ep.getText()+"', '1' )";
            }
        });
    }
    
    
      
    


    /* 
    // Private Nested Classes Below.
    //
    //
    //
     */
    //Private Nested Class 1
    //For the Add Appointment
    public class AddBilling extends Stage {

        Patient pat = null;
        ArrayList<String> orders = new ArrayList<String>();
        DatePicker datePicker = new DatePicker();
        
        public void billWindow(){
        Stage x = new Stage();
        x.setTitle("Add Appointment");
        x.initOwner(this);
        x.initModality(Modality.WINDOW_MODAL);
        x.show();
        BorderPane z = new BorderPane();
        Scene y = new Scene(z);
        y.getStylesheets().add("file:stylesheet.css");
        x.setScene(y);
        VBox container = new VBox();
        z.setCenter(container);
        //Your code goes below


        //
        container.getChildren().addAll( /*Everything you create, you add here*/);        
}

        //Class Variables
        AddBilling() {
            TextField patFullName = new TextField("Full Name");
            TextField patEmail = new TextField("Email");
            TextField patInsurance = new TextField("Insurance");
            TextField patOrder = new TextField("Order");
            Button check = new Button("Gather Information");
            check.setOnAction(e -> billWindow() );
            
           //check.setOnAction( billWindow() );
//            billWindow.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent e) {
//                billWindow();
//            }
//        });
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
            HBox initialContainer = new HBox(patFullName, patEmail, patInsurance, patOrder, check);
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

           
//            public void billWindow(){
//        Stage x = new Stage();
//        x.setTitle("Add Appointment");
//        x.initOwner(this);
//        x.initModality(Modality.WINDOW_MODAL);
//        x.showAndWait();
//        BorderPane z = new BorderPane();
//        Scene y = new Scene(z);
//        y.getStylesheets().add("file:stylesheet.css");
//        x.setScene(y);
//        VBox container = new VBox();
//        z.setCenter(container);
//        //Your code goes below
//
//
//        //
//        container.getChildren().addAll( /*Everything you create, you add here*/);        
//}
    
            

//                        dropdown.setOnAction(new EventHandler<ActionEvent>() {
//                            @Override
//                            public void handle(ActionEvent t) {
//                                orders.add(dropdown.getValue().toString());
//                                Button temp = new Button(dropdown.getValue().toString());
//                                hiddenOrderContainer.getChildren().add(temp);
//                                temp.setOnAction(new EventHandler<ActionEvent>() {
//                                    @Override
//                                    public void handle(ActionEvent t) {
//                                        if (!dropdown.getValue().toString().isBlank()) {
//                                            orders.remove(temp.getText());
//                                            hiddenOrderContainer.getChildren().remove(temp);
//                                        }
//                                    }
//                                });
//                            }
//                        });
//
//                    }
//                }

          //  });

// MIGHT USE EVENT LISTENER BELOW

//            submit.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent e) {
//                    boolean everythingCool = true;
//                    if (everythingCool) {
//                        //insertAppointment(pat.getPatientID(), orders, datePicker.getValue().toString() + " " + time.getText());
//
//                    }
//                }
//            });

        } //addBilling()

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

        private Patient pullPatientInfo(String patFullName, String patEmail, String patInsurance, String patOrder) {
            Patient temp = null;
            String sql = "Select * "
           + " FROM patients"
           + " WHERE email = '" + patEmail + "' AND full_name = '" + patFullName + "' AND patInsurance = '" + patInsurance + "' AND patOrder = '" + patOrder + "';";
           
String url = "jdbc:sqlite:C://sqlite/" + App.fileName;

//            String sql = "Select * "
//                    + " FROM patients"
//                    + " WHERE email = '" + patEmail + "' AND full_name = '" + patFullName + "';";
//            String url = "jdbc:sqlite:C://sqlite/" + App.fileName;

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
    } // public class addBilling
}

