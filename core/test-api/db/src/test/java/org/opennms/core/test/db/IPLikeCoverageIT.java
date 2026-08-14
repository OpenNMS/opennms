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
package org.opennms.core.test.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.schema.Migrator;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.IplikeSqlTranslator;

public class IPLikeCoverageIT {
    private TemporaryDatabasePostgreSQL m_db;

    @Before
    public void setUp() throws Exception {
        m_db = new TemporaryDatabasePostgreSQL(null, false);
        m_db.setPlpgsqlIplike(true);
        m_db.setPopulateSchema(true);
        m_db.setupDatabase();
    }
    
    @After
    public void tearDown() throws Exception {
        m_db.destroyTestDatabase();
    }

    /*
     * This set of coverage data matches that in https://github.com/OpenNMS/iplike/blob/master/tests.dat
     */
    @Test
    public void testIplikeCoverage() throws Exception {
        // IPv4 basic matches
        checkIplikeRule("1.2.3.4","1.2.3.4",true);
        checkIplikeRule("1.2.3.4","1.2.3.5",false);
        checkIplikeRule("1.2.3.4","1.2.3.*",true);
        checkIplikeRule("1.2.3.4","1.*.3.4",true);
        checkIplikeRule("1.2.3.4","1.*.3.5",false);

        // IPv4 range matches
        checkIplikeRule("192.168.10.11","192.168.10.10-11",true);
        checkIplikeRule("192.168.10.12","192.168.10.10-11",false);
        checkIplikeRule("192.168.223.9","192.168.216-223.*",true);
        checkIplikeRule("192.168.224.9","192.168.216-223.*",false);

        // IPv4 list matches
        checkIplikeRule("192.168.1.9","192.168.0,1,2.*",true);
        checkIplikeRule("192.168.1.9","192.168.1,2,0.*",true);
        checkIplikeRule("192.168.1.9","192.168.2,0,1.*",true);
        checkIplikeRule("192.168.3.9","192.168.0,1,2.*",false);
        checkIplikeRule("192.168.3.9","192.168.1,2,0.*",false);
        checkIplikeRule("192.168.3.9","192.168.2,0,1.*",false);
        checkIplikeRule("192.168.3.9","192.168.*,1,2.*",true);
        checkIplikeRule("192.168.3.9","192.168.0,*,2.*",true);
        checkIplikeRule("192.168.3.9","192.168.0,1,*.*",true);

        // IPv4 list and range in separate octet
        checkIplikeRule("192.168.1.9","192.168.0,1,2.0-20",true);
        checkIplikeRule("192.168.1.21","192.168.0,1,2.0-20",false);

        // IPv4 list and range in same octet
        checkIplikeRule("192.168.1.9","192.168.0,1,2-4.0-20",true);
        checkIplikeRule("192.168.3.9","192.168.0,1,2-4.0-20",true);
        checkIplikeRule("192.168.5.9","192.168.0,1,2-4.0-20",false);
        checkIplikeRule("192.168.1.21","192.168.0,1,2,3-4.0-20",false);
        checkIplikeRule("192.168.0.1","192.168.1-2,5.*",false);

        // IPv6 tests
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:*:*:*%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:*:*:*%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:*:*:*:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%45","fe80:*:*:*:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%45","fe80:*:*:*:*:*:*:*%45",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:*:*:*:*:*:*:*%45",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:0:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:0:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:0:*:*:*:*%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:0:*:*:*:*%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:bbbb:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:bbbb:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:bbbb:*:*%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:bbbb:*:*%5",false);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:bbbb:*:*%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:bbb0-bbbf:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:bbb0-bbbf:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:bbb0-bbbf:*:*%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:bbb0-bbbf:*:*%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4",false);
    }

    private void checkIplikeRule(final String value, final String rule, final boolean expected) throws Exception {
        final DBUtils util = new DBUtils();

        Connection c = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            c = m_db.getDataSource().getConnection();
            util.watch(c);

            st = c.prepareStatement("SELECT iplike(CAST(? AS TEXT),CAST(? AS TEXT))");
            util.watch(st);
            st.setString(1, value);
            st.setString(2, rule);

            st.execute();

            rs = st.getResultSet();
            util.watch(rs);
            rs.next();
            final boolean result = rs.getBoolean(1);
            assertEquals("SELECT iplike(" + value + "," + rule + ") === " + expected, expected, result);

            // Where the rule translates to a native-inet predicate (what the
            // SQL emitters produce instead of the iplike() call), the
            // database must give the same answer for the same value — both
            // directly and under NOT. The NOT check matters for values the
            // predicate cannot parse: it must evaluate to FALSE there, not
            // NULL, or negated filters would drop rows NOT iplike() keeps.
            final String predicate = IplikeSqlTranslator.toSqlPredicate(rule, "v");
            if (predicate != null) {
                final PreparedStatement nativeSt = c.prepareStatement(
                        "SELECT " + predicate + ", NOT " + predicate
                        + " FROM (VALUES (CAST(? AS TEXT))) AS t(v)");
                util.watch(nativeSt);
                nativeSt.setString(1, value);
                nativeSt.execute();
                final ResultSet nativeRs = nativeSt.getResultSet();
                util.watch(nativeRs);
                nativeRs.next();
                assertEquals("native predicate for rule " + rule + " on value " + value
                        + ": " + predicate, expected, nativeRs.getBoolean(1));
                assertEquals("negated native predicate for rule " + rule + " on value " + value
                        + ": " + predicate, !expected, nativeRs.getBoolean(2));
                assertFalse("negated native predicate must never be NULL for rule " + rule
                        + " on value " + value, nativeRs.wasNull());
            }
        } finally {
            util.cleanUp();
        }
    }

    /**
     * A working PL/pgSQL iplike older than the shipped revision must be
     * replaced in place on upgrade (its revision tag lives in the function
     * comment), while any working non-PL/pgSQL implementation — the compiled
     * C extension in real installs — is left untouched.
     */
    @Test
    public void testUpdateIplikeReplacesStalePlpgsqlRevision() throws Exception {
        final Migrator migrator = new Migrator();
        migrator.setDataSource(m_db.getDataSource());
        migrator.setAdminDataSource(m_db.getDataSource());

        executeSql("DROP FUNCTION iplike(text,text)");
        executeSql("CREATE FUNCTION iplike(text,text) RETURNS boolean AS "
                + "$$ begin return 't'; end; $$ LANGUAGE plpgsql");
        executeSql("COMMENT ON FUNCTION iplike(text,text) IS 'opennms-iplike-plpgsql-1'");

        migrator.updateIplike();

        assertEquals(Migrator.IPLIKE_PLPGSQL_REVISION, getIplikeComment());
        // the always-true stub is gone and real matching is back
        checkIplikeRule("1.2.3.4", "1.2.3.5", false);
        checkIplikeRule("1.2.3.4", "1.2.3.4", true);
    }

    @Test
    public void testUpdateIplikeLeavesForeignImplementationsAlone() throws Exception {
        final Migrator migrator = new Migrator();
        migrator.setDataSource(m_db.getDataSource());
        migrator.setAdminDataSource(m_db.getDataSource());

        // a working iplike that is not PL/pgSQL stands in for the compiled
        // extension: the stale check only ever replaces plpgsql revisions
        executeSql("DROP FUNCTION iplike(text,text)");
        executeSql("CREATE FUNCTION iplike(text,text) RETURNS boolean AS "
                + "$$ SELECT true $$ LANGUAGE sql");
        executeSql("COMMENT ON FUNCTION iplike(text,text) IS 'custom-iplike'");

        migrator.updateIplike();

        assertEquals("custom-iplike", getIplikeComment());
    }

    private void executeSql(final String sql) throws Exception {
        final DBUtils util = new DBUtils();
        try {
            final Connection c = m_db.getDataSource().getConnection();
            util.watch(c);
            final Statement st = c.createStatement();
            util.watch(st);
            st.execute(sql);
        } finally {
            util.cleanUp();
        }
    }

    private String getIplikeComment() throws Exception {
        final DBUtils util = new DBUtils();
        try {
            final Connection c = m_db.getDataSource().getConnection();
            util.watch(c);
            final Statement st = c.createStatement();
            util.watch(st);
            final ResultSet rs = st.executeQuery(
                    "SELECT obj_description('iplike(text,text)'::regprocedure, 'pg_proc')");
            util.watch(rs);
            rs.next();
            return rs.getString(1);
        } finally {
            util.cleanUp();
        }
    }

    /**
     * Values iplike() rejects but that can sit in text ipaddr columns (the
     * events table in particular). The native predicate must agree with
     * iplike() on them in both polarities.
     */
    @Test
    public void testUnrepresentableValuesAgainstTranslatableRules() throws Exception {
        for (final String garbage : new String[] {
                "garbage",
                "fe80::1",              // compressed IPv6: iplike needs 8 groups
                "fe80::1:2:3:4:5:6",    // one-group compression still has 7 colons
                "::1:2:3:4:5:6:7",
                "1:2:3:4:5:6:7::",
                "1.2.3.4%eth0",         // zone ids are IPv6-only
                "10.0.0.999",
                " 1.2.3.4",
                ""}) {
            checkIplikeRule(garbage, "1.2.3.*", false);
            checkIplikeRule(garbage, "fe80:*:*:*:*:*:*:*", false);
        }
    }

    /**
     * A middle one-group compression (seven colons, valid inet) must not
     * enter the 8-group parsing loop: ltrim would swallow the '::' and
     * shift every later field, so the shifted rule matched and the correct
     * expansion did not.
     */
    @Test
    public void testCompressedIpv6NeverParsesShifted() throws Exception {
        checkIplikeRule("fe80::1:2:3:4:5:6", "fe80:1:2:3:4:5:6:0", false); // the shifted misparse
        checkIplikeRule("fe80::1:2:3:4:5:6", "fe80:0:1:2:3:4:5:6", false); // the correct expansion (value is rejected, as revision 1 did)
        checkIplikeRule("fe80::1:2:0:4:5:6", "*:*:*:0:*:*:*:*", false);    // untranslatable rule -> the iplike() fallback path
    }

}
