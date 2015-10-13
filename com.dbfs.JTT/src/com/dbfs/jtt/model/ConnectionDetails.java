package com.dbfs.jtt.model;

public class ConnectionDetails {
	private final static String endPoint = "/rpc/soap/jirasoapservice-v2";
	private String company;
	private String server;
	private String user;
	private String password;

	public static String getEndpoint() {
		return endPoint;
	}

	public ConnectionDetails(String company, String server, String user, String password) {
		this.company = company;
		this.server = server;
		this.user = user;
		this.password = password;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String url) {
		server = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
