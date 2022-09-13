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
import com.bettercloud.vault.response.LogicalResponse;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HcpVaultImpl implements SecureCredentialsVault {

    private static final String PREFIX = SystemInfoUtils.getInstanceId() + "-scv/";
    private static final String ROOT_PATH = "secret/";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private final Vault vault;
    private final VaultConfig config;

    public HcpVaultImpl(String url, String token) throws VaultException {
        config = new VaultConfig()
                .address(url)
                .token(token)
                .build();
        vault = new Vault(config);
    }

    @Override
    public Set<String> getAliases() {
        try {
            var aliases = vault.logical().list(ROOT_PATH + PREFIX).getListData();
            return Sets.newHashSet(aliases);
        } catch (VaultException e) {

        }
        return new HashSet<>();
    }

    @Override
    public Credentials getCredentials(String alias) {

        try {
            LogicalResponse response = vault.logical().read(ROOT_PATH + PREFIX + alias);
            Map<String, String> data = response.getData();
            String username = data.remove(USERNAME);
            String password = data.remove(PASSWORD);
            return new Credentials(username, password, data);

        } catch (VaultException e) {

        }
        return null;
    }

    @Override
    public void setCredentials(String alias, Credentials credentials) {

        Map<String, Object> kvMap = new HashMap<>();
        kvMap.put(USERNAME, credentials.getUsername());
        kvMap.put(PASSWORD, credentials.getPassword());
        kvMap.putAll(credentials.getAttributes());
        try {
            vault.logical().write(ROOT_PATH + PREFIX + alias, kvMap);
        } catch (VaultException e) {

        }
    }

    @VisibleForTesting
    Vault getVault() {
        return vault;
    }
}
