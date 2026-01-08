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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v1.ForeignSourceConfigRestService.ElementList;
import org.opennms.web.rest.v1.ForeignSourceConfigRestService.SimplePluginConfigList;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * The Class ForeignSourceConfigRestServiceIT.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
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
public class ForeignSourceConfigRestServiceIT extends AbstractSpringJerseyRestTestCase {

    /**
     * Test get detectors.
     *
     * @throws Exception the exception
     */
    // FIXME I don't know how to load the policies on the Spring Context.
    @Test
    public void testGetDetectors() throws Exception {
        String xml = sendRequest(GET, "/foreignSourcesConfig/detectors", 200);
        SimplePluginConfigList list = JaxbUtils.unmarshal(SimplePluginConfigList.class, xml);
        Assert.assertNotNull(list);
    }

    /**
     * Test get policies.
     *
     * @throws Exception the exception
     */
    // FIXME I don't know how to load the policies on the Spring Context.
    @Test
    public void testGetPolicies() throws Exception {
        String xml = sendRequest(GET, "/foreignSourcesConfig/policies", 200);
        SimplePluginConfigList list = JaxbUtils.unmarshal(SimplePluginConfigList.class, xml);
        Assert.assertNotNull(list);
    }

    /**
     * Test get assets.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetAssets() throws Exception {
        String xml = sendRequest(GET, "/foreignSourcesConfig/assets", 200);
        ElementList list = JaxbUtils.unmarshal(ElementList.class, xml);
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(list.getElements().contains("address1"));
        Assert.assertFalse(list.getElements().contains("id"));
        Assert.assertFalse(list.getElements().contains("class"));
        Assert.assertFalse(list.getElements().contains("node"));
        Assert.assertFalse(list.getElements().contains("geolocation"));
    }

    /**
     * Test get categories.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetCategories() throws Exception {
        String xml = sendRequest(GET, "/foreignSourcesConfig/categories", 200);
        ElementList list = JaxbUtils.unmarshal(ElementList.class, xml);
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(list.getElements().contains("Production"));
    }

    /**
     * Test get services.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetServices() throws Exception {
        String req = "<model-import xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" date-stamp=\"2006-03-09T00:03:09\" foreign-source=\"Test\">" +
                "<node node-label=\"a\" foreign-id=\"a\" /></model-import>";
        sendPost("/requisitions", req, 202, null);
        String xml = sendRequest(GET, "/foreignSourcesConfig/services/Test", 200);
        ElementList list = JaxbUtils.unmarshal(ElementList.class, xml);
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(list.getElements().contains("ICMP"));
    }

}
