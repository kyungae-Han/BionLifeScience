package com.dev.BionLifeScienceWeb.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="history_subject")
public class HistorySubject {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="HISTORY_SUBJECT_ID")
	private Long id;
	
	@Column(name="HISTORY_SUBJECT_START")
	private String start;
	
	@Column(name="HISTORY_SUBJECT_END")
	private String end;
	
	@OneToMany(
			fetch = FetchType.EAGER, 
			cascade = CascadeType.ALL,
			orphanRemoval = true,
			mappedBy = "subjectId"
			)
	private List<HistoryContent> contents;
	
	
}
