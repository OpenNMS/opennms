//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
// Copyright (C) 2001 Oculan Corp. All rights reserved.
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
//
//
package org.opennms.netmgt.threshd.jmx;

public class Threshd
	implements ThreshdMBean
{
	public void init()
	{
		org.opennms.netmgt.threshd.Threshd.getInstance().init();
	}

	public void start()
	{
		org.opennms.netmgt.threshd.Threshd.getInstance().start();
	}

	public void stop()
	{
		org.opennms.netmgt.threshd.Threshd.getInstance().stop();
	}

	public int getStatus()
	{
		return org.opennms.netmgt.threshd.Threshd.getInstance().getStatus();
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
