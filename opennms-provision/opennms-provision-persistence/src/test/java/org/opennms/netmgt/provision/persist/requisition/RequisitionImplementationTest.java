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
import org.opennms.netmgt.model.requisition.OnmsForeignSource;
import org.opennms.netmgt.model.requisition.OnmsRequisition;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
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

    private Map<String, ForeignSourceRepository> m_repositories;

    @Before
    @After
    public void cleanUp() throws Exception {
        if (m_repositories != null) {
            for (final ForeignSourceRepository fsr : m_repositories.values()) {
                fsr.getRequisitions().forEach(r -> fsr.delete(r));
            }
        }
        LOG.info("Test context prepared.");
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
                        OnmsRequisition req = toPersistenceModel(JAXB.unmarshal(new ClassPathResource("/requisition-test.xml").getURL(), Requisition.class));
                        fsr.save(req);
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
    public void testCreateSimpleForeignSource() {
        runTest(
                fsr -> {
                    OnmsForeignSource fs = fsr.getForeignSource("blah");
                    fs.setDefault(false);
                    fsr.save(fs);
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
                    final OnmsRequisition req = new OnmsRequisition("foo bar");
                    req.setLastUpdate(new Date(0));
                    fsr.save(req);
                    final OnmsRequisition saved = fsr.getRequisition("foo bar");
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
                    final OnmsRequisition req = new OnmsRequisition("foo/bar");
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
                    final OnmsForeignSource fs = fsr.getForeignSource("foo bar");
                    fs.setDefault(false);
                    fsr.save(fs);
                    final OnmsForeignSource saved = fsr.getForeignSource("foo bar");
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
                    final OnmsForeignSource fs = fsr.getForeignSource("foo/bar");
                    fs.setDefault(false);
                    fsr.save(fs);
                    final OnmsForeignSource saved = fsr.getForeignSource("foo/bar");
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
