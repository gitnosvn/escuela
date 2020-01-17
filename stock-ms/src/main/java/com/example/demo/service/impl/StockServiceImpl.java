package com.example.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.exceptions.ValidacionException;
import com.example.demo.repository.StockRepository;
import com.example.demo.service.StockService;

@Transactional(readOnly = true)
@Service
public class StockServiceImpl implements StockService {

	@Autowired
	private StockRepository stockRepository;

	@Override
	public int obtenerCantidadPorProducto(Long idProducto) throws ValidacionException {
		return stockRepository.contarCantidadProductosStock(idProducto)
				.orElseThrow(() -> new ValidacionException("No se encontro el Producto en la tabla stock"));
	}

	@Override
	public int obtenerCantidadPorProductoYTienda(Long idProducto, Long idTienda) throws ValidacionException {
		return stockRepository.contarCantidadProductosStockEnTienda(idProducto, idTienda)
				.orElseThrow(() -> new ValidacionException("No se encontro el Producto con la Tienda en la tabla stock"));
	}

}
