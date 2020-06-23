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

package org.opennms.netmgt.ticketer.quickbase;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.intuit.quickbase.util.QuickBaseClient;
import com.intuit.quickbase.util.QuickBaseException;

public class QuickBaseAPITest extends TestCase {
    
    public void XXXtestCreateLead() {
        PrintWriter out = null;
        try {
            out = new PrintWriter(System.out);
            out.println("Welcome to QuickBase\n");

            QuickBaseClient qdb = createClient();
            
            
            qdb.findDbByName("TPMG Support");

            HashMap tables = (HashMap)qdb.grantedDBs(false, false, true);
            if(tables == null) {
              out.println("No tables belong to this user.");
            }
            Set tableNames = tables.keySet();
            String tableName = "";
            String tableDbid = "";
            for (Iterator it = tableNames.iterator(); it.hasNext();){
                  tableName = (String)it.next();
                  tableDbid = (String)tables.get(tableName);
                  out.println("Name: " + tableName + " DBID: " + tableDbid);
            Document schema = qdb.getSchema(tableDbid);
            
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(schema), new StreamResult(out));
                                     
            NodeList fields = schema.getElementsByTagName("field");
            out.println("The QuickBase application " + tableName + " has " + fields.getLength() + " fields.");
            out.println("The fields are listed below.");

            for (int i = 0; i < fields.getLength(); i++) {
              out.println("Field ID: " + fields.item(i).getAttributes().getNamedItem("id").getNodeValue());
              out.println("Field Type: " + fields.item(i).getAttributes().getNamedItem("type").getNodeValue());
              out.println("Field Label: " + fields.item(i).getChildNodes().item(0).getNodeValue());
            }
            
            }            
        } catch (QuickBaseException qdbe) {
            System.err.println("Exception in main "+ qdbe.toString()+ " error code: "+qdbe.getErrorCode() );
            qdbe.printStackTrace();
        } catch (Throwable e) {
            System.err.println("Exception in main "+ e.toString() );
            e.printStackTrace();

        } finally {
            if (null != out) {
                out.flush();    
                out.close();
            }
        }
        
    }
    
    
    private QuickBaseClient createClient() {
        return new QuickBaseClient("brozow@opennms.org", "password");
    }
    
    public void testCreateTicket() throws QuickBaseException, Exception {
        QuickBaseClient qdb = createClient();
        
        
        String appName = "TPMG Support";
        String dbId = qdb.findDbByName(appName);
        
        System.out.println("dbId for "+appName+" is "+dbId);
        
        HashMap record = new HashMap();
        record.put("request_type", "OpenNMS Alarm");
        record.put("summary", "ticket summary");
        record.put("description", "ticket details");
        record.put("status", "Reported");
        
        String recordId = qdb.addRecord(dbId, record);
        
        System.out.println("record Id is "+recordId);
        
    }
    
    public void testUpdateTicket() {
  
        
 
    }

}
