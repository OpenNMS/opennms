/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.rws;


import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Stand By Url(s) for Rancid Servers.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "standby-url")
@XmlAccessorType(XmlAccessType.FIELD)
public class StandbyUrl implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_DIRECTORY = "/rws";

    @XmlAttribute(name = "server_url", required = true)
    private String server_url;

    @XmlAttribute(name = "timeout")
    private Integer timeout;

    @XmlAttribute(name = "directory")
    private String directory;

    @XmlAttribute(name = "username")
    private String username;

    @XmlAttribute(name = "password")
    private String password;

    /**
     */
    public void deleteTimeout() {
        this.timeout= null;
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof StandbyUrl) {
            StandbyUrl temp = (StandbyUrl)obj;
            boolean equals = Objects.equals(temp.server_url, server_url)
                && Objects.equals(temp.timeout, timeout)
                && Objects.equals(temp.directory, directory)
                && Objects.equals(temp.username, username)
                && Objects.equals(temp.password, password);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'directory'.
     * 
     * @return the value of field 'Directory'.
     */
    public String getDirectory() {
        return this.directory != null ? this.directory : DEFAULT_DIRECTORY;
    }

    /**
     * Returns the value of field 'password'.
     * 
     * @return the value of field 'Password'.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Returns the value of field 'server_url'.
     * 
     * @return the value of field 'Server_url'.
     */
    public String getServer_url() {
        return this.server_url;
    }

    /**
     * Returns the value of field 'timeout'.
     * 
     * @return the value of field 'Timeout'.
     */
    public Integer getTimeout() {
        return this.timeout != null ? this.timeout : Integer.valueOf("3");
    }

    /**
     * Returns the value of field 'username'.
     * 
     * @return the value of field 'Username'.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Method hasTimeout.
     * 
     * @return true if at least one Timeout has been added
     */
    public boolean hasTimeout() {
        return this.timeout != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            server_url, 
            timeout, 
            directory, 
            username, 
            password);
        return hash;
    }

    /**
     * Sets the value of field 'directory'.
     * 
     * @param directory the value of field 'directory'.
     */
    public void setDirectory(final String directory) {
        this.directory = directory;
    }

    /**
     * Sets the value of field 'password'.
     * 
     * @param password the value of field 'password'.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets the value of field 'server_url'.
     * 
     * @param server_url the value of field 'server_url'.
     */
    public void setServer_url(final String server_url) {
        this.server_url = server_url;
    }

    /**
     * Sets the value of field 'timeout'.
     * 
     * @param timeout the value of field 'timeout'.
     */
    public void setTimeout(final Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the value of field 'username'.
     * 
     * @param username the value of field 'username'.
     */
    public void setUsername(final String username) {
        this.username = username;
    }

}
