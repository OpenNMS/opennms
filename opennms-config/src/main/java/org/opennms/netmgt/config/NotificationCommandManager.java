/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.notificationCommands.Command;
import org.opennms.netmgt.config.notificationCommands.NotificationCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract NotificationCommandManager class.</p>
 *
 * @author David Hustace <david@opennms.org>
 * @version $Id: $
 */
public abstract class NotificationCommandManager {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationCommandManager.class);
    /**
     * List of all configuration notification commands.  parseXml must be called to populate this.
     */
    private Map<String, Command> m_commands;

    /**
     * Populate the internal list of notification commands from an XML file.
     *
     * @param reader contains the XML file to be parsed
     * @throws IOException 
     */
    protected void parseXML(InputStream stream) throws IOException {
        final NotificationCommands config;
        try (final Reader reader = new InputStreamReader(stream)) {
            config = JaxbUtils.unmarshal(NotificationCommands.class, reader);
        }

        Map<String, Command> commands = new HashMap<String, Command>();
        for (Command curCommand : getCommandsFromConfig(config)) {
            if (curCommand != null && curCommand.getName() != null) {
                commands.put(curCommand.getName(), curCommand);
            } else {
                LOG.warn("invalid notification command: {}", curCommand);
            }
        }

        m_commands = commands;
    }

    private static List<Command> getCommandsFromConfig(NotificationCommands config) {
        if (config == null) {
            LOG.warn("no notification commands found");
            return Collections.emptyList();
        }
        return config.getCommands();
    }
    
    /**
     * <p>update</p>
     *
     * @throws java.lang.Exception if any.
     */
    public abstract void update() throws Exception;

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
