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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.BundleLists;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.springframework.dao.DataRetrievalFailureException;


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
 */
public class UserDaoImpl implements UserDao {
	private static final UpperCaseMd5PasswordEncoder s_encoder = new UpperCaseMd5PasswordEncoder();
	private static final GrantedAuthority s_roleUser = new GrantedAuthorityImpl(Authentication.USER_ROLE);

	private String m_usersConfigurationFile;
	
    /**
     * The set of valid users from users.xml, keyed by userId
     */
    private Map<String, org.opennms.web.acegisecurity.User> m_users = null;
    
    private long m_usersLastModified;
    

	private String m_magicUsersConfigurationFile;
	
	private String m_foo;

	/**
     * The set of valid users from magic-users.properties, keyed by userId
     */
    private Map<String, org.opennms.web.acegisecurity.User> m_magicUsers = null;
    private Map<String, GrantedAuthority[]> m_roles = null;
    
    private long m_magicUsersLastModified;


    /**
     * The Log4J category for logging web authentication messages.
     */
    private Category log = ThreadCategory.getInstance(getClass());

    public UserDaoImpl() {
    }
    
    private Userinfo unmarshallUsers() throws DataRetrievalFailureException {
		if (m_usersConfigurationFile == null) {
			// XXX there must be a better way to do this
			throw new IllegalStateException("usersConfigurationFile parameter must be set to the location of the users.xml configuration file");
		}
		
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
    		return (Userinfo) Unmarshaller.unmarshal(Userinfo.class, reader);
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

        Collection usersList = userinfo.getUsers().getUserCollection();
        
        Iterator i = usersList.iterator();
        while (i.hasNext()) {
            User user = (User) i.next();
            org.opennms.web.acegisecurity.User newUser = new org.opennms.web.acegisecurity.User();
            newUser.setUsername(user.getUserId());
            newUser.setPassword(user.getPassword());

            users.put(user.getUserId(), newUser);
        }

        log.debug("Reloaded the users.xml file into memory");

        log.debug("Loaded " + users.size() + " users into memory");

        
        m_usersLastModified = lastModified; 
        m_users = users;
    }
    
    /**
     * Parses the magic-users.properties file into two mappings: from magic
     * username to password, and from magic role to authorized users of that
     * role.
     */
    private void parseMagicUsers() throws DataRetrievalFailureException {
        HashMap<String, org.opennms.web.acegisecurity.User> magicUsers = new HashMap<String, org.opennms.web.acegisecurity.User>();
        HashMap<String, GrantedAuthority[]> roles = new HashMap<String, GrantedAuthority[]>();

		long lastModified = new File(m_usersConfigurationFile).lastModified();

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
            newUser.setPassword(s_encoder.encodePassword(password, null));

            magicUsers.put(username, newUser);
        }

        String[] configuredRoles = BundleLists.parseBundleList(properties.getProperty("roles"));
        HashMap<String, LinkedList<String>> roleMap = new HashMap<String, LinkedList<String>>();
        for (String role : configuredRoles) {
            String rolename = properties.getProperty("role." + role + ".name");
            String[] authUsers = BundleLists.parseBundleList(properties.getProperty("role." + role + ".users"));
            
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
        }
        
        for (String user : roleMap.keySet()) {
        	roles.put(user, getAuthorityListFromRoleList(roleMap.get(user)));
        }
        
        m_magicUsersLastModified = lastModified; 
        m_magicUsers = magicUsers;
        m_roles = roles;
    }

    private GrantedAuthority[] getAuthorityListFromRoleList(LinkedList<String> roleList) {
    	GrantedAuthority[] authorities = new GrantedAuthority[roleList.size() + 1];
    	int index = 0;
    	authorities[index++] = s_roleUser;
    	
    	for (String role : roleList) {
    		authorities[index++] = new GrantedAuthorityImpl(role);
    	}

    	return authorities;
	}
    
    protected GrantedAuthority[] getAuthoritiesByUsername(String user) {
    	if (m_roles.containsKey(user)) {
    		return m_roles.get(user);
    	} else {
    		return new GrantedAuthority[] { s_roleUser };
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

	public void setUsersConfigurationFile(String usersConfigurationFile) {
		m_usersConfigurationFile = usersConfigurationFile;
	}
	
	public String getUsersConfigurationFile() {
		return m_usersConfigurationFile;
	}

	public void setMagicUsersConfigurationFile(String magicUsersConfigurationFile) {
		m_magicUsersConfigurationFile = magicUsersConfigurationFile;
	}
	
	public String getMagicUsersConfigurationFile() {
		return m_magicUsersConfigurationFile;
	}
	
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
		if (m_usersConfigurationFile == null) {
			// XXX there must be a better way to do this
			throw new IllegalStateException("usersConfigurationFile parameter must be set to the location of the users.xml configuration file");
		}
		if (m_magicUsersConfigurationFile == null) {
			// XXX there must be a better way to do this
			throw new IllegalStateException("magicUsersConfigurationFile parameter must be set to the location of the magic-users.properties configuration file");
		}
		
		if (isUsersParseNecessary()) {
			parseUsers();
		}
		
		if (isMagicUsersParseNecessary()) {
			parseMagicUsers();
		}
	}
}
