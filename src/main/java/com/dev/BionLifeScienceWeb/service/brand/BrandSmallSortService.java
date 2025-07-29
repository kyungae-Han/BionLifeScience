package com.dev.BionLifeScienceWeb.service.brand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev.BionLifeScienceWeb.repository.brand.BrandSmallSortRepository;

@Service
public class BrandSmallSortService {

	@Autowired
	BrandSmallSortRepository brandSmallSortRepository;
}
