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

import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.addr;

import org.junit.runner.RunWith;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/correlation-engine.xml",
        "classpath:/test-context.xml"
})
@JUnitConfigurationEnvironment(systemProperties={"org.opennms.activemq.broker.disable=true"})
@DirtiesContext
public abstract class CorrelationRulesTestCase {

    @Autowired
    private MockEventIpcManager m_eventIpcMgr;
    protected Integer m_anticipatedMemorySize = 0;
    
    @Autowired
    private CorrelationEngineRegistrar m_correlator;

    protected CorrelationRulesTestCase() {
        ConfigurationTestUtils.setRelativeHomeDirectory("src/test/opennms-home");
    }

    public void setCorrelationEngineRegistrar(CorrelationEngineRegistrar correlator) {
        m_correlator = correlator;
    }
    
    protected void resetAnticipated() {
    	getAnticipator().reset();
    	m_anticipatedMemorySize = null;
    }

    protected void verify(DroolsCorrelationEngine engine) {
    	getAnticipator().verifyAnticipated(0, 0, 0, 0, 0);
        if (m_anticipatedMemorySize != null) {
            assertEquals("Unexpected number of objects in working memory: "+engine.getKieSessionObjects(), m_anticipatedMemorySize.intValue(), engine.getKieSessionObjects().size());
        }
    }

    protected DroolsCorrelationEngine findEngineByName(String engineName) {
        return (DroolsCorrelationEngine) m_correlator.findEngineByName(engineName);
    }

    protected void anticipate(Event event) {
        getAnticipator().anticipateEvent(event);
    }

    protected Event createNodeLostServiceEvent(int nodeId, String ipAddr, String svcName, int locationMonitor) {
    	return createEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, nodeId, ipAddr, svcName, locationMonitor);
    }

    protected Event createPerspectiveNodeLostServiceEvent(int nodeId, String ipAddr, String svcName, int locationMonitor) {
    	return createEvent(EventConstants.PERSPECTIVE_NODE_LOST_SERVICE_UEI, nodeId, ipAddr, svcName, locationMonitor);
    }

    protected Event createPerspectiveNodeRegainedServiceEvent(int nodeId, String ipAddr, String svcName, int locationMonitor) {
    	return createEvent(EventConstants.PERSPECTIVE_NODE_REGAINED_SERVICE_UEI, nodeId, ipAddr, svcName, locationMonitor);
    }

    protected Event createEvent(String uei, int nodeId, String ipAddr, String svcName, int locationMonitor) {
    	return new EventBuilder(uei, "test")
        .setNodeid(nodeId).setInterface(addr(ipAddr))
        	.setService(svcName)
        	.addParam(EventConstants.PARM_LOCATION_MONITOR_ID, locationMonitor)
            .getEvent();
    }

    protected Event createServiceEvent(String uei, int nodeId, String ipAddr, String svcName) {
        return new EventBuilder(uei, "test")
        .setNodeid(nodeId).setInterface(addr("192.168.1.1"))
            .setService("HTTP")
            .getEvent();
    }

    /**
     * @return the anticipator
     */
    protected EventAnticipator getAnticipator() {
        return m_eventIpcMgr.getEventAnticipator();
    }

}
