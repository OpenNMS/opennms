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
package org.opennms.core.collection.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.test.FileAnticipator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

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

    private static final Logger LOG = LoggerFactory.getLogger(JUnitCollectorExecutionListener.class);
    private File m_snmpRrdDirectory;
    private FileAnticipator m_fileAnticipator;

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        m_fileAnticipator = new FileAnticipator();

        JUnitCollector config = findCollectorAnnotation(testContext);
        if (config == null) {
            return;
        }

        // make a fake database schema with hibernate
        InputStream is = null;
        DatabaseSchemaConfigFactory.init();

        // set up temporary directories for RRD files
        m_snmpRrdDirectory = m_fileAnticipator.tempDir("snmp");
        m_snmpRrdDirectory.mkdirs();
        testContext.setAttribute("fileAnticipator", m_fileAnticipator);
        testContext.setAttribute("rrdDirectory", m_snmpRrdDirectory);

        // set up the collection configuration factory
        if ("http".equalsIgnoreCase(config.datacollectionType()) || "https".equalsIgnoreCase(config.datacollectionType())) {
            is = ConfigurationTestUtils.getInputStreamForResourceWithReplacements(testContext.getTestInstance(), config.datacollectionConfig(), new String[] { "%rrdRepository%", m_snmpRrdDirectory.getAbsolutePath() });
            HttpCollectionConfigFactory factory = new HttpCollectionConfigFactory(is);
            HttpCollectionConfigFactory.setInstance(factory);
            HttpCollectionConfigFactory.init();
        } else if ("snmp".equalsIgnoreCase(config.datacollectionType())) {
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

        boolean shouldIgnoreNonExistent = testContext.getTestException() != null;

        /*
        if (config.anticipateFiles().length > 0 || config.anticipateRrds().length > 0) {
            // make sure any RRDs have time to get written
            Thread.sleep(config.timeout());
        }
        */

        if (config.anticipateRrds().length > 0) {
            for (String rrdFile : config.anticipateRrds()) {
                // Expect the RRD files, for which we don't know the suffix
                // Make sure they don't match the .meta files though
                m_fileAnticipator.expectingFileWithPrefix(m_snmpRrdDirectory, rrdFile, ".meta");

                if (config.anticipateMetaFiles()) {
                    //the nrtg feature requires .meta files in parallel to the rrd/jrb files.
                    //this .meta files are expected
                    m_fileAnticipator.expecting(m_snmpRrdDirectory, rrdFile + ".meta");
                }
            }
        }

        if (config.anticipateFiles().length > 0) {
            for (String file : config.anticipateFiles()) {
                m_fileAnticipator.expecting(m_snmpRrdDirectory, file);
            }
        }

        Exception e = null;
        if (m_fileAnticipator.isInitialized()) {
            final long finished = System.currentTimeMillis() + config.timeout();
            while (System.currentTimeMillis() <= finished) {
                if (m_fileAnticipator.foundExpected()) {
                    break;
                }
                try {
                    Thread.sleep(200);
                } catch (final InterruptedException ie) {
                    break;
                }
            }

            try {
                m_fileAnticipator.deleteExpected(shouldIgnoreNonExistent);
            } catch (Throwable t) {
                e = new RuntimeException(t);
            }
        }

        try {
            FileUtils.deleteDirectory(m_snmpRrdDirectory);
        } catch (IOException deleteDirectoryException) {
            // Windows is failing tests due to spurious cleanup errors
            LOG.warn("Failed to delete {} during cleanup.", m_snmpRrdDirectory, deleteDirectoryException);
            String os = System.getProperty("os.name");
            if (os == null || !os.contains("Windows")) {
                throw deleteDirectoryException;
            }
        }

        try {
            m_fileAnticipator.tearDown();
        } catch (RuntimeException anticipatorTeardownException) {
            // Windows is failing tests due to spurious cleanup errors
            LOG.warn("Failed to delete {} during cleanup.", m_fileAnticipator.getTempDir(), anticipatorTeardownException);
            String os = System.getProperty("os.name");
            if (os == null || !os.contains("Windows")) {
                throw anticipatorTeardownException;
            }
        }

        if (e != null) {
            throw e;
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
