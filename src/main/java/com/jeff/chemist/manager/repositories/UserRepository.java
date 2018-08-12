package com.jeff.chemist.manager.repositories;

import com.jeff.chemist.manager.entities.DigitalChemistUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/*
 * Repository for DigitalChemistUser entity instances
 */

public interface UserRepository extends CrudRepository<DigitalChemistUser, String>{
     // get the only user stored in the repo
     @Query("SELECT u FROM DigitalChemistUser u WHERE u.userName = :username")
     DigitalChemistUser loadDefaultUser(@Param("username") String username);
}
