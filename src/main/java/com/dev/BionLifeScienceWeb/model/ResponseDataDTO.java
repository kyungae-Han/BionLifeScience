package com.dev.BionLifeScienceWeb.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseDataDTO {
	private String code;
    private Boolean check = true;
    private String status;
    private String message;
    private Object item;
}
