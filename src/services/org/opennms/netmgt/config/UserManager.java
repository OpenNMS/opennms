//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.netmgt.config.users.Header;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.config.users.Users;

/**
 * @author david hustace <david@opennms.org>
 */
public abstract class UserManager {

    protected GroupManager m_groupManager;
    /**
     * A mapping of user ids to the User objects
     */
    protected HashMap m_users;
    /**
     * The duty schedules for each user
     */
    protected HashMap m_dutySchedules;
    private Header oldHeader;

    protected UserManager(GroupManager groupManager) {
        m_groupManager = groupManager;
    }
    /**
     * @param reader
     * @throws MarshalException
     * @throws ValidationException
     */
    public void parseXML(Reader reader) throws MarshalException, ValidationException {
        Userinfo userinfo = (Userinfo) Unmarshaller.unmarshal(Userinfo.class, reader);
        Users users = userinfo.getUsers();
        oldHeader = userinfo.getHeader();
        Collection usersList = users.getUserCollection();
        m_users = new HashMap();
    
        Iterator i = usersList.iterator();
        while (i.hasNext()) {
            User curUser = (User) i.next();
            m_users.put(curUser.getUserId(), curUser);
        }
    
        buildDutySchedules(m_users);
    }

    /**
     * Adds a new user and overwrites the "users.xml"
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
     * Builds a mapping between user ids and duty schedules. These are used by
     * Notifd when determining to send a notice to a given user. This helps
     * speed up the decision process.
     * 
     * @param users
     *            the map of users parsed from the xml config file
     */
    private void buildDutySchedules(Map users) {
        m_dutySchedules = new HashMap();
        Iterator i = users.keySet().iterator();
    
        while (i.hasNext()) {
            String key = (String) i.next();
            User curUser = (User) users.get(key);
    
            if (curUser.getDutyScheduleCount() > 0) {
                List dutyList = new ArrayList();
                Enumeration duties = curUser.enumerateDutySchedule();
    
                while (duties.hasMoreElements()) {
                    dutyList.add(new DutySchedule((String) duties.nextElement()));
                }
    
                m_dutySchedules.put(key, dutyList);
            }
        }
    }

    /**
     * Determines if a user is on duty at a given time. If a user has no duty
     * schedules listed in the config file, that user is assumed to always be on
     * duty.
     * 
     * @param user
     *            the user id
     * @param time
     *            the time to check for a duty schedule
     * @return boolean, true if the user is on duty, false otherwise.
     */
    public boolean isUserOnDuty(String user, Calendar time) throws IOException, MarshalException, ValidationException {
    
        update();
    
        // if the user has no duty schedules then he is on duty
        if (!m_dutySchedules.containsKey(user))
            return true;
    
        boolean result = false;
        List dutySchedules = (List) m_dutySchedules.get(user);
    
        for (int i = 0; i < dutySchedules.size(); i++) {
            DutySchedule curSchedule = (DutySchedule) dutySchedules.get(i);
    
            result = curSchedule.isInSchedule(time);
    
            // don't continue if the time is in this schedule
            if (result)
                break;
        }
    
        return result;
    }

    /**
     * Return a <code>Map</code> of usernames to user instances.
     */
    public Map getUsers() throws IOException, MarshalException, ValidationException {
    
        update();
    
        return m_users;
    }

    /**
     * Returns a boolean indicating if the user name appears in the xml file
     * 
     * @return true if the user exists in the xml file, false otherwise
     */
    public boolean hasUser(String userName) throws IOException, MarshalException, ValidationException {
    
        update();
    
        return m_users.containsKey(userName);
    }

    /**
     */
    public List getUserNames() throws IOException, MarshalException, ValidationException {
    
        update();
    
        List userNames = new ArrayList();
    
        Iterator i = m_users.keySet().iterator();
    
        while (i.hasNext()) {
            userNames.add((String) i.next());
        }
    
        return userNames;
    }

    /**
     * Get a user by name
     * 
     * @param name
     *            the name of the user to return
     * @return the user specified by name
     */
    public User getUser(String name) throws IOException, MarshalException, ValidationException {
    
        update();
    
        return (User) m_users.get(name);
    }

    /**
     * Get the contact info given a command string
     * 
     * @param userID
     *            the name of the user
     * @param command
     *            the command to look up the contact info for
     * @return the contact information
     */
    public String getContactInfo(String userID, String command) throws IOException, MarshalException, ValidationException {
        update();
    
        User user = (User) m_users.get(userID);
        if (user == null)
            return "";
        String value = "";
        Enumeration contacts = user.enumerateContact();
        while (contacts != null && contacts.hasMoreElements()) {
            Contact contact = (Contact) contacts.nextElement();
            if (contact != null) {
                if (contact.getType().equals(command)) {
                    value = contact.getInfo();
                    break;
                }
            }
        }
        return value;
    }

    /**
     * Get a email by name
     * 
     * @param userid
     *            the userid of the user to return
     * @return String the email specified by name
     */
    public String getEmail(String userid) throws IOException, MarshalException, ValidationException {
    
        update();
    
        User user = (User) m_users.get(userid);
        if (user == null)
            return "";
        String value = "";
        Enumeration contacts = user.enumerateContact();
        while (contacts != null && contacts.hasMoreElements()) {
            Contact contact = (Contact) contacts.nextElement();
            if (contact != null) {
                if (contact.getType().equals("email")) {
                    value = contact.getInfo();
                    break;
                }
            }
        }
        return value;
    }

    /**
     * Get a pager email by name
     * 
     * @param userid
     *            the userid of the user to return
     * @return String the pager email
     */
    public String getPagerEmail(String userid) throws IOException, MarshalException, ValidationException {
    
        update();
    
        User user = (User) m_users.get(userid);
        if (user == null)
            return "";
        String value = "";
        Enumeration contacts = user.enumerateContact();
        while (contacts != null && contacts.hasMoreElements()) {
            Contact contact = (Contact) contacts.nextElement();
            if (contact != null) {
                if (contact.getType().equals("pagerEmail")) {
                    value = contact.getInfo();
                    break;
                }
            }
        }
        return value;
    }

    /**
     * Get an XMPP address by name
     * 
     * @param userid
     *            the userid of the user to return
     * @return String the XMPP address
     */

    public String getXMPPAddress(String userid) throws IOException, MarshalException, ValidationException {

        update();
        
        User user = (User) m_users.get(userid);
        if (user == null)
            return "";
        String value = "";
        Enumeration contacts = user.enumerateContact();
        while (contacts != null && contacts.hasMoreElements()) {
            Contact contact = (Contact) contacts.nextElement();
            if (contact != null) {
            	if (contact.getType().equals("xmppAddress")) {
            		value = contact.getInfo();
            		break;
            	}
            }
        }
        return value;

    }

    /**
     * Get a numeric service provider
     * 
     * @param userid
     *            the userid of the user to return
     * @return String the service provider
     */
    public String getNumericPage(String userid) throws IOException, MarshalException, ValidationException {
    
        update();
    
        User user = (User) m_users.get(userid);
        if (user == null)
            return "";
        String value = "";
        Enumeration contacts = user.enumerateContact();
        while (contacts != null && contacts.hasMoreElements()) {
            Contact contact = (Contact) contacts.nextElement();
            if (contact != null) {
                if (contact.getType().equals("numericPage")) {
                    value = contact.getServiceProvider();
                    break;
                }
            }
        }
        return value;
    }

    /**
     * Get a text pin
     * 
     * @param userid
     *            the userid of the user to return
     * @return String the text pin
     */
    public String getTextPin(String userid) throws IOException, MarshalException, ValidationException {
    
        update();
    
        User user = (User) m_users.get(userid);
        if (user == null)
            return "";
        String value = "";
        Enumeration contacts = user.enumerateContact();
        while (contacts != null && contacts.hasMoreElements()) {
            Contact contact = (Contact) contacts.nextElement();
            if (contact != null) {
                if (contact.getType().equals("textPage")) {
                    value = contact.getInfo();
                    break;
                }
            }
        }
        return value;
    }

    /**
     * Get a Text Page Service Provider
     * 
     * @param userid
     *            the userid of the user to return
     * @return String the text page service provider.
     */
    public String getTextPage(String userid) throws IOException, MarshalException, ValidationException {
    
        update();
    
        User user = (User) m_users.get(userid);
        if (user == null)
            return "";
        String value = "";
        Enumeration contacts = user.enumerateContact();
        while (contacts != null && contacts.hasMoreElements()) {
            Contact contact = (Contact) contacts.nextElement();
            if (contact != null) {
                if (contact.getType().equals("textPage")) {
                    value = contact.getServiceProvider();
                    break;
                }
            }
        }
        return value;
    }

    /**
     * Get a numeric pin
     * 
     * @param userid
     *            the userid of the user to return
     * @return String the numeric pin
     */
    public String getNumericPin(String userid) throws IOException, MarshalException, ValidationException {
    
        update();
    
        User user = (User) m_users.get(userid);
        if (user == null)
            return "";
        String value = "";
        Enumeration contacts = user.enumerateContact();
        while (contacts != null && contacts.hasMoreElements()) {
            Contact contact = (Contact) contacts.nextElement();
            if (contact != null) {
                if (contact.getType().equals("numericPage")) {
                    value = contact.getInfo();
                    break;
                }
            }
        }
        return value;
    }

    /**
     */
    public synchronized void saveUsers(Collection usersList) throws Exception {
        // clear out the interanal structure and reload it
        m_users.clear();
    
        Iterator i = usersList.iterator();
        while (i.hasNext()) {
            User curUser = (User) i.next();
            m_users.put(curUser.getUserId(), curUser);
        }
    
    }

    /**
     * Removes the user from the list of users. Then overwrites to the
     * "users.xml"
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
        Collection collUsers = (Collection) m_users.values();
        Iterator iter = collUsers.iterator();
        while (iter != null && iter.hasNext()) {
            User tmpUser = (User) iter.next();
            users.addUser(tmpUser);
        }
    
        Userinfo userinfo = new Userinfo();
        userinfo.setUsers(users);
        userinfo.setHeader(header);
    
        oldHeader = header;
    
        // marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the xml from the marshall is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(userinfo, stringWriter);
        String writerString = stringWriter.toString();
        saveXML(writerString);
    }

    /**
     * @param writerString
     * @throws IOException
     */
    protected abstract void saveXML(String writerString) throws IOException ;

    /**
     * When this method is called users name is changed, so also is the username
     * belonging to the group and the view. Also overwrites the "users.xml" file
     */
    public synchronized void renameUser(String oldName, String newName) throws Exception {
        // Get the old data
        if (m_users.containsKey(oldName)) {
            User data = (User) m_users.get(oldName);
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
     *            the user ID to change the pasword for
     * @param aPassword
     *            the encrypted password
     */
    public void setEncryptedPassword(String userID, String aPassword) throws Exception {
        User user = (User) m_users.get(userID);
        if (user != null) {
            user.setPassword(aPassword);
        }
    
        saveCurrent();
    }

    /**
     * Sets the password for this user, first encrypting it
     * 
     * @param userID
     *            the user ID to change the pasword for
     * @param aPassword
     *            the password
     */
    public void setUnencryptedPassword(String userID, String aPassword) throws Exception {
        User user = (User) m_users.get(userID);
        if (user != null) {
            user.setPassword(encryptedPassword(aPassword));
        }
    
        saveCurrent();
    }

    /**
     * @param aPassword
     * @return
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
        User user = (User) m_users.get(userID);
        if (user == null)
            return false;
    
        return user.getPassword().equals(encryptedPassword(aPassword));
    }

    /**
     * @throws IOException
     * @throws FileNotFoundException
     * @throws MarshalException
     * @throws ValidationException
     */
    protected abstract void update() throws IOException, FileNotFoundException, MarshalException, ValidationException;

}
