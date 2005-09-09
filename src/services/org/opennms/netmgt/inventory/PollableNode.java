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
//

package org.opennms.netmgt.inventory;


import java.util.*;


import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;


/**
 * <P>The PollableNode class...</P>
 * 
 * @author maurizio
 */
public class PollableNode
{
	/**
	 * nodeId
	 */
	private int	m_nodeId;
	
	/**
	 * last known/current status of the node
	 */
	private int	m_status;
	
	
	/** 
	 * Map of 'PollableInterface' objects keyed by IP address
	 */
	private Map	m_interfaces;
	
	/**
	 * Used to lock access to the PollableNode during a poll()
	 */
	private Object	m_lock;
	private boolean m_isLocked;
	
	private boolean m_isDeleted;
	
	
	/** 
	 * Constructor.
	 */
	public PollableNode(int nodeId)
	{
		m_nodeId = nodeId;
		m_lock = new Object();
		m_isLocked = false;
		m_interfaces = Collections.synchronizedMap(new HashMap());
		m_status = Pollable.STATUS_UNKNOWN;
		m_isDeleted = false;
	}
	
	public int getNodeId()
	{
		return m_nodeId;
	}
	
	public Collection getInterfaces()
	{
		return m_interfaces.values();
	}
	
	public synchronized void addInterface(PollableInterface pInterface)
	{
		m_interfaces.put(pInterface.getAddress().getHostAddress(), pInterface);
		this.recalculateStatus();
	}
	
	public synchronized void deleteAllInterfaces()
	{
		m_interfaces.clear();
	}
	
	public PollableInterface getInterface(String ipAddress)
	{
		return (PollableInterface)m_interfaces.get(ipAddress);
	} 
	
	public synchronized void removeInterface(PollableInterface pInterface)
	{
		m_interfaces.remove(pInterface.getAddress().getHostAddress());
		this.recalculateStatus();
	}
	
	public int getStatus()
	{
		return m_status;
	}
	
	/*public boolean statusChanged()
	{
		return m_statusChangedFlag;
	}*/
	
	public synchronized void resetStatusChanged()
	{
		// Iterate over interface list and reset each interface's 
		// status changed flag
		Iterator i = m_interfaces.values().iterator();
		while (i.hasNext())
		{
			PollableInterface pIf = (PollableInterface)i.next();
			pIf.resetStatusChanged();
		}
	}
	
	public void markAsDeleted()
	{
		m_isDeleted = true;
	}
	
	public boolean isDeleted()
	{
		return m_isDeleted;
	}
	
	/**
	 * Responsible for recalculating this node's UP/DOWN status.
	 */
	public synchronized void recalculateStatus()
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		if (log.isDebugEnabled())
		log.debug("recalculateStatus: nodeId=" + m_nodeId);
		
		int status = Pollable.STATUS_UNKNOWN;
		
		// Inspect status of each of the node's interfaces
		// in order to determine the current status of the node.
		//
		boolean allInterfacesDown = true;
		Iterator iter = m_interfaces.values().iterator();
		while (iter.hasNext())
		{
			PollableInterface pIf = (PollableInterface)iter.next();
			if (pIf.getStatus() == Pollable.STATUS_UNKNOWN)
				pIf.recalculateStatus();
				
			if (pIf.getStatus() == Pollable.STATUS_UP)
			{
				if (log.isDebugEnabled())
				log.debug("recalculateStatus: interface=" + pIf.getAddress().getHostAddress() + 
						" status=Up, atleast one interface is UP!");
				allInterfacesDown = false;
				break;
			}
		}
		
		if (allInterfacesDown)
			status = Pollable.STATUS_DOWN;
		else
			status = Pollable.STATUS_UP;
			
		m_status = status;
		
		if (log.isDebugEnabled())
		log.debug("recalculateStatus: completed, nodeId=" + m_nodeId + " status=" + Pollable.statusType[m_status]);
	
	}
	
	public boolean getNodeLock(long timeout)
		throws InterruptedException
	{
		
	
		boolean ownLock = false;
		Category log = ThreadCategory.getInstance(getClass());
		synchronized(m_lock)
		{
			
			log.debug("getting lock for node "+m_nodeId);
			// Is the node currently locked?
			if (!m_isLocked)
			{
				// Now it is...
				m_isLocked = true;
				ownLock = true;
			}
			else
			{
				// Someone else has the lock, wait
				// for the specified timeout...
				m_lock.wait(timeout);
				
				// Was the lock released?
				if (!m_isLocked)
				{
					// Yep...
					m_isLocked = true;
					ownLock = true;
				}
			}
		}
		log.debug("exiting  from getNodeLock() for node "+m_nodeId+ "ownlock=" +ownLock);
		return ownLock;
	}
	
	public void releaseNodeLock()
		throws InterruptedException
	{
		synchronized(m_lock)
		{
			Category log = ThreadCategory.getInstance(getClass());
			log.debug("releasing node "+m_nodeId);
			if (m_isLocked)
			{
				m_isLocked = false;
				m_lock.notifyAll();
			}
			
			log.debug("node "+m_nodeId+ " released");
		}
	} 

	
	/**  
	 * Invokes a poll of the remote interface. 
	 * 
	 * If the interface changes status then node outage processing 
	 * will be invoked and the status of the entire node will be
	 * evaluated.
	 */
	public synchronized void pollGroup(PollableGroup pGrp)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		if (log.isDebugEnabled())
			log.debug("pollGroup: polling nodeid " + m_nodeId + " status=" + Pollable.statusType[m_status]);
					
		
		// Retrieve PollableInterface object from the NIF
		PollableInterface pInterface = pGrp.getInterface();
		
	
		pInterface.poll(pGrp);
		
		return;
	}
}
