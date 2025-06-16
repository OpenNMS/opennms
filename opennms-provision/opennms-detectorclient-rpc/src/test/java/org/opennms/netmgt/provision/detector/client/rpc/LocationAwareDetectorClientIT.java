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
package org.opennms.netmgt.provision.detector.client.rpc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.apache.camel.Component;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.distributed.core.api.SystemType;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.provision.LocationAwareDetectorClient;
import org.opennms.netmgt.provision.detector.loop.LoopDetector;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-scan-executor.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-jms.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-detect.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-utils.xml",
        "classpath:/META-INF/opennms/applicationContext-jceks-scv.xml"
})
@JUnitConfigurationEnvironment  
@org.springframework.test.annotation.IfProfileValue(name="runFlappers", value="true")
public class LocationAwareDetectorClientIT extends CamelBlueprintTest {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker broker = new ActiveMQBroker();

    @Autowired
    @Qualifier("queuingservice")
    private Component queuingservice;

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    private LocationAwareDetectorClient locationAwareDetectorClient;

    @Autowired
    private DetectorClientRpcModule detectorClientRpcModule;

    @Autowired
    private ServiceDetectorRegistry serviceDetectorRegistry;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        detectorClientRpcModule.setExecutor(Executors.newSingleThreadExecutor());
    }

    @Override
    protected String setConfigAdminInitialConfiguration(Properties props) {
        props.put("body.debug", "-5");
        return "org.opennms.core.ipc";
    }

    @SuppressWarnings( "rawtypes" )
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(MinionIdentity.class.getName(),
                new KeyValueHolder<Object, Dictionary>(new MinionIdentity() {
                    @Override
                    public String getId() {
                        return "0";
                    }
                    @Override
                    public String getLocation() {
                        return REMOTE_LOCATION_NAME;
                    }
                    @Override
                    public String getType() {
                        return SystemType.Minion.name();
                    }
                }, new Properties()));

        Properties props = new Properties();
        props.setProperty("alias", "opennms.broker");
        services.put(Component.class.getName(), new KeyValueHolder<Object, Dictionary>(queuingservice, props));
        services.put(ServiceDetectorRegistry.class.getName(), new KeyValueHolder<Object, Dictionary>(serviceDetectorRegistry, new Properties()));
    }

    @Override
    protected String getBlueprintDescriptor() {
        return "classpath:/OSGI-INF/blueprint/blueprint.xml";
    }

    /**
     * Verifies that a detector can be invoked using the current location.
     */
    @Test(timeout=60000)
    public void canDetectViaCurrentLocation() throws InterruptedException, ExecutionException, UnknownHostException {
        boolean isDetected = locationAwareDetectorClient.detect()
                .withLocation(identity.getLocation())
                .withClassName(LoopDetector.class.getCanonicalName())
                .withAddress(InetAddress.getByName("127.0.0.1"))
                .withAttribute("ipMatch", "127.0.0.*")
                .execute().get();
        assertEquals(true, isDetected);
    }

    /**
     * Verifies that a detector can be invoked using a different location.
     *
     * This should invoke the route in the Camel context initialized in this blueprint.
     */
    @Test(timeout=60000)
    public void canDetectViaAnotherLocation() throws Exception {
        // Was detected
        boolean isDetected = locationAwareDetectorClient.detect()
                .withLocation(REMOTE_LOCATION_NAME)
                .withClassName(LoopDetector.class.getCanonicalName())
                .withAddress(InetAddress.getByName("127.0.0.1"))
                .withAttribute("ipMatch", "127.0.0.*")
                .execute().get();
        assertEquals(true, isDetected);

        // Was not detected
        isDetected = locationAwareDetectorClient.detect()
                .withLocation(REMOTE_LOCATION_NAME)
                .withClassName(LoopDetector.class.getCanonicalName())
                .withAddress(InetAddress.getByName("10.0.1.10"))
                .withAttribute("ipMatch", "127.0.0.*")
                .execute().get();
        assertEquals(false, isDetected);

        // Error on detection with synchronous detector
        try {
            locationAwareDetectorClient.detect()
                .withLocation(REMOTE_LOCATION_NAME)
                .withClassName(ExceptionalSyncServiceDetector.class.getCanonicalName())
                .withAddress(InetAddress.getLoopbackAddress())
                .execute().get();
            fail("Exception was not thrown.");
        } catch (ExecutionException e) {
            final String message = e.getCause().getMessage();
            assertTrue(message, message.contains("Failure on sync detection."));
        }

        // Error on detection with asynchronous detector
        try {
            locationAwareDetectorClient.detect()
                .withLocation(REMOTE_LOCATION_NAME)
                .withClassName(ExceptionalAsyncServiceDetector.class.getCanonicalName())
                .withAddress(InetAddress.getLoopbackAddress())
                .execute().get();
            fail("Exception was not thrown.");
        } catch (ExecutionException e) {
            final String message = e.getCause().getMessage();
            assertTrue(message, message.contains("Failure on async detection."));
        }
    }

    @Test
    public void didOverrideBodyDebug() throws Exception {
        assertEquals("-5", context.getProperty("CamelLogDebugBodyMaxChars"));
    }
}
