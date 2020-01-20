package com.example.demo.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.CantidadDTO;
import com.example.demo.dto.OrdenDTO;
import com.example.demo.dto.OrdenReducidaDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.entidad.Orden;
import com.example.demo.entidad.OrdenDetalle;
import com.example.demo.exceptions.ValidacionException;
import com.example.demo.service.OrdenService;

@RestController
public class OrdenController {
	@Autowired
	private DiscoveryClient client;

	@Autowired
	private OrdenService ordenService;

	public CantidadDTO getCantidad(String service, Long idProducto) {
		List<ServiceInstance> list = client.getInstances(service);
		if (list != null && list.size() > 0) {
			int rand = (int) Math.round(Math.random() * 10) % list.size();
			URI uri = list.get(rand).getUri();
			if (uri != null) {
				return (new RestTemplate()).getForObject(uri.toString() + "/stock/acumulado/{idProducto}",
						CantidadDTO.class, idProducto);
			}
		}
		return null;
	}

	public ProductoDTO getProducto(String service, Long idProducto) {
		List<ServiceInstance> list = client.getInstances(service);
		if (list != null && list.size() > 0) {
			int rand = (int) Math.round(Math.random() * 10) % list.size();
			URI uri = list.get(rand).getUri();
			if (uri != null) {
				return (new RestTemplate()).getForObject(uri.toString() + "/productos/{id}", ProductoDTO.class,
						idProducto);
			}
		}
		return null;
	}

	@PostMapping("/orden/guardar")
	public OrdenDTO guardar(@Valid @RequestBody OrdenReducidaDTO ordenDTO) throws ValidacionException {
		ModelMapper mapper = new ModelMapper();
		BigDecimal total = new BigDecimal(0);
		Orden orden = mapper.map(ordenDTO, Orden.class);

		for (OrdenDetalle ordenDetalle : orden.getDetalle()) {
			int cantidad = getCantidad("almacen-ms", ordenDetalle.getIdProducto()).getCantidad();
			if (cantidad < ordenDetalle.getCantidad()) {
				throw new ValidacionException("Cantidad sobrepasa el stock actual");
			}

			ProductoDTO producto = getProducto("producto-ms", ordenDetalle.getIdProducto());
			BigDecimal precio = producto.getPrecio();
			total = total.add(precio.multiply(new BigDecimal(cantidad)));
			ordenDetalle.setPrecio(precio);
			ordenDetalle.setOrden(orden);
		}

		orden.setTotal(total);
		orden.setFecha(new Date());
		return mapper.map(ordenService.guardar(orden), OrdenDTO.class);
	}
}
