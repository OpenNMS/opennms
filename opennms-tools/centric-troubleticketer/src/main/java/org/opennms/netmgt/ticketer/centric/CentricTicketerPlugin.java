package org.opennms.netmgt.ticketer.centric;

import java.util.ArrayList;

import org.aspcfs.apps.transfer.DataRecord;
import org.aspcfs.utils.CRMConnection;
import org.aspcfs.utils.XMLUtils;
import org.opennms.netmgt.ticketd.Ticket;
import org.opennms.netmgt.ticketd.TicketerPlugin;
import org.opennms.netmgt.ticketd.Ticket.State;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

public class CentricTicketerPlugin implements TicketerPlugin {
    
    public static class CentricConnection extends CRMConnection {
        
        public String getErrorText() {
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
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public Ticket get(String ticketId) {
        CentricConnection crm = createConnection();
        
        ArrayList<String> returnFields = new ArrayList<String>();
        returnFields.add("id");
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
        ticket.setSummary(crm.getResponseValue("problem"));
        ticket.setDetails(crm.getResponseValue("comment"));
        ticket.setState(getStateFromId(crm.getResponseValue("stateId")));
                
        return ticket;
        
    }

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

    public void saveOrUpdate(Ticket ticket) {
        CentricConnection crm = createConnection();
        
        ArrayList<String> returnFields = new ArrayList<String>();
        returnFields.add("id");
        crm.setTransactionMeta(returnFields);
        
        
        DataRecord record = new DataRecord();
        record.setName("ticket");
        if (ticket.getId() == null) {
            record.setAction(DataRecord.INSERT);
            record.addField("orgId", 0);
            record.addField("contactId", 1);
            record.addField("enteredBy", 0);
            record.addField("modifiedBy", 0);
        } else {
            record.setAction(DataRecord.UPDATE);
            record.addField("id", ticket.getId());
            record.addField("enteredBy", 0);
            record.addField("modifiedBy", 0);
        }
        record.addField("problem", ticket.getSummary());
        record.addField("comment", ticket.getDetails());
        record.addField("stateId", getStateId(ticket.getState()));
        
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

    private CentricConnection createConnection() {
        // Client ID must already exist in target CRM system and is created
        // under Admin -> Configure System -> HTTP-XML API Client Manager
        int clientId = 1;

        // Establish connectivity as a client
        CentricConnection crm = new CentricConnection();
        crm.setUrl("http://localhost:8080/centric");
        crm.setId("localhost");
        crm.setCode("opennms");
        crm.setClientId(clientId);

        // Start a new transaction
        crm.setAutoCommit(false);
        return crm;
    }
  

}
