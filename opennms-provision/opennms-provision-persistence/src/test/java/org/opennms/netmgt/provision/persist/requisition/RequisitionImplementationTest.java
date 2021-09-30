/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.requisition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/testForeignSourceContext.xml"
})
@JUnitConfigurationEnvironment
public class RequisitionImplementationTest implements InitializingBean, ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(RequisitionImplementationTest.class);

    private Map<String, ForeignSourceRepository> m_repositories;

    @Before
    @After
    public void cleanUp() throws Exception {
        if (m_repositories != null) {
            resetDirectories();
            for (final ForeignSourceRepository fsr : m_repositories.values()) {
                fsr.flush();
                fsr.clear();
            }
        }
        LOG.info("Test context prepared.");
    }

    private void resetDirectories() throws IOException {
        FileUtils.deleteDirectory(new File("target/opennms-home/etc/imports"));
        FileUtils.forceMkdir(new File("target/opennms-home/etc/imports/pending"));
        FileUtils.deleteDirectory(new File("target/opennms-home/etc/foreign-sources"));
        FileUtils.forceMkdir(new File("target/opennms-home/etc/foreign-sources/pending"));
    }

    interface RepositoryTest<T> {
        void test(T t);
    }

    protected <T> void runTest(final RepositoryTest<ForeignSourceRepository> rt, final Class<? extends Throwable> expected) {
        m_repositories.entrySet().stream().forEach(entry -> {
            final String bundleName = entry.getKey();
            final ForeignSourceRepository fsr = entry.getValue();
            LOG.info("=== " + bundleName + " ===");
            fsr.resetDefaultForeignSource();
            fsr.flush();
            try {
                rt.test(fsr);
            } catch (final Throwable t) {
                if (expected == null) {
                    // we didn't expect a failure, but got one... rethrow it
                    throw t;
                } else {
                    LOG.debug("expected: {}, got: {}", expected, t);
                    if (t.getClass().getCanonicalName().equals(expected.getCanonicalName())) {
                        // we got the exception we expected, carry on
                    } else {
                        throw new RuntimeException("Expected throwable " + expected.getName() + " when running test against " + bundleName + ", but got " + t.getClass() + " instead!", t);
                    }
                }
            }
        });
    }

    @Test
    public void testCreateSimpleRequisition() {
        runTest(
                fsr -> {
                    Requisition req = fsr.importResourceRequisition(new ClassPathResource("/requisition-test.xml"));
                    fsr.save(req);
                    fsr.flush();
                    req = fsr.getRequisition("imported:");
                    assertNotNull(req);
                    assertEquals(2, req.getNodeCount());
                },
                null
                );
    }

    @Test
    public void testCreateSimpleForeignSource() {
        runTest(
                fsr -> {
                    ForeignSource fs = fsr.getForeignSource("blah");
                    fs.setDefault(false);
                    fsr.save(fs);
                    fsr.flush();
                    fs = fsr.getForeignSource("blah");
                    assertNotNull(fs);
                    assertNotNull(fs.getScanInterval());
                },
                null
                );
    }

    @Test
    public void testRequisitionWithSpace() {
        runTest(
                fsr -> {
                    final Requisition req = new Requisition("foo bar");
                    req.setDate(new Date(0));
                    fsr.save(req);
                    fsr.flush();
                    final Requisition saved = fsr.getRequisition("foo bar");
                    assertNotNull(saved);
                    assertEquals(req, saved);
                },
                null
                );
    }

    @Test
    public void testRequisitionWithSlash() {
        runTest(
                fsr -> {
                    final Requisition req = new Requisition("foo/bar");
                    req.setForeignSource("foo/bar");
                    fsr.save(req);
                },
                ForeignSourceRepositoryException.class
                );
    }

    @Test
    public void testForeignSourceWithSpace() {
        runTest(
                fsr -> {
                    final ForeignSource fs = fsr.getForeignSource("foo bar");
                    fs.setDefault(false);
                    fsr.save(fs);
                    fsr.flush();
                    final ForeignSource saved = fsr.getForeignSource("foo bar");
                    assertNotNull(saved);
                    assertEquals(fs, saved);
                },
                null
                );
    }

    @Test
    public void testForeignSourceWithSlash() {
        runTest(
                fsr -> {
                    final ForeignSource fs = fsr.getForeignSource("foo/bar");
                    fs.setDefault(false);
                    fsr.save(fs);
                    fsr.flush();
                    final ForeignSource saved = fsr.getForeignSource("foo/bar");
                    assertNotNull(saved);
                    assertEquals(fs, saved);
                },
                ForeignSourceRepositoryException.class
                );
    }

    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        m_repositories = context.getBeansOfType(ForeignSourceRepository.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_repositories);
    }
}
