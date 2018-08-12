package com.jeff.chemist.manager.controllers;

import animatefx.animation.FadeIn;
import com.jeff.chemist.manager.entities.DigitalChemistUser;
import com.jeff.chemist.manager.entities.ThemeImage;
import com.jeff.chemist.manager.repositories.ThemeImageRepository;
import com.jeff.chemist.manager.repositories.UserRepository;
import com.jeff.chemist.manager.services.BackgroundImageObserver;
import com.jeff.chemist.manager.services.ThemeImageService;
import com.jeff.chemist.manager.services.UserService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

@Controller
public class SettingsController implements Initializable, BackgroundImageObserver {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private ThemeImageRepository themeImageRepository;
    @Autowired
    private ThemeImageService themeImageService;

    @FXML
    private JFXTextField updateNameField;

    @FXML
    private JFXPasswordField oldPaswordField, newPasswordField, confirmPasswordField;

    @FXML
    private JFXButton updateNameBtn, updatePasswordBtn, selectImageBtn, updateImageBtn;

    @FXML
    private ImageView displayCurrentImageView, settingsBackgroundImage;
    
    private File selectedImageFile = null;


    EventHandler<ActionEvent> actionEventEventHandler = event -> {
        Object source = event.getSource();
        if (source == this.updateNameBtn) { // update the full name of the user.
            String name = updateNameField.getText();
            if (name.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Name Error");
                alert.setHeaderText("The supplied name is empty!");
                alert.setContentText("Supply a name and try again.");
                alert.show();
            } else {
                // delegate the update op to the UserService
                this.userService.updateName(name);
            }
        } else if (source == this.updatePasswordBtn) {
            boolean areNonEmpty = Arrays.asList(oldPaswordField, newPasswordField, confirmPasswordField)
                    .stream()
                    .map(field -> field.getText())
                    .map(text -> text.trim())
                    .allMatch(text -> !text.isEmpty());
            if (areNonEmpty) {
                if (newPasswordField.getText().trim().equals(confirmPasswordField.getText().trim())) {
                    boolean wasSuccessful = this.userService.updatePassword(oldPaswordField.getText().trim(),
                            newPasswordField.getText().trim());
                    if (wasSuccessful) {
                        this.oldPaswordField.clear();
                        this.newPasswordField.clear();
                        this.confirmPasswordField.clear();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Password Error");
                    alert.setHeaderText("Passwords do not match!");
                    alert.setContentText("Ensure the supplied passwords match and try again.");
                    alert.show();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Password Error");
                alert.setHeaderText("Empty Fields");
                alert.setContentText("Fill all the fields and try again!");
                alert.show();
            }
        } else if (source == this.selectImageBtn) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Theme Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("JPEG", "*.jpeg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png")
            );

            selectedImageFile = fileChooser.showOpenDialog(null);
            if (null != selectedImageFile) {
                try {
                    this.displayCurrentImageView.setImage(new Image(new FileInputStream(selectedImageFile)));
                    new FadeIn(displayCurrentImageView).play();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (source == this.updateImageBtn) {

            if (null != selectedImageFile) {
                ThemeImage themeImage = null;

                if (this.themeImageRepository.existsById("currentImage")) {
                    themeImage = this.themeImageRepository.findById("currentImage").get();
                } else {
                    themeImage = new ThemeImage();
                }

                byte[] imageBytes = new byte[(int) selectedImageFile.length()];
                try {
                    FileInputStream fis = new FileInputStream(selectedImageFile);
                    fis.read(imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                themeImage.setImageBytes(imageBytes);
                this.themeImageRepository.save(themeImage);

                this.updateBackgoundImage();
            }
        }
    };



    public void updateBackgoundImage() {
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            if (null != imageData && imageData.length != 0) {
               this.themeImageService.updateBackgroundImage(imageData);
            }
        });
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Arrays.asList(updateNameBtn, updatePasswordBtn, selectImageBtn, updateImageBtn).forEach(
                button -> button.setOnAction(this.actionEventEventHandler)
        );

        DigitalChemistUser digitalChemistUser = this.userRepository.loadDefaultUser("admin");
        this.updateNameField.setText(digitalChemistUser.getFullName());

        // add this class to the list of image observers
        this.themeImageService.getBackgroundImageObservers().add(this);

        // update the background theme image.
        //this.updateBackgoundImage();
        this.setBackgroundImage();
    }

    @Override
    public void update() {
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            this.settingsBackgroundImage.setImage(new Image(new ByteArrayInputStream(imageData)));
            this.displayCurrentImageView.setImage(new Image(new ByteArrayInputStream(imageData)));
        });
        new FadeIn(this.settingsBackgroundImage).play();
    }

    private  void setBackgroundImage(){
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            if(null != imageData && imageData.length != 0){
                this.settingsBackgroundImage.setImage(new Image(new ByteArrayInputStream(imageData)));
                this.displayCurrentImageView.setImage(new Image(new ByteArrayInputStream(imageData)));
            }
        });
    }


}
