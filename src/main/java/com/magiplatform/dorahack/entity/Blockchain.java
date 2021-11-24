package com.magiplatform.dorahack.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Blockchain implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@TableId("contract_address")
	private String contractAddress;
	
	private int blockHeight;
	
	private String balance;
	
	
}
