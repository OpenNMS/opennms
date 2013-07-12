/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.daemonstatus.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.api.DaemonStatusDao;
import org.opennms.netmgt.model.MockServiceDaemon;
import org.opennms.netmgt.model.ServiceInfo;

public class DefaultDaemonStatusServiceTest extends TestCase {

	private DefaultDaemonStatusService defaultDaemonStatusService;

	private DaemonStatusDao daemonStatusDao;

        @Override
	protected void setUp() throws Exception {
		super.setUp();
		daemonStatusDao = createMock(DaemonStatusDao.class);
		defaultDaemonStatusService = new DefaultDaemonStatusService();
		defaultDaemonStatusService.setDaemonStatusDao(daemonStatusDao);
	}

	public List<ServiceInfo> testGetCurrentDaemonStatusNotEmpty() {
		return null;
	}

	public List<ServiceInfo> testGetCurrentDaemonStatusEmpty() {
		return null;
	}

	public void testStartDaemon() {
		Map<String, ServiceInfo> info2Return = new HashMap<String, ServiceInfo>();
		ServiceInfo sinfo1 = new ServiceInfo("ignore", "failed");
		info2Return.put("notifd", sinfo1);
		ServiceInfo sinfo2 = new ServiceInfo("notifd", "Started");
		info2Return.put("notifd", sinfo2);

		MockServiceDaemon dstub = new MockServiceDaemon("notifd");
		expect(daemonStatusDao.getServiceHandle("notifd")).andReturn(dstub);
		expect(daemonStatusDao.getCurrentDaemonStatus()).andReturn(info2Return); // expecting
												                                 // this
																					// method
																					// to
																					// be
																					// called
		replay(daemonStatusDao); // done recording

		Map<String, ServiceInfo> listServiceInfo = defaultDaemonStatusService
				.startDaemon("notifd");
		String status = listServiceInfo.get("notifd").getServiceStatus();

		verify(daemonStatusDao); // verify that I called everything that I
									// "expect" ed to
        assertTrue("Service not started", dstub.getStartCalled());
		assertEquals("status must be 'Started'", "Started", status);
	}

	public void testStopDaemon() {

	}

	public void testRestartDaemon() {

	}

	public void testPerformOperationOnDaemonsEmptySetOfDaemons() {
		// TODO Auto-generated method stub

	}

	public void testPerformOperationOnDaemonsNonEmptySetOfDaemons() {
		// TODO Auto-generated method stub

	}

	public void testPerformOperationOnDaemonsNonEmptySetOfDaemonsInvalidOperation() {
		// TODO Auto-generated method stub

	}
}
