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
package org.opennms.netmgt.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@link OnmsRestrictions#ipLike} must emit a native-inet predicate for
 * translatable match expressions and keep the iplike() call for the rest;
 * both are SQL fragments over the aliased ipAddr column.
 */
public class OnmsRestrictionsTest {

    @Test
    public void translatableExpressionBecomesNativePredicate() {
        final String sql = OnmsRestrictions.ipLike("192.168.1-5.*").toString();
        assertTrue("expected a safe_inet range predicate: " + sql,
                sql.contains("opennms_safe_inet({alias}.ipAddr) >= inet '192.168.1.0'"));
        assertTrue(sql.contains("opennms_safe_inet({alias}.ipAddr) <= inet '192.168.5.255'"));
        assertFalse("no iplike call expected: " + sql, sql.contains("iplike("));
    }

    @Test
    public void zoneIdExpressionKeepsIplike() {
        final String sql = OnmsRestrictions.ipLike("fe80:*:*:*:*:*:*:*%1").toString();
        assertTrue("zone-id rules must keep iplike(): " + sql,
                sql.contains("iplike({alias}.ipAddr, ?)"));
        assertFalse(sql.contains("opennms_safe_inet"));
    }

    @Test
    public void malformedExpressionKeepsIplike() {
        // iplike() evaluates malformed rules to false at runtime; the
        // translator must decline them rather than guess
        final String sql = OnmsRestrictions.ipLike("not-an-address").toString();
        assertTrue(sql.contains("iplike({alias}.ipAddr, ?)"));
        assertFalse(sql.contains("opennms_safe_inet"));
    }
}
