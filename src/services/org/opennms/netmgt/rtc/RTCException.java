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

package org.opennms.netmgt.rtc;

/**
 * <P>The RTCException class is thrown by the RTC </P>
 *
 * @author 	<A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy</A>
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 */
public final class RTCException extends Exception
{
	/**
	 * Default class constructor. Constructs a new exception with 
	 * a default message.
	 */
	public RTCException()
	{
		super("RTCException");
	}
	
	/**
	 * Constructs a new exception with the passed string as 
	 * the message encapsulated in the exception.
	 *
	 * @param s	The exception's message.
	 *
	 */
	public RTCException(String s)
	{
		super(s);
	}
}

