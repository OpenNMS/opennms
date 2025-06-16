/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
