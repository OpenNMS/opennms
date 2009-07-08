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

import static org.easymock.EasyMock.expect;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.EasyMockUtils;


public class NodeParentRulesTest extends CorrelationRulesTestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    
    public void testParentNodeDown() throws Exception {
        
        //anticipate(createRootCauseEvent(1, 1));
        
        NodeService nodeService = m_mocks.createMock(NodeService.class);
        
        expect(nodeService.getParentNode(1L)).andReturn(null).atLeastOnce();
        
        m_mocks.replayAll();
        
        DroolsCorrelationEngine engine = findEngineByName("nodeParentRules");
        engine.setGlobal("nodeService", nodeService);
        
        engine.correlate(createNodeDownEvent(1));

        // event + root cause
        m_anticipatedMemorySize = 2;
        
        m_mocks.verifyAll();
        verify(engine);
        
        anticipate(createRootCauseResolvedEvent(1, 1));
        
    }
    
    private Event createRootCauseResolvedEvent(int symptom, int cause) {
        return new EventBuilder(createNodeEvent("rootCauseResolved", cause)).getEvent();

    }

    // Currently unused
//    private Event createRootCauseEvent(int symptom, int cause) {
//        return new EventBuilder(createNodeEvent("rootCauseEvent", cause)).getEvent();
//    }


    public Event createNodeDownEvent(int nodeid) {
        return createNodeEvent(EventConstants.NODE_DOWN_EVENT_UEI, nodeid);
    }
    
    public Event createNodeUpEvent(int nodeid) {
        return createNodeEvent(EventConstants.NODE_UP_EVENT_UEI, nodeid);
    }

    private Event createNodeEvent(String uei, int nodeid) {
        return new EventBuilder(uei, "test")
            .setNodeid(nodeid)
            .getEvent();
    }
    



}