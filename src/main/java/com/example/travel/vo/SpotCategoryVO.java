package com.example.travel.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SpotCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Integer sort;
}
