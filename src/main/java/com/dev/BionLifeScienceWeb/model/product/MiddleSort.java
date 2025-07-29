package com.dev.BionLifeScienceWeb.model.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name="middle_sort")
public class MiddleSort {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="MIDDLE_SORT_ID")
	private Long id;
	
	@Column(name="MIDDLE_SORT_NAME")
	private String name;
	
	@Transient
	private Long bigId;
	
	@Column(name="MIDDLE_SORT_INDEX")
	private int middleSortIndex;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(
			name="MIDDLE_REFER_ID", referencedColumnName="BIG_SORT_ID"
			)
	private BigSort bigSort;
}















