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

package org.opennms.web.eventconf.parsers;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.parsers.*;


/**This class is used to save data to the eventconf.xml file.
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
 * 
 */
public class EventConfWriter extends XMLWriter
{
	/**Default constructor, intializes the member variables
	*/
	public EventConfWriter(String fileName)
		throws XMLWriteException
	{
		super(fileName);
	}
	
	/**This method saves the list of events to the specified file
	   @exception XMLWriteException, if the save failed
	*/
	protected void saveDocument(Collection events)
		throws XMLWriteException
	{
		Element root = m_document.createElement("events");
		m_document.appendChild(root);
		
		//if there are any users print them out
		if (events.size() > 0)
		{
			//write each event
			Iterator i = events.iterator();
			while(i.hasNext())
			{
				Object eventObject = i.next();
				
				//expecting the first object in the collection to be the Global information
				if (eventObject instanceof Global)
				{
					buildGlobalElement(root, (Global)eventObject);
				}
				//everything else should be the Event objects
				else if (eventObject instanceof Event)
				{
					Event curEvent = (Event)eventObject;
					
					Element curEventElement = addEmptyElement(root, "event");
					
					//mask information
					if (curEvent.getMask().size() > 0)
					{
						Element maskElement = addEmptyElement(curEventElement, "mask");
						
						List maskElements = curEvent.getMask();
						for (int q = 0; q < maskElements.size(); q++)
						{
							MaskElement curElement = (MaskElement)maskElements.get(q);
							
							Element maskElementElement = addEmptyElement(maskElement, "maskelement");
							
							addDataElement(maskElementElement, "mename", curElement.getElementName());
							
							List values = curElement.getElementValues();
							for (int j = 0; j < values.size(); j++)
							{
								addDataElement(maskElementElement, "mevalue", (String)values.get(j));
							}
						}
					}
					
					addDataElement(curEventElement, "uei", curEvent.getUei());
					
					//snmp tag
					if (curEvent.getSnmp() != null)
					{
						buildSnmpElement(curEventElement, curEvent.getSnmp());
					}
					
					addCDataElement(curEventElement, "descr", curEvent.getDescription());
					
					Element logMessageElement = addCDataElement(curEventElement, "logmsg", curEvent.getLogMessage());
					logMessageElement.setAttribute("dest", curEvent.getLogMessageDestination());
					
					addDataElement(curEventElement, "severity", curEvent.getSeverity());
					
					//correlation tag
					if (curEvent.getCorrelation() != null)
					{
						buildCorrelationElement(curEventElement, curEvent.getCorrelation());
					}
					
					//operator instruction
					if (curEvent.getOperInstruct() != null && !curEvent.getOperInstruct().trim().equals(""))
					{
						addDataElement(curEventElement, "operinstruct", curEvent.getOperInstruct());
					}
					
					List autoActions = curEvent.getAutoActions();
					for (int k = 0; k < autoActions.size(); k++)
					{
						AutoAction curAction = (AutoAction)autoActions.get(k);
						
						Element actionElement = addCDataElement(curEventElement, "autoaction", curAction.getAutoAction());
						actionElement.setAttribute("state", curAction.getState());
					}
					
					List operActions = curEvent.getOperatorActions();
					for (int l = 0; l < operActions.size(); l++)
					{
						OperatorAction curAction = (OperatorAction)operActions.get(l);
						
						Element actionElement = addCDataElement(curEventElement, "operaction", curAction.getOperatorAction());
						actionElement.setAttribute("state", curAction.getState());
						actionElement.setAttribute("menutext", curAction.getMenuText());
					}
					
					List logGroups = curEvent.getLogGroups();
					for (int m = 0; m < logGroups.size(); m++)
					{
						addDataElement(curEventElement, "loggroup", (String)logGroups.get(m));
					}
					
					List notifications = curEvent.getNotifications();
					for (int n = 0; n < notifications.size(); n++)
					{
						addCDataElement(curEventElement, "notification", (String)notifications.get(n));
					}
					
					if (curEvent.getTTicket() != null && !curEvent.getTTicket().trim().equals(""))
					{
						Element ticketElement = addCDataElement(curEventElement, "tticket", curEvent.getTTicket());
						ticketElement.setAttribute("state", curEvent.getTTicketState());
					}
					
					List forwards = curEvent.getForwards();
					for (int p = 0; p < forwards.size(); p++)
					{
						Forward curForward = (Forward)forwards.get(p);
						
						Element forwardElement = addDataElement(curEventElement, "forward", curForward.getForward());
						forwardElement.setAttribute("state", curForward.getState());
						forwardElement.setAttribute("mechanism", curForward.getMechanism());
					}
					
					if (curEvent.getMouseOverText() != null && !curEvent.getMouseOverText().trim().equals(""))
					{
						addDataElement(curEventElement, "mouseovertext", curEvent.getMouseOverText());
					}
				}
			}
		}
		
		serializeToFile();
	}
	
	/**
	*/
	private Element buildGlobalElement(Element parent, Global global)
	{
		Element globalElement = addEmptyElement(parent, "global");
		
		Element securityElement = addEmptyElement(globalElement, "security");
		
		List securities = global.getSecurities();
		
		for (int i = 0; i < securities.size(); i++)
		{
			addDataElement(securityElement, "doNotOverride", (String)securities.get(i));
		}
		
		return globalElement;
	}
	
	/**
	*/
	private Element buildSnmpElement(Element parent, Snmp snmp)
	{
		Element snmpElement = null;
		
		if (snmp != null)
		{
			snmpElement = addEmptyElement(parent, "snmp");
			
			addDataElement(snmpElement, "id", snmp.getId());
			
			if (snmp.getIdText() != null && !snmp.getIdText().trim().equals(""))
			{
				addDataElement(snmpElement, "idtext", snmp.getIdText());
			}
			
			addDataElement(snmpElement, "version", snmp.getVersion());
			
			if (snmp.getSpecific() != null && !snmp.getSpecific().trim().equals(""))
			{
				addDataElement(snmpElement, "specific", snmp.getSpecific());
			}
			
			if (snmp.getGeneric() != null && !snmp.getGeneric().trim().equals(""))
			{
				addDataElement(snmpElement, "generic", snmp.getGeneric());
			}
			
			if (snmp.getCommunity() != null && !snmp.getCommunity().trim().equals(""))
			{
				addDataElement(snmpElement, "community", snmp.getCommunity());
			}
		}
		
		return snmpElement;
	}
	
	/**
	*/
	private Element buildCorrelationElement(Element parent, Correlation correlation)
	{
		Element correlationElement = null;
		
		if (correlation != null)
		{
			correlationElement = addEmptyElement(parent, "correlation");
			correlationElement.setAttribute("path", correlation.getCorrelationPath());
			correlationElement.setAttribute("state", correlation.getState());
			
			List correlationUEIs = correlation.getCorrelationUEIs();
			
			if (correlationUEIs.size() > 0)
			{
				for (int i = 0; i < correlationUEIs.size(); i++)
				{
					addDataElement(correlationElement, "cuei", (String)correlationUEIs.get(i));
				}
			}
			
			if (correlation.getCorrelationMin() != null && !correlation.getCorrelationMin().trim().equals(""))
			{
				addDataElement(correlationElement, "cmin", correlation.getCorrelationMin());
			}
			
			if (correlation.getCorrelationMax() != null && !correlation.getCorrelationMax().trim().equals(""))
			{
				addDataElement(correlationElement, "cmax", correlation.getCorrelationMax());
			}
			
			if (correlation.getCorrelationTime() != null && !correlation.getCorrelationTime().trim().equals(""))
			{
				addDataElement(correlationElement, "ctime", correlation.getCorrelationTime());
			}
		}
		
		return correlationElement;
	}
}
