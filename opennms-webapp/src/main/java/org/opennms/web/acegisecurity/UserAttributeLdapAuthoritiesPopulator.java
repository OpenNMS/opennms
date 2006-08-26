package org.opennms.web.acegisecurity;

import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.ldap.InitialDirContextFactory;
import org.acegisecurity.ldap.LdapDataAccessException;
import org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

public class UserAttributeLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {
    
    private static final Log s_logger = LogFactory.getLog(UserAttributeLdapAuthoritiesPopulator.class);

    private InitialDirContextFactory m_initialDirContextFactory;
    private String m_userAttribute;

    public UserAttributeLdapAuthoritiesPopulator(InitialDirContextFactory initialDirContextFactory, String userAttribute) {
        Assert.notNull(initialDirContextFactory, "InitialDirContextFactory can not be null");
        Assert.notNull(userAttribute, "UserAttribute can not be null");
        m_initialDirContextFactory = initialDirContextFactory;
        m_userAttribute = userAttribute;
    }

    public GrantedAuthority[] getGrantedAuthorities(LdapUserDetails userDetails) throws LdapDataAccessException {
        Assert.notNull(userDetails, "UserDetails can not be null");

        Attributes attributes = userDetails.getAttributes();
        Assert.notNull(attributes, "Attributes in the UserDetails object cannot be null");
        
        Attribute attribute = attributes.get(m_userAttribute);
        if (attribute == null) {
            s_logger.info("User '" + userDetails.getDn() + "' does not have '" + m_userAttribute + "'.  Returning empty list.");
            return new GrantedAuthority[0];
        }
        
        List<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>();
        try {
            NamingEnumeration enumeration = attribute.getAll();
            while (enumeration.hasMore()) {
                Object o = enumeration.next();
                s_logger.debug("Got '" + m_userAttribute + "' value for user '" + userDetails.getDn() + "': '" + o + "'");

                String s;
                if (o == null) {
                    s = null;
                } else {
                    s = o.toString();
                }
                if ("OpenNMS User".equals(s)) {
                    authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
                } else if ("OpenNMS Administrator".equals(s)) {
                    authorities.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
                } else if ("OpenNMS RTC".equals(s)) {
                    authorities.add(new GrantedAuthorityImpl("ROLE_RTC"));
                } else {
                    authorities.add(new GrantedAuthorityImpl(s));
                }
            }
        } catch (NamingException e) {
            s_logger.info("Received NamingException: " + e.getMessage(), e);
            throw new LdapDataAccessException("Received naming exception while iterating through values for '" + m_userAttribute + "' attribute.", e);
        }
        
        return authorities.toArray(new GrantedAuthority[0]);
    }
}
