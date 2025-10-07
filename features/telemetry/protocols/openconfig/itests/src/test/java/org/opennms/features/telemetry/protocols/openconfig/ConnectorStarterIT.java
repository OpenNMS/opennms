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

package org.opennms.features.telemetry.protocols.openconfig;


import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.util.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.config.model.ConnectorTwinConfig;
import org.opennms.netmgt.telemetry.connectors.ConnectorStarter;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-collectionAgentFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-openconfig-components.xml",
        "classpath:/META-INF/opennms/applicationContext-daoEvents.xml",
        "classpath:/META-INF/opennms/applicationContext-telemetryDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-thresholding.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-testThresholdingDaos.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass = MockDatabase.class, reuseDatabase = false)
public class ConnectorStarterIT {

    @Autowired
    private ConnectorStarter connectorStarter;

    @Before
    public void setUp() {
    }

    @Test
    public void testConnectorStarterInitialization() {
        assertNotNull("ConnectorStarter should be initialized", connectorStarter);

        Map<String, String> configMap = connectorStarter.getConfigMap();
        assertNotNull("Config map should not be null", configMap);
        assertEquals("OpenConfig", configMap.get("name"));
        assertEquals("OpenConfig", configMap.get("queue"));
        assertEquals("OpenConfig", configMap.get("service-name"));
    }

    @Test
    public void testConfigurationUpdateWithNewQueueName() throws ConfigurationException {
        Hashtable<String, Object> newConfig = new Hashtable<>();
        newConfig.put("name", "OpenConfig-Test");
        newConfig.put("class-name", "org.opennms.netmgt.telemetry.protocols.openconfig.connector.OpenConfigConnector");
        newConfig.put("queue", "New-Test-Queue");  // Different queue name
        newConfig.put("service-name", "OpenConfig-Test-Service");

        connectorStarter.updated(newConfig);
        TelemetryRegistry registry = connectorStarter.getTelemetryRegistry();
        assertNotNull("New dispatcher should be registered",
                registry.getDispatcher("New-Test-Queue"));

        connectorStarter.stop();

        assertTrue("All entities should be cleaned up",
                connectorStarter.getEntities().isEmpty());
    }

    @Test
    public void testConfigurationUpdateWithNullProperties() throws ConfigurationException {

        connectorStarter.updated(null);
        assertNotNull("Config map should not be null after null update",
                connectorStarter.getConfigMap());
    }

    @Test
    public void testQueueNameChangeDetection() throws Exception {

        Method hasQueueNameChangedMethod = ConnectorStarter.class.getDeclaredMethod("hasQueueNameChanged", Map.class);
        hasQueueNameChangedMethod.setAccessible(true);

        Map<String, String> sameQueueConfig = new HashMap<>();
        sameQueueConfig.put("queue", "OpenConfig");

        Map<String, String> differentQueueConfig = new HashMap<>();
        differentQueueConfig.put("queue", "Different-Queue");

        boolean sameQueueResult = (Boolean) hasQueueNameChangedMethod.invoke(connectorStarter, sameQueueConfig);
        boolean differentQueueResult = (Boolean) hasQueueNameChangedMethod.invoke(connectorStarter, differentQueueConfig);

        assertFalse("Same queue name should not trigger change", sameQueueResult);
        assertTrue("Different queue name should trigger change", differentQueueResult);
    }

    @After
    public void tearDown() {
        // Ensure clean state
        if (connectorStarter != null) {
            connectorStarter.getEntities().keySet().forEach(connectorStarter::delete);
        }
    }
}