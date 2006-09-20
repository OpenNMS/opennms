package org.opennms.netmgt.dao;

import java.util.Date;

import org.opennms.netmgt.model.AvailabilityReportLocator;

public class AvailabilityReportLocatorDaoTest extends BaseDaoTestCase {

	private AvailabilityReportLocatorDao m_availabilityReportLocatorDao;

	public void setAvailabilityReportLocatorDao(AvailabilityReportLocatorDao availabilityReportLocatorDao) {
		m_availabilityReportLocatorDao = availabilityReportLocatorDao;
	}
	
	public void testBogus() {
		// do nothing... we're here so JUnit doesn't complain
	}

	public void FIXMEtestFindAll() {
		
		AvailabilityReportLocator locator = new AvailabilityReportLocator();
		locator.setAvailable(true);
		locator.setCategory("cat1");
		locator.setDate(new Date());
		locator.setFormat("HTML");
		locator.setType("Random String");
		
		m_availabilityReportLocatorDao.save(locator);
		
		
		AvailabilityReportLocator retrieved = m_availabilityReportLocatorDao.get(locator.getId());
		
		assertEquals(retrieved.getId(), locator.getId());
		assertEquals(retrieved.getAvailable(), locator.getAvailable());
		assertEquals(retrieved.getCategory(), locator.getCategory());
	}
	
}
