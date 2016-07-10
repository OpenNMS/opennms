/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.camel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Component;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.minion.core.api.MinionIdentity;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.provision.ServiceDetectorFactory;
import org.opennms.netmgt.provision.detector.common.DelegatingLocationAwareDetectorClientImpl;
import org.opennms.netmgt.provision.detector.loop.LoopDetector;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;
import org.opennms.netmgt.provision.detector.registry.impl.ServiceDetectorRegistryImpl;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Used to test the Camel context defined in blueprint.xml.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitConfigurationEnvironment
public class LocationAwareDetectorClientBlueprintIT extends CamelBlueprintTest {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker s_broker = new ActiveMQBroker();

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    private DelegatingLocationAwareDetectorClientImpl locationAwareDetectorClient;

    @Autowired
    private ServiceDetectorRegistryImpl serviceDetectorRegistry;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        ServiceDetectorFactory<ExceptionalSyncServiceDetector> factory = new ServiceDetectorFactory<ExceptionalSyncServiceDetector>() {
            @Override
            public ExceptionalSyncServiceDetector createDetector() {
                return new ExceptionalSyncServiceDetector();
            }
        };
        Map<String, String> props = new HashMap<>();
        props.put(ServiceDetectorRegistryImpl.IMPLEMENTATION_KEY, factory.createDetector().getClass().getCanonicalName());
        serviceDetectorRegistry.onBind(factory, props);
    }

    @SuppressWarnings( "rawtypes" )
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(ServiceDetectorRegistry.class.getName(), new KeyValueHolder<Object, Dictionary>(
                serviceDetectorRegistry, new Properties()));
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
                }, new Properties()));

        Properties props = new Properties();
        props.setProperty("alias", "opennms.broker");
        services.put(Component.class.getName(),
                new KeyValueHolder<Object, Dictionary>(ActiveMQComponent.activeMQComponent("vm://localhost?create=false"), props));
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
        boolean caughtException = false;
        try {
            locationAwareDetectorClient.detect()
                .withLocation(REMOTE_LOCATION_NAME)
                .withClassName(ExceptionalSyncServiceDetector.class.getCanonicalName())
                .withAddress(InetAddress.getLoopbackAddress())
                .execute().get();
        } catch (ExecutionException e) {
            assertEquals("Failure on sync detection.", e.getCause().getMessage());
            caughtException = true;
        }
        assertTrue("Did not catch exception", caughtException);

        // Error on detection with asynchronous detector
        caughtException = false;
        try {
            locationAwareDetectorClient.detect()
                .withLocation(REMOTE_LOCATION_NAME)
                .withClassName(ExceptionalAsyncServiceDetector.class.getCanonicalName())
                .withAddress(InetAddress.getLoopbackAddress())
                .execute().get();
        } catch (ExecutionException e) {
            assertEquals("Failure on async detection.", e.getCause().getMessage());
            caughtException = true;
        }
        assertTrue("Did not catch exception", caughtException);
    }
}
