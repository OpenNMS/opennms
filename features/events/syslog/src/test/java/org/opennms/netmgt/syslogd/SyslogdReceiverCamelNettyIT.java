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

package org.opennms.netmgt.syslogd;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.minion.core.api.MinionIdentity;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class SyslogdReceiverCamelNettyIT extends CamelBlueprintTest {

	private static final String LOCATION = "RDU";

	@SuppressWarnings("rawtypes")
	@Override
	protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
		// Register any mock OSGi services here
		services.put(SyslogConnectionHandler.class.getName(), new KeyValueHolder<Object, Dictionary>(new SyslogConnectionHandler() {
			@Override
			public void handleSyslogConnection(SyslogConnection message) {
			}
		}, new Properties()));

		services.put( MinionIdentity.class.getName(), new KeyValueHolder<Object, Dictionary>( new MinionIdentity() {
			@Override
			public String getId() {
				return DistPollerDao.DEFAULT_DIST_POLLER_ID;
			}
			@Override
			public String getLocation() {
				return LOCATION;
			}
		}, new Properties()));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-syslog-listener-camel-netty.xml,blueprint-empty-camel-context.xml";
	}

	@Test
	public void testSyslogd() throws Exception {
		// TODO: Perform integration testing
	}
}
