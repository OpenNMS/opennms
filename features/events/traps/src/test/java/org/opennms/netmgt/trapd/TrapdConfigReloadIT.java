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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.util.KeyValueHolder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.mock.MockMessageConsumerManager;
import org.opennms.core.ipc.sink.mock.MockMessageDispatcherFactory;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.minion.core.api.RestClient;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.base.Throwables;

/**
 * Verifies that the {@link org.opennms.netmgt.config.TrapdConfig} is reloaded regularily from OpenNMS
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml"
})
@JUnitConfigurationEnvironment
public class TrapdConfigReloadIT extends CamelBlueprintTest {

	private AtomicInteger m_port = new AtomicInteger(1162);

	@Autowired
	private DistPollerDao distPollerDao;

	@Override
	protected String setConfigAdminInitialConfiguration(Properties props) {
		getAvailablePort(m_port, 2162);
		props.put("trapd.listen.port", String.valueOf(m_port.get()));
		return "org.opennms.netmgt.trapd";
	}

	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
		final RestClient client;
		try {
			client = mock(RestClient.class);
			when(client.getSnmpV3Users()).thenReturn("<?xml version='1.0'?>"
				+ "<trapd-configuration xmlns='http://xmlns.opennms.org/xsd/config/trapd' snmp-trap-address='127.0.0.1' snmp-trap-port='10500' new-suspect-on-trap='true'>"
				+ "     <snmpv3-user security-name='opennms' security-level='0' auth-protocol='MD5' auth-passphrase='0p3nNMSv3' privacy-protocol='DES' privacy-passphrase='0p3nNMSv3' />"
				+ "</trapd-configuration>");
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		// add mocked services to osgi mocked container (Felix Connect)
		services.put(RestClient.class.getName(), asService(client, null, null));
		services.put(MessageConsumerManager.class.getName(), asService(new MockMessageConsumerManager(), null, null));
		services.put(MessageDispatcherFactory.class.getName(), asService(new MockMessageDispatcherFactory<>(), null, null));
		services.put(DistPollerDao.class.getName(), asService(distPollerDao, null, null));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "OSGI-INF/blueprint/blueprint-trapd-listener.xml";
	}

	@Test
	public void verifyReload() throws Exception {
		// Check that it has not yet been refreshed
		TrapdConfig trapdConfig = getOsgiService(TrapdConfig.class);
		Assert.assertEquals(true, trapdConfig.getSnmpV3Users().isEmpty());

		// The setSnmpV3Users method must have been invoked
		Thread.sleep(20000);

		// Verify
		Assert.assertEquals(1, trapdConfig.getSnmpV3Users().size());
	}
}
