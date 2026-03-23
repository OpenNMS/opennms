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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.opennms.features.apitokens.ApiToken;
import org.opennms.features.apitokens.ApiTokenCreateResponse;
import org.opennms.features.apitokens.ApiTokenDao;
import org.opennms.features.apitokens.ApiTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class ApiTokenServiceImpl implements ApiTokenService {
    private static final Logger LOG = LoggerFactory.getLogger(ApiTokenServiceImpl.class);
    private static final String TOKEN_PREFIX = "onms_";
    private static final long LAST_USED_DEBOUNCE_MS = 5 * 60 * 1000; // 5 minutes
    private static final int MAX_DEBOUNCE_ENTRIES = 10_000;
    private static final int MAX_CREATION_LOCK_ENTRIES = 10_000;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<Integer, Long> lastUsedUpdateTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Lock> creationLocks = new ConcurrentHashMap<>();

    private ApiTokenDao apiTokenDao;
    private int maxExpiryDays = 365;
    private int defaultExpiryDays = 365;
    private int maxTokensPerUser = 50;

    @Override
    @Transactional
    public String authenticate(String plaintextToken) {
        if (plaintextToken == null || !plaintextToken.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        String hash = sha256Hex(plaintextToken);
        ApiToken token = apiTokenDao.findByTokenHash(hash);
        if (token == null) {
            return null;
        }
        if (token.getExpiresAt().before(new Date())) {
            return null;
        }
        // Debounced last_used_at update
        Long lastUpdate = lastUsedUpdateTimes.get(token.getId());
        long now = System.currentTimeMillis();
        if (lastUpdate == null || (now - lastUpdate) > LAST_USED_DEBOUNCE_MS) {
            token.setLastUsedAt(new Date());
            apiTokenDao.saveOrUpdate(token);
            lastUsedUpdateTimes.put(token.getId(), now);
            if (lastUsedUpdateTimes.size() > MAX_DEBOUNCE_ENTRIES) {
                lastUsedUpdateTimes.clear();
            }
        }
        return token.getUsername();
    }

    @Override
    @Transactional
    public ApiTokenCreateResponse createToken(String username, String description, Integer expiresInDays) {
        if (maxExpiryDays == 0) {
            throw new IllegalStateException("API token creation is disabled");
        }
        int days = expiresInDays != null ? expiresInDays : defaultExpiryDays;
        if (days > maxExpiryDays) {
            throw new IllegalArgumentException("Requested expiry exceeds the allowed maximum");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("Expiry must be positive");
        }
        if (description != null && description.length() > 256) {
            throw new IllegalArgumentException("Description must be 256 characters or less");
        }

        // Per-user lock to prevent TOCTOU race on maxTokensPerUser
        Lock lock = creationLocks.computeIfAbsent(username, k -> new ReentrantLock());
        if (creationLocks.size() > MAX_CREATION_LOCK_ENTRIES) {
            creationLocks.clear();
        }
        lock.lock();
        try {
            int count = apiTokenDao.countByUsername(username);
            if (count >= maxTokensPerUser) {
                throw new IllegalStateException("Maximum number of tokens reached for user");
            }

            // Generate token
            byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);
            String plaintextToken = TOKEN_PREFIX + bytesToHex(randomBytes);
            String hash = sha256Hex(plaintextToken);

            Instant now = Instant.now();
            ApiToken token = new ApiToken();
            token.setTokenHash(hash);
            token.setUsername(username);
            token.setDescription(description);
            token.setCreatedAt(Date.from(now));
            token.setExpiresAt(Date.from(now.plus(days, ChronoUnit.DAYS)));

            apiTokenDao.save(token);
            apiTokenDao.flush();

            LOG.info("API token created for user {} (id={}, expires={})", username, token.getId(), token.getExpiresAt());

            ApiTokenCreateResponse response = new ApiTokenCreateResponse();
            response.setId(token.getId());
            response.setToken(plaintextToken);
            response.setDescription(description);
            response.setCreatedAt(token.getCreatedAt());
            response.setExpiresAt(token.getExpiresAt());
            return response;
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiToken> listTokens(String username) {
        return apiTokenDao.findByUsername(username);
    }

    @Override
    @Transactional
    public boolean revokeToken(Integer tokenId) {
        ApiToken token = apiTokenDao.get(tokenId);
        if (token == null) {
            return false;
        }
        LOG.info("API token revoked for user {} (id={})", token.getUsername(), tokenId);
        apiTokenDao.delete(token);
        lastUsedUpdateTimes.remove(tokenId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public String getTokenOwner(Integer tokenId) {
        return apiTokenDao.findUsernameByTokenId(tokenId);
    }

    @Override
    @Transactional
    public int revokeAllTokens(String username) {
        int count = apiTokenDao.countByUsername(username);
        if (count > 0) {
            apiTokenDao.deleteByUsername(username);
            lastUsedUpdateTimes.clear();
            LOG.info("All {} API tokens revoked for user {}", count, username);
        }
        return count;
    }

    static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void setApiTokenDao(ApiTokenDao apiTokenDao) { this.apiTokenDao = apiTokenDao; }
    public void setMaxExpiryDays(int maxExpiryDays) { this.maxExpiryDays = maxExpiryDays; }
    public void setDefaultExpiryDays(int defaultExpiryDays) { this.defaultExpiryDays = defaultExpiryDays; }
    public void setMaxTokensPerUser(int maxTokensPerUser) { this.maxTokensPerUser = maxTokensPerUser; }
}
