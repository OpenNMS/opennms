package org.opennms.web.acegisecurity;

public interface UserDao {
	public User getByUsername(String username);
}
