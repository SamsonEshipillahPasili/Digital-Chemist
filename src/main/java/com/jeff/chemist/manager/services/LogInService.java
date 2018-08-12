package com.jeff.chemist.manager.services;

import com.jeff.chemist.manager.entities.DigitalChemistUser;
import com.jeff.chemist.manager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LogInService{
    @Autowired
    private UserRepository userRepository;

    // method to carry out the log in process
    public boolean isValidUser(String username, int password){
        Optional<DigitalChemistUser> digitalChemistUserOptional = userRepository.findById(username);
        if(digitalChemistUserOptional.isPresent()){
            DigitalChemistUser chemistUser = digitalChemistUserOptional.get();
            if(password == chemistUser.getEncryptedPassword()){
                return true;
            }
            return false;
        }else{
            // there is no such a user
            return false;
        }
    }

}
