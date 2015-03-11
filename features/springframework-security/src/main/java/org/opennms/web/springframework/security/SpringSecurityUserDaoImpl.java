/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.opennms.core.utils.BundleLists;
import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.web.api.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;

/**
 * Implements the interface to allow Tomcat to check our users.xml file
 * to authenticate users.
 * <p/>
 * <p>This class is Tomcat-specific and will not be portable to other
 * servlet containers. It relies on packages supplied with Tomcat.</p>
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="mailto:eric@tuxbot.com">Eric Molitor</A>
 */
public class SpringSecurityUserDaoImpl implements SpringSecurityUserDao, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(SpringSecurityUserDaoImpl.class);
    private UserManager m_userManager;

    private GroupManager m_groupManager;

    private String m_usersConfigurationFile;
    
    private String m_groupsConfigurationFile;
	
    /**
     * The set of valid users from users.xml, keyed by userId
     */
    private Map<String, OnmsUser> m_users = null;
    
    private long m_usersLastModified;

    private String m_magicUsersConfigurationFile;
	
    /**
     * The set of valid users from magic-users.properties, keyed by userId
     */
    private Map<String, OnmsUser> m_magicUsers = null;

    private Map<String, Collection<? extends GrantedAuthority>> m_roles = null;
    
    private Map<String,GrantedAuthority> m_authorities = new HashMap<>();

    private long m_magicUsersLastModified;

    private long m_groupsLastModified;

    private boolean m_useGroups;

    /**
     * <p>Constructor for SpringSecurityUserDaoImpl.</p>
     */
    public SpringSecurityUserDaoImpl() {
    }
    
    /**
     * Convenience method for parsing the users.xml file.
     * <p/>
     * <p>This method is synchronized so only one thread at a time
     * can parse the users.xml file and create the <code>principal</code>
     * instance variable.</p>
     */
    private void parseUsers() throws DataRetrievalFailureException {
        final HashMap<String, OnmsUser> users = new HashMap<String, OnmsUser>();

        try {
            for (final OnmsUser user : m_userManager.getOnmsUserList()) {
                users.put(user.getUsername(), user);
            }
        } catch (final Throwable t) {
            throw new DataRetrievalFailureException("Unable to get user list.", t);
        }

        LOG.debug("Loaded the users.xml file with {} users", users.size());

        m_usersLastModified = m_userManager.getLastModified();
        m_users = users;
    }
    
    /**
     * Parses the groups.xml file into mapping roles to users of that role
     * through group membership.
     */
    private Map<String, LinkedList<String>> parseGroupRoles() throws DataRetrievalFailureException {
        long lastModified = new File(m_groupsConfigurationFile).lastModified();
        
        final Map<String, LinkedList<String>> roleMap = new HashMap<String, LinkedList<String>>();

        final Collection<Role> roles = m_groupManager.getRoles();
        for (final Role role : roles) {
            final String groupname = role.getMembershipGroup();
            final String securityRole = Authentication.getSpringSecurityRoleFromOldRoleName(role.getName());
            if (securityRole != null) {
                final List<String> users;
                try {
                    users = m_groupManager.getGroup(groupname).getUserCollection();
                } catch (Throwable e) {
                    throw new DataRetrievalFailureException("Error reading groups configuration file '" + m_groupsConfigurationFile + "': " + e.getMessage(), e);
                }

                for (final String user : users) {
                    if (roleMap.get(user) == null) {
                        roleMap.put(user, new LinkedList<String>());
                    }
                    final LinkedList<String> userRoleList = roleMap.get(user);
                    userRoleList.add(securityRole);
                }
            }
        }

        LOG.debug("Loaded roles from groups.xml file for {} users", roleMap.size());

        m_groupsLastModified = lastModified;

        return roleMap;
    }

    /**
     * Parses the magic-users.properties file into two mappings: from magic
     * username to password, and from magic role to authorized users of that
     * role.
     */
    public void parseMagicUsers() throws DataRetrievalFailureException {
        HashMap<String, OnmsUser> magicUsers = new HashMap<String, OnmsUser>();
        Map<String, Collection<? extends GrantedAuthority>> roles = new HashMap<String, Collection<? extends GrantedAuthority>>();

        long lastModified = new File(m_magicUsersConfigurationFile).lastModified();

        // read the file
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(m_magicUsersConfigurationFile));
        } catch (FileNotFoundException e) {
            throw new DataRetrievalFailureException("Magic users configuration file '" + m_magicUsersConfigurationFile + "' not found: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DataRetrievalFailureException("Error reading magic users configuration file '" + m_magicUsersConfigurationFile + "': " + e.getMessage(), e);
        }

        // look up users and their passwords
        String[] configuredUsers = BundleLists.parseBundleList(properties.getProperty("users"));

        for (String user : configuredUsers) {
            String username = properties.getProperty("user." + user + ".username");
            String password = properties.getProperty("user." + user + ".password");

            OnmsUser newUser = null;
            try {
                newUser = m_userManager.getOnmsUser(user);
            } catch (final Exception ioe) {
                throw new DataRetrievalFailureException("Unable to read user " + user + " from users.xml", ioe);
            }
            
            if (newUser == null) {
                newUser = new OnmsUser();
                newUser.setUsername(username);
                newUser.setPassword(m_userManager.encryptedPassword(password, true));
                newUser.setPasswordSalted(true);
            }

            magicUsers.put(username, newUser);
        }

        String[] configuredRoles = BundleLists.parseBundleList(properties.getProperty("roles"));
        // Use roles from the groups.xml file if specified in applicationContext-spring-security.xml
        Map<String, LinkedList<String>> roleMap = m_useGroups ? parseGroupRoles() 
                                                              : new HashMap<String, LinkedList<String>>();
        Map<String, Boolean> roleAddDefaultMap = new HashMap<String, Boolean>();
        for (String role : configuredRoles) {
            String rolename = properties.getProperty("role." + role + ".name");
            if (rolename == null) {
                  LOG.warn("Role configuration for '{}' does not have 'name' parameter.  Expecting a 'role.{}.name' property. The role will not be usable.", role, role);
                  continue;
            }

            String userList = properties.getProperty("role." + role + ".users");
            if (userList == null) {
                LOG.warn("Role configuration for '{}' does not have 'users' parameter.  Expecting a 'role.{}.users' property. The role will not be usable.", role, role);
                continue;
            }
            String[] authUsers = BundleLists.parseBundleList(userList);

            boolean notInDefaultGroup = "true".equals(properties.getProperty("role." + role + ".notInDefaultGroup"));

            String securityRole = Authentication.getSpringSecurityRoleFromOldRoleName(rolename);
            if (securityRole == null) {
                throw new DataRetrievalFailureException("Could not find Spring Security role mapping for old role name '" + rolename + "' for role '" + role + "'");
            }

            for (String authUser : authUsers) {
                if (roleMap.get(authUser) == null) {
                    roleMap.put(authUser, new LinkedList<String>());
                }
                LinkedList<String> userRoleList = roleMap.get(authUser); 
                userRoleList.add(securityRole);
            }
            
            roleAddDefaultMap.put(securityRole, !notInDefaultGroup);
        }

        for (final Entry<String, LinkedList<String>> entry : roleMap.entrySet()) {
            roles.put(entry.getKey(), getAuthorityListFromRoleList(entry.getValue(), roleAddDefaultMap));
        }
        
        LOG.debug("Loaded the magic-users.properties file with {} magic users, {} roles, and {} user roles", magicUsers.size(), configuredRoles.length, roles.size());


        m_magicUsersLastModified = lastModified; 
        m_magicUsers = magicUsers;
        m_roles = roles;
    }

    private Collection<? extends GrantedAuthority> getAuthorityListFromRoleList(List<String> roleList, Map<String, Boolean> roleAddDefaultMap) {
        boolean addToDefaultGroup = false;
        
        for (String role : roleList) {
            if (Boolean.TRUE.equals(roleAddDefaultMap.get(role))) {
                addToDefaultGroup = true;
                break;
            }
        }
        
        List<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>();
        if (addToDefaultGroup) {
            authorities.add(ROLE_USER);
        }

        for (String role : roleList) {
            authorities.add(getAuthority(role));
        }

        return authorities;
    }

    protected GrantedAuthority getAuthority(final String role) {
        if (!m_authorities.containsKey(role)) {
            m_authorities.put(role, new SimpleGrantedAuthority(role));
        }
        return m_authorities.get(role);
    }

    /**
     * <p>getAuthoritiesByUsername</p>
     *
     * @param username a {@link java.lang.String} object.
     * @return an array of {@link org.springframework.security.GrantedAuthority} objects.
     */
    protected Collection<? extends GrantedAuthority> getAuthoritiesByUsername(final String username) {
        if (m_roles.containsKey(username)) {
            final Collection<? extends GrantedAuthority> roles = m_roles.get(username);
            LOG.debug("User {} has roles: {}", username, roles);
            return roles;
        } else {
            final List<GrantedAuthority> roles = Arrays.asList(new GrantedAuthority[] { ROLE_USER });
            LOG.debug("User {} has roles: {}", username, roles);
            return roles;
        }
    }

    /**
     * Checks the last modified time of the user file against
     * the last known last modified time. If the times are different, then the
     * file must be reparsed.
     * 
     * <p>
     * Note that the <code>lastModified</code> variables are not set here.
     * This is in case there is a problem parsing either file. If we set the
     * value here, and then try to parse and fail, then we will not try to parse
     * again until the file changes again. Instead, when we see the file
     * changes, we continue parsing attempts until the parsing succeeds.
     * </p>
     */
    private boolean isUsersParseNecessary() {
        if (m_users == null) {
            return true;
        } else {
            return m_usersLastModified != m_userManager.getLastModified();
        }
    }
    
    /**
     * Checks the last modified time of the group file against
     * the last known last modified time. If the times are different, then the
     * file must be reparsed.
     * 
     * <p>
     * Note that the <code>lastModified</code> variables are not set here.
     * This is in case there is a problem parsing either file. If we set the
     * value here, and then try to parse and fail, then we will not try to parse
     * again until the file changes again. Instead, when we see the file
     * changes, we continue parsing attempts until the parsing succeeds.
     * </p>
     */
    private boolean isGroupsParseNecessary() {
        if (m_groupsLastModified != new File(m_groupsConfigurationFile).lastModified()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks the last modified time of the magic-users file against
     * the last known last modified time. If the times are different, then the
     * file must be reparsed.
     * 
     * <p>
     * Note that the <code>lastModified</code> variables are not set here.
     * This is in case there is a problem parsing either file. If we set the
     * value here, and then try to parse and fail, then we will not try to parse
     * again until the file changes again. Instead, when we see the file
     * changes, we continue parsing attempts until the parsing succeeds.
     * </p>
     */
    private boolean isMagicUsersParseNecessary() {
        if (m_magicUsers == null) {
            return true;
        } else if (m_magicUsersLastModified != new File(m_magicUsersConfigurationFile).lastModified()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>setUsersConfigurationFile</p>
     *
     * @param usersConfigurationFile a {@link java.lang.String} object.
     */
    public void setUsersConfigurationFile(String usersConfigurationFile) {
        m_usersConfigurationFile = usersConfigurationFile;
        UserFactory.setInstance(null);
    }
    
    /**
     * <p>setGroupsConfigurationFile</p>
     *
     * @param groupsConfigurationFile a {@link java.lang.String} object.
     */
    public void setGroupsConfigurationFile(String groupsConfigurationFile) {
        m_groupsConfigurationFile = groupsConfigurationFile;
        GroupFactory.setInstance(null);
    }
    
    /**
     * <p>setUseGroups</p>
     *
     * @param useGroups a boolean.
     */
    public void setUseGroups(boolean useGroups){
        m_useGroups = useGroups;
    }

    /**
     * <p>getUsersConfigurationFile</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUsersConfigurationFile() {
        return m_usersConfigurationFile;
    }

    /**
     * <p>setMagicUsersConfigurationFile</p>
     *
     * @param magicUsersConfigurationFile a {@link java.lang.String} object.
     */
    public void setMagicUsersConfigurationFile(String magicUsersConfigurationFile) {
        m_magicUsersConfigurationFile = magicUsersConfigurationFile;
    }

    /**
     * <p>getMagicUsersConfigurationFile</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMagicUsersConfigurationFile() {
        return m_magicUsersConfigurationFile;
    }

    /** {@inheritDoc} */
    @Override
    public SpringSecurityUser getByUsername(String username) {
        reloadIfNecessary();

        final OnmsUser user;
        if (m_magicUsers.containsKey(username)) {
            user = m_magicUsers.get(username);
        } else {
            user = m_users.get(username);
        }

        if (user == null) {
            return null;
        }

        final SpringSecurityUser springUser = new SpringSecurityUser(user);
        springUser.setAuthorities(getAuthoritiesByUsername(username));
        return springUser;
    }

    private void reloadIfNecessary() {
        if (isUsersParseNecessary()) {
            parseUsers();
        }

        if (isMagicUsersParseNecessary() || (m_useGroups && isGroupsParseNecessary())) {
            parseMagicUsers();
        }
    }

    /**
     * <p>getMagicUsersLastModified</p>
     *
     * @return a long.
     */
    public long getMagicUsersLastModified() {
        return m_magicUsersLastModified;
    }

    /**
     * <p>getUsersLastModified</p>
     *
     * @return a long.
     */
    public long getUsersLastModified() {
        return m_usersLastModified;
    }
    
    /**
     * <p>getGroupsLastModified</p>
     *
     * @return a long.
     */
    public long getGroupsLastModified() {
        return m_groupsLastModified;
    }
    
    /**
     * <p>isUseGroups</p>
     *
     * @return a boolean.
     */
    public boolean isUseGroups() {
        return m_useGroups;
    }

    public UserManager getUserManager() {
        return m_userManager;
    }

    public void setUserManager(final UserManager mgr) {
        m_userManager = mgr;
    }

    public GroupManager getGroupManager() {
        return m_groupManager;
    }

    public void setGroupManager(final GroupManager mgr) {
        m_groupManager = mgr;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_usersConfigurationFile != null, "usersConfigurationFile parameter must be set to the location of the users.xml configuration file");
        Assert.state(!m_useGroups || m_groupsConfigurationFile != null, "groupsConfigurationFile parameter must be set to the location of the groups.xml configuration file");
        Assert.state(m_magicUsersConfigurationFile != null, "magicUsersConfigurationFile parameter must be set to the location of the magic-users.properties configuration file");
        Assert.notNull(m_userManager);
        Assert.notNull(m_groupManager);
    }
}
