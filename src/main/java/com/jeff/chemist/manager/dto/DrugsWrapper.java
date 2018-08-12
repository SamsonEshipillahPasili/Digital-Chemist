package com.jeff.chemist.manager.dto;

import com.jeff.chemist.manager.entities.Drug;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class DrugsWrapper implements Serializable {
    private List<Drug> drugs;
}
