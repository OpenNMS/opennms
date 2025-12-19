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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.api.SnmpConfigRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

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
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SnmpConfigRestServiceIT {

    @Autowired
    private SnmpConfigRestApi snmpConfigRestApi;

    private SecurityContext securityContext;

    @Before
    public void setUp() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("integration-user");
        securityContext = mock(SecurityContext.class);
        when(securityContext.getUserPrincipal()).thenReturn(principal);
    }

    @Test
    @Transactional
    public void testGetSnmpConfig() throws Exception {
        Response response = snmpConfigRestApi.getSnmpConfig(securityContext);
        assertNotNull(response);

        assertEquals(200, response.getStatus());

        SnmpConfig config = (SnmpConfig) response.getEntity();
        assertNotNull(config);

        assertTrue(config.getDefinitions().isEmpty());
        assertTrue(config.getSnmpProfiles().getSnmpProfiles().isEmpty());

        assertEquals("public", config.getReadCommunity());
        assertEquals("private", config.getWriteCommunity());
    }
}
