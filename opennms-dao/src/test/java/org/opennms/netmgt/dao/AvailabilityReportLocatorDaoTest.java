package org.opennms.netmgt.dao;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.C3P0ConnectionFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.model.AvailabilityReportLocator;

public class AvailabilityReportLocatorDaoTest extends BaseDaoTestCase {

	private AvailabilityReportLocatorDao m_availabilityReportLocatorDao;
        
        public AvailabilityReportLocatorDaoTest() throws MarshalException, ValidationException, IOException, PropertyVetoException, SQLException {
            /*
             * Note: I'm using the opennms-database.xml file in target/classes/etc
             * so that it has been filtered first.
             */
            DataSourceFactory.setInstance(new C3P0ConnectionFactory("../opennms-daemon/target/classes/etc/opennms-database.xml"));
        }

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
