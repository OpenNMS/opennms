/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.repository.remote;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.cxf.common.util.Base64Utility;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;

public class NMS15764IT {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void checkMetadata() throws Exception {
        final File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
        secureCredentialsVault.setCredentials("remote", new Credentials("john", "doe"));

        final RemoteRepositoryDefinition repositoryDefinition = new RemoteRepositoryDefinition();
        repositoryDefinition.setLoginUser("${scv:remote:username}");
        repositoryDefinition.setLoginRepoPassword("${scv:remote:password}");

        final DefaultRemoteRepository defaultRemoteRepository = new DefaultRemoteRepository(repositoryDefinition, "", secureCredentialsVault);
        assertEquals("Basic " + Base64Utility.encode(("john:doe").getBytes()), defaultRemoteRepository.m_authorizationHeader);
    }
}
