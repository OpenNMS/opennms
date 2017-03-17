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
        if (name == null) {
            throw new IllegalArgumentException("'name' is a required attribute!");
        }
        m_name = name;
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
        m_commands.clear();       
        m_commands.addAll(commands);
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
