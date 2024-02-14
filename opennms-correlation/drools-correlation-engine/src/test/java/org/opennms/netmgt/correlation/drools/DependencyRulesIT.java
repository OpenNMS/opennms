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

import static org.opennms.core.utils.InetAddressUtils.addr;

import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;


public class DependencyRulesIT extends CorrelationRulesTestCase {
    @Test
    public void testInitialize() throws Exception {
        
        anticipate( createInitializedEvent( 1, 1 ) );
        
    	EventBuilder bldr = new EventBuilder( "impactedService", "Drools" );
    	bldr.setNodeid( 1 );
    	bldr.setInterface( addr( "10.1.1.1" ) );
    	bldr.setService( "HTTP" );
    	bldr.addParam("CAUSE", 17 );

    	anticipate( bldr.getEvent() );
    	
    	bldr = new EventBuilder( "impactedApplication", "Drools" );
    	bldr.addParam("APP", "e-commerce" );
    	bldr.addParam("CAUSE", 17 );
    	
    	anticipate( bldr.getEvent() );
        
        DroolsCorrelationEngine engine = findEngineByName("dependencyRules");

        Event event = createNodeLostServiceEvent( 1, "10.1.1.1", "ICMP" );
        event.setDbid(17);
	engine.correlate(event);

        // event + initialized
        m_anticipatedMemorySize = 18;

        verify(engine);
        
    }
    
    private Event createInitializedEvent(int symptom, int cause) {
        return new EventBuilder("initialized", "Drools").getEvent();
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
    
    public Event createNodeLostServiceEvent(int nodeid, String ipAddr, String svcName)
    {
    	return createSvcEvent("uei.opennms.org/nodes/nodeLostService", nodeid, ipAddr, svcName);
    }
    
    public Event createNodeRegainedServiceEvent(int nodeid, String ipAddr, String svcName)
    {
    	return createSvcEvent("uei.opennms.org/nodes/nodeRegainedService", nodeid, ipAddr, svcName);
    }
    
    private Event createSvcEvent(String uei, int nodeid, String ipaddr, String svcName)
    {
    	return new EventBuilder(uei, "Drools")
    		.setNodeid(nodeid)
    		.setInterface( addr( ipaddr ) )
    		.setService( svcName )
    		.getEvent();
    		
    }

    private Event createNodeEvent(String uei, int nodeid) {
        return new EventBuilder(uei, "test")
            .setNodeid(nodeid)
            .getEvent();
    }
    



}