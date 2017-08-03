/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ticketer.centric;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.aspcfs.utils.CRMConnection;
import org.aspcfs.apps.transfer.DataRecord;

public class CentricAPITest extends TestCase {
    
    public void testCreateLead() {
       

        CRMConnection crm = createConnection();

        DataRecord contact = new DataRecord();
        contact.setName("contact");
        contact.setAction(DataRecord.INSERT);
        contact.setShareKey(true);
        contact.addField("nameFirst", "Jeff");
        contact.addField("nameLast", "Gehlbach");
        contact.addField("company", "Malta");
        contact.addField("title", "Knight of Order");
        contact.addField("source", "Always");
        contact.addField("isLead", "true");
        contact.addField("accessType", 2);
        contact.addField("leadStatus", 1);
        contact.addField("enteredBy", 0);
        contact.addField("modifiedBy", 0);
        assertTrue(crm.save(contact));

        // Transform the email
        DataRecord email = new DataRecord();
        email.setName("contactEmailAddress");
        email.setAction(DataRecord.INSERT);
        email.addField("email", "jeff@example.com");
        email.addField("contactId", "$C{contact.id}");
        email.addField("type", 1);
        email.addField("enteredBy", 0);
        email.addField("modifiedBy", 0);
        assertTrue(crm.save(email));
        
        
        commit(crm);
        
        
    }

    private void commit(CRMConnection crm) {
        boolean result = crm.commit();
        assertTrue(crm.getLastResponse(), result);
    }

    private CRMConnection createConnection() {
        // Client ID must already exist in target CRM system and is created
        // under Admin -> Configure System -> HTTP-XML API Client Manager
        int clientId = 1;

        // Establish connectivity as a client
        CRMConnection crm = new CRMConnection();
        crm.setUrl("http://localhost:8080/centric");
        crm.setId("localhost");
        crm.setCode("opennms");
        crm.setClientId(clientId);

        // Start a new transaction
        crm.setAutoCommit(false);
        return crm;
    }
    
    public static class RecordLocator {
        private String m_id;

        public String getId() {
            return m_id;
        }

        public void setId(String id) {
            m_id = id;
        }
        
        public int getIdAsInt() {
            return Integer.parseInt(m_id);
        }
        

    }
    
    
	public void testCreateTicket() {
        
        CRMConnection crm = createConnection();
        
        ArrayList<String> sucky = new ArrayList<>();
        sucky.add("id");
        crm.setTransactionMeta(sucky);
        
        
        DataRecord email = new DataRecord();
        email.setName("ticket");
        email.setAction(DataRecord.INSERT);
        email.setShareKey(true);
        //email.addField("orgId", 1);
        //email.addField("contactId", 1);
        email.addField("problem", "can't get there from here");
        email.addField("enteredBy", 0);
        email.addField("modifiedBy", 0);

        
        crm.save(email);
        
        commit(crm);
        
        assertEquals(1, crm.getRecordCount());
        
        @SuppressWarnings("unchecked")
        List<RecordLocator> results = crm.getRecords(RecordLocator.class.getName());
        
        assertEquals(1, results.size());
        
        RecordLocator record = results.get(0);
        assertTrue(0 < record.getIdAsInt());
        

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
    
    public void testUpdateTicket() {
  
       // Client ID must already exist in target CRM system and is created
        // under Admin -> Configure System -> HTTP-XML API Client Manager
        int clientId = 1;
        
        // Establish connectivity as a client
        CRMConnection crm = new CRMConnection();
        crm.setUrl("http://localhost:8080/centric");
        crm.setId("localhost");
        crm.setCode("opennms");
        crm.setClientId(clientId);
        
        
        ArrayList<String> returnFields = new ArrayList<>();
        returnFields.add("id");
        returnFields.add("modified");
        crm.setTransactionMeta(returnFields);

        DataRecord query = new DataRecord();
        query.setAction(DataRecord.SELECT);
        query.setName("ticketList");
        query.addField("id", 91);
        
        crm.load(query);
        
        String modified = crm.getResponseValue("modified");
        
        // Start a new transaction
        crm.setAutoCommit(false);
        
        
        
        
        DataRecord ticket = new DataRecord();
        ticket.setName("ticket");
        ticket.setAction(DataRecord.UPDATE);
        //email.addField("orgId", 1);
        //email.addField("contactId", 1);
        ticket.addField("id", 91);
        ticket.addField("problem", "can't get there from here");
        ticket.addField("enteredBy", 0);
        ticket.addField("modifiedBy", 0);
        ticket.addField("stateId", 6);
        ticket.addField("severityCode", 2);
        ticket.addField("modified", modified);
        
        crm.save(ticket);
        
        boolean result = crm.commit();
        assertTrue(crm.getLastResponse(), result);
        
 
    }

}
