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
@Table(name="small_sort")
public class SmallSort {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="SMALL_SORT_ID")
	private Long id;
	
	@Column(name="SMALL_SORT_NAME")
	private String name;
	
	@Transient
	private Long middleId;
	
	@Column(name="SMALL_SORT_INDEX")
	private int smallSortIndex;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(
			name="SMALL_REFER_ID", referencedColumnName="MIDDLE_SORT_ID"
			)
	private MiddleSort middleSort;
	
}
