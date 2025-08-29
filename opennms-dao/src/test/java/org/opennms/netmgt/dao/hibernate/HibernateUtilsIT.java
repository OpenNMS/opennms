/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.hibernate;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.AbstractJRobinIT;
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
public class HibernateUtilsIT extends AbstractJRobinIT implements InitializingBean {

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
