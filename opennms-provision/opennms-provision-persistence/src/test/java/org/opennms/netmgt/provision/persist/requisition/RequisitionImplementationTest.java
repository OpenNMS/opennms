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
        "classpath:/testForeignSourceContext.xml"
})
@JUnitConfigurationEnvironment
@Transactional
public class RequisitionImplementationTest implements InitializingBean, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(RequisitionImplementationTest.class);

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

    interface RepositoryTest<T> {
        void test(T t);
    }

    protected <T> void runTest(final RepositoryTest<RequisitionService> rt, final Class<? extends Throwable> expected) {
        m_repositories.entrySet().stream().forEach(entry -> {
            final String bundleName = entry.getKey();
            final RequisitionService fsr = entry.getValue();
            LOG.info("=== " + bundleName + " ===");
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
                    try {
                        RequisitionEntity req = toPersistenceModel(JAXB.unmarshal(new ClassPathResource("/requisition-test.xml").getURL(), Requisition.class));
                        fsr.saveOrUpdateRequisition(req);
                        req = fsr.getRequisition("imported:");
                        assertNotNull(req);
                        assertEquals(2, req.getNodes().size());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                null
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
                },
                null
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
                // TOOD MVR was ForeignSourceRepositoryException but now is RuntimeException, what to do?
//                ForeignSourceRepositoryException.class
                RuntimeException.class
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
