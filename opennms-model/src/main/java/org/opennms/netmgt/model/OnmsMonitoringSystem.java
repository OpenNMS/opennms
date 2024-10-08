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

import com.google.common.base.MoreObjects;

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
    public static final String TYPE_MINION = "Minion";
    public static final String TYPE_SENTINEL = "Sentinel";

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
        return MoreObjects.toStringHelper(this)
            .add("id", getId())
            .add("label", getLabel())
            .add("location", getLocation())
            .add("type", getType())
            //.add("ipAddress", str(getIpAddress()))
            .toString();
    }
}
