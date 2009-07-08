/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.correlation.drools;

import java.util.List;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.test.DaoTestConfigBean;

public class DroolsCorrelationEngineBuilderTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private DroolsCorrelationEngineBuilder m_droolsCorrelationEngineBuilder;
    private CorrelationEngineRegistrar m_mockCorrelator;
    
    @Override
    protected void setUpConfiguration() {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/opennms-home");
        daoTestConfig.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {

        return new String[] {
                "classpath:META-INF/opennms/applicationContext-dao.xml",
                "classpath:META-INF/opennms/applicationContext-daemon.xml",
                "classpath:META-INF/opennms/mockEventIpcManager.xml",
                "classpath:META-INF/opennms/applicationContext-correlator.xml",
                "classpath:META-INF/opennms/correlation-engine.xml",
        };
    }

    public void testIt() throws Exception {
        assertNotNull(m_droolsCorrelationEngineBuilder);
        List<CorrelationEngine> engines = m_mockCorrelator.getEngines();
        assertNotNull(engines);
        assertEquals(2, m_mockCorrelator.getEngines().size());
        assertTrue(engines.get(0) instanceof DroolsCorrelationEngine);
        assertTrue(m_mockCorrelator.findEngineByName("locationMonitorRules") instanceof DroolsCorrelationEngine);
        DroolsCorrelationEngine engine = (DroolsCorrelationEngine) m_mockCorrelator.findEngineByName("locationMonitorRules");
        assertEquals(2, engine.getInterestingEvents().size());
        assertTrue(engine.getInterestingEvents().contains(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI));
        assertTrue(engine.getInterestingEvents().contains(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI));
    }
    
    public void setDroolsCorrelationEngineBuilder(DroolsCorrelationEngineBuilder bean) {
        m_droolsCorrelationEngineBuilder = bean;
    }
    
    public void setMockCorrelator(CorrelationEngineRegistrar mockCorrelator) {
        m_mockCorrelator = mockCorrelator;
    }
}
