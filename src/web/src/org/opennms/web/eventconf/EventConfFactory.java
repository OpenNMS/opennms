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
// 2002 Oct 24: Replaced references to HashTable with HashMap.
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

package org.opennms.web.eventconf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.web.eventconf.bobject.Event;
import org.opennms.web.eventconf.bobject.Global;
import org.opennms.web.eventconf.parsers.EventConfParser;
import org.opennms.web.eventconf.parsers.EventConfWriter;
import org.opennms.web.parsers.XMLWriteException;

/**
*/
public class EventConfFactory
{
	/**
	*/
	private static Map m_factoryInstances;
	
	/**
	*/
	private EventConfWriter m_eventConfWriter;
	
	/**
	*/
	private File m_eventConfFile;
	
	/**
	*/
	private List m_events;
	
	/**
	*/
	private Global m_globalInformation;
	
	/**
	*/
	private long m_lastModified;
	
	/**
	*/
	private Map m_eventsMap;
	
	/**
	 *
	 */
	private EventConfFactory(String fileName) throws Exception
	{
		if(fileName != null)
		{
			m_eventConfFile = ConfigFileConstants.getConfigFileByName(fileName);
			
			//parses the xml file and loads the users
			reloadXML();
			
			//prepare the xml writer
			m_eventConfWriter = new EventConfWriter(fileName);
		}
		else
		{
			throw new Exception("File Not Specified");
		}
	}
	
	/**
	*/
	static synchronized public EventConfFactory getInstance() throws Exception
	{
		return getInstance(ConfigFileConstants.getFileName(ConfigFileConstants.EVENT_CONF_FILE_NAME));
	}
	
	/**
	 */
	static synchronized public EventConfFactory getInstance(String fileName) throws Exception
	{
		EventConfFactory factory = null;
		
		if (m_factoryInstances == null)
		{
			m_factoryInstances = new HashMap();
		}
		
		if (m_factoryInstances.containsKey(fileName))
		{
			factory = (EventConfFactory)m_factoryInstances.get(fileName);
		}
		else
		{
			EventConfFactory newFactory = new EventConfFactory(fileName);
			m_factoryInstances.put(fileName, newFactory);
			factory = newFactory;
		}
		
		return factory;
	}
	
	/**
	 *
	 */
	public synchronized void reloadXML() throws Exception
	{
		EventConfParser parser = new EventConfParser();
		parser.parse(m_eventConfFile.getPath());
		m_events = parser.getEventsList();
		m_eventsMap = makeMap(m_events);
		m_globalInformation = parser.getGlobalInfo();
	}
	
	/**
	*/
	private Map makeMap(List eventsList)
	{
		HashMap newMap = new HashMap();
		
		for (int i = 0; i < eventsList.size(); i++)
		{
			Event curEvent = (Event)eventsList.get(i);
			newMap.put(curEvent.getUei(), new Integer(i));
		}
		
		return newMap;
	}
	
	/**
	*/
	public List getEvents()
	{
		if (m_lastModified != m_eventConfFile.lastModified())
		{
			try
			{
				reloadXML();
			}
			catch (Exception e)
			{
				return null;
			}
		}
		
		return m_events;
	}
	
	/**
	 *
	 */
	public Map getEventsMap()
	{
		if (m_lastModified != m_eventConfFile.lastModified())
		{
			try
			{
				reloadXML();
			}
			catch (Exception e)
			{
				return null;
			}
		}
		
		Map newMap = new HashMap();
		
		for (int i = 0; i < m_events.size(); i++)
		{
			Event curEvent = (Event)m_events.get(i);
			newMap.put(curEvent.getUei(), (Event)curEvent.clone());
		}
		
		return newMap;
	}
	
	/**
	*/
	public boolean eventHasNotice(String uei, String notice)
	{
		if (m_lastModified != m_eventConfFile.lastModified())
		{
			try
			{
				reloadXML();
			}
			catch (Exception e)
			{
				return false;
			}
		}
		
		Event event = getEvent(uei);
		
		int index = event.getNoticeIndex(notice);
		
		if (index != -1)
			return true;
		
		return false;
	}
	
	/**
	 *
	 */
	public Event getEvent(String uei)
	{
		if (m_lastModified != m_eventConfFile.lastModified())
		{
			try
			{
				reloadXML();
			}
			catch (Exception e)
			{
				return null;
			}
		}
		
		Integer index = (Integer)m_eventsMap.get(uei);
		
		//return null if the event asked for doesn't exist
		if (index == null)
			return null;
		
		Event original = (Event)m_events.get(index.intValue());
		
		return (Event)original.clone();
	}
	
	/**
	*/
	public List getEventUEIs()
	{
		if (m_lastModified != m_eventConfFile.lastModified())
		{
			try
			{
				reloadXML();
			}
			catch (Exception e)
			{
				return null;
			}
		}
		
		List eventUEIs = new ArrayList();
		
		for (int i = 0; i < m_events.size(); i++)
		{
			Event curEvent = (Event)m_events.get(i);
			eventUEIs.add( curEvent.getUei());
		}
		
		return eventUEIs;
	}
	
	/**
	*/
	public synchronized void saveEvents(Collection eventsList)
		throws XMLWriteException
	{
		//make a backup and save to xml
		m_eventConfWriter.backup();
		writeXML(eventsList);
		
		try
		{
			reloadXML();
		}
		catch (Exception e)
		{
			throw new XMLWriteException("error rereading xml after writing out new configuration: " + e.getMessage());
		}
	}
	
	/**
	*/
	public synchronized void removeEvent(String uei)
		throws XMLWriteException
	{
		Integer index = (Integer)m_eventsMap.get(uei);
		
		//return null if the event asked for doesn't exist
		if (index == null)
			return;
			
		m_events.remove(index.intValue());
		m_eventsMap = makeMap(m_events);
		
		writeXML(m_events);
	}
	
	/**This method saves an event configuration. 
	   NOTE: If an existing event config has the same uei as
	         the new one it will be overridden (updated) by
		 the new event config.
	   @param Event
	  */
	public synchronized void saveEvent(Event event) 
		throws XMLWriteException
	{
		Integer index = (Integer)m_eventsMap.get(event.getUei());
		
		//if the index is null then it is a new event
		if (index == null)
		{
			m_events.add((Event)event.clone());
			m_eventsMap.put(event.getUei(), new Integer(m_events.size()-1));
		}
		//replace the existing event
		else
		{
			m_events.set(index.intValue(), (Event)event.clone());
		}
		
		writeXML(m_events);
	}
	
	/**
	*/
	public synchronized void renameEvent(String newName, Event event)
		throws XMLWriteException
	{
		Integer index = (Integer)m_eventsMap.get(event.getUei());
		
		m_eventsMap.remove(event.getUei());
		m_eventsMap.put(newName, index);
		
		Event originalEvent = (Event)m_events.get(index.intValue());
		originalEvent.setUei(newName);
		
		writeXML(m_events);
	}
	
	/**
	*/
	private void writeXML(Collection events)
		throws XMLWriteException
	{
		List globalAndEvents = new ArrayList();
		globalAndEvents.add(m_globalInformation);
		globalAndEvents.addAll(events);
		
		m_eventConfWriter.save((Collection)globalAndEvents);
		m_lastModified = m_eventConfFile.lastModified();
	}
}
