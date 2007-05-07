package org.opennms.netmgt.ticketer.centric;

import junit.framework.TestCase;
import org.aspcfs.utils.CRMConnection;
import org.aspcfs.apps.transfer.DataRecord;

public class CentricAPITest extends TestCase {
    
    public void testOpen() {
       

        // Client ID must already exist in target CRM system and is created
        // under Admin -> Configure System -> HTTP-XML API Client Manager
        int clientId = 1;

        // Establish connectivity as a client
        CRMConnection crm = new CRMConnection();
        crm.setUrl("http://localhost:8080/centric");
        crm.setId("opennms");
        crm.setCode("opennms");
        crm.setClientId(clientId);

        // Start a new transaction
        crm.setAutoCommit(false);

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
        
        
        boolean result = crm.commit();
        assertTrue(crm.getLastResponse(), result);
        
        
    }

}
