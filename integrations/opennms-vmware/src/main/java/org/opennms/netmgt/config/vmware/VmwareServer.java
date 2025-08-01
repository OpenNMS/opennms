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
