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

package org.opennms.web.rest;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.common.io.Files;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-jersey.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class KscRestServiceTest extends AbstractSpringJerseyRestTestCase {
    private File m_configFile = new File("target/test-classes/ksc-performance-reports.xml");

    @Override
    protected void beforeServletStart() throws Exception {
        /* make sure the file is reset every time, otherwise we're reliant on test ordering */
        final File sourceFile = new File("src/test/resources/ksc-performance-reports.xml");
        Files.copy(sourceFile, m_configFile);
        KSC_PerformanceReportFactory.setConfigFile(m_configFile);
        KSC_PerformanceReportFactory.getInstance().reload();
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    public void testReadOnly() throws Exception {
        // Testing GET Collection
        String xml = sendRequest(GET, "/ksc", 200);
        assertTrue(xml, xml.contains("Test 2"));

        xml = sendRequest(GET, "/ksc/0", 200);
        assertTrue(xml, xml.contains("label=\"Test\""));

        sendRequest(GET, "/ksc/3", 404);
    }

    @Test
    public void testAddGraph() throws Exception {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("title", "foo");
        params.put("reportName", "bar");
        params.put("resourceId", "baz");
        sendRequest(PUT, "/ksc/0", params, 303, "/ksc/0");

        final String xml = slurp(m_configFile);
        assertTrue(xml, xml.contains("title=\"foo\""));
    }

    @Test
    public void testAddNewGraph() throws Exception {
        final String kscReport = "<kscReport id=\"3\" label=\"foo2\">"
                +"<kscGraph title=\"Title1\" resourceId=\"node[2].responseTime[127.0.0.1]\" timespan=\"1_hour\" graphtype=\"icmp\"/>"
                +"</kscReport>";

        sendPost("/ksc", kscReport, 303, null);

        final String xml = slurp(m_configFile);
        assertTrue(xml, xml.contains("title=\"foo2\""));
    }

    private static String slurp(final File file) throws Exception {
        Reader fileReader = null;
        BufferedReader reader = null;

        try {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            final StringBuilder sb = new StringBuilder();
            while (reader.ready()) {
                final String line = reader.readLine();
                System.err.println(line);
                sb.append(line).append('\n');
            }

            return sb.toString();
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(fileReader);
        }
    }
}
