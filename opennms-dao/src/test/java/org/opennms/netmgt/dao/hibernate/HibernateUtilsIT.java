/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertTrue;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = false)
public class HibernateUtilsIT implements InitializingBean {

    @Autowired
    SessionFactory factory;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }


    @Before
    public void setUp() throws Exception {
    
    }

    @Test
    public void testGetHibernateColumnNames() {
        List<String> noPrefix = HibernateUtils.getHibernateTableColumnNames(factory, MockHibernateModel.class, false);
        assertTrue(noPrefix.size() == 3);
        assertTrue(noPrefix.contains("mockid"));
        assertTrue(noPrefix.contains("mockname"));
        assertTrue(noPrefix.contains("mockdescription"));

        List<String> withPrefix = HibernateUtils.getHibernateTableColumnNames(factory, MockHibernateModel.class, true);
        assertTrue(withPrefix.size() == 3);
        assertTrue(withPrefix.contains("mocktablename.mockid"));
        assertTrue(withPrefix.contains("mocktablename.mockname"));
        assertTrue(withPrefix.contains("mocktablename.mockdescription"));
    }

    @Test
    public void testValidateHibernateColumnNames() {
        // These should not throw an exception
        HibernateUtils.validateHibernateColumnNames(factory, MockHibernateModel.class, true,
                "mocktablename.mockid",
                "mocktablename.mockname",
                "mocktablename.mockdescription");
        HibernateUtils.validateHibernateColumnNames(factory, MockHibernateModel.class, false,
                "mockid",
                "mockname",
                "mockdescription");
    }

    @Test (expected = IllegalArgumentException.class)
    public void testValidateHibernateColumnNamesException() {
        HibernateUtils.validateHibernateColumnNames(factory, MockHibernateModel.class, false,
                "mockid",
                "mockname",
                "mockdescription",
                "not a valid column");
    }

}
