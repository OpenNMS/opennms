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
package org.opennms.netmgt.filter;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;

/**
 * Pure (no-database) test that the WHERE clause produced by {@link JdbcFilterDao#parseRule}
 * is wrapped in parentheses, so that constraints appended by callers (ipaddr/nodeID/service,
 * the isManaged/deleted filter) apply to the whole rule rather than binding only to the last
 * branch of a top-level OR.
 */
public class JdbcFilterDaoParenthesizationTest {

    private JdbcFilterDao m_dao;

    @Before
    public void setUp() throws Exception {
        m_dao = new JdbcFilterDao();
        m_dao.setDatabaseSchemaConfigFactory(
                new DatabaseSchemaConfigFactory(getClass().getResourceAsStream("/database-schema.xml")));
    }

    @Test
    public void orRuleIsParenthesized() throws Exception {
        // "a | b" must become "WHERE (a OR b)" — not "WHERE a OR b"
        final String sql = m_dao.getInterfaceWithServiceStatement("catincFoo | catincBar");
        System.out.println("getInterfaceWithServiceStatement: " + sql);
        final String where = sql.substring(sql.indexOf("WHERE "));
        assertTrue("rule should be parenthesized: " + where, where.startsWith("WHERE ("));
        assertTrue("rule should be closed with ): " + where, where.endsWith(")"));
        assertTrue("OR should be inside the parentheses", where.contains(" OR "));
    }

    @Test
    public void appendedConstraintIsOutsideTheOr() throws Exception {
        // getSQLStatement(rule, nodeId, ...) appends "AND <col> = nodeId" after the rule.
        // With the fix the nodeID constraint must sit OUTSIDE the rule's parentheses:
        //   WHERE (<rule with OR>) AND node.nodeID = 5
        // (Before the fix it was "WHERE <a> OR <b> AND node.nodeID = 5", scoping the
        //  constraint to only the right OR branch.)
        final String sql = m_dao.getSQLStatement("catincFoo | catincBar", 5L, null, null);
        System.out.println("getSQLStatement(rule, nodeId): " + sql);
        final String where = sql.substring(sql.indexOf("WHERE "));
        assertTrue("appended constraint must follow the closed rule parenthesis: " + where,
                where.contains(") AND "));
        // The OR must be enclosed before the appended AND-constraint
        final int orIdx = where.indexOf(" OR ");
        final int closeParenIdx = where.indexOf(") AND ");
        assertTrue("OR must appear before the closing paren of the rule",
                orIdx > 0 && orIdx < closeParenIdx);
    }

    @Test
    public void plainRuleStillWorks() throws Exception {
        final String sql = m_dao.getInterfaceWithServiceStatement("ipaddr IPLIKE *.*.*.*");
        System.out.println("plain rule: " + sql);
        final String where = sql.substring(sql.indexOf("WHERE "));
        assertTrue(where.startsWith("WHERE (IPLIKE("));
        assertTrue(where.endsWith(")"));
    }
}
