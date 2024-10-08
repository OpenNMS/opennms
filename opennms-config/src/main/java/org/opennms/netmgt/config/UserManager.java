/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.netmgt.config.users.Header;
import org.opennms.netmgt.config.users.Password;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventDatetimeFormatter;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;

/**
 * <p>Abstract UserManager class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public abstract class UserManager implements UserConfig {
	public static final String ALLOW_UNSALTED_PROPERTY = "org.opennms.users.allowUnsalted";
    private final boolean m_allowUnsalted;

    private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static final PasswordEncryptor s_passwordEncryptor = new StrongPasswordEncryptor();

    private final ReadWriteLock m_readWriteLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_readWriteLock.readLock();
    private final Lock m_writeLock = m_readWriteLock.writeLock();

    private static final EventDatetimeFormatter FORMATTER = EventConstants.getEventDatetimeFormatter();

    protected GroupManager m_groupManager;
    /**
     * A mapping of user IDs to the User objects
     */
    protected Map<String, User> m_users;
    /**
     * The duty schedules for each user
     */
    protected Map<String, List<DutySchedule>> m_dutySchedules;
    private Header oldHeader;

    /**
     * <p>Constructor for UserManager.</p>
     *
     * @param groupManager a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    protected UserManager(final GroupManager groupManager) {
        m_groupManager = groupManager;
        m_allowUnsalted = Boolean.parseBoolean(System.getProperty(ALLOW_UNSALTED_PROPERTY, "true"));
    }
    
    /**
     * <p>parseXML</p>
     *
     * @param in a {@link java.io.InputStream} object.
     */
    public void parseXML(final InputStream in) {
        m_writeLock.lock();

        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(in);
            final Userinfo userinfo = JaxbUtils.unmarshal(Userinfo.class, isr);
            oldHeader = userinfo.getHeader();
            m_users = new TreeMap<String, User>();
        
            for (final User curUser : userinfo.getUsers()) {
                m_users.put(curUser.getUserId(), curUser);
            }
        
            _buildDutySchedules(m_users);
        } finally {
            IOUtils.closeQuietly(isr);
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
        update();

        m_writeLock.lock();
        
        try {
            _writeUser(name, details);
        } finally {
            m_writeLock.unlock();
        }
    }

    private void _assertSaltAcceptable(final User details) throws Exception {
        if (!m_allowUnsalted) {
            final Password p = details.getPassword();
            if (p != null) {
                if (!p.getSalt()) {
                    throw new IllegalStateException("org.opennms.users.allowUnsaltedPasswords=false, but " + details.getUserId() + " contains an unsalted password.");
                }
            }
        }
    }

    private void _writeUser(final String name, final User details) throws Exception {
        if (name == null || details == null) {
            throw new Exception("UserFactory:saveUser  null");
        } else {
            _assertSaltAcceptable(details);
            m_users.put(name, details);
        }
      
        _saveCurrent();
    }
    
    public void save(final OnmsUser onmsUser) throws Exception {
        update();

        m_writeLock.lock();
        
        try {
            User xmlUser = _getUser(onmsUser.getUsername());
            if (xmlUser == null) {
                xmlUser = new User();
                xmlUser.setUserId(onmsUser.getUsername());
            }
            xmlUser.setFullName(onmsUser.getFullName());
            xmlUser.setUserComments(onmsUser.getComments());

            // Contact info
            _setContact(xmlUser, ContactType.email, onmsUser.getEmail());
            
            final Password pass = new Password();
            pass.setEncryptedPassword(onmsUser.getPassword());
            pass.setSalt(onmsUser.getPasswordSalted());
            xmlUser.setPassword(pass);
    
            if (onmsUser.getDutySchedule() != null) {
                xmlUser.setDutySchedules(new ArrayList<String>(onmsUser.getDutySchedule()));
            }
            if (onmsUser.getRoles() != null) {
                xmlUser.setRoles(new ArrayList<String>(onmsUser.getRoles()));
            }
            _writeUser(onmsUser.getUsername(), xmlUser);
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
        
        for (final Entry<String,User> entry : users.entrySet()) {
            final String key = entry.getKey();
            final User curUser = entry.getValue();
    
            if (curUser.getDutySchedules().size() > 0) {
                final List<DutySchedule> dutyList = new ArrayList<DutySchedule>();
                for (final String duty : curUser.getDutySchedules()) {
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
     */
    public boolean isUserOnDuty(final String user, final Calendar time) throws IOException {
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
     */
    public Map<String, User> getUsers() throws IOException {
        update();
    
        m_readLock.lock();
        try {
            return Collections.unmodifiableMap(m_users);
        } finally {
            m_readLock.unlock();
        }
    }

    public OnmsUserList getOnmsUserList() throws IOException {
        update();

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
    
    public OnmsUser getOnmsUser(final String username) throws IOException {
        update();

        m_readLock.lock();
        try {
            return _getOnmsUser(username);
        } finally {
            m_readLock.unlock();
        }
    }

    private OnmsUser _getOnmsUser(final String username) throws IOException {
        final User xmlUser = _getUser(username);
        if (xmlUser == null) return null;

        final OnmsUser user = new OnmsUser(username);
        user.setFullName(trim(xmlUser.getFullName()));
        user.setComments(trim(xmlUser.getUserComments()));
        user.setPassword(xmlUser.getPassword().getEncryptedPassword());
        user.setPasswordSalted(Boolean.valueOf(xmlUser.getPassword().getSalt()));
        user.setDutySchedule(xmlUser.getDutySchedules());
        user.setRoles(xmlUser.getRoles());
        user.setEmail(_getContactInfo(xmlUser, ContactType.email));
        return user;
    }
    
    private String trim(final Optional<String> text) {
        return (text == null || !text.isPresent())? null : text.get().trim();
    }

    private Contact _getContact(final String userId, final ContactType contactType) {
    	User user = _getUser(userId);
    	if (user != null && contactType != null) {
    		for (Contact eachContact : user.getContacts()) {
    			if (contactType.name().equals(eachContact.getType())) {
    				return eachContact;
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * Returns a boolean indicating if the user name appears in the XML file
     *
     * @return true if the user exists in the XML file, false otherwise
     * @param userName a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public boolean hasUser(final String userName) throws IOException {
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
     */
    public List<String> getUserNames() throws IOException {
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
     */
    public User getUser(final String name) throws IOException {
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
     */
    public String getTuiPin(final String name) throws IOException {
    
        update();

        m_readLock.lock();
        try {
            return m_users.get(name).getTuiPin().orElse(null);
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
     */
    public String getTuiPin(final User user) throws IOException {
    
        update();
    
        m_readLock.lock();
        try {
            return m_users.get(user.getUserId()).getTuiPin().orElse(null);
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
     */
    public String getMicroblogName(final String name) throws FileNotFoundException, IOException {
        return getContactInfo(name, ContactType.microblog.toString());
    }

    /**
     * Get a user's microblog username by User
     *
     * @param user
     *            the user object of the user whose microblog username should be returned
     * @return the microblog username of the specified user
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public String getMicroblogName(final User user) throws FileNotFoundException, IOException {
        return getContactInfo(user, ContactType.microblog.toString());
    }
    
    public void setContactInfo(final String userId, final ContactType contactType, final String contactValue) throws Exception {
    	 update();
         m_writeLock.lock();
         
         try {
             final User user = _getUser(userId);
             if (user != null) {
            	 _setContact(user, contactType, contactValue);
             }
             _saveCurrent();
         } finally {
             m_writeLock.unlock();
         }
	}
    
    private void _setContact(final User user, final ContactType contactType, final String value) {
        if (user != null && !StringUtils.isEmpty(value)) {
        	Contact contact = _getContact(user.getUserId(), contactType);
        	if (contact == null) {
        		contact = new Contact();
        		user.addContact(contact);
        	}
        	contact.setInfo(value);
    		contact.setType(contactType.name());
        }
    }

    /**
     * @see {@link #getContactInfo(String, String)} 
     */
    public String getContactInfo(final String userId, final ContactType contactType) throws IOException {
    	if (userId == null || contactType == null) return null;
    	return getContactInfo(userId, contactType.name());
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
     */
    public String getContactInfo(final String userID, final String command) throws IOException {
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
     */
    public String getContactInfo(final User user, final String command) throws IOException {
        update();

        m_readLock.lock();
        
        try {
            return _getContactInfo(user, command);
        } finally {
            m_readLock.unlock();
        }
    }

    private String _getContactInfo(final User user, final ContactType contactType) {
    	return _getContactInfo(user, contactType.name());
    }
    
    private String _getContactInfo(final User user, final String command) {
        if (user == null) return "";
        
        for (final Contact contact : user.getContacts()) {
        	if (contact != null && contact.getType().equals(command)) {
        		return contact.getInfo().orElse("");
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
     */
    public String getContactServiceProvider(final String userID, final String command) throws IOException {
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
     */
    public String getContactServiceProvider(final User user, final String command) throws IOException {
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

        for (final Contact contact : user.getContacts()) {
        	if (contact != null && contact.getType().equals(command)) {
        		return contact.getServiceProvider().orElse("");
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
     */
    public String getEmail(final String userID) throws IOException {
        return getContactInfo(userID, ContactType.email.toString());
    }
    
    /**
     * Get a email by user
     *
     * @param user the user to find the email for
     * @return String the email specified by name
     * @throws java.io.IOException if any.
     */
    public String getEmail(final User user) throws IOException {
        return getContactInfo(user, ContactType.email.toString());
    }

    /**
     * Get a pager email by name
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the pager email
     * @throws java.io.IOException if any.
     */
    public String getPagerEmail(final String userID) throws IOException {
        return getContactInfo(userID, ContactType.pagerEmail.toString());
    }

    /**
     * Get a pager email by user
     *
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @return String the pager email
     * @throws java.io.IOException if any.
     */
    public String getPagerEmail(final User user) throws IOException {
        return getContactInfo(user, ContactType.pagerEmail.toString());
    }

    /**
     * Get a numeric pin
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the numeric pin
     * @throws java.io.IOException if any.
     */
    public String getNumericPin(final String userID) throws IOException {
        return getContactInfo(userID, ContactType.numericPage.toString());
    }

    /**
     * Get a numeric pin
     *
     * @return String the numeric pin
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @throws java.io.IOException if any.
     */
    public String getNumericPin(final User user) throws IOException {
        return getContactInfo(user, ContactType.numericPage.toString());
    }

    /**
     * Get an XMPP address by name
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the XMPP address
     * @throws java.io.IOException if any.
     */
    public String getXMPPAddress(final String userID) throws IOException {
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
     */
    public String getXMPPAddress(final User user) throws IOException {
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
        
        for (final Contact contact : user.getContacts()) {
        	if (contact != null && contact.getType().equals(ContactType.xmppAddress.toString())) {
        		return contact.getInfo().orElse("");
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
     */
    public String getNumericPage(final String userID) throws IOException {
        return getContactServiceProvider(userID, ContactType.numericPage.toString());
    }
    
    /**
     * Get a numeric service provider
     *
     * @return String the service provider
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @throws java.io.IOException if any.
     */
    public String getNumericPage(final User user) throws IOException {
        return getContactServiceProvider(user, ContactType.numericPage.toString());
    }

    /**
     * Get a text pin
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the text pin
     * @throws java.io.IOException if any.
     */
    public String getTextPin(final String userID) throws IOException {
        return getContactInfo(userID, ContactType.textPage.toString());
    }
    
    /**
     * Get a text pin
     *
     * @return String the text pin
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @throws java.io.IOException if any.
     */
    public String getTextPin(final User user) throws IOException {
        return getContactInfo(user, ContactType.textPage.toString());
    }

    /**
     * Get a Text Page Service Provider
     *
     * @param userID
     *            the user ID of the user to return
     * @return String the text page service provider.
     * @throws java.io.IOException if any.
     */
    public String getTextPage(final String userID) throws IOException {
        return getContactServiceProvider(userID, ContactType.textPage.toString());
    }
    
    /**
     * Get a Text Page Service Provider
     *
     * @return String the text page service provider.
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     * @throws java.io.IOException if any.
     */
    public String getTextPage(final User user) throws IOException {
        return getContactServiceProvider(user, ContactType.textPage.toString());
    }
    
    /**
     * Get a work phone number
     *
     * @param userID
     *             the user ID of the user to return
     * @return String the work phone number
     * @throws java.io.IOException if any.
     */
    public String getWorkPhone(final String userID) throws IOException {
        return getContactInfo(userID, ContactType.workPhone.toString());
    }
    
    /**
     * Get a work phone number
     *
     * @return String the work phone number
     * @throws java.io.IOException if any.
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     */
    public String getWorkPhone(final User user) throws IOException {
        return getContactInfo(user, ContactType.workPhone.toString());
    }

    /**
     * Get a mobile phone number
     *
     * @param userID
     *             the user ID of the user to return
     * @return String the mobile phone number
     * @throws java.io.IOException if any.
     */
    public String getMobilePhone(final String userID) throws IOException {
        return getContactInfo(userID, ContactType.mobilePhone.toString());
    }
    
    /**
     * Get a mobile phone number
     *
     * @return String the mobile phone number
     * @throws java.io.IOException if any.
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     */
    public String getMobilePhone(final User user) throws IOException {
        return getContactInfo(user, ContactType.mobilePhone.toString());
    }

    /**
     * Get a home phone number
     *
     * @param userID
     *             the user ID of the user to return
     * @return String the home phone number
     * @throws java.io.IOException if any.
     */
    public String getHomePhone(final String userID) throws IOException {
        return getContactInfo(userID, ContactType.homePhone.toString());
    }
    
    /**
     * Get a home phone number
     *
     * @return String the home phone number
     * @throws java.io.IOException if any.
     * @param user a {@link org.opennms.netmgt.config.users.User} object.
     */
    public String getHomePhone(final User user) throws IOException {
        return getContactInfo(user, ContactType.homePhone.toString());
    }

    /**
     * <p>saveUsers</p>
     *
     * @param usersList a {@link java.util.Collection} object.
     * @throws java.lang.Exception if any.
     */
    public void saveUsers(final Collection<User> usersList) throws Exception {
        update();

        m_writeLock.lock();
        
        try {
            if (!m_allowUnsalted) {
                for (final User details : usersList) {
                    _assertSaltAcceptable(details);
                }
            }

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
        
                // Refresh the groups config first
                m_groupManager.update();
                
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
        final List<User> users = new ArrayList<>(m_users.values());
    
        final Userinfo userinfo = new Userinfo();
        userinfo.setUsers(users);

        final Header header = oldHeader;
        if (header != null) {
            header.setCreated(FORMATTER.format(new Date()));
            userinfo.setHeader(header);
        }
        oldHeader = header;
    
        // marshal to a string first, then write the string to the file. This
        // way the original configuration
        // isn't lost if the XML from the marshal is hosed.
        final StringWriter stringWriter = new StringWriter();
        JaxbUtils.marshal(userinfo, stringWriter);
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
        update();

        m_writeLock.lock();
        
        try {
            // Get the old data
            if (m_users.containsKey(oldName)) {
                final User data = m_users.get(oldName);
                if (data == null) {
                    m_users.remove(oldName);
                    throw new Exception("UserFactory:rename the data contained for old user " + oldName + " is null");
                } else {
                    if (m_users.containsKey(newName)) {
                        throw new Exception("UserFactory: cannot rename user " + oldName + ". An user with the given name " + newName + " already exists");
                    }

                    // Rename the user in the user map.
                    m_users.remove(oldName);
                    data.setUserId(newName);
                    m_users.put(newName, data);
        
                    // Refresh the groups config first
                    m_groupManager.update();
                    
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
        update();

        m_writeLock.lock();
        
        try {
            final User user = m_users.get(userID);
            if (user != null) {
                final Password pass = new Password();
                pass.setEncryptedPassword(aPassword);
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
        update();

        m_writeLock.lock();
        
        try {
            final User user =  m_users.get(userID);
            if (user != null) {
                final Password pass = new Password();
                pass.setEncryptedPassword(encryptedPassword(aPassword, true));
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
            encryptedPassword = s_passwordEncryptor.encryptPassword(aPassword);
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

            final String password = user.getPassword().getEncryptedPassword().trim();
            final boolean isSalted = Boolean.valueOf(user.getPassword().getSalt());
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
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        return passwordEncryptor.checkPassword(raw, encrypted);
    }
    
    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    protected abstract void doUpdate() throws IOException, FileNotFoundException;

    public final void update() throws IOException {
        doUpdate();
    }
    
    /**
     * <p>getUsersWithRole</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     * @throws java.io.IOException if any.
     */
    public String[] getUsersWithRole(final String roleid) throws IOException {
        update();

        m_readLock.lock();
        try {
            return _getUsersWithRole(roleid);
        } finally {
            m_readLock.unlock();
        }
    }

    private String[] _getUsersWithRole(final String roleid) throws IOException {
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
     * @throws java.io.IOException if any.
     */
    public boolean userHasRole(final User user, final String roleid) throws FileNotFoundException, IOException {
        update();

        m_readLock.lock();
        try {
            return _userHasRole(user, roleid);
        } finally {
            m_readLock.unlock();
        }
    }

    private boolean _userHasRole(final User user, final String roleid) throws IOException {
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
     * @throws java.io.IOException if any.
     */
    public boolean isUserScheduledForRole(final User user, final String roleid, final Date time) throws FileNotFoundException, IOException {
        update();
        
        m_readLock.lock();
        try {
            return _isUserScheduledForRole(user, roleid, time);
        } finally {
            m_readLock.unlock();
        }
    }

    private boolean _isUserScheduledForRole(final User user, final String roleid, final Date time) throws IOException {
        if (roleid == null) throw new NullPointerException("roleid is null");
        return m_groupManager.isUserScheduledForRole(user.getUserId(), roleid, time);
    }

    /**
     * <p>getUsersScheduledForRole</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @return an array of {@link java.lang.String} objects.
     * @throws java.io.IOException if any.
     */
    public String[] getUsersScheduledForRole(final String roleid, final Date time) throws IOException {
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
     * <p>hasOnCallRole</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.io.IOException if any.
     */
    public boolean hasOnCallRole(final String roleid) throws IOException {
        update();

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
     * @throws java.io.IOException if any.
     */
    public int countUsersWithRole(final String roleid) throws IOException {
        update();

        m_readLock.lock();
        try {
            final String[] users = _getUsersWithRole(roleid);
            if (users == null) return 0;
            return users.length;
        } finally {
            m_readLock.unlock();
        }
    }

    public abstract boolean isUpdateNeeded();
    public abstract long getLastModified();
    public abstract long getFileSize();
    public abstract void reload() throws IOException, FileNotFoundException;
}
