package org.opennms.web.svclayer;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public class DefaultOutageServiceIntegrationTest extends
		AbstractTransactionalDataSourceSpringContextTests {

	private static final int RANGE_LIMIT = 5;
	private OutageService outageService;

	/**
	 * This get autowired by the base class
	 * @param outageService
	 */
	public void setOutageService(OutageService outageService) {
		this.outageService = outageService;
	}
	
	@Override
	protected String[] getConfigLocations() {
		return new String[] {
				"org/opennms/web/svclayer/applicationContextOutage-web.xml" };
	}

	public void testGetRangeOutages() {
		Collection<OnmsOutage> outages = outageService.getCurrenOutagesByRange(1,RANGE_LIMIT);
		assertFalse("Collection should not be emtpy", outages.isEmpty());
		assertEquals("Collection should be of size " + RANGE_LIMIT, RANGE_LIMIT, outages.size());
	}
	
	// More tests should be defined for these
	
	public void testGetSupressedOutages() {
		Collection<OnmsOutage> outages = outageService.getSuppressedOutages();
		assertTrue("Collection should be emtpy ", outages.isEmpty());
		
	}
	
	public void testNoOfSuppressedOutages(){
		Integer outages = outageService.getSuppressedOutageCount();
		assertTrue("We should find suppressed messages ", outages == 0);
	}

}
