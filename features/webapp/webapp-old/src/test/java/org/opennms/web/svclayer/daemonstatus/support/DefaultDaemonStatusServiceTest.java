//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.svclayer.daemonstatus.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.DaemonStatusDao;
import org.opennms.netmgt.dao.ServiceInfo;
import org.opennms.netmgt.dao.jmx.ServiceDaemonStub;

public class DefaultDaemonStatusServiceTest extends TestCase {

	private DefaultDaemonStatusService defaultDaemonStatusService;

	private DaemonStatusDao daemonStatusDao;

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

		ServiceDaemonStub dstub = new ServiceDaemonStub("notifd");
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
