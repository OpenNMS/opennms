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
package org.opennms.netmgt.trapd;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.TrapdConfigFactory;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.scriptd.helper.SnmpTrapHelper;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-dao.xml",
		"classpath*:/META-INF/opennms/component-dao.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		"classpath:/META-INF/opennms/applicationContext-trapDaemon.xml",
		// Overrides the port that Trapd binds to and sets newSuspectOnTrap to 'true'
		"classpath:/org/opennms/netmgt/trapd/applicationContext-trapDaemonTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class TrapdConfigReloadIT {

	@Autowired
	private TrapdConfigFactory trapdConfig;

	@Autowired
	private Trapd trapd;

	@Autowired
	private TrapListener trapListener;

	@Autowired
	private MockEventIpcManager events;

	private final static InetAddress LOCALHOST = InetAddressUtils.addr("127.0.0.1");

	@Before
	public void setUp() {
		this.events.setSynchronous(true);
		this.events.getEventAnticipator().reset();
		this.trapd.setSecureCredentialsVault(new TrapdIT.MockSecureCredentialsVault());
		this.trapd.onStart();
	}

	@After
	public void tearDown() {
		this.trapd.onStop();

		this.events.getEventAnticipator().verifyAnticipated();
	}

	@Test
	public void testSnmpV3UserUpdate() throws Exception {
		final var user = this.trapd.interpolateUser(this.trapdConfig.getConfig().getSnmpv3User(0));

		this.events.getEventAnticipator().anticipateEvent(new EventBuilder("uei.opennms.org/default/trap", "trapd")
																  .setInterface(LOCALHOST).getEvent());
		this.events.getEventAnticipator().anticipateEvent(new EventBuilder("uei.opennms.org/internal/discovery/newSuspect", "trapd")
																  .setInterface(LOCALHOST).getEvent());

		this.sendTrap(user, 1);
		await().until(this.events.getEventAnticipator()::getAnticipatedEventsReceived, hasSize(2));

		// Update SNMPv3 users
		user.setPrivacyPassphrase(user.getPrivacyPassphrase() + "-changed");
		user.setAuthPassphrase(user.getAuthPassphrase() + "-altered");

		this.trapd.publishListenerConfig();

		this.events.getEventAnticipator().anticipateEvent(new EventBuilder("uei.opennms.org/default/trap", "trapd")
																  .setInterface(LOCALHOST).getEvent());
		this.events.getEventAnticipator().anticipateEvent(new EventBuilder("uei.opennms.org/internal/discovery/newSuspect", "trapd")
																  .setInterface(LOCALHOST).getEvent());

		this.sendTrap(user, 2);
		await().until(this.events.getEventAnticipator()::getAnticipatedEventsReceived, hasSize(4));
	}

	private void sendTrap(final Snmpv3User user, final int marker) throws Exception {
		final SnmpTrapHelper snmpTrapHelper = new SnmpTrapHelper();

		final var trap = snmpTrapHelper.createV3Trap(".1.3.6.1.4.1.5813.1.1", "0");
		snmpTrapHelper.addVarBinding(trap, ".1.3.6.1.4.1.5813.20.1.8.0", "OctetString", "text", Integer.toString(marker));

		trap.send(InetAddressUtils.toIpAddrString(LOCALHOST),
				  this.trapdConfig.getSnmpTrapPort(),
				  user.getSecurityLevel(),
				  user.getSecurityName(),
				  user.getAuthPassphrase(),
				  user.getAuthProtocol(),
				  user.getPrivacyPassphrase(),
				  user.getPrivacyProtocol());
	}
}
