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
package org.opennms.web.rest.support;

import org.junit.Test;
import org.mockito.stubbing.Answer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.SecurityContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.web.api.Authentication.ROLE_ADMIN;
import static org.opennms.web.api.Authentication.ROLE_DELEGATE;
import static org.opennms.web.api.Authentication.ROLE_MOBILE;
import static org.opennms.web.api.Authentication.ROLE_READONLY;
import static org.opennms.web.api.Authentication.ROLE_REST;
import static org.opennms.web.api.Authentication.ROLE_USER;

public class SecurityHelperTest {
    private static final String USER = "joe";
    private static final String OTHER_USER = "bob";

    @Test
    public void assertUserEditPrivilegesWithSameAckUser() {
        // Admin always allowed
        assertUserEditPrivileges(true, USER, ROLE_ADMIN);

        // REST, USER and MOBILE roles allowed
        assertUserEditPrivileges(true, USER, ROLE_USER);
        assertUserEditPrivileges(true, USER, ROLE_REST);
        assertUserEditPrivileges(true, USER, ROLE_MOBILE);

        // No role rejected
        assertUserEditPrivileges(false, USER);

        // Read-only users rejected
        assertUserEditPrivileges(false, USER, ROLE_USER, ROLE_READONLY);
    }

    @Test
    public void assertUserEditPrivilegesWithDifferentAckUser() {
        // Admin always allowed
        assertUserEditPrivileges(true, OTHER_USER, ROLE_ADMIN);

        // REST, USER and MOBILE roles not allowed
        assertUserEditPrivileges(false, OTHER_USER, ROLE_USER);
        assertUserEditPrivileges(false, OTHER_USER, ROLE_REST);
        assertUserEditPrivileges(false, OTHER_USER, ROLE_MOBILE);

        // REST, USER and MOBILE roles allowed when they have the delegate role too
        assertUserEditPrivileges(true, OTHER_USER, ROLE_USER, ROLE_DELEGATE);
        assertUserEditPrivileges(true, OTHER_USER, ROLE_REST, ROLE_DELEGATE);
        assertUserEditPrivileges(true, OTHER_USER, ROLE_MOBILE, ROLE_DELEGATE);
    }

    // --- maskCredentials tests ---

    @Test
    public void maskCredentialsShouldMaskAnnotatedNonNullFields() {
        TestDto dto = new TestDto();
        dto.password = "realpass";
        dto.other = "keepme";

        SecurityHelper.maskCredentials(dto);

        assertEquals(SecurityHelper.MASKED_PASSWORD, dto.password);
        assertEquals("keepme", dto.other);
    }

    @Test
    public void maskCredentialsShouldSkipNullAnnotatedFields() {
        TestDto dto = new TestDto();
        dto.password = null;

        SecurityHelper.maskCredentials(dto);

        assertNull(dto.password);
    }

    @Test
    public void maskCredentialsShouldNotAffectNonAnnotatedFields() {
        TestDto dto = new TestDto();
        dto.other = "untouched";

        SecurityHelper.maskCredentials(dto);

        assertEquals("untouched", dto.other);
    }

    @Test
    public void maskCredentialsShouldHandleNullDto() {
        SecurityHelper.maskCredentials(null); // must not throw
    }

    @Test
    public void maskCredentialsShouldNotMaskScvExpression() {
        TestDto dto = new TestDto();
        dto.password = "${scv:myalias:mykey}";

        SecurityHelper.maskCredentials(dto);

        assertEquals("${scv:myalias:mykey}", dto.password);
    }

    @Test
    public void maskCredentialsShouldNotMaskMinimalScvExpression() {
        TestDto dto = new TestDto();
        dto.password = "${scv:a:k}";

        SecurityHelper.maskCredentials(dto);

        assertEquals("${scv:a:k}", dto.password);
    }

    // --- resolveCredentials tests ---

    @Test
    public void resolveCredentialsShouldFillMaskedFieldFromEntity() {
        TestDto incoming = new TestDto();
        incoming.password = SecurityHelper.MASKED_PASSWORD;

        TestDto existing = new TestDto();
        existing.password = "realpass";

        SecurityHelper.resolveCredentials(incoming, () -> existing);

        assertEquals("realpass", incoming.password);
    }

    @Test
    public void resolveCredentialsShouldLeaveRealValueUnchanged() {
        TestDto incoming = new TestDto();
        incoming.password = "newpass";

        SecurityHelper.resolveCredentials(incoming, () -> { throw new AssertionError("supplier must not be called"); });

        assertEquals("newpass", incoming.password);
    }

    @Test
    public void resolveCredentialsShouldLeaveScvPlaceholderUnchanged() {
        TestDto incoming = new TestDto();
        incoming.password = "${scv:alias:key}";

        SecurityHelper.resolveCredentials(incoming, () -> { throw new AssertionError("supplier must not be called"); });

        assertEquals("${scv:alias:key}", incoming.password);
    }

    // --- isMaskedPassword tests ---

    @Test
    public void isMaskedPasswordShouldReturnTrueForExactMaskedPassword() {
        assertTrue(SecurityHelper.isMaskedPassword(SecurityHelper.MASKED_PASSWORD));
    }

    @Test
    public void isMaskedPasswordShouldReturnFalseForNull() {
        assertFalse(SecurityHelper.isMaskedPassword(null));
    }

    @Test
    public void isMaskedPasswordShouldReturnFalseForFewerStars() {
        assertFalse(SecurityHelper.isMaskedPassword("*****")); // 5 asterisks
        assertFalse(SecurityHelper.isMaskedPassword("**"));    // 2 asterisks
    }

    @Test
    public void isMaskedPasswordShouldReturnFalseForMoreStars() {
        assertFalse(SecurityHelper.isMaskedPassword("*******")); // 7 asterisks
    }

    @Test
    public void isMaskedPasswordShouldReturnFalseForNonMaskedValues() {
        assertFalse(SecurityHelper.isMaskedPassword(""));
        assertFalse(SecurityHelper.isMaskedPassword("realpassword"));
        assertFalse(SecurityHelper.isMaskedPassword("*password"));
    }

    // --- isScvExpression tests ---

    @Test
    public void isScvExpressionShouldReturnTrueForWellFormedExpressions() {
        assertTrue(SecurityHelper.isScvExpression("${scv:alias:key}"));
        assertTrue(SecurityHelper.isScvExpression("${scv:a:k}"));
        assertTrue(SecurityHelper.isScvExpression("${scv:my-alias:my-key}"));
    }

    @Test
    public void isScvExpressionShouldReturnFalseForNonScvValues() {
        assertFalse(SecurityHelper.isScvExpression(null));
        assertFalse(SecurityHelper.isScvExpression(""));
        assertFalse(SecurityHelper.isScvExpression("realpassword"));
        assertFalse(SecurityHelper.isScvExpression(SecurityHelper.MASKED_PASSWORD));
        assertFalse(SecurityHelper.isScvExpression("${scv:}"));   // no content after prefix
        assertFalse(SecurityHelper.isScvExpression("${other:a:k}"));
    }

    @Test
    public void resolveCredentialsShouldLeaveNullFieldUnchanged() {
        TestDto incoming = new TestDto();
        incoming.password = null;

        SecurityHelper.resolveCredentials(incoming, () -> { throw new AssertionError("supplier must not be called"); });

        assertNull(incoming.password);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveCredentialsShouldThrowWhenEntityNullAndValueMasked() {
        TestDto incoming = new TestDto();
        incoming.password = SecurityHelper.MASKED_PASSWORD;

        SecurityHelper.resolveCredentials(incoming, () -> null);
    }

    @Test
    public void resolveCredentialsShouldHandleNullIncomingDto() {
        SecurityHelper.resolveCredentials(null, () -> { throw new AssertionError("supplier must not be called"); });
    }

    @Test
    public void resolveCredentialsShouldCallSupplierOnlyOnce() {
        int[] callCount = {0};
        TestDtoTwoFields incoming = new TestDtoTwoFields();
        incoming.password1 = SecurityHelper.MASKED_PASSWORD;
        incoming.password2 = SecurityHelper.MASKED_PASSWORD;

        TestDtoTwoFields existing = new TestDtoTwoFields();
        existing.password1 = "real1";
        existing.password2 = "real2";

        SecurityHelper.resolveCredentials(incoming, () -> {
            callCount[0]++;
            return existing;
        });

        assertEquals(1, callCount[0]);
        assertEquals("real1", incoming.password1);
        assertEquals("real2", incoming.password2);
    }

    private static class TestDto {
        @MaskedCredential
        String password;
        String other;
    }

    private static class TestDtoTwoFields {
        @MaskedCredential
        String password1;
        @MaskedCredential
        String password2;
    }

    private void assertUserEditPrivileges(boolean isAllowed, String ackUser, String... roles) {
        final Set<String> userRoles = new HashSet<>(Arrays.asList(roles));
        SecurityContext securityContext = mock(SecurityContext.class, RETURNS_DEEP_STUBS);
        when(securityContext.getUserPrincipal().getName()).thenReturn(USER);
        when(securityContext.isUserInRole(anyString())).thenAnswer((Answer) invocation -> {
            final String role = invocation.getArgument(0);
            return userRoles.contains(role);
        });

        WebApplicationException ex = null;
        try {
            SecurityHelper.assertUserEditCredentials(securityContext, ackUser);
        } catch (WebApplicationException e) {
            ex = e;
        }

        if (isAllowed) {
            assertNull("Should be allowed, but got: " + ex, ex);
        } else {
            assertNotNull("Should not be allowed, but passed.", ex);
        }
    }
}
