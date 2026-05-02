package com.example.travel.dto;

import java.io.Serializable;
import javax.validation.constraints.Min;
import lombok.Data;

@Data
public class SpotQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long categoryId;

    private String keyword;

    @Min(value = 1, message = "page must be greater than or equal to 1")
    private Integer page = 1;

    @Min(value = 1, message = "pageSize must be greater than or equal to 1")
    private Integer pageSize = 10;
}
