/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.users.parsers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * This is a data class for storing the information on a user. This information
 * is stored in the users.xml file and is manipulated via the "Users, Groups and
 * Views" screen.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 * @deprecated Use the OnmsUser class instead
 * @since 1.8.1
 */
public class UserInfo extends Object implements Cloneable {
    /**
     * The user id
     */
    private String m_userId;

    /**
     * The full name of the user
     */
    private String m_fullName;

    /**
     * The comments associated with the user
     */
    private String m_userComments;

    /**
     * The password for the user
     */
    private String m_password;

    /**
     * The notification information for the user
     */
    private NotificationInfo m_notifInfo;

    /**
     * Default constructor, intializes the member variables.
     */
    public UserInfo() {
        m_userId = "";
        m_fullName = "";
        m_userComments = "";
        m_password = "";
        m_notifInfo = new NotificationInfo();
    }

    /**
     * <p>clone</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    @Override
    public Object clone() {
        UserInfo clone = new UserInfo();
        clone.setUserId(m_userId);
        clone.setFullName(m_fullName);
        clone.setUserComments(m_userComments);
        clone.setEncryptedPassword(m_password);

        NotificationInfo notifClone = new NotificationInfo();
        notifClone.setEmail(m_notifInfo.getEmail());
        notifClone.setNumericalService(m_notifInfo.getNumericalService());
        notifClone.setNumericalPin(m_notifInfo.getNumericalPin());
        notifClone.setTextService(m_notifInfo.getTextService());
        notifClone.setTextPin(m_notifInfo.getTextPin());
        List<DutySchedule> dutySchedules = m_notifInfo.getDutySchedules();

        for (DutySchedule dutySchedule : dutySchedules) {
            DutySchedule curDutySchedule = new DutySchedule(dutySchedule.toString());
            notifClone.addDutySchedule(curDutySchedule);
        }

        clone.setNotificationInfo(notifClone);

        return clone;
    }

    /**
     * Returns the notification information for this user
     *
     * @return the notification info
     */
    public NotificationInfo getNotificationInfo() {
        return m_notifInfo;
    }

    /**
     * Sets the notificaton information for this user
     *
     * @param someInfo
     *            the notification info
     */
    public void setNotificationInfo(NotificationInfo someInfo) {
        m_notifInfo = someInfo;
    }

    /**
     * Sets the user id for this user
     *
     * @param aUserId
     *            the user id
     */
    public void setUserId(String aUserId) {
        m_userId = aUserId;
    }

    /**
     * Returns the user id for this user
     *
     * @return the user id
     */
    public String getUserId() {
        return m_userId;
    }

    /**
     * Sets the full name for this user
     *
     * @param aFullName
     *            the full name
     */
    public void setFullName(String aFullName) {
        m_fullName = aFullName;
    }

    /**
     * Returns the full name of this user
     *
     * @return the full name
     */
    public String getFullName() {
        return m_fullName;
    }

    /**
     * Sets the user comments for this user
     *
     * @param someUserComments
     *            the user comments
     */
    public void setUserComments(String someUserComments) {
        m_userComments = someUserComments;
    }

    /**
     * Returns the user comments for this user
     *
     * @return the user comments
     */
    public String getUserComments() {
        return m_userComments;
    }

    /**
     * Sets the password for this user, assuming that the value passed in is
     * already encrypted properly
     *
     * @param aPassword
     *            the encrypted password
     */
    public void setEncryptedPassword(String aPassword) {
        m_password = aPassword;
    }

    /**
     * Sets the password for this user, first encrypting it
     *
     * @param aPassword
     *            the password
     * @throws java.lang.IllegalStateException if any.
     */
    public void setUnencryptedPassword(String aPassword) throws IllegalStateException {
        m_password = encryptPassword(aPassword);
    }

    /**
     * This method encrypts the password using MD5 hashing.
     *
     * @param aPassword
     *            the password to encrypt
     * @return the MD5 hash of the password, or null if the encryption fails
     * @throws java.lang.IllegalStateException if any.
     */
    public static String encryptPassword(String aPassword) throws IllegalStateException {
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
     * Converts a byte array into a hexadecimal String representation. The byte
     * array must have an even number of elements (otherwise it would not be
     * convertable to a valid String).
     * 
     * @param data
     *            Array containing the bytes to convert
     * @return the converted string, or null if encoding failed
     */
    private static String hexToString(byte data[]) {
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
     * This method compares two encrypted strings for equality
     *
     * @param aPassword
     *            the password to check for equality
     * @return true if the two passwords are equal (after encryption), false
     *         otherwise
     */
    public boolean comparePasswords(String aPassword) {
        return m_password.equals(encryptPassword(aPassword));
    }

    /**
     * Returns the password for this user
     *
     * @return String, the password for the user
     */
    public String getPassword() {
        return m_password;
    }

    /**
     * Returns a String representation of the user info, used primarily for
     * debugging purposes.
     *
     * @return String, a string representation
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("user id           = " + m_userId + "\n");
        buffer.append("full name         = " + m_fullName + "\n");
        buffer.append("user comments     = " + m_userComments + "\n");
        buffer.append("password          = " + m_password + "\n");
        buffer.append(m_notifInfo.toString());

        return buffer.toString();
    }
}
