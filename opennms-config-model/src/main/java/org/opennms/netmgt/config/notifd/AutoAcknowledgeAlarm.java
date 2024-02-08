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
package org.opennms.netmgt.config.notifd;

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

@XmlRootElement(name = "auto-acknowledge-alarm")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notifd-configuration.xsd")
public class AutoAcknowledgeAlarm implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_RESOLUTION_PREFIX = "RESOLVED: ";

    @XmlAttribute(name = "resolution-prefix")
    private String m_resolutionPrefix;

    @XmlAttribute(name = "notify")
    private Boolean m_notify;

    @XmlElement(name = "uei")
    private List<String> m_ueis = new ArrayList<>();

    public AutoAcknowledgeAlarm() { }

    public String getResolutionPrefix() {
        return m_resolutionPrefix != null ? m_resolutionPrefix : DEFAULT_RESOLUTION_PREFIX;
    }

    public void setResolutionPrefix(final String resolutionPrefix) {
        m_resolutionPrefix = ConfigUtils.normalizeString(resolutionPrefix);
    }

    public Boolean getNotify() {
        return m_notify != null ? m_notify : Boolean.TRUE;
    }

    public void setNotify(final Boolean notify) {
        m_notify = notify;
    }

    public List<String> getUeis() {
        return m_ueis;
    }

    public void setUei(final List<String> ueis) {
        if (ueis == m_ueis) return;
        m_ueis.clear();
        if (ueis != null) m_ueis.addAll(ueis);
    }

    public void addUei(final String uei) {
        m_ueis.add(uei);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof AutoAcknowledgeAlarm) {
            final AutoAcknowledgeAlarm that = (AutoAcknowledgeAlarm)obj;
            return Objects.equals(this.m_resolutionPrefix, that.m_resolutionPrefix)
                    && Objects.equals(this.m_notify, that.m_notify)
                    && Objects.equals(this.m_ueis, that.m_ueis);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_resolutionPrefix, 
                            m_notify, 
                            m_ueis);
    }

}
