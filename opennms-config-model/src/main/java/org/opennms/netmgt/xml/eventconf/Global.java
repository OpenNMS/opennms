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
package org.opennms.netmgt.xml.eventconf;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Global settings for this configuration
 */
@XmlRootElement(name="global")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Global implements Serializable {
    private static final long serialVersionUID = 2L;
    /**
     * Security settings for this configuration
     */
    @XmlElement(name="security", required=true)
    private Security m_security;

    public Security getSecurity() {
        return m_security;
    }

    public void setSecurity(final Security security) {
        m_security = ConfigUtils.assertNotNull(security, "security");
    }

    public boolean isSecureTag(final String tag) {
        return m_security == null ? false : m_security.isSecureTag(tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_security);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Global) {
            final Global that = (Global) obj;
            return Objects.equals(this.m_security, that.m_security);
        }
        return false;
    }

}
