/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/


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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.netmgt.config.users.Header;
import org.opennms.netmgt.config.users.Password;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.config.users.Users;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;

/**
 * <p>Abstract UserManager class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
public abstract class UserManager {
    private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static final PasswordEncryptor m_passwordEncryptor = new StrongPasswordEncryptor();

    private final ReadWriteLock m_readWriteLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_readWriteLock.readLock();
    private final Lock m_writeLock = m_readWriteLock.writeLock();
    
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
    protected UserManager(final GroupManager groupManager) {
        m_groupManager = groupManager;
    }
    
    /**
     * <p>parseXML</p>
     *
     * @param in a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void parseXML(final InputStream in) throws MarshalException, ValidationException {
        m_writeLock.lock();
        
        try {
            final Userinfo userinfo = CastorUtils.unmarshal(Userinfo.class, in);
            final Users users = userinfo.getUsers();
            oldHeader = userinfo.getHeader();
            final List<User> usersList = users.getUserCollection();
            m_users = new TreeMap<String, User>();
        
            for (final User curUser : usersList) {
                m_users.put(curUser.getUserId(), curUser);
            }
        
            _buildDutySchedules(m_users);
        } finally {
            m_writeLock.unlock();
        }
    }


    /**
     * Adds a new user and overwrites the "users.xml"
     *
     * @param name a {@link java.lang.String} object.
     * @param details a {@link org.opennms.netmgt.config.users.User} object.
     * @throws java.lang.Exception if any.
     */
    public void saveUser(final String name, final User details) throws Exception {
        m_writeLock.lock();
        
        try {
            _writeUser(name, details);
        } finally {
            m_writeLock.unlock();
        }
    }

    private void _writeUser(final String name, final User details) throws Exception {
        if (name == null || details == null) {
            throw new Exception("UserFactory:saveUser  null");
        } else {
            m_users.put(name, details);
        }
      
        _saveCurrent();
    }
    
    public void save(final OnmsUser user) throws Exception {
        m_writeLock.lock();
        
        try {
            User castorUser = _getUser(user.getUsername());
            if (castorUser == null) {
                castorUser = new User();
                castorUser.setUserId(user.getUsername());
            }
            castorUser.setFullName(user.getFullName());
            castorUser.setUserComments(user.getComments());
            
            final Password pass = new Password();
            pass.setContent(user.getPassword());
            pass.setSalt(user.getPasswordSalted());
            castorUser.setPassword(pass);
    
            if (user.getDutySchedule() != null) {
                castorUser.setDutySchedule(user.getDutySchedule());
            }
            
            _writeUser(user.getUsername(), castorUser);
        } finally {
            m_writeLock.unlock();
        }
    }

    /**
     * Builds a mapping between user IDs and duty schedules. These are used by
     * Notifd when determining to send a notice to a given user. This helps
     * speed up the decision process.
     * 
     * @param users
     *            the map of users parsed from the XML configuration file
     */
    private void _buildDutySchedules(final Map<String,User> users) {
        m_dutySchedules = new HashMap<String,List<DutySchedule>>();
        
        for (final String key : users.keySet()) {
            final User curUser = users.get(key);
    
            if (curUser.getDutyScheduleCount() > 0) {
                final List<DutySchedule> dutyList = new ArrayList<DutySchedule>();
                for (final String duty : curUser.getDutyScheduleCollection()) {
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
    public boolean isUserOnDuty(final String user, final Calendar time) throws IOException, MarshalException, ValidationException {
    
        update();
    
        m_readLock.lock();
        try {
            // if the user has no duty schedules then he is on duty
            if (!m_dutySchedules.containsKey(user))
                return true;
    
            for (final DutySchedule curSchedule : m_dutySchedules.get(user)) {
            	if (curSchedule.isInSchedule(time)) {
            		return true;
            	}
            }
        } finally {
            m_readLock.unlock();
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
    
        m_readLock.lock();
        try {
            return Collections.unmodifiableMap(m_users);
        } finally {
            m_readLock.unlock();
        }
    }

    public OnmsUserList getOnmsUserList() throws MarshalException, ValidationException, IOException {
        final OnmsUserList list = new OnmsUserList();

        m_readLock.lock();
        
        try {
            for (final String username : _getUserNames()) {
                list.add(_getOnmsUser(username));
            }
            list.setTotalCount(list.getCount());
    
            return list;
        } finally {
            m_readLock.unlock();
        }
    }
    
    public OnmsUser getOnmsUser(final String username) throws MarshalException, ValidationException, IOException {
        m_readLock.lock();
        
        try {
            return _getOnmsUser(username);
        } finally {
            m_readLock.unlock();
        }
    }

    private OnmsUser _getOnmsUser(final String username) throws IOException, MarshalException, ValidationException {
        final User castorUser = _getUser(username);
        if (castorUser == null) return null;

        final OnmsUser user = new OnmsUser(username);
        user.setFullName(castorUser.getFullName());
        user.setComments(castorUser.getUserComments());
        user.setPassword(castorUser.getPassword().getContent());
        user.setPasswordSalted(castorUser.getPassword().getSalt());
        user.setDutySchedule(castorUser.getDutyScheduleCollection());
   
        return user;
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
    public boolean hasUser(final String userName) throws IOException, MarshalException, ValidationException {
    
        update();

        m_readLock.lock();
        try {
            return m_users.containsKey(userName);
        } finally {
            m_readLock.unlock();
        }
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

        m_readLock.lock();
        try {
            return _getUserNames();
        } finally {
            m_readLock.unlock();
        }
    }

    private List<String> _getUserNames() {
        final List<String> userNames = new ArrayList<String>();
        userNames.addAll(m_users.keySet());
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
    public User getUser(final String name) throws IOException, MarshalException, ValidationException {
    
        update();

        m_readLock.lock();
        try {
            return _getUser(name);
        } finally {
            m_readLock.unlock();
        }
    }

    private User _getUser(final String name) {
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
    public String getTuiPin(final String name) throws IOException, MarshalException, ValidationException {
    
        update();

        m_readLock.lock();
        try {
            return m_users.get(name).getTuiPin();
        } finally {
            m_readLock.unlock();
        }
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
    public String getTuiPin(final User user) throws IOException, MarshalException, ValidationException {
    
        update();
    
        m_readLock.lock();
        try {
            return m_users.get(user.getUserId()).getTuiPin();
        } finally {
            m_readLock.unlock();
        }
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
    public String getMicroblogName(final String name) throws MarshalException, ValidationException, FileNotFoundException, IOException {
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
    public String getMicroblogName(final User user) throws MarshalException, ValidationException, FileNotFoundException, IOException {
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
    public String getContactInfo(final String userID, final String command) throws IOException, MarshalException, ValidationException {
        update();

        m_readLock.lock();
        try {
            final User user = m_users.get(userID);
            return _getContactInfo(user, command);
        } finally {
            m_readLock.unlock();
        }
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
    public String getContactInfo(final User user, final String command) throws IOException, MarshalException, ValidationException {
        update();

        m_readLock.lock();
        
        try {
            return _getContactInfo(user, command);
        } finally {
            m_readLock.unlock();
        }
    }

    private String _getContactInfo(final User user, final String command) {
        if (user == null) return "";
        
        for (final Contact contact : user.getContactCollection()) {
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
    public String getContactServiceProvider(final String userID, final String command) throws IOException, MarshalException, ValidationException {
        update();

        m_readLock.lock();
        try {
            final User user = m_users.get(userID);
            return _getContactServiceProvider(user, command);
        } finally {
            m_readLock.unlock();
        }
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
    public String getContactServiceProvider(final User user, final String command) throws IOException, MarshalException, ValidationException {
        update();
        
        m_readLock.lock();
        try {
            return _getContactServiceProvider(user, command);
        } finally {
            m_readLock.unlock();
        }
    }

    private String _getContactServiceProvider(final User user, final String command) {
        if (user == null) return "";

        for (final Contact contact : user.getContactCollection()) {
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
    public String getEmail(final String userID) throws IOException, MarshalException, ValidationException {
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
    public String getEmail(final User user) throws IOException, MarshalException, ValidationException {
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
    public String getPagerEmail(final String userID) throws IOException, MarshalException, ValidationException {
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
    public String getPagerEmail(final User user) throws IOException, MarshalException, ValidationException {
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
    public String getNumericPin(final String userID) throws IOException, MarshalException, ValidationException {
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
    public String getNumericPin(final User user) throws IOException, MarshalException, ValidationException {
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
    public String getXMPPAddress(final String userID) throws IOException, MarshalException, ValidationException {

        update();
        
        m_readLock.lock();
        try {
            final User user = m_users.get(userID);
            return _getXMPPAddress(user);
        } finally {
            m_readLock.unlock();
        }
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
    public String getXMPPAddress(final User user) throws IOException, MarshalException, ValidationException {

        update();

        m_readLock.lock();
        try {
            return _getXMPPAddress(user);
        } finally {
            m_readLock.unlock();
        }
    }

    private String _getXMPPAddress(final User user) {
        if (user == null)
            return "";
        
        for (final Contact contact : user.getContactCollection()) {
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
    public String getNumericPage(final String userID) throws IOException, MarshalException, ValidationException {
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
    public String getNumericPage(final User user) throws IOException, MarshalException, ValidationException {
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
    public String getTextPin(final String userID) throws IOException, MarshalException, ValidationException {
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
    public String getTextPin(final User user) throws IOException, MarshalException, ValidationException {
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
    public String getTextPage(final String userID) throws IOException, MarshalException, ValidationException {
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
    public String getTextPage(final User user) throws IOException, MarshalException, ValidationException {
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
    public String getWorkPhone(final String userID) throws MarshalException, ValidationException, IOException {
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
    public String getWorkPhone(final User user) throws MarshalException, ValidationException, IOException {
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
    public String getMobilePhone(final String userID) throws MarshalException, ValidationException, IOException {
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
    public String getMobilePhone(final User user) throws MarshalException, ValidationException, IOException {
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
    public String getHomePhone(final String userID) throws MarshalException, ValidationException, IOException {
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
    public String getHomePhone(final User user) throws MarshalException, ValidationException, IOException {
        return getContactInfo(user, "homePhone");
    }

    /**
     * <p>saveUsers</p>
     *
     * @param usersList a {@link java.util.Collection} object.
     * @throws java.lang.Exception if any.
     */
    public void saveUsers(final Collection<User> usersList) throws Exception {
        m_writeLock.lock();
        
        try {
            // clear out the internal structure and reload it
            m_users.clear();
        
            for (final User curUser : usersList) {
            	m_users.put(curUser.getUserId(), curUser);
            }
        } finally {
            m_writeLock.unlock();
        }
    }

    /**
     * Removes the user from the list of users. Then overwrites to the
     * "users.xml"
     *
     * @param name a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public void deleteUser(final String name) throws Exception {
        m_writeLock.lock();
        
        try {
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
        
            _saveCurrent();
        } finally {
            m_writeLock.unlock();
        }
    }

    /**
     * Saves into "users.xml" file
     */
    private void _saveCurrent() throws Exception {
        final Users users = new Users();
        
        for (final User user : m_users.values()) {
            users.addUser(user);
        }
    
        final Userinfo userinfo = new Userinfo();
        userinfo.setUsers(users);

        final Header header = oldHeader;
        if (header != null) {
            header.setCreated(EventConstants.formatToString(new Date()));
            userinfo.setHeader(header);
        }
        oldHeader = header;
    
        // marshal to a string first, then write the string to the file. This
        // way the original configuration
        // isn't lost if the XML from the marshal is hosed.
        final StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(userinfo, stringWriter);
        final String writerString = stringWriter.toString();
        saveXML(writerString);
    }

    /**
     * <p>saveXML</p>
     *
     * @param writerString a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void saveXML(final String writerString) throws IOException ;

    /**
     * When this method is called users name is changed, so also is the username
     * belonging to the group and the view. Also overwrites the "users.xml" file
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newName a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public void renameUser(final String oldName, final String newName) throws Exception {
        m_writeLock.lock();
        
        try {
            // Get the old data
            if (m_users.containsKey(oldName)) {
                final User data = m_users.get(oldName);
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
        
            _saveCurrent();
        } finally {
            m_writeLock.unlock();
        }
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
    public void setEncryptedPassword(final String userID, final String aPassword, final boolean salted) throws Exception {
        m_writeLock.lock();
        
        try {
            final User user = m_users.get(userID);
            if (user != null) {
                final Password pass = new Password();
                pass.setContent(aPassword);
                pass.setSalt(salted);
                user.setPassword(pass);
            }
        
            _saveCurrent();
        } finally {
            m_writeLock.unlock();
        }
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
    public void setUnencryptedPassword(final String userID, final String aPassword) throws Exception {
        m_writeLock.lock();
        
        try {
            final User user =  m_users.get(userID);
            if (user != null) {
                final Password pass = new Password();
                pass.setContent(encryptedPassword(aPassword, true));
                pass.setSalt(true);
                user.setPassword(pass);
            }
        
            _saveCurrent();
        } finally {
            m_writeLock.unlock();
        }
    }

    /**
     * <p>encryptedPassword</p>
     *
     * @param aPassword a {@link java.lang.String} object.
     * @param useSalt TODO
     * @return a {@link java.lang.String} object.
     */
    public String encryptedPassword(final String aPassword, final boolean useSalt) {
        String encryptedPassword = null;

        if (useSalt) {
            encryptedPassword = m_passwordEncryptor.encryptPassword(aPassword);
        } else {
            // old crappy algorithm
            try {
                final MessageDigest digest = MessageDigest.getInstance("MD5");
        
                // build the digest, get the bytes, convert to hexadecimal string
                // and return
                encryptedPassword = hexToString(digest.digest(aPassword.getBytes()));
            } catch (final NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.toString());
            }
        }

        return encryptedPassword;
    }

    /**
     * @param data
     * @return
     */
    private String hexToString(final byte[] data) {
        // check to see if the byte array has an even number of elements
        if ((data.length % 2) != 0) return null;
    
        // there will be two hexadecimal characters for each byte element
        final char[] buffer = new char[data.length * 2];
    
        for (int i = 0; i < data.length; i++) {
            final int low = (int) (data[i] & 0x0f);
            final int high = (int) ((data[i] & 0xf0) >> 4);
            buffer[i * 2] = HEX[high];
            buffer[i * 2 + 1] = HEX[low];
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
    public boolean comparePasswords(final String userID, final String aPassword) {
        m_readLock.lock();
        
        try {
            final User user = m_users.get(userID);
            if (user == null) return false;

            final String password = user.getPassword().getContent().trim();
            final boolean isSalted = user.getPassword().getSalt();
            if (isSalted) {
                return checkSaltedPassword(aPassword, password);
            } else {
                return password.equals(encryptedPassword(aPassword, false));
            }
        } finally {
            m_readLock.unlock();
        }
    }

    public boolean checkSaltedPassword(final String raw, final String encrypted) {
        return m_passwordEncryptor.checkPassword(raw, encrypted);
    }
    
    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    protected abstract void doUpdate() throws IOException, FileNotFoundException, MarshalException, ValidationException;

    public final void update() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        m_writeLock.lock();
        try {
            doUpdate();
        } finally {
            m_writeLock.unlock();
        }
    }
    
    /**
     * <p>getUsersWithRole</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String[] getUsersWithRole(final String roleid) throws IOException, MarshalException, ValidationException {
        update();

        m_readLock.lock();
        try {
            return _getUsersWithRole(roleid);
        } finally {
            m_readLock.unlock();
        }
    }

    private String[] _getUsersWithRole(final String roleid) throws MarshalException, ValidationException, IOException {
        final List<String> usersWithRole = new ArrayList<String>();
   
        for (final User user : m_users.values()) {
            if (_userHasRole(user, roleid)) {
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
    public boolean userHasRole(final User user, final String roleid) throws FileNotFoundException, MarshalException, ValidationException, IOException {
        update();

        m_readLock.lock();
        try {
            return _userHasRole(user, roleid);
        } finally {
            m_readLock.unlock();
        }
    }

    private boolean _userHasRole(final User user, final String roleid) throws MarshalException, ValidationException, IOException {
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
    public boolean isUserScheduledForRole(final User user, final String roleid, final Date time) throws FileNotFoundException, MarshalException, ValidationException, IOException {
        update();
        
        m_readLock.lock();
        try {
            return _isUserScheduledForRole(user, roleid, time);
        } finally {
            m_readLock.unlock();
        }
    }

    private boolean _isUserScheduledForRole(final User user, final String roleid, final Date time) throws MarshalException, ValidationException, IOException {
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
    public String[] getUsersScheduledForRole(final String roleid, final Date time) throws MarshalException, ValidationException, IOException {
        update();

        m_readLock.lock();
        try {
            final List<String> usersScheduledForRole = new ArrayList<String>();
            
            for (final User user : m_users.values()) {
                if (_isUserScheduledForRole(user, roleid, time)) {
                    usersScheduledForRole.add(user.getUserId());
                }
            }
            
            return usersScheduledForRole.toArray(new String[usersScheduledForRole.size()]);
        } finally {
            m_readLock.unlock();
        }
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
    public boolean hasRole(final String roleid) throws MarshalException, ValidationException, IOException {
        m_readLock.lock();
        try {
            return m_groupManager.getRole(roleid) != null;
        } finally {
            m_readLock.unlock();
        }
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
    public int countUsersWithRole(final String roleid) throws MarshalException, ValidationException, IOException {
        m_readLock.lock();
        try {
            final String[] users = _getUsersWithRole(roleid);
            if (users == null) return 0;
            return users.length;
        } finally {
            m_readLock.unlock();
        }
    }
    /**
     * <p>isUpdateNeeded</p>
     *
     * @return a boolean.
     */
    public abstract boolean isUpdateNeeded();

    public abstract long getLastModified();

}
