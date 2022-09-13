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

package org.opennms.features.scv.hcp;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HcpVaultImpl implements SecureCredentialsVault {

    private static final String KVSTORE_HCP_PREFIX = "hcp-vault:";
    private final Vault vault;
    private final VaultConfig config;
    private final BlobStore blobStore;

    public HcpVaultImpl(String url, String token, BlobStore blobStore) throws VaultException {
        this.blobStore = blobStore;
        config = new VaultConfig()
                .address(url)
                .token(token)
                .build();
        vault = new Vault(config);
    }

    @Override
    public Set<String> getAliases() {
        // TODO: Not implemented
        return new HashSet<>();
    }

    @Override
    public Credentials getCredentials(String alias) {

        Map<String, byte[]> credentailsMap = blobStore.enumerateContext(KVSTORE_HCP_PREFIX + alias);
        String username = new String(credentailsMap.get("username"));
        String password = new String(credentailsMap.get("password"));
        // TODO: Consider attributes
        return new Credentials(username, password);
    }

    @Override
    public void setCredentials(String alias, Credentials credentials) {

        blobStore.put("username", credentials.getUsername().getBytes(StandardCharsets.UTF_8), KVSTORE_HCP_PREFIX + alias);
        blobStore.put("password", credentials.getPassword().getBytes(StandardCharsets.UTF_8), KVSTORE_HCP_PREFIX + alias);
        credentials.getAttributeKeys().forEach((attributeKey) -> {
            blobStore.put(attributeKey, credentials.getAttribute(attributeKey).getBytes(StandardCharsets.UTF_8), KVSTORE_HCP_PREFIX + alias);
        });
    }

}
