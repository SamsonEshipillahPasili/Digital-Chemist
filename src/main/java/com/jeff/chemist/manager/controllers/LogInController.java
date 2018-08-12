/*
 * Controller class for to log user in
 */

package com.jeff.chemist.manager.controllers;

import animatefx.animation.FadeIn;
import com.jeff.chemist.manager.ChemistManagerApplication;
import com.jeff.chemist.manager.repositories.ThemeImageRepository;
import com.jeff.chemist.manager.services.BackgroundImageObserver;
import com.jeff.chemist.manager.services.LogInService;
import com.jeff.chemist.manager.services.ThemeImageService;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class LogInController implements Initializable, BackgroundImageObserver{
    @Autowired
    private LogInService logInService;
    @FXML
    private JFXTextField userNameField;
    @FXML
    private JFXPasswordField passwordField;
    @FXML
    private JFXSpinner loginSpinner;
    @FXML
    private Label invalidCredentialsLabel;
    @FXML
    private ImageView logInImageView;
    @Autowired
    private ThemeImageService themeImageService;
    @Autowired
    private ThemeImageRepository themeImageRepository;

    public void logIn() throws Exception {
        // show the spinner
        loginSpinner.setVisible(true);

        String userName = userNameField.getText();
        int password = passwordField.getText().hashCode();

        if (logInService.isValidUser(userName, password)) {
            // hide log in spinner
            loginSpinner.setVisible(false);
            // hide the invalidCredentialsLabel
            invalidCredentialsLabel.setVisible(false);

            // get the ChemistManagerApplication class, the stage is initialized by now.
            ChemistManagerApplication chemistManagerApplication = ChemistManagerApplication.getInstance();
            // get the primary stage and hide it.
            Stage primaryStage = chemistManagerApplication.getPrimaryStage();
            primaryStage.hide();

            // show the dashboard by re-using the old, previous stage
            FXMLLoader loader = chemistManagerApplication.getFxmlLoader("fxml/dashboard.fxml");
            Parent dashboard = loader.load();
            primaryStage.setScene(new Scene(dashboard));
            primaryStage.show();
        } else {
            // hide log in spinner
            loginSpinner.setVisible(false);

            // display error message
            invalidCredentialsLabel.setVisible(true);
            new FadeIn(invalidCredentialsLabel).play();


        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // set the background image.
        this.setBackgroundImage();
        // subscribe to get background image updates
        themeImageService.getBackgroundImageObservers().add(this);
    }

    // set the background image
    private  void setBackgroundImage(){
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            if(null != imageData && imageData.length != 0){
                this.logInImageView.setImage(new Image(new ByteArrayInputStream(imageData)));
            }
        });
    }

    @Override
    public void update() {
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            this.logInImageView.setImage(new Image(new ByteArrayInputStream(imageData)));
        });
    }
}
