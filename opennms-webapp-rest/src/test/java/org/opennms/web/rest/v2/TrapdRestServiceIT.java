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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.TrapdConfigDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.api.TrapdRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

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
public class TrapdRestServiceIT {

    private SecurityContext securityContext;

    @Autowired
    private TrapdRestApi m_trapdRestApi;

    // @Autowired
    // private TrapdConfigDao m_trapdConfigDao;

    // @Before
    // public void setUp() {
    //     Principal principal = mock(Principal.class);
    //     when(principal.getName()).thenReturn("integration-user");
    //     securityContext = mock(SecurityContext.class);
    //     when(securityContext.getUserPrincipal()).thenReturn(principal);
    // }

    // @Test
    // public void testUploadTrapdConfig() {
    //     final Attachment attachment = mock(Attachment.class);
    //     when(attachment.getObject(InputStream.class)).thenReturn(new ByteArrayInputStream(validTrapdConfigXml().getBytes(StandardCharsets.UTF_8)));

    //     try (Response response = m_trapdRestApi.uploadTrapdConfiguration(attachment, securityContext)) {
    //         assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    //     }
    //     assertEquals(10163, m_trapdConfigDao.getConfig().getSnmpTrapPort());
    // }

    // @Test
    // public void testUploadTrapdConfigWithInvalidXml() {
    //     final Attachment attachment = mock(Attachment.class);
    //     when(attachment.getObject(InputStream.class)).thenReturn(new ByteArrayInputStream("<trapd-configuration".getBytes(StandardCharsets.UTF_8)));

    //     try (Response response = m_trapdRestApi.uploadTrapdConfiguration(attachment, securityContext)) {
    //         assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    //         assertTrue(String.valueOf(response.getEntity()).contains("Invalid trapd XML configuration."));
    //     }
    // }

    // @Test
    // public void testUploadTrapdConfigWithMissingAttachment() {
    //     try (Response response = m_trapdRestApi.uploadTrapdConfiguration(null, securityContext)) {
    //         assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    //         assertNotNull(response.getEntity());
    //     }
    // }

    // private static String validTrapdConfigXml() {
    //     return "<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
    //             + "snmp-trap-address=\"*\" snmp-trap-port=\"10163\" new-suspect-on-trap=\"false\" "
    //             + "include-raw-message=\"false\" threads=\"0\" queue-size=\"10000\" "
    //             + "batch-size=\"1000\" batch-interval=\"500\"/>";
    // }
}


