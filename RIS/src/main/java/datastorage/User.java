/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datastorage;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author 14048
 */
public class User {

    private int userID;
    private String fullName, email, username;
    private int role;
    private int enabled;
    public Button placeholder = new Button("placeholder");
    private String roleVal;
    private Image pfp;

    public Image getPfp() {
        return pfp;
    }

    public ImageView getPfpView() {
        return new ImageView(pfp);
    }

    public void setPfp(Image pfp) {
        this.pfp = pfp;
    }

    public int getEnabled() {
        return enabled;
    }

    public String getRoleVal() {
        return roleVal;
    }

    public User() {
    }

    public User(int userID, String email, String fullName, String username, int role, int enabled, String roleVal) {
        this.userID = userID;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.role = role;
        this.enabled = enabled;
        this.roleVal = roleVal;
    }

    public User(int userID, String fullName, int role) {
        this.userID = userID;
        this.fullName = fullName;
        this.role = role;
        this.enabled = 1;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int isEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public Button getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(Button placeholder) {
        this.placeholder = placeholder;
    }

}
