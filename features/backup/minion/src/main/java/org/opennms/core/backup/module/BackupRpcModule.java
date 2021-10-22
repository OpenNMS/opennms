/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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


package org.opennms.core.backup.module;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.features.backup.api.BackupRequestDTO;
import org.opennms.features.backup.api.BackupResponseDTO;
import org.opennms.features.backup.api.Config;
import org.opennms.features.backup.minion.CiscoBackupStrategy;

public class BackupRpcModule extends AbstractXmlRpcModule<BackupRequestDTO, BackupResponseDTO> {

    private final String BACK_UP_RPC_MODULE_ID = "backup";

    public BackupRpcModule() {
        super(BackupRequestDTO.class, BackupResponseDTO.class);
    }

    @Override
    public CompletableFuture<BackupResponseDTO> execute(BackupRequestDTO request) {
        CompletableFuture<BackupResponseDTO> future = new CompletableFuture<>();
        CiscoBackupStrategy strategy = new CiscoBackupStrategy();
        Map<String,String> params = new LinkedHashMap<>();
        request.getAttributes().forEach(e -> params.put(e.getKey(), e.getValue()));
        try {
            Config config = strategy.getConfig(request.getHost(), 22, params);
            BackupResponseDTO responseDTO = new BackupResponseDTO();
            if (config.getData() != null) {
                responseDTO.setResponse(new String(config.getData(), StandardCharsets.UTF_8));
            }
            future.complete(responseDTO);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public String getId() {
        return BACK_UP_RPC_MODULE_ID;
    }

    @Override
    public BackupResponseDTO createResponseWithException(Throwable ex) {
        return new BackupResponseDTO(ex.getMessage());
    }
}
