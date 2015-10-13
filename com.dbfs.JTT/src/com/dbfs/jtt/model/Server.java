package com.dbfs.jtt.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server extends ModelObject {

	public Server(String url) {
		super(url);
	}

	private final List<User> users = new ArrayList<User>();

	public List<User> getUsers() {
		return users;
	}

	public boolean addUser(User usr) {
		if ((usr == null) || (usr.getName() == null) || (usr.getPassword() == null)) {
			return false;
		}
		Iterator<User> iterator = users.iterator();
		while (iterator.hasNext()) {
			User user = iterator.next();
			if (usr.getName().equals(user.getName())) {
				iterator.remove();
			}
		}
		users.add(0, usr);
		return true;
	}

}
