//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
// Copyright (C) 2001 Oculan Corp.  All rights reserved.
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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
// Tab Size = 8
//
package org.opennms.netmgt.eventd;

/**
 *
 * @author 	<A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj</A>
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 */
public class EventIpcManagerFactory
{
	/**
	 * The singleton instance of this factory
	 */
	private static EventIpcManagerFactory	m_singleton=null;

	/**
	 * This member is set to true if init() has been called
	 */
	private static boolean			m_loaded=false;

	/**
	 * The default EventIpcManager
	 */
	private EventIpcManagerDefaultImpl	m_defIpcManager; 

	/**
	 * Private constructor
	 */
	private EventIpcManagerFactory()
	{
		m_defIpcManager = new EventIpcManagerDefaultImpl();
	}

	/**
	 * Create the singleton instance of this factory
	 */
	public static synchronized void init()
	{
		if (m_loaded)
		{
			// init already called - return
			return;
		}

		m_singleton = new EventIpcManagerFactory();

		m_loaded = true;
	}

	/**
	 * <p>Return the singleton instance of this factory<p>
	 *
	 * @return The current factory instance.
	 *
	 * @throws java.lang.IllegalStateException Thrown if the factory
	 * 	has not yet been initialized.
	 */
	public static synchronized EventIpcManagerFactory getInstance()
	{
		if(!m_loaded)
			throw new IllegalStateException("The factory has not been initialized");

		return m_singleton;
	}

	/**
	 * Returns an implementation of the default EventIpcManager class
	 */
	public EventIpcManagerDefaultImpl getManager()
	{
		return m_defIpcManager;
	}


	//
	// Will eventually have methods to get instances of managers to
	// communicate across VMs
	//

}

