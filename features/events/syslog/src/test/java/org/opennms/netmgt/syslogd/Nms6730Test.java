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
package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.mock.MockDistPollerDao;
import org.opennms.netmgt.provision.LocationAwareDnsLookupClient;

/**
 * @author Seth
 */
public class Nms6730Test {

	private LocationAwareDnsLookupClient locationAwareDnsLookupClient = Mockito.mock(LocationAwareDnsLookupClient.class);

	@Test
	public void testCustomSyslogParser() throws Exception {

		SyslogConfigBean config = new SyslogConfigBean();
		config.setParser("org.opennms.netmgt.syslogd.CustomSyslogParser");
		config.setForwardingRegexp("^.*\\s(19|20)\\d\\d([-/.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])(\\s+)(\\S+)(\\s)(\\S.+)");
		config.setMatchingGroupHost(6);
		config.setMatchingGroupMessage(8);
		config.setDiscardUei("DISCARD-MATCHING-MESSAGES");

		ByteBuffer example1 = SyslogdTestUtils.toByteBuffer("<14> 2001-01-01 localhost this is [my] message");
		ByteBuffer example2 = SyslogdTestUtils.toByteBuffer("<14> 2001-01-01 localhost [37183]: this is [my] message");
		ByteBuffer example3 = SyslogdTestUtils.toByteBuffer("<14> 2001-01-01 localhost procname: this is [my] message");
		ByteBuffer example4 = SyslogdTestUtils.toByteBuffer("<14> 2001-01-01 localhost procname[37183]: this is [my] message");

		for (ByteBuffer incoming : new ByteBuffer[] { example1, example2, example3, example4 }) {
			try {
				ConvertToEvent convertToEvent = new ConvertToEvent(
					MockDistPollerDao.DEFAULT_DIST_POLLER_ID,
					MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
					InetAddressUtils.ONE_TWENTY_SEVEN,
					514,
					incoming,
					config,
						locationAwareDnsLookupClient
				);
				assertEquals("this is [my] message", convertToEvent.getEvent().getLogmsg().getContent());
			} catch (MessageDiscardedException e) {
				fail("Message parsing failed: " + e.getMessage());
			}
		}


		{
			ByteBuffer colonNoSpace = SyslogdTestUtils.toByteBuffer("<14> 2001-01-01 localhost procname:this is [my] message");
			ConvertToEvent convertToEvent = new ConvertToEvent(
				MockDistPollerDao.DEFAULT_DIST_POLLER_ID,
				MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
				InetAddressUtils.ONE_TWENTY_SEVEN,
				514,
				colonNoSpace,
				config,
					locationAwareDnsLookupClient
			);
			assertEquals("procname:this is [my] message", convertToEvent.getEvent().getLogmsg().getContent());
		}

		{
			ByteBuffer spaceBeforeColon = SyslogdTestUtils.toByteBuffer("<14> 2001-01-01 localhost proc name: this is [my] message");
			ConvertToEvent convertToEvent = new ConvertToEvent(
				MockDistPollerDao.DEFAULT_DIST_POLLER_ID,
				MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
				InetAddressUtils.ONE_TWENTY_SEVEN,
				514,
				spaceBeforeColon,
				config,
					locationAwareDnsLookupClient
			);
			assertEquals("proc name: this is [my] message", convertToEvent.getEvent().getLogmsg().getContent());
		}
	}

	@Test
	public void testSyslogNgParser() throws Exception {

		SyslogConfigBean config = new SyslogConfigBean();
		config.setParser("org.opennms.netmgt.syslogd.SyslogNGParser");
		config.setDiscardUei("DISCARD-MATCHING-MESSAGES");

		ByteBuffer example3 = SyslogdTestUtils.toByteBuffer("<14> Jan 22 12:39:25 localhost this is [my] message");
		ByteBuffer example4 = SyslogdTestUtils.toByteBuffer("<14> Jan 22 12:39:25 localhost [37183]: this is [my] message");
		ByteBuffer example1 = SyslogdTestUtils.toByteBuffer("<14> Jan 22 12:39:25 localhost procname: this is [my] message");
		ByteBuffer example2 = SyslogdTestUtils.toByteBuffer("<14> Jan 22 12:39:25 localhost procname[37183]: this is [my] message");

		for (ByteBuffer incoming : new ByteBuffer[] { example1, example2, example3, example4 }) {
			try {
				ConvertToEvent convertToEvent = new ConvertToEvent(
					MockDistPollerDao.DEFAULT_DIST_POLLER_ID,
					MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
					InetAddressUtils.ONE_TWENTY_SEVEN,
					514,
					incoming,
					config,
						locationAwareDnsLookupClient
				);
				assertEquals("this is [my] message", convertToEvent.getEvent().getLogmsg().getContent());
			} catch (MessageDiscardedException e) {
				fail("Message parsing failed: " + e.getMessage());
			}
		}

		{
			ByteBuffer colonNoSpace = SyslogdTestUtils.toByteBuffer("<14> Jan 22 12:39:25 localhost procname:this is [my] message");
			ConvertToEvent convertToEvent = new ConvertToEvent(
				MockDistPollerDao.DEFAULT_DIST_POLLER_ID,
				MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
				InetAddressUtils.ONE_TWENTY_SEVEN,
				514,
				colonNoSpace,
				config,
					locationAwareDnsLookupClient
			);
			assertEquals("procname:this is [my] message", convertToEvent.getEvent().getLogmsg().getContent());
		}

		{
			ByteBuffer spaceBeforeColon = SyslogdTestUtils.toByteBuffer("<14> Jan 22 12:39:25 localhost proc name: this is [my] message");
			ConvertToEvent convertToEvent = new ConvertToEvent(
				MockDistPollerDao.DEFAULT_DIST_POLLER_ID,
				MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
				InetAddressUtils.ONE_TWENTY_SEVEN,
				514,
				spaceBeforeColon,
				config,
					locationAwareDnsLookupClient
			);
			assertEquals("proc name: this is [my] message", convertToEvent.getEvent().getLogmsg().getContent());
		}
	}
}
