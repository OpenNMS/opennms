/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.vmware;

import org.apache.commons.lang.builder.EqualsBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A VMware Server entry
 */
@XmlRootElement(name = "vmware-server")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class VmwareServer implements java.io.Serializable {

    /**
     * The hostname of IP address of this server
     */
    @XmlAttribute(name = "hostname")
    private java.lang.String _hostname;

    /**
     * The username of the read-only user
     */
    @XmlAttribute(name = "username")
    private java.lang.String _username;

    /**
     * The password of the read-only user
     */
    @XmlAttribute(name = "password")
    private java.lang.String _password;

    public VmwareServer() {
        super();
    }


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Overrides the java.lang.Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final java.lang.Object obj) {
        if (obj instanceof VmwareServer) {
            VmwareServer other = (VmwareServer) obj;
            return new EqualsBuilder()
                    .append(getHostname(), other.getHostname())
                    .append(getUsername(), other.getUsername())
                    .append(getPassword(), other.getPassword())
                    .isEquals();
        }
        return false;
    }

    /**
     * Returns the value of field 'hostname'. The field 'hostname'
     * has the following description: The hostname of IP address of
     * this server
     *
     * @return the value of field 'Hostname'.
     */
    public java.lang.String getHostname(
    ) {
        return this._hostname == null ? "" : this._hostname;
    }

    /**
     * Returns the value of field 'password'. The field 'password'
     * has the following description: The password of the read-only
     * user
     *
     * @return the value of field 'Password'.
     */
    public java.lang.String getPassword(
    ) {
        return this._password == null ? "" : this._password;
    }

    /**
     * Returns the value of field 'username'. The field 'username'
     * has the following description: The username of the read-only
     * user
     *
     * @return the value of field 'Username'.
     */
    public java.lang.String getUsername(
    ) {
        return this._username == null ? "" : this._username;
    }

    /**
     * Sets the value of field 'hostname'. The field 'hostname' has
     * the following description: The hostname of IP address of
     * this server
     *
     * @param hostname the value of field 'hostname'.
     */
    public void setHostname(
            final java.lang.String hostname) {
        this._hostname = hostname;
    }

    /**
     * Sets the value of field 'password'. The field 'password' has
     * the following description: The password of the read-only
     * user
     *
     * @param password the value of field 'password'.
     */
    public void setPassword(
            final java.lang.String password) {
        this._password = password;
    }

    /**
     * Sets the value of field 'username'. The field 'username' has
     * the following description: The username of the read-only
     * user
     *
     * @param username the value of field 'username'.
     */
    public void setUsername(
            final java.lang.String username) {
        this._username = username;
    }

}
