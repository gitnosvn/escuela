package com.example.demo.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ActualizarStockDTO;
import com.example.demo.dto.OrdenDTO;
import com.example.demo.dto.OrdenDetalleReducidoDTO;
import com.example.demo.dto.OrdenReducidaDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.entidad.Orden;
import com.example.demo.entidad.OrdenDetalle;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.ValidacionException;
import com.example.demo.feign.AlmacenClient;
import com.example.demo.feign.ProductoClient;
import com.example.demo.service.OrdenService;

@RestController
public class OrdenController {
	@Autowired
	private OrdenService ordenService;
	@Autowired
	private ProductoClient productoClient;
	@Autowired
	private AlmacenClient almacenClient;

	@PostMapping("/orden/guardar")
	public OrdenDTO guardar(@Valid @RequestBody OrdenReducidaDTO ordenDTO)
			throws ValidacionException, ResourceNotFoundException {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		BigDecimal total = new BigDecimal(0);
		Orden orden = modelMapper.map(ordenDTO, Orden.class);
		for (OrdenDetalle ordenDetalle : orden.getDetalle()) {
			int cantidad = almacenClient.obtenerCantidadStockProducto(ordenDetalle.getIdProducto()).getCantidad();
			if (cantidad < ordenDetalle.getCantidad()) {
				throw new ValidacionException("Cantidad sobrepasa el stock actual");
			}

			ProductoDTO producto = productoClient.obtenerProductoPorId(ordenDetalle.getIdProducto());
			BigDecimal precio = producto.getPrecio();
			total = total.add(precio.multiply(new BigDecimal(cantidad)));
			ordenDetalle.setPrecio(precio);
		}
		orden.setTotal(total);
		orden.setFecha(new Date());
		Orden guardado = ordenService.guardar(orden);
		ActualizarStockDTO actualizarStockDTO = new ActualizarStockDTO();
		actualizarStockDTO.setDetalle(new ArrayList<OrdenDetalleReducidoDTO>());
		guardado.getDetalle().forEach(detalle -> {
			OrdenDetalleReducidoDTO d = new OrdenDetalleReducidoDTO(detalle.getIdProducto(), detalle.getCantidad());
			actualizarStockDTO.getDetalle().add(d);
		});
		almacenClient.actualizarStock(actualizarStockDTO);
		return modelMapper.map(guardado, OrdenDTO.class);
	}
}
