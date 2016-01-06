/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.remote.support.ScanReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanReportTest {
	private static final Logger LOG = LoggerFactory.getLogger(ScanReportTest.class);

	/**
	 * Test the JAXB marshalling of the {@link ScanReport} class.
	 */
	@Test
	public void testSerialization() throws Exception {
		ScanReport report = new ScanReport();
		report.setCustomerAccountNumber("12345");
		report.setCustomerName("Zombo.com");
		report.setLocale("en-US");
		report.setLocation("RDU");
		report.setMonitoringSystem(UUID.randomUUID().toString());
		report.setReferenceId("ABZ135");
		report.setTimestamp(new Date());
		report.setTimeZone("-5:00");
		for (int i = 0; i < 5; i++) {
			PollStatus status = PollStatus.get(PollStatus.SERVICE_AVAILABLE, "Anything is possible", 4.5d);
			status.setProperty("whatever", 2.0);
			report.addPollStatus(status);
		}

		String reportString = JaxbUtils.marshal(report);
		LOG.debug("Report string: \n " + reportString);

		assertTrue(reportString.contains("customer-account-number=\"12345\""));
		assertTrue(reportString.contains("customer-name=\"Zombo.com\""));
		assertTrue(reportString.contains("response-time=\"4.5\""));
	}
}
