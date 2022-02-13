/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.drools;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;


public class NodeParentRulesTest extends CorrelationRulesTestCase {
    @Test
    public void testParentNodeDown() throws Exception {
        
        //anticipate(createRootCauseEvent(1, 1));
        
        NodeService nodeService = mock(NodeService.class);
        
        when(nodeService.getParentNode(1L)).thenReturn(null);
        
        DroolsCorrelationEngine engine = findEngineByName("nodeParentRules");
        engine.setGlobal("nodeService", nodeService);
        
        engine.correlate(createNodeDownEvent(1));

        // event + root cause
        m_anticipatedMemorySize = 2;

        Mockito.verify(nodeService, atLeastOnce()).getParentNode(1L);
        verify(engine);
        
        anticipate(createRootCauseResolvedEvent(1, 1));
        
    }
    
    private Event createRootCauseResolvedEvent(int symptom, int cause) {
        return new EventBuilder(createNodeEvent("rootCauseResolved", cause)).getEvent();
    }

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