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

package org.opennms.netmgt.correlation.drools;

import static org.easymock.EasyMock.expect;

import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.EasyMockUtils;


public class FusionRulesTest extends CorrelationRulesTestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    
    @Test
    public void testDroolsFusion() throws Exception {
        
        anticipate(createNodeUpEvent(1));
        
        DroolsCorrelationEngine engine = findEngineByName("droolsFusion");
        
        engine.correlate(createNodeLostServiceEvent(1, "SSH"));
        Thread.sleep(4000); // Give time to execute the time-based rules.
        m_anticipatedMemorySize = 5;
        engine.getMemoryObjects().forEach(System.err::println);
        
        verify(engine);
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

    private Event createNodeLostServiceEvent(int nodeid, String serviceName) {
        return new EventBuilder(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, serviceName)
            .setNodeid(nodeid)
            .getEvent();
    }
}
