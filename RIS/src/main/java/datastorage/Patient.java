/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datastorage;

import java.util.logging.Logger;

/**
 *
 * @author 14048
 */
public class Patient {

    int patientID;
    String fullName;
    String address;
    String insurance;
    String orderIDs;
    String referralDocs;

    public Patient(int patientID, String fullName, String address, String insurance, String orderIDs, String referralDocs) {
        this.patientID = patientID;
        this.fullName = fullName;
        this.address = address;
        this.insurance = insurance;
        this.orderIDs = orderIDs;
        this.referralDocs = referralDocs;
    }

    public Patient() {
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getOrderIDs() {
        return orderIDs;
    }

    public void setOrderIDs(String orderIDs) {
        this.orderIDs = orderIDs;
    }

    public String getReferralDocs() {
        return referralDocs;
    }

    public void setReferralDocs(String referralDocs) {
        this.referralDocs = referralDocs;
    }
}
