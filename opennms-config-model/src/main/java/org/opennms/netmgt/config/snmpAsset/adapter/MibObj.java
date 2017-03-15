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

package org.opennms.netmgt.config.snmpAsset.adapter;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * a MIB object
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "mibObj")
@XmlAccessorType(XmlAccessType.FIELD)
public class MibObj implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * object identifier
     */
    @XmlAttribute(name = "oid", required = true)
    private String oid;

    /**
     * a human readable name for the object (such as
     *  "ifOctetsIn"). NOTE: This value is used as the RRD file name and
     *  data source name. RRD only supports data source names up to 19 chars
     *  in length. If the SNMP data collector encounters an alias which
     *  exceeds 19 characters it will be truncated.
     */
    @XmlAttribute(name = "alias", required = true)
    private String alias;

    public MibObj() {
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof MibObj) {
            MibObj temp = (MibObj)obj;
            boolean equals = Objects.equals(temp.oid, oid)
                && Objects.equals(temp.alias, alias);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'alias'. The field 'alias' has the following
     * description: a human readable name for the object (such as
     *  "ifOctetsIn"). NOTE: This value is used as the RRD file name and
     *  data source name. RRD only supports data source names up to 19 chars
     *  in length. If the SNMP data collector encounters an alias which
     *  exceeds 19 characters it will be truncated.
     * 
     * @return the value of field 'Alias'.
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * Returns the value of field 'oid'. The field 'oid' has the following
     * description: object identifier
     * 
     * @return the value of field 'Oid'.
     */
    public String getOid() {
        return this.oid;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            oid, 
            alias);
        return hash;
    }

    /**
     * Sets the value of field 'alias'. The field 'alias' has the following
     * description: a human readable name for the object (such as
     *  "ifOctetsIn"). NOTE: This value is used as the RRD file name and
     *  data source name. RRD only supports data source names up to 19 chars
     *  in length. If the SNMP data collector encounters an alias which
     *  exceeds 19 characters it will be truncated.
     * 
     * @param alias the value of field 'alias'.
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    /**
     * Sets the value of field 'oid'. The field 'oid' has the following
     * description: object identifier
     * 
     * @param oid the value of field 'oid'.
     */
    public void setOid(final String oid) {
        this.oid = oid;
    }

}
