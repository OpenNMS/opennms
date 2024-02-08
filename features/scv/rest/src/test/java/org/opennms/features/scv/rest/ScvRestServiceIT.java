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
package org.opennms.features.scv.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScvRestServiceIT {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        System.setProperty("karaf.etc", tempFolder.getRoot().getAbsolutePath());
    }

    private ScvRestService scvRestService;

    @Test
    public void testScvRest() throws JsonProcessingException {
        File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        SecureCredentialsVault scv = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "OpenNMS@22");
        scvRestService = new DefaultScvRestService(scv);
        // Create alias
        String alias = "juniper-vsrx";
        var credentialDTO = new CredentialsDTO(alias, "horizon", "OpenNMS");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("user1", "minion1");
        attributes.put("user2", "minion2");
        attributes.put("user3", "minion3");
        attributes.put("pass1", "2021");
        attributes.put("pass2", "2022");
        attributes.put("pass3", "2023");

        // Test POST
        credentialDTO.setAttributes(attributes);
        Response posted = scvRestService.addCredentials(credentialDTO);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), posted.getStatus());

        // Test Get for credentials
        var response = scvRestService.getCredentials(alias);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        var entity = response.getEntity();
        if (!(entity instanceof CredentialsDTO)) {
            Assert.fail();
        }
        var result = ((CredentialsDTO) entity);
        Assert.assertEquals(credentialDTO.getUsername(), result.getUsername());
        Assert.assertEquals(credentialDTO.getAlias(), result.getAlias());
        Assert.assertEquals(result.getAttributes().size(), attributes.size());

        // Test Get for alias
        var aliasesResult = scvRestService.getAliases();
        Assert.assertEquals(aliasesResult.getStatus(), Response.Status.OK.getStatusCode());
        Set<String> aliases = ((Set<String>) aliasesResult.getEntity());
        Assert.assertThat(aliases, Matchers.contains(alias));

        // Test edit ( PUT)
        CredentialsDTO updatedCredentials = new CredentialsDTO();
        updatedCredentials.setAlias(alias);
        updatedCredentials.setUsername("meridian");
        updatedCredentials.setPassword("OpenNMS@22");
        updatedCredentials.getAttributes().put("user3", "minion@3");
        updatedCredentials.getAttributes().put("pass4", "2024");
        var putResponse = scvRestService.editCredentials(alias, updatedCredentials);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), putResponse.getStatus());

        // Test Get ( to test updated credentials)
        var updatedResponse = scvRestService.getCredentials(alias);
        var updatedCredsResponse = ((CredentialsDTO) updatedResponse.getEntity());
        Assert.assertEquals(updatedCredentials.getUsername(), updatedCredsResponse.getUsername());
        Assert.assertEquals(updatedCredentials.getAlias(), updatedCredsResponse.getAlias());
        Assert.assertEquals(updatedCredentials.getAttributes().size(), updatedCredsResponse.getAttributes().size());


        // Test edit ( PUT) with masked password.
        CredentialsDTO updatedCredentialsWithMaskPassword = new CredentialsDTO();
        updatedCredentialsWithMaskPassword.setAlias(alias);
        updatedCredentialsWithMaskPassword.setUsername("meridian1");
        updatedCredentialsWithMaskPassword.setPassword(updatedCredsResponse.getPassword());
        updatedCredentialsWithMaskPassword.getAttributes().put("user4", "minion4");
        putResponse = scvRestService.editCredentials(alias, updatedCredentialsWithMaskPassword);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), putResponse.getStatus());

        // Test Get ( to test updated credentials)
        updatedResponse = scvRestService.getCredentials(alias);
        updatedCredsResponse = ((CredentialsDTO) updatedResponse.getEntity());

        // When using masked password, username/password doesn't get updated.
        Assert.assertEquals(updatedCredentials.getUsername(), updatedCredsResponse.getUsername());
        // But attributes may be updated.
        Assert.assertEquals(updatedCredentialsWithMaskPassword.getAttributes().size(), updatedCredsResponse.getAttributes().size());

        // Add another alias and test.
        String alias1 = "another-device";
        credentialDTO = new CredentialsDTO(alias1, "horizon", "OpenNMS");
        scvRestService.addCredentials(credentialDTO);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), posted.getStatus());

        aliasesResult = scvRestService.getAliases();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), aliasesResult.getStatus());

        aliases = ((Set<String>) aliasesResult.getEntity());
        Assert.assertThat(aliases, Matchers.contains(alias1, alias));
    }

}
