/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.MinionPropertyDao;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.model.minion.OnmsMinionProperty;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
    "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
    "classpath*:/META-INF/opennms/component-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = true)
public class MinionDaoTest {
    @Autowired
    private MinionDao m_minionDao;
    
    @Autowired
    private MinionPropertyDao m_minionPropertyDao;

    @Before
    public void setUp() throws Exception {
        final Collection<OnmsMinion> minions = m_minionDao.findAll();
        for (final OnmsMinion minion : minions) {
            m_minionDao.delete(minion);
        }
        m_minionDao.flush();
    }

    @Test
    public void testQueryByLocation() throws Exception {
        final Date now = new Date();
        m_minionDao.save(new OnmsMinion(UUID.randomUUID().toString(), "TestLocation", "Started", now));
        m_minionDao.save(new OnmsMinion(UUID.randomUUID().toString(), "TestLocation", "Stopped", now));
        m_minionDao.save(new OnmsMinion(UUID.randomUUID().toString(), "OtherLocation", "Stopped", now));
        
        final Collection<OnmsMinion> testMinions = m_minionDao.findByLocation("TestLocation");
        assertEquals(2, testMinions.size());
    }

    @Test
    public void testProperties() throws Exception {
        final Date now = new Date();
        
        final OnmsMinion a = new OnmsMinion(UUID.randomUUID().toString(), "TestLocation", "Started", now);
        final OnmsMinion b = new OnmsMinion(UUID.randomUUID().toString(), "OtherLocation", "Started", now);
        a.setProperty("Yes", "No");
        a.setProperty("Up", "Down");
        b.setProperty("Left", "Right");
        b.setProperty("Wrong",  "Right");
        
        m_minionDao.save(a);
        m_minionDao.save(b);
        m_minionDao.flush();
        
        Collection<OnmsMinionProperty> props = m_minionPropertyDao.findAll();
        assertEquals(4, props.size());
        
        props = m_minionPropertyDao.findByMinionId(a.getId());
        assertEquals(2, props.size());
        assertEquals(a.getId(), props.iterator().next().getMinion().getId());
        
        OnmsMinionProperty prop = m_minionPropertyDao.findByKey(a.getId(), "Left");
        // a doesn't have that property, b does
        assertNull(prop);
        prop = m_minionPropertyDao.findByKey(b.getId(), "Left");
        assertNotNull(prop);
        assertEquals("Right", prop.getValue());
    }
}
