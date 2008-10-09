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
// Modifications:
//
// 2007 Feb 01: Make testCurrentByRange work again. - dj@opennms.org
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
package org.opennms.web.svclayer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.svclayer.outage.DefaultOutageService;

public class DefaultOutageServiceTest extends TestCase {

	DefaultOutageService outageService = new DefaultOutageService();

	private OutageDao outageDao;

	public void setUp() throws Exception {
		super.setUp();
		outageDao = createMock(OutageDao.class);
		outageService.setDao(outageDao);
	}

	public void testLoadOneOutage() {
		Integer outageId = new Integer(505);
		OnmsOutage outage = new OnmsOutage();

		outage.setId(505);

		expect(outageDao.load(outageId)).andReturn(outage);
		replay(outageDao);

		OnmsOutage outage2 = outageService.load(outageId);
		verify(outageDao);
		assertTrue("Yay we can load a single outage... ", outage
				.equals(outage2));

	}

	

	public void testGetCurrentOutageCount() {

		Integer expectedCount = new Integer(1);

		expect(outageDao.currentOutageCount()).andReturn(expectedCount);
		replay(outageDao);
		Integer count = outageService.getCurrentOutageCount();
		verify(outageDao);
		assertTrue("A good system should have outages ", count
				.equals(new Integer(1)));

	}

	public void testSuppressedOutageCount() {

		fail("Needs to be upgraded to hibernate");
//		Integer expectedCount = new Integer(1);
//
//		expect(outageDao.currentSuppressedOutageCount()).andReturn(
//				expectedCount);
//		replay(outageDao);
//		Integer count = outageService.getSuppressedOutageCount();
//		verify(outageDao);
//		assertTrue("All is suppressed ", count.equals(1));

	}

	public void testCurrentOutages() {
		Collection<OnmsOutage> expectedOutages = new HashSet<OnmsOutage>();
		OnmsOutage expectedCurrent = new OnmsOutage();
		expectedCurrent.setId(1);
		expectedOutages.add(expectedCurrent);

		expect(outageDao.currentOutages()).andReturn(expectedOutages);
        
		replay(outageDao);
		Collection<OnmsOutage> current = outageService.getCurrentOutages();
		verify(outageDao);
        
		assertTrue("Current Outages", current.equals(expectedOutages));
	}

	public void testSuppressedOutages() {
		
		fail("Needs to be upgraded to hibernate");

//	 	Collection<OnmsOutage> expectedOutages = new JdbcSet();
//		OnmsOutage expectedCurrent = new OnmsOutage();
//		expectedCurrent.setId(1);
//		expectedOutages.add(expectedCurrent);
//
//		expect(outageDao.suppressedOutages()).andReturn(expectedOutages);
//		replay(outageDao);
//
//		Set suppressed = (Set) outageService.getSuppressedOutages();
//		verify(outageDao);
//		assertTrue("Current Outages", suppressed.equals(expectedOutages));
	}

	public void testOpenAndResolved() {

		fail("Needs to be upgraded to hibernate");

//		Collection<OnmsOutage> expectedOutages = new JdbcSet();
//		OnmsOutage expectedCurrent = new OnmsOutage();
//		expectedCurrent.setId(1);
//		expectedOutages.add(expectedCurrent);
//
//		expect(outageDao.openAndResolvedOutages()).andReturn(expectedOutages);
//		replay(outageDao);
//
//		Set suppressed = (Set) outageService.getOpenAndResolved();
//		verify(outageDao);
//		assertTrue("Current Outages", suppressed.equals(expectedOutages));

	}

	public void testCurrentByRange() {
		List<OnmsOutage> expectedOutages = new LinkedList<OnmsOutage>();
		OnmsOutage expectedCurrent = new OnmsOutage();
		expectedCurrent.setId(1);
                expectedCurrent.setMonitoredService(new OnmsMonitoredService());
                expectedCurrent.getMonitoredService().setIpInterface(new OnmsIpInterface());
                expectedCurrent.getMonitoredService().getIpInterface().setNode(new OnmsNode());

		expectedOutages.add(expectedCurrent);

                OnmsCriteria criteria = new OnmsCriteria(OnmsOutage.class);
		expect(outageDao.findMatching(criteria)).andReturn(expectedOutages);
                
		replay(outageDao);
		Collection<OnmsOutage> outages = outageService.getOutagesByRange(1, 1, "iflostservice", "asc", criteria);
		verify(outageDao);
                
		assertTrue("Current Outages", outages.equals(expectedOutages));
	}

	public void testSuppressedByRange() {

		fail("Needs to be upgraded to hibernate");

//		Collection<OnmsOutage> expectedOutages = new JdbcSet();
//		OnmsOutage expectedCurrent = new OnmsOutage();
//		expectedCurrent.setId(1);
//		expectedOutages.add(expectedCurrent);
//
//		expect(outageDao.suppressedOutages(1, 1)).andReturn(expectedOutages);
//		replay(outageDao);
//
//		Set suppressed = (Set) outageService.getSuppressedOutagesByRange(1, 1);
//		verify(outageDao);
//		assertTrue("Current Outages", suppressed.equals(expectedOutages));

	}

	public void testGetOpenAndResolvedByRange() {

		fail("Needs to be upgraded to hibernate");

//		Collection<OnmsOutage> expectedOutages = new JdbcSet();
//		OnmsOutage expectedCurrent = new OnmsOutage();
//		expectedCurrent.setId(1);
//		expectedOutages.add(expectedCurrent);
//
//		expect(outageDao.findAll(1, 1)).andReturn(expectedOutages);
//		replay(outageDao);
//
//		Set suppressed = (Set) outageService.getOpenAndResolved(1, 1);
//		verify(outageDao);
//		assertTrue("Current Outages", suppressed.equals(expectedOutages));

	}

}
