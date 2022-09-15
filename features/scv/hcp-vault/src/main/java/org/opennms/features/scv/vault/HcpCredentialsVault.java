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
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.vault.config.VaultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HcpCredentialsVault implements SecureCredentialsVault {


    private static final Logger LOG = LoggerFactory.getLogger(HcpCredentialsVault.class);
    private static final String PREFIX = SystemInfoUtils.getInstanceId() + "-scv/";
    private static final String ROOT_PATH = "secret/";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Autowired
    private VaultService vaultConfigService;


    public HcpCredentialsVault() {
    }

    public HcpCredentialsVault(VaultService vaultConfigService) {
        this.vaultConfigService = vaultConfigService;
    }


    @Override
    public Set<String> getAliases() {
        try {
            if (getVault() != null) {
                var aliases = getVault().logical().list(ROOT_PATH + PREFIX).getListData();
                return Sets.newHashSet(aliases);
            } else {
                throw new IllegalStateException("Vault Service not initialized");
            }

        } catch (VaultException e) {
            LOG.error("Exception while listing aliases", e);
        }
        return new HashSet<>();
    }

    @Override
    public Credentials getCredentials(String alias) {

        try {
            if (getVault() != null) {
                LogicalResponse response = getVault().logical().read(ROOT_PATH + PREFIX + alias);
                Map<String, String> data = response.getData();
                String username = data.remove(USERNAME);
                String password = data.remove(PASSWORD);
                return new Credentials(username, password, data);
            } else {
                throw new IllegalStateException("Vault Service not initialized");
            }

        } catch (VaultException e) {
            LOG.error("Exception while getting credentials for alias {}", alias, e);
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
            if (getVault() != null) {
                getVault().logical().write(ROOT_PATH + PREFIX + alias, kvMap);
            } else {
                throw new IllegalStateException("Vault Service not initialized");
            }
        } catch (VaultException e) {
            LOG.error("Exception while setting credentials for alias {}", alias, e);
        }
    }

    public VaultService getVaultConfigService() {
        return vaultConfigService;
    }

    @VisibleForTesting
    Vault getVault() {
        return vaultConfigService.getVault();
    }

    public void setVaultConfigService(VaultService vaultConfigService) {
        this.vaultConfigService = vaultConfigService;
    }
}
