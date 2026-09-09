package com.dev.BionLifeScienceWeb.model.namecard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * 명함의 거래 서류 한 건. 파일은 PDF 만 받는다.
 * 원본 파일은 /upload 로 직접 열리지 않고 컨트롤러를 거쳐 나간다 (검색 차단 헤더를 붙이기 위해서다).
 */
@Entity
@Table(name = "name_card_document")
@Getter
@Setter
public class NameCardDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "name_card_document_id")
	private Long nameCardDocumentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "name_card_id", nullable = false)
	private NameCard nameCard;

	@Enumerated(EnumType.STRING)
	@Column(name = "doc_type", nullable = false, length = 40)
	private NameCardDocumentType docType;

	/** 서버에 저장된 실제 파일 경로. 화면에는 노출하지 않는다 */
	@Column(name = "file_path", nullable = false, length = 500)
	private String filePath;

	/** 올릴 때의 파일 이름 */
	@Column(name = "file_name", length = 255)
	private String fileName;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "sort_index")
	private Integer sortIndex = 0;

	@Transient
	public String getKoLabel() {
		return docType == null ? "" : docType.getKoLabel();
	}

	@Transient
	public String getEnLabel() {
		return docType == null ? "" : docType.getEnLabel();
	}
}
