//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
//

package org.opennms.netmgt.outage;

import java.util.*;
import java.text.ParseException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.EventConstants;

/**
 * This class will help solve a current intermittant bug where the resolution
 * to an error event is received before the error event itself, due to the
 * database being busy,
 *
 * If an "up" is received without a matching "down", the event will be 
 * saved off to the cache. Thus, when the "down" is received, it can be matched
 * against the events in this cache to see if it has actually been resolved.
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus</A>
 */
public final class OutageEventCache extends java.util.LinkedList
{
	/**
	* Singleton instance of the ServiceEventArchive class
	*/
	private static final OutageEventCache	m_singleton = new OutageEventCache();
	
	public OutageEventCache()
	{
		super();
	}
	
	/** 
	 * Returns the SelectEventArchive singleton.
	 */
	public static OutageEventCache getInstance()
	{
		return m_singleton;
	}

	/**
	 * Adds an object to the cache.
	 */
	public boolean add(Object o)
	{
		synchronized (m_singleton)
		{
			return super.add(o);
		}
	}
	
	/**
	 * When called, this method will try to find a match for the "unmatched"
	 * error event. It will search from least to most severe -> UNRESPONSIVE
	 * LOST_SERVICE, INTERFACE_DOWN and NODE_DOWN. If a match is made, the 
	 * event from the cache is returned, otherwise null.
	 *
	 * @param eventId       Event id
	 * @param nodeId	Event node ID
	 * @param ipAddr	Event IP address
	 * @param serviceId	Event service ID
	 * @param eventTime	Event timestamp
	 * @param type		Event type (service lost, service regained, etc...)
	 *
	 * @return the matching entry from the cache
	 */
	public OutageEventEntry findCacheMatch(long eventId, long nodeId, String ipAddr, long serviceId, String eventTime, int type)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		java.util.Date serviceLostTime = null;
		try
		{
			serviceLostTime = EventConstants.parseToDate(eventTime);
		}
		catch(ParseException e)
		{
			ThreadCategory.getInstance(this.getClass()).warn("Failed to parse event time: " + eventTime, e);
			return null;
		}
		
		// Iterate over event cache list and attempt to find 
		// a matching entry.  If a matching entry is found which
		// meets the criteria then return the entry 
		//
		OutageEventEntry cacheEntry = null;
		OutageEventEntry currentEvent = new OutageEventEntry(eventId, nodeId, ipAddr, serviceId, eventTime, type);
		boolean match = false;
		
		synchronized (m_singleton)
		{
			ListIterator liter = m_singleton.listIterator();
			while (liter.hasNext() && !match)
			{
				cacheEntry = (OutageEventEntry)liter.next();
				
				if (log.isDebugEnabled())
				{
					log.debug("findCacheMatch: cache entry type=" + cacheEntry.getType() + ", lost entry type=" + currentEvent.getType());
				}
				
				// Now test if cache entry has corresponding event type
				//     if specified type is LOST_SERVICE, looking for REGAINED_SERVICE,
				//     if specified type is INTERFACE_DOWN, looking for INTERFACE_UP, etc...
				//
				switch (type)
				{
					case OutageEventEntry.EVENT_TYPE_LOST_SERVICE :
					// Test to see if there is a cached REGAINED_SERVICE event that matches this
					// LOST_SERVICE event in nodeId, IP Address and serviceID. If so, compare times.
					// If the REGAINED event occurred after the LOST event, a match will be made.
					// Otherwise, if this LOST event matches a cached LOST event, the cached event
					// will be deleted.
						
						if (cacheEntry.getType() == OutageEventEntry.EVENT_TYPE_REGAINED_SERVICE)
						{
							if (cacheEntry.getNodeId() == currentEvent.getNodeId() &&
								cacheEntry.getIpAddr().equals(currentEvent.getIpAddr()) &&
								cacheEntry.getSvcId() == currentEvent.getSvcId())
							{
								java.util.Date serviceRegainedTime = null;
								try
								{
									serviceRegainedTime = EventConstants.parseToDate(cacheEntry.getEventTime());
								}
								catch(ParseException e)
								{
									ThreadCategory.getInstance(this.getClass()).warn("Failed to parse event time " + cacheEntry.getEventTime(), e);
									// Remove the entry
									liter.remove();
									continue;
								}
								
								if (serviceRegainedTime.after(serviceLostTime))
								{
									if (log.isDebugEnabled())
										log.debug("findCacheMatch: Cache Hit! - Outage will be cleared");
									match = true;
									// Remove the entry
									liter.remove();
								}
								else
								{
									// Remove the entry
									liter.remove();
									continue;
								}
							}
						}
						else if (cacheEntry.getType() == OutageEventEntry.EVENT_TYPE_LOST_SERVICE)
						{
							liter.remove();
							continue;
						}
						break;
					
					case OutageEventEntry.EVENT_TYPE_INTERFACE_DOWN :
					// Test to see if there is a cached INTERFACE_UP event that matches this
					// INTERFACE_DOWN event in nodeId and IP Address. If so, compare times.
					// If the INTERFACE_UP event occurred after the INTERFACE_DOWN event, a match will be made.
					// Otherwise, if this INTERFACE_DOWN event matches a cached INTERFACE_DOWN event, the cached event
					// will be deleted.
						
						if (cacheEntry.getType() == OutageEventEntry.EVENT_TYPE_INTERFACE_UP)
						{
							if (cacheEntry.getNodeId() == currentEvent.getNodeId() &&
								cacheEntry.getIpAddr().equals(currentEvent.getIpAddr()))
							{
								java.util.Date serviceRegainedTime = null;
								try
								{
									serviceRegainedTime = EventConstants.parseToDate(cacheEntry.getEventTime());
								}
								catch(ParseException e)
								{
									ThreadCategory.getInstance(this.getClass()).warn("Failed to parse event time " + cacheEntry.getEventTime(), e);
									// Remove the entry
									liter.remove();
									continue;
								}
								
								if (serviceRegainedTime.after(serviceLostTime))
								{
									if (log.isDebugEnabled())
										log.debug("findCacheMatch: Cache Hit! - Outage will be cleared");
									match = true;
									// Remove the entry
									liter.remove();
								}
								else
								{
									// Remove the entry
									liter.remove();
									continue;
								}
							}
						}
						else if (cacheEntry.getType() == OutageEventEntry.EVENT_TYPE_INTERFACE_DOWN)
						{
							liter.remove();
							continue;
						}
						break;
					
					case OutageEventEntry.EVENT_TYPE_NODE_DOWN :
					// Test to see if there is a cached NODE_UP event that matches this
					// NODE_DOWN event in nodeId. If so, compare times.
					// If the NODE_UP event occurred after the NODE_DOWN event, a match will be made.
					// Otherwise, if this NODE_DOWN event matches a cached NODE_DOWN event, the cached event
					// will be deleted.
						
						if (cacheEntry.getType() == OutageEventEntry.EVENT_TYPE_NODE_UP)
						{
							if (cacheEntry.getNodeId() == currentEvent.getNodeId())
							{
								java.util.Date serviceRegainedTime = null;
								try
								{
									serviceRegainedTime = EventConstants.parseToDate(cacheEntry.getEventTime());
								}
								catch(ParseException e)
								{
									ThreadCategory.getInstance(this.getClass()).warn("Failed to parse event time " + cacheEntry.getEventTime(), e);
									// Remove the entry
									liter.remove();
									continue;
								}
								
								if (serviceRegainedTime.after(serviceLostTime))
								{
									if (log.isDebugEnabled())
										log.debug("findCacheMatch: Cache Hit! - Outage will be cleared");
									match = true;
									// Remove the entry
									liter.remove();
								}
								else
								{
									// Remove the entry
									liter.remove();
									continue;
								}
							}
						}
						else if (cacheEntry.getType() == OutageEventEntry.EVENT_TYPE_NODE_DOWN)
						{
							liter.remove();
							continue;
						}
						break;
					
					default :
						log.warn("findCacheMatch: Unexpected Event - Deleting.");
						liter.remove();
						break;
				}
			}
			
			if (match && cacheEntry != null)
			{
				return cacheEntry;
			}
			else
			{
				return null;
			}
			
		} 
	}
}
