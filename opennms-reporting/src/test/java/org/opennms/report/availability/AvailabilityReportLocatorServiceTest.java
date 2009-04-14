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
package org.opennms.report.availability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.dao.AvailabilityReportLocatorDao;
import org.opennms.netmgt.model.AvailabilityReportLocator;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

public class AvailabilityReportLocatorServiceTest extends TestCase {

	AvailabilityReportLocatorService locatorService;
	private AvailabilityReportLocatorDao availabilityReportLocatorDao;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		availabilityReportLocatorDao = createMock(AvailabilityReportLocatorDao.class);
		locatorService = new AvailabilityReportLocatorService();
		locatorService.setAvailabilityReportLocatorDao(availabilityReportLocatorDao);
		
		
		
		
	}
	
	public void testDelete() {
		
		// record expected calls
		availabilityReportLocatorDao.deleteById(1);
		
		// tell mock to match up actual call to expected calls
		replay(availabilityReportLocatorDao);
		
		// call service method that makes call to mock
		locatorService.deleteReport(1);
		
		// verify that all calls matched
		verify(availabilityReportLocatorDao);
	}
	
	public void testAdd() {
		
		AvailabilityReportLocator locator = new AvailabilityReportLocator();
		
//		record expected calls
		availabilityReportLocatorDao.save(locator);
		
		// tell mock to match up actual call to expected calls
		replay(availabilityReportLocatorDao);
		
		// call service method that makes call to mock
		locatorService.addReport(locator);
		
		// verify that all calls matched
		verify(availabilityReportLocatorDao);
		
	}
	
	public void testLocateReports() {
		
		List<AvailabilityReportLocator> expectedReports = new ArrayList<AvailabilityReportLocator>();
		
		expect(availabilityReportLocatorDao.findAll()).andReturn(expectedReports);
		
		replay(availabilityReportLocatorDao);
		
		Collection<AvailabilityReportLocator> actualReports = locatorService.locateReports();
		
		verify(availabilityReportLocatorDao);
		
		assertSame("Expected loctedReports to be the same as the dao list", expectedReports, actualReports);
	}
	
	

}
