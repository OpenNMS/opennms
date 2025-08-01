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
package org.opennms.features.apilayer.common.scv;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.features.apilayer.common.scv.SecureCredentialsVaultImpl.OIA_PREFIX;

import java.util.Set;

import org.junit.Test;
import org.opennms.features.scv.api.Credentials;

public class SecureCredentialsVaultImplTest {

    @Test
    public void shouldOnlyReturnOiaRelevantCredentials() {
        String oiaKey = OIA_PREFIX + "ccc";
        org.opennms.features.scv.api.SecureCredentialsVault delegate = mock(org.opennms.features.scv.api.SecureCredentialsVault.class);
        org.opennms.integration.api.v1.scv.SecureCredentialsVault scv = new SecureCredentialsVaultImpl(delegate);
        when(delegate.getAliases()).thenReturn(Set.of("aaa", "bbb", oiaKey));
        when(delegate.getCredentials(oiaKey)).thenReturn(new Credentials("user", "password"));

        assertEquals(Set.of("ccc"), scv.getAliases());
        assertNull(scv.getCredentials("aaa"));
        assertNotNull(scv.getCredentials( "ccc"));
    }
}