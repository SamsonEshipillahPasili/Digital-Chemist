package com.jeff.chemist.manager.services;

import com.jeff.chemist.manager.controllers.InventoryControlController;
import com.jeff.chemist.manager.controllers.ReportsController;
import com.jeff.chemist.manager.entities.Drug;
import com.jeff.chemist.manager.repositories.DrugRepository;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class DrugService {
    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private InventoryControlController inventoryControlController;

    // get ID'd drugs
    private List<Drug> iDdrugs(List<String> codeSequence, String price, String name, String dosage, String buyingPrice){
        return codeSequence
                .stream()
                .map(id -> {
                   Drug drug = new Drug();
                   drug.setId(id);
                   drug.setPrice(Integer.valueOf(price));
                   drug.setDosage(dosage);
                   drug.setName(name);
                   drug.setBuyingPrice(Integer.valueOf(buyingPrice));
                   return drug;
                })
                .collect(Collectors.toList());
    }

    public void addDrug(String codeSequence, String price, String name, String dosage, String buyingPrice){

        // show the spinner as this could be a potentially long procedure
        inventoryControlController.showSpinner();

        javafx.concurrent.Service<Void> service = new javafx.concurrent.Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // delete any previous entities with the id's supplied.
                        drugRepository.deleteAll(drugRepository.findAllById(getCodes(codeSequence)));
                        // create generic drugs and supply them with IDs
                        List<Drug> drugs = iDdrugs(getCodes(codeSequence), price, name, dosage, buyingPrice);
                        // save all the drugs
                        drugRepository.saveAll(drugs);

                        // update the drug names displayed to the user
                        inventoryControlController.updateDrugNames();
                        inventoryControlController.clearFields();
                        inventoryControlController.hideSpinner();
                        // update the drug table
                        inventoryControlController.refreshDrugTable();
                        return null;
                    }
                };
            }
        };

        // run the add process in a separate thread to avoid the UI from hanging
        service.start();
    }


    private List<String> getCodes(String codeSequence){
        return Arrays.asList(codeSequence.split(",")).
                stream().filter(sequence -> !sequence.isEmpty())
                .map(sequence -> sequence.trim())
                .distinct()
                .collect(Collectors.toList());
    }
}
