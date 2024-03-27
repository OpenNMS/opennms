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
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "mibObj")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("snmp-asset-adapter-configuration.xsd")
public class MibObj implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * object identifier
     */
    @XmlAttribute(name = "oid", required = true)
    private String m_oid;

    /**
     * a human readable name for the object (such as
     *  "ifOctetsIn"). NOTE: This value is used as the RRD file name and
     *  data source name. RRD only supports data source names up to 19 chars
     *  in length. If the SNMP data collector encounters an alias which
     *  exceeds 19 characters it will be truncated.
     */
    @XmlAttribute(name = "alias", required = true)
    private String m_alias;

    public MibObj() {
    }

    public String getOid() {
        return m_oid;
    }

    public void setOid(final String oid) {
        m_oid = ConfigUtils.assertNotEmpty(oid, "oid");
    }

    public String getAlias() {
        return m_alias;
    }

    public void setAlias(final String alias) {
        m_alias = ConfigUtils.assertNotEmpty(alias, "alias");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_oid, m_alias);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof MibObj) {
            final MibObj that = (MibObj)obj;
            return Objects.equals(this.m_oid, that.m_oid)
                    && Objects.equals(this.m_alias, that.m_alias);
        }
        return false;
    }

}
