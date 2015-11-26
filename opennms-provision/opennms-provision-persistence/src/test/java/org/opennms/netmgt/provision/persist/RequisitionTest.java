package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
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
public class RequisitionTest implements InitializingBean, ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(RequisitionTest.class);

    private Map<String, ForeignSourceRepository> m_repositories;

    @Before
    public void setUp() throws Exception {
        if (m_repositories != null) {
            for (final ForeignSourceRepository fsr : m_repositories.values()) {
                fsr.clear();
                fsr.flush();
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
            fsr.flush();
            if (expected != null) {
                try {
                    rt.test(fsr);
                    throw new RuntimeException("Expected throwable " + expected.getName() + " when running test against " + bundleName + ", but it passed!");
                } catch (final Throwable t) {
                    LOG.debug("expected: {}, got: {}", expected, t);
                    if (t.getClass().getCanonicalName().equals(expected.getCanonicalName())) {
                        // we got the exception we expected, carry on
                    } else {
                        throw new RuntimeException("Expected throwable " + expected.getName() + " when running test against " + bundleName + ", but got " + t.getClass() + " instead!", t);
                    }
                }
            } else {
                rt.test(fsr);
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
                    final Requisition req = fsr.importResourceRequisition(new ClassPathResource("/requisition-test.xml"));
                    req.setForeignSource("foo bar");
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
