package org.opennms.web.svclayer;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public class DefaultOutageServiceIntegrationTest extends
		AbstractTransactionalDataSourceSpringContextTests {

	private static final int RANGE_LIMIT = 10;
	private OutageService outageService;
	
	public void setOutageService(OutageService outageService) {
		this.outageService = outageService;
	}
	
	@Override
	protected String[] getConfigLocations() {
		return new String[] {
				"org/opennms/web/svclayer/applicationContextOutage-web.xml" };
	}

	public void testGet10Outages() {
		Collection<OnmsOutage> outages = outageService.getCurrenOutagesByRange(1,RANGE_LIMIT);
		assertFalse("Collection should not be emtpy", outages.isEmpty());
		assertEquals("Collection should be of size" +RANGE_LIMIT, 10, outages.size());
	}

}
