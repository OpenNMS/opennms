/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.correlation.drools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:META-INF/opennms/applicationContext-soa.xml",
        "classpath:META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:META-INF/opennms/applicationContext-daemon.xml",
        "classpath:META-INF/opennms/mockEventIpcManager.xml",
        "classpath:META-INF/opennms/applicationContext-correlator.xml",
        "classpath:test-context.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DroolsCorrelationEngineBuilderTest implements InitializingBean {
    @Autowired
    private DroolsCorrelationEngineBuilder m_droolsCorrelationEngineBuilder;
    @Autowired
    private CorrelationEngineRegistrar m_mockCorrelator;

    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_droolsCorrelationEngineBuilder);
        assertNotNull(m_mockCorrelator);
    }

    @Test
    public void testIt() throws Exception {
        List<CorrelationEngine> engines = m_mockCorrelator.getEngines();
        assertNotNull(engines);
        assertEquals(4, m_mockCorrelator.getEngines().size());
        assertTrue(engines.get(0) instanceof DroolsCorrelationEngine);
        assertTrue(m_mockCorrelator.findEngineByName("locationMonitorRules") instanceof DroolsCorrelationEngine);
        DroolsCorrelationEngine engine = (DroolsCorrelationEngine) m_mockCorrelator.findEngineByName("locationMonitorRules");
        assertEquals(2, engine.getInterestingEvents().size());
        assertTrue(engine.getInterestingEvents().contains(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI));
        assertTrue(engine.getInterestingEvents().contains(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI));
    }
}
