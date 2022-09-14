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

package org.opennms.features.scv.vault.config;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import org.json.JSONObject;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HcpVaultService implements VaultService {

    private static final Logger LOG = LoggerFactory.getLogger(HcpVaultService.class);
    private static final String HCP_VAULT_CONTEXT = "hcp-vault";
    private static final String HCP_VAULT_CONFIG = "config";

    private Vault vault;

    private final JsonStore jsonStore;

    public HcpVaultService(JsonStore jsonStore) {
        this.jsonStore = jsonStore;
    }

    public void init() {

        var jsonConfig = jsonStore.get(HCP_VAULT_CONFIG, HCP_VAULT_CONTEXT);
        if (jsonConfig.isPresent()) {
            JSONObject jsonObject = new JSONObject(jsonConfig.get());
            String vaultAddress = jsonObject.getString("address");
            try {
                VaultConfig vaultConfig = new VaultConfig()
                        .token(jsonObject.getString("token"))
                        .address(vaultAddress)
                        .build();
                initializeVault(vaultConfig);
            } catch (VaultException e) {
                LOG.error("Error initializing vault config for address {}", vaultAddress, e);
            }
        }
    }

    @Override
    public Vault getVault() {
        return vault;
    }

    @Override
    public synchronized void initializeVault(VaultConfig vaultConfig) {

        try {
            vault = new Vault(vaultConfig);
            vault.auth().lookupSelf();
            JSONObject vaultJsonConfig = new JSONObject();
            vaultJsonConfig.put("address", vaultConfig.getAddress());
            vaultJsonConfig.put("token", vaultConfig.getToken());
            jsonStore.put(HCP_VAULT_CONFIG, vaultJsonConfig.toString(), HCP_VAULT_CONTEXT);
            LOG.info("HCP Vault initialized");
        } catch (VaultException e) {
            vault = null;
            LOG.error("Error initializing vault with address {}", vaultConfig.getAddress(), e);
        }
    }

}
