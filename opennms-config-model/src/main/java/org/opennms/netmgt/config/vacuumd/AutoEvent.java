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
package org.opennms.netmgt.config.vacuumd;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * (THIS IS BEING DEPRECATED) actions modify the database based on results of
 * a trigger
 */
@XmlRootElement(name = "auto-event")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("vacuumd-configuration.xsd")
public class AutoEvent implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "fields")
    private String m_fields;

    /**
     * Must be a UEI defined in event-conf.xml
     */
    @XmlElement(name = "uei", required = true)
    private Uei m_uei;

    public AutoEvent() {
    }

    public AutoEvent(final String name, final String fields, final Uei uei) {
        setName(name);
        setFields(fields);
        setUei(uei);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Optional<String> getFields() {
        return Optional.ofNullable(m_fields);
    }

    public void setFields(final String fields) {
        m_fields = ConfigUtils.normalizeString(fields);
    }

    public Uei getUei() {
        return m_uei;
    }

    public void setUei(final Uei uei) {
        m_uei = ConfigUtils.assertNotNull(uei, "uei");
    }

    public int hashCode() {
        return Objects.hash(m_name, m_fields, m_uei);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof AutoEvent) {
            final AutoEvent that = (AutoEvent) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_fields, that.m_fields) &&
                    Objects.equals(this.m_uei, that.m_uei);
        }
        return false;
    }
}
