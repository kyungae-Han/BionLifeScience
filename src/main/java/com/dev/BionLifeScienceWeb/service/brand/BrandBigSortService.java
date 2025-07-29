package com.dev.BionLifeScienceWeb.service.brand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev.BionLifeScienceWeb.repository.brand.BrandBigSortRepository;

@Service
public class BrandBigSortService {

	@Autowired
	BrandBigSortRepository brandBigSortRepository;
}
