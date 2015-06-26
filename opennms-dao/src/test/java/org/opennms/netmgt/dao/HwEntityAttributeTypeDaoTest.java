/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao;
import org.opennms.netmgt.model.HwEntityAttributeType;
import org.opennms.test.JUnitConfigurationEnvironment;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HwEntityAttributeTypeDaoTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class HwEntityAttributeTypeDaoTest implements InitializingBean {

    /** The m_hw entity attribute type dao. */
    @Autowired
    HwEntityAttributeTypeDao m_hwEntityAttributeTypeDao;

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        org.opennms.core.spring.BeanUtils.assertAutowiring(this);
    }

    /**
     * Test save type.
     */
    @Test
    @Transactional
    public void testEntityCycle() {
        HwEntityAttributeType ram = new HwEntityAttributeType(".1.3.6.1.4.1.9.9.195.1.1.1.1", "ram", "integer");
        m_hwEntityAttributeTypeDao.save(ram);
        m_hwEntityAttributeTypeDao.flush();

        HwEntityAttributeType type = m_hwEntityAttributeTypeDao.findTypeByName("ram");
        assertNotNull(type);
        assertEquals("ram", type.getName());
        assertEquals(".1.3.6.1.4.1.9.9.195.1.1.1.1", type.getOid());
        assertEquals("integer", type.getAttributeClass());
        assertEquals(new Integer(1), type.getId());

        m_hwEntityAttributeTypeDao.flush();

        m_hwEntityAttributeTypeDao.delete(type.getId());
    }

}
