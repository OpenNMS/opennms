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

package org.opennms.netmgt.ticketd;

import org.opennms.api.integration.ticketing.*;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.core.utils.ThreadCategory;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * OpenNMS Trouble Ticket API implementation.
 *
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 * @version $Id: $
 */
public class DroolsTicketerServiceLayer extends DefaultTicketerServiceLayer {
    DroolsTicketerConfigDao m_configDao;
    KnowledgeBase m_knowledgeBase;
	
	/**
	 * <p>Constructor for DroolsTicketerServiceLayer.</p>
	 */
    public DroolsTicketerServiceLayer() {
    	m_configDao = new DroolsTicketerConfigDao();
        m_knowledgeBase = createKnowledgeBase();
    }
    
    public DroolsTicketerServiceLayer(DroolsTicketerConfigDao configDao) {
    	m_configDao = configDao;
        m_knowledgeBase = createKnowledgeBase();
    }
    
    public DroolsTicketerConfigDao getConfigDao() {
    	return m_configDao;
    }
    
    public void reloadTicketer() {
        log().debug("reloadTicketer: Reloading ticketer");
        m_knowledgeBase = createKnowledgeBase();
    }
    
    private KnowledgeBase createKnowledgeBase() {
        log().debug("createKnowledgeBase: Creating Drools KnowledgeBase");
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        // Use the rules file defined in the configuration file
        // We will not throw an exception if the rules failed to be parsed
        builder.add(ResourceFactory.newFileResource(m_configDao.getRulesFile()), ResourceType.DRL);
        if( builder.hasErrors() ) {
            log().error("Failed to create Drools KnowledgeBase: " + builder.getErrors().toString());
            return null;
        }
        
        KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
        knowledgeBase.addKnowledgePackages(builder.getKnowledgePackages());
        return knowledgeBase;
    }
    
	/**
	 * Called from API implemented method after successful retrieval of Alarm.
	 * 
	 * @param alarm OpenNMS Model class alarm
	 * @return OpenNMS Ticket processed by Drools logic.
	 */
    protected Ticket createTicketFromAlarm(OnmsAlarm alarm) {
        log().debug("createTicketFromAlarm: Processing ticket.");
        
        // Call superclass method if the knowledge-base was not properly created.
        if( m_knowledgeBase == null ) {
            log().error("KnowledgeBase is NULL, creating basic ticket form alarm.");
            return super.createTicketFromAlarm(alarm);
        }
        
        Ticket ticket = new Ticket();
        StatefulKnowledgeSession session = m_knowledgeBase.newStatefulKnowledgeSession();
        try {
            // Pass the ticket as a global - the logic will fill the appropriate fields
            session.setGlobal("ticket", ticket);
            // Pass the alarm and the node objects
            session.insert(alarm);
            session.insert(alarm.getNode());
            session.fireAllRules();
        } finally {
            session.dispose();
        }
        
        log().debug("createTicketFromAlarm: Succesfully processed ticket.");
        return ticket;
    }
    
    /**
    * Convenience logging.
    * @return a log4j Category for this class
    */
    ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
