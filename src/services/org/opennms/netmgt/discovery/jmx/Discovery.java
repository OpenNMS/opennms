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
//
// Tab Size = 8
//
package org.opennms.netmgt.discovery.jmx;

public class Discovery
	implements DiscoveryMBean
{
	public void init()
	{
		org.opennms.netmgt.discovery.Discovery.getInstance().init();
	}

	public void start()
	{
		org.opennms.netmgt.discovery.Discovery.getInstance().start();
	}

	public void stop()
	{
		org.opennms.netmgt.discovery.Discovery.getInstance().stop();
	}

	public int getStatus()
	{
		return org.opennms.netmgt.discovery.Discovery.getInstance().getStatus();
	}

	public String status()
	{
		return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
	}

	public String getStatusText()
	{
		return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
	}
}

