package com.example.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("tb_spot")
public class Spot implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String name;

    private String coverUrl;

    private String introduction;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal price;

    private BigDecimal score;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
