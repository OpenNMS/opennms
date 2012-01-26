/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.notificationCommands.Command;
import org.opennms.netmgt.config.notificationCommands.NotificationCommands;

/**
 * <p>Abstract NotificationCommandManager class.</p>
 *
 * @author David Hustace <david@opennms.org>
 * @version $Id: $
 */
public abstract class NotificationCommandManager {
    /**
     * List of all configuration notification commands.  parseXml must be called to populate this.
     */
    private Map<String, Command> m_commands;

    /**
     * Populate the internal list of notification commands from an XML file.
     *
     * @param reader contains the XML file to be parsed
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    protected void parseXML(InputStream reader) throws MarshalException, ValidationException {
        NotificationCommands config = CastorUtils.unmarshal(NotificationCommands.class, reader);

        Map<String, Command> commands = new HashMap<String, Command>();
        for (Command curCommand : getCommandsFromConfig(config)) {
            if (curCommand != null && curCommand.getName() != null) {
                commands.put(curCommand.getName(), curCommand);
            } else {
                log().warn("invalid notification command: " + curCommand);
            }
        }

        m_commands = commands;
    }

    private static List<Command> getCommandsFromConfig(NotificationCommands config) {
        if (config == null) {
            log().warn("no notification commands found");
            return Collections.emptyList();
        }
        return config.getCommandCollection();
    }
    
    /**
     * <p>update</p>
     *
     * @throws java.lang.Exception if any.
     */
    public abstract void update() throws Exception;

    private static ThreadCategory log() {
        return ThreadCategory.getInstance(NotificationCommandManager.class);
    }

    /**
     * Gets a notification command for a particular command name.
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.notificationCommands.Command} object.
     */
    public Command getCommand(String name) {
        return m_commands.get(name);
    }

    /**
     * Gets all configured notification commands.
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Command> getCommands() {
        return Collections.unmodifiableMap(m_commands);
    }
}
