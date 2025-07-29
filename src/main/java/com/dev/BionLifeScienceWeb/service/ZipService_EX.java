package com.dev.BionLifeScienceWeb.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

@Service
public class ZipService_EX {

	public class CompressZip {
		/**
		 * @description 압축 메소드 
		 * @param path 압축할 폴더 경로
		 * @param outputFileName 출력파일명
		 */
		public boolean compress(String path, String outputPath, String outputFileName) throws Throwable {
			// 파일 압축 성공 여부 
			boolean isChk = false;
			
			File file = new File(path);
			
			// 파일의 .zip이 없는 경우, .zip 을 붙여준다. 
			int pos = outputFileName.lastIndexOf(".") == -1 ? outputFileName.length() : outputFileName.lastIndexOf(".");
			
			// outputFileName .zip이 없는 경우 
			if (!outputFileName.substring(pos).equalsIgnoreCase(".zip")) {
				outputFileName += ".zip";
			}
			
			// 압축 경로 체크
			if (!file.exists()) {
				throw new Exception("Not File!");
			}
			
			// 출력 스트림
			FileOutputStream fos = null;
			// 압축 스트림
			ZipOutputStream zos = null;
			
			try {
				fos = new FileOutputStream(new File(outputPath + outputFileName));
				zos = new ZipOutputStream(fos);
				
				// 디렉토리 검색를 통한 하위 파일과 폴더 검색 
				searchDirectory(file, file.getPath(), zos);
				
				// 압축 성공.
				isChk = true;
			} catch (Throwable e) {
				throw e;
			} finally {
				if (zos != null)
					zos.close();
				if (fos != null)
					fos.close();
			}
			return isChk;
		}

		/**
		 * @description 디렉토리 탐색
		 * @param file 현재 파일
		 * @param root 루트 경로
		 * @param zos  압축 스트림
		 */
		private void searchDirectory(File file, String root, ZipOutputStream zos) throws Exception {
			// 지정된 파일이 디렉토리인지 파일인지 검색
			if (file.isDirectory()) {
				// 디렉토리일 경우 재탐색(재귀)
				File[] files = file.listFiles();
				for (File f : files) {
					System.out.println("file = > " + f);
					searchDirectory(f, root, zos);
				}
			} else {
				// 파일일 경우 압축을 한다.
				try {
					compressZip(file, root, zos);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
		 * @description압축 메소드 
		 * @param file
		 * @param root
		 * @param zos
		 * @throws Throwable
		 */
		private void compressZip(File file, String root, ZipOutputStream zos) throws Throwable {
			FileInputStream fis = null;
			try {
				String zipName = file.getPath().replace(root + "\\", "");
				// 파일을 읽어드림
				fis = new FileInputStream(file);
				// Zip엔트리 생성(한글 깨짐 버그)
				ZipEntry zipentry = new ZipEntry(zipName);
				// 스트림에 밀어넣기(자동 오픈)
				zos.putNextEntry(zipentry);
				int length = (int) file.length();
				byte[] buffer = new byte[length];
				// 스트림 읽어드리기
				fis.read(buffer, 0, length);
				// 스트림 작성
				zos.write(buffer, 0, length);
				// 스트림 닫기
				zos.closeEntry();

			} catch (Throwable e) {
				throw e;
			} finally {
				if (fis != null)
					fis.close();
			}
		}

	}
	
	public class UnZip {
		/**
		 * 압축풀기 메소드
		 * 
		 * @param zipFileName 압축파일
		 * @param directory   압축 풀 폴더
		 */
		public boolean unZip(String zipPath, String zipFileName, String zipUnzipPath) {

			// 파일 정상적으로 압축이 해제가 되어는가.
			boolean isChk = false;

			// 해제할 홀더 위치를 재조정
			zipUnzipPath = zipUnzipPath + zipFileName.replace(".zip", "");

			// zip 파일
			File zipFile = new File(zipPath + zipFileName);

			FileInputStream fis = null;
			ZipInputStream zis = null;
			ZipEntry zipentry = null;

			try {
				// zipFileName을 통해서 폴더 만들기
				if (makeFolder(zipUnzipPath)) {
					System.out.println("폴더를 생성했습니다");
				}

				// 파일 스트림
				fis = new FileInputStream(zipFile);

				// Zip 파일 스트림
				zis = new ZipInputStream(fis, Charset.forName("EUC-KR"));

				// 압축되어 있는 ZIP 파일의 목록 조회
				while ((zipentry = zis.getNextEntry()) != null) {
					String filename = zipentry.getName();
					System.out.println("filename(zipentry.getName()) => " + filename);
					File file = new File(zipUnzipPath, filename);

					// entiry가 폴더면 폴더 생성
					if (zipentry.isDirectory()) {
						System.out.println("zipentry가 디렉토리입니다.");
						file.mkdirs();
					} else {
						// 파일이면 파일 만들기
						System.out.println("zipentry가 파일입니다.");
						try {
							createFile(file, zis);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
				isChk = true;
			} catch (Exception e) {
				isChk = false;
			} finally {
				if (zis != null) {
					try {
						zis.close();
					} catch (IOException e) {
					}
				}
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
					}
				}
			}
			return isChk;
		}

		/**
		 * @param folder - 생성할 폴더 경로와 이름
		 */
		private boolean makeFolder(String folder) {
			if (folder.length() < 0) {
				return false;
			}

			String path = folder; // 폴더 경로
			File Folder = new File(path);

			// 해당 디렉토리가 없을경우 디렉토리를 생성합니다.
			if (!Folder.exists()) {
				try {
					Folder.mkdir(); // 폴더 생성합니다.
					System.out.println("폴더가 생성되었습니다.");
				} catch (Exception e) {
					e.getStackTrace();
				}
			} else {
				System.out.println("이미 폴더가 생성되어 있습니다.");
			}
			return true;
		}

		/**
		 * 파일 만들기 메소드
		 * 
		 * @param file 파일
		 * @param zis  Zip스트림
		 */
		private void createFile(File file, ZipInputStream zis) throws Throwable {

			// 디렉토리 확인
			File parentDir = new File(file.getParent());
			// 디렉토리가 없으면 생성하자
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}
			FileOutputStream fos = null;
			// 파일 스트림 선언
			try {

				fos = new FileOutputStream(file);
				byte[] buffer = new byte[256];
				int size = 0;
				// Zip스트림으로부터 byte뽑아내기
				while ((size = zis.read(buffer)) > 0) {
					// byte로 파일 만들기
					fos.write(buffer, 0, size);
				}
			} catch (Throwable e) {
				throw e;
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}

				}

			}

		}
	}
}
