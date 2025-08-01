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
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "wql")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("wsman-asset-adapter-configuration.xsd")
public class WqlObj implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "query", required = true)
    private String m_wql;

    @XmlAttribute(name = "resourceUri", required = true)
    private String m_resourceuri;
    /**
     * a human readable name for the object 
     *  NOTE: This value is used as the RRD file name and
     *  data source name. RRD only supports data source names up to 19 chars
     *  in length. If the data collector encounters an alias which
     *  exceeds 19 characters it will be truncated.
     */
    @XmlAttribute(name = "alias", required = true)
    private String m_alias;

    public WqlObj() {
    }

    public String getWql() {
        return m_wql;
    }

    public void setWql(final String wql) {
        m_wql = ConfigUtils.assertNotEmpty(wql, "wql");
    }

    public String getResourceUri() {
        return m_resourceuri;
    }

    public void setResourceUri(final String resourceUri) {
        m_resourceuri = ConfigUtils.assertNotEmpty(resourceUri, "resourceUri");
    }

    public String getAlias() {
        return m_alias;
    }

    public void setAlias(final String alias) {
        m_alias = ConfigUtils.assertNotEmpty(alias, "alias");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_resourceuri, m_wql, m_alias);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof WqlObj) {
            final WqlObj that = (WqlObj)obj;
            return Objects.equals(this.m_wql, that.m_wql)
                    && Objects.equals(this.m_resourceuri, that.m_resourceuri)
                    && Objects.equals(this.m_alias, that.m_alias);
        }
        return false;
    }

}
