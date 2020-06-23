/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.springframework.security;

import java.util.Collection;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.kerberos.authentication.KerberosAuthenticationProvider;
import org.springframework.security.kerberos.authentication.KerberosClient;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

/**
 * Authentication provider which validates users via Kerberos credentials
 * and then populates the users' authorities using the specified implementation
 * of LdapAuthoritiesPopulator. Created for use cases where no existing
 * UserDetailsService implementation will quite do the job.
 * 
 * @author Jeff Gehlbach <jeffg@opennms.org>
 * @see KerberosServiceLdapAuthenticationProvider
 * @see KerberosServiceAuthenticationProvider
 * @see LdapUserSearch
 * @see LdapAuthoritiesPopulator
 */
public class KerberosLdapAuthenticationProvider extends KerberosAuthenticationProvider {
    private KerberosClient m_kerberosClient;
    private LdapUserSearch m_ldapUserSearch;
    private LdapAuthoritiesPopulator m_ldapAuthoritiesPopulator;
    private boolean m_trimRealm = true;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) authentication;
        
        /*
         * The incoming username will be in the form of a Kerberos user principal name,
         * e.g. user@EXAMPLE.ORG. We typically need to strip off the realm name before
         * doing any LDAP operations with the username.
         */
        String validatedUsername = trimRealmFromUsername(m_kerberosClient.login(auth.getName(), auth.getCredentials().toString()));

        DirContextOperations ldapUserEntry = m_ldapUserSearch.searchForUser(validatedUsername);
        Collection<? extends GrantedAuthority> grantedAuthorities = m_ldapAuthoritiesPopulator.getGrantedAuthorities(ldapUserEntry, validatedUsername);
        
        UserDetails userDetails = new User(validatedUsername, "notUsed", true, true, true, true, grantedAuthorities);
        UsernamePasswordAuthenticationToken output = new UsernamePasswordAuthenticationToken(userDetails, auth.getCredentials(), grantedAuthorities);
        return output;
    }
    
    private String trimRealmFromUsername(final String username) {
        if (m_trimRealm && username.contains("@")) {
            return username.substring(0, username.indexOf("@"));
        }
        return username;
    }
    
    /**
     * 
     * @param ldapAuthoritiesPopulator The LdapAuthoritiesPopulator to use
     * for retrieving granted authorities from an LDAP directory
     */
    public void setLdapAuthoritiesPopulator(LdapAuthoritiesPopulator ldapAuthoritiesPopulator) {
        m_ldapAuthoritiesPopulator = ldapAuthoritiesPopulator;
    }
    
    public LdapAuthoritiesPopulator getLdapAuthoritiesPopulator() {
        return m_ldapAuthoritiesPopulator;
    }
    
    /**
     * 
     * @param ldapUserSearch The LdapUserSearch with which to look up
     * users in an LDAP directory
     */
    public void setLdapUserSearch(LdapUserSearch ldapUserSearch) {
        m_ldapUserSearch = ldapUserSearch;
    }
    
    public LdapUserSearch getLdapUserSearch() {
        return m_ldapUserSearch;
    }
    
    @Override
    public void setKerberosClient(KerberosClient kerberosClient) {
        m_kerberosClient = kerberosClient;
    }
    
    public KerberosClient getKerberosClient() {
        return m_kerberosClient;
    }
    
    /**
     * 
     * @param trimRealm Defaults to true. If set to false, do not trim the
     * realm portion (e.g. @EXAMPLE.ORG) from the authenticated user principal
     * name (e.g. user@EXAMPLE.ORG).
     */
    public void setTrimRealm(boolean trimRealm) {
        m_trimRealm = trimRealm;
    }
    
    public boolean getTrimRealm() {
        return m_trimRealm;
    }
}
