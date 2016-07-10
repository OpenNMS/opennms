package org.opennms.netmgt.provision.detector.common;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "detector-attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class DetectorAttributeDTO {

    @XmlAttribute(name = "key")
    private String key;

    @XmlValue
    private String value;

    public DetectorAttributeDTO() {
        // Default constructor for JAXB
    }

    public DetectorAttributeDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DetectorAttributeDTO other = (DetectorAttributeDTO) obj;
        return Objects.equals(this.key, other.key)
                && Objects.equals(this.value, other.value);
    }
}
