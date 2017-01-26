/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketd;

import java.util.Map;
import java.util.Properties;

import org.opennms.api.integration.ticketing.*;
import org.opennms.netmgt.model.OnmsAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.drools.compiler.compiler.PackageBuilderConfiguration;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

/**
 * OpenNMS Trouble Ticket API implementation.
 *
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 * @version $Id: $
 */
public class DroolsTicketerServiceLayer extends DefaultTicketerServiceLayer {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsTicketerServiceLayer.class);
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
    
    @Override
    public void reloadTicketer() {
        LOG.debug("reloadTicketer: Reloading ticketer");
        m_knowledgeBase = createKnowledgeBase();
    }
    
    private KnowledgeBase createKnowledgeBase() {
        LOG.debug("createKnowledgeBase: Creating Drools KnowledgeBase");
        final Properties props = new Properties();
        props.setProperty("drools.dialect.java.compiler.lnglevel", "1.6");

        final PackageBuilderConfiguration conf = new PackageBuilderConfiguration(props);
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(conf);
        
        // Use the rules file defined in the configuration file
        // We will not throw an exception if the rules failed to be parsed
        builder.add(ResourceFactory.newFileResource(m_configDao.getRulesFile()), ResourceType.DRL);
        if( builder.hasErrors() ) {
            LOG.error("Failed to create Drools KnowledgeBase: {}", builder.getErrors().toString());
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
	 * @param attributes
     * @return OpenNMS Ticket processed by Drools logic.
	 */
    @Override
    protected Ticket createTicketFromAlarm(OnmsAlarm alarm, Map<String, String> attributes) {
        LOG.debug("Initializing ticket from alarm: {}", alarm);

        // Call superclass method if the knowledge-base was not properly created.
        if( m_knowledgeBase == null ) {
            LOG.error("KnowledgeBase is NULL, creating basic ticket form alarm.");
            return super.createTicketFromAlarm(alarm, attributes);
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

        LOG.debug("Successfully initialized ticket: {} from alarm: {}.", ticket, alarm);
        return ticket;
    }
}
