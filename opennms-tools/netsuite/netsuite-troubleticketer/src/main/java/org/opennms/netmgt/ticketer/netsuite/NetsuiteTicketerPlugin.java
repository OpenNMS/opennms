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
import java.util.List;

import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.Ticket.State;
import org.opennms.core.utils.ThreadCategory;
//import org.opennms.netmgt.model.OnmsAlarm;

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.netsuite.ReadOnlyNetsuiteConfigDao;

//FIXME this is a stub to cover code left over from the axis code generator
import org.opennms.netmgt.netsuite.NetSuiteServiceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.netsuite.webservices.lists.support_2012_1.SupportCase;
import com.netsuite.webservices.lists.support_2012_1.SupportCaseSearch;
//import com.netsuite.webservices.lists.support_2011_2.SupportCaseStatus;
import com.netsuite.webservices.platform.common_2012_1.SupportCaseSearchBasic;
import com.netsuite.webservices.platform.core_2012_1.Passport;
import com.netsuite.webservices.platform.core_2012_1.Record;
import com.netsuite.webservices.platform.core_2012_1.RecordList;
import com.netsuite.webservices.platform.core_2012_1.RecordRef;
import com.netsuite.webservices.platform.core_2012_1.SearchRecord;
import com.netsuite.webservices.platform.core_2012_1.SearchResult;
import com.netsuite.webservices.platform.core_2012_1.SearchStringField;
import com.netsuite.webservices.platform.core_2012_1.Status;
import com.netsuite.webservices.platform.core_2012_1.StatusDetail;
import com.netsuite.webservices.platform.core_2012_1.types.RecordType;
import com.netsuite.webservices.platform.core_2012_1.types.SearchStringFieldOperator;
import com.netsuite.webservices.platform.faults_2012_1.ExceededRequestLimitFault;
import com.netsuite.webservices.platform.faults_2012_1.InvalidSessionFault;
import com.netsuite.webservices.platform.faults_2012_1.UnexpectedErrorFault;
import com.netsuite.webservices.platform.messages_2012_1.AddRequest;
import com.netsuite.webservices.platform.messages_2012_1.AddResponse;
import com.netsuite.webservices.platform.messages_2012_1.GetRequest;
import com.netsuite.webservices.platform.messages_2012_1.GetResponse;
import com.netsuite.webservices.platform.messages_2012_1.LoginRequest;
import com.netsuite.webservices.platform.messages_2012_1.LoginResponse;
import com.netsuite.webservices.platform.messages_2012_1.LogoutRequest;
import com.netsuite.webservices.platform.messages_2012_1.ReadResponse;
import com.netsuite.webservices.platform.messages_2012_1.SearchRequest;
import com.netsuite.webservices.platform.messages_2012_1.SearchResponse;
import com.netsuite.webservices.platform.messages_2012_1.SessionResponse;
import com.netsuite.webservices.platform.messages_2012_1.UpdateRequest;
import com.netsuite.webservices.platform.messages_2012_1.UpdateResponse;
import com.netsuite.webservices.platform.messages_2012_1.WriteResponse;
import com.netsuite.webservices.platform_2012_1.NetSuitePortType;
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

    private NetSuitePortType m_port;
    private String m_username;
    private String m_password;
    private String m_account;
    private String m_role;
    private String m_baseUrl;
    
    @Autowired
    private AlarmDao m_alarmDao;


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
    
    /*
     * Get the node for an alarm.
     */
    @Transactional
    private OnmsAssetRecord getNodeAsset(int alarmId) {
        OnmsAlarm alarm = m_alarmDao.get(alarmId);
        OnmsNode node = alarm.getNode();
        OnmsAssetRecord asset = node.getAssetRecord();
        return asset;
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
        try {
            logout();
        } catch (com.netsuite.webservices.platform_2012_1.InvalidSessionFault e) {
            //FIXME need to log
            throw new PluginException("Logout Failed, invalid session.", e);
        } catch (com.netsuite.webservices.platform_2012_1.UnexpectedErrorFault e) {
            //FIXME need to log
            throw new PluginException("Logout Failed, unexpected error.", e);
        } catch (com.netsuite.webservices.platform_2012_1.ExceededRequestLimitFault e) {
            //FIXME need to log
            throw new PluginException("Logout Failed, exceeded the request limit.", e);
        }
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
                status.setType(RecordType.SUPPORT_CASE);
                ucase.setStatus(status);

                // Need to set status with translation
                log().debug("Set status " + ticket.getState());
                log().debug("Set details " + ticket.getDetails());

                // Update in Netsuite
                log().debug("Do update");
                //FIXME this probably isn't write... just getting it to compile...
                UpdateRequest parameters = new UpdateRequest();
                parameters.setRecord(ucase);
                UpdateResponse updateResponse = m_port.update(parameters);
                Status status2 = updateResponse.getWriteResponse().getStatus();
                
                List<StatusDetail> statusDetails = status2.getStatusDetail();
                String message = statusDetails.get(0).getMessage();
                if (!status2.isIsSuccess()) {
                    log().debug("Error on port.update "
                            +message);
                }
            }

            // else ticket ID is not in OpenNMS
            //   If open ticket for customer with status customer-performance, just update
            //   Otherwise, create
            else {

                // Company
                RecordRef co = new RecordRef();
                co.setInternalId("2557403");
                co.setType(RecordType.CUSTOMER);
                ucase.setCompany(co);

                // Status
                RecordRef status = new RecordRef();
                status.setInternalId("15");
                status.setType(RecordType.SUPPORT_CASE_STATUS);
                ucase.setStatus(status);

                // Incident date
                // Incident time
                //FIXME: This must be fixed
                //ucase.setStartDate(Calendar.getInstance());

                // Case Issue (Other - Other)
                RecordRef issue = new RecordRef();
                issue.setInternalId("12");
                issue.setType(RecordType.ISSUE);
                ucase.setIssue(issue);

                // Priority
                RecordRef priority = new RecordRef();
                priority.setInternalId("2");
                priority.setType(RecordType.SUPPORT_CASE_PRIORITY);
                ucase.setPriority(priority);

                // Title
                ucase.setTitle(ticket.getSummary());
                ucase.setCaseNumber(null); // Clear, get error if try to set

                WriteResponse wresp = null;
                log().debug("do add");
                AddRequest addRequest = new AddRequest();
                addRequest.setRecord(ucase);
                AddResponse addResponse = m_port.add(addRequest);
                Status status2 = addResponse.getWriteResponse().getStatus();
                if (status2.isIsSuccess()) {
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

        try {
            logout();
        } catch (Exception e) {
          //FIXME do something here
        }

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
            ref.setType(RecordType.SUPPORT_CASE);
            SupportCaseSearch cs = new SupportCaseSearch();
            SupportCaseSearchBasic sb = new SupportCaseSearchBasic();
            SearchStringField ssfCaseNumber = new SearchStringField();
            ssfCaseNumber.setOperator(SearchStringFieldOperator.IS);
            ssfCaseNumber.setSearchValue(caseNumber);
            sb.setCaseNumber(ssfCaseNumber);
            cs.setBasic(sb);
            SearchRequest searchRequest = new SearchRequest();
            SearchResponse searchResponse = m_port.search(searchRequest);
            Status searchStatus = searchResponse.getSearchResult().getStatus();
            if (searchStatus.isIsSuccess()) {
                RecordList recordList = searchResponse.getSearchResult().getRecordList();
                List<Record> records = recordList.getRecord();
                sc = (SupportCase) records.get(0);
                //SupportCaseStatus scs = new SupportCaseStatus();
                log().debug("Got back: " + sc.getCaseNumber());
            } else {
                log().debug("Get failed "
                        + searchStatus.getStatusDetail().get(0).getMessage());
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

            /*
             * Another possible way to be doing this search
             * Just not familiar enough with Netsuite, yet.
             */

            SearchRequest searchReq = createSearchRequest(internalId);
            SearchResponse searchResponse = m_port.search(searchReq);

            SearchResult searchResult = searchResponse.getSearchResult();
            Status searchStatus = searchResult.getStatus();
            Boolean isSuccess = searchStatus.isIsSuccess();
            if (isSuccess) {
                RecordList recordList = searchResult.getRecordList();
                List<Record> records = recordList.getRecord();
                if (records != null && records.size() == 1) {
                    sc = (SupportCase) records.get(0);
                }
            }


            /*
             * Original way (Jerome's)
             */
            RecordRef ref = new RecordRef();
            ref.setType(RecordType.SUPPORT_CASE);
            ref.setInternalId(internalId);
            GetRequest getReq = new GetRequest();
            getReq.setBaseRef(ref);
            GetResponse getResponse = m_port.get(getReq);

            ReadResponse readResponse = getResponse.getReadResponse();
            Status status = readResponse.getStatus();
            if (status != null && status.isIsSuccess()) {
                Record record = readResponse.getRecord();
                sc = (SupportCase) record;
                log().debug("Got case " + sc.getInternalId() + " "
                        + sc.getCaseNumber());
            } else {
                log().debug("Get failed "
                        + status.getStatusDetail().get(0).getMessage());
            }
            
        } catch (Exception e) {
            //FIXME: This is badness, we should do something with this exception and
            //we need a logger to log this rather than to stderr
            e.printStackTrace();
        }

        sc.setNewSolutionFromMsg(null); // Clear this. Was getting an error
        // trying to update.
        sc.setSearchSolution(null); // Ditto
        return sc;
    }

    private SearchRequest createSearchRequest(String internalId) {
        SearchRequest searchReq = new SearchRequest();
        SupportCaseSearchBasic searchRecord = new SupportCaseSearchBasic();
        SearchStringField searchString = new SearchStringField();
        searchString.setSearchValue(internalId);
        searchString.setOperator(SearchStringFieldOperator.IS);
        searchRecord.setCaseNumber(searchString);
        searchReq.setSearchRecord(searchRecord);
        return searchReq;
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

        Passport passport = createPassport();

        Status status = null;
        try {
            log().debug("stuff " +m_username +" " +m_account +" " +m_password 
                    +" " +m_role +" " +m_baseUrl);
            m_port = service.getNetSuitePort(new URL(m_baseUrl));
            log().debug("do port.login");
            LoginRequest parameters = new LoginRequest();
            parameters.setPassport(passport);
            LoginResponse login = m_port.login(parameters);
            SessionResponse sessionResponse = login.getSessionResponse();
            status = sessionResponse.getStatus();
            log().debug("login complete " + status.isIsSuccess());
            List<StatusDetail> statusDetail = status.getStatusDetail();
            StatusDetail statusDetail2 = statusDetail.get(0);
            String message = statusDetail2.getMessage();
            if (!status.isIsSuccess()) {
                log().debug("login failed: "
                        + message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private Passport createPassport() {
        Passport passport = new Passport();
        passport.setEmail(m_username);
        passport.setAccount(m_account);
        RecordRef role = new RecordRef();
        role.setInternalId(m_role);
        passport.setRole(role);
        passport.setPassword(m_password);
        return passport;
    }

    private void logout() throws 
    com.netsuite.webservices.platform_2012_1.InvalidSessionFault, 
    com.netsuite.webservices.platform_2012_1.UnexpectedErrorFault, 
    com.netsuite.webservices.platform_2012_1.ExceededRequestLimitFault {
        try {
            createPassport();
            LogoutRequest parameters = new LogoutRequest();
            m_port.logout(parameters);
        } finally {
            m_port = null;
        }
    }

    /**
     * Convenience logging.
     * 
     * @return a log4j Category for this class
     */
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
