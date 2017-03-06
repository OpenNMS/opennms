/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.foreignsource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.foreignsource.ForeignSourceEntity;
import org.opennms.netmgt.provision.persist.ForeignSourceService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionImplementationIT;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/testForeignSourceContext.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
})
@JUnitConfigurationEnvironment
@Transactional
public class ForeignSourceImplementationIT implements InitializingBean, ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(RequisitionImplementationIT.class);

    private Map<String, ForeignSourceService> m_repositories;

    @Before
    @After
    public void cleanUp() throws Exception {
        if (m_repositories != null) {
            for (final ForeignSourceService service : m_repositories.values()) {
                service.getAllForeignSources().forEach(fs -> service.deleteForeignSource(fs.getName()));
            }
        }
        LOG.info("Test context prepared.");
    }

    interface RepositoryTest<T> {
        void test(T t);
    }

    protected <T> void runTest(final RepositoryTest<ForeignSourceService> rt) {
        m_repositories.entrySet().stream().forEach(entry -> {
            final String bundleName = entry.getKey();
            final ForeignSourceService fsr = entry.getValue();
            LOG.info("=== " + bundleName + " ===");
            fsr.resetDefaultForeignSource();
            rt.test(fsr);
        });
    }

    @Test
    public void testCreateSimpleForeignSource() {
        runTest(
                fsr -> {
                    ForeignSourceEntity fs = fsr.getForeignSource("blah");
                    fs.setDefault(false);
                    fsr.saveForeignSource(fs);
                    fs = fsr.getForeignSource("blah");
                    assertNotNull(fs);
                    assertNotNull(fs.getScanInterval());
                }
        );
    }

    @Test
    public void testForeignSourceWithSpace() {
        runTest(
                fsr -> {
                    final ForeignSourceEntity fs = fsr.getForeignSource("foo bar");
                    fs.setDefault(false);
                    fsr.saveForeignSource(fs);
                    final ForeignSourceEntity saved = fsr.getForeignSource("foo bar");
                    assertNotNull(saved);
                    assertEquals(fs, saved);
                }
        );
    }

    @Test
    public void testForeignSourceWithSlash() {
        runTest(
                fsr -> {
                    final ForeignSourceEntity fs = fsr.getForeignSource("foo/bar");
                    fs.setDefault(false);
                    fsr.saveForeignSource(fs);
                    final ForeignSourceEntity saved = fsr.getForeignSource("foo/bar");
                    assertNotNull(saved);
                    assertEquals(fs, saved);
                }
        );
    }


    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        m_repositories = context.getBeansOfType(ForeignSourceService.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_repositories);
    }
}
