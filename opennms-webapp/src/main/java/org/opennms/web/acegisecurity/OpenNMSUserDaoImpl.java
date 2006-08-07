package org.opennms.web.acegisecurity;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

public class OpenNMSUserDaoImpl implements UserDetailsService {
	private UserDao m_userDao;
	
	public UserDetails loadUserByUsername(String username)
		throws UsernameNotFoundException, DataAccessException {
		if (m_userDao == null) {
			// XXX there must be a better way to do this
			throw new IllegalStateException("usersDao parameter must be set to a UsersDao bean");
		}
		
		UserDetails userDetails = m_userDao.getByUsername(username);
		
		if (userDetails == null) {
			throw new UsernameNotFoundException("User test_user is not a valid user");
		}
		
		return userDetails;
	}

	public void setUserDao(UserDao userDao) {
		m_userDao = userDao;
		
	}

	public UserDao getUserDao() {
		return m_userDao;
	}
}
