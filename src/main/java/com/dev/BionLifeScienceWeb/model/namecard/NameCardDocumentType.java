package com.dev.BionLifeScienceWeb.model.namecard;

/**
 * 명함에 걸 수 있는 거래 서류 종류.
 *
 * 이 목록에 없는 서류는 올릴 수 없다. 통장사본·재무제표·자산목록처럼
 * 공개되면 안 되는 서류가 명함에 붙는 일을 막기 위한 목록이다.
 * 여기 값을 늘릴 때는 "처음 만나는 거래처에 그냥 줘도 되는 서류인가" 를 기준으로 판단한다.
 */
public enum NameCardDocumentType {

	BUSINESS_LICENSE("사업자등록증", "Business registration certificate"),
	BUSINESS_CERT("사업자등록증명원", "Certificate of business registration"),
	FACTORY_REG("공장등록증", "Factory registration certificate"),
	VENTURE_CERT("벤처기업확인서", "Venture business certificate"),
	INNOBIZ_CERT("이노비즈 확인서", "Inno-Biz certificate"),
	ISO_CERT("ISO 인증서", "ISO certificate"),
	COMPANY_PROFILE("회사소개서", "Company profile"),
	PRODUCT_CATALOG("제품 카탈로그", "Product catalog");

	private final String koLabel;
	private final String enLabel;

	NameCardDocumentType(String koLabel, String enLabel) {
		this.koLabel = koLabel;
		this.enLabel = enLabel;
	}

	public String getKoLabel() {
		return koLabel;
	}

	public String getEnLabel() {
		return enLabel;
	}
}
