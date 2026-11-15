package com.library.controller;
import com.library.Main;
import com.library.dao.AuthDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
public class LoginController {
 @FXML private TextField usernameField;
 @FXML private PasswordField passwordField;
 @FXML private Label messageLabel;
 @FXML private void login(){
  String u=usernameField.getText().trim(), p=passwordField.getText();
  if(u.isEmpty()||p.isEmpty()){messageLabel.setText("Please enter both username and password.");return;}
  try{
   if(new AuthDAO().login(u,p)) Main.showDashboard();
   else messageLabel.setText("Incorrect username or password.");
  }catch(Exception e){
   messageLabel.setText("Cannot connect to MySQL. Check db.properties and database setup.");
   e.printStackTrace();
  }
 }
 @FXML private void clear(){usernameField.clear();passwordField.clear();messageLabel.setText("");}
}
