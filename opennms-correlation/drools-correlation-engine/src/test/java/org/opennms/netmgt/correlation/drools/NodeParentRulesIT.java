/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.correlation.drools;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;


public class NodeParentRulesIT extends CorrelationRulesTestCase {
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