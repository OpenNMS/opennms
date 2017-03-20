/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;

/**
 * This is an interface for UserManager
 * 
 * @author <a href="ryan@mail1.opennms.com"> Ryan Lambeth </a>
 *
 */
public interface UserConfig {
	
	public enum ContactType {
        email,
        pagerEmail,
        xmppAddress,
        microblog,
        numericPage,
        textPage,
        workPhone,
        mobilePhone,
        homePhone
    }
	
	/**
	 * <p>parseXML</p>
	 * 
	 * @param an InputStream
	 */
	void parseXML(final InputStream in);
	
	/**
	 * <p>saveUser</p>
	 * 
	 * @param a String
	 * @param a User
	 * @throws Exception
	 */
	void saveUser(final String name, final User details) throws Exception;
	
	/**
	 * <p>save</p>
	 * 
	 * @param an OnmsUser
	 * @throws Exception
	 */
	void save(final OnmsUser onmsUser) throws Exception;
	
	/**
	 * <p>isUserOnDuty</p>
	 * 
	 * @param a String
	 * @param a Calendar
	 * @return a boolean
	 * @throws IOException
	 */
	boolean isUserOnDuty(final String user, final Calendar time) throws IOException;
	
	/**
	 * <p>getUsers</p>
	 * 
	 * @return a Map<String, User>
	 * @throws IOException
	 */
	Map<String, User> getUsers() throws IOException;
	
	/**
	 * <p>getOnmsUserList</p>
	 * 
	 * @return an OnmsUserList
	 * @throws IOException
	 */
	OnmsUserList getOnmsUserList() throws  IOException;
	
	/**
	 * <p>getOnmsUser</p>
	 * 
	 * @param a String
	 * @return an OnmsUser
	 * @throws IOException
	 */
	OnmsUser getOnmsUser(final String username) throws  IOException;
	
	/**
	 * <p>hasUser</p>
	 * 
	 * @param a String
	 * @return a boolean
	 * @throws IOException
	 */
	boolean hasUser(final String userName) throws IOException;
	
	/**
	 * <p>getUserNames</p>
	 * 
	 * @return a List<String>
	 * @throws IOException
	 */
	List<String> getUserNames() throws IOException;
	
	/**
	 * <p>getUser</p>
	 * 
	 * @param a String
	 * @return a User
	 * @throws IOException
	 */
	User getUser(final String name) throws IOException;
	
	/**
	 * <p>getTuiPin</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getTuiPin(final String name) throws IOException;
	
	/**
	 * <p>getTuiPin</p>
	 * 
	 * @param a User
	 * @return a String
	 * @throws IOException
	 */
	String getTuiPin(final User user) throws IOException;
	
	/**
	 * <p>getMicroblogName</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	String getMicroblogName(final String name) throws  FileNotFoundException, IOException;
	
	/**
	 * <p>setContactInfo</p>
	 * 
	 * @param a String
	 * @param a ContactType
	 * @param a String
	 * @throws Exception
	 */
	void setContactInfo(final String userId, final ContactType contactType, final String contactValue) throws Exception;
	
	/**
	 * <p>getContactInfo</p>
	 * 
	 * @param a String
	 * @param a ContactType
	 * @return a String
	 * @throws IOException
	 */
	String getContactInfo(final String userId, final ContactType contactType) throws  IOException;
	
	/**
	 * <p>getContactInfo</p>
	 * 
	 * @param a String
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getContactInfo(final String userID, final String command) throws IOException;
	
	/**
	 * <p>getContactInfo</p>
	 * 
	 * @param a User
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getContactInfo(final User user, final String command) throws IOException;
	
	/**
	 * <p>getContactServiceProvider</p>
	 * 
	 * @param a String
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getContactServiceProvider(final String userID, final String command) throws IOException;
	
	/**
	 * <p>getContactServiceProvider</p>
	 * 
	 * @param a User
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getContactServiceProvider(final User user, final String command) throws IOException;
	
	/**
	 * <p>getEmail</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getEmail(final String userID) throws IOException;
	
	/**
	 * <p>getEmail</p>
	 * 
	 * @param a User
	 * @return a String
	 * @throws IOException
	 */
	String getEmail(final User user) throws IOException;
	
	/**
	 * <p>getPagerEmail</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getPagerEmail(final String userID) throws IOException;
	
	/**
	 * <p>getPagerEmail</p>
	 * 
	 * @param a User
	 * @return a String
	 * @throws IOException
	 */
	String getPagerEmail(final User user) throws IOException;
	
	/**
	 * <p>getNumericaPin</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getNumericPin(final String userID) throws IOException;
	
	/**
	 * <p>getNumericPin</p>
	 * 
	 * @param a User
	 * @return a String
	 * @throws IOException
	 */
	String getNumericPin(final User user) throws IOException;
	
	/**
	 * <p>getXMPPAddress</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getXMPPAddress(final String userID) throws IOException;
	
	/**
	 * <p>getXMPPAddress</p>
	 * 
	 * @param a User
	 * @return a String
	 * @throws IOException
	 */
	String getXMPPAddress(final User user) throws IOException;
	
	/**
	 * <p>getNumericPage</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getNumericPage(final String userID) throws IOException;
	
	/**
	 * <p>getNumericPage</p>
	 * 
	 * @param a User
	 * @return a String
	 * @throws IOException
	 */
	String getNumericPage(final User user) throws IOException;
	
	/**
	 * <p>getTextPin</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getTextPin(final String userID) throws IOException;
	
	/**
	 * <p>getTextPin</p>
	 * 
	 * @param a User
	 * @return a String
	 * @throws IOException
	 */
	String getTextPin(final User user) throws IOException;
	
	/**
	 * <p>getTextPage</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getTextPage(final String userID) throws IOException;
	
	/**
	 * <p>getTextPage</p>
	 * 
	 * @param a User
	 * @return a String
	 * @throws IOException
	 */
	String getTextPage(final User user) throws IOException;
	
	/**
	 * <p>getWorkPhone</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getWorkPhone(final String userID) throws  IOException;
	
	/**
	 * <p>getWorkPhone</p>
	 * 
	 * @param a User
	 * @return a String
	 * @throws IOException
	 */
	String getWorkPhone(final User user) throws  IOException;
	
	/**
	 * <p>getMobilePhone</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getMobilePhone(final String userID) throws  IOException;
	
	/**
	 * <p>getMobilePhone</p>
	 * 
	 * @param a User
	 * @return a String
	 * @throws IOException
	 */
	String getMobilePhone(final User user) throws  IOException;
	
	/**
	 * <p>getHomePhone</p>
	 * 
	 * @param a String
	 * @return a String
	 * @throws IOException
	 */
	String getHomePhone(final String userID) throws  IOException;
	
	/**
	 * <p>getHomePhone</p>
	 * 
	 * @param a User
	 * @return a String
	 * @throws IOException
	 */
	String getHomePhone(final User user) throws  IOException;
	
	/**
	 * <p>saveUsers</p>
	 * 
	 * @param a Collection<User>
	 * @throws Exception
	 */
	void saveUsers(final Collection<User> usersList) throws Exception;
	
	/**
	 * <p>deleteUser</p>
	 * 
	 * @param a String
	 * @throws Exception
	 */
	void deleteUser(final String name) throws Exception;
	
	/**
	 * <p>renameUser</p>
	 * 
	 * @param a String
	 * @param a String
	 * @throws Exception
	 */
	void renameUser(final String oldName, final String newName) throws Exception;
	
	/**
	 * <p>setEncryptedPassword</p>
	 * 
	 * @param a String
	 * @param a String
	 * @param a boolean
	 * @throws Exception
	 */
	void setEncryptedPassword(final String userID, final String aPassword, final boolean salted) throws Exception;
	
	/**
	 * <p>setUnencryptedPassword</p>
	 * 
	 * @param a String
	 * @param a String
	 * @throws Exception
	 */
	void setUnencryptedPassword(final String userID, final String aPassword) throws Exception;
	
	/**
	 * <p>encryptedPassword</p>
	 * 
	 * @param a String
	 * @param a boolean
	 * @return a String
	 */
	String encryptedPassword(final String aPassword, final boolean useSalt);
	
	/**
	 * <p>comparePasswords</p>
	 * 
	 * @param a String
	 * @param a String
	 * @return a boolean
	 */
	boolean comparePasswords(final String userID, final String aPassword);

	/**
	 * <p>checkSaltedPassword</p>
	 * 
	 * @param a String
	 * @param a String
	 * @return a boolean
	 */
	boolean checkSaltedPassword(final String raw, final String encrypted);
	
	/**
	 * <p>update</p>
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	void update() throws IOException, FileNotFoundException;
	
	/**
	 * <p>getUsersWithRole</p>
	 * 
	 * @param roleid
	 * @return
	 * @throws IOException
	 */
	String[] getUsersWithRole(final String roleid) throws IOException;
	
	/**
	 * <p>userHasRole</p>
	 * 
	 * @param a User
	 * @param a String
	 * @return a boolean
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	boolean userHasRole(final User user, final String roleid) throws FileNotFoundException, IOException;
	
	/**
	 * <p>isUserScheduledForRole</p>
	 * 
	 * @param a User
	 * @param a String
	 * @param a Date
	 * @return a boolean
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	boolean isUserScheduledForRole(final User user, final String roleid, final Date time) throws FileNotFoundException, IOException;
	
	/**
	 * <p>getUsersScheduledForRole</p>
	 * 
	 * @param a String
	 * @param a Date
	 * @return a String[]
	 * @throws IOException
	 */
	String[] getUsersScheduledForRole(final String roleid, final Date time) throws  IOException;
	
	/**
	 * <p>hasOnCallRole</p>
	 * 
	 * @param a String
	 * @return a boolean
	 * @throws IOException
	 */
	boolean hasOnCallRole(final String roleid) throws IOException;
	
	/**
	 * <p>countUsersWithRole</p>
	 * 
	 * @param a String
	 * @return an int
	 * @throws IOException
	 */
	int countUsersWithRole(final String roleid) throws IOException;
}
