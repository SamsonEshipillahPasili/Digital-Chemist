package com.jeff.chemist.manager.controllers;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeInDownBig;
import com.jeff.chemist.manager.ChemistManagerApplication;
import com.jeff.chemist.manager.entities.DigitalChemistUser;
import com.jeff.chemist.manager.repositories.ThemeImageRepository;
import com.jeff.chemist.manager.repositories.UserRepository;
import com.jeff.chemist.manager.services.BackgroundImageObserver;
import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

/*
  * Controller for the dashboard.
 */
@Controller
public class DashboardController implements Initializable, BackgroundImageObserver{
    @Autowired
    private UserRepository userRepository;


    @FXML
    private Pane pointOfSalePane, homePane, settingsPane, inventoryControlPane, reportsPane;

    @FXML
    private JFXButton homeBtn, pointOfSaleBtn, inventoryControlBtn, reportsBtn, settingsBtn, logOutBtn;

    @FXML
    private Label loggedInAsLabel, welcomeText;

    @FXML
    private ImageView backgroundImage;

    @Autowired
    private ThemeImageRepository themeImageRepository;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // apply the action listener to all the buttons.

        Stream.of(homeBtn, pointOfSaleBtn, inventoryControlBtn, reportsBtn, settingsBtn, logOutBtn).forEach(
                button -> button.setOnAction(buttonActionEventHandler)
        );
        DigitalChemistUser digitalChemistUser = this.userRepository.loadDefaultUser("admin");
        // show who the user is. Only show the first name of the user.
        loggedInAsLabel.setText(String.format("Logged In As:  %s", digitalChemistUser.getFullName().split("\\s")[0]));
        welcomeText.setText(String.format("Welcome %s", digitalChemistUser.getFullName()));
        new FadeInDownBig(welcomeText).play();
        setBackgroundImage();
    }

    // update the name to be displayed to the user
    public void updateName(){
        DigitalChemistUser digitalChemistUser = this.userRepository.loadDefaultUser("admin");
        // show who the user is. Only show the first name of the user.
        loggedInAsLabel.setText(String.format("Logged In As:  %s", digitalChemistUser.getFullName().split("\\s")[0]));
        welcomeText.setText(String.format("Welcome %s", digitalChemistUser.getFullName()));
    }

    private void clearButtonStyle(){
        Stream.of(homeBtn, pointOfSaleBtn, inventoryControlBtn, reportsBtn, settingsBtn)
                .forEach(btn -> btn.setStyle("-fx-text-fill: #ffffff"));
    }

    private EventHandler<ActionEvent> buttonActionEventHandler = event -> {
        if(event.getSource() == this.homeBtn){
            this.homePane.toFront();
            new FadeIn(this.homePane).play();
            new FadeInDownBig(welcomeText).play();
            clearButtonStyle();
            homeBtn.setStyle("-fx-text-fill: #f48221");
        }else if(event.getSource() == this.pointOfSaleBtn){
            this.pointOfSalePane.toFront();
            new FadeIn(this.pointOfSalePane).play();
            clearButtonStyle();
            pointOfSaleBtn.setStyle("-fx-text-fill: #f48221");
        }else if(event.getSource() == this.inventoryControlBtn){
            this.inventoryControlPane.toFront();
            new FadeIn(this.inventoryControlPane).play();
            clearButtonStyle();
            this.inventoryControlBtn.setStyle("-fx-text-fill: #f48221");
        }else if(event.getSource() == this.settingsBtn){
            this.settingsPane.toFront();
            new FadeIn(this.settingsPane).play();
            clearButtonStyle();
            settingsBtn.setStyle("-fx-text-fill: #f48221");
        }else if(event.getSource() == this.reportsBtn){
            this.reportsPane.toFront();
            new FadeIn(this.reportsPane).play();
            clearButtonStyle();
            reportsBtn.setStyle("-fx-text-fill: #f48221");
        }else if(event.getSource() == logOutBtn){
            // log out.
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Quit Application");
            alert.setHeaderText("You are about to exit the application.");
            alert.setContentText("Proceed?");
            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent()){
                if(result.get() == ButtonType.OK){
                    ChemistManagerApplication chemistManagerApplication = ChemistManagerApplication.getInstance();
                    // get the primary stage and hide it.
                    Stage primaryStage = chemistManagerApplication.getPrimaryStage();
                    primaryStage.hide();

                    // show the dashboard by re-using the old, previous stage
                    FXMLLoader loader = null;
                    try {
                        loader = chemistManagerApplication.getFxmlLoader("fxml/log_in.fxml");
                        Parent logIn = loader.load();
                        primaryStage.setScene(new Scene(logIn));
                        primaryStage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    };

    // set the background image
    private  void setBackgroundImage(){
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            if(null != imageData && imageData.length != 0){
                this.backgroundImage.setImage(new Image(new ByteArrayInputStream(imageData)));
            }
        });
    }

    @Override
    public void update() {
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            this.backgroundImage.setImage(new Image(new ByteArrayInputStream(imageData)));
        });
    }
}
