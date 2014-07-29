package org.opennms.netmgt.model.minion;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

/**
 * @hibernate.class table="minions_properties"
 */
@Table(name="minions_properties", uniqueConstraints = {
        @UniqueConstraint(columnNames={"id", "key"})
})
@Entity
@XmlRootElement(name="property")
@XmlAccessorType(XmlAccessType.NONE)
public class OnmsMinionProperty implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="id")
    private Integer m_id;

    /*
    @XmlIDREF
    @XmlAttribute(name="minion-id")
    */
    @XmlTransient
    private OnmsMinion m_minion;

    @XmlAttribute(name="key")
    private String m_key;

    @XmlValue
    private String m_value;

    public OnmsMinionProperty() {}

    public OnmsMinionProperty(final OnmsMinion minion, final String key, final String value) {
        m_minion = minion;
        m_key = key;
        m_value = value;
    }

    @Id
    @SequenceGenerator(name="minionSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="minionSequence")
    @Column(name="id", nullable=false, unique=true)
    public Integer getId() {
        return m_id;
    }

    void setId(final Integer id) {
        m_id = id;
    }

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="minion_id")
    public OnmsMinion getMinion() {
        return m_minion;
    }

    void setMinion(final OnmsMinion minion) {
        m_minion = minion;
    }

    @Column(name="key", nullable=false)
    public String getKey() {
        return m_key;
    }

    void setKey(final String key) {
        m_key = key;
    }

    @Column(name="value")
    public String getValue() {
        return m_value;
    }

    void setValue(final String value) {
        m_value = value;
    }

    @Override
    public String toString() {
        return "OnmsMinionProperty [id=" + m_id + ", minion=" + m_minion.getId() + ", key=" + m_key + ", value=" + m_value + "]";
    }
}
