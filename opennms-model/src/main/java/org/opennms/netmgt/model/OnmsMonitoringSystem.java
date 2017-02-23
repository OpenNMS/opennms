/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;

import org.hibernate.annotations.DiscriminatorOptions;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>Represents an OpenNMS monitoring system that can poll status of nodes
 * and report events that occur on the network. Examples of monitoring systems
 * include:</p>
 * 
 * <ul>
 * <li>OpenNMS</li>
 * <li>OpenNMS Remote Poller</li>
 * <li>OpenNMS Minion</li>
 * </ul>
 * 
 * <p>CAUTION: Don't add final modifiers to methods here because they need to be
 * proxyable to the child classes and Javassist doesn't override final methods.
 * 
 * @author Seth
 */
@Entity
@Table(name="monitoringSystems")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="type",
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("System")
// Require all objects to have a discriminator type
@DiscriminatorOptions(force=true)
@XmlAccessorType(XmlAccessType.NONE)
public class OnmsMonitoringSystem implements Serializable {

    private static final long serialVersionUID = -5095710111103727832L;

    public static final String TYPE_OPENNMS = "OpenNMS";
    public static final String TYPE_REMOTE_POLLER = "Remote Poller";
    public static final String TYPE_MINION = "Minion";

    @XmlID
    @XmlAttribute(name="id")
    private String m_id;

    @XmlAttribute(name="label")
    private String m_label;

    @XmlAttribute(name="location")
    private String m_location;

    @XmlAttribute(name="type")
    private String m_type;

    // TODO: Add type converter
    //@XmlElement(name="ipAddress")
    //private InetAddress m_ipAddress;

    /*
    @XmlElement(name="status")
    private String m_status;
    */

    @XmlAttribute(name="date")
    private Date m_lastUpdated;
    
    @XmlAttribute(name = "lastCheckedIn")
    private Date m_lastCheckedIn;

    @XmlElementWrapper(name="properties")
    @XmlElement(name="property")
    private Map<String,String> m_properties = new HashMap<String,String>();

    /**
     * default constructor
     */
    public OnmsMonitoringSystem() {}

    /**
     * Minimal constructor.
     *
     * @param id a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     */
    public OnmsMonitoringSystem(String id, String location) {
        m_id = id;
        m_location = location;
    }

    /**
     * A human-readable name for each system.
     * Typically, the system's hostname (not fully qualified).
     *
     * @return a {@link java.lang.String} object.
     */
    @Id 
    @Column(name="id", nullable=false)
    public String getId() {
        return m_id;
    }

    /**
     * <p>setName</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public void setId(String id) {
        m_id = id;
    }

    /**
     * A human-readable name for each system.
     * Typically, the system's hostname (not fully qualified).
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="label")
    public String getLabel() {
        return m_label;
    }

    /**
     * @param label a {@link java.lang.String} object.
     */
    public void setLabel(String label) {
        m_label = label;
    }

    /**
     * The monitoring location that this system is located in.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="location", nullable=false)
    public String getLocation() {
        return m_location;
    }

    /**
     *
     * @param location a {@link java.lang.String} object.
     */
    public void setLocation(String location) {
        m_location = location;
    }

    /**
     * The type of monitoring system. Mark this as insertable=false and updatable=false
     * because it is also used as the @DiscriminatorColumn.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="type", nullable=false, insertable=false, updatable=false)
    public String getType() {
        return m_type;
    }

    /**
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setType(String type) {
        m_type = type;
    }

    /**
     * The timestamp of the last message passed from the remote system.
     * TODO: Should this be nullable=false?
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastUpdated() {
        return m_lastUpdated;
    }

    public void setLastUpdated(final Date lastUpdated) {
        m_lastUpdated = lastUpdated;
    }

    /**
     * IP address of the distributed poller.
     *
     * @return a {@link java.lang.String} object.
     */
    /*
    @Column(name="ipaddr", nullable=false)
    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    public final InetAddress getIpAddress() {
        return m_ipAddress;
    }
    */

    /**
     * <p>setIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    /*
    public final void setIpAddress(InetAddress ipAddress) {
        m_ipAddress = ipAddress;
    }
    */

    
    @ElementCollection
    @JoinTable(name="monitoringSystemsProperties", joinColumns = @JoinColumn(name="monitoringSystemId"))
    @MapKeyColumn(name="property", nullable=false)
    @Column(name="propertyValue")
    public Map<String, String> getProperties() {
        return m_properties;
    }

    /**
     * @param properties a {@link java.util.Map} object.
     */
    public void setProperties(Map<String, String> properties) {
        m_properties = properties;
    }

    public void setProperty(String property, String value) {
        m_properties.put(property, value);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
            .append("id", getId())
            .append("label", getLabel())
            .append("location", getLocation())
            .append("type", getType())
            //.append("ipAddress", str(getIpAddress()))
            .toString();
    }
}
