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
package org.opennms.netmgt.config.destinationPaths;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "target")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("destinationPaths.xsd")
public class Target implements Serializable {
    public static String DEFAULT_INTERVAL = "0s";
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "interval")
    private String m_interval;

    @XmlElement(name = "name", required = true)
    private String m_name;

    @XmlElement(name = "autoNotify")
    private String m_autoNotify;

    @XmlElement(name = "command", required = true)
    private List<String> m_commands = new ArrayList<>();

    public Target() {
    }

    public Target(final String name, final String... commands) {
        m_name = name;
        for (final String c : commands) {
            m_commands.add(c);
        }
    }

    public Optional<String> getInterval() {
        return Optional.ofNullable(m_interval);
    }

    public void setInterval(final String interval) {
        m_interval = interval;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Optional<String> getAutoNotify() {
        return Optional.ofNullable(m_autoNotify);
    }

    public void setAutoNotify(final String autoNotify) {
        m_autoNotify = autoNotify;
    }

    public List<String> getCommands() {
        return m_commands;
    }

    public void setCommands(final List<String> commands) {
        if (commands == m_commands) return;
        m_commands.clear();
        if (commands != null) m_commands.addAll(commands);
    }

    public void addCommand(final String command) {
        m_commands.add(command);
    }

    public void clearCommands() {
        m_commands.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_interval, 
                            m_name, 
                            m_autoNotify, 
                            m_commands);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Target) {
            final Target temp = (Target)obj;
            return Objects.equals(temp.m_interval, m_interval)
                    && Objects.equals(temp.m_name, m_name)
                    && Objects.equals(temp.m_autoNotify, m_autoNotify)
                    && Objects.equals(temp.m_commands, m_commands);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Target [interval=" + m_interval + ", name=" + m_name
                + ", autoNotify=" + m_autoNotify + ", commands="
                + m_commands + "]";
    }

}
