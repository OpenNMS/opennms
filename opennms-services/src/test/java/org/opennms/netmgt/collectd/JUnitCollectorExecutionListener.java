/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.File;
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
        InputStream is = ConfigurationTestUtils.getInputStreamForResource(testContext.getTestInstance(), config.schemaConfig());
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(is));
        is.close();

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

        FileUtils.deleteDirectory(m_snmpRrdDirectory);

        m_fileAnticipator.tearDown();

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
