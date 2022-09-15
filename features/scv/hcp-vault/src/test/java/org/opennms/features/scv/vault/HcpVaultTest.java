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

package org.opennms.features.scv.vault;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.rest.Rest;
import com.bettercloud.vault.rest.RestException;
import com.bettercloud.vault.rest.RestResponse;
import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.vault.config.HcpVaultService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class HcpVaultTest {

    private Vault vault;
    private VaultConfig config;

    @Before
    public void init() throws VaultException {
        config = new VaultConfig()
                .address("")
                // Update token from local vault server
                .token("")
                .nameSpace("admin")
                .build();
        vault = new Vault(config);
    }

    @Test
    @Ignore("Need a running vault")
    public void testConfigEncryptionAndDecryption() throws VaultException, RestException {

        var authResponse = vault.auth().lookupSelf();
        Assert.assertThat(authResponse.getRestResponse().getStatus(), CoreMatchers.is(200));

        final String encryptKey = UUID.randomUUID().toString();
        final RestResponse keyCreateResponse = new Rest()
                .url(config.getAddress() + "/v1/" + "transit/keys/" + encryptKey)
                .header("X-Vault-Token", config.getToken())
                .post();
        Assert.assertThat(keyCreateResponse.getStatus(), CoreMatchers.is(204));

        String secret = "top-secret";
        JSONObject encodeJsonBody = new JSONObject();
        encodeJsonBody.put("plaintext", Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8)));
        final RestResponse encryptResponse = new Rest()
                .url(config.getAddress() + "/v1/" + "transit/encrypt/" + encryptKey)
                .header("X-Vault-Token", config.getToken())
                .header("Content-Type", "application/json")
                .body(encodeJsonBody.toString().getBytes(StandardCharsets.UTF_8))
                .post();

        Assert.assertThat(encryptResponse.getStatus(), CoreMatchers.is(200));
        String jsonEncrypted = new String(encryptResponse.getBody());
        JSONObject jsonObject = new JSONObject(jsonEncrypted);
        JSONObject encodedData = jsonObject.getJSONObject("data");
        Assert.assertNotNull(encodedData);
        String cipherText = encodedData.getString("ciphertext");
        Assert.assertThat(cipherText, CoreMatchers.containsString("vault"));

        JSONObject decodeJsonBody = new JSONObject();
        decodeJsonBody.put("ciphertext", cipherText);
        final RestResponse decryptResponse = new Rest()
                .url(config.getAddress() + "/v1/" + "transit/decrypt/" + encryptKey)
                .header("X-Vault-Token", config.getToken())
                .header("Content-Type", "application/json")
                .body(decodeJsonBody.toString().getBytes(StandardCharsets.UTF_8))
                .post();

        Assert.assertThat(decryptResponse.getStatus(), CoreMatchers.is(200));
        String jsonDecrypted = new String(decryptResponse.getBody());
        JSONObject decodedObject = new JSONObject(jsonDecrypted);
        JSONObject decodedData = decodedObject.getJSONObject("data");
        Assert.assertNotNull(decodedData);
        String plaintextOutput = decodedData.getString("plaintext");
        Assert.assertEquals(secret, new String(Base64.getDecoder().decode(plaintextOutput)));
    }


    @Test
    @Ignore("needs vault running locally")
    public void testSecretsStoreWithVault() throws VaultException {

        var authResponse = vault.auth().lookupSelf();
        Assert.assertThat(authResponse.getRestResponse().getStatus(), CoreMatchers.is(200));
        Map<String, Object> secrets = new HashMap<>();
        secrets.put("key1", "value1");
        var response = vault.logical().write("secret/hello", secrets);
        Assert.assertThat(response.getRestResponse().getStatus(), CoreMatchers.is(200));
        var readResponse = vault.logical().read("secret/hello");
        Map<String, String> secretResponse = readResponse.getData();
        Assert.assertEquals(secrets, secretResponse);
    }

    @Test
    @Ignore
    public void testHcpVault() throws VaultException {
        var jsonStore = Mockito.mock(JsonStore.class);
        Mockito.when(jsonStore.get(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(jsonStore.put(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(0L);
        HcpVaultService hcpVaultService = new HcpVaultService(jsonStore);
        hcpVaultService.initializeVault(config);
        HcpCredentialsVault hcpVault = new HcpCredentialsVault(hcpVaultService);
        var authResponse = hcpVault.getVault().auth().lookupSelf();
        Map<String, String> attributes = new HashMap<>();
        attributes.put("version", "release-30");
        Credentials credentials = new Credentials("opennms", "horizon", attributes);
        String alias = "dcb";
        hcpVault.setCredentials(alias, credentials);
        var response = hcpVault.getCredentials(alias);
        Assert.assertEquals(credentials, response);
        String aliasResponse = hcpVault.getAliases().iterator().next();
        Assert.assertEquals(alias, aliasResponse);
    }


}
