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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name="filter")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Filter implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="eventparm", required=true)
    private String m_eventparm;

    @XmlAttribute(name="pattern", required=true)
    private String m_pattern;

    @XmlAttribute(name="replacement", required=true)
    private String m_replacement;

    public String getEventparm() {
        return m_eventparm;
    }

    public void setEventparm(final String eventparm) {
        m_eventparm = ConfigUtils.assertNotNull(eventparm, "eventparm").intern();
    }

    public String getPattern() {
        return m_pattern;
    }

    public void setPattern(final String pattern) {
        m_pattern = ConfigUtils.assertNotNull(pattern, "pattern").intern();
    }

    public String getReplacement() {
        return m_replacement;
    }

    public void setReplacement(final String replacement) {
        m_replacement = ConfigUtils.assertNotNull(replacement, "replacement").intern();
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_eventparm, m_pattern, m_replacement);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Filter) {
            final Filter that = (Filter) obj;
            return Objects.equals(this.m_eventparm, that.m_eventparm) &&
                    Objects.equals(this.m_pattern, that.m_pattern) &&
                    Objects.equals(this.m_replacement, that.m_replacement);
        }
        return false;
    }

}
