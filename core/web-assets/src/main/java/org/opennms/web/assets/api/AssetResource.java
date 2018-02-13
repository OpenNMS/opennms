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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.assets.api;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="asset-resource")
@XmlAccessorType(XmlAccessType.NONE)
public class AssetResource {
    @XmlAttribute(name="asset")
    private final String m_asset;
    @XmlElement(name="type")
    private final String m_type;
    @XmlElement(name="path")
    private final String m_path;

    public AssetResource(final String asset, final String type, final String path) {
        m_asset = asset;
        m_type = type;
        m_path = path;
    }

    public String getAsset() {
        return m_asset;
    }
    public String getType() {
        return m_type;
    }
    public String getPath() {
        return m_path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_asset, m_path, m_type);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AssetResource that = (AssetResource) obj;
        return Objects.equals(this.m_asset, that.m_asset) &&
                Objects.equals(this.m_path, that.m_path) &&
                Objects.equals(this.m_type, that.m_type);
    }

    @Override
    public String toString() {
        return m_asset + ":" + m_type + "=" + m_path;
    }
}