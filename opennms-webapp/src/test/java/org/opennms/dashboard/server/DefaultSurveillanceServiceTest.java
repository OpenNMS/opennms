package org.opennms.dashboard.server;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.opennms.test.ThrowableAnticipator;

import junit.framework.TestCase;

public class DefaultSurveillanceServiceTest extends TestCase {
    private DefaultSurveillanceService m_service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_service = new DefaultSurveillanceService();
        
        /*
         * Since the SecurityContext is stored in a ThreadLocal we need to
         * be sure to clear it after every test.
         */
        SecurityContextHolder.clearContext();
    }
    
    public void testGetUsernameWithUserDetails() {
        UserDetails details = new User("user", "password", true, true, true, true, new GrantedAuthority[0]);
        Authentication auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        String user = m_service.getUsername();
        assertNotNull("user should not be null", user);
        assertEquals("user name", details.getUsername(), user);
    }
    
    public void testGetUsernameWithStringPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", null, new GrantedAuthority[0]);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        String user = m_service.getUsername();
        assertNotNull("user should not be null", user);
        assertEquals("user name", "user", user);
    }
    
    public void testGetUsernameNoAuthenticationObject() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("No Authentication object found when calling getAuthentication on our SecurityContext object"));
        
        try {
            m_service.getUsername();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }
    
    public void testGetUsernameNoPrincipalObject() {
        Authentication auth = new UsernamePasswordAuthenticationToken(null, null, new GrantedAuthority[0]);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("No principal object found when calling getPrinticpal on our Authentication object"));
        
        try {
            m_service.getUsername();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }
}
