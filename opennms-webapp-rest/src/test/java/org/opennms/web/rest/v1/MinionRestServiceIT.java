/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class MinionRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    MinionDao m_minionDao;

    @Override
    protected void afterServletStart() throws Exception {

        final OnmsMinion minion = new OnmsMinion("12345", "Here", "Started", new Date());
        minion.setProperty("Foo", "Bar");
        m_minionDao.save(minion);
        m_minionDao.save(new OnmsMinion("23456", "There", "Stopped", new Date()));
        m_minionDao.flush();
    }

    @Override
    protected void beforeServletDestroy() throws Exception {
        final Collection<OnmsMinion> minions = m_minionDao.findAll();
        for (final OnmsMinion minion : minions) {
            m_minionDao.delete(minion);
        }
        m_minionDao.flush();
    }

    @Test
    public void testMinions() throws Exception {
        final String xml = sendRequest(GET, "/minions", 200);
        assertTrue(xml, xml.contains("id=\"12345\""));
        assertTrue(xml, xml.contains("id=\"23456\""));
        assertTrue(xml, xml.contains("<key>Foo</key>"));
        assertTrue(xml, xml.contains("<value>Bar</value>"));
    }
    
    @Test
    public void testGetMinion() throws Exception {
        String xml = sendRequest(GET, "/minions/12345", 200);
        assertTrue(xml, xml.contains("id=\"12345\""));
        assertFalse(xml, xml.contains("id=\"23456\""));
        assertTrue(xml, xml.contains("<key>Foo</key>"));
        assertTrue(xml, xml.contains("<value>Bar</value>"));
        
        xml = sendRequest(GET, "/minions/23456", 200);
        assertFalse(xml, xml.contains("id=\"12345\""));
        assertTrue(xml, xml.contains("id=\"23456\""));
        assertFalse(xml, xml.contains("<key>Foo</key>"));
        assertFalse(xml, xml.contains("<value>Bar</value>"));
    }

    @Test
    public void testGetProperty() throws Exception {
        String xml = sendRequest(GET, "/minions/12345/Foo", 200);
        assertFalse(xml, xml.contains("id=\"12345\""));
        assertEquals("Bar", xml);
    }
}
