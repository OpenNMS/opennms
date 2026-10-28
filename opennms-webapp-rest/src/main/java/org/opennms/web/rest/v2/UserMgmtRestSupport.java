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
package org.opennms.web.rest.v2;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.opennms.netmgt.config.UserDataChangedException;
import org.opennms.netmgt.config.UserMgmtManagers;
import org.opennms.netmgt.config.api.UserMgmtStore;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.VersionedTable;
import org.opennms.web.api.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Shared plumbing for the v2 user-management services (users, groups, security roles, on-call roles):
 * the admin and store-mode checks, the write lock with its after-commit manager reload, the
 * ETag / If-Match handling, and the translation of exceptions to HTTP errors.
 *
 * Every error is thrown, never returned as a Response, so that the surrounding transaction rolls back.
 */
@Component
public class UserMgmtRestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(UserMgmtRestSupport.class);

    /** One entity tag, strong or weak; group 1 is the opaque value. */
    private static final String TAG = "(?:W/)?\"([^\"]*)\"";
    private static final Pattern ENTITY_TAG = Pattern.compile(TAG);
    /** A comma-separated list of entity tags with at least one tag; empty elements are ignored (RFC 9110, 5.6.1.2). */
    private static final Pattern ENTITY_TAG_LIST = Pattern.compile("(?:\\s*,)*\\s*" + TAG + "(?:(?:\\s*,)+\\s*" + TAG + ")*(?:\\s*,)*\\s*");

    private static final String DEADLOCK = "40P01";
    private static final String SERIALIZATION_FAILURE = "40001";

    @Autowired
    private UserMgmtVersionDao m_versionDao;

    @Autowired
    private UserMgmtManagers m_managers;

    public void assertAdmin(final SecurityContext securityContext, final String what) {
        if (securityContext == null || !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw error(Status.FORBIDDEN, what + " requires the admin role.");
        }
    }

    public void requireDatabaseStore() {
        if (!UserMgmtStore.isDatabase()) {
            throw error(Status.SERVICE_UNAVAILABLE,
                    "User management through this API needs " + UserMgmtStore.PROPERTY + "=" + UserMgmtStore.DB + ".");
        }
    }

    /**
     * Takes the write lock (users, then groups) and arranges for the in-memory managers to be reloaded
     * once the surrounding transaction commits. Must be called inside an active transaction.
     * <p>
     * The committed transaction's session is still bound to the thread in {@code afterCommit}, so data access
     * there would join it. The managers' reloads run in a new transaction (PROPAGATION_REQUIRES_NEW), with a
     * new session, so they read the committed rows and not that session's cache.
     */
    public void lockForWrite() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("lockForWrite() must be called inside an active transaction.");
        }
        m_versionDao.lockAll();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    m_managers.reloadAll();
                } catch (final RuntimeException e) {
                    // the write is already committed; the managers' version poll catches up within a second
                    LOG.warn("Can't reload the user-management managers after a committed write; the version poll will catch up", e);
                }
            }
        });
    }

    /**
     * Throws 412 if the request carries an If-Match that doesn't match the stored row's current version.
     * <p>
     * The header is evaluated here rather than by the JAX-RS {@code Request}, which only understands strong tags
     * and would also apply If-None-Match (a 304 for a write). The rules are those of RFC 9110, with one deliberate
     * difference. No header passes; {@code *} matches any existing object; otherwise the header is a
     * comma-separated list of entity tags (empty list elements are ignored), and the precondition holds if any
     * tag's opaque value is the current version. RFC 9110 (13.1.1) requires the strong comparison for If-Match,
     * so a weak tag ({@code W/"7"}) never matches. Here a weak tag matches like a strong one: a proxy that
     * compresses the response turns the strong tag we send into a weak one, and the tag depends on nothing but
     * the version, so the weak tag still identifies the version exactly. A header that cannot be parsed matches
     * nothing, so it is a 412 and never a silent pass. If-None-Match is ignored.
     */
    public void checkIfMatch(final HttpHeaders headers, final VersionedTable table, final int id, final String what) {
        final String ifMatch = ifMatch(headers);
        if (ifMatch == null) {
            return;
        }
        if (!ifMatchHolds(ifMatch, version(table, id))) {
            throw preconditionFailed(what);
        }
    }

    /**
     * The exception for a write whose target does not exist: 412 if the request carries an If-Match (RFC 9110:
     * the condition cannot hold for an object that is not there, even for {@code *}), 404 otherwise.
     */
    public WebApplicationException notFoundOrPreconditionFailed(final HttpHeaders headers, final String what, final String notFoundMessage) {
        return ifMatch(headers) == null ? notFound(notFoundMessage) : preconditionFailed(what);
    }

    private WebApplicationException preconditionFailed(final String what) {
        return error(Status.PRECONDITION_FAILED, what + " was changed by someone else. Reload it and try again.");
    }

    /** The If-Match header lines joined into one list, or null if the request has none. */
    private static String ifMatch(final HttpHeaders headers) {
        final List<String> values = headers == null ? null : headers.getRequestHeader(HttpHeaders.IF_MATCH);
        return values == null || values.isEmpty() ? null : String.join(",", values);
    }

    static boolean ifMatchHolds(final String ifMatch, final int version) {
        final String value = ifMatch.trim();
        if ("*".equals(value)) {
            return true;
        }
        final Matcher matcher = ENTITY_TAG_LIST.matcher(value);
        if (!matcher.matches()) {
            return false;
        }
        final Matcher tags = ENTITY_TAG.matcher(value);
        final String current = Integer.toString(version);
        while (tags.find()) {
            if (current.equals(tags.group(1))) {
                return true;
            }
        }
        return false;
    }

    public EntityTag etag(final int version) {
        return new EntityTag(Integer.toString(version));
    }

    public int version(final VersionedTable table, final int id) {
        return m_versionDao.getRowVersion(table, id);
    }

    public WebApplicationException badRequest(final String message) {
        return error(Status.BAD_REQUEST, message);
    }

    public WebApplicationException notFound(final String message) {
        return error(Status.NOT_FOUND, message);
    }

    public WebApplicationException conflict(final String message) {
        return error(Status.CONFLICT, message);
    }

    /** Returns the exception to throw for a failure while performing {@code action}. */
    public RuntimeException translate(final String action, final Exception e) {
        if (e instanceof WebApplicationException) {
            return (WebApplicationException) e;
        }
        if (e instanceof UserMgmtValidationException) {
            return badRequest(e.getMessage());
        }
        if (e instanceof UserDataChangedException) {
            return conflict(e.getMessage());
        }
        if (e instanceof DataAccessException && isLockFailure(e)) {
            return conflict("The data was changed at the same time by another request. Try again.");
        }
        if (e instanceof DataIntegrityViolationException) {
            // e.g. a foreign key that still references the row; keep the SQL out of the message
            LOG.warn("Can't {}: {}", action, NestedExceptionUtils.getMostSpecificCause(e).getMessage());
            return conflict("The change conflicts with related data. Reload and try again.");
        }
        // an unexpected error: the message may hold SQL or internals, so it is logged and not returned
        // (this includes an IllegalArgumentException, which is a bug here, not a bad request)
        LOG.error("Can't {}", action, e);
        return error(Status.INTERNAL_SERVER_ERROR, "Can't " + action + ".");
    }

    private static boolean isLockFailure(final Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof SQLException) {
                final String state = ((SQLException) cause).getSQLState();
                if (DEADLOCK.equals(state) || SERIALIZATION_FAILURE.equals(state)) {
                    return true;
                }
            }
            cause = cause.getCause() == cause ? null : cause.getCause();
        }
        return false;
    }

    private static WebApplicationException error(final Status status, final String message) {
        return new WebApplicationException(Response.status(status).type("text/plain").entity(message).build());
    }
}
