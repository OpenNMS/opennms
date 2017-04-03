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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;

/**
 * @author Seth
 */
public class Nms6730Test {

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
					DistPollerDao.DEFAULT_DIST_POLLER_ID,
					MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
					InetAddressUtils.ONE_TWENTY_SEVEN,
					514,
					incoming,
					config
				);
				assertEquals("this is [my] message", convertToEvent.getEvent().getLogmsg().getContent());
			} catch (MessageDiscardedException e) {
				fail("Message parsing failed: " + e.getMessage());
			}
		}


		{
			ByteBuffer colonNoSpace = SyslogdTestUtils.toByteBuffer("<14> 2001-01-01 localhost procname:this is [my] message");
			ConvertToEvent convertToEvent = new ConvertToEvent(
				DistPollerDao.DEFAULT_DIST_POLLER_ID,
				MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
				InetAddressUtils.ONE_TWENTY_SEVEN,
				514,
				colonNoSpace,
				config
			);
			assertEquals("procname:this is [my] message", convertToEvent.getEvent().getLogmsg().getContent());
		}

		{
			ByteBuffer spaceBeforeColon = SyslogdTestUtils.toByteBuffer("<14> 2001-01-01 localhost proc name: this is [my] message");
			ConvertToEvent convertToEvent = new ConvertToEvent(
				DistPollerDao.DEFAULT_DIST_POLLER_ID,
				MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
				InetAddressUtils.ONE_TWENTY_SEVEN,
				514,
				spaceBeforeColon,
				config
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
					DistPollerDao.DEFAULT_DIST_POLLER_ID,
					MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
					InetAddressUtils.ONE_TWENTY_SEVEN,
					514,
					incoming,
					config
				);
				assertEquals("this is [my] message", convertToEvent.getEvent().getLogmsg().getContent());
			} catch (MessageDiscardedException e) {
				fail("Message parsing failed: " + e.getMessage());
			}
		}

		{
			ByteBuffer colonNoSpace = SyslogdTestUtils.toByteBuffer("<14> Jan 22 12:39:25 localhost procname:this is [my] message");
			ConvertToEvent convertToEvent = new ConvertToEvent(
				DistPollerDao.DEFAULT_DIST_POLLER_ID,
				MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
				InetAddressUtils.ONE_TWENTY_SEVEN,
				514,
				colonNoSpace,
				config
			);
			assertEquals("procname:this is [my] message", convertToEvent.getEvent().getLogmsg().getContent());
		}

		{
			ByteBuffer spaceBeforeColon = SyslogdTestUtils.toByteBuffer("<14> Jan 22 12:39:25 localhost proc name: this is [my] message");
			ConvertToEvent convertToEvent = new ConvertToEvent(
				DistPollerDao.DEFAULT_DIST_POLLER_ID,
				MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
				InetAddressUtils.ONE_TWENTY_SEVEN,
				514,
				spaceBeforeColon,
				config
			);
			assertEquals("proc name: this is [my] message", convertToEvent.getEvent().getLogmsg().getContent());
		}
	}
}
