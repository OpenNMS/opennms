package org.opennms.netmgt.ticketer.centric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.aspcfs.apps.transfer.DataRecord;
import org.aspcfs.utils.CRMConnection;
import org.aspcfs.utils.XMLUtils;
import org.opennms.core.utils.ThreadCategory;
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
    
    public DataRecord getRecord() {
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
        
    Properties getProperties() {
        File home = new File(System.getProperty("opennms.home"));
        File etc = new File(home, "etc");
        File config = new File(etc, "centric.properties");


        Properties props = new Properties();

        InputStream in = null;
        try {
            in = new FileInputStream(config);
            props.load(in);
        } catch (IOException e) {
            log().error("Unable to load "+config+" ignoring.", e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return props; 

    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    public void save(Ticket ticket) {
        
    }
    
    public void update(Ticket ticket) {
        
    }

    
    public void saveOrUpdate(Ticket ticket) {
        CentricConnection crm = createConnection();
        
        ArrayList<String> returnFields = new ArrayList<String>();
        returnFields.add("id");
        crm.setTransactionMeta(returnFields);
        
        DataRecord record = getRecord();
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
