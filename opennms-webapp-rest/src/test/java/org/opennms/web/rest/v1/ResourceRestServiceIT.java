/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.rrd.RrdStrategyFactory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class ResourceRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private ServletContext m_servletContext;

    @Autowired
    private DatabasePopulator m_dbPopulator;

    @Autowired
    private FilesystemResourceStorageDao m_resourceStorageDao;

    @Autowired
    private RrdStrategyFactory m_rrdStrategyFactory;

    @Rule
    public TemporaryFolder m_tempFolder = new TemporaryFolder();

    protected String m_extension;

    @Before
    public void setUp() throws Throwable {
        super.setUp();

        // Add some nodes
        m_dbPopulator.populateDatabase();

        // Point to our temporary directory
        m_resourceStorageDao.setRrdDirectory(m_tempFolder.getRoot());

        // Add some blank RRD files
        m_extension = m_rrdStrategyFactory.getStrategy().getDefaultFileExtension();
        File nodeSnmp1 = m_tempFolder.newFolder("snmp", "1");
        FileUtils.touch(new File(nodeSnmp1, "SwapIn" + m_extension));
        FileUtils.touch(new File(nodeSnmp1, "SwapOut" + m_extension));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testResources() throws Exception {
        // Top level
        String url = "/resources";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("Node-level Performance Data"));
        System.err.println(xml);

        // By ID
        url = "/resources/" + URLEncoder.encode("node[1].nodeSnmp[]", StandardCharsets.UTF_8.name());
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("Node-level Performance Data"));

        // 404 on invalid resource
        url = "/resources/" + URLEncoder.encode("node[99].nodeSnmp[]", StandardCharsets.UTF_8.name());
        sendRequest(GET, url, 404);

        // By Node ID
        url = "/resources/fornode/1";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("Node-level Performance Data"));

        // 404 on invalid Node ID
        url = "/resources/fornode/99";
        xml = sendRequest(GET, url, 404);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testResourcesJson() throws Exception {
        String url = "/resources";

        // GET all users
        MockHttpServletRequest jsonRequest = createRequest(m_servletContext, GET, url);
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(jsonRequest, 200);

        JSONObject restObject = new JSONObject(json);
        final String jsonString = IOUtils.toString(new FileInputStream("src/test/resources/v1/resources.json"));
        JSONObject expectedObject = new JSONObject(jsonString.replace(".jrb", m_extension));
        JSONAssert.assertEquals(expectedObject, restObject, true);
    }
}
