//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//
package org.opennms.netmgt.config;

import java.util.*;
import java.io.*;
import java.sql.*;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.netmgt.config.notificationCommands.*;
import org.opennms.netmgt.*;

/**
*/
public class NotificationCommandFactory
{
	/**
	*/
	private static NotificationCommandFactory instance;
	
	/**
	*/
	private static File m_commandConfFile;
	
	/**
	 *
	 */
	private static Map m_commands;
	
	/**
	*/
	protected static InputStream configIn;
	
	/**
	 * Boolean indicating if the init() method has been called
	 */
	private static boolean initialized = false;
	
	/**
	 *
	 */
	private NotificationCommandFactory()
	{
	}
	
	/**
	 *
	 */
	public static synchronized void init()
		throws IOException, MarshalException, ValidationException
	{
		if (!initialized)
		{
			configIn = new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.NOTIF_COMMANDS_CONF_FILE_NAME));
			reload();
			initialized = true;
		}
	}
	
	/**
	*/
	public static synchronized NotificationCommandFactory getInstance()
	{
		if (!initialized)
			return null;
		
		if (instance == null)
		{
			instance = new NotificationCommandFactory();
		}
		
		return instance;
	}
	
	/**
	 *
	 */
	public static synchronized void reload() 
		throws MarshalException, ValidationException
	{
		Collection commands = ((NotificationCommands)Unmarshaller.unmarshal(NotificationCommands.class, new InputStreamReader(configIn))).getCommandCollection();
		m_commands = new HashMap();
		
		Iterator i = commands.iterator();
		while(i.hasNext())
		{
			Command curCommand = (Command)i.next();
			m_commands.put(curCommand.getName(), curCommand);
		}
	}
	
	/**
	 *
	 */
	public Command getCommand(String name)
	{
		return (Command)m_commands.get(name);
	}
	
	/**
	 *
	 */
	public Map getCommands()
	{
		return m_commands;
	}
}
