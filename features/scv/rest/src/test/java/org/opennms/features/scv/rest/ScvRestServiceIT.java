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
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashMap;
import java.util.List;
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

        // Create first alias
        final String firstAlias = "juniper-vsrx";
        final var firstCredentialDTO = new CredentialsDTO(firstAlias, "horizon", "OpenNMS");
        Map<String, String> firstAttributes = new HashMap<>();
        firstAttributes.put("user1", "minion1");
        firstAttributes.put("user2", "minion2");
        firstAttributes.put("user3", "minion3");
        firstAttributes.put("pass1", "2021");
        firstAttributes.put("pass2", "2022");
        firstAttributes.put("pass3", "2023");

        // Test POST
        firstCredentialDTO.setAttributes(firstAttributes);
        Response posted = scvRestService.addCredentials(firstCredentialDTO);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), posted.getStatus());

        // Create second alias
        final String secondAlias = "cisco";
        final var secondCredentialDTO = new CredentialsDTO(secondAlias, "horizonTwo", "OpenNMSTwo");
        Map<String, String> secondAttributes = new HashMap<>();
        secondAttributes.put("user21", "minion21");
        secondAttributes.put("user22", "minion22");
        secondAttributes.put("user23", "minion23");
        secondAttributes.put("pass21", "2021A");
        secondAttributes.put("pass22", "2022A");
        secondAttributes.put("pass23", "2023A");

        // Test POST
        secondCredentialDTO.setAttributes(secondAttributes);
        posted = scvRestService.addCredentials(secondCredentialDTO);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), posted.getStatus());


        // Test Get for first credentials
        var response = scvRestService.getCredentials(firstAlias);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Object entity = response.getEntity();
        if (!(entity instanceof CredentialsDTO)) {
            Assert.fail();
        }
        CredentialsDTO result = ((CredentialsDTO) entity);
        Assert.assertEquals(firstCredentialDTO.getUsername(), result.getUsername());
        Assert.assertEquals(firstCredentialDTO.getAlias(), result.getAlias());
        Assert.assertEquals(result.getAttributes().size(), firstAttributes.size());

        // Test Get for second credentials
        response = scvRestService.getCredentials(secondAlias);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        entity = response.getEntity();
        if (!(entity instanceof CredentialsDTO)) {
            Assert.fail();
        }
        result = ((CredentialsDTO) entity);
        Assert.assertEquals(secondCredentialDTO.getUsername(), result.getUsername());
        Assert.assertEquals(secondCredentialDTO.getAlias(), result.getAlias());
        Assert.assertEquals(result.getAttributes().size(), secondAttributes.size());


        // Test Get for all credentials
        response = scvRestService.getCredentials("_all");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        entity = response.getEntity();
        if (!(entity instanceof List)) {
            Assert.fail();
        }

        List<CredentialsDTO> listResult = (List<CredentialsDTO>) entity;

        Assert.assertNotNull(listResult);
        Assert.assertEquals(2, listResult.size());

        CredentialsDTO firstResult = listResult.stream()
                .filter(d -> d.getAlias().equals(firstAlias)).findFirst().orElse(null);
        CredentialsDTO secondResult = listResult.stream()
                .filter(d -> d.getAlias().equals(secondAlias)).findFirst().orElse(null);

        Assert.assertNotNull(firstResult);
        Assert.assertNotNull(secondResult);

        Assert.assertEquals(firstCredentialDTO.getUsername(), firstResult.getUsername());
        Assert.assertEquals(firstCredentialDTO.getAlias(), firstResult.getAlias());
        Assert.assertEquals(firstCredentialDTO.getAttributes().size(), firstResult.getAttributes().size());

        Assert.assertEquals(secondCredentialDTO.getUsername(), secondResult.getUsername());
        Assert.assertEquals(secondCredentialDTO.getAlias(), secondResult.getAlias());
        Assert.assertEquals(secondCredentialDTO.getAttributes().size(), secondResult.getAttributes().size());


        // Test Get for aliases
        Response aliasesResult = scvRestService.getAliases();
        Assert.assertEquals(aliasesResult.getStatus(), Response.Status.OK.getStatusCode());
        Set<String> aliases = ((Set<String>) aliasesResult.getEntity());

        Assert.assertEquals(2, aliases.size());
        Assert.assertThat(aliases, Matchers.containsInAnyOrder(firstAlias, secondAlias));


        // Test edit ( PUT)
        CredentialsDTO updatedCredentials = new CredentialsDTO();
        updatedCredentials.setAlias(firstAlias);
        updatedCredentials.setUsername("meridian");
        updatedCredentials.setPassword("OpenNMS@22");
        updatedCredentials.getAttributes().put("user3", "minion@3");
        updatedCredentials.getAttributes().put("pass4", "2024");
        var putResponse = scvRestService.editCredentials(firstAlias, updatedCredentials);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), putResponse.getStatus());

        // Test Get ( to test updated credentials)
        var updatedResponse = scvRestService.getCredentials(firstAlias);
        var updatedCredsResponse = ((CredentialsDTO) updatedResponse.getEntity());
        Assert.assertEquals(updatedCredentials.getUsername(), updatedCredsResponse.getUsername());
        Assert.assertEquals(updatedCredentials.getAlias(), updatedCredsResponse.getAlias());
        Assert.assertEquals(updatedCredentials.getAttributes().size(), updatedCredsResponse.getAttributes().size());


        // Test edit ( PUT) with masked password.
        CredentialsDTO updatedCredentialsWithMaskPassword = new CredentialsDTO();
        updatedCredentialsWithMaskPassword.setAlias(firstAlias);
        updatedCredentialsWithMaskPassword.setUsername("meridian1");
        updatedCredentialsWithMaskPassword.setPassword(updatedCredsResponse.getPassword());
        updatedCredentialsWithMaskPassword.getAttributes().put("user4", "minion4");
        putResponse = scvRestService.editCredentials(firstAlias, updatedCredentialsWithMaskPassword);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), putResponse.getStatus());

        // Test Get ( to test updated credentials)
        updatedResponse = scvRestService.getCredentials(firstAlias);
        updatedCredsResponse = ((CredentialsDTO) updatedResponse.getEntity());

        // When using masked password, username/password doesn't get updated.
        Assert.assertEquals(updatedCredentials.getUsername(), updatedCredsResponse.getUsername());
        // But attributes may be updated.
        Assert.assertEquals(updatedCredentialsWithMaskPassword.getAttributes().size(), updatedCredsResponse.getAttributes().size());

        // Add another alias and test.
        String alias1 = "another-device";
        var credentialDTO = new CredentialsDTO(alias1, "horizon", "OpenNMS");
        scvRestService.addCredentials(credentialDTO);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), posted.getStatus());

        aliasesResult = scvRestService.getAliases();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), aliasesResult.getStatus());

        aliases = ((Set<String>) aliasesResult.getEntity());
        Assert.assertEquals(3, aliases.size());
        Assert.assertThat(aliases, Matchers.containsInAnyOrder(alias1, firstAlias, secondAlias));
    }

    @Test
    public void testScvRestUpdateUsingInvalidAliasFails() {
        File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        SecureCredentialsVault scv = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "OpenNMS@22");
        scvRestService = new DefaultScvRestService(scv);

        // Attempt to add credentials using the reserved alias
        final var credentialDTO = new CredentialsDTO(Credentials.GET_ALL_ALIAS, "horizon", "OpenNMS");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("user1", "minion1");
        attributes.put("pass1", "2021");
        credentialDTO.setAttributes(attributes);

        // Test POST
        Response posted = scvRestService.addCredentials(credentialDTO);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), posted.getStatus());

        final String expectedErrorMessage =
            "Invalid Credentials, cannot use '" + Credentials.GET_ALL_ALIAS + "' as an alias.";

        String message = (String) posted.getEntity();
        Assert.assertEquals(expectedErrorMessage, message);

        // Ensure it also fails regardless of case
        credentialDTO.setAlias(Credentials.GET_ALL_ALIAS.toUpperCase());
        posted = scvRestService.addCredentials(credentialDTO);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), posted.getStatus());

        message = (String) posted.getEntity();
        Assert.assertEquals(expectedErrorMessage, message);
    }
}
