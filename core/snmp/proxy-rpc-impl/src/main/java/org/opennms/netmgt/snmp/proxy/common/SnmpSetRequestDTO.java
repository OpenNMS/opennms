package org.opennms.netmgt.snmp.proxy.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpObjIdXmlAdapter;
import org.opennms.netmgt.snmp.SnmpValue;

@XmlRootElement(name="snmp-set-request")
@XmlAccessorType(XmlAccessType.NONE)
public class SnmpSetRequestDTO {

    @XmlAttribute(name="correlation-id")
    private String correlationId;

    @XmlElement(name="oid")
    @XmlJavaTypeAdapter(SnmpObjIdXmlAdapter.class)
    private List<SnmpObjId> oids = new ArrayList<>(0);

    @XmlElement(name="value")
    @XmlJavaTypeAdapter(SnmpObjIdXmlAdapter.class)
    private List<SnmpValue> values = new ArrayList<>(0);

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public List<SnmpObjId> getOids() {
        return oids;
    }

    public void setOids(List<SnmpObjId> oids) {
        this.oids = oids;
    }

    public List<SnmpValue> getValues() {
        return values;
    }

    public void setValues(List<SnmpValue> values) {
        this.values = values;
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, oids, values);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SnmpSetRequestDTO other = (SnmpSetRequestDTO) obj;
        return Objects.equals(this.correlationId, other.correlationId)
                && Objects.equals(this.oids, other.oids)
                && Objects.equals(this.values, other.values);
    }
}
