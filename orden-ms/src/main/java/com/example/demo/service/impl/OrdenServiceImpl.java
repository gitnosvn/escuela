package com.example.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entidad.Orden;
import com.example.demo.exceptions.ValidacionException;
import com.example.demo.repository.OrdenRepository;
import com.example.demo.service.OrdenService;

@Service
public class OrdenServiceImpl implements OrdenService {

	@Autowired
	private OrdenRepository ordenRepository;

	@Override
	public Orden guardar(Orden orden) {
		return ordenRepository.save(orden);
	}

}
