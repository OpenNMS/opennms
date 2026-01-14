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
package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.FileInputStream;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsMonitoredServiceDetail;
import org.opennms.netmgt.model.OnmsMonitoredServiceDetailList;
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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
@Transactional
public class IfServicesRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private ServletContext m_servletContext;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_databasePopulator.populateDatabase();
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetServices() throws Exception {
        String url = "/ifservices";
        OnmsMonitoredServiceDetailList list = getXmlObject(JaxbUtils.getContextFor(OnmsMonitoredServiceDetailList.class), url, 200, OnmsMonitoredServiceDetailList.class);
        for(OnmsMonitoredServiceDetail detail : list.getObjects()) {
            assertFalse("F".equals(detail.getStatusCode()));
        }

        // Mark all services as forced unmanaged
        sendPut(url, "status=F", 204);

        // Verify that all statuses were updated
        list = getXmlObject(JaxbUtils.getContextFor(OnmsMonitoredServiceDetailList.class), url, 200, OnmsMonitoredServiceDetailList.class);
        for(OnmsMonitoredServiceDetail detail : list.getObjects()) {
            assertEquals("F", detail.getStatusCode());
        }
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetById() throws Exception {
        OnmsMonitoredServiceDetail service = getXmlObject(JaxbUtils.getContextFor(OnmsMonitoredServiceDetail.class), "/ifservices/2", 200, OnmsMonitoredServiceDetail.class);
        Assert.assertNotNull(service);
        Assert.assertEquals("2", service.getId());

        // verify that 404 is implemented correctly
        sendRequest(GET, "/ifservices/-2", 404);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetServicesJson() throws Exception {
        String url = "/ifservices";

        // GET all users
        MockHttpServletRequest jsonRequest = createRequest(m_servletContext, GET, url);
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        jsonRequest.setQueryString("orderBy=id");
        String json = sendRequest(jsonRequest, 200);

        JSONObject restObject = new JSONObject(json);
        JSONObject expectedObject = new JSONObject(IOUtils.toString(new FileInputStream("src/test/resources/v1/ifservices.json")));
        JSONAssert.assertEquals(expectedObject, restObject, true);
    }

}
