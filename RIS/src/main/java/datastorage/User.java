/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datastorage;

/**
 *
 * @author 14048
 */
public class User {

    private int userID = -1;
    private String fullName = "";
    private int role = -1;

    public User(int userID, String fullName, int role) {
        setFullName(fullName);
        setRole(role);
        setUserID(userID);
    }

    public User() {
    }

    //Getters
    public String getFullName() {
        return fullName;
    }

    public int getRole() {
        return role;
    }

    public int getUserID() {
        return userID;
    }

    //Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

}
