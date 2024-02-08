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

import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;


public class FusionRulesIT extends CorrelationRulesTestCase {

    @Test
    public void testDroolsFusion() throws Exception {

        anticipate(createNodeUpEvent(1));
        DroolsCorrelationEngine engine = findEngineByName("droolsFusion");
        // The engine should run other rules after reload.
        engine.correlate(createNodeLostServiceEvent(1, "SSH"));
        Thread.sleep(4000); // Give time to execute the time-based rules.
        m_anticipatedMemorySize = 5;
        engine.getKieSessionObjects().forEach(System.err::println);
        verify(engine);
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
