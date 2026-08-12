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

import java.lang.reflect.Field;
import java.util.function.Supplier;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.opennms.features.scv.utils.ScvUtils;
import org.opennms.web.api.Authentication;

public class SecurityHelper {
    public static final String MASKED_PASSWORD = "******";

    public static void assertUserReadCredentials(SecurityContext securityContext) {
        final String currentUser = securityContext.getUserPrincipal().getName();

        if (securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            // admin can do anything
            return;
        }
        if (securityContext.isUserInRole(Authentication.ROLE_REST) ||
                securityContext.isUserInRole(Authentication.ROLE_USER) ||
                securityContext.isUserInRole(Authentication.ROLE_MOBILE)) {
            return;
        }
        // otherwise
        throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("User '" + currentUser + "', is not allowed to read alarms.").type(MediaType.TEXT_PLAIN).build());
    }

    public static void assertUserEditCredentials(final SecurityContext securityContext, final String ackUser) {
        final String currentUser = securityContext.getUserPrincipal().getName();

        if (securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            // admin can do anything
            return;
        }
        if (securityContext.isUserInRole(Authentication.ROLE_READONLY)) {
            // read only is not allowed to edit
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("User '" + currentUser + "', is a read-only user!").type(MediaType.TEXT_PLAIN).build());
        }
        if (securityContext.isUserInRole(Authentication.ROLE_REST) ||
                securityContext.isUserInRole(Authentication.ROLE_USER) ||
                securityContext.isUserInRole(Authentication.ROLE_MOBILE)) {
            if (ackUser.equals(currentUser) || (!ackUser.equals(currentUser) && securityContext.isUserInRole(Authentication.ROLE_DELEGATE))) {
                // ROLE_REST and ROLE_MOBILE are allowed to modify things as long as it's as the
                // same user as they're logging in with, or if they also have ROLE_DELEGATE.
                return;
            }
        }
        // otherwise
        throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("User '" + currentUser + "', is not allowed to perform updates to alarms as user '" + ackUser + "'").type(MediaType.TEXT_PLAIN).build());
    }

    public static boolean isMaskedPassword(final String value) {
        return MASKED_PASSWORD.equals(value);
    }

    public static boolean isScvExpression(final String value) {
        return ScvUtils.isScvExpression(value);
    }

    /**
     * Replaces each {@link MaskedCredential}-annotated field on {@code dto} with
     * {@link #MASKED_PASSWORD}. Null fields and SCV expressions ({@code ${scv:...}})
     * are left unchanged. Only the declared fields of the object's own class are inspected.
     */
    public static void maskCredentials(final Object dto) {
        if (dto == null) {
            return;
        }
        for (Field field : dto.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(MaskedCredential.class)) {
                continue;
            }
            field.setAccessible(true);
            try {
                final Object value = field.get(dto);
                if (value instanceof String strValue && !isScvExpression(strValue)) {
                    field.set(dto, MASKED_PASSWORD);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to mask credential field: " + field.getName(), e);
            }
        }
    }

    /**
     * For each {@link MaskedCredential}-annotated field on {@code incomingDto}: if the
     * field value is a mask (per {@link #isMaskedPassword}), the supplier is called to
     * obtain the existing entity and the real value is copied from the matching field name
     * on that entity. Non-masked values (including SCV placeholders) are left unchanged.
     *
     * <p>The supplier is lazy: it is only invoked the first time a masked field is
     * encountered. If the supplier returns {@code null} and a masked field is found,
     * an {@link IllegalArgumentException} is thrown — callers should surface this as a
     * 400 response.</p>
     *
     * @throws IllegalArgumentException if a masked value is found but the supplier
     *                                  returns {@code null} (no existing entity to look up)
     */
    public static void resolveCredentials(final Object incomingDto, final Supplier<?> existingEntitySupplier) {
        if (incomingDto == null) {
            return;
        }
        Object existing = null;
        boolean existingLoaded = false;
        for (Field field : incomingDto.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(MaskedCredential.class)) {
                continue;
            }
            field.setAccessible(true);
            try {
                final Object value = field.get(incomingDto);
                if (!(value instanceof String) || !isMaskedPassword((String) value)) {
                    continue;
                }
                if (!existingLoaded) {
                    existing = existingEntitySupplier.get();
                    existingLoaded = true;
                }
                if (existing == null) {
                    throw new IllegalArgumentException(
                        "Cannot use a masked passphrase for a new entry: no existing record to retrieve field '"
                            + field.getName() + "' from");
                }
                try {
                    Field entityField = existing.getClass().getDeclaredField(field.getName());
                    entityField.setAccessible(true);
                    field.set(incomingDto, entityField.get(existing));
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException(
                        "Cannot resolve masked field '" + field.getName() + "': no matching field on entity "
                            + existing.getClass().getSimpleName(), e);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to resolve credential field: " + field.getName(), e);
            }
        }
    }
}
