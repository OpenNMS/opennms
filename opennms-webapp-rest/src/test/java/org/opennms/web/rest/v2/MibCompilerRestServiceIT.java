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
import org.opennms.web.rest.v2.api.MibCompilerRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;
import java.security.Principal;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/applicationContext-rest-test.xml"

})

@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class MibCompilerRestServiceIT {

    @Autowired
    private MibCompilerRestApi mibCompilerRestApi;

    private SecurityContext securityContext;

    @Before
    public void setUp() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("integration-user");
        securityContext = mock(SecurityContext.class);
        when(securityContext.getUserPrincipal()).thenReturn(principal);
    }
    @Test
    public void testUploadMibFiles_success() throws Exception {
        String[] filenames = {"RFC-1212.mib"};
        List<Attachment> attachments = new ArrayList<>();

        for (final var name : filenames) {
            final var path = "/MIB-FILES/" + name;
            final var is = getClass().getResourceAsStream(path);
            assertNotNull("Resource not found: " + path, is);

            Attachment att = mock(Attachment.class);
            ContentDisposition cd = mock(ContentDisposition.class);
            when(cd.getParameter("filename")).thenReturn(name);
            when(att.getContentDisposition()).thenReturn(cd);
            when(att.getObject(InputStream.class)).thenReturn(is);
            attachments.add(att);
        }

        Response resp = mibCompilerRestApi.uploadMibFiles(attachments, securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        assertNotNull(entity);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> success = (List<Map<String, Object>>) entity.get("success");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) entity.get("errors");

        assertNotNull(success);
        assertNotNull(errors);

        assertEquals(1, success.size());
        assertTrue("Expected no errors, but got: " + errors, errors.isEmpty());

        assertEquals("RFC-1212.mib", success.get(0).get("filename"));
        assertNotNull("savedAs should be present", success.get(0).get("savedAs"));
        assertEquals(true, success.get(0).get("success"));
    }

}
