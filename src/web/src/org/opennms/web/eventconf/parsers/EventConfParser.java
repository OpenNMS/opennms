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
//      http://www.blast.com/
//

package org.opennms.web.eventconf.parsers;

import java.util.ArrayList;
import java.util.List;

import org.opennms.web.eventconf.bobject.AutoAction;
import org.opennms.web.eventconf.bobject.Correlation;
import org.opennms.web.eventconf.bobject.Event;
import org.opennms.web.eventconf.bobject.Forward;
import org.opennms.web.eventconf.bobject.Global;
import org.opennms.web.eventconf.bobject.MaskElement;
import org.opennms.web.eventconf.bobject.OperatorAction;
import org.opennms.web.eventconf.bobject.Snmp;
import org.opennms.web.parsers.BBParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**This class is used to parse data from eventconf.xml file.
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
 * 
 */
public class EventConfParser extends BBParser
{
	/**The list of events parsed from the xml file
	*/
	private List m_events;
	
	/**The current event being parsed from the xml file
	*/
	private Event m_curEvent;
	
	/**The information in the <global> tags of the eventconf.xml
	*/
	private Global m_eventGlobalInfo;
	
	/**Default constructor, intializes the member variables
	*/
	public EventConfParser()
	{
		super();
		m_events = new ArrayList();
		m_eventGlobalInfo = new Global();
	}
	
	/**This method returns the list of Event objects parsed from the xml file.
	   @return List, a list of Event objects
	*/
	public List getEventsList()
	{
		return m_events;
	}
	
	/**
	*/
	public Global getGlobalInfo()
	{
		return m_eventGlobalInfo;
	}
	
	/**This method is called from the parse method and overrides the method
	   in BBParser. It identifies tag names and takes appropriate actions to 
	   get data from the xml tags.
	   @param Element el, the current tree element
	   @param boolean isRoot, true if the element is the root of the tree
	   @return boolean, true if method was successful, false otherwise
	*/
	protected boolean processElement(Element el, boolean isRoot)
	{
		if (el.getTagName().equals("global"))
		{
			processGlobal(el);
		}
		else if (el.getTagName().equals("event"))
		{
			m_curEvent = new Event();
			m_events.add(m_curEvent);
		}
		else if (el.getTagName().equals("uei"))
		{
			m_curEvent.setUei(processParmValue((Node)el));
		}
		else if (el.getTagName().equals("mask"))
		{
			processMask(el, m_curEvent);
		}
		else if (el.getTagName().equals("snmp"))
		{
			m_curEvent.setSnmp(processSnmp(el));
		}
		else if (el.getTagName().equals("descr"))
		{
			m_curEvent.setDescription(processParmValue((Node)el));
		}
		else if (el.getTagName().equals("logmsg"))
		{
			m_curEvent.setLogMessage(processParmValue((Node)el));
		}
		else if (el.getTagName().equals("severity"))
		{
			m_curEvent.setSeverity(processParmValue((Node)el));
		}
		else if (el.getTagName().equals("correlation"))
		{
			Correlation correlation = new Correlation();
			
			correlation.setCorrelationPath(el.getAttribute("path"));
			correlation.setState(el.getAttribute("state"));
			processCorrelation(el, correlation);
			
			m_curEvent.setCorrelation(correlation);
		}
		else if (el.getTagName().equals("operinstruct"))
		{
			m_curEvent.setOperInstruct(processParmValue((Node)el));
		}
		else if (el.getTagName().equals("autoaction"))
		{
			m_curEvent.addAutoAction(processAutoAction(el));
		}
		else if (el.getTagName().equals("operaction"))
		{
			m_curEvent.addOperatorAction(processOperatorAction(el));
		}
		else if (el.getTagName().equals("autoacknowledge"))
		{
			m_curEvent.setAutoAcknowledge(processParmValue((Node)el));
			m_curEvent.setAutoAcknowledgeState(el.getAttribute("state"));
		}
		else if (el.getTagName().equals("loggroup"))
		{
			m_curEvent.addLogGroup(processParmValue((Node)el));
		}
		else if (el.getTagName().equals("notification"))
		{
			m_curEvent.addNotification(processParmValue((Node)el));
		}
		else if (el.getTagName().equals("tticket"))
		{
			m_curEvent.setTticket(processParmValue((Node)el));
			m_curEvent.setTticketState(el.getAttribute("state"));
		}
		else if (el.getTagName().equals("forward"))
		{
			Forward forward = new Forward();
			
			forward.setMechanism(el.getAttribute("mechanism"));
			forward.setState(el.getAttribute("state"));
			forward.setForward(processParmValue((Node)el));
			
			m_curEvent.addForward(forward);
		}
		else if (el.getTagName().equals("mouseovertext"))
		{
			m_curEvent.setMouseOverText(processParmValue((Node)el));
		}
		
		NodeList nl = el.getChildNodes();
		int size = nl.getLength();
		for(int i = 0;i < size;i++)
		{
			processNode(nl.item(i));
		}
		
		return true;
	}
	
	/**<P>This method is used to process a mask tag</P>
	 *
	 * @param Element, the DOM element to handle.
	 * @return Event, the event to add the mask elements to
	 */
	private void processGlobal(Element globalElement)
	{
		NodeList nl = ((Node)globalElement).getChildNodes();
		int size = nl.getLength();
		
		for(int i = 0; i < size; i++)
		{
			Node curNode = nl.item(i);
			if (curNode.getNodeType()== Node.ELEMENT_NODE)
			{
				String curTag = ((Element)curNode).getTagName();
				m_curElement.replace(0, m_curElement.length(), curTag);
				
				if(curTag.equals("security"))
				{
					processSecurity((Element)curNode);
				}
			}
		}
	}
	
	/**<P>This method is used to process a mask tag</P>
	 *
	 * @param Element, the DOM element to handle.
	 * @return Event, the event to add the mask elements to
	 */
	private void processSecurity(Element securityElement)
	{
		NodeList nl = ((Node)securityElement).getChildNodes();
		int size = nl.getLength();
		
		for(int i = 0; i < size; i++)
		{
			Node curNode = nl.item(i);
			if (curNode.getNodeType()== Node.ELEMENT_NODE)
			{
				String curTag = ((Element)curNode).getTagName();
				m_curElement.replace(0, m_curElement.length(), curTag);
				
				if(curTag.equals("doNotOverride"))
				{
					m_eventGlobalInfo.addSecurity(processParmValue(curNode));
				}
			}
		}
	}
	
	/**<P>This method is used to process a mask tag</P>
	 *
	 * @param Element, the DOM element to handle.
	 * @return Event, the event to add the mask elements to
	 */
	private void processMask(Element maskElement, Event event)
	{
		NodeList nl = ((Node)maskElement).getChildNodes();
		int size = nl.getLength();
		
		for(int i = 0; i < size; i++)
		{
			Node curNode = nl.item(i);
			if (curNode.getNodeType()== Node.ELEMENT_NODE)
			{
				String curTag = ((Element)curNode).getTagName();
				m_curElement.replace(0, m_curElement.length(), curTag);
				
				if(curTag.equals("maskelement"))
				{
					MaskElement curMaskElement = new MaskElement();
					event.addMask(curMaskElement);
					processMaskElement((Element)curNode, curMaskElement);
				}
			}
		}
	}
	
	/**<P>This method is used to process a maskelement tag</P>
	 *
	 * @param Element, the DOM element to handle.
	 * @return MaskElement, the MaskElement object to populate
	 */
	private void processMaskElement(Element maskElement, MaskElement element)
	{
		NodeList nl = ((Node)maskElement).getChildNodes();
		int size = nl.getLength();
		
		for(int i = 0; i < size; i++)
		{
			Node curNode = nl.item(i);
			if (curNode.getNodeType()== Node.ELEMENT_NODE)
			{
				String curTag = ((Element)curNode).getTagName();
				m_curElement.replace(0, m_curElement.length(), curTag);
				
				if(curTag.equals("mename"))
				{
					element.setElementName(processParmValue(curNode));
				}
				else if (curTag.equals("mevalue"))
				{
					element.addElementValue(processParmValue(curNode));
				}
			}
		}
	}
	
	/**<P>This method is used to process an snmp tag</P>
	 *
	 * @param Element, the DOM element to handle.
	 * @return Snmp, the parse Snmp object
	 */
	private Snmp processSnmp(Element snmpElement)
	{
		Snmp snmp = new Snmp();
		
		NodeList nl = ((Node)snmpElement).getChildNodes();
		int size = nl.getLength();
		
		for(int i = 0; i < size; i++)
		{
			Node curNode = nl.item(i);
			if (curNode.getNodeType()== Node.ELEMENT_NODE)
			{
				String curTag = ((Element)curNode).getTagName();
				m_curElement.replace(0, m_curElement.length(), curTag);
				
				if(curTag.equals("id"))
				{
					snmp.setId(processParmValue(curNode));
				}
				else if(curTag.equals("idtext"))
				{
					snmp.setIdText(processParmValue(curNode));
				}
				else if(curTag.equals("version"))
				{
					snmp.setVersion(processParmValue(curNode));
				}
				else if(curTag.equals("specific"))
				{
					snmp.setSpecific(processParmValue(curNode));
				}
				else if (curTag.equals("generic"))
				{
					snmp.setGeneric(processParmValue(curNode));
				}
				else if (curTag.equals("community"))
				{
					snmp.setCommunity(processParmValue(curNode));
				}
			}
		}
		
		return snmp;
	}
	
	/**<P>This method is used to process a correlation tag</P>
	 *
	 * @param Element, the DOM element to handle.
	 * @param Correlation, the Correlation object to build
	 */
	private void processCorrelation(Element el, Correlation correlation)
	{
		NodeList nl = ((Node)el).getChildNodes();
		int size = nl.getLength();
		
		for(int i = 0; i < size; i++)
		{
			Node curNode = nl.item(i);
			if (curNode.getNodeType()== Node.ELEMENT_NODE)
			{
				String curTag = ((Element)curNode).getTagName();
				m_curElement.replace(0, m_curElement.length(), curTag);
				
				if(curTag.equals("cuei"))
				{
					correlation.addCorrelationUEI(processParmValue(curNode));
				}
				else if(curTag.equals("cmin"))
				{
					correlation.setCorrelationMin(processParmValue(curNode));
				}
				else if(curTag.equals("cmax"))
				{
					correlation.setCorrelationMax(processParmValue(curNode));
				}
				else if(curTag.equals("ctime"))
				{
					correlation.setCorrelationTime(processParmValue(curNode));
				}
			}
		}
	}
	
	/**<P>This method is used to process an autoaction</P>
	 *
	 * @param Element, the DOM element to handle.
	 * @return AutoAction, the parsed auto action information
	 */
	private AutoAction processAutoAction(Element el)
	{
		AutoAction autoAction = new AutoAction();
		
		autoAction.setAutoAction(processParmValue((Node)el));
		autoAction.setState(el.getAttribute("state"));
		
		return autoAction;
	}
	
	/**<P>This method is used to process an operator action</P>
	 *
	 * @param Element, the DOM element to handle.
	 * @return OperatorAction, the parsed operator action information
	 */
	private OperatorAction processOperatorAction(Element el)
	{
		OperatorAction operatorAction = new OperatorAction();
		
		operatorAction.setOperatorAction(processParmValue((Node)el));
		operatorAction.setMenuText(el.getAttribute("menutext"));
		operatorAction.setState(el.getAttribute("state"));
		
		return operatorAction;
	}
}
