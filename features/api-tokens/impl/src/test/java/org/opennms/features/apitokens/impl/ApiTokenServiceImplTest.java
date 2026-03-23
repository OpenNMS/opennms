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
package org.opennms.features.apitokens.impl;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.apitokens.ApiToken;
import org.opennms.features.apitokens.ApiTokenCreateResponse;
import org.opennms.features.apitokens.ApiTokenDao;

import java.util.Date;

public class ApiTokenServiceImplTest {
    private ApiTokenServiceImpl service;
    private ApiTokenDao mockDao;

    @Before
    public void setUp() {
        mockDao = mock(ApiTokenDao.class);
        service = new ApiTokenServiceImpl();
        service.setApiTokenDao(mockDao);
        service.setMaxExpiryDays(365);
        service.setDefaultExpiryDays(90);
        service.setMaxTokensPerUser(50);
    }

    @Test
    public void testCreateTokenReturnsPlaintext() {
        when(mockDao.countByUsername("admin")).thenReturn(0);
        when(mockDao.save(any(ApiToken.class))).thenReturn(1);

        ApiTokenCreateResponse response = service.createToken("admin", "test token", 30);

        assertNotNull(response.getToken());
        assertTrue(response.getToken().startsWith("onms_"));
        assertEquals("test token", response.getDescription());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getExpiresAt());
        verify(mockDao).save(any(ApiToken.class));
    }

    @Test
    public void testCreateTokenUsesDefaultExpiry() {
        when(mockDao.countByUsername("admin")).thenReturn(0);
        when(mockDao.save(any(ApiToken.class))).thenReturn(1);

        ApiTokenCreateResponse response = service.createToken("admin", "test", null);

        long diffMs = response.getExpiresAt().getTime() - response.getCreatedAt().getTime();
        long diffDays = diffMs / (1000 * 60 * 60 * 24);
        assertEquals(90, diffDays);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTokenRejectsExcessiveExpiry() {
        when(mockDao.countByUsername("admin")).thenReturn(0);
        service.createToken("admin", "test", 999);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTokenRejectsLongDescription() {
        when(mockDao.countByUsername("admin")).thenReturn(0);
        String longDesc = "a".repeat(257);
        service.createToken("admin", longDesc, 30);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateTokenRejectsWhenMaxTokensReached() {
        when(mockDao.countByUsername("admin")).thenReturn(50);
        service.createToken("admin", "test", 30);
    }

    @Test
    public void testAuthenticateValidToken() {
        String plaintext = "onms_abcdef1234567890abcdef1234567890abcdef1234567890abcdef12345678";
        String hash = ApiTokenServiceImpl.sha256Hex(plaintext);

        ApiToken token = new ApiToken();
        token.setId(1);
        token.setTokenHash(hash);
        token.setUsername("admin");
        token.setExpiresAt(new Date(System.currentTimeMillis() + 86400000));

        when(mockDao.findByTokenHash(hash)).thenReturn(token);

        String result = service.authenticate(plaintext);
        assertEquals("admin", result);
    }

    @Test
    public void testAuthenticateExpiredToken() {
        String plaintext = "onms_abcdef1234567890abcdef1234567890abcdef1234567890abcdef12345678";
        String hash = ApiTokenServiceImpl.sha256Hex(plaintext);

        ApiToken token = new ApiToken();
        token.setId(1);
        token.setTokenHash(hash);
        token.setUsername("admin");
        token.setExpiresAt(new Date(System.currentTimeMillis() - 86400000));

        when(mockDao.findByTokenHash(hash)).thenReturn(token);

        assertNull(service.authenticate(plaintext));
    }

    @Test
    public void testAuthenticateInvalidToken() {
        when(mockDao.findByTokenHash(anyString())).thenReturn(null);
        assertNull(service.authenticate("onms_doesnotexist"));
    }

    @Test
    public void testAuthenticateNullAndNonPrefixed() {
        assertNull(service.authenticate(null));
        assertNull(service.authenticate("not_a_token"));
    }
}
