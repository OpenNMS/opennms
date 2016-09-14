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

import java.net.ServerSocket;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class TrapdListenerBlueprintRestClientIT extends CamelBlueprintTest {

	private static final Logger LOG = LoggerFactory.getLogger(TrapdListenerBlueprintRestClientIT.class);

	private static final String PORT_NAME="trapd.listen.port";

	private static final String PERSISTANCE_ID="org.opennms.netmgt.trapd";

	private int m_port;

	/**
	 * This method overrides the blueprint property and sets port to 10514 instead of 162
	 */
	@Override
	protected String setConfigAdminInitialConfiguration(Properties props) {
		m_port = getAvailablePort(10500, 10900);
		props.put(PORT_NAME, String.valueOf(m_port));
		return PERSISTANCE_ID;
	}

	private static int getAvailablePort(int min, int max) {
		for (int i = min; i <= max; i++) {
			try (ServerSocket socket = new ServerSocket(i)) {
				return socket.getLocalPort();
			} catch (Throwable e) {}
		}
		throw new IllegalStateException("Can't find an available network port");
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
		// Register any mock OSGi services here
	    services.put( TrapNotificationHandler.class.getName(), new KeyValueHolder<Object, Dictionary>(new TrapNotificationHandlerCamelImpl("seda:handleMessage"), new Properties()));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-trapd-listener.xml,blueprint-empty-camel-context.xml";
	}


	@Test
	public void testTrapd() throws Exception {}
	
	
	
}
