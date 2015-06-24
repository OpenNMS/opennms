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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.Ticket.State;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.intuit.quickbase.util.QuickBaseClient;
import com.intuit.quickbase.util.QuickBaseException;

/**
 * OpenNMS Trouble Ticket Plugin API implementation for Intuit's QuickBase Trouble Ticketing.
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class QuickBaseTicketerPlugin implements Plugin {
    
    Properties m_properties;
    
	public Ticket get(String ticketId) {
	    try {
	        Properties props = getProperties();
	        MyQuickBaseClient qdb = createClient(getUserName(props), getPassword(props), getUrl(props));
	    
	        String dbId = qdb.findDbByName(getApplicationName(props));
	        
	        HashMap<String, String> record = qdb.getRecordInfo(dbId, ticketId);
	        
	        Ticket ticket = new Ticket();
	        ticket.setId(ticketId);
	        ticket.setModificationTimestamp(record.get(getModificationTimeStampFile(props)));
	        ticket.setSummary(record.get(getSummaryField(props)));
	        ticket.setDetails(record.get(getDetailsField(props)));
	        ticket.setState(getTicketStateValue(record.get(getStateField(props)), props));
	        
	        return ticket;
        
	    } catch (Throwable e) {
	        throw new DataRetrievalFailureException("Failed to commit QuickBase transaction: "+e.getMessage(), e);
	    }
    
	}

    private MyQuickBaseClient createClient(String username, String passwd, String url) {
	    return new MyQuickBaseClient(username, passwd, url);
	}
	
	private Properties getProperties() {
	    File home = new File(System.getProperty("opennms.home"));
	    File etc = new File(home, "etc");
	    File config = new File(etc, "quickbase.properties");
	    
	    
	    Properties props = new Properties(System.getProperties());
	    
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

	/**
     * Convenience logging.
     * @return a log4j Category for this class
     */
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void saveOrUpdate(Ticket ticket) {

	    try {
	        
	       Properties props = getProperties();
	       
	       QuickBaseClient qdb = createClient(getUserName(props), getPassword(props), getUrl(props));
	       
           String dbId = qdb.findDbByName(getApplicationName(props));
	       
           HashMap<String, String> record = new HashMap<String, String>();
           
           record.put(getSummaryField(props), ticket.getSummary());
           record.put(getDetailsField(props), ticket.getDetails());
           record.put(getStateField(props), getQuickBaseStateValue(ticket.getState(), props));
           
	       if (ticket.getId() == null) {
	           addAdditionCreationFields(record, props);
	           String recordId = qdb.addRecord(dbId, record);
	           ticket.setId(recordId);
	       } else {
	           Ticket oldTicket = get(ticket.getId());
	           if (ticket.getModificationTimestamp().equals(oldTicket.getModificationTimestamp())) {
	               qdb.editRecord(dbId, record, ticket.getId());	               
	           } else {
	               throw new OptimisticLockingFailureException("Ticket has been updated while this ticket was in memory! Reload and try again!");
	           }

	       }
	       
	    } catch (Throwable e) {
            throw new DataRetrievalFailureException("Failed to commit QuickBase transaction: "+e.getMessage(), e);
	    }
	        
	}

    private String getQuickBaseStateValue(State state, Properties props) {
        return getRequiredProperty(props, "statemap.ticket."+state.name());
    }
    
    private State getTicketStateValue(String status, Properties props) {
        return State.valueOf(getRequiredProperty(props, "statemap.quickbase."+status));
    }

    private String getRequiredProperty(Properties props, String propName) {
        assertPropertyDefined(props, propName);
        return props.getProperty(propName);
    }

    private void assertPropertyDefined(Properties props, String propName) {
        Assert.notNull(props.getProperty(propName), propName + " is not defined in quickbase.properties");
    }
    
    private String getIdField(Properties props) {
        return getRequiredProperty(props, "ticket.id");
    }
    
    private String getModificationTimeStampFile(Properties props) {
        return getRequiredProperty(props, "ticket.modificationTimestamp");
    }

    private String getStateField(Properties props) {
        return getRequiredProperty(props, "ticket.state");
    }

    private String getDetailsField(Properties props) {
        return getRequiredProperty(props, "ticket.details");
    }

    private String getSummaryField(Properties props) {
        return getRequiredProperty(props, "ticket.summary");
    }
    
    private String getApplicationName(Properties props) {
        return getRequiredProperty(props, "quickbase.appname");
    }
    
    private String getUserName(Properties props) {
        return getRequiredProperty(props, "quickbase.username");
    }
    
    private String getPassword(Properties props) {
        return getRequiredProperty(props, "quickbase.password");
    }
    
    private String getUrl(Properties props) {
        return getRequiredProperty(props, "quickbase.url");
    }


    private void addAdditionCreationFields(HashMap<String, String> record, Properties props) {
        final String prefix = "quickbase.create.";
        Enumeration keys = props.propertyNames();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith(prefix)) {
                String field = key.substring(prefix.length());
                record.put(field, props.getProperty(key));
            }
        }
    }
    
    private static class MyQuickBaseClient extends QuickBaseClient {

        public MyQuickBaseClient(String username, String password, String url) {
            super(username, password, url);
        }
        
        public HashMap<String,String> getRecordInfo(String dbid, String rid) throws QuickBaseException, Exception {
            Document qdbRequest = newXmlDocument();
            addRequestParameter(qdbRequest, "rid", rid);
            
            Document qdbResponse = postApiXml(dbid, "API_GetRecordInfo", qdbRequest);
            
            NodeList records = getNodeList(qdbResponse, "field");
            if (records == null) return null;
            
            HashMap<String,String> record = new HashMap<String,String>(0);
            for (int recordCounter = 0; recordCounter < records.getLength(); recordCounter++){
                Element field = (Element)records.item(recordCounter);
                String id = field.getElementsByTagName("fid").item(0).getChildNodes().item(0).getNodeValue();
                Node valueNode = field.getElementsByTagName("value").item(0).getChildNodes().item(0);
                String value = (valueNode == null ? null : valueNode.getNodeValue());
                record.put(id, value);
            }
            
            return record;
        }
        
        private NodeList getNodeList(Document xmlDoc, String select){
            String currentNodeName;
            Element el = xmlDoc.getDocumentElement();
            NodeList nl = null;
            StringTokenizer st = new StringTokenizer(select, "/");
            while (st.hasMoreTokens()){
              currentNodeName = st.nextToken();
              if (el ==null){return null;}
              nl = el.getElementsByTagName(currentNodeName);
              el = (Element) nl.item(0);
              }
            return nl;
          }
        
    }
    
 }
