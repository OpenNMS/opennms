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

    private String m_userAttribute;

    public UserAttributeLdapAuthoritiesPopulator(String userAttribute) {
        Assert.notNull(userAttribute, "UserAttribute can not be null");
        m_userAttribute = userAttribute;
    }

    @Deprecated
    public UserAttributeLdapAuthoritiesPopulator(InitialDirContextFactory initialDirContextFactory, String userAttribute) {
        this(userAttribute);
    }

    public GrantedAuthority[] getGrantedAuthorities(LdapUserDetails userDetails) throws LdapDataAccessException {
        Assert.notNull(userDetails, "UserDetails can not be null");

        Attributes attributes = userDetails.getAttributes();
        Assert.notNull(attributes, "Attributes in the UserDetails object cannot be null");
        
        Attribute attribute = attributes.get(m_userAttribute);
        if (attribute == null) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("User '" + userDetails.getDn() + "' does not have attribute '" + m_userAttribute + "'.  Returning empty GrantedAuthority list.");
            }
            return new GrantedAuthority[0];
        }
        
        List<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>();
        try {
            NamingEnumeration enumeration = attribute.getAll();
            while (enumeration.hasMore()) {
                Object o = enumeration.next();
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Got '" + m_userAttribute + "' value for user '" + userDetails.getDn() + "': '" + o + "'");
                }

                if (o == null) {
                    continue;
                }
                String s = o.toString();

                if ("OpenNMS User".equals(s)) {
                    addAuthority(userDetails, authorities, "ROLE_USER");
                } else if ("OpenNMS Administrator".equals(s)) {
                    addAuthority(userDetails, authorities, "ROLE_ADMIN");
                } else if ("OpenNMS RTC Daemon".equals(s)) {
                    addAuthority(userDetails, authorities, "ROLE_RTC");
                } else {
                    addAuthority(userDetails, authorities, s);
                }
            }
        } catch (NamingException e) {
            if (s_logger.isWarnEnabled()) {
                s_logger.warn("Received NamingException: " + e.getMessage(), e);
            }
            throw new LdapDataAccessException("Received naming exception while iterating through values for '" + m_userAttribute + "' attribute.", e);
        }
        
        return authorities.toArray(new GrantedAuthority[0]);
    }

    private void addAuthority(LdapUserDetails userDetails, List<GrantedAuthority> authorities, String authority) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Adding authority '" + authority + "' to user '" + userDetails.getDn() + "'");
        }

        authorities.add(new GrantedAuthorityImpl(authority));
    }
}
