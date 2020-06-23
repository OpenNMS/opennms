/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.datacollection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.internal.collection.DatacollectionConfigVisitor;

/**
 * a MIB object
 */

@XmlRootElement(name="mibObj", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"m_oid", "m_instance", "m_alias", "m_type", "m_maxval", "m_minval"})
@ValidateUsing("datacollection-config.xsd")
public class MibObj implements java.io.Serializable {
    private static final long serialVersionUID = -7133492973783810404L;

    /**
     * object identifier
     */
    @XmlAttribute(name="oid", required=true)
    private String m_oid;

    /**
     * instance identifier. Only valid instance identifier values are a
     * positive integer value or the keyword "ifIndex" which indicates that
     * the ifIndex of the interface is to be substituted for the instance
     * value for each interface the oid is retrieved for.
     */
    @XmlAttribute(name="instance", required=true)
    private String m_instance;

    /**
     * a human readable name for the object (such as "ifOctetsIn"). NOTE: This
     * value is used as the RRD file name and data source name. RRD only
     * supports data source names up to 19 chars in length. If the SNMP data
     * collector encounters an alias which exceeds 19 characters it will be
     * truncated.
     */
    @XmlAttribute(name="alias", required=true)
    private String m_alias;

    /**
     * SNMP data type SNMP supported types: counter, gauge, timeticks,
     * integer, octetstring, string. The SNMP type is mapped to one of two RRD
     * supported data types COUNTER or GAUGE, or the string.properties file.
     * The mapping is as follows: SNMP counter -&gt; RRD COUNTER; SNMP gauge,
     * timeticks, integer, octetstring -&gt; RRD GAUGE; SNMP string -&gt; String
     * properties file
     */
    @XmlAttribute(name="type", required=true)
    private String m_type;

    /**
     * Maximum Value. In order to correctly manage counter wraps, it is
     * possible to add a maximum value for a collection. For example, a 32-bit
     * counter would have a max value of 4294967295.
     */
    @XmlAttribute(name="maxval", required=false)
    private String m_maxval;

    /**
     * Minimum Value. For completeness, adding the ability to use a minimum
     * value.
     */
    @XmlAttribute(name="minval", required=false)
    private String m_minval;


    public MibObj() {
        super();
    }

    public MibObj(final String oid, final String instance, final String alias, final String type) {
        super();
        m_oid = oid == null? null : oid.intern();
        m_instance = instance == null? null : instance.intern();
        m_alias = alias == null? null : alias.intern();
        m_type = type == null? null : type.intern();
    }

    /**
     * object identifier
     */
    public String getOid() {
        return m_oid;
    }

    public void setOid(final String oid) {
        m_oid = oid == null? null : oid.intern();
    }

    /**
     * instance identifier. Only valid instance identifier values are a
     * positive integer value or the keyword "ifIndex" which indicates that
     * the ifIndex of the interface is to be substituted for the instance
     * value for each interface the oid is retrieved for.
     */
    public String getInstance() {
        return m_instance;
    }

    public void setInstance(final String instance) {
        m_instance = instance == null? null : instance.intern();
    }

    /**
     * a human readable name for the object (such as "ifOctetsIn"). NOTE: This
     * value is used as the RRD file name and data source name. RRD only
     * supports data source names up to 19 chars in length. If the SNMP data
     * collector encounters an alias which exceeds 19 characters it will be
     * truncated.
     */
    public String getAlias() {
        return m_alias;
    }

    public void setAlias(final String alias) {
        m_alias = alias == null? null : alias.intern();
    }

    /**
     * SNMP data type SNMP supported types: counter, gauge, timeticks,
     * integer, octetstring, string. The SNMP type is mapped to one of two RRD
     * supported data types COUNTER or GAUGE, or the string.properties file.
     * The mapping is as follows: SNMP counter -&gt; RRD COUNTER; SNMP gauge,
     * timeticks, integer, octetstring -&gt; RRD GAUGE; SNMP string -&gt; String
     * properties file
     */
    public String getType() {
        return m_type;
    }

    public void setType(final String type) {
        m_type = type == null? type : type.intern();
    }

    /**
     * Maximum Value. In order to correctly manage counter wraps, it is
     * possible to add a maximum value for a collection. For example, a 32-bit
     * counter would have a max value of 4294967295.
     */
    public String getMaxval() {
        return m_maxval;
    }

    public void setMaxval(final String maxval) {
        m_maxval = maxval == null? null : maxval.intern();
    }

    /**
     * Minimum Value. For completeness, adding the ability to use a minimum
     * value.
     */
    public String getMinval() {
        return m_minval;
    }

    public void setMinval(final String minval) {
        m_minval = minval == null? null : minval.intern();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_alias == null) ? 0 : m_alias.hashCode());
        result = prime * result + ((m_instance == null) ? 0 : m_instance.hashCode());
        result = prime * result + ((m_maxval == null) ? 0 : m_maxval.hashCode());
        result = prime * result + ((m_minval == null) ? 0 : m_minval.hashCode());
        result = prime * result + ((m_oid == null) ? 0 : m_oid.hashCode());
        result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MibObj)) {
            return false;
        }
        final MibObj other = (MibObj) obj;
        if (m_alias == null) {
            if (other.m_alias != null) {
                return false;
            }
        } else if (!m_alias.equals(other.m_alias)) {
            return false;
        }
        if (m_instance == null) {
            if (other.m_instance != null) {
                return false;
            }
        } else if (!m_instance.equals(other.m_instance)) {
            return false;
        }
        if (m_maxval == null) {
            if (other.m_maxval != null) {
                return false;
            }
        } else if (!m_maxval.equals(other.m_maxval)) {
            return false;
        }
        if (m_minval == null) {
            if (other.m_minval != null) {
                return false;
            }
        } else if (!m_minval.equals(other.m_minval)) {
            return false;
        }
        if (m_oid == null) {
            if (other.m_oid != null) {
                return false;
            }
        } else if (!m_oid.equals(other.m_oid)) {
            return false;
        }
        if (m_type == null) {
            if (other.m_type != null) {
                return false;
            }
        } else if (!m_type.equals(other.m_type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MibObj [oid=" + m_oid + ", instance=" + m_instance + ", alias=" + m_alias + ", type=" + m_type + ", maxval=" + m_maxval + ", minval=" + m_minval + "]";
    }

    public void visit(final DatacollectionConfigVisitor visitor) {
        visitor.visitMibObj(this);
        visitor.visitMibObjComplete();
    }

}
