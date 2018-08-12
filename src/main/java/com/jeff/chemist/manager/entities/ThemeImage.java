package com.jeff.chemist.manager.entities;

/*
 * Represent a theme image entity
 */

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.io.Serializable;

@Entity
@Data
public class ThemeImage implements Serializable{
    @Id
    @Column
    private final String id = "currentImage";

    @Column
    @Lob
    private byte[] imageBytes;

}
