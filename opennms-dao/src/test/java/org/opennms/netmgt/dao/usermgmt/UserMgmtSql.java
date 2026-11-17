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
package org.opennms.netmgt.dao.usermgmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

/** Plain-SQL checks for the user management ITs, run on the test transaction's connection. */
final class UserMgmtSql {

    static final String UNIQUE_VIOLATION = "23505";
    static final String FOREIGN_KEY_VIOLATION = "23503";
    static final String CHECK_VIOLATION = "23514";

    private UserMgmtSql() {
    }

    /**
     * Flushes the session, then asserts the statement is rejected with that SQL state. Deferred constraints
     * are made immediate first, and the statement runs in a savepoint, so the test transaction stays usable.
     */
    static void assertRejected(final SessionFactory sessionFactory, final String sqlState, final String sql, final Object... params) {
        final Session session = sessionFactory.getCurrentSession();
        session.flush();
        session.doWork(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET CONSTRAINTS ALL IMMEDIATE");
            }
            final Savepoint savepoint = connection.setSavepoint();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                statement.executeUpdate();
                fail("expected SQL state " + sqlState + " from: " + sql);
            } catch (SQLException e) {
                assertEquals("SQL state of: " + e.getMessage(), sqlState, e.getSQLState());
                connection.rollback(savepoint);
            }
        });
    }

    /** Flushes the session and checks the deferred constraints now, failing the test if any is violated. */
    static void checkDeferredConstraints(final SessionFactory sessionFactory) {
        final Session session = sessionFactory.getCurrentSession();
        session.flush();
        session.doWork(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET CONSTRAINTS ALL IMMEDIATE");
                statement.execute("SET CONSTRAINTS ALL DEFERRED");
            }
        });
    }

    /** Flushes the session and runs a {@code SELECT COUNT(*)}. */
    static int count(final SessionFactory sessionFactory, final String sql, final Object... params) {
        final Session session = sessionFactory.getCurrentSession();
        session.flush();
        final AtomicInteger count = new AtomicInteger();
        session.doWork(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    count.set(rs.getInt(1));
                }
            }
        });
        return count.get();
    }
}
