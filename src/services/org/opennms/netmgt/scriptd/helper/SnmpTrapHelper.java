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
// Copyright (C) 2003 Tavve Software Company.  All rights reserved.
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

package org.opennms.netmgt.scriptd.helper;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.Base64;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.trapd.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.protocols.ip.IPv4Address;
import org.opennms.protocols.snmp.SnmpBadConversionException;
import org.opennms.protocols.snmp.SnmpCounter32;
import org.opennms.protocols.snmp.SnmpCounter64;
import org.opennms.protocols.snmp.SnmpGauge32;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpNull;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpOpaque;
import org.opennms.protocols.snmp.SnmpParameters;
import org.opennms.protocols.snmp.SnmpPduEncodingException;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpPduTrap;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpTimeTicks;
import org.opennms.protocols.snmp.SnmpTrapHandler;
import org.opennms.protocols.snmp.SnmpTrapSession;
import org.opennms.protocols.snmp.SnmpVarBind;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * This "helper" class provides a convenience interface for generating
 * and forwarding SNMP traps. This class was created in order to make 
 * it easier to write simple scripts to generate traps based on events
 * or to forward traps, using scripting languages that are able to access
 * Java classes (such as BeanShell). 
 * 
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble</a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org</a>
 *
 */
public class SnmpTrapHelper implements SnmpTrapHandler
{

	/**
	 * The sysUpTimeOID, which should be the first varbind in a V2 trap
	 */
	private static final String SNMP_SYSUPTIME_OID =".1.3.6.1.2.1.1.3.0";

	/**
	 * The snmpTrapOID, which should be the second varbind in a V2 trap
	 */
	private static final String SNMP_TRAP_OID =".1.3.6.1.6.3.1.1.4.1.0";

	/**
	 * The snmpTrapAddress, which may occur in a V2 trap
	 */
	private static final String SNMP_TRAP_ADDRESS_OID =".1.3.6.1.6.3.18.1.3.0";

	/**
	 * The snmpTrapCommunity, which may occur in a V2 trap
	 */
	private static final String SNMP_TRAP_COMMUNITY_OID =".1.3.6.1.6.3.18.1.4.0";

	/**
	 * The snmp trap enterprise OID, which if present in a V2 trap
	 * is the last varbind
	 */
	private static final String SNMP_TRAP_ENTERPRISE_OID=".1.3.6.1.6.3.1.1.4.3.0";

	/**
	 * OID prefix for generic SNMP traps
	 */
	private static final String SNMP_TRAPS =".1.3.6.1.6.3.1.1.5";

	/**
	 * The SNMP generic value for an enterprise-specific trap
	 */
	private static final int ENTERPRISE_SPECIFIC = 6;

  /**
   * Map of factories for generating different types of SNMP variable
   * binding content
   */
	private HashMap m_factoryMap;

  /**
   * Trap session for sending traps
   */
	private SnmpTrapSession m_trapSession;

	/**
	 * Constructs a new SNMPTrapHelper.
	 */
	public SnmpTrapHelper()
	{
		
		// create the trap session

		try
		{
			// The port -1 tells SnmpPortal to find any unused port on 
			// the system.
			m_trapSession = new SnmpTrapSession(this, -1);
		}

		catch (Exception e)
		{
			Category log = ThreadCategory.getInstance(SnmpTrapHelper.class);
			log.error("SnmpTrapHelper failed to open trap session: " + e.getMessage());
		}

    // create and populate the factory map
    
		m_factoryMap = new HashMap();

		m_factoryMap.put(EventConstants.TYPE_SNMP_OCTET_STRING, new SnmpOctetStringFactory());
		m_factoryMap.put(EventConstants.TYPE_SNMP_INT32, new SnmpInt32Factory());
		m_factoryMap.put(EventConstants.TYPE_SNMP_NULL, new SnmpNullFactory());
		m_factoryMap.put(EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, new SnmpObjectIdFactory());
		m_factoryMap.put(EventConstants.TYPE_SNMP_IPADDRESS, new SnmpIPAddressFactory());
		m_factoryMap.put(EventConstants.TYPE_SNMP_TIMETICKS, new SnmpTimeTicksFactory());
		m_factoryMap.put(EventConstants.TYPE_SNMP_COUNTER32, new SnmpCounter32Factory());
		m_factoryMap.put(EventConstants.TYPE_SNMP_GAUGE32, new SnmpGauge32Factory());
		m_factoryMap.put(EventConstants.TYPE_SNMP_OPAQUE, new SnmpOpaqueFactory());
		m_factoryMap.put(EventConstants.TYPE_SNMP_COUNTER64, new SnmpCounter64Factory());
	}

	/**
	 * Stops the SnmpTrapHelper. If there is a valid SnmpTrapSession,
	 * that trap session is stopped. 
	 */
	public void stop()
	{
		if (m_trapSession != null)
		{
			m_trapSession.close();
		}
	}

	// BEGIN: Implement SnmpTrapHandler

	public void snmpReceivedTrap(SnmpTrapSession session, InetAddress agent, int port, SnmpOctetString community, SnmpPduPacket pdu)
	{
		Category log = ThreadCategory.getInstance(SnmpTrapHelper.class);
		log.error("SnmpTrapHelper received unexpected trap");
	}

	public void snmpReceivedTrap(SnmpTrapSession session, InetAddress agent, int port, SnmpOctetString community, SnmpPduTrap pdu)
	{
		Category log = ThreadCategory.getInstance(SnmpTrapHelper.class);
		log.error("SnmpTrapHelper received unexpected trap");
	}

	public void snmpTrapSessionError(SnmpTrapSession session, int error, Object ref)
	{
		Category log = ThreadCategory.getInstance(SnmpTrapHelper.class);
		log.error("Trap session error in SnmpTrapHelper");
	}

	// END: Implement SnmpTrapHandler

  /**
   * Common interface for all variabe binding factories
   */
	private interface VarBindFactory
	{
		/**
		 * Constructs a new SnmpVarBind with the specified name and value. The 
		 * value is assumed to have been encoded with the specified encoding 
		 * (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64).
		 *
		 * @param name	The name (a.k.a. "id") of the variable binding to be created
		 * @param encoding  Describes the way in which the value content has been
		 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
		 * @param value  The variable binding value
		 *
		 * @return  The newly-created variable binding
     * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
     * created for any reason (e.g. encoding not supported, invalid value, etc.).
		 */
		public SnmpVarBind getVarBind(String name, String encoding, String value) throws SnmpTrapHelperException;
	}

  /**
   * Variable binding factory for SnmpOctetString
   */
	private class SnmpOctetStringFactory implements VarBindFactory
	{
		/**
		 * Constructs a new SnmpVarBind with the specified name and value. The value
		 * will be encoded as an SnmpOctetString. The value is assumed to have been 
		 * encoded with the specified encoding (i.e. XML_ENCODING_TEXT, or 
		 * XML_ENCODING_BASE64).
		 *
		 * @param name	The name (a.k.a. "id") of the variable binding to be created
		 * @param encoding  Describes the way in which the value content has been
		 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
		 * @param value  The variable binding value
		 *
		 * @return  The newly-created variable binding
     * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
     * created for any reason (e.g. encoding not supported, invalid value, etc.).
		 */
		public SnmpVarBind getVarBind(String name, String encoding, String value) throws SnmpTrapHelperException
		{

			if (EventConstants.XML_ENCODING_TEXT.equals(encoding))
			{
				return new SnmpVarBind(name, new SnmpOctetString(value.getBytes()));
			}
			else if (EventConstants.XML_ENCODING_BASE64.equals(encoding))
			{
				return new SnmpVarBind(name, new SnmpOctetString(Base64.decodeBase64(value.toCharArray())));
			}
			else
			{
				throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpOctetString");
			}
		}
	}

  /**
   * Variable binding factory for SnmpInt32
   */
	private class SnmpInt32Factory implements VarBindFactory
	{
		/**
		 * Constructs a new SnmpVarBind with the specified name and value. The value
		 * will be encoded as an SnmpInt32. The value is assumed to have been 
		 * encoded with the specified encoding (only XML_ENCODING_TEXT is supported).
		 *
		 * @param name	The name (a.k.a. "id") of the variable binding to be created
		 * @param encoding  Describes the way in which the value content has been
		 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
		 * @param value  The variable binding value
		 *
		 * @return  The newly-created variable binding
     * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
     * created for any reason (e.g. encoding not supported, invalid value, etc.).
		 */
		public SnmpVarBind getVarBind(String name, String encoding, String value) throws SnmpTrapHelperException
		{

			if (EventConstants.XML_ENCODING_TEXT.equals(encoding))
			{
				try
				{
					return new SnmpVarBind(name, new SnmpInt32(value));
				}

				catch (NumberFormatException e)
				{
					throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpInt32");
				}

				catch (NullPointerException e)
				{
					throw new SnmpTrapHelperException("Value is null for SnmpInt32");
				}
			}
			else
			{
				throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpInt32");
			}
		}
	}

  /**
   * Variable binding factory for SnmpNull
   */
	private class SnmpNullFactory implements VarBindFactory
	{
		/**
		 * Constructs a new SnmpVarBind with the specified name and value. The value
		 * will be encoded as an SnmpNull.The value and encoding parameters are ignored.
		 *
		 * @param name	The name (a.k.a. "id") of the variable binding to be created
		 * @param encoding  This parameter value is ignored.
		 * @param value  This parameter value is ignored.
		 *
		 * @return  The newly-created variable binding
		 */
		public SnmpVarBind getVarBind(String name, String encoding, String value)
		{
			return new SnmpVarBind(name, new SnmpNull());
		}
	}

  /**
   * Variable binding factory for SnmpObjectId
   */
	private class SnmpObjectIdFactory implements VarBindFactory
	{
		/**
		 * Constructs a new SnmpVarBind with the specified name and value. The value
		 * will be encoded as an SnmpObjectId. The value is assumed to have been 
		 * encoded with the specified encoding (only XML_ENCODING_TEXT is supported).
		 *
		 * @param name	The name (a.k.a. "id") of the variable binding to be created
		 * @param encoding  Describes the way in which the value content has been
		 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
		 * @param value  The variable binding value
		 *
		 * @return  The newly-created variable binding
     * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
     * created for any reason (e.g. encoding not supported, invalid value, etc.).
		 */
		public SnmpVarBind getVarBind(String name, String encoding, String value) throws SnmpTrapHelperException
		{

			if (EventConstants.XML_ENCODING_TEXT.equals(encoding))
			{
				return new SnmpVarBind(name, new SnmpObjectId(value));
			}
			else
			{
				throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpObjectId");
			}
		}
	}

  /**
   * Variable binding factory for SnmpIPAddress
   */
	private class SnmpIPAddressFactory implements VarBindFactory
	{
		/**
		 * Constructs a new SnmpVarBind with the specified name and value. The value
		 * will be encoded as an SnmpIPAddress. The value is assumed to have been 
		 * encoded with the specified encoding (only XML_ENCODING_TEXT is supported).
		 *
		 * @param name	The name (a.k.a. "id") of the variable binding to be created
		 * @param encoding  Describes the way in which the value content has been
		 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
		 * @param value  The variable binding value
		 *
		 * @return  The newly-created variable binding
     * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
     * created for any reason (e.g. encoding not supported, invalid value, etc.).
		 */
		public SnmpVarBind getVarBind(String name, String encoding, String value) throws SnmpTrapHelperException
		{

			if (EventConstants.XML_ENCODING_TEXT.equals(encoding))
			{
				try
				{
					return new SnmpVarBind(name, new SnmpIPAddress(value));
				}

				catch (SnmpBadConversionException e)
				{
					throw new SnmpTrapHelperException("Value " + value + "is invalid, or host unknown for SnmpIPAddress");
				}
			}
			else
			{
				throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpIPAddress");
			}
		}
	}

  /**
   * Variable binding factory for SnmpTimeTicks
   */
	private class SnmpTimeTicksFactory implements VarBindFactory
	{
		/**
		 * Constructs a new SnmpVarBind with the specified name and value. The value
		 * will be encoded as an SnmpTimeTicks. The value is assumed to have been 
		 * encoded with the specified encoding (only XML_ENCODING_TEXT is supported).
		 *
		 * @param name	The name (a.k.a. "id") of the variable binding to be created
		 * @param encoding  Describes the way in which the value content has been
		 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
		 * @param value  The variable binding value
		 *
		 * @return  The newly-created variable binding
     * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
     * created for any reason (e.g. encoding not supported, invalid value, etc.).
		 */
		public SnmpVarBind getVarBind(String name, String encoding, String value) throws SnmpTrapHelperException
		{

			if (EventConstants.XML_ENCODING_TEXT.equals(encoding))
			{
				try
				{
					return new SnmpVarBind(name, new SnmpTimeTicks(value));
				}

				catch (IllegalArgumentException e)
				{
					throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpTimeTicks");
				}

				catch (NullPointerException e)
				{
					throw new SnmpTrapHelperException("Value is null for SnmpTimeTicks");
				}
			}
			else
			{
				throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpTimeTicks");
			}
		}
	}

  /**
   * Variable binding factory for SnmpCounter32
   */
	private class SnmpCounter32Factory implements VarBindFactory
	{
		/**
		 * Constructs a new SnmpVarBind with the specified name and value. The value
		 * will be encoded as an SnmpCounter32. The value is assumed to have been 
		 * encoded with the specified encoding (only XML_ENCODING_TEXT is supported).
		 *
		 * @param name	The name (a.k.a. "id") of the variable binding to be created
		 * @param encoding  Describes the way in which the value content has been
		 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
		 * @param value  The variable binding value
		 *
		 * @return  The newly-created variable binding
     * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
     * created for any reason (e.g. encoding not supported, invalid value, etc.).
		 */
		public SnmpVarBind getVarBind(String name, String encoding, String value) throws SnmpTrapHelperException
		{

			if (EventConstants.XML_ENCODING_TEXT.equals(encoding))
			{
				try
				{
					return new SnmpVarBind(name, new SnmpCounter32(value));
				}

				catch (IllegalArgumentException e)
				{
					throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpCounter32");
				}

				catch (NullPointerException e)
				{
					throw new SnmpTrapHelperException("Value is null for SnmpCounter32");
				}
			}
			else
			{
				throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpCounter32");
			}
		}
	}

  /**
   * Variable binding factory for SnmpGuage32
   */
	private class SnmpGauge32Factory implements VarBindFactory
	{
		/**
		 * Constructs a new SnmpVarBind with the specified name and value. The value
		 * will be encoded as an SnmpGuage32. The value is assumed to have been 
		 * encoded with the specified encoding (only XML_ENCODING_TEXT is supported).
		 *
		 * @param name	The name (a.k.a. "id") of the variable binding to be created
		 * @param encoding  Describes the way in which the value content has been
		 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
		 * @param value  The variable binding value
		 *
		 * @return  The newly-created variable binding
     * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
     * created for any reason (e.g. encoding not supported, invalid value, etc.).
		 */
		public SnmpVarBind getVarBind(String name, String encoding, String value) throws SnmpTrapHelperException
		{

			if (EventConstants.XML_ENCODING_TEXT.equals(encoding))
			{
				try
				{
					return new SnmpVarBind(name, new SnmpGauge32(value));
				}

				catch (IllegalArgumentException e)
				{
					throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpGauge32");
				}

				catch (NullPointerException e)
				{
					throw new SnmpTrapHelperException("Value is null for SnmpGauge32");
				}
			}
			else
			{
				throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpGauge32");
			}
		}
	}

  /**
   * Variable binding factory for SnmpOpaque
   */
	private class SnmpOpaqueFactory implements VarBindFactory
	{
		/**
		 * Constructs a new SnmpVarBind with the specified name and value. The value
		 * will be encoded as an SnmpOpaque. The value is assumed to have been 
		 * encoded with the specified encoding (only XML_ENCODING_BASE64 is supported).
		 *
		 * @param name	The name (a.k.a. "id") of the variable binding to be created
		 * @param encoding  Describes the way in which the value content has been
		 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
		 * @param value  The variable binding value
		 *
		 * @return  The newly-created variable binding
     * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
     * created for any reason (e.g. encoding not supported, invalid value, etc.).
		 */
		public SnmpVarBind getVarBind(String name, String encoding, String value) throws SnmpTrapHelperException
		{

			if (EventConstants.XML_ENCODING_BASE64.equals(encoding))
			{
				return new SnmpVarBind(name, new SnmpOpaque(Base64.decodeBase64(value.toCharArray())));
			}
			else
			{
				throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpOpaque");
			}
		}
	}

  /**
   * Variable binding factory for SnmpCounter64
   */
	private class SnmpCounter64Factory implements VarBindFactory
	{
		/**
		 * Constructs a new SnmpVarBind with the specified name and value. The value
		 * will be encoded as an SnmpCounter64. The value is assumed to have been 
		 * encoded with the specified encoding (only XML_ENCODING_TEXT is supported).
		 *
		 * @param name	The name (a.k.a. "id") of the variable binding to be created
		 * @param encoding  Describes the way in which the value content has been
		 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
		 * @param value  The variable binding value
		 *
		 * @return  The newly-created variable binding
     * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
     * created for any reason (e.g. encoding not supported, invalid value, etc.).
		 */
		public SnmpVarBind getVarBind(String name, String encoding, String value) throws SnmpTrapHelperException
		{

			if (EventConstants.XML_ENCODING_TEXT.equals(encoding))
			{
				try
				{
					return new SnmpVarBind(name, new SnmpCounter64(value));
				}

				catch (IllegalArgumentException e)
				{
					throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpCounter64");
				}

				catch (NullPointerException e)
				{
					throw new SnmpTrapHelperException("Value is null for SnmpCounter64");
				}
			}
			else
			{
				throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpCounter64");
			}
		}
	}

  /**
   * Send the specified SNMP V1 trap to the specified address and port, with the 
   * specified community string.
   *
   * @param  community  The community string to be used.
   * @param  trap       The trap to be sent.
   * @param  destAddr   The IP address to which the trap should be sent.
   * @param  destPort   The port to which the trap should be sent.
   *
   * @exception  Throws SnmpTrapHelperException if the trap cannot be
   * sent for any reason.
   */
	public void sendTrap(String community, SnmpPduTrap trap, String destAddr, int destPort) throws SnmpTrapHelperException
	{

		try
		{
			if (m_trapSession != null)
			{
				SnmpPeer peer = new SnmpPeer(InetAddress.getByName(destAddr), destPort);
				peer.setParameters(new SnmpParameters(community));
				m_trapSession.send(peer, trap);
			}
		}

		catch (SnmpPduEncodingException e)
		{
			throw new SnmpTrapHelperException("Failed to send trap", e);
		}

		catch (AsnEncodingException e)
		{
			throw new SnmpTrapHelperException("Failed to send trap", e);
		}

		catch (IOException e)
		{
			throw new SnmpTrapHelperException("Failed to send trap", e);
		}
	}

  /**
   * Send the specified SNMP V2 trap to the specified address and port, with the 
   * specified community string.
   *
   * @param  community  The community string to be used.
   * @param  packet     The trap to be sent.
   * @param  destAddr   The IP address to which the trap should be sent.
   * @param  destPort   The port to which the trap should be sent.
   *
   * @exception  Throws SnmpTrapHelperException if the trap cannot be
   * sent for any reason.
   */
	public void sendTrap(String community, SnmpPduPacket packet, String destAddr, int destPort) throws SnmpTrapHelperException
	{

		try
		{
			if (m_trapSession != null)
			{
				SnmpPeer peer = new SnmpPeer(InetAddress.getByName(destAddr), destPort);
				peer.setParameters(new SnmpParameters(community));
				m_trapSession.send(peer, packet);
			}
		}

		catch (SnmpPduEncodingException e)
		{
			throw new SnmpTrapHelperException("Failed to send trap", e);
		}

		catch (AsnEncodingException e)
		{
			throw new SnmpTrapHelperException("Failed to send trap", e);
		}

		catch (IOException e)
		{
			throw new SnmpTrapHelperException("Failed to send trap", e);
		}
	}

  /**
   * Create an SNMP V1 trap with the specified enterprise IS, agent address, generic ID,
   * specific ID, and time stamp.
   *
   * @param  entId  The enterprise ID for the trap.
   * @param  agentAddr  The agent address for the trap.
   * @param  generic  The generic ID for the trap.
   * @param  specific  The specific ID for the trap.
   * @param  timeStamp  The time stamp for the trap.
   *
   * @return  The newly-created trap.
   */
	public SnmpPduTrap createV1Trap(String entId, String agentAddr, int generic, int specific, long timeStamp)
	{

		SnmpPduTrap trap = new SnmpPduTrap();
		trap.setEnterprise(entId);
		IPv4Address ipv4AgentAddr = new IPv4Address(agentAddr);
		trap.setAgentAddress(new SnmpIPAddress(ipv4AgentAddr.getAddressBytes()));
		trap.setGeneric(generic);
		trap.setSpecific(specific);
		trap.setTimeStamp(timeStamp);

		return trap;
	}

  /**
   * Create an SNMP V2 trap with the specified trap object ID, and sysUpTime
   * value.
   *
   * @param  trapOid  The trap object id.
   * @param  sysUpTime  The system up time.
   *
   * @return  The newly-created trap.
   * @exception  Throws SnmpTrapHelperException if the trap cannot be
   * created for any reason.
   */
	public SnmpPduPacket createV2Trap(String trapOid, String sysUpTime) throws SnmpTrapHelperException
	{

		SnmpPduRequest packet = new SnmpPduRequest();

		addVarBinding(packet, SNMP_SYSUPTIME_OID, EventConstants.TYPE_SNMP_TIMETICKS, sysUpTime);
		addVarBinding(packet, SNMP_TRAP_OID, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, trapOid);

		return packet;
	}

  /** 
   * Crate a new variable binding and add it to the specified SNMP V1 trap.
   * The value encoding is assumed to be XML_ENCODING_TEXT.
   *
   * @param  trap  The trap to which the variable binding should be added.
   * @param  name  The name (a.k.a. "id") of the variable binding to be created
   * @param  type  The type of variable binding to be created
	 * @param value  The variable binding value
	 *
	 * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
	 * added to the trap for any reason.
	 */   
	public void addVarBinding(SnmpPduTrap trap, String name, String type, String value) throws SnmpTrapHelperException
	{
		addVarBinding(trap, name, type, EventConstants.XML_ENCODING_TEXT, value);
	}

  /** 
   * Crate a new variable binding and add it to the specified SNMP V1 trap.
   *
   * @param  trap  The trap to which the variable binding should be added.
   * @param  name  The name (a.k.a. "id") of the variable binding to be created
   * @param  type  The type of variable binding to be created
   * @param  encoding  Describes the way in which the value content has been
	 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
	 * @param value  The variable binding value
	 *
	 * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
	 * added to the trap for any reason.
	 */   
	public void addVarBinding(SnmpPduTrap trap, String name, String type, String encoding, String value) throws SnmpTrapHelperException
	{

		if (name == null)
		{
			throw new SnmpTrapHelperException("Name is null");
		}

		VarBindFactory factory = (VarBindFactory)m_factoryMap.get(type);

		if (factory == null)
		{
			throw new SnmpTrapHelperException("Type " + type + " is invalid or not implemented");
		}

		trap.addVarBind(factory.getVarBind(name, encoding, value));
	}

  /** 
   * Crate a new variable binding and add it to the specified SNMP V2 trap.
   * The value encoding is assumed to be XML_ENCODING_TEXT.
   *
   * @param  packet  The trap to which the variable binding should be added.
   * @param  name  The name (a.k.a. "id") of the variable binding to be created
   * @param  type  The type of variable binding to be created
	 * @param value  The variable binding value
	 *
	 * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
	 * added to the trap for any reason.
	 */   
	public void addVarBinding(SnmpPduPacket packet, String name, String type, String value) throws SnmpTrapHelperException
	{
		addVarBinding(packet, name, type, EventConstants.XML_ENCODING_TEXT, value);
	}

  /** 
   * Crate a new variable binding and add it to the specified SNMP V2 trap.
   *
   * @param  packet  The trap to which the variable binding should be added.
   * @param  name  The name (a.k.a. "id") of the variable binding to be created
   * @param  type  The type of variable binding to be created
   * @param  encoding  Describes the way in which the value content has been
	 * encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
	 * @param value  The variable binding value
	 *
	 * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
	 * added to the trap for any reason.
	 */   
	public void addVarBinding(SnmpPduPacket packet, String name, String type, String encoding, String value) throws SnmpTrapHelperException
	{

		if (name == null)
		{
			throw new SnmpTrapHelperException("Name is null");
		}

		VarBindFactory factory = (VarBindFactory)m_factoryMap.get(type);

		if (factory == null)
		{
			throw new SnmpTrapHelperException("Type " + type + " is invalid or not implemented");
		}

		packet.addVarBind(factory.getVarBind(name, encoding, value));
	}

  /**
   * Create an SNMP V1 trap, based on the content of the specified event, and forward
   * the trap to the specified address and port. It is assumed that the specified event
   * represents an SNMP V1 or V2 trap that was received by OpenNMS (TrapD).
   *
   * @param  event  The event upon which the trap content should be based
   * @param  destAddr  The address to which the trap should be forwarded
   * @param  destPort  The port to which the trap should be forwarded
   *
	 * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
	 * added to the trap for any reason.
	 */
	public void forwardV1Trap(Event event, String destAddr, int destPort) throws SnmpTrapHelperException
	{
    // the event must correspond to an SNMP trap
    
		Snmp snmpInfo = event.getSnmp();

		if (snmpInfo == null)
		{
			throw new SnmpTrapHelperException("Cannot forward an event with no SNMP info: " + event.getUei());
		}

    // check the version of the original trap
    
		String version = snmpInfo.getVersion();

		SnmpPduTrap trap = new SnmpPduTrap();

		if ("v1".equals(version))
		{

			trap.setEnterprise(snmpInfo.getId());

			IPv4Address addr = new IPv4Address(event.getSnmphost());
			trap.setAgentAddress(new SnmpIPAddress(addr.getAddressBytes()));

			trap.setGeneric(snmpInfo.getGeneric());

			trap.setSpecific(snmpInfo.getSpecific());

			trap.setTimeStamp(snmpInfo.getTimeStamp());

			// varbinds

			Parm[] parms = event.getParms().getParm();

			for (int i = 0; i < parms.length; i++)
			{
				Parm parm = parms[i];
				Value value = parm.getValue();

				try
				{
					addVarBinding(trap, parm.getParmName(), value.getType(), value.getEncoding(), value.getContent());
				}

				catch (SnmpTrapHelperException e)
				{
					throw new SnmpTrapHelperException(e.getMessage() + " in event parm[" + i + "]");
				}
			}
		}
		else if ("v2".equals(version))
		{

			// converting V2 trap to V1 (see RFC2576)

			trap.setEnterprise(snmpInfo.getId());

			Parm[] parms = event.getParms().getParm();

			IPv4Address addr = null;

			for (int i = 0; i < parms.length; i++)
			{
				Parm parm = parms[i];

				if (SNMP_TRAP_ADDRESS_OID.equals(parm.getParmName()))
				{
					addr = new IPv4Address(parm.getValue().getContent());
					break;
				}
			}

			if (addr == null)
			{
				addr = new IPv4Address("0.0.0.0");
			}

			trap.setAgentAddress(new SnmpIPAddress(addr.getAddressBytes()));

			trap.setGeneric(snmpInfo.getGeneric());

			trap.setSpecific(snmpInfo.getSpecific());

			trap.setTimeStamp(snmpInfo.getTimeStamp());

			// varbinds

			for (int i = 0; i < parms.length; i++)
			{
				Parm parm = parms[i];
				Value value = parm.getValue();

				// omit any parms with type=Counter64

				if (!(EventConstants.TYPE_SNMP_COUNTER64.equals(value.getType())))
				{

					try
					{
						addVarBinding(trap, parm.getParmName(), value.getType(), value.getEncoding(), value.getContent());
					}

					catch (SnmpTrapHelperException e)
					{
						throw new SnmpTrapHelperException(e.getMessage() + " in event parm[" + i + "]");
					}
				}
			}

		}
		else
		{
			throw new SnmpTrapHelperException("Invalid SNMP version: " + version);
		}

		// send the trap

		sendTrap(snmpInfo.getCommunity(), trap, destAddr, destPort);
	}

  /**
   * Create an SNMP V2 trap, based on the content of the specified event, and forward
   * the trap to the specified address and port. It is assumed that the specified event
   * represents an SNMP V1 or V2 trap that was received by OpenNMS (TrapD).
   *
   * @param  event  The event upon which the trap content should be based
   * @param  destAddr  The address to which the trap should be forwarded
   * @param  destPort  The port to which the trap should be forwarded
   *
	 * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
	 * added to the trap for any reason.
	 */
	public void forwardV2Trap(Event event, String destAddr, int destPort) throws SnmpTrapHelperException
	{

    // the event must correspond to an SNMP trap

		Snmp snmpInfo = event.getSnmp();

		if (snmpInfo == null)
		{
			throw new SnmpTrapHelperException("Cannot forward an event with no SNMP info: " + event.getUei());
		}

    // check the version of the original trap
    
		String version = snmpInfo.getVersion();

		SnmpPduRequest packet = new SnmpPduRequest(SnmpPduPacket.V2TRAP);

		if ("v1".equals(version))
		{

			// converting V1 trap to V2 (see RFC2576)

			addVarBinding(packet, SNMP_SYSUPTIME_OID, EventConstants.TYPE_SNMP_TIMETICKS, Long.toString(snmpInfo.getTimeStamp()));

			String oid;

			if (snmpInfo.getGeneric() == ENTERPRISE_SPECIFIC)
			{
				oid = snmpInfo.getId() + ".0." + snmpInfo.getSpecific();
			}
			else
			{
				oid = SNMP_TRAPS + '.' + (snmpInfo.getGeneric() + 1);
			}

			addVarBinding(packet, SNMP_TRAP_OID, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, oid);

			// add the V1 var bindings

			boolean addrPresent = false;
			boolean communityPresent = false;
			boolean enterprisePresent = false;

			Parm[] parms = event.getParms().getParm();

			for (int i = 0; i < parms.length; i++)
			{
				Parm parm = parms[i];
				Value value = parm.getValue();

				try
				{
					addVarBinding(packet, parm.getParmName(), value.getType(), value.getEncoding(), value.getContent());
				}

				catch (SnmpTrapHelperException e)
				{
					throw new SnmpTrapHelperException(e.getMessage() + " in event parm[" + i + "]");
				}

				if (SNMP_TRAP_ADDRESS_OID.equals(parm.getParmName()))
				{
					addrPresent = true;
				}
				else if (SNMP_TRAP_COMMUNITY_OID.equals(parm.getParmName()))
				{
					communityPresent = true;
				}
				else if (SNMP_TRAP_ENTERPRISE_OID.equals(parm.getParmName()))
				{
					enterprisePresent = true;
				}
			}

			if (!addrPresent)
			{
				addVarBinding(packet,
											SNMP_TRAP_ADDRESS_OID,
											EventConstants.TYPE_SNMP_IPADDRESS,
											event.getSnmphost());
			}

			if (!communityPresent)
			{
				addVarBinding(packet,
											SNMP_TRAP_COMMUNITY_OID,
											EventConstants.TYPE_SNMP_OCTET_STRING,
											snmpInfo.getCommunity());
			}

			if (!enterprisePresent)
			{
				addVarBinding(packet,
											SNMP_TRAP_ENTERPRISE_OID,
											EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER,
											snmpInfo.getId());
			}
		}
		else if ("v2".equals(version))
		{

			Parm[] parms = event.getParms().getParm();

			for (int i = 0; i < parms.length; i++)
			{
				Parm parm = parms[i];
				Value value = parm.getValue();

				try
				{
					addVarBinding(packet, parm.getParmName(), value.getType(), value.getEncoding(), value.getContent());
				}

				catch (SnmpTrapHelperException e)
				{
					throw new SnmpTrapHelperException(e.getMessage() + " in event parm[" + i + "]");
				}

			}
		}
		else
		{
			throw new SnmpTrapHelperException("Invalid SNMP version: " + version);
		}

		// send the trap

		sendTrap(snmpInfo.getCommunity(), packet, destAddr, destPort);
	}
	
  /**
   * Create an SNMP trap, based on the content of the specified event, and forward
   * the trap to the specified address and port. It is assumed that the specified event
   * represents an SNMP V1 or V2 trap that was received by OpenNMS (TrapD). The type
   * of trap to be created depends on the type of the original trap (i.e. if the 
   * original trap was an SNMP V1 trap, an SNMP V1 trap will be created; if the  
   * original trap was an SNMP V2 trap, an SNMP V2 trap will be created).
   *
   * @param  event  The event upon which the trap content should be based
   * @param  destAddr  The address to which the trap should be forwarded
   * @param  destPort  The port to which the trap should be forwarded
   *
	 * @exception  Throws SnmpTrapHelperException if the variable binding cannot be
	 * added to the trap for any reason.
	 */

	public void forwardTrap(Event event, String destAddr, int destPort) throws SnmpTrapHelperException
	{

		Snmp snmpInfo = event.getSnmp();

		if (snmpInfo == null)
		{
			throw new SnmpTrapHelperException("Cannot forward an event with no SNMP info: " + event.getUei());
		}

		String version = snmpInfo.getVersion();

		if ("v1".equals(version))
		{
			forwardV1Trap(event, destAddr, destPort);
		}
		else if ("v2".equals(version))
		{
			forwardV2Trap(event, destAddr, destPort);
		}
		else
		{
			throw new SnmpTrapHelperException("Invalid SNMP version: " + version);
		}
	}
}
