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
package org.opennms.web.rest.v2;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.api.DataCollectionConfRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
@Transactional
public class DataCollectionConfRestServiceIT {

    private static final String FILENAME = "dell.xml";
    private static final String RESOURCE_PATH = "/DATACOLLECTION/";
    private SecurityContext securityContext;

    @Autowired
    private DataCollectionConfRestApi dataCollectionConfRestApi;

    @Before
    public void setUp() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("integration-user");

        securityContext = mock(SecurityContext.class);
        when(securityContext.getUserPrincipal()).thenReturn(principal);
    }

    @Test
    public void testUploadSnmpDataCollectionConfFiles_Success() throws Exception {
        List<Attachment> attachments = List.of(createMockedAttachment(FILENAME));
        Response resp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(attachments, securityContext);

        assertEquals("Expected OK status", Response.Status.OK.getStatusCode(), resp.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> success = (List<Map<String, Object>>) entity.get("success");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) entity.get("errors");

        assertEquals("Should be one successful upload", 1, success.size());
        assertEquals("Uploaded file key should match", "dell", success.get(0).get("file"));
        assertTrue("Error list should be empty", errors.isEmpty());
    }

    @Test
    public void testEmptyAttachments_ShouldReturnEmptyLists() throws Exception {
        Response resp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(Collections.emptyList(), securityContext);

        assertEquals("Expected OK status", Response.Status.OK.getStatusCode(), resp.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        assertTrue("Success list should be empty when no attachments", ((List<?>) entity.get("success")).isEmpty());
        assertTrue("Errors list should be empty when no attachments", ((List<?>) entity.get("errors")).isEmpty());
    }

    @Test
    public void testNullSecurityContext_ShouldUseUnknownUser() throws Exception {
        List<Attachment> attachments = List.of(createMockedAttachment(FILENAME));
        Response resp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(attachments, null);

        assertEquals("Expected OK status", Response.Status.OK.getStatusCode(), resp.getStatus());
    }

    /** Helper to create a mocked Attachment for a given file */
    private Attachment createMockedAttachment(String name) {
        InputStream is = getClass().getResourceAsStream(RESOURCE_PATH + name);
        assertNotNull("Test resource not found: " + name, is);

        Attachment att = mock(Attachment.class);
        ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn(name);
        when(att.getContentDisposition()).thenReturn(cd);
        when(att.getObject(InputStream.class)).thenReturn(is);
        return att;
    }
}
