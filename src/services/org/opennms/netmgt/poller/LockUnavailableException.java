//
// Copyright (C) 1999-2002 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.poller;

/** 
 * <P>This exception is generated when a method needs to lock an
 * element, but cannot obtain a lock within an acceptable time.
 * </P>
 *
 * @author <A HREF="mailto:justis@opennms.org">Justis Peters</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
public class LockUnavailableException 
	extends IllegalStateException
{
	/**
	 * Constructs a new exception instance.
	 */
	public LockUnavailableException()
	{
		super();
	}
	
	/**
	 * Constructs a new exception instance with the specific message
	 *
	 * @param msg	The exception message.
	 *
	 */
	public LockUnavailableException(String msg)
	{
		super(msg);
	}
}
