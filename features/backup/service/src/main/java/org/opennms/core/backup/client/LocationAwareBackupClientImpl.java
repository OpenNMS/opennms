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

package org.opennms.core.backup.client;

import java.util.concurrent.CompletableFuture;

import org.opennms.core.backup.module.BackupRpcModule;
import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.features.backup.BackupRequestBuilder;
import org.opennms.features.backup.LocationAwareBackupClient;
import org.opennms.features.backup.api.BackupRequestDTO;
import org.opennms.features.backup.api.BackupResponseDTO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationAwareBackupClientImpl implements RpcClient<BackupRequestDTO, BackupResponseDTO>, LocationAwareBackupClient, InitializingBean {

    @Autowired
    private RpcClientFactory m_rpcClientFactory;

    private RpcClient<BackupRequestDTO, BackupResponseDTO> m_delegate;

    @Override
    public void afterPropertiesSet() {
        m_delegate = m_rpcClientFactory.getClient(new BackupRpcModule());
    }

    @Override
    public CompletableFuture<BackupResponseDTO> execute(BackupRequestDTO request) {
        return m_delegate.execute(request);
    }

    @Override
    public BackupRequestBuilder backup() {
        return new BackupRequestBuilderImpl(this);
    }
}
