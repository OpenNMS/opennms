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
package org.opennms.web.rest.v1;

import java.security.Principal;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.opennms.netmgt.config.UserDataChangedException;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v2.UserMgmtValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * What the deprecated v1 user and group services share: the form keys they accept, the application of a form to a
 * bean, the translation of a failed write to an HTTP error, and the masking of password hashes.
 */
final class V1UserMgmtForms {

    private static final Logger LOG = LoggerFactory.getLogger(V1UserMgmtForms.class);

    /** The keys of a user form PUT; hashPassword is a flag, not a property. */
    static final Set<String> USER_FORM_KEYS = Set.of("fullName", "comments", "email", "password", "hashPassword", "dutySchedule", "roles");

    /** The keys of a group form PUT. */
    static final Set<String> GROUP_FORM_KEYS = Set.of("comments", "users");

    static final String RENAME_USER = "Renaming is not supported by the deprecated v1 API; use PUT /api/v2/users/{userId}/rename.";

    static final String RENAME_GROUP = "Renaming is not supported by the deprecated v1 API; use PUT /api/v2/groups/{name}/rename.";

    private V1UserMgmtForms() {
    }

    /**
     * Refuses the form with 400 unless every key is one of {@code allowed}, spelled exactly; checked before anything
     * is applied. Spring's bean wrapper also accepts other spellings and paths, such as Username or roles[0], which
     * would bypass the checks of the plain fields.
     *
     * @param renameMessage the message for a key that would rename the user or group
     */
    static void checkFormKeys(final MultivaluedMapImpl params, final Set<String> allowed, final String renameMessage) {
        for (final String key : params.keySet()) {
            if (allowed.contains(key)) {
                continue;
            }
            final String lower = key.toLowerCase(Locale.ROOT);
            if ("username".equals(lower) || "name".equals(lower)) {
                throw badRequest(renameMessage);
            }
            throw badRequest("The form field " + key + " is not supported. Supported fields: "
                    + String.join(", ", new TreeSet<>(allowed)) + ".");
        }
    }

    /**
     * Sets the bean properties of the form's keys, except those in {@code skip}; call {@link #checkFormKeys} first.
     *
     * @return whether anything was set
     */
    static boolean applyForm(final Object bean, final MultivaluedMapImpl params, final Set<String> skip) {
        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        boolean modified = false;
        for (final String key : params.keySet()) {
            if (skip.contains(key)) {
                continue;
            }
            final Object value = wrapper.convertIfNecessary(params.getFirst(key), wrapper.getPropertyType(key));
            wrapper.setPropertyValue(key, value);
            modified = true;
        }
        return modified;
    }

    /**
     * Answers what the checks and the managers refuse with 400 or 409, wherever it is in the cause chain (the group
     * DAO wraps what the manager throws); only a real failure is a 500.
     */
    static WebApplicationException translate(final Throwable t) {
        for (Throwable cause = t; cause != null; cause = cause.getCause()) {
            if (cause instanceof WebApplicationException) {
                return (WebApplicationException) cause;
            }
            if (cause instanceof UserMgmtValidationException || cause instanceof IllegalArgumentException) {
                return error(Status.BAD_REQUEST, cause);
            }
            if (cause instanceof UserDataChangedException) {
                return error(Status.CONFLICT, cause);
            }
        }
        return error(Status.INTERNAL_SERVER_ERROR, t);
    }

    /** Masks the password hash of every user but the caller for a caller without the admin role. */
    static OnmsUser filterUserPassword(final SecurityContext securityContext, final OnmsUser user) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            final Principal principal = securityContext.getUserPrincipal();
            // users may see their own password hash
            if (!user.getUsername().equals(principal.getName())) {
                user.setPassword("xxxxxxxx");
                user.setPasswordSalted(false);
            }
        }
        return user;
    }

    static WebApplicationException badRequest(final String message) {
        return new WebApplicationException(Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(message).build());
    }

    private static WebApplicationException error(final Status status, final Throwable t) {
        if (status == Status.INTERNAL_SERVER_ERROR) {
            LOG.error(t.getMessage(), t);
        } else {
            LOG.debug(t.getMessage(), t);
        }
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(t.getMessage()).build());
    }
}
