/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.MessageProducer;

import org.apache.camel.util.KeyValueHolder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.mock.MockMessageConsumerManager;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.minion.core.api.RestClient;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Verify that the TrapListener is actually receiving traps and is using the Sink pattern to "dispatch" messages.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml"
})
@JUnitConfigurationEnvironment
public class TrapdSinkPatternWiringIT extends CamelBlueprintTest {

    private AtomicInteger m_port = new AtomicInteger(1162);

    @Autowired
    private DistPollerDao distPollerDao;

    private final CountDownLatch messageProcessedLatch = new CountDownLatch(1);

    @Override
    protected String setConfigAdminInitialConfiguration(Properties props) {
        getAvailablePort(m_port, 2162);
        props.put("trapd.listen.port", String.valueOf(m_port.get()));
        return "org.opennms.netmgt.trapd";
    }

    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        final MessageDispatcherFactory mockMessageDispatcherFactory = mock(MessageDispatcherFactory.class);
        when(mockMessageDispatcherFactory.createAsyncDispatcher(Mockito.any(TrapSinkModule.class)))
            .thenAnswer(invocation -> {
                messageProcessedLatch.countDown(); // register call
                return mock(MessageProducer.class);
            });

        // add mocked services to osgi mocked container (Felix Connect)
        services.put(MessageConsumerManager.class.getName(), asService(new MockMessageConsumerManager(), null, null));
        services.put(MessageDispatcherFactory.class.getName(), asService(mockMessageDispatcherFactory, null, null));
        services.put(RestClient.class.getName(), asService(mock(RestClient.class), null, null));
        services.put(DistPollerDao.class.getName(), asService(distPollerDao, null, null));
    }

    // The CamelBlueprintTest should have started the bundle and therefore also started
    // the TrapListener (see blueprint-trapd-listener.xml), which listens to traps.
    @Test
    public void testWiring() throws Exception {
        // No traps received or processed
        Assert.assertEquals(1, messageProcessedLatch.getCount());

        // At this point everything should be set up correctly
        final SnmpTrapBuilder builder = SnmpUtils.getV2TrapBuilder();
        builder.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils.getValueFactory().getTimeTicks(0));
        builder.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), SnmpUtils.getValueFactory().getObjectId(SnmpObjId.get(".1.3.6.1.6.3.1.1.5.2")));
        builder.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), SnmpUtils.getValueFactory().getObjectId(SnmpObjId.get(".1.3.6.1.4.1.5813")));
        builder.send("localhost", m_port.get(), "public");

        // Wait before continuing
        messageProcessedLatch.await(10, TimeUnit.SECONDS);

        // Trap should be received and processed
        Assert.assertEquals(0, messageProcessedLatch.getCount());
    }

    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/blueprint-trapd-listener.xml";
    }
}
