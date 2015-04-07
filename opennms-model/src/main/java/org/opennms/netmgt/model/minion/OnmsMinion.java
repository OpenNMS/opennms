package org.opennms.netmgt.model.minion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>The OnmsMinion represents a minion node which has reported to OpenNMS.</p>
 *
 * @hibernate.class table="minions"
 */
@Entity
@Table(name="minions")
@XmlRootElement(name="minion")
@XmlAccessorType(XmlAccessType.NONE)
public class OnmsMinion {
    @XmlID
    @XmlAttribute(name="id")
    private String m_id;

    @XmlElement(name="location")
    private String m_location;

    @XmlElement(name="status")
    private String m_status;

    @XmlElement(name="date")
    private Date m_lastUpdated;

    @XmlElementWrapper(name="properties")
    @XmlElement(name="property")
    private List<OnmsMinionProperty> m_properties = new ArrayList<OnmsMinionProperty>();

    public OnmsMinion() {
    }

    public OnmsMinion(final String id, final String location, final String status, final Date lastUpdated) {
        m_id          = id;
        m_location    = location;
        m_status      = status;
        m_lastUpdated = lastUpdated;
    }

    @Id
    @Column(name="id", nullable=false, length=36, unique=true)
    public String getId() {
        return m_id;
    }

    public void setId(final String id) {
        m_id = id;
    }

    @Column(name="location", nullable=false)
    public String getLocation() {
        return m_location;
    }

    public void setLocation(final String location) {
        m_location = location;
    }

    @Column(name="status")
    public String getStatus() {
        return m_status;
    }

    public void setStatus(final String status) {
        m_status = status;
    }

    @Column(name="last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastUpdated() {
        return m_lastUpdated;
    }

    public void setLastUpdated(final Date lastUpdated) {
        m_lastUpdated = lastUpdated;
    }

    @Transient
    public Map<String,String> getProperties() {
        final Map<String,String> properties = new HashMap<String,String>();
        for (final OnmsMinionProperty prop : getMinionProperties()) {
            properties.put(prop.getKey(), prop.getValue());
        }
        return properties;
    }

    public OnmsMinionProperty getProperty(final String key) {
        for (final OnmsMinionProperty prop : getMinionProperties()) {
            if (prop.getKey().equals(key)) {
                return prop;
            }
        }
        return null;
    }

    public void setProperty(final String key, final String value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null!");
        }
        for (final OnmsMinionProperty prop : getMinionProperties()) {
            if (prop.getKey().equals(key)) {
                prop.setValue(value);
                return;
            }
        }
        final OnmsMinionProperty prop = new OnmsMinionProperty(this, key, value);
        m_properties.add(prop);
    }

    public boolean removeProperty(final OnmsMinionProperty prop) {
        return m_properties.remove(prop);
    }

    public OnmsMinionProperty removeProperty(final String key) {
        OnmsMinionProperty removed = null;
        for (final OnmsMinionProperty prop : getMinionProperties()) {
            if (prop.getKey().equals(key)) {
                removed = prop;
                break;
            }
        }

        if (removed != null) {
            m_properties.remove(removed);
        }
        return removed;
    }

    @OneToMany(cascade=CascadeType.ALL, mappedBy="minion", fetch=FetchType.EAGER)
    protected List<OnmsMinionProperty> getMinionProperties() {
        if (m_properties == null) {
            return Collections.emptyList();
        }
        return m_properties;
    }

    protected void setMinionProperties(final List<OnmsMinionProperty> properties) {
        m_properties = properties;
    }

    @Override
    public String toString() {
        return "OnmsMinion [id=" + m_id + ", location=" + m_location + ", status=" + m_status + ", lastUpdated=" + m_lastUpdated + ", properties=" + m_properties + "]";
    }
}
