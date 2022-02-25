/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datastorage;

import java.io.InputStream;
import java.sql.Date;

/**
 *
 * @author 14048
 */
public class Appointment {

    int apptId, patientID;
    String fullname, time, address, insurance, referral, status, order;
    InputStream fulOrder;

    public Appointment() {
    }

    public Appointment(int apptId, int patientID, String fullname, String time, String address, String insurance, String referral, String status, String order) {
        this.apptId = apptId;
        this.patientID = patientID;
        this.fullname = fullname;
        this.time = time;
        this.address = address;
        this.insurance = insurance;
        this.referral = referral;
        this.status = status;
        this.order = order;
    }

    public Appointment(int apptId, int patientID, String fullname, String time, String status, String order, InputStream fulfilledOrder) {
        this.apptId = apptId;
        this.patientID = patientID;
        this.fullname = fullname;
        this.time = time;
        this.status = status;
        this.order = order;
        this.fulOrder = fulfilledOrder;

    }

    public InputStream getfulOrder() {
        return fulOrder;
    }

    public void setfulOrder() {
        this.fulOrder = fulOrder;
    }

    public int getApptId() {
        return apptId;
    }

    public void setApptId(int apptId) {
        this.apptId = apptId;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInsurance() {
        return insurance;
    }

    public void setInsurance(String insurance) {
        this.insurance = insurance;
    }

    public String getReferral() {
        return referral;
    }

    public void setReferral(String referral) {
        this.referral = referral;
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

}
