package org.opennms.web.acegisecurity;

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
        GrantedAuthority[] defaultAuthorities = new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_USER") };
        // new GrantedAuthority[0];
        
        Assert.notNull(userDetails, "UserDetails can not be null");

        Attributes attributes = userDetails.getAttributes();
        Assert.notNull(attributes, "Attributes in the UserDetails object cannot be null");
        
        Attribute attribute = attributes.get(m_userAttribute);
        if (attribute == null) {
            s_logger.info("User '" + userDetails.getDn() + "' does not have '" + m_userAttribute + "'.  Returning [ROLE_USER].");
            return defaultAuthorities;
        }
        
        try {
            NamingEnumeration enumeration = attribute.getAll();
            while (enumeration.hasMore()) {
                Object o = enumeration.next();
                s_logger.info("got attribute value for user '" + userDetails.getDn() + "': '" + o + "'");
            }
        } catch (NamingException e) {
            s_logger.info("got namingexception: " + e.getMessage(), e);
        }
        
        s_logger.info("Returning default of [ROLE_USER] for '" + userDetails.getDn() + "'");
        return defaultAuthorities;
    }
}
