//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
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

/**
 * <p>UserAttributeLdapAuthoritiesPopulator class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class UserAttributeLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {
    
    private static final Log s_logger = LogFactory.getLog(UserAttributeLdapAuthoritiesPopulator.class);

    private String m_userAttribute;

    /**
     * <p>Constructor for UserAttributeLdapAuthoritiesPopulator.</p>
     *
     * @param userAttribute a {@link java.lang.String} object.
     */
    public UserAttributeLdapAuthoritiesPopulator(String userAttribute) {
        Assert.notNull(userAttribute, "UserAttribute can not be null");
        m_userAttribute = userAttribute;
    }

    /**
     * <p>Constructor for UserAttributeLdapAuthoritiesPopulator.</p>
     *
     * @param initialDirContextFactory a {@link org.acegisecurity.ldap.InitialDirContextFactory} object.
     * @param userAttribute a {@link java.lang.String} object.
     */
    @Deprecated
    public UserAttributeLdapAuthoritiesPopulator(InitialDirContextFactory initialDirContextFactory, String userAttribute) {
        this(userAttribute);
    }

    /** {@inheritDoc} */
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
