package com.dbfs.jtt.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Company extends ModelObject {

	public Company(String name) {
		super(name);
	}

	private final List<Server> servers = new ArrayList<Server>();

	public List<Server> getServers() {
		return servers;
	}

	public boolean addServer(Server svr) {
		if ((svr == null) || (svr.getName() == null)) {
			return false;
		}
		Iterator<Server> iterator = servers.iterator();
		while (iterator.hasNext()) {
			Server server = iterator.next();
			if (svr.getName().equals(server.getName())) {
				for (User newUser : server.getUsers()) {
					svr.addUser(newUser);
				}
				iterator.remove();
				break;
			}
		}
		servers.add(0, svr);
		return true;
	}

}
