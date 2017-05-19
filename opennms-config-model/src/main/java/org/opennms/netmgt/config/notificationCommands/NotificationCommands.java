/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
