/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc. All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ticketer.remedy;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.integration.remedy.ticketservice.AuthenticationInfo;
import org.opennms.integration.remedy.ticketservice.CreateInputMap;
import org.opennms.integration.remedy.ticketservice.GetInputMap;
import org.opennms.integration.remedy.ticketservice.GetOutputMap;
import org.opennms.integration.remedy.ticketservice.HPD_IncidentInterface_Create_WSPortTypePortType;
import org.opennms.integration.remedy.ticketservice.HPD_IncidentInterface_Create_WSServiceLocator;
import org.opennms.integration.remedy.ticketservice.HPD_IncidentInterface_WSPortTypePortType;
import org.opennms.integration.remedy.ticketservice.HPD_IncidentInterface_WSServiceLocator;
import org.opennms.integration.remedy.ticketservice.ImpactType;
import org.opennms.integration.remedy.ticketservice.Reported_SourceType;
import org.opennms.integration.remedy.ticketservice.Service_TypeType;
import org.opennms.integration.remedy.ticketservice.SetInputMap;
import org.opennms.integration.remedy.ticketservice.StatusType;
import org.opennms.integration.remedy.ticketservice.Status_ReasonType;
import org.opennms.integration.remedy.ticketservice.UrgencyType;
import org.opennms.integration.remedy.ticketservice.VIPType;
import org.opennms.integration.remedy.ticketservice.Work_Info_SourceType;
import org.opennms.integration.remedy.ticketservice.Work_Info_TypeType;
import org.opennms.integration.remedy.ticketservice.Work_Info_View_AccessType;


import org.opennms.api.integration.ticketing.*;
import org.opennms.api.integration.ticketing.Ticket.State;

/**
 * OpenNMS Trouble Ticket Plugin API implementation for Remedy
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @author <a href="antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class RemedyTicketerPlugin implements Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(RemedyTicketerPlugin.class);
    
	private DefaultRemedyConfigDao m_configDao; 
	
	private String m_endpoint;
	private String m_portname;
	private String m_createendpoint; 
	private String m_createportname; 
	
	private final static String ACTION_CREATE="CREATE";
	private final static String ACTION_MODIFY="MODIFY";

    public final static String ATTRIBUTE_NODE_LABEL_ID = "nodelabel";
    private final static String ATTRIBUTE_USER_COMMENT_ID = "remedy.user.comment";
	private final static String ATTRIBUTE_URGENCY_ID="remedy.urgency";
	private final static String ATTRIBUTE_ASSIGNED_GROUP_ID="remedy.assignedgroup";
    
	private final static int MAX_SUMMARY_CHARS=99;
	// Remember:
	// Summary ---> alarm logmsg
	// Details ---> alarm descr
	// State   ---> OPEN,CLOSE, CANCELLED
	// User    ---> The owner of the ticket --who create the ticket
	// Attributes --->list of free form attributes in the Ticket.  Typically, from
    // the OnmsAlarm attributes.
	
	/**
	 * <p>Constructor for RemedyTicketerPlugin.</p>
	 */
	public RemedyTicketerPlugin() {
		
		m_configDao = new DefaultRemedyConfigDao();
		m_endpoint = m_configDao.getEndPoint();
		m_portname = m_configDao.getPortName();
		m_createendpoint = m_configDao.getCreateEndPoint();
		m_createportname = m_configDao.getCreatePortName();
	}

	/** {@inheritDoc} */
        @Override
	public Ticket get(String ticketId) throws PluginException {

		Ticket opennmsTicket = new Ticket();

		if (ticketId == null)  {
		    
		    LOG.error("No Remedy ticketID available in OpenNMS Ticket");
		    throw new PluginException("No Remedy ticketID available in OpenNMS Ticket");
		    
		} else {
		    LOG.debug("get: search ticket with id: {}", ticketId);
		    HPD_IncidentInterface_WSPortTypePortType port = getTicketServicePort(m_portname,m_endpoint);
	   
		    if (port != null) {
			    try {
					GetOutputMap outputmap = port.helpDesk_Query_Service(getRemedyInputMap(ticketId) , getRemedyAuthenticationHeader());
					LOG.info("get: found ticket: {} status: {}", ticketId, outputmap.getStatus().getValue());
					LOG.info("get: found ticket: {} urgency: {}", ticketId, outputmap.getUrgency().getValue());
					opennmsTicket.setId(ticketId);
					opennmsTicket.setSummary(outputmap.getSummary());
					opennmsTicket.setDetails(outputmap.getNotes());
					opennmsTicket.setState(remedyToOpenNMSState(outputmap.getStatus()));
					opennmsTicket.setUser(outputmap.getAssigned_Group());
				} catch (RemoteException e) {
					e.printStackTrace();
					throw new PluginException("Problem getting ticket", e);
				}    
		    }		
		}

		// add ticket basics from the Remedy ticket
		return opennmsTicket;

	}


	private State remedyToOpenNMSState(StatusType status) {
		State state = State.OPEN;
		if (status.toString().equals(StatusType._value6) || status.toString().equals(StatusType._value5)) {
			state = State.CLOSED;
		} else if (status.toString().equals(StatusType._value7))
			state = State.CANCELLED;
		return state;
	}

	/** {@inheritDoc} */
        @Override
	public void saveOrUpdate(Ticket newTicket) throws PluginException {
		
		if ((newTicket.getId() == null) ) {
			save(newTicket);
		} else {
    		update(newTicket);
		}					
	}
    
    private void update(Ticket ticket) throws PluginException {
    	
    	HPD_IncidentInterface_WSPortTypePortType port = getTicketServicePort(m_portname,m_endpoint);
    	if (port != null) {
    		try {
    			GetOutputMap remedy = port.helpDesk_Query_Service(getRemedyInputMap(ticket.getId()), getRemedyAuthenticationHeader());
    			if (remedy == null) {
					LOG.info("update: Remedy: Cannot find incident with incindent_number: {}", ticket.getId());
					return;
    			}
    			if (remedy.getStatus().getValue().equals(StatusType._value7)) {
					LOG.info("update: Remedy: Ticket Cancelled. Not updating ticket with incindent_number: {}", ticket.getId());
    				return;
    			}
    			if (remedy.getStatus().getValue().equals(StatusType._value6)) {
					LOG.info("update: Remedy: Ticket Closed. Not updating ticket with incindent_number: {}", ticket.getId());
    				return;
    			}
				SetInputMap output = getRemedySetInputMap(ticket,remedy); 
				
				// The only things to update are urgency and state
				LOG.debug("update: Remedy: found urgency: {} - for ticket with incindent_number: {}", output.getUrgency().getValue(), ticket.getId());
				output.setUrgency(getUrgency(ticket));
				
				LOG.debug("update: opennms status: {} - for ticket with incindent_number: {}", ticket.getState(), ticket.getId());
				
				LOG.debug("update: Remedy: found status: {} - for ticket with incindent_number: {}", output.getStatus().getValue(), ticket.getId());
				State outputState = remedyToOpenNMSState(output.getStatus());
				LOG.debug("update: Remedy: found opennms status: {} - for ticket with incindent_number: {}", outputState, ticket.getId());
				if (! (ticket.getState() == outputState))
					output = opennmsToRemedyState(output,ticket.getState());

				port.helpDesk_Modify_Service(output , getRemedyAuthenticationHeader());
			} catch (RemoteException e) {
				e.printStackTrace();
				throw new PluginException("Problem creating ticket", e);
			}
    	}
		
	}

    private SetInputMap getRemedySetInputMap(Ticket ticket, GetOutputMap output) {
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(new Date());
		return new SetInputMap(
				output.getCategorization_Tier_1(), 
				output.getCategorization_Tier_2(), 
				output.getCategorization_Tier_3(),
				output.getClosure_Manufacturer(), 
				output.getClosure_Product_Category_Tier1(), 
				output.getClosure_Product_Category_Tier2(), 
				output.getClosure_Product_Category_Tier3(), 
				output.getClosure_Product_Model_Version(), 
				output.getClosure_Product_Name(), 
				output.getCompany(), 
				output.getSummary(), 
				output.getNotes(), 
				output.getImpact(), 
				output.getManufacturer(), 
				output.getProduct_Categorization_Tier_1(), 
				output.getProduct_Categorization_Tier_2(), 
				output.getProduct_Categorization_Tier_3(), 
				output.getProduct_Model_Version(), 
				output.getProduct_Name(), 
				output.getReported_Source(), 
				output.getResolution(), 
				output.getResolution_Category(), 
				output.getResolution_Category_Tier_2(), 
				output.getResolution_Category_Tier_3(), 
				"", 
				output.getService_Type(), 
				output.getStatus(), 
				output.getUrgency(), 
				ACTION_MODIFY, 
				"",
				"", 
				Work_Info_TypeType.value10, 
				cal,
				Work_Info_SourceType.value1, 
				VIPType.No, 
				Work_Info_View_AccessType.Public, 
				ticket.getId(), 
				output.getStatus_Reason(), 
				output.getServiceCI(), 
				output.getServiceCI_ReconID(), 
				output.getHPD_CI(), 
				output.getHPD_CI_ReconID(), 
				output.getHPD_CI_FormName(), 
				output.getZ1D_CI_FormName(), 
				"", 
				new byte[0], 
				0);
    }
    
    private UrgencyType getUrgency(Ticket ticket) {

    	UrgencyType urgency;
    	try {
    	if (ticket.getAttribute(ATTRIBUTE_URGENCY_ID) != null )
    		urgency = UrgencyType.fromValue(ticket.getAttribute(ATTRIBUTE_URGENCY_ID));
    	else 
    		urgency = UrgencyType.fromValue(m_configDao.getUrgency());
    	} catch (IllegalArgumentException e) {
        	return  UrgencyType.value4;
    	}
    	return urgency;
    }
    
    private String getAssignedGroup(Ticket ticket) {
    	if (ticket.getAttribute(ATTRIBUTE_ASSIGNED_GROUP_ID) != null) {
    		for ( String group : m_configDao.getTargetGroups()) {
    			if (group.equals(ticket.getAttribute(ATTRIBUTE_ASSIGNED_GROUP_ID)))
    				return m_configDao.getAssignedGroup(group);
    		}
    	}
    	return m_configDao.getAssignedGroup();
    }

    private String getAssignedSupportCompany(Ticket ticket) {
    	if (ticket.getAttribute(ATTRIBUTE_ASSIGNED_GROUP_ID) != null) {
    		for ( String group : m_configDao.getTargetGroups()) {
    			if (group.equals(ticket.getAttribute(ATTRIBUTE_ASSIGNED_GROUP_ID)))
    				return m_configDao.getAssignedSupportCompany(group);
    		}
    	}
    	return m_configDao.getAssignedSupportCompany();
    }

    private String getAssignedSupportOrganization(Ticket ticket) {
    	if (ticket.getAttribute(ATTRIBUTE_ASSIGNED_GROUP_ID) != null) {
    		for ( String group : m_configDao.getTargetGroups()) {
    			if (group.equals(ticket.getAttribute(ATTRIBUTE_ASSIGNED_GROUP_ID)))
    				return m_configDao.getAssignedSupportOrganization(group);
    		}
    	}
    	return m_configDao.getAssignedSupportOrganization();
    }

    
    private String getSummary(Ticket ticket) {
    	StringBuffer summary = new StringBuffer();
    	if (ticket.getAttribute(ATTRIBUTE_NODE_LABEL_ID) != null) {
    		summary.append(ticket.getAttribute(ATTRIBUTE_NODE_LABEL_ID));
    		summary.append(": OpenNMS: ");
    	}
    	summary.append(ticket.getSummary());
    	if (summary.length() > MAX_SUMMARY_CHARS)
    		return summary.substring(0,MAX_SUMMARY_CHARS-1);
    	return summary.toString();
    }
    
    private String getNotes(Ticket ticket) {
    	StringBuffer notes = new StringBuffer("OpenNMS generated ticket by user: ");
    	notes.append(ticket.getUser());
    	notes.append("\n");
    	notes.append("\n");
    	if (ticket.getAttribute(ATTRIBUTE_USER_COMMENT_ID) != null ) {
    	 	notes.append("OpenNMS user comment: ");
     		notes.append(ticket.getAttribute(ATTRIBUTE_USER_COMMENT_ID));
        	notes.append("\n");
        	notes.append("\n");
    	}
    	notes.append("OpenNMS logmsg: ");
    	notes.append(ticket.getSummary());
    	notes.append("\n");
    	notes.append("\n");
    	notes.append("OpenNMS descr: ");
    	notes.append(ticket.getDetails());
    	return notes.toString();
    }
    
    private SetInputMap opennmsToRemedyState(SetInputMap inputmap, State state) {
		LOG.debug("getting remedy state from OpenNMS State: {}", state);

        switch (state) {
            case OPEN:
            	inputmap.setStatus(StatusType.fromValue(StatusType._value4));
            	inputmap.setStatus_Reason(Status_ReasonType.fromValue(m_configDao.getReOpenStatusReason()));
            	break;
            case CANCELLED:
            	inputmap.setStatus(StatusType.fromValue(StatusType._value7));
            	inputmap.setStatus_Reason(Status_ReasonType.fromValue(m_configDao.getCancelledStatusReason()));
            	break;
            case CLOSED:
            	inputmap.setStatus(StatusType.fromValue(StatusType._value5));
            	inputmap.setStatus_Reason(Status_ReasonType.fromValue(m_configDao.getResolvedStatusReason()));
                inputmap.setResolution(m_configDao.getResolution());
                break;
            default:
            	LOG.debug("No valid OpenNMS state on ticket skipping status change");
        }
        
        LOG.debug("OpenNMS state was        {}", state);
        LOG.debug("setting Remedy state ID to {}", inputmap.getStatus().getValue());
        
        
        return inputmap;
	}

	private GetInputMap getRemedyInputMap(String ticketId) {
		GetInputMap parameters = new GetInputMap();
		parameters.setIncident_Number(ticketId);
		return parameters;

    }
    
    private AuthenticationInfo getRemedyAuthenticationHeader() {
		AuthenticationInfo request_header = new AuthenticationInfo();
		request_header.setUserName(m_configDao.getUserName());
		request_header.setPassword(m_configDao.getPassword());

		String authentication = m_configDao.getAuthentication();
		if (authentication != null )
			request_header.setAuthentication(authentication);
		String locale = m_configDao.getLocale();
		if (locale != null) 
			request_header.setLocale(locale);
		String timezone = m_configDao.getTimeZone();
		if (timezone != null)
			request_header.setTimeZone(timezone);
		return request_header;
    }

    
    private CreateInputMap getRemedyCreateInputMap(Ticket newTicket) {

		CreateInputMap createInputMap = new CreateInputMap();
		
		// the only data setted by the opennms ticket alarm
		createInputMap.setSummary(getSummary(newTicket));
		createInputMap.setNotes(getNotes(newTicket));
		
		// all this is mandatory and set using the configuration file
		createInputMap.setFirst_Name(m_configDao.getFirstName());
		createInputMap.setLast_Name(m_configDao.getLastName());		
		createInputMap.setServiceCI(m_configDao.getServiceCI());
		createInputMap.setServiceCI_ReconID(m_configDao.getServiceCIReconID());
		createInputMap.setImpact(ImpactType.fromValue(m_configDao.getImpact()));
		createInputMap.setReported_Source(Reported_SourceType.fromValue(m_configDao.getReportedSource()));
		createInputMap.setService_Type(Service_TypeType.fromValue(m_configDao.getServiceType()));
		createInputMap.setUrgency(getUrgency(newTicket));
		createInputMap.setStatus(StatusType.value1);
		createInputMap.setAction(ACTION_CREATE);
		createInputMap.setCategorization_Tier_1(m_configDao.getCategorizationtier1());
		createInputMap.setCategorization_Tier_2(m_configDao.getCategorizationtier2());
		createInputMap.setCategorization_Tier_3(m_configDao.getCategorizationtier3());
		createInputMap.setAssigned_Group(getAssignedGroup(newTicket));
		createInputMap.setAssigned_Support_Company(getAssignedSupportCompany(newTicket));
		createInputMap.setAssigned_Support_Organization(getAssignedSupportOrganization(newTicket));
		return createInputMap;

		
    }

    private void save(Ticket newTicket) throws PluginException {
    	HPD_IncidentInterface_Create_WSPortTypePortType port = getCreateTicketServicePort(m_createportname,m_createendpoint);
    	try {
			String incident_number = port.helpDesk_Submit_Service(getRemedyAuthenticationHeader(), getRemedyCreateInputMap(newTicket)).getIncident_Number();
			LOG.debug("created new remedy ticket with reported incident number: {}", incident_number);
			newTicket.setId(incident_number);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new PluginException("Problem saving ticket", e);
		}
    	
    }
    
	/**
     * Convenience method for initializing the ticketServicePort and correctly setting the endpoint.
     *
     * @return TicketServicePort to connect to the remote service.
     */
    
    private HPD_IncidentInterface_WSPortTypePortType getTicketServicePort(String portname, String endpoint) throws PluginException {
        
        HPD_IncidentInterface_WSServiceLocator service = new HPD_IncidentInterface_WSServiceLocator();
        
        HPD_IncidentInterface_WSPortTypePortType port = null;

        try {
           service.setEndpointAddress(portname, endpoint);
           port = service.getHPD_IncidentInterface_WSPortTypeSoap();
        } catch (ServiceException e) {
            LOG.error("Failed initialzing Remedy TicketServicePort", e);
            throw new PluginException("Failed initialzing Remedy TicketServicePort", e);
        }
        
        return port;
    }


    /**
     * Convenience method for initialising the ticketServicePort and correctly setting the endpoint.
     *
     * @return TicketServicePort to connect to the remote service.
     */
    
    private HPD_IncidentInterface_Create_WSPortTypePortType getCreateTicketServicePort(String portname, String endpoint) throws PluginException {
        
		HPD_IncidentInterface_Create_WSServiceLocator service = new HPD_IncidentInterface_Create_WSServiceLocator();
        
        HPD_IncidentInterface_Create_WSPortTypePortType port = null;
        
        try {
           service.setEndpointAddress(portname, endpoint);
           port = service.getHPD_IncidentInterface_Create_WSPortTypeSoap();
        } catch (ServiceException e) {
            LOG.error("Failed initialzing Remedy TicketServicePort", e);
            throw new PluginException("Failed initialzing Remedy TicketServicePort", e);
        }
        
        return port;
    }
}
