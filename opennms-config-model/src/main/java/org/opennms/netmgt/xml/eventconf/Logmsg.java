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
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.NullStringAdapter;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The event logmsg with the destination attribute defining
 * if event is for display only, logonly, log and display or
 * neither. A destination attribute of 'donotpersist' indicates
 * that Eventd persist the event to the database. A value of
 * 'discardtraps' instructs the SNMP trap daemon to not create
 * events for incoming traps that match this event. The optional
 * notify attributed can be used to suppress notices on a
 * particular event (by default it is true - i.e. a notice
 * will be sent).
 */
@XmlRootElement(name="logmsg")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Logmsg implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlValue
    @XmlJavaTypeAdapter(NullStringAdapter.class)
    private String m_content;

    @XmlAttribute(name="notify", required=false)
    private Boolean m_notify;

    @XmlAttribute(name="dest", required=false)
    private LogDestType m_dest;

    public String getContent() {
        return m_content;
    }

    public void setContent(final String content) {
        m_content = ConfigUtils.normalizeAndInternString(content);
    }

    public Boolean getNotify() {
        return m_notify == null ? Boolean.TRUE : m_notify; // Default is true according to XSD
    }

    public void setNotify(final boolean notify) {
        m_notify = notify;
    }

    public LogDestType getDest() {
        return m_dest == null ? LogDestType.LOGNDISPLAY : m_dest;
    }

    public void setDest(final LogDestType dest) {
        m_dest = dest;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_content, m_notify, m_dest);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Logmsg) {
            final Logmsg that = (Logmsg) obj;
            return Objects.equals(this.m_content, that.m_content) &&
                    Objects.equals(this.m_notify, that.m_notify) &&
                    Objects.equals(this.m_dest, that.m_dest);
        }
        return false;
    }

}
