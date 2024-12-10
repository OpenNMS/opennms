/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.search.providers.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-mock.xml"
})
@JUnitConfigurationEnvironment
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NodeAlarmSearchProviderIT {

    @Autowired
    NodeDao nodeDao;

    @Autowired
    AlarmDao alarmDao;

    @Autowired
    EntityScopeProvider entityScopeProvider;

    @Before
    public void before() {
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("MyNode");
        nodeDao.save(node);

        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setNode(nodeDao.get(1));
        alarm.setSeverity(OnmsSeverity.CRITICAL);
        alarmDao.save(alarm);
    }

    @Test
    public void testEmptyRestrictionList() {
        final NodeAlarmSearchProvider nodeAlarmSearchProvider = new NodeAlarmSearchProvider(nodeDao, alarmDao, entityScopeProvider);
        final SearchResult results = nodeAlarmSearchProvider.query(new SearchQuery("foo"));
        assertTrue(results.isEmpty());
    }

    @Test
    public void testNonEmptyRestrictionList() {
        final NodeAlarmSearchProvider nodeAlarmSearchProvider = new NodeAlarmSearchProvider(nodeDao, alarmDao, entityScopeProvider);
        final SearchResult results = nodeAlarmSearchProvider.query(new SearchQuery("Critical"));
        assertFalse(results.isEmpty());
        assertEquals(1, results.getResults().size());
    }
}
