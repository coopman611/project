/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datastorage;

import java.io.InputStream;
import javafx.scene.control.Button;

/**
 *
 * @author 14048
 */
public class Appointment {

    int apptID, patientID;
    String time, status, order;

    String fullName;
    public Button placeholder = new Button("Placeholder");

    public Appointment(int apptID, int patientID, String time, String status, String order) {
        this.apptID = apptID;
        this.patientID = patientID;
        this.time = time;
        this.status = status;
        this.order = order;
    }

    public Button getPlaceholder() {
        return placeholder;
    }

    public int getApptID() {
        return apptID;
    }

    public void setApptID(int apptID) {
        this.apptID = apptID;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

}
