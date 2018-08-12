package com.jeff.chemist.manager.dto;

import com.jeff.chemist.manager.entities.Drug;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.ToString;

@ToString
public class DrugDTO {

    private StringProperty id = new SimpleStringProperty();
    private StringProperty name =  new SimpleStringProperty();
    private StringProperty dosage =  new SimpleStringProperty();
    private IntegerProperty price = new SimpleIntegerProperty();
    private IntegerProperty buyingPrice = new SimpleIntegerProperty();

    /* constructor */
    public DrugDTO(Drug drug){
        this.id.set(drug.getId());
        this.name.set(drug.getName());
        this.dosage.set(drug.getDosage());
        this.price.set(drug.getPrice());
        this.buyingPrice.set(drug.getBuyingPrice());
    }


    /* define some getters */
    public String getId(){
       return this.id.get();
    }

    public String getName(){
        return this.name.get();
    }

    public String getDosage(){
        return this.dosage.get();
    }

    public int getPrice(){
        return this.price.get();
    }

    public int getBuyingPrice(){
        return this.buyingPrice.get();
    }

    /* define some setters */
    public void setId(String id){
        this.id.set(id);
    }

    public void setName(String name){
        this.name.set(name);
    }

    public void setDosage(String dosage){
        this.dosage.set(dosage);
    }

    public void setPrice(int price){
        this.price.set(price);
    }

    public void setBuyingPrice(int buyingPrice){
        this.buyingPrice.set(buyingPrice);
    }


}
