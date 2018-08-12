package com.jeff.chemist.manager.services;

import com.jeff.chemist.manager.controllers.DashboardController;
import com.jeff.chemist.manager.entities.DigitalChemistUser;
import com.jeff.chemist.manager.repositories.UserRepository;
import javafx.scene.control.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DashboardController dashboardController;

    // update the name of the user
    public void updateName(String name){
        DigitalChemistUser digitalChemistUser = userRepository.loadDefaultUser("admin");
        digitalChemistUser.setFullName(name);
        userRepository.save(digitalChemistUser);

        // notify the dashboard controller to update the name displayed to the user
         this.dashboardController.updateName();
         //TODO: notify the home controller to update the name displayed to the user.

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Operation Successful");
        alert.setContentText("The name updaged successfully");
        alert.show();

    }

    // update the user's password
    public boolean updatePassword(String oldPassword, String newPassword){
        DigitalChemistUser digitalChemistUser = userRepository.loadDefaultUser("admin");
        if(oldPassword.hashCode() == digitalChemistUser.getEncryptedPassword()){
            // proceed to update the password
            digitalChemistUser.setEncryptedPassword(newPassword.hashCode());
            this.userRepository.save(digitalChemistUser);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Operation Successful");
            alert.setContentText("The password updated successfully");
            alert.show();
            return true;
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Password Error");
            alert.setHeaderText("Insufficient rights.");
            alert.setContentText("Check your old password and retry.");
            alert.show();
            return false;
        }
    }

}
