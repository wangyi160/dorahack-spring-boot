package com.esaulpaugh.headlong.abi;

public class EventResult {
	
	private String name;
	private Tuple indexedResult;
	private Tuple nonIndexedResult;
	
	public EventResult(String name, Tuple indexedResult, Tuple nonIndexedResult) {
		this.name=name;
		this.indexedResult = indexedResult;
		this.nonIndexedResult = nonIndexedResult;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Tuple getIndexedResult() {
		return indexedResult;
	}

	public void setIndexedResult(Tuple indexedResult) {
		this.indexedResult = indexedResult;
	}

	public Tuple getNonIndexedResult() {
		return nonIndexedResult;
	}

	public void setNonIndexedResult(Tuple nonIndexedResult) {
		this.nonIndexedResult = nonIndexedResult;
	}
	
	public String toString() {
		return this.name + "," + this.indexedResult + "," + this.nonIndexedResult;
	}
	
}
