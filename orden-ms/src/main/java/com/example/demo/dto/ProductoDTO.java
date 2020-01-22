package com.example.demo.dto;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductoDTO {
	
	@ApiModelProperty(value = "")
	private Long id;
	private String nombre;
	private String codigo;
	private String descripcion;
	private BigDecimal precio;
}
