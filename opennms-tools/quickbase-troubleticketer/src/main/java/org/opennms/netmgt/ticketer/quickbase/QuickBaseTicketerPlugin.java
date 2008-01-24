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
package org.opennms.netmgt.ticketer.quickbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ticketd.Ticket;
import org.opennms.netmgt.ticketd.TicketerPlugin;
import org.opennms.netmgt.ticketd.Ticket.State;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

import com.intuit.quickbase.util.QuickBaseClient;
import com.intuit.quickbase.util.QuickBaseException;

/**
 * OpenNMS Trouble Ticket Plugin API implementation for Intuit's QuickBase Trouble Ticketing.
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class QuickBaseTicketerPlugin implements TicketerPlugin {
    
	public Ticket get(String ticketId) {
		return null;
	}

	private QuickBaseClient createClient() {
	    return new QuickBaseClient("brozow@opennms.org", "password");
	}
	
	private Properties getProperties() {
	    File home = new File(System.getProperty("opennms.home"));
	    File etc = new File(home, "etc");
	    File config = new File(etc, "quickbase.properties");
	    
	    
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

	/**
     * Convenience logging.
     * @return a log4j Category for this class
     */
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void saveOrUpdate(Ticket ticket) {

	    try {
	        
	       Properties props = getProperties();
	       
	       QuickBaseClient qdb = createClient();
	       
	       String appName = "TPMG Support";
	       String dbId = qdb.findDbByName(appName);
	       
           HashMap<String, String> record = new HashMap<String, String>();
           
           record.put(getSummaryField(props), ticket.getSummary());
           record.put(getDetailsField(props), ticket.getDetails());
           record.put(getStateField(props), getQuickBaseStateValue(ticket.getState(), props));
           
	       if (ticket.getId() == null) {
	           addAdditionCreationFields(record, props);
	           String recordId = qdb.addRecord(dbId, record);
	           ticket.setId(recordId);
	       } else {
	           
	           
	           
	           qdb.editRecord(dbId, record, ticket.getId());
	       }
	       
	    } catch (Exception e) {
            throw new DataRetrievalFailureException("Failed to commit QuickBase transaction: "+e.getMessage(), e);
	    }
	        
	}

    private String getQuickBaseStateValue(State state, Properties props) {
        return props.getProperty("statemap.ticket."+state.name());
    }

    private String getStateField(Properties props) {
        return props.getProperty("ticket.state");
    }

    private String getDetailsField(Properties props) {
        return props.getProperty("ticket.details");
    }

    private String getSummaryField(Properties props) {
        return props.getProperty("ticket.summary");
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
    
 }
