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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.opennms.netmgt.config.UserDataChangedException;
import org.opennms.netmgt.config.UserMgmtManagers;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao.VersionedTable;
import org.opennms.web.api.Authentication;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class UserMgmtRestSupportTest {

    private static final String DEADLOCK_TEXT = "The data was changed at the same time by another request. Try again.";

    private UserMgmtVersionDao m_versionDao;
    private UserMgmtManagers m_managers;
    private UserMgmtRestSupport m_support;

    @Before
    public void setUp() {
        m_versionDao = mock(UserMgmtVersionDao.class);
        m_managers = mock(UserMgmtManagers.class);
        m_support = new UserMgmtRestSupport();
        ReflectionTestUtils.setField(m_support, "m_versionDao", m_versionDao);
        ReflectionTestUtils.setField(m_support, "m_managers", m_managers);
    }

    @After
    public void tearDown() {
        System.clearProperty("org.opennms.usermgmt.store");
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private static HttpHeaders headersWithIfMatch(final String... ifMatch) {
        final HttpHeaders headers = mock(HttpHeaders.class);
        if (ifMatch.length > 0) {
            when(headers.getRequestHeader("If-Match")).thenReturn(Arrays.asList(ifMatch));
        }
        // a stray If-None-Match must not matter to a write
        when(headers.getRequestHeader("If-None-Match")).thenReturn(Collections.singletonList("*"));
        return headers;
    }

    private static void assertStatus(final int status, final String text, final WebApplicationException e) {
        assertEquals(status, e.getResponse().getStatus());
        assertEquals(text, e.getResponse().getEntity());
    }

    @Test
    public void testRequireDatabaseStoreIn503XmlMode() {
        System.setProperty("org.opennms.usermgmt.store", "xml");
        try {
            m_support.requireDatabaseStore();
            fail("expected 503");
        } catch (final WebApplicationException e) {
            assertStatus(503, "User management through this API needs org.opennms.usermgmt.store=db.", e);
        }
        System.clearProperty("org.opennms.usermgmt.store");
        m_support.requireDatabaseStore();
        System.setProperty("org.opennms.usermgmt.store", "db");
        m_support.requireDatabaseStore();
    }

    @Test
    public void testAssertAdmin() {
        final SecurityContext admin = mock(SecurityContext.class);
        when(admin.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(true);
        m_support.assertAdmin(admin, "Changing users");
        try {
            m_support.assertAdmin(mock(SecurityContext.class), "Changing users");
            fail("expected 403");
        } catch (final WebApplicationException e) {
            assertStatus(403, "Changing users requires the admin role.", e);
        }
        try {
            m_support.assertAdmin(null, "Changing users");
            fail("expected 403");
        } catch (final WebApplicationException e) {
            assertEquals(403, e.getResponse().getStatus());
        }
    }

    @Test
    public void testCheckTarget() {
        final SecurityContext admin = mock(SecurityContext.class);
        when(admin.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(true);
        m_support.checkTarget(admin, "User management", "userId", "DOMAIN\\jdoe");
        for (final String blank : new String[] { null, "", " " }) {
            try {
                m_support.checkTarget(admin, "User management", "userId", blank);
                fail("expected 400 for '" + blank + "'");
            } catch (final WebApplicationException e) {
                assertStatus(400, "userId is required", e);
            }
        }
        // without the admin role, a missing parameter is still a 403, never a 400
        try {
            m_support.checkTarget(mock(SecurityContext.class), "Group management", "name", null);
            fail("expected 403");
        } catch (final WebApplicationException e) {
            assertStatus(403, "Group management requires the admin role.", e);
        }
        System.setProperty("org.opennms.usermgmt.store", "xml");
        try {
            m_support.checkTarget(admin, "Group management", "name", null);
            fail("expected 503");
        } catch (final WebApplicationException e) {
            assertEquals(503, e.getResponse().getStatus());
        }
    }

    @Test
    public void testCheckNotLookAlike() {
        for (final String lookAlike : new String[] { "ADMIN", "Admin", "aDmIn" }) {
            try {
                m_support.checkNotLookAlike(lookAlike, Set.of("admin", "rtc"), "user-id");
                fail("expected 400 for " + lookAlike);
            } catch (final WebApplicationException e) {
                assertStatus(400, "The user-id \"" + lookAlike + "\" is reserved.", e);
            }
        }
        // the protected name itself is a duplicate, reported as such elsewhere; other names pass
        m_support.checkNotLookAlike("admin", Set.of("admin", "rtc"), "user-id");
        m_support.checkNotLookAlike("administrator", Set.of("admin", "rtc"), "user-id");
    }

    @Test
    public void testCheckIfMatchStaleThrows412() {
        when(m_versionDao.getRowVersion(VersionedTable.users, 5)).thenReturn(7);
        try {
            m_support.checkIfMatch(headersWithIfMatch("\"6\""), VersionedTable.users, 5, "The user");
            fail("expected 412");
        } catch (final WebApplicationException e) {
            assertStatus(412, "The user was changed by someone else. Reload it and try again.", e);
        }
    }

    @Test
    public void testCheckIfMatchAbsentPasses() {
        when(m_versionDao.getRowVersion(VersionedTable.users, 5)).thenReturn(7);
        m_support.checkIfMatch(headersWithIfMatch(), VersionedTable.users, 5, "The user");
        m_support.checkIfMatch(null, VersionedTable.users, 5, "The user");
    }

    @Test
    public void testCheckIfMatchStarPasses() {
        when(m_versionDao.getRowVersion(VersionedTable.users, 5)).thenReturn(7);
        m_support.checkIfMatch(headersWithIfMatch("*"), VersionedTable.users, 5, "The user");
    }

    @Test
    public void testCheckIfMatchCurrentPasses() {
        when(m_versionDao.getRowVersion(VersionedTable.groups, 3)).thenReturn(7);
        m_support.checkIfMatch(headersWithIfMatch("\"7\""), VersionedTable.groups, 3, "The group");
    }

    @Test
    public void testCheckIfMatchWeakTagPasses() {
        when(m_versionDao.getRowVersion(VersionedTable.users, 5)).thenReturn(7);
        m_support.checkIfMatch(headersWithIfMatch("W/\"7\""), VersionedTable.users, 5, "The user");
        assertStale(headersWithIfMatch("W/\"6\""), 5);
    }

    @Test
    public void testCheckIfMatchListPassesIfAnyTagIsCurrent() {
        when(m_versionDao.getRowVersion(VersionedTable.users, 5)).thenReturn(7);
        m_support.checkIfMatch(headersWithIfMatch("\"3\", W/\"7\" ,\"9\""), VersionedTable.users, 5, "The user");
        m_support.checkIfMatch(headersWithIfMatch("\"3\"", "\"7\""), VersionedTable.users, 5, "The user");
        assertStale(headersWithIfMatch("\"3\", W/\"4\""), 5);
    }

    @Test
    public void testCheckIfMatchIgnoresEmptyListElements() {
        when(m_versionDao.getRowVersion(VersionedTable.users, 5)).thenReturn(7);
        for (final String ok : new String[] { "\"7\", , \"8\"", "\"7\",", ",\"7\"", "\"6\",,W/\"7\"", " , \"7\" , " }) {
            m_support.checkIfMatch(headersWithIfMatch(ok), VersionedTable.users, 5, "The user");
        }
        assertStale(headersWithIfMatch("\"6\", , \"8\""), 5);
        assertStale(headersWithIfMatch("\"6\","), 5);
    }

    @Test
    public void testCheckIfMatchMalformedIsStale() {
        when(m_versionDao.getRowVersion(VersionedTable.users, 5)).thenReturn(7);
        for (final String bad : new String[] { "7", "\"7", "W/7", "\"7\" \"8\"", ",", " , ,", "", "*, \"7\"", "w/\"7\"", "\"7\"x" }) {
            assertStale(headersWithIfMatch(bad), 5);
        }
    }

    @Test
    public void testCheckIfMatchIgnoresIfNoneMatch() {
        when(m_versionDao.getRowVersion(VersionedTable.users, 5)).thenReturn(7);
        final HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.getRequestHeader("If-None-Match")).thenReturn(Collections.singletonList("\"7\""));
        m_support.checkIfMatch(headers, VersionedTable.users, 5, "The user");
    }

    @Test
    public void testNotFoundOrPreconditionFailed() {
        assertStatus(404, "gone", m_support.notFoundOrPreconditionFailed(headersWithIfMatch(), "The user", "gone"));
        assertStatus(404, "gone", m_support.notFoundOrPreconditionFailed(null, "The user", "gone"));
        for (final String ifMatch : new String[] { "*", "\"7\"", "W/\"7\"", "garbage" }) {
            assertStatus(412, "The user was changed by someone else. Reload it and try again.",
                    m_support.notFoundOrPreconditionFailed(headersWithIfMatch(ifMatch), "The user", "gone"));
        }
    }

    private void assertStale(final HttpHeaders headers, final int id) {
        try {
            m_support.checkIfMatch(headers, VersionedTable.users, id, "The user");
            fail("expected 412");
        } catch (final WebApplicationException e) {
            assertStatus(412, "The user was changed by someone else. Reload it and try again.", e);
        }
    }

    @Test
    public void testEtagIsQuotedDecimal() {
        final EntityTag tag = m_support.etag(12);
        assertEquals("12", tag.getValue());
        assertFalse(tag.isWeak());
        assertEquals("\"12\"", tag.toString());
    }

    @Test
    public void testVersionDelegates() {
        when(m_versionDao.getRowVersion(VersionedTable.security_roles, 2)).thenReturn(4);
        assertEquals(4, m_support.version(VersionedTable.security_roles, 2));
    }

    @Test
    public void testErrorFactories() {
        assertStatus(400, "bad", m_support.badRequest("bad"));
        assertStatus(404, "gone", m_support.notFound("gone"));
        assertStatus(409, "dup", m_support.conflict("dup"));
    }

    @Test
    public void testTranslateDeadlockTo409() {
        for (final String state : new String[] { "40P01", "40001" }) {
            final Exception e = new CannotAcquireLockException("deadlock", new SQLException("deadlock detected", state));
            final RuntimeException translated = m_support.translate("save the user", e);
            assertTrue(translated instanceof WebApplicationException);
            assertStatus(409, DEADLOCK_TEXT, (WebApplicationException) translated);
        }
    }

    @Test
    public void testTranslateDeadlockDeepInTheCauseChainTo409() {
        final SQLException deadlock = new SQLException("deadlock detected", "40P01");
        final Exception e = new CannotAcquireLockException("could not execute statement",
                new RuntimeException("wrapper", new IllegalStateException("another wrapper", deadlock)));
        assertStatus(409, DEADLOCK_TEXT, (WebApplicationException) m_support.translate("save the user", e));
    }

    @Test
    public void testTranslateDeadlockInTheNextExceptionChainTo409() {
        // a JDBC batch reports the statement's own failure as the next exception of the BatchUpdateException
        for (final String state : new String[] { "40P01", "40001" }) {
            final BatchUpdateException batch = new BatchUpdateException("Batch entry 0 was aborted", "25P02", new int[0]);
            batch.setNextException(new SQLException("current transaction is aborted", "25P02"));
            batch.setNextException(new SQLException("deadlock detected", state));
            final Exception e = new DataIntegrityViolationException("could not execute batch", batch);
            assertStatus(409, DEADLOCK_TEXT, (WebApplicationException) m_support.translate("save the user", e));
        }
    }

    @Test
    public void testTranslateCommitTimeLockFailureTo409() {
        // thrown by the transaction manager rather than as a DataAccessException
        for (final String state : new String[] { "40P01", "40001" }) {
            final BatchUpdateException batch = new BatchUpdateException("Batch entry 0 was aborted", "25P02", new int[0]);
            batch.setNextException(new SQLException("could not serialize access", state));
            final Exception e = new TransactionSystemException("Could not commit Hibernate transaction",
                    new RuntimeException("commit failed", batch));
            assertStatus(409, DEADLOCK_TEXT, (WebApplicationException) m_support.translate("save the user", e));
        }
        final Exception direct = new TransactionSystemException("Could not commit", new SQLException("deadlock detected", "40P01"));
        assertStatus(409, DEADLOCK_TEXT, (WebApplicationException) m_support.translate("save the user", direct));
    }

    @Test
    public void testTranslateOtherSqlStatesAreNotLockFailures() {
        // a commit-time failure with any other state stays a 500, wherever the state is
        final BatchUpdateException batch = new BatchUpdateException("Batch entry 0 was aborted", "25P02", new int[0]);
        batch.setNextException(new SQLException("duplicate key value violates unique constraint", "23505"));
        final Exception commit = new TransactionSystemException("Could not commit Hibernate transaction", batch);
        assertStatus(500, "Can't save the user.", (WebApplicationException) m_support.translate("save the user", commit));
        final Exception noState = new TransactionSystemException("Could not commit", new SQLException("connection reset"));
        assertStatus(500, "Can't save the user.", (WebApplicationException) m_support.translate("save the user", noState));
        // an integrity violation whose next exception is another integrity error keeps its own message
        final BatchUpdateException fk = new BatchUpdateException("Batch entry 0 was aborted", "25P02", new int[0]);
        fk.setNextException(new SQLException("violates foreign key constraint", "23503"));
        assertStatus(409, "The change conflicts with related data. Reload and try again.",
                (WebApplicationException) m_support.translate("delete the user", new DataIntegrityViolationException("could not execute batch", fk)));
    }

    @Test(timeout = 10000)
    public void testTranslateStopsOnACycleInTheChains() {
        final SQLException first = new SQLException("first", "08006");
        final SQLException second = new SQLException("second", "08006", first);
        first.initCause(second);
        first.setNextException(second);
        second.setNextException(first);
        final Exception e = new TransactionSystemException("Could not commit", first);
        assertStatus(500, "Can't save the user.", (WebApplicationException) m_support.translate("save the user", e));
    }

    @Test
    public void testTranslateIntegrityViolationTo409WithoutSql() {
        final Exception e = new DataIntegrityViolationException("could not execute statement",
                new SQLException("update or delete on table \"users\" violates foreign key constraint \"fk_x\"", "23503"));
        final RuntimeException translated = m_support.translate("delete the user", e);
        assertStatus(409, "The change conflicts with related data. Reload and try again.", (WebApplicationException) translated);
    }

    @Test
    public void testTranslateValidationExceptionTo400() {
        final RuntimeException translated = m_support.translate("save the user", new UserMgmtValidationException("Bad value"));
        assertStatus(400, "Bad value", (WebApplicationException) translated);
    }

    @Test
    public void testTranslateIllegalArgumentTo500WithoutItsMessage() {
        final RuntimeException translated = m_support.translate("save the user", new IllegalArgumentException("could not execute: select secret"));
        assertStatus(500, "Can't save the user.", (WebApplicationException) translated);
    }

    @Test
    public void testTranslateUserDataChangedTo409() {
        final RuntimeException translated = m_support.translate("save the user", new UserDataChangedException());
        assertEquals(409, ((WebApplicationException) translated).getResponse().getStatus());
    }

    @Test
    public void testTranslatePassesWebApplicationExceptionThrough() {
        final WebApplicationException original = m_support.notFound("gone");
        assertSame(original, m_support.translate("save the user", original));
    }

    @Test
    public void testTranslateOtherTo500() {
        final RuntimeException translated = m_support.translate("save the user", new IllegalStateException("boom"));
        assertStatus(500, "Can't save the user.", (WebApplicationException) translated);
    }

    @Test
    public void testLockForWriteLocksAndExpiresAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        m_support.lockForWrite();
        final InOrder order = inOrder(m_versionDao);
        order.verify(m_versionDao).lockAll();
        verify(m_managers, never()).expireAll();
        for (final TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
            sync.afterCommit();
        }
        verify(m_managers).expireAll();
    }

    @Test
    public void testLockForWriteDoesNotReloadOnRollback() {
        TransactionSynchronizationManager.initSynchronization();
        m_support.lockForWrite();
        for (final TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
            sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        }
        verify(m_managers, never()).expireAll();
    }

    @Test(expected = IllegalStateException.class)
    public void testLockForWriteOutsideTransactionFails() {
        m_support.lockForWrite();
    }

    @Test
    public void testDestinationPathTargetIs409NamingThePathsSorted() throws Exception {
        final TestDestinationPaths paths = new TestDestinationPaths();
        try {
            paths.add("Page-Night", "someone-else", "ops"); // an escalation names it
            paths.add("Page-Day", "ops", null);
            paths.add("Page-Other", "opsx", "Ops"); // other names, and the match is exact
            try {
                m_support.checkNotDestinationPathTarget("Group ops", "ops");
                fail("expected 409");
            } catch (final WebApplicationException e) {
                assertStatus(409, "Group ops is a target of destination path(s) Page-Day, Page-Night; remove it from them first.", e);
            }
            m_support.checkNotDestinationPathTarget("Group devs", "devs");
        } finally {
            paths.restore();
        }
    }
}
