package org.opennms.netmgt.dao;

import java.util.Date;

import org.opennms.netmgt.model.AvailabilityReportLocator;

public class AvailabilityReportLocatorDaoTest extends AbstractDaoTestCase {

    public void setUp() throws Exception {
        //setPopulate(false);
        super.setUp();
    }
    
	/*private AvailabilityReportLocatorDao m_availabilityReportLocatorDao;
        
        public AvailabilityReportLocatorDaoTest() throws MarshalException, ValidationException, IOException, PropertyVetoException, SQLException {
            
             * Note: I'm using the opennms-database.xml file in target/classes/etc
             * so that it has been filtered first.
             
            DataSourceFactory.setInstance(new C3P0ConnectionFactory("../opennms-daemon/target/classes/etc/opennms-database.xml"));
        }

	public void setAvailabilityReportLocatorDao(AvailabilityReportLocatorDao availabilityReportLocatorDao) {
		m_availabilityReportLocatorDao = availabilityReportLocatorDao;
	}*/
	
	public void testBogus() {
		// do nothing... we're here so JUnit doesn't complain
	}

	public void testFindAll() {
		
		System.out.println("going for the report locator");
		AvailabilityReportLocator locator = new AvailabilityReportLocator();
		System.out.println("got the report locator");
		locator.setAvailable(true);
		locator.setCategory("cat1");
		locator.setDate(new Date());
		locator.setFormat("HTML");
		locator.setType("Random String");
		locator.setLocation("down the back of the sofa");
		
		getAvailabilityReportLocatorDao().save(locator);
		
		
		AvailabilityReportLocator retrieved = getAvailabilityReportLocatorDao().get(locator.getId());
		
		assertEquals(retrieved.getId(), locator.getId());
		assertEquals(retrieved.getAvailable(), locator.getAvailable());
		assertEquals(retrieved.getCategory(), locator.getCategory());
	}
	
}
