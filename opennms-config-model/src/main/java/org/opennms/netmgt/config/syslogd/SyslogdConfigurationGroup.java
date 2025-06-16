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
package org.opennms.netmgt.config.syslogd;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "syslogd-configuration-group")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("syslog.xsd")
public class SyslogdConfigurationGroup implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElementWrapper(name = "ueiList", required=false)
    @XmlElement(name = "ueiMatch")
    private List<UeiMatch> m_ueiMatches = new ArrayList<>();

    @XmlElementWrapper(name = "hideMessage", required=false)
    @XmlElement(name = "hideMatch")
    private List<HideMatch> m_hideMessages = new ArrayList<>();

    public SyslogdConfigurationGroup() {
    }

    public List<UeiMatch> getUeiMatches() {
        return m_ueiMatches;
    }

    public void setUeiMatches(final List<UeiMatch> ueiList) {
        if (ueiList == m_ueiMatches) return;
        m_ueiMatches.clear();
        if (ueiList != null) m_ueiMatches.addAll(ueiList);
    }

    public void addUeiMatch(final UeiMatch ueiMatch) {
        m_ueiMatches.add(ueiMatch);
    }

    public boolean removeUeiMatch(final UeiMatch ueiMatch) {
        return m_ueiMatches.remove(ueiMatch);
    }

    public List<HideMatch> getHideMatches() {
        return m_hideMessages;
    }

    public void setHideMatches(final List<HideMatch> hideMessages) {
        if (hideMessages == m_hideMessages) return;
        m_hideMessages.clear();
        if (hideMessages != null) m_hideMessages.addAll(hideMessages);
    }

    public void addHideMatch(final HideMatch hideMatch) {
        m_hideMessages.add(hideMatch);
    }

    public boolean removeHideMatch(final HideMatch hideMatch) {
        return m_hideMessages.remove(hideMatch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_ueiMatches, m_hideMessages);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof SyslogdConfigurationGroup) {
            final SyslogdConfigurationGroup that = (SyslogdConfigurationGroup)obj;
            return Objects.equals(this.m_ueiMatches, that.m_ueiMatches)
                    && Objects.equals(this.m_hideMessages, that.m_hideMessages);
        }
        return false;
    }
}
