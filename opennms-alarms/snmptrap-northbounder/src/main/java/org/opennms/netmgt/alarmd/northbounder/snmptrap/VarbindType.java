package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.opennms.netmgt.events.api.EventConstants;

/**
 * The Enumeration VarbindType.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlType
@XmlEnum(String.class)
public enum VarbindType {

    @XmlEnumValue(EventConstants.TYPE_SNMP_OCTET_STRING)
    TYPE_SNMP_OCTET_STRING(EventConstants.TYPE_SNMP_OCTET_STRING),

    @XmlEnumValue(EventConstants.TYPE_SNMP_INT32)
    TYPE_SNMP_INT32(EventConstants.TYPE_SNMP_INT32),

    @XmlEnumValue(EventConstants.TYPE_SNMP_NULL)
    TYPE_SNMP_NULL(EventConstants.TYPE_SNMP_NULL),

    @XmlEnumValue(EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER)
    TYPE_SNMP_OBJECT_IDENTIFIER(EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER),

    @XmlEnumValue(EventConstants.TYPE_SNMP_IPADDRESS)
    TYPE_SNMP_IPADDRESS(EventConstants.TYPE_SNMP_IPADDRESS),

    @XmlEnumValue(EventConstants.TYPE_SNMP_TIMETICKS)
    TYPE_SNMP_TIMETICKS(EventConstants.TYPE_SNMP_TIMETICKS),

    @XmlEnumValue(EventConstants.TYPE_SNMP_COUNTER32)
    TYPE_SNMP_COUNTER32(EventConstants.TYPE_SNMP_COUNTER32),

    @XmlEnumValue(EventConstants.TYPE_SNMP_GAUGE32)
    TYPE_SNMP_GAUGE32(EventConstants.TYPE_SNMP_GAUGE32),

    @XmlEnumValue(EventConstants.TYPE_SNMP_OPAQUE)
    TYPE_SNMP_OPAQUE(EventConstants.TYPE_SNMP_OPAQUE),

    @XmlEnumValue(EventConstants.TYPE_SNMP_COUNTER64)
    TYPE_SNMP_COUNTER64(EventConstants.TYPE_SNMP_COUNTER64);

    /** The varbind type. */
    private String m_type;

    /**
     * Instantiates a new Varbind Type.
     *
     * @param type the Varbind as string
     */
    VarbindType(String type) {
        m_type = type;
    }

    /**
     * Gets the Varbind Type as string.
     *
     * @return the Varbind Type as string
     */
    public String value() {
        return m_type;
    }

}

