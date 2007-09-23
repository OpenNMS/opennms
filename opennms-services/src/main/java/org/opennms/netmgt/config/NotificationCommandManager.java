/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Jul 31: Format. - dj@opennms.org
 * 2007 Jul 31: Use CastorUtils.unmarshal, Java 5 for loops, and don't set
 *              m_commands until we've finished populating the hash. - dj@opennms.org
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact: 
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.notificationCommands.Command;
import org.opennms.netmgt.config.notificationCommands.NotificationCommands;

/**
 * @author david hustace <david@opennms.org>
 */

public abstract class NotificationCommandManager {

    /**
     * 
     */
    private Map<String, Command> m_commands;
    /**
     */
    protected InputStream configIn;

    /**
     * @param reader
     * @throws MarshalException
     * @throws ValidationException
     */
    protected void parseXML(Reader reader) throws MarshalException, ValidationException {
        NotificationCommands config = CastorUtils.unmarshal(NotificationCommands.class, reader);

        Map<String, Command> commands = new HashMap<String, Command>();
        for (Command curCommand : getCommandsFromConfig(config)) {
            commands.put(curCommand.getName(), curCommand);
        }

        m_commands = commands;
    }

    @SuppressWarnings("unchecked")
    private List<Command> getCommandsFromConfig(NotificationCommands config) {
        return config.getCommandCollection();
    }

    /**
     * 
     */
    public Command getCommand(String name) {
        return m_commands.get(name);
    }

    /**
     * 
     */
    public Map<String, Command> getCommands() {
        return m_commands;
    }
}
