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
package org.opennms.web.utils.assets;

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