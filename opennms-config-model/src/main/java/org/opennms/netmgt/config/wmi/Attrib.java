/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.wmi;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.config.utils.ConfigUtils;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="wmiObject" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="type" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;pattern value="([Cc](ounter|OUNTER)|[Gg](auge|AUGE)|[Ss](tring|TRING))"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="maxval" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="minval" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "")
@XmlRootElement(name = "attrib")
@ValidateUsing("wmi-datacollection.xsd")
public class Attrib implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "name", required = true)
    protected String m_name;

    @XmlAttribute(name = "alias", required = true)
    protected String m_alias;

    @XmlAttribute(name = "wmiObject", required = true)
    protected String m_wmiObject;

    @XmlJavaTypeAdapter(WmiTypeAdapter.class)
    @XmlAttribute(name = "type", required = true)
    protected AttributeType m_type;

    @XmlAttribute(name = "maxval")
    protected String m_maxval;

    @XmlAttribute(name = "minval")
    protected String m_minval;

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getAlias() {
        return m_alias;
    }

    public void setAlias(final String alias) {
        m_alias = ConfigUtils.assertNotEmpty(alias, "alias");
    }

    public String getWmiObject() {
        return m_wmiObject;
    }

    public void setWmiObject(final String wmiObject) {
        m_wmiObject = ConfigUtils.assertNotEmpty(wmiObject, "wmiObject");
    }

    public AttributeType getType() {
        return m_type;
    }

    public void setType(final AttributeType value) {
        m_type = ConfigUtils.assertNotNull(value, "type");
    }

    public Optional<String> getMaxval() {
        return Optional.ofNullable(m_maxval);
    }

    public void setMaxval(final String maxval) {
        m_maxval = ConfigUtils.normalizeString(maxval);
    }

    public Optional<String> getMinval() {
        return Optional.ofNullable(m_minval);
    }

    public void setMinval(final String minval) {
        m_minval = ConfigUtils.normalizeString(minval);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Attrib)) {
            return false;
        }
        final Attrib that = (Attrib) obj;
        return Objects.equals(this.m_name, that.m_name) &&
                Objects.equals(this.m_alias, that.m_alias) &&
                Objects.equals(this.m_wmiObject, that.m_wmiObject) &&
                Objects.equals(this.m_type, that.m_type) &&
                Objects.equals(this.m_maxval, that.m_maxval) &&
                Objects.equals(this.m_minval, that.m_minval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_alias, m_wmiObject, m_type, m_maxval, m_minval);
    }

}
