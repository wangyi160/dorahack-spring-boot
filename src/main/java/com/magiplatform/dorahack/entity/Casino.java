package com.magiplatform.dorahack.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Casino implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String address;
	
	private String transactionHash;
	
	private int blockHeight;
	
	private String fromAddress;
	
	private String value;
	
	private String method;
	
	private String params;
		
	private String wonAmount;
	
	private String status;
	
	
	
}
