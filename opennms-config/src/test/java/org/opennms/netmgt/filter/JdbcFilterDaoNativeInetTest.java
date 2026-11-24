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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.IplikeSqlTranslator;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;

/**
 * Pure (no-database) tests that {@link JdbcFilterDao#parseRule} rewrites
 * translatable IPLIKE expressions into native-inet predicates over
 * {@code opennms_safe_inet()} (served by an expression index) and keeps the
 * iplike() call for everything the translator declines.
 */
public class JdbcFilterDaoNativeInetTest {

    private JdbcFilterDao m_dao;

    @Before
    public void setUp() throws Exception {
        m_dao = new JdbcFilterDao();
        m_dao.setDatabaseSchemaConfigFactory(
                new DatabaseSchemaConfigFactory(getClass().getResourceAsStream("/database-schema.xml")));
    }

    @After
    public void tearDown() {
        System.clearProperty(IplikeSqlTranslator.ENABLED_PROPERTY);
    }

    private String whereClause(final String rule) {
        final String sql = m_dao.getInterfaceWithServiceStatement(rule);
        return sql.substring(sql.indexOf("WHERE "));
    }

    @Test
    public void translatableBareRuleBecomesNativePredicate() {
        final String where = whereClause("ipaddr IPLIKE 10.0.1-3.*");
        assertTrue("expected a schema-qualified safe_inet predicate: " + where,
                where.contains("opennms_safe_inet(ipInterface.ipaddr) >= inet '10.0.1.0'"));
        assertTrue(where.contains("opennms_safe_inet(ipInterface.ipaddr) <= inet '10.0.3.255'"));
        assertTrue(where.contains("ipInterface.ipaddr IS NOT NULL"));
        assertFalse("no iplike call expected: " + where, where.toUpperCase().contains("IPLIKE("));
    }

    @Test
    public void translatableQuotedRuleBecomesNativePredicate() {
        final String where = whereClause("ipaddr IPLIKE \"192.168.1.1\"");
        assertTrue("expected an equality safe_inet predicate: " + where,
                where.contains("opennms_safe_inet(ipInterface.ipaddr) = inet '192.168.1.1'"));
        assertFalse(where.toUpperCase().contains("IPLIKE("));
    }

    @Test
    public void matchAllBecomesNotNull() {
        final String where = whereClause("ipaddr IPLIKE *.*.*.*");
        assertTrue(where.contains("ipInterface.ipaddr IS NOT NULL"));
        assertFalse(where.toUpperCase().contains("IPLIKE("));
    }

    @Test
    public void zoneIdRuleKeepsIplike() {
        // zone-id patterns have no inet representation; must fall back to a
        // correctly single-quoted iplike() call (the extracted string carries
        // its own quotes and must not be wrapped in another pair)
        final String where = whereClause("ipaddr IPLIKE \"fe80:*:*:*:*:*:*:*%1\"");
        assertTrue("zone-id rules must keep iplike(): " + where,
                where.contains("IPLIKE(ipInterface.ipaddr, 'fe80:*:*:*:*:*:*:*%1')"));
        assertFalse(where.contains("''"));
        assertFalse(where.contains("opennms_safe_inet"));
    }

    @Test
    public void negatedRuleWrapsNativePredicate() {
        final String where = whereClause("!(ipaddr IPLIKE 10.0.1-3.*)");
        assertTrue("negation must survive translation: " + where, where.contains(" NOT "));
        assertTrue(where.contains("opennms_safe_inet(ipInterface.ipaddr)"));
    }

    @Test
    public void escapeHatchRestoresIplike() {
        System.setProperty(IplikeSqlTranslator.ENABLED_PROPERTY, "false");
        final String where = whereClause("ipaddr IPLIKE 10.0.1-3.*");
        assertTrue("disabled translation must emit iplike(): " + where,
                where.toUpperCase().contains("IPLIKE("));
        assertFalse(where.contains("opennms_safe_inet"));
    }
}
