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

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.mock.MockMessageConsumerManager;
import org.opennms.core.ipc.sink.mock.MockMessageDispatcherFactory;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.distributed.core.api.RestClient;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.snmp.TrapListenerConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.base.Throwables;

/**
 * Verifies that the {@link org.opennms.netmgt.config.TrapdConfig} is reloaded regularly from OpenNMS
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath:/META-INF/opennms/applicationContext-twin-memory.xml"
})
@JUnitConfigurationEnvironment
public class TrapdConfigReloadIT extends CamelBlueprintTest {

	private AtomicInteger m_port = new AtomicInteger(1162);

	@Autowired
	private DistPollerDao distPollerDao;

	@Autowired
	private TwinPublisher twinPublisher;

	@Override
	protected String setConfigAdminInitialConfiguration(Properties props) {
		getAvailablePort(m_port, 2162);
		props.put("trapd.listen.port", String.valueOf(m_port.get()));
		return "org.opennms.netmgt.trapd";
	}

	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
		// add mocked services to osgi mocked container (Felix Connect)
		services.put(MessageConsumerManager.class.getName(), asService(new MockMessageConsumerManager(), null, null));
		services.put(MessageDispatcherFactory.class.getName(), asService(new MockMessageDispatcherFactory<>(), null, null));
		services.put(DistPollerDao.class.getName(), asService(distPollerDao, null, null));
		services.put(CamelContext.class.getName(), asService(new DefaultCamelContext(), null, null));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "OSGI-INF/blueprint/blueprint-empty.xml";
	}

	@Test
	public void verifyReload() throws Exception {
		// Check that it has not yet been refreshed
		TrapdConfig trapdConfig = getOsgiService(TrapdConfig.class);
		Assert.assertEquals(true, trapdConfig.getSnmpV3Users().isEmpty());
		var session = twinPublisher.register(TrapListenerConfig.TWIN_KEY, TrapListenerConfig.class);
		session.publish(createTrapListenerConfig());

		// The setSnmpV3Users method must have been invoked
		Thread.sleep(20000);

		// Verify
		Assert.assertEquals(1, trapdConfig.getSnmpV3Users().size());
	}

	private TrapListenerConfig createTrapListenerConfig() {
		TrapdConfiguration config = new TrapdConfiguration();
		config.setSnmpTrapPort(1162);
		config.setSnmpTrapAddress("localhost");
		Snmpv3User snmpv3User = TrapListenerTest.createUser("MD5", "0p3nNMSv3", "some-engine-id",
				"DES", "0p3nNMSv3", "some-security-name");
		List<Snmpv3User> snmpv3UserList = new ArrayList<>();
		snmpv3UserList.add(snmpv3User);
		config.setSnmpv3User(snmpv3UserList);

		TrapListenerConfig trapListenerConfig = new TrapListenerConfig();
		TrapdConfigBean configBean = new TrapdConfigBean(config);
		trapListenerConfig.setSnmpV3Users(configBean.getSnmpV3Users());
		return trapListenerConfig;
	}
}
