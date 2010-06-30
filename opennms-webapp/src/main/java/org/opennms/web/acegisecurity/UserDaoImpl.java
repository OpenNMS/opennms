//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2004 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jun 14: Use CastorUtils.unmarshal and eliminate a needless
//              get*Collection method. - dj@opennms.org
//
// Copyright (C) 2004 Eric Molitor (eric@tuxbot.com)
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.BundleLists;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataRetrievalFailureException;
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
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="mailto:eric@tuxbot.com">Eric Molitor</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="mailto:eric@tuxbot.com">Eric Molitor</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version $Id: $
 * @since 1.6.12
 */
public class UserDaoImpl implements UserDao, InitializingBean {
    private static final UpperCaseMd5PasswordEncoder PASSWORD_ENCODER = new UpperCaseMd5PasswordEncoder();

    private static final GrantedAuthority ROLE_USER = new GrantedAuthorityImpl(Authentication.USER_ROLE);

    private String m_usersConfigurationFile;
    
    private String m_groupsConfigurationFile;
	
    /**
     * The set of valid users from users.xml, keyed by userId
     */
    private Map<String, org.opennms.web.acegisecurity.User> m_users = null;
    
    private long m_usersLastModified;

    private String m_magicUsersConfigurationFile;
	
    /**
     * The set of valid users from magic-users.properties, keyed by userId
     */
    private Map<String, org.opennms.web.acegisecurity.User> m_magicUsers = null;

    private Map<String, GrantedAuthority[]> m_roles = null;
    
    private long m_magicUsersLastModified;

    private long m_groupsLastModified;

    private boolean m_useGroups;

    /**
     * <p>Constructor for UserDaoImpl.</p>
     */
    public UserDaoImpl() {
    }
    
    private Userinfo unmarshallUsers() throws DataRetrievalFailureException {
        FileInputStream in;
        try {
            in = new FileInputStream(m_usersConfigurationFile);
        } catch (FileNotFoundException e) {
            throw new DataRetrievalFailureException("Could not find configuration file '" + m_usersConfigurationFile + "': " + e.getMessage(), e);
        }

        Reader reader = new InputStreamReader(in);
        return unmarshall(reader);
    }

    private Userinfo unmarshall(Reader reader) throws DataRetrievalFailureException {
        try {
            return CastorUtils.unmarshal(Userinfo.class, reader);
        } catch (MarshalException e) {
            throw new DataRetrievalFailureException("Failed unmarshalling configuration: " + e.getMessage(), e);
        } catch (ValidationException e) {
            throw new DataRetrievalFailureException("Failed validating configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Convenience method for parsing the users.xml file.
     * <p/>
     * <p>This method is synchronized so only one thread at a time
     * can parse the users.xml file and create the <code>principal</code>
     * instance variable.</p>
     */
    private void parseUsers() throws DataRetrievalFailureException {
        HashMap<String, org.opennms.web.acegisecurity.User> users = new HashMap<String, org.opennms.web.acegisecurity.User>();

        long lastModified = new File(m_usersConfigurationFile).lastModified();
        Userinfo userinfo = unmarshallUsers();

        Collection<User> usersList = userinfo.getUsers().getUserCollection();

        for (User user : usersList) {
            org.opennms.web.acegisecurity.User newUser = new org.opennms.web.acegisecurity.User();
            newUser.setUsername(user.getUserId());
            newUser.setPassword(user.getPassword());

            users.put(user.getUserId(), newUser);
        }

        log().debug("Loaded the users.xml file with " + users.size() + " users");


        m_usersLastModified = lastModified; 
        m_users = users;
    }
    
    /**
     * Parses the groups.xml file into mapping roles to users of that role
     * through group membership.
     */
    private Map<String, LinkedList<String>> parseGroupRoles()
            throws DataRetrievalFailureException {
        long lastModified = new File(m_groupsConfigurationFile).lastModified();
        
        try {
            GroupFactory.init();
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Error reading groups configuration file '" + m_groupsConfigurationFile + "': " + e.getMessage(), e);
        }
        GroupManager gm = GroupFactory.getInstance();
        Map<String, LinkedList<String>> roleMap = new HashMap<String, LinkedList<String>>();

        Collection<Role> roles = gm.getRoles();
        for (Role role : roles) {
            String groupname = role.getMembershipGroup();
            String acegiRole = Authentication.getAcegiRoleFromOldRoleName(role.getName());
            if (acegiRole != null) {
                List<String> users;
                try {
                    users = gm.getGroup(groupname).getUserCollection();
                } catch (Exception e) {
                    throw new DataRetrievalFailureException("Error reading groups configuration file '" + m_groupsConfigurationFile + "': " + e.getMessage(), e);
                }

                for (String user : users) {
                    if (roleMap.get(user) == null) {
                        roleMap.put(user, new LinkedList<String>());
                    }
                    LinkedList<String> userRoleList = roleMap.get(user);
                    userRoleList.add(acegiRole);
                }
            }
        }

        log().debug("Loaded roles from groups.xml file for " + roleMap.size() + " users");

        m_groupsLastModified = lastModified;

        return roleMap;
    }

    /**
     * Parses the magic-users.properties file into two mappings: from magic
     * username to password, and from magic role to authorized users of that
     * role.
     */
    private void parseMagicUsers() throws DataRetrievalFailureException {
        HashMap<String, org.opennms.web.acegisecurity.User> magicUsers = new HashMap<String, org.opennms.web.acegisecurity.User>();
        HashMap<String, GrantedAuthority[]> roles = new HashMap<String, GrantedAuthority[]>();

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

        for (String user : configuredUsers ) {
            String username = properties.getProperty("user." + user + ".username");
            String password = properties.getProperty("user." + user + ".password");

            org.opennms.web.acegisecurity.User newUser = new org.opennms.web.acegisecurity.User();
            newUser.setUsername(username);
            newUser.setPassword(PASSWORD_ENCODER.encodePassword(password, null));

            magicUsers.put(username, newUser);
        }

        String[] configuredRoles = BundleLists.parseBundleList(properties.getProperty("roles"));
        // Use roles from the groups.xml file if specified in applicationContext-acegi-security.xml
        Map<String, LinkedList<String>> roleMap = m_useGroups ? parseGroupRoles() 
                                                              : new HashMap<String, LinkedList<String>>();
        Map<String, Boolean> roleAddDefaultMap = new HashMap<String, Boolean>();
        for (String role : configuredRoles) {
            String rolename = properties.getProperty("role." + role + ".name");
            if (rolename == null) {
                throw new DataRetrievalFailureException("Role configuration for '" + role + "' does not have 'name' parameter.  Expecting a 'role." + role + ".name' property");
            }

            String userList = properties.getProperty("role." + role + ".users");
            if (userList == null) {
                throw new DataRetrievalFailureException("Role configuration for '" + role + "' does not have 'users' parameter.  Expecting a 'role." + role + ".users' property");
            }
            String[] authUsers = BundleLists.parseBundleList(userList);

            boolean notInDefaultGroup = "true".equals(properties.getProperty("role." + role + ".notInDefaultGroup"));

            String acegiRole = Authentication.getAcegiRoleFromOldRoleName(rolename);
            if (acegiRole == null) {
                throw new DataRetrievalFailureException("Could not find Acegi Security role mapping for old role name '" + rolename + "' for role '" + role + "'");
            }

            for (String authUser : authUsers) {
                if (roleMap.get(authUser) == null) {
                    roleMap.put(authUser, new LinkedList<String>());
                }
                LinkedList<String> userRoleList = roleMap.get(authUser); 
                userRoleList.add(acegiRole);
            }
            
            roleAddDefaultMap.put(acegiRole, !notInDefaultGroup);
        }

        for (String user : roleMap.keySet()) {
            roles.put(user, getAuthorityListFromRoleList(roleMap.get(user), roleAddDefaultMap));
        }
        
        log().debug("Loaded the magic-users.properties file with " + magicUsers.size() + " magic users, " + configuredRoles.length + " roles, and " + roles.size() + " user roles");


        m_magicUsersLastModified = lastModified; 
        m_magicUsers = magicUsers;
        m_roles = roles;
    }

    private GrantedAuthority[] getAuthorityListFromRoleList(LinkedList<String> roleList, Map<String, Boolean> roleAddDefaultMap) {
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
            authorities.add(new GrantedAuthorityImpl(role));
        }

        return authorities.toArray(new GrantedAuthority[authorities.size()]);
    }

    /**
     * <p>getAuthoritiesByUsername</p>
     *
     * @param user a {@link java.lang.String} object.
     * @return an array of {@link org.acegisecurity.GrantedAuthority} objects.
     */
    protected GrantedAuthority[] getAuthoritiesByUsername(String user) {
        if (m_roles.containsKey(user)) {
            return m_roles.get(user);
        } else {
            return new GrantedAuthority[] { ROLE_USER };
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
        }

        if (m_usersLastModified != new File(m_usersConfigurationFile).lastModified()) {
            return true;
        }

        return false;
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
        }

        return false;
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
        }

        if (m_magicUsersLastModified != new File(m_magicUsersConfigurationFile).lastModified()) {
            return true;
        }

        return false;
    }

    /**
     * <p>setUsersConfigurationFile</p>
     *
     * @param usersConfigurationFile a {@link java.lang.String} object.
     */
    public void setUsersConfigurationFile(String usersConfigurationFile) {
        m_usersConfigurationFile = usersConfigurationFile;
    }
    
    /**
     * <p>setGroupsConfigurationFile</p>
     *
     * @param groupsConfigurationFile a {@link java.lang.String} object.
     */
    public void setGroupsConfigurationFile(String groupsConfigurationFile) {
        m_groupsConfigurationFile = groupsConfigurationFile;
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
    public org.opennms.web.acegisecurity.User getByUsername(String username) {
        reloadIfNecessary();

        org.opennms.web.acegisecurity.User user;
        if (m_magicUsers.containsKey(username)) {
            user = m_magicUsers.get(username);
        } else {
            user = m_users.get(username);
        }

        if (user == null) {
            return null;
        }

        user.setAuthorities(getAuthoritiesByUsername(username));

        return user;
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
     * Returns the Log4J category for logging web authentication messages.
     */
    private final Category log() {
        return ThreadCategory.getInstance(getClass());
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
    
    /**
     * <p>afterPropertiesSet</p>
     */
    public void afterPropertiesSet() {
        Assert.state(m_usersConfigurationFile != null, "usersConfigurationFile parameter must be set to the location of the users.xml configuration file");
        Assert.state(!m_useGroups || m_groupsConfigurationFile != null, "groupsConfigurationFile parameter must be set to the location of the groups.xml configuration file");
        Assert.state(m_magicUsersConfigurationFile != null, "magicUsersConfigurationFile parameter must be set to the location of the magic-users.properties configuration file");
    }
}
