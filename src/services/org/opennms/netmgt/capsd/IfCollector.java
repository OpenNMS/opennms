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

package org.opennms.netmgt.capsd;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.IfTableEntry;
import org.opennms.netmgt.capsd.snmp.IpAddrTable;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;

/**
 * This class is designed to collect all the relevant information from the 
 * target designated during construction. The target is initially polled 
 * using all the configured plugins, then tested for SMB and SNMP. If either
 * of those plugins were detected then an additional collection of the SMB/SNMP
 * information is preformed. If any node has multiple interfaces in it then
 * addition probes of those interfaces are performed. The SNMP/SMB collections
 * are preformed only once though.
 *
 * @author <a href="mailto:weave@oculan.com">Weave</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 *
 */
final class IfCollector
	implements Runnable
{
	/**
	 * The primary target internet address
	 */
	private final InetAddress	m_target;

	/**
	 * The SMB collector. If the interface is determine to have
	 * SMB connectivity then the collector is run.
	 */
	private IfSmbCollector		m_smbCollector;

	/**
	 * If the interface is determined to have SNMP capabilities
	 * then the collector is run.
	 */
	private IfSnmpCollector		m_snmpCollector;

	/**
	 * The list of supported protocols on this interface.
	 */
	private List			m_protocols;

	/**
	 * The list of sub-targets found via SNMP.
	 * Indexed by InetAddress
	 */
	private Map			m_subTargets;
	
	/**
	 * List of SnmpInt32 objects representing each of the
	 * unnamed/non-IP interfaces found via SNMP
	 */
	private List			m_nonIpInterfaces;

	/**
	 * Boolean flag which indicates if SNMP collection is to
	 * be done.
	 */
	private boolean			m_doSnmpCollection;
	
	/**
	 * This class is used to encapsulate the supported protocol
	 * information discovered for an interface. The is the 
	 * combination of the protocol name and the in/out 
	 * qualifiers for the plugin.
	 *
	 * @author <a href="mailto:weave@oculan.com">Weave</a>
	 *
	 */
	static final class SupportedProtocol
	{
		/**
		 * The protocol name
		 */
		private final String	m_name;

		/**
		 * The map of qualifiers from the plugin
		 * that discovered this protocol.
		 */
		private final Map	m_qualifiers;

		/**
		 * Creates a new supported protocol based
		 * upon the protocol string and the qualifier
		 * map.
		 *
		 * @param protoName	The name of the protocol.
		 * @param qualifiers	The protocol qualifiers.
		 */
		SupportedProtocol(String protoName, Map qualifiers)
		{
			m_name = protoName;
			m_qualifiers = qualifiers;
		}

		/** 
		 * Returns the name of the discovered protocol.
		 */
		String getProtocolName()
		{
			return m_name;
		}

		/**
		 * Returns the map of qualifiers from the plugin
		 * that discovered this protocol.
		 */
		Map getQualifiers()
		{
			return m_qualifiers;
		}
	}
	
	/**
	 * This method is used to <em>probe</em> the target addresses using
	 * the configured list of protocol specifications from the Configuration
	 * Manager. The list of supported protocols are added to the
	 * supports list. Any failures in the plugins are logged and discarded.
	 *
	 * @param target	The target to probe
	 * @param supports	The supported protocols (SupportedProtocol)
	 *
	 */
	private static void probe(InetAddress target, List supports)
	{
		Category log = ThreadCategory.getInstance(IfCollector.class);
		String logAddr = target.getHostAddress();

		CapsdConfigFactory.ProtocolInfo[] plugins = CapsdConfigFactory.getInstance().getProtocolSpecification(target);

		// First run the plugins to find out all the capabilities
		// for the interface
		//
		for(int i = 0; i < plugins.length; i++)
		{
			log.debug(logAddr + " testing plugin " + plugins[i].getProtocol());
			if(plugins[i].autoEnabled())
			{
				log.debug(logAddr + " protocol " + plugins[i].getProtocol() + " is auto enabled");
				supports.add(new SupportedProtocol(plugins[i].getProtocol(), null));
				continue;
			}

			try
			{
				Plugin p = plugins[i].getPlugin();
				Map q    = plugins[i].getParameters();
				boolean r= p.isProtocolSupported(target, q);

				log.debug(logAddr + " protocol " + plugins[i].getProtocol() + " supported? " + (r ? "true" : "false"));

				if(r)
					supports.add(new SupportedProtocol(plugins[i].getProtocol(), q));
			}
			catch(UndeclaredThrowableException utE)
			{
				Throwable t = utE.getUndeclaredThrowable();
				if (t instanceof NoRouteToHostException)
				{
					if (CapsdConfigFactory.getInstance().getAbortProtocolScansFlag())
					{
						log.warn("IfCollector: No route to host " + logAddr + ", aborting protocol scans.");
					break;   // Break out of plugin loop
					}
					else
					{
						log.warn("IfCollector: No route to host " + logAddr + ", continuing protocol scans.");
					}
				}
				else
				{
					log.warn("IfCollector: Caught undeclared throwable exception when testing for protocol "
						+ plugins[i].getProtocol() + " on host " + logAddr, utE);
				}
			}
			catch(Throwable t)
			{
				log.warn("IfCollector: Caught an exception when testing for protocol "
					 + plugins[i].getProtocol() + " on host " + logAddr, t);
			}
			log.debug(logAddr + " plugin " + plugins[i].getProtocol() + " completed!");
		}
	}

	/**
	 * Default constructor. This constructor is disallowed since the
	 * collector must have a target IP address to collect on. This
	 * constructor will always throw an Unsupported Operation Exception.
	 *
	 */
	IfCollector()
	{
		throw new UnsupportedOperationException("default construction not available!");
	}

	/**
	 * Constructs a new collector instance. The collector's target is passed
	 * as an argument to the constructor. Very little initialization is preformed
	 * in the constructor. The main work of the class is preformed in the 
	 * {@link #run run} method. This provides a well known interface that can
	 * be collected in a thread pool or directly invoked.
	 *
	 * @param addr	The target of the poll.
	 * @param doSnmpCollection  Flag which indicates if SNMP collection should
	 *                          be done.
	 *
	 */
	IfCollector(InetAddress addr, boolean doSnmpCollection)
	{
		m_target = addr;
		m_doSnmpCollection = doSnmpCollection;
		m_smbCollector = null;
		m_snmpCollector = null;
		m_protocols = new ArrayList(8);
		m_subTargets = null;
		m_nonIpInterfaces = null;
	}

	/**
	 * Returns the target of this collection
	 */
	InetAddress getTarget()
	{
		return m_target;
	}

	/**
	 * Returns the supported protocols for this
	 * interface.
	 */
	List getSupportedProtocols()
	{
		return m_protocols;
	}

	/**
	 * Returns true if this target had additional
	 * interfaces found by SNMP
	 */
	boolean hasAdditionalTargets()
	{
		return m_subTargets != null && !m_subTargets.isEmpty();
	}

	/**
	 * Returns the map of additional interface targets.
	 * The keys are instances of {@link java.net.InetAddress addresses}
	 * and the mapped values are {@link java.util.List lists} of supported
	 * protocols.
	 *
	 */
	Map getAdditionalTargets()
	{
		return m_subTargets;
	}

	/**
	 * Returns true if this target has non-IP
	 * interfaces found by SNMP
	 */
	boolean hasNonIpInterfaces()
	{
		return m_nonIpInterfaces != null && !m_nonIpInterfaces.isEmpty();
	}

	/**
	 * Returns the list of non-IP interfaces..
	 *
	 */
	List getNonIpInterfaces()
	{
		return m_nonIpInterfaces;
	}
	
	/**
	 * Returns true if the node supported SMB and the collection
	 * succeeded
	 */
	boolean hasSmbCollection()
	{
		return (m_smbCollector != null);
	}

	/**
	 * Returns the collected SMB information for the node.
	 */
	IfSmbCollector getSmbCollector()
	{
		return m_smbCollector;
	}

	/** 
	 * Returns true if the target supported SNMP and
	 * the collection succeeded.
	 */
	boolean hasSnmpCollection()
	{
		return (m_snmpCollector != null);
	}

	/**
	 * Returns the Snmp Collection of information
	 */
	IfSnmpCollector getSnmpCollector()
	{
		return m_snmpCollector;
	}

	/**
	 * The main collection routine of the class. This method is used
	 * to poll the address, and any additional interfaces discovered
	 * via SNMP.
	 *
	 */
	public void run()
	{
		Category log = ThreadCategory.getInstance(IfCollector.class);
		if(log.isDebugEnabled())
			log.debug("IfCollector.run: run method invoked to collect information for address " + m_target.getHostAddress());

		// Now go throught the successful plugin checks
		// and see if either SMB, MSExchange, or SNMP is
		// supported on the target node
		//
		boolean isSnmp = false;
		boolean isSnmpV2 = false;
		boolean isSmb = false;
		boolean hasExchange = false;

		probe(m_target, m_protocols);

		// First run the plugins to find out all the capabilities
		// for the interface
		//
		Iterator iter = m_protocols.iterator();
		while(iter.hasNext())
		{
			SupportedProtocol proto = (SupportedProtocol)iter.next();
			if(proto.getProtocolName().equalsIgnoreCase("snmp"))
			{
				isSnmp = true;
			}
			else if(proto.getProtocolName().equalsIgnoreCase("snmpv2"))
			{
				isSnmpV2 = true;
			}
			else if(proto.getProtocolName().equalsIgnoreCase("smb"))
			{
				isSmb = true;
			}
			else if(proto.getProtocolName().equalsIgnoreCase("msexchange"))
			{
				isSmb = true;
				hasExchange = true;
			}
		}

		// collect the SMB information
		//
		if(isSmb)
		{
			if(log.isDebugEnabled())
				log.debug("IfCollector.run: starting SMB collection");

			try
			{
				m_smbCollector = new IfSmbCollector(m_target, hasExchange);
				m_smbCollector.run();
			}
			catch(Throwable t)
			{
				m_smbCollector = null;
				log.warn("IfCollector.run: Caught an exception when collecting SMB information from target " + m_target.getHostAddress(), t);
			}

			if(log.isDebugEnabled())
				log.debug("IfCollector.run: SMB collection completed");
		}

		// collect the snmp information if necessary
		//
		if((isSnmp || isSnmpV2) && m_doSnmpCollection)
		{
			if(log.isDebugEnabled())
				log.debug("IfCollector.run: starting SNMP collection");

			try
			{
				SnmpPeer peer = SnmpPeerFactory.getInstance().getPeer(m_target, (isSnmpV2)?SnmpSMI.SNMPV2:SnmpSMI.SNMPV1);
				
				m_snmpCollector = new IfSnmpCollector(peer);
				m_snmpCollector.run();
				
				// now probe the remaining interfaces, if any
				//
				if(m_snmpCollector.hasIpAddrTable() && m_snmpCollector.hasIfTable())
				{
					m_subTargets = new TreeMap(KnownIPMgr.AddrComparator.comparator);
					m_nonIpInterfaces = new ArrayList();
					
					// Iterate over ifTable entries
					//
					Iterator i = m_snmpCollector.getIfTable().getEntries().iterator();
					while(i.hasNext())
					{
						//IpAddrTableEntry entry = (IpAddrTableEntry)i.next();
						IfTableEntry ifEntry = (IfTableEntry)i.next();

						// Get the ifIndex
						//
						SnmpInt32 ifIndex = (SnmpInt32)ifEntry.get(IfTableEntry.IF_INDEX);
						if(ifIndex == null)
							continue;
 
						// Get list of all IP addresses for the current ifIndex
						//
						List ipAddrs = IpAddrTable.getIpAddresses(m_snmpCollector.getIpAddrTable().getEntries(), ifIndex.getValue());
						if (ipAddrs == null || ipAddrs.size() == 0)
						{
							// Non IP interface
							InetAddress nonIpAddr = null;
							try
							{
								nonIpAddr = InetAddress.getByName("0.0.0.0");
							}
							catch (UnknownHostException e)
							{
								log.info("IfCollector.run: Failed to create InetAddress for Non IP interface at ifIndex  "
									 + ifIndex + " for original target " + m_target.getHostAddress(), e);
							}
							
							if (ipAddrs == null)
							{
								ipAddrs = new ArrayList();
							}
							ipAddrs.add(nonIpAddr);
						}
						
						// Iterate over this interface's IP address list
						//
						Iterator s = ipAddrs.iterator();
						while (s.hasNext())
						{
							InetAddress subtarget = (InetAddress)s.next();
						
							// if the target failed to convert  or if it
							// is equal to the current target then skip it
							//
							if(subtarget == null || subtarget.equals(m_target))
								continue;
						
							// now find the ifType
							//
							SnmpInt32 ifType = (SnmpInt32)ifEntry.get(IfTableEntry.IF_TYPE);
	
							// lookup of if type failed, next!
							//
							if(ifType == null)
								continue;
	
							// now check for loopback
							// now will allow loopback as long as its IP Address doesn't
							// start with 127
							if(subtarget.getHostAddress().startsWith("127"))
							{
								// Skip if loopback
								if (log.isDebugEnabled())
									log.debug("ifCollector.run: Loopback interface: " + subtarget.getHostAddress() + ", skipping...");
								continue;
							}
	
							// now check for non-IP interface
							//
							if(subtarget.getHostAddress().equals("0.0.0.0"))
							{
								// its a non-IP interface...add its ifIndex to the non-IP interface list
								//
								m_nonIpInterfaces.add(ifIndex);
								continue;
							}
							
							// ok it appears to be ok, so probe it!
							//
							List probelist = new ArrayList();
							if (log.isDebugEnabled())
							{
								log.debug("----------------------------------------------------------------------------------------");
								log.debug("ifCollector.run: probing subtarget " + subtarget.getHostAddress());
							}
							probe(subtarget, probelist);
	
							if (log.isDebugEnabled())
							{
								log.debug("ifCollector.run: adding subtarget " + subtarget.getHostAddress() + " # supported protocols: "+ probelist.size());
								log.debug("----------------------------------------------------------------------------------------");
							}
							m_subTargets.put(subtarget, probelist);
						} // end while(more ip addresses)
					} // end while(more interfaces)
				} // end if(ipAddrTable and ifTable entries collected)
			} // end try()
			catch(Throwable t)
			{
				m_snmpCollector = null;
				log.warn("IfCollector.run: Caught an exception when collecting SNMP information from target " + m_target.getHostAddress(), t);
			}

			if(log.isDebugEnabled())
				log.debug("IfCollector.run: SNMP collection completed");
		} // end if(SNMP supported)

		if(log.isDebugEnabled())
			log.debug("IfCollector.run: run method exiting after collecting information from address " + m_target.getHostAddress());
	}
}
