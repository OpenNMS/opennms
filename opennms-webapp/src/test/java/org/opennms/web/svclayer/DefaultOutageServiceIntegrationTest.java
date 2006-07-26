package org.opennms.web.svclayer;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public class DefaultOutageServiceIntegrationTest extends
		AbstractTransactionalDataSourceSpringContextTests {

//	private OutageDao outageDao = OutageDaoJdbc ();

//	DefaultOutageService outageService = new DefaultOutageService(outageDao);
	
	private OutageService outageService;
	
	public void setOutageService(OutageService outageService) {
		this.outageService = outageService;
	}
	
	

	@Override
	protected String[] getConfigLocations() {
		return new String[] {
				"org/opennms/netmgt/dao/jdbc/outage/applicationContextOutage.xml" };
	}

	public void testGet10Outages() {
		Collection<OnmsOutage> outages = outageService.getCurrenOutagesByRange(1,10);
		assertFalse("Collection should not be emtpy", outages.isEmpty());
		
//		Collection outages = outageDao.findAll();
//		assertFalse("Collection should not be empty", outages.isEmpty());
	}

}
