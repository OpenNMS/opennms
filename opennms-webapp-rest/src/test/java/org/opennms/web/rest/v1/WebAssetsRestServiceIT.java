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
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

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
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class WebAssetsRestServiceIT extends AbstractSpringJerseyRestTestCase {
    protected void beforeServletStart() throws Exception {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testListAllAssets() throws Exception {
        final MockHttpServletRequest request = createRequest(GET, "/web-assets");
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        final String json = sendRequest(request, 200);

        final JSONArray obj = new JSONArray(json);
        assertEquals(1, obj.length());
        assertEquals("test-asset", obj.getString(0));
    }

    @Test
    public void testGetAssetResources() throws Exception {
        final MockHttpServletRequest request = createRequest(GET, "/web-assets/test-asset");
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        final String json = sendRequest(request, 200);

        final JSONArray obj = new JSONArray(json);
        assertEquals(1, obj.length());
        final JSONObject o = obj.getJSONObject(0);
        assertEquals("js", o.getString("type"));
        assertEquals("assets/test.js", o.getString("path"));
    }

    @Test
    public void testGetAssetJsResource() throws Exception {
        final MockHttpServletRequest request = createRequest(GET, "/web-assets/test-asset.js");
        final String js = sendRequest(request, 200);
        assertTrue(js.contains("console.log"));
    }
}
