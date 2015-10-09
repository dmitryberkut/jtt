package com.dbfs.jtt.model;

public class ConnectionDetails {
	private final static String endPoint = "/rpc/soap/jirasoapservice-v2";
	private String server;
	private String user;
	private String password;

	public static String getEndpoint() {
		return endPoint;
	}

	public ConnectionDetails(String user, String server, String password) {
		this.user = user;
		this.server = server;
		this.password = password;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
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
