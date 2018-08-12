package com.jeff.chemist.manager.entities;

/*
 * Model a sale of a drug
 */

import com.jeff.chemist.manager.dto.DrugsWrapper;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.lang.annotation.Target;
import java.util.Date;
import java.util.List;

@Entity
@Table
@Data
public class Sale {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Lob
    @Column
    private DrugsWrapper drugsWrapper;

    @Column
    private Date date;

    @Column
    private int cashTendered;

}
