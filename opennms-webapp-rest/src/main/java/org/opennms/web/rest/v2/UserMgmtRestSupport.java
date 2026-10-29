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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.hibernate.SessionFactory;
import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.netmgt.config.UserDataChangedException;
import org.opennms.netmgt.config.UserMgmtManagers;
import org.opennms.netmgt.config.api.UserMgmtStore;
import org.opennms.netmgt.config.destinationPaths.Path;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.VersionedTable;
import org.opennms.web.api.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Shared plumbing for the v2 user-management services (users, groups, security roles, on-call roles):
 * the admin and store-mode checks, the write lock with its after-commit manager expiry, the deferred-constraint check
 * before the commit, the destination path check, the ETag / If-Match handling, and the translation of exceptions to
 * HTTP errors.
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

    /** The deferred constraints of the user-management tables, checked by {@link #flushAndCheckConstraints}. */
    private static final String DEFERRED_CONSTRAINTS = "uk_user_contacts_user_type, uk_group_members_group_user";

    @Autowired
    private UserMgmtVersionDao m_versionDao;

    @Autowired
    private UserMgmtManagers m_managers;

    /** Translates Hibernate's exceptions, e.g. a unique violation to DataIntegrityViolationException. */
    private HibernateTemplate m_hibernateTemplate;

    @Autowired
    void setSessionFactory(final SessionFactory sessionFactory) {
        m_hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    /** The destination paths that notifd sends to. */
    static final Callable<Collection<Path>> NOTIFD_DESTINATION_PATHS = () -> {
        DestinationPathFactory.init();
        return DestinationPathFactory.getInstance().getPaths().values();
    };

    /** Static so that tests can replace it in the per-test CXF context. */
    private static volatile Callable<Collection<Path>> s_destinationPaths = NOTIFD_DESTINATION_PATHS;

    /** For tests; {@link #NOTIFD_DESTINATION_PATHS} restores the default. */
    static void setDestinationPaths(final Callable<Collection<Path>> destinationPaths) {
        s_destinationPaths = destinationPaths;
    }

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
     * Takes the write lock (users, then groups) and, once the surrounding transaction commits, expires the
     * in-memory managers: their next read checks the version and reloads, so the next request sees the write.
     * Nothing is read in {@code afterCommit}, where the committed transaction's session is still bound to the
     * thread, and a burst of writes costs one reload instead of one per write. Must be called inside an active
     * transaction.
     */
    public void lockForWrite() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("lockForWrite() must be called inside an active transaction.");
        }
        m_versionDao.lockAll();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                m_managers.expireAll();
            }
        });
    }

    /**
     * Writes out the pending changes and checks the deferred constraints now ({@code uk_user_contacts_user_type},
     * {@code uk_group_members_group_user}), so that a violation is thrown here, inside the service method, where
     * {@link #translate} turns it into an HTTP error, and not by the commit after the method returns, which would be
     * a 500. Only these two constraints are checked, and they are deferred again afterwards, so that later changes
     * in the same transaction are checked at the next call or at the commit. Call it after a write's last change.
     * <p>
     * In READ COMMITTED, PostgreSQL raises neither a deadlock nor a serialization failure at COMMIT itself, so after
     * this the commit can only fail for connection-level reasons.
     */
    public void flushAndCheckConstraints() {
        m_hibernateTemplate.execute(session -> {
            session.flush();
            // setting a deferred constraint to IMMEDIATE checks the pending changes at once
            session.createNativeQuery("SET CONSTRAINTS " + DEFERRED_CONSTRAINTS + " IMMEDIATE").executeUpdate();
            session.createNativeQuery("SET CONSTRAINTS " + DEFERRED_CONSTRAINTS + " DEFERRED").executeUpdate();
            return null;
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

    /**
     * Throws 409 if a destination path names {@code target}, among its targets or those of an escalation. notifd
     * looks a target up by name when it sends, so after a delete the path would notify nobody in its place (and only
     * log a warning), and an object created later under that name would receive its notices.
     */
    public void checkNotDestinationPathTarget(final String what, final String target) throws Exception {
        final List<String> paths = s_destinationPaths.call().stream()
                .filter(path -> Stream.concat(path.getTargets().stream(),
                                path.getEscalates().stream().flatMap(escalate -> escalate.getTargets().stream()))
                        .anyMatch(t -> target.equals(t.getName())))
                .map(Path::getName)
                .sorted()
                .collect(Collectors.toList());
        if (!paths.isEmpty()) {
            throw conflict(what + " is a target of destination path(s) " + String.join(", ", paths)
                    + "; remove it from them first.");
        }
    }

    /**
     * Returns the exception to throw for a failure while performing {@code action}.
     * <p>
     * A deadlock or serialization failure (SQL state 40P01 or 40001) anywhere in the cause chain, or in the
     * next-exception chain of an {@link SQLException} (where a JDBC batch reports the statement's own failure), is a
     * 409, whatever wraps it, for example a {@link DataAccessException} from a flush. No other SQL state is.
     * <p>
     * A commit's exception never gets here: the commit runs in the transaction proxy after the service method
     * returns. The writes call {@link #flushAndCheckConstraints()} so that their failures are thrown inside the
     * method. A deadlock inside a {@link org.springframework.transaction.TransactionSystemException} is still a 409,
     * as a defence.
     */
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
        if (isLockFailure(e)) {
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

    /** Walks the causes and the next exceptions of every SQLException among them, each exception once. */
    private static boolean isLockFailure(final Throwable e) {
        final Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        final Deque<Throwable> pending = new ArrayDeque<>();
        pending.add(e);
        while (!pending.isEmpty()) {
            final Throwable current = pending.remove();
            if (!seen.add(current)) {
                continue;
            }
            if (current instanceof SQLException) {
                final SQLException sql = (SQLException) current;
                if (DEADLOCK.equals(sql.getSQLState()) || SERIALIZATION_FAILURE.equals(sql.getSQLState())) {
                    return true;
                }
                if (sql.getNextException() != null) {
                    pending.add(sql.getNextException());
                }
            }
            if (current.getCause() != null) {
                pending.add(current.getCause());
            }
        }
        return false;
    }

    private static WebApplicationException error(final Status status, final String message) {
        return new WebApplicationException(Response.status(status).type("text/plain").entity(message).build());
    }
}
