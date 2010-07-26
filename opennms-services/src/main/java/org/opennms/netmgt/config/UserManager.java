/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Aug 31: Remove an unneeded cast, convert to use CastorUtils
 *              for unmarshalling, unmarshal from an InputSteram instead
 *              of a reader in parseXML, and fix some collection types.
 *              - dj@opennms.org
 * 
 * Created: December 6, 2004
 *
 * Copyright (C) 2004-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.common.Header;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.config.users.Users;
import org.opennms.netmgt.dao.castor.CastorUtils;

/**
 * <p>Abstract UserManager class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @version $Id: $
 */
public abstract class UserManager {

    protected GroupManager m_groupManager;
    /**
     * A mapping of user IDs to the User objects
     */
    protected Map<String, User> m_users;
    /**
     * The duty schedules for each user
     */
    protected HashMap<String, List<DutySchedule>> m_dutySchedules;
    private Header oldHeader;

    /**
     * <p>Constructor for UserManager.</p>
     *
     * @param groupManager a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    protected UserManager(GroupManager groupManager) {
        m_groupManager = groupManager;
    }
    
    /**
     * <p>parseXML</p>
     *
     * @param in a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void parseXML(InputStream in) throws MarshalException, ValidationException {
        Userinfo userinfo = CastorUtils.unmarshal(Userinfo.class, in);
        Users users = userinfo.getUsers();
        oldHeader = userinfo.getHeader();
        List<User> usersList = users.getUserCollection();
        m_users = new HashMap<String, User>();

        for (User curUser : usersList) {
            m_users.put(curUser.getUserId(), curUser);
        }
    
        buildDutySchedules(m_users);
    }


    /**
     * Adds a new user and overwrites the "users.xml"
     *
     * @param name a {@link java.lang.String} object.
     * @param details a {@link org.opennms.netmgt.config.users.User} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void saveUser(String name, User details) throws Exception {
        if (name == null || details == null) {
            throw new Exception("UserFactory:saveUser  null");
        } else {
            m_users.put(name, details);
        }
    
        saveCurrent();
    }

    /**
     * Builds a mapping between user IDs and duty schedules. These are used by
     * Notifd when determining to send a notice to a given user. This helps
     * speed up the decision process.
     * 
     * @param users
     *            the map of users parsed from the XML configuration file
     */
    private void buildDutySchedules(Map<String, User> users) {
        m_dutySchedules = new HashMap<String, List<DutySchedule>>();
        
        for (String key : users.keySet()) {
            User curUser = users.get(key);
    
            if (curUser.getDutyScheduleCount() > 0) {
                List<DutySchedule> dutyList = new ArrayList<DutySchedule>();
                for (String duty : curUser.getDutyScheduleCollection()) {
                	dutyList.add(new DutySchedule(duty));
                }
    
                m_dutySchedules.put(key, dutyList);
            }
        }
    }

    /**
     * Determines if a user is on duty at a given time. If a user has no duty
     * schedules listed in the configuration file, that user is assumed to always be on
     * duty.
     *
     * @param user
     *            the user id
     * @param time
     *            the time to check for a duty schedule
     * @return boolean, true if the user is on duty, false otherwise.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public boolean isUserOnDuty(String user, Calendar time) throws IOException, MarshalException, ValidationException {
    
        update();
    
        // if the user has no duty schedules then he is on duty
        if (!m_dutySchedules.containsKey(user))
            return true;

        for (DutySchedule curSchedule : m_dutySchedules.get(user)) {
        	if (curSchedule.isInSchedule(time)) {
        		return true;
        	}
        }
        
        return false;
    }

    /**
     * Return a <code>Map</code> of usernames to user instances.
     *
     * @return a {@link java.util.Map} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public Map<String, User> getUsers() throws IOException, MarshalException, ValidationException {
    
        update();
    
        return m_users;
    }

    /**
     * Returns a boolean indicating if the user name appears in the XML file
     *
     * @return true if the user exists in the XML file, false otherwise
     * @param userName a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public boolean hasUser(String userName) throws IOException, MarshalException, ValidationException {
    
        update();
    
        return m_users.containsKey(userName);
    }

    /**
     * <p>getUserNames</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public List<String> getUserNames() throws IOException, MarshalException, ValidationException {
    
        update();
    
        List<String> userNames = new ArrayList<String>();
        
        for (String key : m_users.keySet()) {
            userNames.add(key);
        }
    
        return userNames;
    }

    /**
     * Get a user by name
     *
     * @param name
     *            the name of the user to return
     * @return the user specified by name
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public User getUser(String name) throws IOException, MarshalException, ValidationException {
    
        update();
    
        return m_users.get(name);
    }
    
    /**
     * Get a user's telephone PIN by name
     *
     * @param name
     *            the name of the user to return
     * @return the telephone PIN of the user specified by name
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getTuiPin(String name) throws IOException, MarshalException, ValidationException {
    
        update();
    
        return m_users.get(name).getTuiPin();
    }
    
    /**
     * Get a user's telephone PIN by User object
     *
     * @return the telephone PIN of the user specified by user
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getTuiPin(User user) throws IOException, MarshalException, ValidationException {
    
        update();
    
        return m_users.get(user.getUserId()).getTuiPin();
    }
    
    /**
     * Get a user's microblog username by username
     *
     * @param name
     *            the username of the user whose microblog username should be returned
     * @return the microblog username of the specified user
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     */
    public String getMicroblogName(String name) throws MarshalException, ValidationException, FileNotFoundException, IOException {
        return getContactInfo(name, "microblog");
    }

    /**
     * Get a user's microblog username by User
     *
     * @param user
     *            the user object of the user whose microblog username should be returned
     * @return the microblog username of the specified user
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     */
    public String getMicroblogName(User user) throws MarshalException, ValidationException, FileNotFoundException, IOException {
        return getContactInfo(user, "microblog");
    }

    /**
     * Get the contact info given a command string
     *
     * @param userID
     *            the name of the user
     * @param command
     *            the command to look up the contact info for
     * @return the contact information
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getContactInfo(String userID, String command) throws IOException, MarshalException, ValidationException {
        update();
        
        User user = m_users.get(userID);
        return getContactInfo(user, command);
    }
    
    /**
     * <p>getContactInfo</p>
     *
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @param command a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getContactInfo(User user, String command) throws IOException, MarshalException, ValidationException {
        update();
        
        if (user == null)
            return "";
        
        for (Contact contact : user.getContactCollection()) {
        	if (contact != null && contact.getType().equals(command)) {
        		return contact.getInfo();
        	}
        }
        
        return "";
    }

    /**
     * Get the contact service provider, given a command string
     *
     * @param userID
     *            the name of the user
     * @param command
     *            the command to look up the contact info for
     * @return the contact information
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getContactServiceProvider(String userID, String command) throws IOException, MarshalException, ValidationException {
        update();
    
        User user = m_users.get(userID);
        return getContactServiceProvider(user, command);
    }
    
    /**
     * <p>getContactServiceProvider</p>
     *
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @param command a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getContactServiceProvider(User user, String command) throws IOException, MarshalException, ValidationException {
        update();
        
        if (user == null)
            return "";

        for (Contact contact : user.getContactCollection()) {
        	if (contact != null && contact.getType().equals(command)) {
        		return contact.getServiceProvider();
        	}
        }
        
        return "";
    }

    /**
     * Get a email by name
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the email specified by name
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getEmail(String userID) throws IOException, MarshalException, ValidationException {
        return getContactInfo(userID, "email");
    }
    
    /**
     * Get a email by user
     *
     * @param user the user to find the email for
     * @return String the email specified by name
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getEmail(User user) throws IOException, MarshalException, ValidationException {
        return getContactInfo(user, "email");
    }

    /**
     * Get a pager email by name
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the pager email
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getPagerEmail(String userID) throws IOException, MarshalException, ValidationException {
        return getContactInfo(userID, "pagerEmail");
    }

    /**
     * Get a pager email by user
     *
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @return String the pager email
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getPagerEmail(User user) throws IOException, MarshalException, ValidationException {
        return getContactInfo(user, "pagerEmail");
    }

    /**
     * Get a numeric pin
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the numeric pin
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getNumericPin(String userID) throws IOException, MarshalException, ValidationException {
        return getContactInfo(userID, "numericPage");
    }

    /**
     * Get a numeric pin
     *
     * @return String the numeric pin
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getNumericPin(User user) throws IOException, MarshalException, ValidationException {
        return getContactInfo(user, "numericPage");
    }

    /**
     * Get an XMPP address by name
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the XMPP address
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getXMPPAddress(String userID) throws IOException, MarshalException, ValidationException {

        update();
        
        User user = m_users.get(userID);
        if (user == null)
            return "";
        
        for (Contact contact : user.getContactCollection()) {
        	if (contact != null && contact.getType().equals("xmppAddress")) {
        		return contact.getInfo();
        	}
        }
        
        return "";
    }

    /**
     * Get an XMPP address by name
     *
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @return String the XMPP address
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getXMPPAddress(User user) throws IOException, MarshalException, ValidationException {

        update();
        
        if (user == null)
            return "";
        
        for (Contact contact : user.getContactCollection()) {
        	if (contact != null && contact.getType().equals("xmppAddress")) {
        		return contact.getInfo();
        	}
        }
        
        return "";
    }

    /**
     * Get a numeric service provider
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the service provider
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getNumericPage(String userID) throws IOException, MarshalException, ValidationException {
        return getContactServiceProvider(userID, "numericPage");
    }
    
    /**
     * Get a numeric service provider
     *
     * @return String the service provider
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getNumericPage(User user) throws IOException, MarshalException, ValidationException {
        return getContactServiceProvider(user, "numericPage");
    }

    /**
     * Get a text pin
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the text pin
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getTextPin(String userID) throws IOException, MarshalException, ValidationException {
        return getContactInfo(userID, "textPage");
    }
    
    /**
     * Get a text pin
     *
     * @return String the text pin
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getTextPin(User user) throws IOException, MarshalException, ValidationException {
        return getContactInfo(user, "textPage");
    }

    /**
     * Get a Text Page Service Provider
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the text page service provider.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getTextPage(String userID) throws IOException, MarshalException, ValidationException {
        return getContactServiceProvider(userID, "textPage");
    }
    
    /**
     * Get a Text Page Service Provider
     *
     * @return String the text page service provider.
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getTextPage(User user) throws IOException, MarshalException, ValidationException {
        return getContactServiceProvider(user, "textPage");
    }
    
    /**
     * Get a work phone number
     *
     * @param userID
     *             the user ID of the user to return
     * @return String the work phone number
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     */
    public String getWorkPhone(String userID) throws MarshalException, ValidationException, IOException {
        return getContactInfo(userID, "workPhone");
    }
    
    /**
     * Get a work phone number
     *
     * @return String the work phone number
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     */
    public String getWorkPhone(User user) throws MarshalException, ValidationException, IOException {
        return getContactInfo(user, "workPhone");
    }

    /**
     * Get a mobile phone number
     *
     * @param userID
     *             the user ID of the user to return
     * @return String the mobile phone number
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     */
    public String getMobilePhone(String userID) throws MarshalException, ValidationException, IOException {
        return getContactInfo(userID, "mobilePhone");
    }
    
    /**
     * Get a mobile phone number
     *
     * @return String the mobile phone number
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     */
    public String getMobilePhone(User user) throws MarshalException, ValidationException, IOException {
        return getContactInfo(user, "mobilePhone");
    }

    /**
     * Get a home phone number
     *
     * @param userID
     *             the user ID of the user to return
     * @return String the home phone number
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     */
    public String getHomePhone(String userID) throws MarshalException, ValidationException, IOException {
        return getContactInfo(userID, "homePhone");
    }
    
    /**
     * Get a home phone number
     *
     * @return String the home phone number
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     */
    public String getHomePhone(User user) throws MarshalException, ValidationException, IOException {
        return getContactInfo(user, "homePhone");
    }

    /**
     * <p>saveUsers</p>
     *
     * @param usersList a {@link java.util.Collection} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void saveUsers(Collection<User> usersList) throws Exception {
        // clear out the internal structure and reload it
        m_users.clear();
    
        for (User curUser : usersList) {
        	m_users.put(curUser.getUserId(), curUser);
        }
    }

    /**
     * Removes the user from the list of users. Then overwrites to the
     * "users.xml"
     *
     * @param name a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void deleteUser(String name) throws Exception {
        // Check if the user exists
        if (m_users.containsKey(name)) {
            // Delete the user in the user map.
            m_users.remove(name);
    
            // Delete the user in the group.
            m_groupManager.deleteUser(name);
    
            // Delete the user in the view.
            // viewFactory.deleteUser(name);
        } else {
            throw new Exception("UserFactory:delete The old user name " + name + " is not found");
        }
    
        saveCurrent();
    }

    /**
     * Saves into "users.xml" file
     */
    private synchronized void saveCurrent() throws Exception {
        Header header = oldHeader;
    
        header.setCreated(EventConstants.formatToString(new Date()));
    
        Users users = new Users();
        Collection<User> collUsers = m_users.values();
        Iterator<User> iter = collUsers.iterator();
        while (iter != null && iter.hasNext()) {
            User tmpUser = iter.next();
            users.addUser(tmpUser);
        }
    
        Userinfo userinfo = new Userinfo();
        userinfo.setUsers(users);
        userinfo.setHeader(header);
    
        oldHeader = header;
    
        // marshal to a string first, then write the string to the file. This
        // way the original configuration
        // isn't lost if the XML from the marshal is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(userinfo, stringWriter);
        String writerString = stringWriter.toString();
        saveXML(writerString);
    }

    /**
     * <p>saveXML</p>
     *
     * @param writerString a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void saveXML(String writerString) throws IOException ;

    /**
     * When this method is called users name is changed, so also is the username
     * belonging to the group and the view. Also overwrites the "users.xml" file
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newName a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void renameUser(String oldName, String newName) throws Exception {
        // Get the old data
        if (m_users.containsKey(oldName)) {
            User data = m_users.get(oldName);
            if (data == null) {
                m_users.remove(oldName);
                throw new Exception("UserFactory:rename the data contained for old user " + oldName + " is null");
            } else {
                // Rename the user in the user map.
                m_users.remove(oldName);
                data.setUserId(newName);
                m_users.put(newName, data);
    
                // Rename the user in the group.
                m_groupManager.renameUser(oldName, newName);
    
                // Rename the user in the view.
                // viewFactory.renameUser(oldName, newName);
            }
        } else {
            throw new Exception("UserFactory:rename the old user name " + oldName + " is not found");
        }
    
        saveCurrent();
    }

    /**
     * Sets the password for this user, assuming that the value passed in is
     * already encrypted properly
     *
     * @param userID
     *            the user ID to change the password for
     * @param aPassword
     *            the encrypted password
     * @throws java.lang.Exception if any.
     */
    public void setEncryptedPassword(String userID, String aPassword) throws Exception {
        User user = m_users.get(userID);
        if (user != null) {
            user.setPassword(aPassword);
        }
    
        saveCurrent();
    }

    /**
     * Sets the password for this user, first encrypting it
     *
     * @param userID
     *            the user ID to change the password for
     * @param aPassword
     *            the password
     * @throws java.lang.Exception if any.
     */
    public void setUnencryptedPassword(String userID, String aPassword) throws Exception {
        User user =  m_users.get(userID);
        if (user != null) {
            user.setPassword(encryptedPassword(aPassword));
        }
    
        saveCurrent();
    }

    /**
     * <p>encryptedPassword</p>
     *
     * @param aPassword a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String encryptedPassword(String aPassword) {
        String encryptedPassword = null;
    
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
    
            // build the digest, get the bytes, convert to hexadecimal string
            // and return
            encryptedPassword = hexToString(digest.digest(aPassword.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.toString());
        }
    
        return encryptedPassword;
    }

    /**
     * @param data
     * @return
     */
    private String hexToString(byte[] data) {
        char[] hexadecimals = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    
        // check to see if the byte array has an even number of elements
        if ((data.length % 2) != 0)
            return null;
    
        // there will be two hexadecimal characters for each byte element
        char[] buffer = new char[data.length * 2];
    
        for (int i = 0; i < data.length; i++) {
            int low = (int) (data[i] & 0x0f);
            int high = (int) ((data[i] & 0xf0) >> 4);
            buffer[i * 2] = hexadecimals[high];
            buffer[i * 2 + 1] = hexadecimals[low];
        }
    
        return new String(buffer);
    }

    /**
     * This method compares two encrypted strings for equality.
     *
     * @param userID
     *            the user ID to check against.
     * @param aPassword
     *            the password to check for equality
     * @return true if the two passwords are equal (after encryption), false
     *         otherwise
     */
    public boolean comparePasswords(String userID, String aPassword) {
        User user = m_users.get(userID);
        if (user == null)
            return false;
    
        return user.getPassword().equals(encryptedPassword(aPassword));
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public abstract void update() throws IOException, FileNotFoundException, MarshalException, ValidationException;

    /**
     * <p>getUsersWithRole</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String[] getUsersWithRole(String roleid) throws IOException, MarshalException, ValidationException {
        update();
        
        List<String> usersWithRole = new ArrayList<String>();
        
        Iterator<User> i = m_users.values().iterator();
        
        while (i.hasNext()) {
            User user = i.next();
            if (userHasRole(user, roleid)) {
                usersWithRole.add(user.getUserId());
            }
        }
        
        return usersWithRole.toArray(new String[usersWithRole.size()]);
        
    }
    
    /**
     * <p>userHasRole</p>
     *
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @param roleid a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public boolean userHasRole(User user, String roleid) throws FileNotFoundException, MarshalException, ValidationException, IOException {
        update();
        
        if (roleid == null) throw new NullPointerException("roleid is null");
        
        return m_groupManager.userHasRole(user.getUserId(), roleid);
    }
    
    /**
     * <p>isUserScheduledForRole</p>
     *
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @param roleid a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @return a boolean.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public boolean isUserScheduledForRole(User user, String roleid, Date time) throws FileNotFoundException, MarshalException, ValidationException, IOException {
        update();
        
        if (roleid == null) throw new NullPointerException("roleid is null");
        
        return m_groupManager.isUserScheduledForRole(user.getUserId(), roleid, time);
    
    }
    
    /**
     * <p>getUsersScheduledForRole</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @return an array of {@link java.lang.String} objects.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public String[] getUsersScheduledForRole(String roleid, Date time) throws MarshalException, ValidationException, IOException {
        update();
        
        List<String> usersScheduledForRole = new ArrayList<String>();
        
        Iterator<User> i = m_users.values().iterator();
        
        while (i.hasNext()) {
            User user = i.next();
            if (isUserScheduledForRole(user, roleid, time)) {
                usersScheduledForRole.add(user.getUserId());
            }
        }
        
        return usersScheduledForRole.toArray(new String[usersScheduledForRole.size()]);
    }
    
    /**
     * <p>hasRole</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @return a boolean.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public boolean hasRole(String roleid) throws MarshalException, ValidationException, IOException {
        return m_groupManager.getRole(roleid) != null;
    }
    
    /**
     * <p>countUsersWithRole</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @return a int.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public int countUsersWithRole(String roleid) throws MarshalException, ValidationException, IOException {
        String[] users = getUsersWithRole(roleid);
        if (users == null) return 0;
        return users.length;
    }
    /**
     * <p>isUpdateNeeded</p>
     *
     * @return a boolean.
     */
    public abstract boolean isUpdateNeeded();
    
}
