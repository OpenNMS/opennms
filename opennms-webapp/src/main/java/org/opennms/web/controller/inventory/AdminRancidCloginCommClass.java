/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.inventory;

/**
 * <p>AdminRancidCloginCommClass class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class AdminRancidCloginCommClass {

    private String userID;
    private String pass;
    private String enpass;
    private String loginM;
    private String autoE;
    private String groupName;
    private String deviceName;
    
    /**
     * <p>Getter for the field <code>userID</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUserID() {
        return userID;
    }

    /**
     * <p>Setter for the field <code>userID</code>.</p>
     *
     * @param userID a {@link java.lang.String} object.
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * <p>Getter for the field <code>pass</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPass() {
        return pass;
    }

    /**
     * <p>Setter for the field <code>pass</code>.</p>
     *
     * @param pass a {@link java.lang.String} object.
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * <p>Getter for the field <code>enpass</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getEnpass() {
        return enpass;
    }

    /**
     * <p>Setter for the field <code>enpass</code>.</p>
     *
     * @param enpass a {@link java.lang.String} object.
     */
    public void setEnpass(String enpass) {
        this.enpass = enpass;
    }

    /**
     * <p>Getter for the field <code>loginM</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLoginM() {
        return loginM;
    }

    /**
     * <p>Setter for the field <code>loginM</code>.</p>
     *
     * @param loginM a {@link java.lang.String} object.
     */
    public void setLoginM(String loginM) {
        this.loginM = loginM;
    }

    /**
     * <p>Getter for the field <code>autoE</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAutoE() {
        return autoE;
    }

    /**
     * <p>Setter for the field <code>autoE</code>.</p>
     *
     * @param autoE a {@link java.lang.String} object.
     */
    public void setAutoE(String autoE) {
        this.autoE = autoE;
    }

    /**
     * <p>Getter for the field <code>groupName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * <p>Setter for the field <code>groupName</code>.</p>
     *
     * @param groupName a {@link java.lang.String} object.
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * <p>Getter for the field <code>deviceName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * <p>Setter for the field <code>deviceName</code>.</p>
     *
     * @param deviceName a {@link java.lang.String} object.
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
}
