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
