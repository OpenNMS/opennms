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
package org.opennms.netmgt.config.snmpAsset.adapter;


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

    @XmlElementWrapper(name = "mibObjs", required = true)
    @XmlElement(name = "mibObj", required = true)
    private List<MibObj> m_mibObjs = new ArrayList<>();

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

    public List<MibObj> getMibObjs() {
        return m_mibObjs;
    }

    public void setMibObjs(final List<MibObj> mibObjs) {
        ConfigUtils.assertMinimumSize(mibObjs, 1, "mibObj");
        if (mibObjs == m_mibObjs) return;
        m_mibObjs.clear();
        if (mibObjs != null) m_mibObjs.addAll(mibObjs);
    }

    public void addMibObj(final MibObj mibObj) {
        m_mibObjs.add(mibObj);
    }

    public boolean removeMibObj(final MibObj mibObj) {
        return m_mibObjs.remove(mibObj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_formatString, 
                            m_mibObjs);
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
                    && Objects.equals(this.m_mibObjs, that.m_mibObjs);
        }
        return false;
    }

}
