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

package org.opennms.netmgt.provision.persist.requisition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opennms.netmgt.provision.persist.requisition.RequisitionMapper.toPersistenceModel;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.provision.persist.RequisitionService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
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
public class RequisitionImplementationIT implements InitializingBean, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(RequisitionImplementationIT.class);

    private Map<String, RequisitionService> m_repositories;

    @Before
    @After
    public void cleanUp() throws Exception {
        if (m_repositories != null) {
            for (final RequisitionService requisitionService : m_repositories.values()) {
                requisitionService.getRequisitions().forEach(r -> requisitionService.deleteRequisition(r.getForeignSource()));
            }
        }
        LOG.info("Test context prepared.");
    }

    interface RepositoryTest {
        void test(RequisitionService rs);
    }

    protected void runTest(final RepositoryTest rt) {
        runTest(rt, null);
    }

    private void runTest(final RepositoryTest rt, final Class<? extends Exception> expectedException) {
        m_repositories.entrySet().stream().forEach(entry -> {
            final String bundleName = entry.getKey();
            final RequisitionService fsr = entry.getValue();
            LOG.info("=== " + bundleName + " ===");
            try {
                rt.test(fsr);
            } catch (Exception ex) {
                if (expectedException != null) {
                    LOG.debug("expected: {}, got: {}", expectedException, ex);
                    if (!ex.getClass().getCanonicalName().equals(expectedException.getCanonicalName())) {
                        throw new RuntimeException("Expected throwable " + expectedException.getName() + " when running test against " + bundleName + ", but got " + ex.getClass() + " instead!", ex);
                    }
                } else {
                    // we didn't expect a failure, but got one... rethrow it
                    throw ex;
                }
            }
        });
    }

    @Test
    public void testCreateSimpleRequisition() {
        runTest(
                fsr -> {
                    try {
                        RequisitionEntity req = toPersistenceModel(JAXB.unmarshal(new ClassPathResource("/requisition-test.xml").getURL(), Requisition.class));
                        fsr.saveOrUpdateRequisition(req);
                        req = fsr.getRequisition("imported:");
                        assertNotNull(req);
                        assertEquals(2, req.getNodes().size());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }


    @Test
    public void testRequisitionWithSpace() {
        runTest(
                fsr -> {
                    final RequisitionEntity req = new RequisitionEntity("foo bar");
                    req.setLastUpdate(new Date(0));
                    fsr.saveOrUpdateRequisition(req);
                    final RequisitionEntity saved = fsr.getRequisition("foo bar");
                    assertNotNull(saved);
                    assertEquals(req, saved);
                }
        );
    }

    @Test
    public void testRequisitionWithSlash() {
        runTest(
                fsr -> {
                    final RequisitionEntity req = new RequisitionEntity("foo/bar");
                    req.setForeignSource("foo/bar");
                    fsr.saveOrUpdateRequisition(req);
                },
                IllegalStateException.class
        );
    }

    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        m_repositories = context.getBeansOfType(RequisitionService.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_repositories);
    }
}
