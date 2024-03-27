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
package org.opennms.netmgt.config.notificationCommands;


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

@XmlRootElement(name = "argument")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notificationCommands.xsd")
public class Argument implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "streamed", required = true)
    private Boolean m_streamed;

    @XmlElement(name = "substitution")
    private String m_substitution;

    @XmlElement(name = "switch")
    private String m_switch;

    public Argument() {
    }

    public Boolean getStreamed() {
        return m_streamed;
    }

    public void setStreamed(final Boolean streamed) {
        m_streamed = ConfigUtils.assertNotNull(streamed, "streamed");
    }

    public Optional<String> getSubstitution() {
        return Optional.ofNullable(m_substitution);
    }

    public void setSubstitution(final String substitution) {
        m_substitution = ConfigUtils.normalizeString(substitution);
    }

    public Optional<String> getSwitch() {
        return Optional.ofNullable(this.m_switch);
    }

    public void setSwitch(final String s) {
        m_switch = ConfigUtils.normalizeString(s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_streamed, m_substitution,  m_switch);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Argument) {
            final Argument that = (Argument)obj;
            return Objects.equals(this.m_streamed, that.m_streamed)
                    && Objects.equals(this.m_substitution, that.m_substitution)
                    && Objects.equals(this.m_switch, that.m_switch);
        }
        return false;
    }

}
