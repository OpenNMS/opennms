/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.hibernate.criterion.Criterion;
import org.junit.Test;
import org.opennms.core.criteria.restrictions.IplikeRestriction;
import org.opennms.netmgt.dao.hibernate.HibernateCriteriaConverter.HibernateRestrictionVisitor;

/**
 * The attribute of an iplike restriction goes into the SQL text as a column name.
 * These tests make sure that only a plain column name gets into the SQL.
 */
public class HibernateCriteriaConverterIplikeTest {

    @Test
    public void testValidColumnName() {
        final HibernateRestrictionVisitor visitor = new HibernateRestrictionVisitor();
        visitor.visitIplike(new IplikeRestriction("ipAddr", "192.168.*.*"));
        assertEquals(1, visitor.getCriterions().size());
        final Criterion criterion = visitor.getCriterions().get(0);
        assertTrue(criterion.toString().startsWith("({alias}.ipAddr IS NOT NULL AND opennms_safe_inet({alias}.ipAddr)"));
    }

    @Test
    public void testValidColumnNameFallsBackToIplike() {
        // a zone-constrained pattern has no native-inet translation
        final HibernateRestrictionVisitor visitor = new HibernateRestrictionVisitor();
        visitor.visitIplike(new IplikeRestriction("ipAddr", "fe80:*:*:*:*:*:*:*%5"));
        assertEquals("ipLike({alias}.ipAddr, ?)", visitor.getCriterions().get(0).toString());
    }

    @Test
    public void testEmptyAttributeUsesIpAddr() {
        final HibernateRestrictionVisitor visitor = new HibernateRestrictionVisitor();
        visitor.visitIplike(new IplikeRestriction("", "fe80:*:*:*:*:*:*:*%5"));
        assertEquals("ipLike({alias}.ipAddr, ?)", visitor.getCriterions().get(0).toString());
    }

    @Test
    public void testSqlInjectionInAttributeIsRejected() {
        final String[] attributes = new String[] {
                "nodelabel) or 1=1 --",
                "ipAddr, ?) or (select 1 from accounts) is not null or ipLike(this_.ipAddr",
                "ipAddr;drop table node",
                "ipAddr --",
                "ip Addr",
                "ipInterfaces.ipAddress",
                "1ipAddr",
                "ipAddr\n",
        };
        for (final String attribute : attributes) {
            final HibernateRestrictionVisitor visitor = new HibernateRestrictionVisitor();
            try {
                visitor.visitIplike(new IplikeRestriction(attribute, "*.*.*.*"));
                fail("The attribute must be rejected: " + attribute);
            } catch (final IllegalArgumentException e) {
                assertTrue(visitor.getCriterions().isEmpty());
            }
        }
    }
}
