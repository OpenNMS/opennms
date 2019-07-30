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

import java.util.Collection;

import org.junit.Test;
import org.junit.Assert;
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
}
