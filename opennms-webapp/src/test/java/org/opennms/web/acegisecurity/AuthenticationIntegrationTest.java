package org.opennms.web.acegisecurity;

import org.acegisecurity.Authentication;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


public class AuthenticationIntegrationTest extends AbstractDependencyInjectionSpringContextTests {
	private DaoAuthenticationProvider m_provider; 

	@Override
	protected String[] getConfigLocations() {
		return new String[] {
                "org/opennms/web/acegisecurity/applicationContext-authenticationIntegrationTest.xml"
        		};
	}
	
	public void setDaoAuthenticationProvider(DaoAuthenticationProvider provider) {
		m_provider = provider;
	}
	public DaoAuthenticationProvider getDaoAuthenticationProvider() {
		return m_provider;
	}
	
	public void testAuthenticateAdmin() {
		Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "admin");
		Authentication authenticated = m_provider.authenticate(authentication);
		assertNotNull("authenticated Authentication object not null", authenticated);
		GrantedAuthority[] authorities = authenticated.getAuthorities();
		assertNotNull("GrantedAuthorities should not be null", authorities);
		assertEquals("GrantedAuthorities size", 3, authorities.length);
		assertEquals("GrantedAuthorities zero role", "ROLE_USER", authorities[0].getAuthority());
		assertEquals("GrantedAuthorities one name", "ROLE_RTC", authorities[1].getAuthority());
		assertEquals("GrantedAuthorities two name", "ROLE_ADMIN", authorities[2].getAuthority());
	}
	
	public void testAuthenticateRtc() {
		Authentication authentication = new UsernamePasswordAuthenticationToken("rtc", "rtc");
		Authentication authenticated = m_provider.authenticate(authentication);
		assertNotNull("authenticated Authentication object not null", authenticated);
		GrantedAuthority[] authorities = authenticated.getAuthorities();
		assertNotNull("GrantedAuthorities should not be null", authorities);
		assertEquals("GrantedAuthorities size", 2, authorities.length);
		assertEquals("GrantedAuthorities zero role", "ROLE_USER", authorities[0].getAuthority());
		assertEquals("GrantedAuthorities one name", "ROLE_RTC", authorities[1].getAuthority());
	}
	
	public void testAuthenticateTempUser() {
		Authentication authentication = new UsernamePasswordAuthenticationToken("tempuser", "mike");
		Authentication authenticated = m_provider.authenticate(authentication);
		assertNotNull("authenticated Authentication object not null", authenticated);
		GrantedAuthority[] authorities = authenticated.getAuthorities();
		assertNotNull("GrantedAuthorities should not be null", authorities);
		assertEquals("GrantedAuthorities size", 1, authorities.length);
		assertEquals("GrantedAuthorities zero role", "ROLE_USER", authorities[0].getAuthority());
	}
	
	public void testAuthenticateBadUsername() {
		Authentication authentication = new UsernamePasswordAuthenticationToken("badUsername", "admin");
		
		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new BadCredentialsException("Bad credentials"));
		try {
			m_provider.authenticate(authentication);
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}
		ta.verifyAnticipated();
	}
	
	public void testAuthenticateBadPassword() {
		Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "badPassword");

		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new BadCredentialsException("Bad credentials"));
		try {
			m_provider.authenticate(authentication);
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}
		ta.verifyAnticipated();
	}
}
