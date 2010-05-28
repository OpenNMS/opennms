/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opennms.web.springframework.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.directory.SearchControls;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.ldap.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.util.Assert;


/**
 * @author Luke Taylor
 * @version $Id: DefaultLdapAuthoritiesPopulator.java 3260 2008-08-26 12:38:02Z luke_t $
 */
public class UserGroupLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {
    //~ Static fields/initializers =====================================================================================

    private static final Log logger = LogFactory.getLog(UserGroupLdapAuthoritiesPopulator.class);

    //~ Instance fields ================================================================================================

    /**
     * A default role which will be assigned to all authenticated users if set
     */
    private GrantedAuthority defaultRole;

    private SpringSecurityLdapTemplate ldapTemplate;

    /**
     * Controls used to determine whether group searches should be performed over the full sub-tree from the
     * base DN. Modified by searchSubTree property
     */
    private SearchControls searchControls = new SearchControls();

    /**
     * The ID of the attribute which contains the role name for a group
     */
    private String groupRoleAttribute = "cn";

    /**
     * The base DN from which the search for group membership should be performed
     */
    private String groupSearchBase;

    /**
     * The pattern to be used for the user search. {0} is the user's DN
     */
    private String groupSearchFilter = "(member={0})";
    
    
    private Map<String, List<String>> groupToRoleMap = new HashMap<String, List<String>>();

    //~ Constructors ===================================================================================================

    /**
     * Constructor for group search scenarios. <tt>userRoleAttributes</tt> may still be
     * set as a property.
     *
     * @param contextSource supplies the contexts used to search for user roles.
     * @param groupSearchBase          if this is an empty string the search will be performed from the root DN of the
     *                                 context factory.
     */
    public UserGroupLdapAuthoritiesPopulator(ContextSource contextSource, String groupSearchBase) {
        Assert.notNull(contextSource, "contextSource must not be null");
        ldapTemplate = new SpringSecurityLdapTemplate(contextSource);
        ldapTemplate.setSearchControls(searchControls);
        setGroupSearchBase(groupSearchBase);
    }

    //~ Methods ========================================================================================================

    /**
     * Obtains the authorities for the user who's directory entry is represented by
     * the supplied LdapUserDetails object.
     *
     * @param user the user who's authorities are required
     * @return the set of roles granted to the user.
     */
    public final GrantedAuthority[] getGrantedAuthorities(DirContextOperations user, String username) {
        String userDn = user.getNameInNamespace();

        if (logger.isDebugEnabled()) {
            logger.debug("Getting authorities for user " + userDn);
        }

        Set<GrantedAuthority> roles = getGroupMembershipRoles(userDn, username);

        if (defaultRole != null) {
            roles.add(defaultRole);
            logger.debug("Added default role: " + defaultRole);
        }

        return (GrantedAuthority[]) roles.toArray(new GrantedAuthority[roles.size()]);
    }

    @SuppressWarnings("unchecked")
	public Set<GrantedAuthority> getGroupMembershipRoles(String userDn, String username) {
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

        if (getGroupSearchBase() == null) {
            return authorities;
        }

        logger.debug("Searching for groups for user '" + username + "', DN = " + "'" + userDn + "', with filter "
                    + groupSearchFilter + " in search base '" + getGroupSearchBase() + "'");

        Set<String> groups = ldapTemplate.searchForSingleAttributeValues(getGroupSearchBase(), groupSearchFilter,
                new String[]{userDn, username}, groupRoleAttribute);

        logger.debug("Groups from search: " + groups); 
        
        
        Set<String> roles = getRolesFromGroups(groups);

        Iterator<String> it = roles.iterator();
        while (it.hasNext()) {
            String role = (String) it.next();

            authorities.add(new GrantedAuthorityImpl(role));
        }

        return authorities;
    }
    
    protected Set<String> getRolesFromGroups(Set<String> groups) {
         
        Set<String> roles = new HashSet<String>();
        
        for(String group : groups) {
            List<String> rolesForGroup = groupToRoleMap.get(group);
            logger.debug("Checking " + group + " for an associated role");
            if (rolesForGroup != null) {
                for(String role : rolesForGroup) {
                    roles.add(role);
                    logger.debug("Added role: " + role + " based on group " + group);
                }
            }
        }
        
        return roles;
        
    }

    protected ContextSource getContextSource() {
        return ldapTemplate.getContextSource();
    }

    /**
     * Set the group search base (name to search under)
     *
     * @param groupSearchBase if this is an empty string the search will be performed from the root DN of the context
     *                        factory.
     */
    private void setGroupSearchBase(String groupSearchBase) {
        Assert.notNull(groupSearchBase, "The groupSearchBase (name to search under), must not be null.");
        this.groupSearchBase = groupSearchBase;
        if (groupSearchBase.length() == 0) {
            logger.info("groupSearchBase is empty. Searches will be performed from the context source base");
        }
    }

    protected String getGroupSearchBase() {
        return groupSearchBase;
    }
    
    public void setGroupToRoleMap(Map<String, List<String>> groupToRoleMap) {
        this.groupToRoleMap = groupToRoleMap;
    }

    /**
     * The default role which will be assigned to all users.
     *
     * @param defaultRole the role name, including any desired prefix.
     */
    public void setDefaultRole(String defaultRole) {
        Assert.notNull(defaultRole, "The defaultRole property cannot be set to null");
        this.defaultRole = new GrantedAuthorityImpl(defaultRole);
    }

    public void setGroupRoleAttribute(String groupRoleAttribute) {
        Assert.notNull(groupRoleAttribute, "groupRoleAttribute must not be null");
        this.groupRoleAttribute = groupRoleAttribute;
    }

    public void setGroupSearchFilter(String groupSearchFilter) {
        Assert.notNull(groupSearchFilter, "groupSearchFilter must not be null");
        this.groupSearchFilter = groupSearchFilter;
    }

    /**
     * If set to true, a subtree scope search will be performed. If false a single-level search is used.
     *
     * @param searchSubtree set to true to enable searching of the entire tree below the <tt>groupSearchBase</tt>.
     */
    public void setSearchSubtree(boolean searchSubtree) {
        int searchScope = searchSubtree ? SearchControls.SUBTREE_SCOPE : SearchControls.ONELEVEL_SCOPE;
        searchControls.setSearchScope(searchScope);
    }
}
