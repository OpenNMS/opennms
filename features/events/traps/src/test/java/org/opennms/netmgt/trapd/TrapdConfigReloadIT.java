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

import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.camel.util.KeyValueHolder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.minion.core.api.RestClient;
import org.opennms.netmgt.config.TrapdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

/**
 * Verifies that the {@link org.opennms.netmgt.config.TrapdConfig} is reloaded regularily from OpenNMS
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class TrapdConfigReloadIT extends CamelBlueprintTest {

	private static final Logger LOG = LoggerFactory.getLogger(TrapdConfigReloadIT.class);

	private final CountDownLatch latch = new CountDownLatch(1);

	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
		RestClient client = new RestClient() {
			@Override
			public void ping() throws Exception {
			}

			@Override
			public String getSnmpV3Users() throws Exception {
				latch.countDown();
				return "<?xml version='1.0'?>"
						+ "<trapd-configuration xmlns='http://xmlns.opennms.org/xsd/config/trapd' snmp-trap-address='127.0.0.1' snmp-trap-port='10500' new-suspect-on-trap='true'>"
						+ "		<snmpv3-user security-name='opennms' security-level='0' auth-protocol='MD5' auth-passphrase='0p3nNMSv3' privacy-protocol='DES' privacy-passphrase='0p3nNMSv3' />"
						+ "</trapd-configuration>";
			}
		};

		services.put(RestClient.class.getName(), asService(client, null, null));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "OSGI-INF/blueprint/blueprint-trapd-handler-minion.xml";
	}

	@Test
	public void verifyReload() throws Exception {
		// Check that it has not yet been refresehd
		TrapdConfig config = getOsgiService(TrapdConfig.class);
		Assert.assertEquals(true, config.getSnmpV3Users().isEmpty());

		// The getSnmpV3Users method have been invoked
		latch.await(120, TimeUnit.SECONDS);
		Assert.assertEquals(0, latch.getCount());

		// Verify
		Assert.assertEquals(1, config.getSnmpV3Users().size());
	}
}
