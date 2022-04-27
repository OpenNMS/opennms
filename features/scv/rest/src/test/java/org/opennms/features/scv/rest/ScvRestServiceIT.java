/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
