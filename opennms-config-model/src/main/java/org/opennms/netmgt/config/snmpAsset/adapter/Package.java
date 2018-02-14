/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "package")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("snmp-asset-adapter-configuration.xsd")
public class Package implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * System object identifier (sysoid) which uniquely
     *  identifies the system.
     */
    @XmlElement(name = "sysoid", required = true)
    private String m_sysoid;

    /**
     * Sysoid mask which can be used to match multiple
     *  systems if their sysoid begins with the mask
     */
    @XmlElement(name = "sysoidMask", required = true)
    private String m_sysoidMask;

    @XmlElement(name = "assetField", required = true)
    private List<AssetField> m_assetFields = new ArrayList<>();

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getSysoid() {
        return m_sysoid;
    }

    public void setSysoid(final String sysoid) {
        m_sysoid = ConfigUtils.assertNotEmpty(sysoid, "sysoid");
    }

    public String getSysoidMask() {
        return m_sysoidMask;
    }

    public void setSysoidMask(final String sysoidMask) {
        m_sysoidMask = ConfigUtils.assertNotEmpty(sysoidMask, "sysoidMask");
    }

    public List<AssetField> getAssetFields() {
        return m_assetFields;
    }

    public void setAssetFields(final List<AssetField> assetFields) {
        ConfigUtils.assertMinimumSize(assetFields, 1, "assetField");
        if (assetFields == m_assetFields) return;
        m_assetFields.clear();
        if (assetFields != null) m_assetFields.addAll(assetFields);
    }

    public void addAssetField(final AssetField assetField) {
        m_assetFields.add(assetField);
    }

    public boolean removeAssetField(final AssetField assetField) {
        return m_assetFields.remove(assetField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_sysoid,
                            m_sysoidMask,
                            m_assetFields);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Package) {
            final Package that = (Package)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_sysoid, that.m_sysoid)
                    && Objects.equals(this.m_sysoidMask, that.m_sysoidMask)
                    && Objects.equals(this.m_assetFields, that.m_assetFields);
        }
        return false;
    }
}
