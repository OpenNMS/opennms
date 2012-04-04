/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ticketer.netsuite;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.HashMap;

import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.Ticket.State;
import org.opennms.core.utils.ThreadCategory;
//import org.opennms.netmgt.model.OnmsAlarm;

import org.opennms.netmgt.netsuite.ReadOnlyNetsuiteConfigDao;

import com.netsuite.webservices.lists.support_2011_2.SupportCase;
import com.netsuite.webservices.lists.support_2011_2.SupportCaseSearch;
//import com.netsuite.webservices.lists.support_2011_2.SupportCaseStatus;
import com.netsuite.webservices.platform.common_2011_2.SupportCaseSearchBasic;
import com.netsuite.webservices.platform.core_2011_2.Passport;
import com.netsuite.webservices.platform.core_2011_2.RecordList;
import com.netsuite.webservices.platform.core_2011_2.RecordRef;
import com.netsuite.webservices.platform.core_2011_2.SearchResult;
import com.netsuite.webservices.platform.core_2011_2.SearchStringField;
import com.netsuite.webservices.platform.core_2011_2.Status;
import com.netsuite.webservices.platform.core_2011_2.types.RecordType;
import com.netsuite.webservices.platform.core_2011_2.types.SearchStringFieldOperator;
import com.netsuite.webservices.platform.faults_2011_2.ExceededRequestLimitFault;
import com.netsuite.webservices.platform.faults_2011_2.InvalidSessionFault;
import com.netsuite.webservices.platform.faults_2011_2.UnexpectedErrorFault;
import com.netsuite.webservices.platform.messages_2011_2.ReadResponse;
import com.netsuite.webservices.platform.messages_2011_2.WriteResponse;
import com.netsuite.webservices.platform_2011_2.NetSuitePortType;
import com.netsuite.webservices.platform_2011_2.NetSuiteServiceLocator;

/*
 * Required fields in Netsuite
 *   - Company
 *   - Status
 *   - Incident Date
 *   - Incident Time
 *   - Case Issue - (using Other - Other)  value?
 *   - Case description (title?)
 *   - Priority - defaults to Medium	
 */

/**
 * OpenNMS Trouble Ticket Plugin API implementation for Netsuite
 * 
 * @author <a href="mailto:jcallaghan@towerstream.com">Jerome Callaghan</a>
 */

public class NetsuiteTicketerPlugin implements Plugin {

	/**
	 * Constructor for NetsuiteTicketerPlugin.
	 */
	public NetsuiteTicketerPlugin() {
		ReadOnlyNetsuiteConfigDao nsdao = new ReadOnlyNetsuiteConfigDao();
		m_username = nsdao.getUsername();
		m_password = nsdao.getPassword();
		m_account = nsdao.getAccount();
		m_role = nsdao.getRole();
		m_baseUrl = nsdao.getBaseURL();
	}

	/**
	 * Implementation of TicketerPlugin API call to retrieve a Netsuite trouble
	 * ticket.
	 * 
	 * @return an OpenNMS Ticket
	 */
	public Ticket get(String ticketId) throws PluginException {
		log().debug("get " + ticketId);

		login();

		Ticket ticket = new Ticket();
		try {
			SupportCase sc = getNetsuiteSupportCaseByCaseNumber(ticketId);
			if (sc != null) {
				log().debug("sc.getCaseNumber: " + sc.getCaseNumber());
				ticket.setId(sc.getCaseNumber());
				ticket.setSummary(sc.getTitle());
				ticket.setState(getOnmsStateForNetsuiteStatus(sc.getStatus().getInternalId()));
				ticket.setDetails("---> Need details <----");
				ticket.setUser("---> Need user <===");
			} else {
				String msg = "getNestuiteSupportCase for " +ticketId +" failed";
				log().error(msg);
				throw(new PluginException(msg));
			}
		} catch (Exception e) {
			String msg = "getNestuiteSupportCase for " +ticketId	+" exception";
			log().error(msg);
			throw(new PluginException(msg, e));
		}
		log().debug("get return ticket " +ticket.getId() +"state=" +ticket.getState());
		logout();
		return ticket;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opennms.api.integration.ticketing.Plugin#saveOrUpdate(org.opennms
	 * .api.integration.ticketing.Ticket)
	 */
	public void saveOrUpdate(Ticket ticket) throws PluginException {
		log().debug("saveOrUpdate " +ticket.getId() +" " +ticket.getSummary() +" " +ticket.getState());
		log().debug(" details: " +ticket.getDetails());
		log().debug(" attributes: " + ticket.getAttributes());

		login();

		try {
			SupportCase ccase = null;
			SupportCase ucase = new SupportCase();

			// If we have a ticket id, the case already exists
			if (ticket.getId() != null && !ticket.getId().equals("")) {
				// Get the case for the case number from Netsuite so we have the
				// internal id
				ccase = getNetsuiteSupportCaseByCaseNumber(ticket.getId());
				
				log().debug("Internal id " + ccase.getInternalId());
				ucase.setInternalId(ccase.getInternalId());

				// Update the data from the Open NMS ticket
				ucase.setTitle(ticket.getSummary());
				RecordRef status = new RecordRef();
				status.setInternalId(getNetsuiteStatusForOnmsState(ticket.getState()));
				status.setType(RecordType.supportCaseStatus);
				ucase.setStatus(status);
				
				// Need to set status with translation
				log().debug("Set status " + ticket.getState());
				log().debug("Set details " + ticket.getDetails());

				// Update in Netsuite
				log().debug("Do update");
				WriteResponse wresp = port.update(ucase);
				if (!wresp.getStatus().isIsSuccess()) {
					log().debug("Error on port.update "
								+wresp.getStatus().getStatusDetail(0).getMessage());
				}
			}

			// else ticket ID is not in OpenNMS
			//   If open ticket for customer with status customer-performance, just update
			//   Otherwise, create
			else {

				// Company
				RecordRef co = new RecordRef();
				co.setInternalId("2557403");
				co.setType(RecordType.customer);
				ucase.setCompany(co);

				// Status
				RecordRef status = new RecordRef();
				status.setInternalId("15");
				status.setType(RecordType.supportCaseStatus);
				ucase.setStatus(status);

				// Incident date
				// Incident time
				ucase.setStartDate(Calendar.getInstance());

				// Case Issue (Other - Other)
				RecordRef issue = new RecordRef();
				issue.setInternalId("12");
				issue.setType(RecordType.issue);
				ucase.setIssue(issue);

				// Priority
				RecordRef priority = new RecordRef();
				priority.setInternalId("2");
				priority.setType(RecordType.supportCasePriority);
				ucase.setPriority(priority);

				// Title
				ucase.setTitle(ticket.getSummary());
				ucase.setCaseNumber(null); // Clear, get error if try to set

				WriteResponse wresp = null;
				log().debug("do add");
				wresp = port.add(ucase);
				if (wresp.getStatus().isIsSuccess()) {
					RecordRef ref = (RecordRef) wresp.getBaseRef();
					String internalId = ref.getInternalId();
					log().debug("Add success " + internalId);
					SupportCase sc = getNetsuiteSupportCase(internalId);
					log().debug("After get " + sc.getCaseNumber());
					ticket.setId(sc.getCaseNumber());
					HashMap<String, String> attributes = new HashMap<String, String>();
					attributes.put("internalid", sc.getInternalId());
				} else {
					log().debug("add failed "
							+ wresp.getStatus().toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logout();

	}

	/**
	 * 
	 * @param caseNumber
	 * @return Support Case in Netsuite for the case number
	 */
	private SupportCase getNetsuiteSupportCaseByCaseNumber(String caseNumber) {

		SupportCase sc = null;
		try {
			RecordRef ref = new RecordRef();
			ref.setType(RecordType.supportCase);
			SupportCaseSearch cs = new SupportCaseSearch();
			SupportCaseSearchBasic sb = new SupportCaseSearchBasic();
			SearchStringField ssfCaseNumber = new SearchStringField();
			ssfCaseNumber.setOperator(SearchStringFieldOperator.is);
			ssfCaseNumber.setSearchValue(caseNumber);
			sb.setCaseNumber(ssfCaseNumber);
			cs.setBasic(sb);
			SearchResult resp = port.search(cs);
			if (resp.getStatus().isIsSuccess()) {
				RecordList reclist = resp.getRecordList();
				sc = (SupportCase) reclist.getRecord(0);
				//SupportCaseStatus scs = new SupportCaseStatus();
				log().debug("Got back: " + sc.getCaseNumber());
			} else {
				log().debug("Get failed "
						+ resp.getStatus().getStatusDetail()[0].getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		sc.setNewSolutionFromMsg(null); // Clear this. Was getting an error
										// trying to update.
		sc.setSearchSolution(null); // Ditto
		return sc;
	}

	private SupportCase getNetsuiteSupportCase(String internalId) {

		SupportCase sc = null;
		try {
			RecordRef ref = new RecordRef();
			ref.setType(RecordType.supportCase);
			ref.setInternalId(internalId);
			ReadResponse resp = port.get(ref);
			if (resp.getStatus().isIsSuccess()) {
				sc = (SupportCase) resp.getRecord();
				log().debug("Got case " + sc.getInternalId() + " "
						+ sc.getCaseNumber());
			} else {
				log().debug("Get failed "
						+ resp.getStatus().getStatusDetail()[0].getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		sc.setNewSolutionFromMsg(null); // Clear this. Was getting an error
										// trying to update.
		sc.setSearchSolution(null); // Ditto
		return sc;
	}

	private State getOnmsStateForNetsuiteStatus(String netsuiteStatus) {
		State state = null;

		if (       netsuiteStatus.equals("15") ||	// Closed - SPAM
				   netsuiteStatus.equals("22") ||   // Closed-Install Failure: Customer Cancelled
				   netsuiteStatus.equals("20") ||	// Closed-Install Failure: Landlord
				   netsuiteStatus.equals("18") ||	// Closed-Install Failure: Non LOS
				   netsuiteStatus.equals("23") ||	// Closed-Install Failure: Non Serviceable Area
				   netsuiteStatus.equals("21") ||	// Closed-Install Failure: POP distance
				   netsuiteStatus.equals("17") ||	// Closed-Install Success
				   netsuiteStatus.equals( "5")		// Closed				   
				) 
		{
			state = State.CLOSED;
		}
		
		else {
			//  1 Not Started
			// 10 In Progress - Level I
			//  9 In Progress - Level II
			// 12 In Progress - Help Desk
			// 27 In Progress - Install Scheduled
			// 29 In Progress - Dispatch Required
			// 16 In Progress - Tech Site Visit
			// 19 In Progress - Site Survey
			// 28 Oh Hold: Monitoring
			//  7 On Hold:  Customer
			//  8 On Hold:  Vendor
			// 25 On Hold:  Landlord Issues
			// 24 On Hold:  No Line of Site
			// 26 On Hold:  POP or Capacity Issues
			// 13 On Hold:  Help Desk
			// 33 Revenue Rescue Upgrade/Downgrade/Move Complete
			//  3 Escalated
			//  4 Re-Opened
			// 14 Pending - Close
			state = State.OPEN;
			
			/*  Nothing corresponding to cancelled
			if (netsuiteStatus.equals("99") ||
				netsuiteStatus.equals("1")) 		// Not Started
			{
				state = State.CANCELLED;
			} 
			*/

		}

		return state;
	}

	
	/**
	 * Convenience method for converting a string representation of the OpenNMS
	 * enumerated ticket states.
	 * 
	 * @param stateIdString
	 * @return the converted
	 *         <code>org.opennms.api.integration.ticketing.Ticket.State</code>
	 */
	private String getNetsuiteStatusForOnmsState(State state) {
		
		if(State.CLOSED    == state) return "5";	// Closed
		else	return "1";							// Not Started
		/*(State.OPEN      == state) */
		//else if(State.CANCELLED == state)
	}

	private void login() {
		log().debug("login");
		// if(port != null) return;
		NetSuiteServiceLocator service = null;
		try {
			service = new NetSuiteServiceLocator();
		} catch (Exception e) {
			e.printStackTrace();
		}
		service.setMaintainSession(true); // support multiple cookie management

		Passport passport = new Passport();
		passport.setEmail(m_username);
		passport.setAccount(m_account);
		RecordRef role = new RecordRef();
		role.setInternalId(m_role);
		passport.setRole(role);
		passport.setPassword(m_password);

		Status status = null;
		try {
			log().debug("stuff " +m_username +" " +m_account +" " +m_password 
					+" " +m_role +" " +m_baseUrl);
			port = service.getNetSuitePort(new URL(m_baseUrl));
			log().debug("do port.login");
			status = port.login(passport).getStatus();
			log().debug("login complete " + status.isIsSuccess());
			if (!status.isIsSuccess()) {
				log().debug("login failed: "
						+ status.getStatusDetail(0).getMessage());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	private void logout() {
		try {
			port.logout();
		} catch (ExceededRequestLimitFault e) {
			e.printStackTrace();
		} catch (UnexpectedErrorFault e) {
			e.printStackTrace();
		} catch (InvalidSessionFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		port = null;
	}

	/**
	 * Convenience logging.
	 * 
	 * @return a log4j Category for this class
	 */
	private ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}

	private NetSuitePortType port;
	private String m_username;
	private String m_password;
	private String m_account;
	private String m_role;
	private String m_baseUrl;
}
