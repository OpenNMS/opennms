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
package org.opennms.netmgt.config.service;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "argument")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("service-configuration.xsd")
public class Argument implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * internal content storage
     */
    @XmlValue
    private String m_value;

    @XmlAttribute(name = "type", required = true)
    private String m_type;

    public Argument() {
    }

    public Argument(final String type, final String value) {
        setType(type);
        setValue(value);
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(m_value);
    }

    public void setValue(final String value) {
        m_value = ConfigUtils.normalizeString(value);
    }

    public String getType() {
        return m_type;
    }

    public void setType(final String type) {
        m_type = ConfigUtils.assertNotEmpty(type, "type");
    }

    public int hashCode() {
        return Objects.hash(m_value, m_type);
    }

    @Override()
    public boolean equals(final java.lang.Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Argument) {
            final Argument that = (Argument) obj;
            return Objects.equals(this.m_value, that.m_value) &&
                    Objects.equals(this.m_type, that.m_type);
        }
        return false;
    }
}
