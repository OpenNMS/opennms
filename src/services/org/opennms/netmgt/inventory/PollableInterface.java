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

package org.opennms.netmgt.inventory;

import java.io.IOException;
import java.net.*;
import java.util.*;


import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.scheduler.Scheduler;



/**
 * <P>The PollableInterface class...</P>
 * 
 * @author maurizio
 *
 */
public class PollableInterface 
	implements Pollable
{
	/**
	 * node that this interface belongs to
	 */
	private PollableNode	m_node;
	
	/**
	 * IP address of this interface
	 */
	private InetAddress	m_address;
	
	/**
	 * Last known/current status
	 */
	private int m_status;
	
	/** 
	 * Map of 'PollableGroup' objects keyed by service name
	 */
	private Map	m_groups;
	
	/**
	 * Set by poll() method.
	 */
	private boolean	m_statusChangedFlag;
	
	/** 
	 * Reference to the inventory scheduler
	 */
	private Scheduler m_scheduler;
	
	
	/** 
	 * Constructor.
	 */
	public PollableInterface(PollableNode node, InetAddress address)
	{
		m_node = node;
		m_address = address;
		m_groups = Collections.synchronizedMap(new HashMap());
		m_scheduler = Inventory.getInstance().getScheduler();
		m_statusChangedFlag = false;
		m_status = Pollable.STATUS_UNKNOWN;
	}

	/**
	 * Return the node this interface belongs to
	 */
	public PollableNode getNode()
	{
		return m_node;
	}
	
	/**
	 * Return the address of this interface
	 */
	public InetAddress getAddress()
	{
		return m_address;
	}

	public Collection getGroups()
	{	
		return m_groups.values();
	}
	
	public PollableGroup getGroup(String grpName)
	{
		// Sanity check
		if (grpName == null)
			return null;
			
		return (PollableGroup)m_groups.get(grpName);
	}
	
	/**
	 * Add a PollableGroup object to the services map keyed
	 * by service name.
	 * 
	 */
	public synchronized void addGroup(PollableGroup service)
	{
		m_groups.put(service.getGroupName(), service);
		this.recalculateStatus();
	}
	
	public synchronized void removeGroup(PollableGroup service)
	{
		m_groups.remove(service.getGroupName());
		this.recalculateStatus();
	}
	
	public synchronized void deleteAllGroup()
	{
		m_groups.clear();
	}
	
	/**
	 * Takes a service and returns true if this interface supports 
	 * the service.  Returns false otherwise.  
	 *
	 */
	public boolean supportsGroup(String grpName)
	{
		// Sanity check
		if (grpName == null)
			return false;
		
		PollableGroup pGrp = (PollableGroup)m_groups.get(grpName);
		if (pGrp != null)
			return true;
		else
			return false;
	}
		
	public int getStatus()
	{
		return m_status;
	}
	
	public boolean statusChanged()
	{
		return m_statusChangedFlag;
	}
	
	public synchronized void resetStatusChanged()
	{
		m_statusChangedFlag = false;
		
		// Iterate over service list and reset each services's 
		// status changed flag
		Iterator i = m_groups.values().iterator();
		while (i.hasNext())
		{
			PollableGroup pGrp = (PollableGroup)i.next();
			pGrp.resetStatusChanged();
		}
	}
	
	/**
	 * Responsible for recalculating the UP/DOWN status of the interface.
	 * 
	 */
	public synchronized void recalculateStatus()
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		if (log.isDebugEnabled())
			log.debug("recalculateStatus: interface=" + m_address.getHostAddress());
		
		int status = Pollable.STATUS_UNKNOWN;
		
		boolean allServicesDown = true;
		Iterator iter = m_groups.values().iterator();
		while (iter.hasNext())
		{
			PollableGroup pGrp = (PollableGroup)iter.next();
			if ( pGrp.getStatus() == Pollable.STATUS_UP )
			{
				if (log.isDebugEnabled())
					log.debug("recalculateStatus: grp=" + pGrp.getGroupName() + " status=UP, atleast one grp is UP!");
				allServicesDown = false;
				break;
			}
		}
		
		if (allServicesDown)
			status = Pollable.STATUS_DOWN;
		else
			status = Pollable.STATUS_UP;
		
		
		m_status = status;
		
		if (log.isDebugEnabled())
			log.debug("recalculateStatus: completed, interface=" + m_address.getHostAddress() + " status=" + Pollable.statusType[m_status]);
	}
	
	/**
	 * <P>Invokes a poll of the service.</P>
	 * 
	 * If the service changes status then node outage processing 
	 * will be invoked and the status of the entire interface will be
	 * evaluated.
	 */
	public synchronized int poll(PollableGroup pGrp)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		m_statusChangedFlag = false;
		
		int grpStatus = Pollable.STATUS_UNKNOWN;
		
		// Get configured critical service
		if (log.isDebugEnabled())
			log.debug("poll: polling interface " + m_address.getHostAddress() + 
					" status=" + Pollable.statusType[m_status]);


		if (m_status == Pollable.STATUS_DOWN)
		{
				// Issue poll
			    log.debug("Pollable interface "+m_address.getHostAddress()+": before PollableGroup.poll()");
				try{
					grpStatus = pGrp.inventory();
				}catch(IOException iE){
					try{
						log.debug("poll() failed. Releasing node lock... ("+ m_node.getNodeId()+")" +iE);
						m_node.releaseNodeLock();
					}
					catch (InterruptedException ie) {
							log.error(
								"Failed to release node lock on nodeid "
									+ m_node.getNodeId()
									+ ", thread interrupted.");
					}
					grpStatus = Pollable.STATUS_DOWN;
				}
			    log.debug("Pollable interface "+m_address.getHostAddress()+": after PollableGroup.poll() ("+ m_node.getNodeId()+")");
				if (grpStatus == Pollable.STATUS_UP && pGrp.statusChanged())
				{
					// Mark interface as up 
					m_status = Pollable.STATUS_UP;
					m_statusChangedFlag = true;
				
				}
			
 		}
		// Inventory logic if interface is currently UP
		//
		else if (m_status == Pollable.STATUS_UP)
		{
			// Issue poll
			log.debug("Pollable interface "+m_address.getHostAddress()+": before PollableGroup.poll() ("+ m_node.getNodeId()+")");
			try{
				grpStatus = pGrp.inventory();
			}catch(IOException iE){
				try{
					log.debug("poll() failed. Releasing node lock... ("+ m_node.getNodeId()+")"+iE);
					m_node.releaseNodeLock();
				}
				catch (InterruptedException ie) {
						log.error(
							"Failed to release node lock on nodeid "
								+ m_node.getNodeId()
								+ ", thread interrupted.");
				}
				grpStatus = Pollable.STATUS_DOWN;
			}
			log.debug("Pollable interface "+m_address.getHostAddress()+": after PollableGroup.poll() ("+ m_node.getNodeId()+")");
			if (grpStatus == Pollable.STATUS_DOWN && pGrp.statusChanged())
			{
				// If this is the only group supported by
				// the interface mark it as down
				if (m_groups.size() == 1)
				{
					m_status = Pollable.STATUS_DOWN;
					m_statusChangedFlag = true;
				}

			}
		}
		
		log.debug("poll: poll of interface " + m_address.getHostAddress() + " completed, status= " + Pollable.statusType[m_status]+" ("+ m_node.getNodeId()+")");
		return m_status;
	}
}
