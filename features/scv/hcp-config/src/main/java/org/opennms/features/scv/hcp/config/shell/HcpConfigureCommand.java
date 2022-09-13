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

package org.opennms.features.scv.hcp.config.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import org.opennms.features.scv.hcp.config.service.VaultConfigService;

@Command(scope = "opennms", name = "hcp-configure", description = "Set the token and address to use for authentication with vault")
@Service
public class HcpConfigureCommand implements Action {

    @Reference
    private VaultConfigService vaultConfigService;

    @Option(name = "-a", aliases = "--address", description = "Vault address", required = false, multiValued = false)
    String address = "http://127.0.0.1:8200";

    @Argument(index = 0, name = "token", description = "Vault token", required = true, multiValued = false)
    String token;

    @Override
    public Object execute() {
        vaultConfigService.setToken(token);
        vaultConfigService.setVaultAddress(address);
        return null;
    }

}
