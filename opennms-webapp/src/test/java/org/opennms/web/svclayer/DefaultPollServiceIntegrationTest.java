/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.svclayer;

import static org.easymock.EasyMock.createMock;

import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.model.DemandPoll;
import org.opennms.test.WebAppTestConfigBean;
import org.opennms.web.services.PollerService;
import org.opennms.web.svclayer.support.DefaultDemandPollService;

public class DefaultPollServiceIntegrationTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {

	private DemandPollService m_demandPollService;
        
	
	@Override
    protected void setUpConfiguration() {
        WebAppTestConfigBean webAppTestConfig = new WebAppTestConfigBean();
        webAppTestConfig.afterPropertiesSet();
    }

    @Override
	protected String[] getConfigLocations() {
		return new String[] {
				"META-INF/opennms/applicationContext-dao.xml",
                "META-INF/opennms/component-dao.xml",
				"org/opennms/web/svclayer/applicationContext-svclayer.xml",
		};
	}

	public DemandPollService getDemandPollService() {
		return m_demandPollService;
	}

	public void setDemandPollService(DemandPollService pollService) {
		m_demandPollService = pollService;
	}

	public void testBogus() {
	    // Empty test to keep JUnit from complaining about no tests
	}
    
    // FIXME this is a feature that has not been written yet
	public void FIXMEtestPollMonitoredService() {
		PollerService api = createMock(PollerService.class);
		((DefaultDemandPollService)m_demandPollService).setPollerAPI(api);
		
		DemandPoll poll = m_demandPollService.pollMonitoredService(1, "192.168.2.100", 1, 1);
		assertNotNull("DemandPoll should not be null", poll);
		assertTrue("Polled service addr doesn't match...", poll.getId() >= 1);
	}
}
