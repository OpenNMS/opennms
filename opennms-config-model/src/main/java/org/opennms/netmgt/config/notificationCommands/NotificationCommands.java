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
 * Top-level element for the notificationCommands.xml
 *  configuration file.
 */
@XmlRootElement(name = "notification-commands")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notificationCommands.xsd")
public class NotificationCommands implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "header", required = true)
    private Header m_header;

    @XmlElement(name = "command", required = true)
    private List<Command> m_commands = new ArrayList<>();

    public NotificationCommands() { }

    public Header getHeader() {
        return m_header;
    }

    public void setHeader(final Header header) {
        m_header = ConfigUtils.assertNotNull(header, "header");
    }

    public List<Command> getCommands() {
        return m_commands;
    }

    public void setCommands(final List<Command> commands) {
        if (commands == m_commands) return;
        m_commands.clear();
        if (commands != null) m_commands.addAll(commands);
    }

    public void addCommand(final Command command) {
        m_commands.add(command);
    }

    public boolean removeCommand(final Command command) {
        return m_commands.remove(command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_header, m_commands);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof NotificationCommands) {
            final NotificationCommands that = (NotificationCommands)obj;
            return Objects.equals(this.m_header, that.m_header)
                    && Objects.equals(this.m_commands, that.m_commands);
        }
        return false;
    }

}
