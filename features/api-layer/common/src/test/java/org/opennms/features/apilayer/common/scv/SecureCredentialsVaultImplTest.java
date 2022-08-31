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