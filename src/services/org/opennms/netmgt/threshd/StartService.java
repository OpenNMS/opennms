//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.threshd;

import org.opennms.core.fiber.Fiber;


/**
 * This class is used to launch/start the thresholding daemon service
 * in a Java Virtual Machine. It contains the method 
 * <code>main</code> which is a well defined entry point
 * for the virtual machine.
 *
 */
public final class StartService
{
	/**
	 * The main method used as a starting point for the Threshd
	 * service. This method has the required signature necessary
	 * for the virtual machine to invoke on startup.
	 *
	 * @param args	The arguments to the main method (not used).
	 *
	 */
	public static void main(String[] args)
	{
		Threshd svc = Threshd.getInstance();
		int status = svc.getStatus();
		if (status == Fiber.START_PENDING || status == Fiber.STOPPED)
		{
			svc.start();
		}
	}
}
