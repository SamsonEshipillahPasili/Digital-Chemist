package com.jeff.chemist.manager.entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table
@Data
public class Drug implements Serializable {
    @Column
    @Id
    private String id;
    @Column
    private String name;
    @Column
    private String dosage;
    @Column
    private int price;
    @Column
    private boolean sold = false;
    @Column
    private int buyingPrice;
}
