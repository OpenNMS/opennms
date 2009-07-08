/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.correlation.drools;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class CorrelationRulesTestCase extends AbstractDependencyInjectionSpringContextTests {

    private MockEventIpcManager m_eventIpcMgr;
    protected EventAnticipator m_anticipator;
    protected Integer m_anticipatedMemorySize = 0;
    private CorrelationEngineRegistrar m_correlator;
    
    protected CorrelationRulesTestCase() {
        ConfigurationTestUtils.setRelativeHomeDirectory("src/test/opennms-home");
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:test-context.xml",
                "classpath:META-INF/opennms/correlation-engine.xml"
        };
    }
    
    public void setCorrelationEngineRegistrar(CorrelationEngineRegistrar correlator) {
        m_correlator = correlator;
    }

    public void setEventIpcMgr(MockEventIpcManager eventIpcManager) {
        m_eventIpcMgr = eventIpcManager;
        m_anticipator = m_eventIpcMgr.getEventAnticipator();
    }

    protected void verify(DroolsCorrelationEngine engine) {
    	m_anticipator.verifyAnticipated(0, 0, 0, 0, 0);
        if (m_anticipatedMemorySize != null) {
            assertEquals("Unexpected number of objects in working memory: "+engine.getMemoryObjects(), m_anticipatedMemorySize.intValue(), engine.getMemorySize());
        }
    }

    protected DroolsCorrelationEngine findEngineByName(String engineName) {
        return (DroolsCorrelationEngine) m_correlator.findEngineByName(engineName);
    }

    protected void anticipate(Event event) {
        m_anticipator.anticipateEvent(event);
    }

    protected Event createRemoteNodeLostServiceEvent(int nodeId, String ipAddr, String svcName, int locationMonitor) {
    	return createEvent(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI, nodeId, ipAddr, svcName, locationMonitor);
    }

    protected Event createRemoteNodeRegainedServiceEvent(int nodeId, String ipAddr, String svcName, int locationMonitor) {
    	return createEvent(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI, nodeId, ipAddr, svcName, locationMonitor);
    }

    protected Event createEvent(String uei, int nodeId, String ipAddr, String svcName, int locationMonitor) {
    	return new EventBuilder(uei, "test")
            .setNodeid(nodeId)
        	.setInterface(ipAddr)
        	.setService(svcName)
        	.addParam(EventConstants.PARM_LOCATION_MONITOR_ID, locationMonitor)
            .getEvent();
    }

    protected Event createServiceEvent(String uei, int nodeId, String ipAddr, String svcName) {
        return new EventBuilder(uei, "test")
            .setNodeid(nodeId)
            .setInterface("192.168.1.1")
            .setService("HTTP")
            .getEvent();
    }

}
