//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// Tab Size = 8
//
//
package org.opennms.netmgt.utils;

import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Event;

/**
 * This is the interface used to send events into the event subsystem -
 * It is typically used by the poller framework plugins that perform
 * service monitoring to send out aprropriate events. Can also be used by
 * capsd, discovery etc.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
public interface EventProxy 
{
	/**
	 * This method is called to send the event out
	 *
	 * @param event		the event to be sent out
	 *
	 * @exception java.lang.RuntimeException thrown if the send fails for any reason
	 */
	public void send(Event event);

	/**
	 * This method is called to send an event log containing multiple events out
	 *
	 * @param eventlog	the events to be sent out
	 *
	 * @exception java.lang.RuntimeException thrown if the send fails for any reason
	 */
	public void send(Log eventLog);
}
