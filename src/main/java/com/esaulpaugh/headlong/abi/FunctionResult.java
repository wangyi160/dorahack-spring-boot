package com.esaulpaugh.headlong.abi;

public class FunctionResult {
	
	private String name;
	private Tuple result;
	
	public FunctionResult(String name, Tuple result) {
		this.name=name;
		this.result = result;
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Tuple getResult() {
		return result;
	}

	public void setResult(Tuple result) {
		this.result = result;
	}
	
	public String toString() {
		return this.name + "," + this.result;
	}
	
	
}
