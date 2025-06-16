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
package org.opennms.netmgt.config.wsmanAsset.adapter;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "assetField")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("snmp-asset-adapter-configuration.xsd")
public class AssetField implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "formatString", required = true)
    private String m_formatString;

    @XmlElementWrapper(name = "wqlQueries", required = true)
    @XmlElement(name = "wql", required = true)
    private List<WqlObj> m_wqlQueries = new ArrayList<>();

    public AssetField() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getFormatString() {
        return m_formatString;
    }

    public void setFormatString(final String formatString) {
        m_formatString = ConfigUtils.assertNotEmpty(formatString, "formatString");
    }

    public List<WqlObj> getWqlObjs() {
        return m_wqlQueries;
    }

    public void setWqlObjs(final List<WqlObj> wqlQueries) {
        ConfigUtils.assertMinimumSize(wqlQueries, 1, "wql");
        if (wqlQueries == m_wqlQueries) return;
        m_wqlQueries.clear();
        if (wqlQueries != null) m_wqlQueries.addAll(wqlQueries);
    }

    public void addWqlObj(final WqlObj wql) {
        m_wqlQueries.add(wql);
    }

    public boolean removeWqlObj(final WqlObj wql) {
        return m_wqlQueries.remove(wql);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_formatString, 
                            m_wqlQueries);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof AssetField) {
            final AssetField that = (AssetField)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_formatString, that.m_formatString)
                    && Objects.equals(this.m_wqlQueries, that.m_wqlQueries);
        }
        return false;
    }

}
