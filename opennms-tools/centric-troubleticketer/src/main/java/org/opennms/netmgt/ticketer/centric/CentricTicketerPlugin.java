/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketer.centric;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.aspcfs.apps.transfer.DataRecord;
import org.aspcfs.utils.CRMConnection;
import org.aspcfs.utils.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.api.integration.ticketing.Ticket.State;

/**
 * OpenNMS Trouble Ticket Plugin API implementation for CentricCRM (c) Darkhorse Ventures.
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class CentricTicketerPlugin implements Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(CentricTicketerPlugin.class);
    
    /**
     * This class extends Centric Class that is responsible for transferring data
     * to/from the Centric server via their HTTP-XML API.
     * 
     * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
     * @author <a href="mailto:david@opennms.org">David Hustace</a>
     *
     */
    public static class CentricConnection extends CRMConnection {

    	/**
    	 * Convenience method added to retrieve error message embedded in the XML
    	 * packet returned by the CentricCRM API.
    	 * 
    	 * @return <code>java.lang.String</code> if an error message exists in the server response.
    	 */
        public String getErrorText() throws CentricPluginException {
            XMLUtils xml;
            try {
                String responseXML = getLastResponse();
                if (responseXML == null) {
                    return "Connection failed.  See output.log for details";
                }
                xml = new XMLUtils(responseXML);
                Element response = xml.getFirstChild("response");
                Element errorText = XMLUtils.getFirstChild(response, "errorText");
                return errorText.getTextContent();
            } catch (Throwable e) {
            	throw new CentricPluginException(e);
            }
        }
        
        /**
         * Wrapper class used to nicely handle Centric API exceptions
         * 
         * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
         * @author <a href="mailto:david@opennms.org">David Hustace</a>
         *
         */
        class CentricPluginException extends RuntimeException {

			private static final long serialVersionUID = -2279922257910422937L;
			
			public CentricPluginException(Throwable e) {
				super(e);
			}
        	
        }
    }
    
    
    /**
     * Implementation of TicketerPlugin API call to retrieve a CentricCRM trouble ticket.
     * @return an OpenNMS 
     */
    public Ticket get(String ticketId) {
        CentricConnection crm = createConnection();
        
        ArrayList<String> returnFields = new ArrayList<String>();
        returnFields.add("id");
        returnFields.add("modified");
        returnFields.add("problem");
        returnFields.add("comment");
        returnFields.add("stateId");
        crm.setTransactionMeta(returnFields);
        
        DataRecord query = new DataRecord();
        query.setName("ticketList");
        query.setAction(DataRecord.SELECT);
        query.addField("id", ticketId);

        boolean success = crm.load(query);
        if (!success) {
            throw new DataRetrievalFailureException(crm.getLastResponse());
        }
        
        Ticket ticket = new Ticket();
        ticket.setId(crm.getResponseValue("id"));
        ticket.setModificationTimestamp(crm.getResponseValue("modified"));
        ticket.setSummary(crm.getResponseValue("problem"));
        ticket.setDetails(crm.getResponseValue("comment"));
        ticket.setState(getStateFromId(crm.getResponseValue("stateId")));
                
        return ticket;
        
    }
    
    /**
     * Convenience method of determining a "close" state of a ticket.
     * @param newState
     * @return true if canceled or closed state
     */
    private boolean isClosingState(State newState) {
        switch(newState) {
        case CANCELLED:
        case CLOSED:
            return true;
        case OPEN:
        default:    
            return false;
        }
        
    }


    /**
     * Convenience method for converting a string representation of
     * the OpenNMS enumerated ticket states.
     * 
     * @param stateIdString
     * @return the converted <code>org.opennms.api.integration.ticketing.Ticket.State</code> 
     */
    private State getStateFromId(String stateIdString) {
    	if (stateIdString == null) {
    		return State.OPEN;
    	}
        int stateId = Integer.parseInt(stateIdString);
        switch(stateId) {
        case 1:
            return State.OPEN;
        case 2:
            return State.OPEN;
        case 3:
            return State.OPEN;
        case 4:
            return State.OPEN;
        case 5:
            return State.CLOSED;
        case 6:
            return State.CANCELLED;
        case 7:
            return State.CANCELLED;
        default:
            return State.OPEN;
                
        }
    }

    /**
     * Helper method for creating a CentricCRM DataRecord from properties
     * defined in the centric.properties file.
     * 
     * @return a populated <code>org.aspcfs.apps.transfer.DataRecord</code>
     */
    private DataRecord createDataRecord() {
        DataRecord record = new DataRecord();
        
        Properties props = getProperties();
        
        
        for(Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String)entry.getKey();
            String val = (String)entry.getValue();

            if (!key.startsWith("connection.")) {
                record.addField(key, val);
            }
                
        }
                
        return record;
    }   

    /**
     * Retrieves the properties defined in the centric.properties file.
     * 
     * @return a <code>java.util.Properties object containing centric plugin defined properties
     */
    private Properties getProperties() {
        File home = new File(System.getProperty("opennms.home"));
        File etc = new File(home, "etc");
        File config = new File(etc, "centric.properties");


        Properties props = new Properties();

        InputStream in = null;
        try {
            in = new FileInputStream(config);
            props.load(in);
        } catch (IOException e) {
            LOG.error("Unable to load {} ignoring.", config, e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return props; 

    }

    /*
     * (non-Javadoc)
     * @see org.opennms.api.integration.ticketing.Plugin#saveOrUpdate(org.opennms.api.integration.ticketing.Ticket)
     */
    public void saveOrUpdate(Ticket ticket) {
        CentricConnection crm = createConnection();
        
        ArrayList<String> returnFields = new ArrayList<String>();
        returnFields.add("id");
        crm.setTransactionMeta(returnFields);
        
        DataRecord record = createDataRecord();
        record.setName("ticket");
        if (ticket.getId() == null) {
            record.setAction(DataRecord.INSERT);
        } else {
            record.setAction(DataRecord.UPDATE);
            record.addField("id", ticket.getId());
            record.addField("modified", ticket.getModificationTimestamp());
        }
        record.addField("problem", ticket.getSummary());
        record.addField("comment", ticket.getDetails());
        record.addField("stateId", getStateId(ticket.getState()));
        record.addField("closeNow", isClosingState(ticket.getState()));
        
        crm.save(record);
        
        boolean success = crm.commit();
        
        if (!success) {
            throw new DataRetrievalFailureException("Failed to commit Centric transaction: "+crm.getErrorText());
        }
           
        
        Assert.isTrue(1 == crm.getRecordCount(), "Unexpected record count from CRM");
        
        String id = crm.getResponseValue("id");
      
        ticket.setId(id);
/*
        <map class="org.aspcfs.modules.troubletickets.base.Ticket" id="ticket">
        <property alias="guid">id</property>
        <property lookup="account">orgId</property>
        <property lookup="contact">contactId</property>
        <property>problem</property>
        <property>entered</property>
        <property lookup="user">enteredBy</property>
        <property>modified</property>
        <property lookup="user">modifiedBy</property>
        <property>closed</property>
        <property lookup="ticketPriority">priorityCode</property>
        <property>levelCode</property>
        <property lookup="lookupDepartment">departmentCode</property>
        <property lookup="lookupTicketSource">sourceCode</property>
        <property lookup="ticketCategory">catCode</property>
        <property lookup="ticketCategory">subCat1</property>
        <property lookup="ticketCategory">subCat2</property>
        <property lookup="ticketCategory">subCat3</property>
        <property lookup="user">assignedTo</property>
        <property>comment</property>
        <property>solution</property>
        <property lookup="ticketSeverity">severityCode</property>
        <!-- REMOVE: critical -->
        <!-- REMOVE: notified -->
        <!-- REMOVE: custom_data -->    
        <property>location</property>
        <property>assignedDate</property>
        <property>estimatedResolutionDate</property>
        <property>resolutionDate</property>
        <property>cause</property>
        <property>contractId</property>
        <property>assetId</property>
        <property>productId</property>
        <property>customerProductId</property>
        <property>expectation</property>
        <property>projectTicketCount</property>
        <property>estimatedResolutionDateTimeZone</property>
        <property>assignedDateTimeZone</property>
        <property>resolutionDateTimeZone</property>
        <property>statusId</property>
        <property>trashedDate</property>
        <property>userGroupId</property>
        <property>causeId</property>
        <property>resolutionId</property>
        <property>defectId</property>
        <property>escalationLevel</property>
        <property>resolvable</property>
        <property>resolvedBy</property>
        <property>resolvedByDeptCode</property>
        <property>stateId</property>
        <property>siteId</property>
      </map>
      
*/

    }

/*    
    private String getModifiedTimestamp(String id) {
        CentricConnection crm = createConnection();
        
        ArrayList<String> returnFields = new ArrayList<String>();
        returnFields.add("id");
        returnFields.add("modified");
        crm.setTransactionMeta(returnFields);

        DataRecord query = new DataRecord();
        query.setAction(DataRecord.SELECT);
        query.setName("ticketList");
        query.addField("id", 91);
        
        crm.load(query);
        
        return crm.getResponseValue("modified");

    }
    
*/

    
    /**
     * Convenience method for converting OpenNMS Ticket.State enum
     * to an int representation compatible with CentricCRM.
     * 
     * TODO: This needs to be configurable with the ability of the user
     * to define.
     */
    private int getStateId(State state) {
        switch(state) {
        case OPEN:
            return 2;
        case CANCELLED:
            return 6;
        case CLOSED:
            return 5;
        default:
            return 2;
        }
    }

    /**
     * Creates connection to CentricCRM server using CentricCRM HTTP-XML API
     * @return a connection to the configured CentricCRM server. 
     */
    private CentricConnection createConnection() {
        // Client ID must already exist in target CRM system and is created
        // under Admin -> Configure System -> HTTP-XML API Client Manager
        
        Properties props = getProperties();

        // Establish connectivity as a client
        CentricConnection crm = new CentricConnection();
        crm.setUrl(props.getProperty("connection.url"));
        crm.setId(props.getProperty("connection.id"));
        crm.setCode(props.getProperty("connection.code"));
        crm.setClientId(props.getProperty("connection.clientId"));

        // Start a new transaction
        crm.setAutoCommit(false);
        return crm;
    }

}
