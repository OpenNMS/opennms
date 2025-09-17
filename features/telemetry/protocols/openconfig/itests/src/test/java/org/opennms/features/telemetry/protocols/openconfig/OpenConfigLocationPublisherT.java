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



import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.ServiceRef;
import org.opennms.netmgt.telemetry.config.model.ConnectorTwinConfig;
import org.opennms.netmgt.telemetry.daemon.LocationPublisher;
import org.opennms.netmgt.telemetry.daemon.LocationPublisherManager;
import org.opennms.netmgt.telemetry.daemon.OpenConfigTwinPublisherImpl;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

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
@JUnitConfigurationEnvironment(systemProperties={ // We don't need a real pinger here
        "org.opennms.netmgt.icmp.pingerClass=org.opennms.netmgt.icmp.NullPinger"})
@JUnitTemporaryDatabase(tempDbClass= MockDatabase.class,reuseDatabase=false)
public class OpenConfigLocationPublisherT {

    @Autowired
    private LocationPublisherManager locationPublisherManager;

    @Autowired
    private OpenConfigTwinPublisherImpl openConfigTwinPublisher;



    @Autowired
    private Map<String, TwinSubscriber> twinSubscribers;

    @Before
    public void setUp() {
        assertNotNull(locationPublisherManager);
        assertNotNull(openConfigTwinPublisher);

    }

    // helper to read private fields via reflection
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(target);
    }
    @Test
    public void testOpenConfigTwinPublisher_publish_and_remove_flow_using_real_ServiceRef() throws Exception {
        InetAddress addr;
        try {
            addr = InetAddressUtils.addr("10.0.0.5");
        } catch (NoClassDefFoundError | Exception e) {
            addr = InetAddress.getByName("10.0.0.5");
        }

        ServiceRef srv = new ServiceRef(100, addr, "OpenConfigService", "LOC-TEST");

        List<Map<String, String>> params = Collections.singletonList(Collections.singletonMap("k", "v"));
        String nodeConnectorKey = "nk-xyz";

        openConfigTwinPublisher.publishConfig(srv, params, nodeConnectorKey);

        LocationPublisher lp = locationPublisherManager.getOrCreate("LOC-TEST");
        assertNotNull(lp);

        @SuppressWarnings("rawtypes")
        java.util.Map configs = getPrivateField(lp, "configs");
        assertNotNull("configs map should not be null", configs);
        assertTrue("configs should contain our nodeConnectorKey", configs.containsKey(nodeConnectorKey));

        Object session = getPrivateField(lp, "session");
        assertNotNull("session should be created after publish", session);

        openConfigTwinPublisher.removeConfig(srv, nodeConnectorKey);

        LocationPublisher lpAfter = locationPublisherManager.getOrCreate("LOC-TEST");
        @SuppressWarnings("rawtypes")
        java.util.Map configsAfter = getPrivateField(lpAfter, "configs");
        assertTrue("after removal there should be no configs", configsAfter.isEmpty());

    }

    @Test
    public void testLocationPublisherManager_getOrCreate_and_removeIfEmpty() throws Exception {
        LocationPublisher p1 = locationPublisherManager.getOrCreate("LOC-MGR-1");
        LocationPublisher p2 = locationPublisherManager.getOrCreate("LOC-MGR-1");
        assertSame("getOrCreate should return same instance for same location", p1, p2);

        assertFalse("new publisher should have no configs", p1.hasConfigs());

        locationPublisherManager.removeIfEmpty("LOC-MGR-1");
        LocationPublisher p3 = locationPublisherManager.getOrCreate("LOC-MGR-1");
        assertNotSame("after removeIfEmpty a new instance should be created", p1, p3);
    }

    @Test
    public void testForceCloseAll_clears_publishers() throws Exception {
        String defaultLocation = "default";
        LocationPublisher p1 = locationPublisherManager.getOrCreate("LOC-X");

        ConnectorTwinConfig.ConnectorConfig cfg;
        try {
            String ipStr = InetAddressUtils.str(InetAddress.getByName("1.1.1.1"));
            cfg = new ConnectorTwinConfig.ConnectorConfig(999, ipStr, "ck-1", Collections.emptyList());
        } catch (NoClassDefFoundError | Exception e) {
            cfg = new ConnectorTwinConfig.ConnectorConfig(999, "1.1.1.1", "ck-1", Collections.emptyList());
        }

        p1.addConfigAndPublish(cfg);
        assertTrue("p1 must have configs", p1.hasConfigs());

        locationPublisherManager.forceCloseAll();

        LocationPublisher p2 = locationPublisherManager.getOrCreate("LOC-X");
        assertNotSame("manager.forceCloseAll should clear internal map", p1, p2);
        assertFalse("fresh publisher should not have configs", p2.hasConfigs());
    }

}
