package com.daou.common;

public class StampResult {
	private boolean isErr;
	private String msg;
	
	public String getMsg() {
		return msg;
	}
	public StampResult() {
		super();

		setMsg("OK");
	}
	public void setMsg(String msg) {
		this.isErr = msg.equalsIgnoreCase("OK") ? false : true;
		
		this.msg = msg;
	}
	public boolean isErr() {
		return isErr;
	}
}
