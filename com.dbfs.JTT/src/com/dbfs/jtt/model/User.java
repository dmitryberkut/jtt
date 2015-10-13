package com.dbfs.jtt.model;

public class User extends ModelObject {

	public User(String name) {
		super(name);
	}

	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}