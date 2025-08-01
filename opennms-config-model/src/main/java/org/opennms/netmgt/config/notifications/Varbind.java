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
package org.opennms.netmgt.config.notifications;


import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The varbind element
 */
@XmlRootElement(name = "varbind")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notifications.xsd")
public class Varbind implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The varbind element number
     */
    @XmlElement(name = "vbname", required = true)
    private String m_vbname;

    /**
     * The varbind element value
     */
    @XmlElement(name = "vbvalue", required = true)
    private String m_vbvalue;

    public Varbind() {
    }

    public String getVbname() {
        return m_vbname;
    }

    public void setVbname(final String vbname) {
        m_vbname = ConfigUtils.assertNotEmpty(vbname, "vbname");
    }

    public String getVbvalue() {
        return m_vbvalue;
    }

    public void setVbvalue(final String vbvalue) {
        m_vbvalue = ConfigUtils.assertNotEmpty(vbvalue, "vbvalue");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_vbname, m_vbvalue);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Varbind) {
            final Varbind that = (Varbind)obj;
            return Objects.equals(this.m_vbname, that.m_vbname)
                    && Objects.equals(this.m_vbvalue, that.m_vbvalue);
        }
        return false;
    }

}
