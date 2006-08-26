package org.opennms.web.acegisecurity;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.opennms.test.ThrowableAnticipator;

import junit.framework.TestCase;

public class UserAttributeLdapAuthoritiesPopulatorTest extends TestCase {
    private static final String s_userAttribute = "userRole";
    private static final String s_dn = "cn=\"l33t OpenNMS User\", "
        + "ou=OpenNMS Rocks, dc=opennms, dc=org";
    
    public void testInit() {
        init();
    }

    public void testInitNullUserAttribute() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("UserAttribute can not be null"));
        try {
            new UserAttributeLdapAuthoritiesPopulator(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testGetGrantedAuthorities() {
        UserAttributeLdapAuthoritiesPopulator populator = init();
        
        LdapUserDetails userDetails = createMock(LdapUserDetails.class);
        Attributes attributes = new BasicAttributes();
        expect(userDetails.getDn()).andReturn(s_dn).anyTimes();
        expect(userDetails.getAttributes()).andReturn(attributes);
        
        replay(userDetails);
        GrantedAuthority[] authorities =
            populator.getGrantedAuthorities(userDetails);
        verify(userDetails);

        assertNotNull("GrantedAuthority[] list returned from "
                      + "getGrantedAuthorities cannot be null", authorities);
    }

    public void testGetGrantedAuthoritiesNull() {
        UserAttributeLdapAuthoritiesPopulator populator = init();
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("UserDetails can not be null"));
        try {
            populator.getGrantedAuthorities(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testGetGrantedAuthoritiesNullAttribute() {
        UserAttributeLdapAuthoritiesPopulator populator = init();
        
        LdapUserDetails userDetails = createMock(LdapUserDetails.class);
        Attributes attributes = new BasicAttributes(s_userAttribute, null, true);
        expect(userDetails.getDn()).andReturn(s_dn).anyTimes();
        expect(userDetails.getAttributes()).andReturn(attributes);
        
        replay(userDetails);
        GrantedAuthority[] authorities =
            populator.getGrantedAuthorities(userDetails);
        verify(userDetails);

        assertNotNull("GrantedAuthority[] list returned from "
                      + "getGrantedAuthorities cannot be null", authorities);
        assertEquals("No entries in the GrantedAuthority list",
                     0, authorities.length); 
    }

    
    public void testGetGrantedAuthoritiesNone() {
        UserAttributeLdapAuthoritiesPopulator populator = init();
        
        LdapUserDetails userDetails = createMock(LdapUserDetails.class);
        Attributes attributes = new BasicAttributes();
        expect(userDetails.getDn()).andReturn(s_dn).anyTimes();
        expect(userDetails.getAttributes()).andReturn(attributes);
        
        replay(userDetails);
        GrantedAuthority[] authorities =
            populator.getGrantedAuthorities(userDetails);
        verify(userDetails);

        assertNotNull("GrantedAuthority[] list returned from "
                      + "getGrantedAuthorities cannot be null", authorities);
        assertEquals("No entries in the GrantedAuthority list",
                     0, authorities.length); 
    }
    
    public void testGetGrantedAuthoritiesAdmin() {
        doSingleAuthority("OpenNMS Administrator", "ROLE_ADMIN");
    }
    
    public void testGetGrantedAuthoritiesUser() {
        doSingleAuthority("OpenNMS User", "ROLE_USER");
    }
    
    public void testGetGrantedAuthoritiesRTC() {
        doSingleAuthority("OpenNMS RTC Daemon", "ROLE_RTC");
    }
    
    public void testGetGrantedAuthoritiesOther() {
        doSingleAuthority("Other", "Other");
    }
    
    public void testGetGrantedAuthoritiesMultiple() {
        UserAttributeLdapAuthoritiesPopulator populator = init();
        //LdapUserDetails userDetails = new OurLdapUserDetailsImpl();
        
        LdapUserDetails userDetails = createMock(LdapUserDetails.class);
        Attributes attributes = new BasicAttributes(true);
        Attribute attribute = new BasicAttribute(s_userAttribute);
        attribute.add("OpenNMS Administrator");
        attribute.add("OpenNMS User");
        attributes.put(attribute);
        expect(userDetails.getDn()).andReturn(s_dn).anyTimes();
        expect(userDetails.getAttributes()).andReturn(attributes);
        
        replay(userDetails);
        GrantedAuthority[] authorities =
            populator.getGrantedAuthorities(userDetails);
        verify(userDetails);
        
        assertNotNull("GrantedAuthority[] list returned from "
                      + "getGrantedAuthorities cannot be null", authorities);
        assertEquals("Incorrect number of entries in the GrantedAuthority list",
                     2, authorities.length); 
        assertEquals("User that we were expecting", "ROLE_ADMIN", authorities[0].getAuthority());
        assertEquals("User that we were expecting", "ROLE_USER", authorities[1].getAuthority());
    }
    
    public UserAttributeLdapAuthoritiesPopulator init() {
        return new UserAttributeLdapAuthoritiesPopulator(s_userAttribute);
    }
    
    public void doSingleAuthority(String in, String out) {
        UserAttributeLdapAuthoritiesPopulator populator = init();
        
        LdapUserDetails userDetails = createMock(LdapUserDetails.class);
        Attributes attributes = new BasicAttributes(s_userAttribute, in, true);
        expect(userDetails.getDn()).andReturn(s_dn).anyTimes();
        expect(userDetails.getAttributes()).andReturn(attributes);
        
        replay(userDetails);
        GrantedAuthority[] authorities =
            populator.getGrantedAuthorities(userDetails);
        verify(userDetails);
        
        assertNotNull("GrantedAuthority[] list returned from "
                      + "getGrantedAuthorities cannot be null", authorities);
        assertEquals("Incorrect number of entries in the GrantedAuthority list",
                     1, authorities.length); 
        assertEquals("User that we were expecting", out, authorities[0].getAuthority());
    }

}
