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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * The Class HwEntityAttributeType.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "hwEntityAttributeType")
@Entity
@Table(name="hwEntityAttributeType")
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HwEntityAttributeType implements Serializable, Comparable<HwEntityAttributeType> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -136267386674546238L;

    /** The id. */
    private Integer m_id;

    /** The attribute OID. */
    private String m_attributeOid;

    /** The attribute name. */
    private String m_attributeName;

    /** The attribute class. */
    private String m_attributeClass = "string";

    /**
     * The Constructor.
     */
    public HwEntityAttributeType() {
    }

    /**
     * The Constructor.
     *
     * @param attributeOid the attribute OID
     * @param attributeName the attribute name
     * @param attributeClass the attribute class
     */
    public HwEntityAttributeType(String attributeOid, String attributeName, String attributeClass) {
        super();
        this.m_attributeOid = attributeOid;
        this.m_attributeName = attributeName;
        this.m_attributeClass = attributeClass;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Id
    @Column(nullable=false)
    @XmlTransient
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId", allocationSize = 1)
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }

    /**
     * Sets the id.
     *
     * @param id the id
     */
    public void setId(Integer id) {
        m_id = id;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Column(name="attribName", unique=true, nullable=false)
    public String getName() {
        return m_attributeName;
    }

    /**
     * Sets the name.
     *
     * @param attributeName the name
     */
    public void setName(String attributeName) {
        this.m_attributeName = attributeName;
    }

    /**
     * Gets the OID.
     *
     * @return the OID
     */
    @Column(name="attribOid", unique=true, nullable=false)
    public String getOid() {
        return m_attributeOid;
    }

    /**
     * Sets the OID.
     *
     * @param attributeOid the OID
     */
    public void setOid(String attributeOid) {
        this.m_attributeOid = attributeOid;
    }

    /**
     * Gets the SNMP object id.
     *
     * @return the SNMP object id
     */
    @Transient
    public SnmpObjId getSnmpObjId() {
        return SnmpObjId.get(m_attributeOid);
    }

    /**
     * Gets the attribute class.
     *
     * @return the attribute class
     */
    @Column(name="attribClass")
    public String getAttributeClass() {
        return m_attributeClass;
    }

    /**
     * Sets the attribute class.
     *
     * @param attributeClass the attribute class
     */
    public void setAttributeClass(String attributeClass) {
        this.m_attributeClass = attributeClass;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this.getClass().getSimpleName(), ToStringStyle.SHORT_PREFIX_STYLE)
        .append("oid", m_attributeOid)
        .append("name", m_attributeName)
        .append("class", m_attributeClass)
        .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof HwEntityAttributeType) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(HwEntityAttributeType o) {
        return getName().compareTo(o.getName());
    }

}
