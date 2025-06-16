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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Security settings for this configuration
 */
@XmlRootElement(name="security")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Security implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Event element whose value cannot be overridden by a
     *  value in an incoming event
     */
    @XmlElement(name="doNotOverride", required=true)
    private List<String> m_doNotOverride = new ArrayList<>();

    public List<String> getDoNotOverrides() {
        return m_doNotOverride;
    }

    public void setDoNotOverride(final List<String> doNotOverride) {
        ConfigUtils.assertMinimumSize(doNotOverride, 1, "doNotOverride");
        if (m_doNotOverride == doNotOverride) return;
        m_doNotOverride.clear();
        if (doNotOverride != null) m_doNotOverride.addAll(doNotOverride);
    }

    public void addDoNotOverride(final String doNotOverride) {
        m_doNotOverride.add(doNotOverride);
    }

    public boolean removeDoNotOverride(final String doNotOverride) {
        return m_doNotOverride.remove(doNotOverride);
    }

    public boolean isSecureTag(final String tag) {
        return m_doNotOverride == null ? false : m_doNotOverride.contains(tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_doNotOverride);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Security) {
            final Security that = (Security) obj;
            return Objects.equals(this.m_doNotOverride, that.m_doNotOverride);
        }
        return false;
    }

}
