package com.dev.BionLifeScienceWeb.service.product;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import com.dev.BionLifeScienceWeb.model.product.Product;
import com.dev.BionLifeScienceWeb.model.product.ProductFile;
import com.dev.BionLifeScienceWeb.model.product.ProductImage;
import com.dev.BionLifeScienceWeb.repository.product.BigSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.MiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductFileRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductImageRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductRepository;
import com.dev.BionLifeScienceWeb.repository.product.SmallSortRepository;

@Service
public class ProductService {

	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	ProductFileService productFileService;
	
	@Autowired
	ProductImageService productImageService;
	
	@Autowired
	SmallSortRepository smallSortRepository;
	
	@Autowired
	MiddleSortRepository middleSortRepository;
	
	@Autowired
	BigSortRepository bigSortRepository;
	
	@Autowired
	ProductFileRepository productFileRepository;
	
	@Autowired
	ProductImageRepository productImageRepository;

	@Value("${spring.upload.env}")
	private String env;

	@Value("${spring.upload.path}")
	private String commonPath;

	public void excelInsert(
			Product product
			) {
		product.setTableImagePath("-");
		product.setTableImageRoad("-");
		product.setTableImageName("-");
		product.setSpecImageName("-");
		product.setSpecImagePath("-");
		product.setSpecImageRoad("-");
		
		int index = 1;
		if(productRepository.findFirstIndex().isPresent()) {
			index = productRepository.findFirstIndex().get() + 1;
		}
		
		product.setProductIndex(index);
		productRepository.save(product);
	}
	
	public void zipProductInsert(
			MultipartFile file
			) throws IOException {
		
		String absolutePath = new File("").getAbsolutePath() + "\\";
		
		File exFile = new File(commonPath + "/company");
		if(exFile.exists() && exFile.isDirectory()) {
			FileUtils.cleanDirectory(exFile); 
			exFile.delete();
		}

		File zipFile = new File(commonPath + "/company");
		zipFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(zipFile);
		fos.write(file.getBytes());
		fos.close();
		ZipUtil.explode(zipFile);
		
		for(File product : zipFile.listFiles()) {
			if(product.isDirectory() && !product.getName().equals("company")) {
				String productCode = product.getName();
				if(product.listFiles().length > 0) {
					for(File sort : product.listFiles()) {
						if(sort.isDirectory()) {
							String fileType = sort.getName();
							switch(fileType) {
								case "slide" :{
									Optional<Product> p = productRepository.findByProductCode(productCode);
									
									if(product.listFiles().length>0 && product.listFiles()!=null) {
										for(File type : sort.listFiles()) {
											String fileName = type.getName();
											ProductImage f = new ProductImage();
							        		f.setProductId(p.get().getId());
											if(env.equals("local")) {
							                	f.setProductImagePath(absolutePath + commonPath + "/company/" + productCode + "/slide/" +  fileName);
											}else if(env.equals("prod")) {
												f.setProductImagePath(commonPath + "/company/" + productCode + "/slide/" +  fileName);
											}
											f.setProductImageRoad("/administration/company/" + productCode + "/slide/" + fileName );
							                f.setProductImageName(fileName);
							                productImageRepository.save(f);
										}
									}
									break;
								}
								case "spec" :{
									for(File type : sort.listFiles()) {
										String fileName = type.getName();
										System.out.println("spec file name" + fileName);
										Optional<Product> p = productRepository.findByProductCode(productCode);
										p.ifPresent(np -> {
											np.setSpecImageName(fileName);
											np.setSpecImagePath(commonPath + "/company/" + productCode + "/spec/" + fileName);
											np.setSpecImageRoad("/administration/company/" + productCode + "/spec/" + fileName);
											productRepository.save(np);
										});
									}
									
									break;
									
								}
								case "overview" :{
									for(File type : sort.listFiles()) {
										String fileName = type.getName();
										Optional<Product> p = productRepository.findByProductCode(productCode);
										p.ifPresent(np -> {
											np.setTableImageName(fileName);
											np.setTableImagePath(commonPath + "/company/" + productCode + "/overview/" + fileName);
											np.setTableImageRoad("/administration/company/" + productCode + "/overview/" + fileName);
											productRepository.save(np);
										});
									}
									break;
								}
								case "files" :{
									Optional<Product> p = productRepository.findByProductCode(productCode);
									
									if(product.listFiles().length>0 && product.listFiles()!=null) {
										for(File type : sort.listFiles()) {
											String fileName = type.getName();
											ProductFile f = new ProductFile();
											f.setProductId(p.get().getId());
											if(env.equals("local")) {
							                	f.setProductFilePath(absolutePath + commonPath + "/company/" + productCode + "/files/" +  fileName);
											}else if(env.equals("prod")) {
												f.setProductFilePath(commonPath + "/company/" + productCode + "/files/" +  fileName);
											}
											f.setProductFileRoad("/administration/company/" + productCode + "/files/" + fileName);
							                f.setProductFileName(fileName);
							                f.setProductFileDate(new Date());
							                productFileRepository.save(f);
										}
									}
									break;
								}
							}
							
						}
					}
				}
			}
		}
	}
	
	public void zipAddProductInsert(
			MultipartFile file
			) throws IOException {
		
		String absolutePath = new File("").getAbsolutePath() + "\\";
		
		File exFile = new File(commonPath + "/temp");
		if(exFile.exists() && exFile.isDirectory()) {
			FileUtils.cleanDirectory(exFile); 
			exFile.delete();
		}
		
		File zipFile = new File(commonPath + "/temp");
		zipFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(zipFile);
		fos.write(file.getBytes());
		fos.close();
		ZipUtil.explode(zipFile);
		
		for(File product : zipFile.listFiles()) {
			if(product.isDirectory() && !product.getName().equals("company")) {
				String productCode = product.getName();
				if(product.listFiles().length > 0) {
					for(File sort : product.listFiles()) {
						if(sort.isDirectory()) {
							String fileType = sort.getName();
							switch(fileType) {
								case "slide" :{
									Optional<Product> p = productRepository.findByProductCode(productCode);
									
									if(product.listFiles().length>0 && product.listFiles()!=null) {
										for(File type : sort.listFiles()) {
											String fileName = type.getName();
											ProductImage f = new ProductImage();
							        		f.setProductId(p.get().getId());
											if(env.equals("local")) {
							                	f.setProductImagePath(absolutePath + commonPath + "/company/" + productCode + "/slide/" +  fileName);
											}else if(env.equals("prod")) {
												f.setProductImagePath(commonPath + "/company/" + productCode + "/slide/" +  fileName);
											}
											f.setProductImageRoad("/administration/company/" + productCode + "/slide/" + fileName );
							                f.setProductImageName(fileName);
							                productImageRepository.save(f);
										}
									}
									break;
								}
								case "spec" :{
									for(File type : sort.listFiles()) {
										String fileName = type.getName();
										System.out.println("spec file name" + fileName);
										Optional<Product> p = productRepository.findByProductCode(productCode);
										p.ifPresent(np -> {
											np.setSpecImageName(fileName);
											np.setSpecImagePath(commonPath + "/company/" + productCode + "/spec/" + fileName);
											np.setSpecImageRoad("/administration/company/" + productCode + "/spec/" + fileName);
											productRepository.save(np);
										});
									}
									
									break;
									
								}
								case "overview" :{
									for(File type : sort.listFiles()) {
										String fileName = type.getName();
										Optional<Product> p = productRepository.findByProductCode(productCode);
										p.ifPresent(np -> {
											np.setTableImageName(fileName);
											np.setTableImagePath(commonPath + "/company/" + productCode + "/overview/" + fileName);
											np.setTableImageRoad("/administration/company/" + productCode + "/overview/" + fileName);
											productRepository.save(np);
										});
									}
									break;
								}
								case "files" :{
									Optional<Product> p = productRepository.findByProductCode(productCode);
									
									if(product.listFiles().length>0 && product.listFiles()!=null) {
										for(File type : sort.listFiles()) {
											String fileName = type.getName();
											ProductFile f = new ProductFile();
											f.setProductId(p.get().getId());
											if(env.equals("local")) {
							                	f.setProductFilePath(absolutePath + commonPath + "/company/" + productCode + "/files/" +  fileName);
											}else if(env.equals("prod")) {
												f.setProductFilePath(commonPath + "/company/" + productCode + "/files/" +  fileName);
											}
											f.setProductFileRoad("/administration/company/" + productCode + "/files/" + fileName);
							                f.setProductFileName(fileName);
							                f.setProductFileDate(new Date());
							                productFileRepository.save(f);
										}
									}
									break;
								}
							}
							
						}
					}
				}
			}
		}
		for(File f : zipFile.listFiles()) {
			File to = new File(commonPath + "/company/" + f.getName());
			Files.move(f.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		
		if(exFile.exists() && exFile.isDirectory()) {
			FileUtils.cleanDirectory(exFile); 
			exFile.delete();
		}
	}
	
	public Product productInsert(
			MultipartFile productOverviewImage, 
			MultipartFile productSpecImage, 
			Product product)
			throws IllegalStateException, IOException {
	
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
		String current_date = simpleDateFormat.format(new Date());
		String absolutePath = new File("").getAbsolutePath() + "\\";

		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1)
				.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
		String productCode = generatedString + "_" + current_date;
		
		String overviewPath = commonPath + "/company/" + productCode + "/overview";
		String overviewRoad = "/administration/company/" + productCode + "/overview";
		File overviewFileFolder = new File(overviewPath);
		if (!overviewFileFolder.exists()) {
			overviewFileFolder.mkdirs();
		}
		
		String overviewContentType = productOverviewImage.getContentType();
		String overviewOriginalFileExtension = "";
		
		if (ObjectUtils.isEmpty(overviewContentType)) {
			return null;
		} else {
			if (overviewContentType.contains("image/jpeg")) {
				overviewOriginalFileExtension = ".jpg";
			} else if (overviewContentType.contains("image/png")) {
				overviewOriginalFileExtension = ".png";
			} else if (overviewContentType.contains("image/gif")) {
				overviewOriginalFileExtension = ".gif";
			} else if (overviewContentType.contains("application/pdf")) {
				overviewOriginalFileExtension = ".pdf";
			} else if (overviewContentType.contains("application/x-zip-compressed")) {
				overviewOriginalFileExtension = ".zip";
			} else if (overviewContentType
					.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
				overviewOriginalFileExtension = ".xlsx";
			} else if (overviewContentType
					.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
				overviewOriginalFileExtension = ".docx";
			} else if (overviewContentType.contains("text/plain")) {
				overviewOriginalFileExtension = ".txt";
			} else if (overviewContentType.contains("image/x-icon")) {
				overviewOriginalFileExtension = ".ico";
			} else if (overviewContentType.contains("application/haansofthwp")) {
				overviewOriginalFileExtension = ".hwp";
			}
		}
		String overviewFileName = generatedString + "_" + productOverviewImage.getOriginalFilename();
		
		String specPath = commonPath + "/company/" + productCode + "/spec";
		String specRoad = "/administration/company/" + productCode+ "/spec";
		File specFileFolder = new File(specPath);

		if (!specFileFolder.exists()) {
			specFileFolder.mkdirs();
		}

		String specContentType = productSpecImage.getContentType();
		String specOriginalFileExtension = "";
		

		if (ObjectUtils.isEmpty(specContentType)) {
			return null;
		} else {
			if (specContentType.contains("image/jpeg")) {
				specOriginalFileExtension = ".jpg";
			} else if (specContentType.contains("image/png")) {
				specOriginalFileExtension = ".png";
			} else if (specContentType.contains("image/gif")) {
				specOriginalFileExtension = ".gif";
			} else if (specContentType.contains("application/pdf")) {
				specOriginalFileExtension = ".pdf";
			} else if (specContentType.contains("application/x-zip-compressed")) {
				specOriginalFileExtension = ".zip";
			} else if (specContentType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
				specOriginalFileExtension = ".xlsx";
			} else if (specContentType
					.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
				specOriginalFileExtension = ".docx";
			} else if (specContentType.contains("text/plain")) {
				specOriginalFileExtension = ".txt";
			} else if (specContentType.contains("image/x-icon")) {
				specOriginalFileExtension = ".ico";
			} else if (specContentType.contains("application/haansofthwp")) {
				specOriginalFileExtension = ".hwp";
			}
		}
		String specFileName = generatedString + "_" + productSpecImage.getOriginalFilename();
		
		if (env.equals("local")) {
			overviewFileFolder = new File(absolutePath + overviewPath + "/" + overviewFileName);
			specFileFolder = new File(absolutePath + specPath + "/" + specFileName);
		} else if (env.equals("prod")) {
			overviewFileFolder = new File(overviewPath + "/" + overviewFileName);
			specFileFolder = new File(specPath + "/" + specFileName);
		}
		int index = 1;
		if(productRepository.findFirstIndex().isPresent()) {
			index = productRepository.findFirstIndex().get() + 1;
		}
		
		if(product.getSign()==null) {
			product.setSign(false);
		}else {
			product.setSign(true);
		}
		
		product.setProductIndex(index);
		productOverviewImage.transferTo(overviewFileFolder);
		productSpecImage.transferTo(specFileFolder);
		product.setProductCode(productCode);
		product.setTableImagePath(overviewPath + "/" + overviewFileName);
		product.setTableImageRoad(overviewRoad + "/" + overviewFileName);
		product.setTableImageName(overviewFileName);
		product.setSpecImageName(specFileName);
		product.setSpecImagePath(specPath + "/" + specFileName);
		product.setSpecImageRoad(specRoad + "/" + specFileName);

		return productRepository.save(product);

	}

	
	public Boolean removeOverViewImage(
			Long id
			) {
		String overViewPath = productRepository.findById(id).get().getTableImagePath();
		Boolean overResult = true;
		try {
			File overViewFile = new File(overViewPath);
			overResult = overViewFile.delete();
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		return overResult;
	}
	
	public void deleteFiles(Long id) throws IOException {
		
		File folder = new File(commonPath + "/company/" + productRepository.findById(id).get().getProductCode());
		ExecutorService executorService = Executors.newCachedThreadPool();

		executorService.submit(() -> {
			try {
				FileUtils.cleanDirectory(folder); 
			} catch (Exception e) {
				System.out.println(e);
			}
		});
		executorService.submit(() -> {
			folder.delete();
		});
	}
	
	public Boolean removeSpecImage(
			Long id
			) {
		String specPath = productRepository.findById(id).get().getSpecImagePath();
		Boolean specResult = true;
		try {
			File specFile = new File(specPath);
			specResult = specFile.delete();
			
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		return specResult;
	}
	
	public String productUpdate(
			MultipartFile productOverviewImage, 
			MultipartFile productSpecImage, 
			Product product)
			throws IllegalStateException, IOException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String current_date = simpleDateFormat.format(new Date());
		String absolutePath = new File("").getAbsolutePath() + "\\";

		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();
		
		
		if (!productOverviewImage.isEmpty()) {
			File exOverViewFile = new File(productRepository.findById(product.getId()).get().getTableImagePath());
			if(exOverViewFile.isDirectory()&&exOverViewFile.exists()) {
				FileUtils.cleanDirectory(exOverViewFile);
			}
			
			String generatedString = random.ints(leftLimit, rightLimit + 1)
					.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
					.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
			
			String overviewPath = commonPath + "/company/" + product.getProductCode() + "/overview";
			String overviewRoad = "/administration/company/" + product.getProductCode() + "/overview";
			
			File overviewFileFolder = new File(overviewPath);

			if (!overviewFileFolder.exists()) {
				overviewFileFolder.mkdirs();
			}

			String overviewContentType = productOverviewImage.getContentType();
			String overviewOriginalFileExtension = "";

			if (ObjectUtils.isEmpty(overviewContentType)) {
				return "fail";
			} else {
				if (overviewContentType.contains("image/jpeg")) {
					overviewOriginalFileExtension = ".jpg";
				} else if (overviewContentType.contains("image/png")) {
					overviewOriginalFileExtension = ".png";
				} else if (overviewContentType.contains("image/gif")) {
					overviewOriginalFileExtension = ".gif";
				} else if (overviewContentType.contains("application/pdf")) {
					overviewOriginalFileExtension = ".pdf";
				} else if (overviewContentType.contains("application/x-zip-compressed")) {
					overviewOriginalFileExtension = ".zip";
				} else if (overviewContentType
						.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
					overviewOriginalFileExtension = ".xlsx";
				} else if (overviewContentType
						.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
					overviewOriginalFileExtension = ".docx";
				} else if (overviewContentType.contains("text/plain")) {
					overviewOriginalFileExtension = ".txt";
				} else if (overviewContentType.contains("image/x-icon")) {
					overviewOriginalFileExtension = ".ico";
				} else if (overviewContentType.contains("application/haansofthwp")) {
					overviewOriginalFileExtension = ".hwp";
				}
			}

			String overviewFileName = generatedString + "_" + productOverviewImage.getOriginalFilename();
			if (env.equals("local")) {
				overviewFileFolder = new File(absolutePath + overviewPath + "/" + overviewFileName);
			} else if (env.equals("prod")) {
				overviewFileFolder = new File(overviewPath + "/" + overviewFileName);
			}

			productOverviewImage.transferTo(overviewFileFolder);
			productRepository.findById(product.getId()).ifPresent(s -> {
				s.setTableImagePath(overviewPath + "/" + overviewFileName);
				s.setTableImageRoad(overviewRoad + "/" + overviewFileName);
				s.setTableImageName(overviewFileName);
				productRepository.save(s);
			});
		}
		if (!productSpecImage.isEmpty()) {
			
			File exSpecFile = new File(productRepository.findById(product.getId()).get().getSpecImagePath());
			if(exSpecFile.isDirectory()&&exSpecFile.exists()) {
				FileUtils.cleanDirectory(exSpecFile);
			}
			String generatedString = random.ints(leftLimit, rightLimit + 1)
					.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
					.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
			
			String specPath = commonPath + "/company/" + product.getProductCode() + "/spec" ;
			String specRoad = "/administration/company/" + product.getProductCode()+ "/spec" ;
			
			File specFileFolder = new File(specPath);

			if (!specFileFolder.exists()) {
				specFileFolder.mkdirs();
			}

			String specContentType = productSpecImage.getContentType();
			String specOriginalFileExtension = "";
			if (ObjectUtils.isEmpty(specContentType)) {
				return null;
			} else {
				if (specContentType.contains("image/jpeg")) {
					specOriginalFileExtension = ".jpg";
				} else if (specContentType.contains("image/png")) {
					specOriginalFileExtension = ".png";
				} else if (specContentType.contains("image/gif")) {
					specOriginalFileExtension = ".gif";
				} else if (specContentType.contains("application/pdf")) {
					specOriginalFileExtension = ".pdf";
				} else if (specContentType.contains("application/x-zip-compressed")) {
					specOriginalFileExtension = ".zip";
				} else if (specContentType
						.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
					specOriginalFileExtension = ".xlsx";
				} else if (specContentType
						.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
					specOriginalFileExtension = ".docx";
				} else if (specContentType.contains("text/plain")) {
					specOriginalFileExtension = ".txt";
				} else if (specContentType.contains("image/x-icon")) {
					specOriginalFileExtension = ".ico";
				} else if (specContentType.contains("application/haansofthwp")) {
					specOriginalFileExtension = ".hwp";
				}
			}

			String specFileName = generatedString + "_" + productSpecImage.getOriginalFilename();

			if (env.equals("local")) {
				specFileFolder = new File(absolutePath + specPath + "/" + specFileName);
			} else if (env.equals("prod")) {
				specFileFolder = new File(specPath + "/" + specFileName);
			}
			productSpecImage.transferTo(specFileFolder);
			productRepository.findById(product.getId()).ifPresent(s -> {
				s.setSpecImageName(specFileName);
				s.setSpecImagePath(specPath + "/" + specFileName);
				s.setSpecImageRoad(specRoad + "/" + specFileName);
				productRepository.save(s);
			});

		}
		productRepository.findById(product.getId()).ifPresent(s -> {
			s.setSmallSort(smallSortRepository.findById(product.getSmallId()).get());
			s.setMiddleSort(middleSortRepository.findById(product.getMiddleId()).get());
			s.setBigSort(bigSortRepository.findById(product.getBigId()).get());
			s.setSubject(product.getSubject());
			s.setContent(product.getContent());
			s.setProductSubContent(product.getProductSubContent());
			s.setSign(product.getSign());
			productRepository.save(s);
		});

		return "success";

	}
	
	public String productOverview(
			MultipartFile productOverviewImage, 
			Product product)
			throws IllegalStateException, IOException {
		String absolutePath = new File("").getAbsolutePath() + "\\";

		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();

		if (!productOverviewImage.isEmpty()) {
			File exOverViewFile = new File(productRepository.findById(product.getId()).get().getTableImagePath());
			exOverViewFile.delete();
			String generatedString = random.ints(leftLimit, rightLimit + 1)
					.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
					.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
			
			
			String overviewPath = commonPath + "/company/" + product.getProductCode() + "/overview";
			String overviewRoad = "/administration/company/" + product.getProductCode() + "/overview";
			
			File overviewFileFolder = new File(overviewPath);

			if (!overviewFileFolder.exists()) {
				overviewFileFolder.mkdirs();
			}

			String overviewContentType = productOverviewImage.getContentType();
			String overviewOriginalFileExtension = "";

			if (ObjectUtils.isEmpty(overviewContentType)) {
				return "fail";
			} else {
				if (overviewContentType.contains("image/jpeg")) {
					overviewOriginalFileExtension = ".jpg";
				} else if (overviewContentType.contains("image/png")) {
					overviewOriginalFileExtension = ".png";
				} else if (overviewContentType.contains("image/gif")) {
					overviewOriginalFileExtension = ".gif";
				} else if (overviewContentType.contains("application/pdf")) {
					overviewOriginalFileExtension = ".pdf";
				} else if (overviewContentType.contains("application/x-zip-compressed")) {
					overviewOriginalFileExtension = ".zip";
				} else if (overviewContentType
						.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
					overviewOriginalFileExtension = ".xlsx";
				} else if (overviewContentType
						.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
					overviewOriginalFileExtension = ".docx";
				} else if (overviewContentType.contains("text/plain")) {
					overviewOriginalFileExtension = ".txt";
				} else if (overviewContentType.contains("image/x-icon")) {
					overviewOriginalFileExtension = ".ico";
				} else if (overviewContentType.contains("application/haansofthwp")) {
					overviewOriginalFileExtension = ".hwp";
				}
			}

			String overviewFileName = generatedString + "_" + productOverviewImage.getOriginalFilename();
			if (env.equals("local")) {
				overviewFileFolder = new File(absolutePath + overviewPath + "/" + overviewFileName);
			} else if (env.equals("prod")) {
				overviewFileFolder = new File(overviewPath + "/" + overviewFileName);
			}

			productOverviewImage.transferTo(overviewFileFolder);
			productRepository.findById(product.getId()).ifPresent(s -> {
				s.setTableImagePath(overviewPath + "/" + overviewFileName);
				s.setTableImageRoad(overviewRoad + "/" + overviewFileName);
				s.setTableImageName(overviewFileName);
				productRepository.save(s);
			});
		}

		return "success";

	}
	
	public String productSpec(
			MultipartFile productSpecImage, 
			Product product)
			throws IllegalStateException, IOException {
		String absolutePath = new File("").getAbsolutePath() + "\\";

		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();

		if (!productSpecImage.isEmpty()) {
			File exSpecFile = new File(productRepository.findById(product.getId()).get().getSpecImagePath());
			exSpecFile.delete();
			String generatedString = random.ints(leftLimit, rightLimit + 1)
					.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
					.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
			
			String specPath = commonPath + "/company/" + product.getProductCode() + "/spec" ;
			String specRoad = "/administration/company/" + product.getProductCode()+ "/spec" ;
			
			File specFileFolder = new File(specPath);

			if (!specFileFolder.exists()) {
				specFileFolder.mkdirs();
			}

			String specContentType = productSpecImage.getContentType();
			String specOriginalFileExtension = "";
			if (ObjectUtils.isEmpty(specContentType)) {
				return null;
			} else {
				if (specContentType.contains("image/jpeg")) {
					specOriginalFileExtension = ".jpg";
				} else if (specContentType.contains("image/png")) {
					specOriginalFileExtension = ".png";
				} else if (specContentType.contains("image/gif")) {
					specOriginalFileExtension = ".gif";
				} else if (specContentType.contains("application/pdf")) {
					specOriginalFileExtension = ".pdf";
				} else if (specContentType.contains("application/x-zip-compressed")) {
					specOriginalFileExtension = ".zip";
				} else if (specContentType
						.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
					specOriginalFileExtension = ".xlsx";
				} else if (specContentType
						.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
					specOriginalFileExtension = ".docx";
				} else if (specContentType.contains("text/plain")) {
					specOriginalFileExtension = ".txt";
				} else if (specContentType.contains("image/x-icon")) {
					specOriginalFileExtension = ".ico";
				} else if (specContentType.contains("application/haansofthwp")) {
					specOriginalFileExtension = ".hwp";
				}
			}

			String specFileName = generatedString + "_" + productSpecImage.getOriginalFilename();

			if (env.equals("local")) {
				specFileFolder = new File(absolutePath + specPath + "/" + specFileName);
			} else if (env.equals("prod")) {
				specFileFolder = new File(specPath + "/" + specFileName);
			}
			productSpecImage.transferTo(specFileFolder);
			productRepository.findById(product.getId()).ifPresent(s -> {
				s.setSpecImageName(specFileName);
				s.setSpecImagePath(specPath + "/" + specFileName);
				s.setSpecImageRoad(specRoad + "/" + specFileName);
				productRepository.save(s);
			});

		}

		return "success";

	}
}
