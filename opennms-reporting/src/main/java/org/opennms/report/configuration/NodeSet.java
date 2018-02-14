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

package org.opennms.report.configuration;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * Class NodeSet.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "nodeSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeSet implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "devicename")
    private String devicename;

    @XmlElement(name = "groupname")
    private String groupname;

    @XmlElement(name = "configurationurl")
    private String configurationurl;

    @XmlElement(name = "creationdate")
    private String creationdate;

    @XmlElement(name = "status")
    private String status;

    @XmlElement(name = "swconfigurationurl")
    private String swconfigurationurl;

    @XmlElement(name = "version")
    private String version;

    public NodeSet() {
    }

    /**
     * Returns the value of field 'configurationurl'.
     * 
     * @return the value of field 'Configurationurl'.
     */
    public String getConfigurationurl() {
        return this.configurationurl;
    }

    /**
     * Returns the value of field 'creationdate'.
     * 
     * @return the value of field 'Creationdate'.
     */
    public String getCreationdate() {
        return this.creationdate;
    }

    /**
     * Returns the value of field 'devicename'.
     * 
     * @return the value of field 'Devicename'.
     */
    public String getDevicename() {
        return this.devicename;
    }

    /**
     * Returns the value of field 'groupname'.
     * 
     * @return the value of field 'Groupname'.
     */
    public String getGroupname() {
        return this.groupname;
    }

    /**
     * Returns the value of field 'status'.
     * 
     * @return the value of field 'Status'.
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Returns the value of field 'swconfigurationurl'.
     * 
     * @return the value of field 'Swconfigurationurl'.
     */
    public String getSwconfigurationurl() {
        return this.swconfigurationurl;
    }

    /**
     * Returns the value of field 'version'.
     * 
     * @return the value of field 'Version'.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the value of field 'configurationurl'.
     * 
     * @param configurationurl the value of field 'configurationurl'.
     */
    public void setConfigurationurl(final String configurationurl) {
        this.configurationurl = configurationurl;
    }

    /**
     * Sets the value of field 'creationdate'.
     * 
     * @param creationdate the value of field 'creationdate'.
     */
    public void setCreationdate(final String creationdate) {
        this.creationdate = creationdate;
    }

    /**
     * Sets the value of field 'devicename'.
     * 
     * @param devicename the value of field 'devicename'.
     */
    public void setDevicename(final String devicename) {
        this.devicename = devicename;
    }

    /**
     * Sets the value of field 'groupname'.
     * 
     * @param groupname the value of field 'groupname'.
     */
    public void setGroupname(final String groupname) {
        this.groupname = groupname;
    }

    /**
     * Sets the value of field 'status'.
     * 
     * @param status the value of field 'status'.
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * Sets the value of field 'swconfigurationurl'.
     * 
     * @param swconfigurationurl the value of field 'swconfigurationurl'.
     */
    public void setSwconfigurationurl(final String swconfigurationurl) {
        this.swconfigurationurl = swconfigurationurl;
    }

    /**
     * Sets the value of field 'version'.
     * 
     * @param version the value of field 'version'.
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof NodeSet)) {
            return false;
        }
        NodeSet castOther = (NodeSet) other;
        return Objects.equals(devicename, castOther.devicename) && Objects.equals(groupname, castOther.groupname)
                && Objects.equals(configurationurl, castOther.configurationurl)
                && Objects.equals(creationdate, castOther.creationdate) && Objects.equals(status, castOther.status)
                && Objects.equals(swconfigurationurl, castOther.swconfigurationurl)
                && Objects.equals(version, castOther.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(devicename, groupname, configurationurl, creationdate, status, swconfigurationurl, version);
    }

}
