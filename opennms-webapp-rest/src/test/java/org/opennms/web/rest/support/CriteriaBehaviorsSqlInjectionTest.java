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

package org.opennms.web.rest.support;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.fiql.FiqlParser;
import org.junit.Test;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.SqlRestriction;
import org.opennms.netmgt.model.OnmsAlarm;

/**
 * NMS-20383: the affectedNodeCount and situationAlarmCount alarm search behaviors must bind their
 * FIQL value as a SQL parameter, never concatenate it into the query text. The value is coerced to
 * a Long by the search visitor before it reaches beforeVisit, so
 * a non-numeric payload is rejected before any SQL is built.
 */
public class CriteriaBehaviorsSqlInjectionTest {

    private static final String[] PROPERTIES = { "affectedNodeCount", "situationAlarmCount" };

    private void applyFiql(final CriteriaBuilder builder, final String expression) {
        final Map<String, CriteriaBehavior<?>> behaviors = new HashMap<>(CriteriaBehaviors.ALARM_BEHAVIORS);
        behaviors.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.alarm, CriteriaBehaviors.ALARM_BEHAVIORS));
        new FiqlParser<>(SearchBean.class).parse(expression).accept(
                new CriteriaBuilderSearchVisitor<OnmsAlarm, SearchBean>(builder, OnmsAlarm.class, behaviors));
    }

    @Test
    public void fiqlComparisonsBindLongValuesForBothPropertyForms() {
        final String[][] operators = {
                { "==", "=" }, { "!=", "!=" }, { "=lt=", "<" },
                { "=le=", "<=" }, { "=gt=", ">" }, { "=ge=", ">=" }
        };
        for (final String property : PROPERTIES) {
            for (final String prefix : new String[] { "", "alarm." }) {
                for (final String[] operator : operators) {
                    final CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
                    applyFiql(builder, prefix + property + operator[0] + "3000000000");
                    assertEquals(1, builder.toCriteria().getRestrictions().size());
                    final Restriction restriction = builder.toCriteria().getRestrictions().iterator().next();
                    assertTrue(restriction instanceof SqlRestriction);
                    final SqlRestriction sql = (SqlRestriction) restriction;
                    assertTrue(sql.getAttribute().matches("(?s).*\\s" + operator[1] + "\\s+\\?\\s*\\)\\s*"));
                    assertFalse(sql.getAttribute().contains("3000000000"));
                    assertArrayEquals(new Object[] { 3000000000L }, sql.getParameters());
                    assertArrayEquals(new SqlRestriction.Type[] { SqlRestriction.Type.LONG }, sql.getTypes());
                }
            }
        }
    }

    @Test
    public void fiqlRejectsInvalidCountsBeforeAddingRestrictions() {
        for (final String property : PROPERTIES) {
            for (final String prefix : new String[] { "", "alarm." }) {
                for (final String payload : new String[] {
                        "0 OR 1=1--", "abc", "1*", "1.5", "9223372036854775808"
                }) {
                    final CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
                    try {
                        applyFiql(builder, prefix + property + "==" + payload);
                        fail("expected invalid count to be rejected for " + prefix + property);
                    } catch (final NumberFormatException expected) {
                        assertTrue(builder.toCriteria().getRestrictions().isEmpty());
                    }
                }
            }
        }
    }

    @Test
    public void fiqlRejectsNullCountsBeforeAddingRestrictions() {
        for (final String property : PROPERTIES) {
            final CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
            try {
                applyFiql(builder, "alarm." + property + "==" + CriteriaBuilderSearchVisitor.NULL_VALUE);
                fail("expected null count to be rejected for " + property);
            } catch (final IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains(property));
                assertTrue(builder.toCriteria().getRestrictions().isEmpty());
            }
        }
    }
}
