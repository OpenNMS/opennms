/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.provision.detector.common.DelegatingLocationAwareDetectorClientImpl;
import org.opennms.netmgt.provision.detector.common.DetectorRequestExecutor;
import org.opennms.netmgt.provision.detector.loop.LoopDetector;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

/**
 * Used to test the Camel context defined in provisiond-extensions.xml.
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
public class LocationAwareDetectorClientIT {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker s_broker = new ActiveMQBroker();

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    private DelegatingLocationAwareDetectorClientImpl locationAwareDetectorClient;

    @Autowired
    @Qualifier("localDetectorExecutor")
    private DetectorRequestExecutor localDetectorRequestExecutor;

    @Autowired
    @Qualifier("queuingservice")
    private Component queuingservice;

    /**
     * Verifies that a detector can be invoked using the current location.
     */
    @Test
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
    */
    @Test
    public void canDetectViaAnotherLocation() throws Exception {
        assertNotEquals(REMOTE_LOCATION_NAME, identity.getLocation());

        final AsyncDetectorRequestProcessor detectorRequestProcessor = new AsyncDetectorRequestProcessor();
        detectorRequestProcessor.setDetectorExecutor(localDetectorRequestExecutor);

        SimpleRegistry registry = new SimpleRegistry();
        CamelContext context = new DefaultCamelContext(registry);
        context.addComponent("queuingservice", queuingservice);
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("queuingservice:detector@" + REMOTE_LOCATION_NAME)
                .setExchangePattern(ExchangePattern.InOut)
                .process(detectorRequestProcessor);
            };
        });
        context.start();

        boolean isDetected = locationAwareDetectorClient.detect()
            .withLocation(REMOTE_LOCATION_NAME)
            .withClassName(LoopDetector.class.getCanonicalName())
            .withAddress(InetAddress.getByName("127.0.0.1"))
            .withAttribute("ipMatch", "127.0.0.*")
            .execute().get();
        assertEquals(true, isDetected);

        context.stop();
    }
}
