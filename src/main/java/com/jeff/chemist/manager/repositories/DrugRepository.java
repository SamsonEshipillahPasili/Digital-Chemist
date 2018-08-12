package com.jeff.chemist.manager.repositories;

import com.jeff.chemist.manager.entities.Drug;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DrugRepository extends CrudRepository<Drug, String>{
    @Query("SELECT d FROM Drug d WHERE d.sold = :sold")

    List<Drug> findAllDrugs(@Param("sold") boolean sold);
}
