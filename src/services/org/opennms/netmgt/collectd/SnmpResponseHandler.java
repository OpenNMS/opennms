//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.collectd;

import org.opennms.protocols.snmp.SnmpHandler;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * The SNMP handler used to receive responses from individual
 * sessions. When a response is received that matches a system
 * object identifier request the session is notified.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
final class SnmpResponseHandler
	implements SnmpHandler
{
	/**
	 * The returned object identifier
	 */
	private SnmpVarBind 	m_result = null;

	/** 
	 * The method that handles a returned packet from the remote agent.
	 *
	 * @param sess		The snmp session that received the result.
	 * @param command	The snmp command.
	 * @param pkt		The snmp packet that was received.
	 */
	public void snmpReceivedPdu(SnmpSession sess, int command, SnmpPduPacket pkt)
	{
		if(pkt.getCommand() == SnmpPduPacket.RESPONSE && pkt.getLength() == 1)
		{
			if (((SnmpPduRequest)pkt).getErrorStatus() == SnmpPduPacket.ErrNoError)
				m_result = pkt.getVarBindAt(0);
				
			synchronized(this)
			{
				notifyAll();
			}
		}
	}

	/** 
	 * This method is invoked when an internal error occurs
	 * on the SNMP session.
	 *
	 * @param sess		The snmp session that received the result.
	 * @param err	The snmp error.
	 * @param obj		The snmp syntax object.
	 */
	public void snmpInternalError(SnmpSession sess, int err, SnmpSyntax obj)
	{
		synchronized(this)
		{
			notifyAll();
		}
	}

	/** 
	 * This method is invoked when the session fails to receive
	 * a response to a particular packet.
	 *
	 * @param sess		The snmp session that received the result.
	 * @param pkt		The snmp packet that was received.
	 */
	public void snmpTimeoutError(SnmpSession sess, SnmpSyntax pkt)
	{
		synchronized(this)
		{
			notifyAll();
		}
	}

	/** 
	 * Returns the recovered snmp system object identifier, if
	 * any. If one was not returned then a null value is returned
	 * to the caller.
	 *
	 */
	public SnmpVarBind getResult()
	{
		return m_result;
	}
}
