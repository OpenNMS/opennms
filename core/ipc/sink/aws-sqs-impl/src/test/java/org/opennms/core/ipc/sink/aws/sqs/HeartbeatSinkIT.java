/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.aws.sqs;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.aws.sqs.heartbeat.Heartbeat;
import org.opennms.core.ipc.sink.aws.sqs.heartbeat.HeartbeatModule;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * The Class HeartbeatSinkIT.
 *
 * <b>Warning:</b> This test requires AWS Access and appropriate credentials stored on ~/.aws/credentials
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Ignore
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-ipc-sink-server-aws.xml"
})
@JUnitConfigurationEnvironment
public class HeartbeatSinkIT {

    /** The local message dispatcher factory. */
    @Autowired
    private MessageDispatcherFactory localMessageDispatcherFactory;

    /** The consumer manager. */
    @Autowired
    private AmazonSQSMessageConsumerManager consumerManager;

    /** The remote message dispatcher factory. */
    private AmazonSQSRemoteMessageDispatcherFactory remoteMessageDispatcherFactory = new AmazonSQSRemoteMessageDispatcherFactory();

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        Hashtable<String, Object> awsConfig = new Hashtable<String, Object>();
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(AmazonSQSSinkConstants.AWS_CONFIG_PID).getProperties()).thenReturn(awsConfig);
        remoteMessageDispatcherFactory.setConfigAdmin(configAdmin);
        remoteMessageDispatcherFactory.init();
        consumerManager.afterPropertiesSet();
    }

    /**
     * Can produce and consume messages.
     *
     * @throws Exception the exception
     */
    @Test(timeout=30000)
    public void canProduceAndConsumeMessages() throws Exception {
        HeartbeatModule module = new HeartbeatModule();

        AtomicInteger heartbeatCount = new AtomicInteger();
        final MessageConsumer<Heartbeat,Heartbeat> heartbeatConsumer = new MessageConsumer<Heartbeat,Heartbeat>() {
            @Override
            public SinkModule<Heartbeat,Heartbeat> getModule() {
                return module;
            }

            @Override
            public void handleMessage(final Heartbeat heartbeat) {
                heartbeatCount.incrementAndGet();
            }
        };

        try {
            consumerManager.registerConsumer(heartbeatConsumer);

            final SyncDispatcher<Heartbeat> localDispatcher = localMessageDispatcherFactory.createSyncDispatcher(module);
            localDispatcher.send(new Heartbeat());
            await().atMost(1, MINUTES).until(() -> heartbeatCount.get(), equalTo(1));

            final SyncDispatcher<Heartbeat> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(HeartbeatModule.INSTANCE);

            dispatcher.send(new Heartbeat());
            await().atMost(1, MINUTES).until(() -> heartbeatCount.get(), equalTo(2));
        } finally {
            consumerManager.unregisterConsumer(heartbeatConsumer);
        }
    }

}
