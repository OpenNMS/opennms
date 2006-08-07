package org.opennms.web.acegisecurity;

import static org.easymock.EasyMock.*;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.web.acegisecurity.OpenNMSUserDaoImpl;
import org.opennms.web.acegisecurity.User;
import org.opennms.web.acegisecurity.UserDao;

import junit.framework.TestCase;

public class OpenNMSUserDaoImplTest extends TestCase {
	
	public void testDaoSetter() {
		UserDao userDao = createMock(UserDao.class);
		OpenNMSUserDaoImpl dao = new OpenNMSUserDaoImpl();
		
		dao.setUserDao(userDao);
	}
	
	public void testDaoGetter() {
		UserDao userDao = createMock(UserDao.class);
		OpenNMSUserDaoImpl dao = new OpenNMSUserDaoImpl();
		dao.setUserDao(userDao);
		assertEquals("getUsersDao returned what we passed to setUsersDao", userDao, dao.getUserDao());
	}
	
	public void testLoadUserWithoutDao() {
		OpenNMSUserDaoImpl dao = new OpenNMSUserDaoImpl();
		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new IllegalStateException("usersDao parameter must be set to a UsersDao bean"));
		try {
			dao.loadUserByUsername("test_user");
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}
		ta.verifyAnticipated();
	}
	
	public void testGetUser() {
		UserDao userDao = createMock(UserDao.class);
		OpenNMSUserDaoImpl dao = new OpenNMSUserDaoImpl();
		dao.setUserDao(userDao);
		
		User user = new User();
		expect(userDao.getByUsername("test_user")).andReturn(user);
		
		replay(userDao);
		
		UserDetails userDetails = dao.loadUserByUsername("test_user");
		
		verify(userDao);
		
		assertNotNull("user object from DAO not null", userDetails);
		assertEquals("user objects", user, userDetails);
	}
	
	public void testGetUnknownUser() {
		UserDao userDao = createMock(UserDao.class);
		OpenNMSUserDaoImpl dao = new OpenNMSUserDaoImpl();
		dao.setUserDao(userDao);
		
		expect(userDao.getByUsername("test_user")).andReturn(null);
		
		replay(userDao);
		
		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new UsernameNotFoundException("User test_user is not a valid user"));
		
		try {
			dao.loadUserByUsername("test_user");
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}
		verify(userDao);
		ta.verifyAnticipated();
	}
}
