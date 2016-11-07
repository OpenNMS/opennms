package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="property")
@XmlAccessorType(XmlAccessType.NONE)
public class ScanReportProperty implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="name")
    private String m_name;
    @XmlValue
    private String m_value;

    public ScanReportProperty() {}
    public ScanReportProperty(final String name, final String value) {
        m_name = name;
        m_value = value;
    }

    public ScanReportProperty(final Entry<String, String> entry) {
        m_name = entry.getKey();
        m_value = entry.getValue();
    }

    public String getName() {
        return m_name;
    }
    public String getValue() {
        return m_value;
    }
    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_value);
    }
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ScanReportProperty)) {
            return false;
        }
        final ScanReportProperty that = (ScanReportProperty)obj;
        return Objects.equals(this.m_name, that.m_name) && Objects.equals(this.m_value, that.m_value);
    }

    @Override
    public String toString() {
        return "ScanReportProperty [" + m_name + "=" + m_value + "]";
    }
}