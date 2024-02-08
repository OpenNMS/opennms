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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-correlator.xml",
        "classpath*:META-INF/opennms/correlation-engine.xml",
        "classpath:/test-context.xml"
})
@JUnitConfigurationEnvironment(systemProperties={"org.opennms.activemq.broker.disable=true"})
@JUnitTemporaryDatabase
public class DroolsCorrelationEngineBuilderIT implements InitializingBean {
    @Autowired
    private DroolsCorrelationEngineBuilder m_droolsCorrelationEngineBuilder;
    @Autowired
    private CorrelationEngineRegistrar m_mockCorrelator;

    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_droolsCorrelationEngineBuilder);
        assertNotNull(m_mockCorrelator);
    }

    @Test
    public void testIt() throws Exception {
        Collection<CorrelationEngine> engines = m_mockCorrelator.getEngines();
        assertNotNull(engines);
        assertEquals(8, m_mockCorrelator.getEngines().size());
        assertTrue(engines.iterator().next() instanceof DroolsCorrelationEngine);
        assertTrue(m_mockCorrelator.findEngineByName("locationMonitorRules") instanceof DroolsCorrelationEngine);
        DroolsCorrelationEngine engine = (DroolsCorrelationEngine) m_mockCorrelator.findEngineByName("locationMonitorRules");
        assertEquals(2, engine.getInterestingEvents().size());
        assertTrue(engine.getInterestingEvents().contains(EventConstants.PERSPECTIVE_NODE_LOST_SERVICE_UEI));
        assertTrue(engine.getInterestingEvents().contains(EventConstants.PERSPECTIVE_NODE_REGAINED_SERVICE_UEI));
    }
}
