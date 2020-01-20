package com.example.demo.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.CantidadDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.dto.ProductoReducidoDTO;
import com.example.demo.entidad.ImagenProducto;
import com.example.demo.entidad.Producto;
import com.example.demo.entidad.TipoProducto;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.ValidacionException;
import com.example.demo.service.ProductoService;

@RestController
public class ProductoController {
	@Autowired
	private DiscoveryClient client;

	@Autowired
	private ProductoService productoService;

	public CantidadDTO getCantidad(String service, Long idProducto) {
		List<ServiceInstance> list = client.getInstances(service);
		if (list != null && list.size() > 0) {
			int rand = (int) Math.round(Math.random()*10) % list.size();
			URI uri = list.get(rand).getUri();
			if (uri != null) {
				return (new RestTemplate()).getForObject(uri.toString() + "/stock/acumulado/{idProducto}",
						CantidadDTO.class, idProducto);
			}
		}
		return null;
	}

	@GetMapping("/productos")
	public List<ProductoDTO> obtenerProductos() {
		ModelMapper mapper = new ModelMapper();
		Iterable<Producto> productos = productoService.obtenerProductos();
		return StreamSupport.stream(productos.spliterator(), false).map(p -> mapper.map(p, ProductoDTO.class))
				.collect(Collectors.toList());
	}

	@GetMapping("/productos/{id}")
	public ProductoDTO obtenerProductoPorId(@PathVariable("id") Long id) throws ResourceNotFoundException {
		ModelMapper mapper = new ModelMapper();
		ProductoDTO producto = mapper.map(productoService.obtenerProductoPorId(id), ProductoDTO.class);
		CantidadDTO cantidadDTO = getCantidad("almacen-ms", id);
		producto.setCantidadStock(cantidadDTO.getCantidad());
		return producto;
	}

	@PostMapping("/productos")
	public ProductoDTO guardarProducto(@Valid @RequestBody ProductoReducidoDTO productoReducidoDTO)
			throws ValidacionException, ResourceNotFoundException {
		ModelMapper mapper = new ModelMapper();
		Producto producto = mapper.map(productoReducidoDTO, Producto.class);

		TipoProducto tipoProducto = new TipoProducto();
		tipoProducto.setCodigo(productoReducidoDTO.getCodigoProducto());

		ImagenProducto imagenProducto = new ImagenProducto();
		imagenProducto.setRutaImagen(productoReducidoDTO.getRutaImagen());
		imagenProducto.setRutaThumbnail(productoReducidoDTO.getRutaThumbnail());

		producto.setTipoProducto(tipoProducto);
		producto.setImagenProducto(imagenProducto);

		return mapper.map(productoService.guardarProducto(producto), ProductoDTO.class);
	}

}
