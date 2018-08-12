package com.jeff.chemist.manager;

import com.jeff.chemist.manager.entities.DigitalChemistUser;
import com.jeff.chemist.manager.entities.Drug;
import com.jeff.chemist.manager.entities.Sale;
import com.jeff.chemist.manager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DefaultAdministratorLoader  implements CommandLineRunner{
    @Autowired
    private UserRepository userRepository;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        if(!userRepository.existsById("admin")){
            // create a sample administrator and save them to DB
            DigitalChemistUser chemistUser = new DigitalChemistUser();
            chemistUser.setFullName("Jeff");
            chemistUser.setUserName("admin");
            chemistUser.setEncryptedPassword("admin".hashCode());

            // save user to db
            userRepository.save(chemistUser);
            System.out.println("Saved Administrator");
        }
    }
}
