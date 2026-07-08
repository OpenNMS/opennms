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
package org.opennms.web.springframework.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;

/**
 * <p>Computes an uppercase, hex-encoded MD5 hash of a password, reproducing the behaviour of the
 * Spring Security 3.x-era {@code Md5PasswordEncoder} (and its {@code mergePasswordAndSalt} salt
 * handling) so that pre-existing OpenNMS password hashes continue to validate.</p>
 *
 * <p>Spring Security 5.x removed {@code org.springframework.security.authentication.encoding}, so the
 * hashing is implemented directly here rather than by extending the removed {@code Md5PasswordEncoder}.
 * It implements the current {@link PasswordEncoder} contract so it can still be wired as the
 * {@code passwordEncoder} of a {@code DaoAuthenticationProvider} in user-maintained
 * {@code spring-security.d} configurations.</p>
 */
public class UpperCaseMd5PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(final CharSequence rawPassword) {
        return encodePassword(rawPassword == null ? null : rawPassword.toString(), null);
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        // constant-time comparison; case-normalize since stored hashes may be lower-case
        return MessageDigest.isEqual(encode(rawPassword).getBytes(StandardCharsets.UTF_8),
                encodedPassword.toUpperCase().getBytes(StandardCharsets.UTF_8));
    }

    public String encodePassword(final String rawPass, final Object salt) {
        final byte[] bytes = mergePasswordAndSalt(rawPass, salt).getBytes(StandardCharsets.UTF_8);
        return DigestUtils.md5DigestAsHex(bytes).toUpperCase();
    }

    private static String mergePasswordAndSalt(final String password, final Object salt) {
        final String pass = (password == null) ? "" : password;
        if (salt == null || "".equals(salt)) {
            return pass;
        }
        return pass + "{" + salt.toString() + "}";
    }
}
