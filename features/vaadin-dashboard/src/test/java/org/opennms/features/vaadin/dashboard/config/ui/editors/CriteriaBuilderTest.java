/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.dashboard.config.ui.editors;

import static org.hamcrest.Matchers.hasItem;

import java.sql.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.BetweenRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.GeRestriction;
import org.opennms.core.criteria.restrictions.GtRestriction;
import org.opennms.core.criteria.restrictions.IlikeRestriction;
import org.opennms.core.criteria.restrictions.InRestriction;
import org.opennms.core.criteria.restrictions.IplikeRestriction;
import org.opennms.core.criteria.restrictions.LeRestriction;
import org.opennms.core.criteria.restrictions.LikeRestriction;
import org.opennms.core.criteria.restrictions.LtRestriction;
import org.opennms.core.criteria.restrictions.NotNullRestriction;
import org.opennms.core.criteria.restrictions.NullRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;

public class CriteriaBuilderTest {

    public void testRestriction(final String criteria, Restriction restriction) {
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsAlarm.class);
        final CriteriaBuilderHelper criteriaBuilderHelper = new CriteriaBuilderHelper(OnmsAlarm.class, OnmsNode.class, OnmsCategory.class, OnmsEvent.class);
        criteriaBuilderHelper.parseConfiguration(criteriaBuilder, criteria);
        final Collection<Restriction> restrictions = criteriaBuilder.toCriteria().getRestrictions();
        Assert.assertEquals(restrictions.iterator().next().toString(), restriction.toString());
    }

    @Test
    public void testInRestriction() {
        testRestriction("Between(node.id,1,4).OrderBy(lastEventTime).Limit(10)", new BetweenRestriction("node.id", 1, 4));
        testRestriction("Contains(node.label,node).OrderBy(lastEventTime).Limit(10)", Restrictions.ilike("node.label", "%node%"));
        testRestriction("Eq(node.id,1).OrderBy(lastEventTime).Limit(10)", new EqRestriction("node.id", 1));
        testRestriction("Ge(node.id,1).OrderBy(lastEventTime).Limit(10)", new GeRestriction("node.id", 1));
        testRestriction("Gt(node.id,1).OrderBy(lastEventTime).Limit(10)", new GtRestriction("node.id", 1));
        testRestriction("Ilike(node.label,node).OrderBy(lastEventTime).Limit(10)", new IlikeRestriction("node.label", "node"));
        testRestriction("Iplike(node.label,node).OrderBy(lastEventTime).Limit(10)", new IplikeRestriction("node.label", "node"));
        testRestriction("IsNull(node.label).OrderBy(lastEventTime).Limit(10)", new NullRestriction("node.label"));
        testRestriction("IsNotNull(node.label).OrderBy(lastEventTime).Limit(10)", new NotNullRestriction("node.label"));
        testRestriction("Le(node.id,1).OrderBy(lastEventTime).Limit(10)", new LeRestriction("node.id", 1));
        testRestriction("Lt(node.id,1).OrderBy(lastEventTime).Limit(10)", new LtRestriction("node.id", 1));
        testRestriction("Like(node.label,node).OrderBy(lastEventTime).Limit(10)", new LikeRestriction("node.label", "node"));
        testRestriction("Ne(node.id,1).OrderBy(lastEventTime).Limit(10)", Restrictions.not(Restrictions.eq("node.id", 1)));
        // integer
        testRestriction("In(node.id,1|2).OrderBy(lastEventTime).Limit(10)", new InRestriction("node.id", new Object[]{1, 2}));
        // string
        testRestriction("In(node.label,node1|node2).OrderBy(lastEventTime).Limit(10)", new InRestriction("node.label", new String[]{"node2", "node1"}));
        // encoded string
        testRestriction("In(node.label,no%28de|no%29de).OrderBy(lastEventTime).Limit(10)", new InRestriction("node.label", new String[]{"no)de", "no(de"}));
    }

    public void testDateRestriction(final String criteria, final ZonedDateTime zonedDateTime, final boolean strict) {
        final Set<String> validRestrictions = new TreeSet<>();
        validRestrictions.add(new LtRestriction("lastEventTime", Date.from(zonedDateTime.toInstant())).toString());

        // add a second restriction one second after the original one...
        if (!strict) {
            validRestrictions.add(new LtRestriction("lastEventTime", Date.from(zonedDateTime.plus(1, ChronoUnit.SECONDS).toInstant())).toString());
        }

        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsAlarm.class);
        final CriteriaBuilderHelper criteriaBuilderHelper = new CriteriaBuilderHelper(OnmsAlarm.class, OnmsNode.class, OnmsCategory.class, OnmsEvent.class);
        criteriaBuilderHelper.parseConfiguration(criteriaBuilder, criteria);
        final Collection<Restriction> restrictions = criteriaBuilder.toCriteria().getRestrictions();
        Assert.assertThat(validRestrictions, hasItem(restrictions.iterator().next().toString()));
    }

    @Test
    public void testDateHandling() {
        final ZonedDateTime currentTime = ZonedDateTime.now();
        final ZonedDateTime currentTimeMinusFive = currentTime.minus(5, ChronoUnit.MINUTES);
        final ZonedDateTime currentTimeMinusTen = currentTime.minus(10, ChronoUnit.MINUTES);
        final ZonedDateTime currentTimePlusFive = currentTime.plus(5, ChronoUnit.MINUTES);
        final ZonedDateTime currentTimePlusTen = currentTime.plus(10, ChronoUnit.MINUTES);

        // check relative values
        testDateRestriction("Lt(lastEventTime,-300)", currentTimeMinusFive, false);
        testDateRestriction("Lt(lastEventTime,-600)", currentTimeMinusTen, false);
        testDateRestriction("Lt(lastEventTime,%2B300)", currentTimePlusFive, false);
        testDateRestriction("Lt(lastEventTime,%2B600)", currentTimePlusTen, false);
        testDateRestriction("Lt(lastEventTime,%2B0)", currentTime, false);
        testDateRestriction("Lt(lastEventTime,-0)", currentTime, false);
        testDateRestriction("Lt(lastEventTime,0)", currentTime, false);

        // check absolute value (UTC)
        final ZonedDateTime customDate1 = ZonedDateTime.of(2019,6,20,20,45,15,0, ZoneId.of("UTC"));
        testDateRestriction("Lt(lastEventTime,2019-06-20T20:45:15.123Z)", customDate1, true);

        // check absolute value (EST -05:00)
        final ZonedDateTime customDate2 = ZonedDateTime.of(2019,6,20,20,45,15,0, TimeZone.getTimeZone("EST").toZoneId());
        testDateRestriction("Lt(lastEventTime,2019-06-20T20:45:15.123-05:00)", customDate2, true);

        // check absolute value (JST +09:00)
        final ZonedDateTime customDate3 = ZonedDateTime.of(2019,6,20,20,45,15,0, TimeZone.getTimeZone("JST").toZoneId());
        testDateRestriction("Lt(lastEventTime,2019-06-20T20:45:15.123%2B09:00)", customDate3, true);
    }
}
