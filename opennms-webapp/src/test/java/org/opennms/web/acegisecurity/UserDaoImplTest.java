package org.opennms.web.acegisecurity;

import java.net.URL;

import org.acegisecurity.GrantedAuthority;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.web.acegisecurity.User;
import org.opennms.web.acegisecurity.UserDaoImpl;

import junit.framework.TestCase;

public class UserDaoImplTest extends TestCase {

	public void testConfigSetter() {
		String usersConfigurationFile = "users.xml";
		String magicUsersConfigurationFile = "magic-users.properties";
		UserDaoImpl dao = new UserDaoImpl();
		
		dao.setUsersConfigurationFile(usersConfigurationFile);
		dao.setMagicUsersConfigurationFile(magicUsersConfigurationFile);
		
	}
	
	public void testConfigGetter() {
		String usersConfigurationFile = "users.xml";
		String magicUsersConfigurationFile = "magic-users.properties";
		UserDaoImpl dao = new UserDaoImpl();
		
		dao.setUsersConfigurationFile(usersConfigurationFile);
		dao.setMagicUsersConfigurationFile(magicUsersConfigurationFile);
		assertEquals("getUsersConfigurationFile returned what we passed to setUsersConfigurationFile", usersConfigurationFile, dao.getUsersConfigurationFile());
		assertEquals("getMagicUsersConfigurationFile returned what we passed to setMagicUsersConfigurationFile", magicUsersConfigurationFile, dao.getMagicUsersConfigurationFile());
	}

	public void testGetByUsernameWithoutUsersConfigFile() {
		UserDaoImpl dao = new UserDaoImpl();
	
		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new IllegalStateException("usersConfigurationFile parameter must be set to the location of the users.xml configuration file"));
	
		try {
			dao.getByUsername("test_user");
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}
		ta.verifyAnticipated();
	}
	
	public void testGetByUsernameWithoutMagicUsersConfigFile() {
		UserDaoImpl dao = new UserDaoImpl();
                setUsersConfigurationFile(dao);
	
		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new IllegalStateException("magicUsersConfigurationFile parameter must be set to the location of the magic-users.properties configuration file"));
	
		try {
			dao.getByUsername("test_user");
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}
		ta.verifyAnticipated();
	}

	public void testGetByUsernameAdmin() {
		UserDaoImpl dao = new UserDaoImpl();
                
                setUsersConfigurationFile(dao);
                setMagicUsersConfigurationFile(dao);
		
		User user = dao.getByUsername("admin");
		assertNotNull("user object should not be null", user);
		assertEquals("User name", "admin", user.getUsername());
		assertEquals("Full name", null, user.getFullName());
		assertEquals("Comments", null, user.getComments());
		assertEquals("Password", "21232F297A57A5A743894A0E4A801FC3", user.getPassword());
		
		GrantedAuthority[] authorities = user.getAuthorities();
		assertNotNull("authorities should not be null", authorities);
		assertEquals("authorities size", 3, authorities.length);
		assertEquals("authorities 0 name", "ROLE_USER", authorities[0].getAuthority());
		assertEquals("authorities 1 name", "ROLE_RTC", authorities[1].getAuthority());
		assertEquals("authorities 2 name", "ROLE_ADMIN", authorities[2].getAuthority());
	}

	public void testGetByUsernameBogus() {
		UserDaoImpl dao = new UserDaoImpl();
                setUsersConfigurationFile(dao);
                setMagicUsersConfigurationFile(dao);
		
		User user = dao.getByUsername("bogus");
		assertNull("user object should be null", user);
	}
	
	public void testGetByUsernameRtc() {
		UserDaoImpl dao = new UserDaoImpl();
                setUsersConfigurationFile(dao);
                setMagicUsersConfigurationFile(dao);
		
		User user = dao.getByUsername("rtc");
		assertNotNull("user object should not be null", user);
		assertEquals("User name", "rtc", user.getUsername());
		assertEquals("Full name", null, user.getFullName());
		assertEquals("Comments", null, user.getComments());
		assertEquals("Password", "68154466F81BFB532CD70F8C71426356", user.getPassword());
		
		GrantedAuthority[] authorities = user.getAuthorities();
		assertNotNull("authorities should not be null", authorities);
		assertEquals("authorities size", 2, authorities.length);
		assertEquals("authorities 0 name", "ROLE_USER", authorities[0].getAuthority());
		assertEquals("authorities 1 name", "ROLE_RTC", authorities[1].getAuthority());
	}
	
	public void testGetByUsernameTempUser() {
		UserDaoImpl dao = new UserDaoImpl();
                setUsersConfigurationFile(dao);
                setMagicUsersConfigurationFile(dao);
		
		User user = dao.getByUsername("tempuser");
		assertNotNull("user object should not be null", user);
		assertEquals("User name", "tempuser", user.getUsername());
		assertEquals("Full name", null, user.getFullName());
		assertEquals("Comments", null, user.getComments());
		assertEquals("Password", "18126E7BD3F84B3F3E4DF094DEF5B7DE", user.getPassword());
		
		GrantedAuthority[] authorities = user.getAuthorities();
		assertNotNull("authorities should not be null", authorities);
		assertEquals("authorities size", 1, authorities.length);
		assertEquals("authorities 0 name", "ROLE_USER", authorities[0].getAuthority());
	}
        
        private void setUsersConfigurationFile(UserDaoImpl dao) {
            //dao.setUsersConfigurationFile(ClassLoader.getSystemResource("org/opennms/web/acegisecurity/users.xml").getFile());
            dao.setUsersConfigurationFile("src/test/resources/org/opennms/web/acegisecurity/users.xml");
        }
        
        private void setMagicUsersConfigurationFile(UserDaoImpl dao) {
            //dao.setMagicUsersConfigurationFile(ClassLoader.getSystemResource("org/opennms/web/acegisecurity/magic-users.properties").getFile());
            dao.setMagicUsersConfigurationFile("src/test/resources/org/opennms/web/acegisecurity/magic-users.properties");
        }
	
}
