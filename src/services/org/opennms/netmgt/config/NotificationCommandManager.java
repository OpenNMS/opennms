//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

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
    private Map m_commands;
    /**
     */
    protected InputStream configIn;

    /**
     * @param reader
     * @throws MarshalException
     * @throws ValidationException
     */
    protected void parseXML(Reader reader) throws MarshalException, ValidationException {
        Collection commands = ((NotificationCommands) Unmarshaller.unmarshal(NotificationCommands.class, reader)).getCommandCollection();
        m_commands = new HashMap();
    
        Iterator i = commands.iterator();
        while (i.hasNext()) {
            Command curCommand = (Command) i.next();
            m_commands.put(curCommand.getName(), curCommand);
        }
    }

    /**
     * 
     */
    public Command getCommand(String name) {
        return (Command) m_commands.get(name);
    }

    /**
     * 
     */
    public Map getCommands() {
        return m_commands;
    }

}
