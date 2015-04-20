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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.kerberos.authentication.KerberosAuthenticationProvider;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken;
import org.springframework.security.kerberos.authentication.KerberosTicketValidation;
import org.springframework.security.kerberos.authentication.KerberosTicketValidator;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.util.Assert;

/**
 * Authentication provider which validates users via Kerberos Service Tickets
 * or SPNEGO Tokens and then populates the users' authorities using the
 * specified implementation of LdapAuthoritiesPopulator. Created for use
 * cases where no existing UserDetailsService implementation will quite do
 * the job.
 * 
 * @author Jeff Gehlbach <jeffg@opennms.org>
 * @see KerberosLdapAuthenticationProvider
 * @see KerberosAuthenticationProvider
 * @see LdapUserSearch
 * @see LdapAuthoritiesPopulator
 */
public class KerberosServiceLdapAuthenticationProvider extends KerberosServiceAuthenticationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(KerberosServiceLdapAuthenticationProvider.class);
    
    private KerberosTicketValidator m_kerberosTicketValidator;
    private UserDetailsChecker m_userDetailsChecker = new AccountStatusUserDetailsChecker();
    private LdapUserSearch m_ldapUserSearch;
    private LdapAuthoritiesPopulator m_ldapAuthoritiesPopulator;
    private boolean m_trimRealm = true;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        KerberosServiceRequestToken auth = (KerberosServiceRequestToken) authentication;
        byte[] token = auth.getToken();
        LOG.debug("Try to validate Kerberos Token");
        KerberosTicketValidation ticketValidation = m_kerberosTicketValidator.validateTicket(token);
        LOG.debug("Succesfully validated " + ticketValidation.username());
        
        /*
         * The incoming username will be in the form of a Kerberos user principal name,
         * e.g. user@EXAMPLE.ORG. We typically need to strip off the realm name before
         * doing any LDAP operations with the username.
         */
        String validatedUsername = trimRealmFromUsername(ticketValidation.username());
        DirContextOperations ldapUserEntry = m_ldapUserSearch.searchForUser(validatedUsername);
        Collection<? extends GrantedAuthority> grantedAuthorities = m_ldapAuthoritiesPopulator.getGrantedAuthorities(ldapUserEntry, validatedUsername);
        
        UserDetails userDetails = new User(validatedUsername, "notUsed", true, true, true, true, grantedAuthorities);
        m_userDetailsChecker.check(userDetails);
        additionalAuthenticationChecks(userDetails, auth);
        KerberosServiceRequestToken responseAuth = new KerberosServiceRequestToken(userDetails, ticketValidation, userDetails.getAuthorities(), token);
        return responseAuth;
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
    
    @Override
    public void setTicketValidator(KerberosTicketValidator ticketValidator) {
        m_kerberosTicketValidator = ticketValidator;
    }
    
    public KerberosTicketValidator getTicketValidator() {
        return m_kerberosTicketValidator;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_kerberosTicketValidator, "ticketValidator must be set");
        Assert.notNull(m_ldapAuthoritiesPopulator, "ldapAuthoritiesPopulator must be set");
    }
}
