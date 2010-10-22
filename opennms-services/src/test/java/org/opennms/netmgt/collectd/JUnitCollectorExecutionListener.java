package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.apache.commons.io.IOUtils;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.netmgt.dao.castor.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.FileAnticipator;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.ReflectionUtils;

/**
 * <p>This {@link TestExecutionListener} looks for the {@link JUnitCollector} annotation
 * and uses attributes on it to:</p>
 * <ul>
 * <li>Load configuration files for the {@link ServiceCollector}</li>
 * <li>Set up {@link FileAnticipator} checks for files created
 * during the unit test execution</li>
 * </ul>
 */
public class JUnitCollectorExecutionListener extends AbstractTestExecutionListener {

    private File m_snmpRrdDirectory;
    private FileAnticipator m_fileAnticipator;

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        JUnitCollector config = findCollectorAnnotation(testContext);
        if (config == null) {
            return;
        }

        // FIXME: Is there a better way to inject the TestContext into the test class?  Seems that spring doesn't give direct access...
        Method m = ReflectionUtils.findMethod(testContext.getTestClass(), "setTestContext", new Class[]{TestContext.class});
        if (m != null && testContext.getTestInstance() != null) {
            System.err.println("invoking setTestContext on " + testContext.getTestInstance());
            m.invoke(testContext.getTestInstance(), testContext);
        }
        
        RrdTestUtils.initialize();

        // make a fake database schema with hibernate
        InputStream is = ConfigurationTestUtils.getInputStreamForResource(testContext.getTestInstance(), config.schemaConfig());
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(is));
        is.close();

        // set up temporary directories for RRD files
        m_fileAnticipator = new FileAnticipator();
        m_snmpRrdDirectory = m_fileAnticipator.tempDir("snmp");
        m_snmpRrdDirectory.mkdirs();
        testContext.setAttribute("fileAnticipator", m_fileAnticipator);
        testContext.setAttribute("rrdDirectory", m_snmpRrdDirectory);

        // set up the collection configuration factory
        if (config.datacollectionType().equalsIgnoreCase("http")) {
        	is = ConfigurationTestUtils.getInputStreamForResourceWithReplacements(testContext.getTestInstance(), config.datacollectionConfig(), new String[] { "%rrdRepository%", m_snmpRrdDirectory.getAbsolutePath() });
            HttpCollectionConfigFactory factory = new HttpCollectionConfigFactory(is);
            HttpCollectionConfigFactory.setInstance(factory);
            HttpCollectionConfigFactory.init();
        } else if (config.datacollectionType().equalsIgnoreCase("snmp")) {
            Resource r = ConfigurationTestUtils.getSpringResourceForResourceWithReplacements(testContext.getTestInstance(), config.datacollectionConfig(), new String[] { "%rrdRepository%", m_snmpRrdDirectory.getAbsolutePath() });
            DefaultDataCollectionConfigDao dataCollectionDao = new DefaultDataCollectionConfigDao();
            dataCollectionDao.setConfigResource(r);
            dataCollectionDao.afterPropertiesSet();
            DataCollectionConfigFactory.setInstance(dataCollectionDao);
        } else {
            throw new UnsupportedOperationException("data collection type '" + config.datacollectionType() + "' not supported");
        }
        IOUtils.closeQuietly(is);
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        JUnitCollector config = findCollectorAnnotation(testContext);
        if (config == null) {
            return;
        }

        if (config.anticipateFiles().length > 0 ||
                config.anticipateRrds().length > 0) {
            // make sure any RRDs have time to get written
            Thread.sleep(1000);
        }

        if (config.anticipateRrds().length > 0) {
            for (String rrdFile : config.anticipateRrds()) {
                m_fileAnticipator.expecting(m_snmpRrdDirectory, rrdFile + RrdUtils.getExtension());
            }
        }

        if (config.anticipateFiles().length > 0) {
            for (String file : config.anticipateFiles()) {
                m_fileAnticipator.expecting(m_snmpRrdDirectory, file);
            }
        }

        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }

        deleteChildDirectories(m_snmpRrdDirectory);
        m_fileAnticipator.tearDown();
    }

    private static void deleteChildDirectories(File directory) {
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                deleteChildDirectories(f);
                if (f.list().length == 0) {
                    f.delete();
                }
            }
        }
    }

    private static JUnitCollector findCollectorAnnotation(TestContext testContext) {
        Method testMethod = testContext.getTestMethod();
        JUnitCollector config = testMethod.getAnnotation(JUnitCollector.class);
        if (config != null) {
            return config;
        }

        Class<?> testClass = testContext.getTestClass();
        return testClass.getAnnotation(JUnitCollector.class);
    }

}
