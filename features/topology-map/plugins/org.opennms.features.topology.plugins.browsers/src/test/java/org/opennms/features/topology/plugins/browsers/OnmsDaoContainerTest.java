/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.browsers;


import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.model.OnmsAlarm;

public class OnmsDaoContainerTest {

    @Test
    public void testGetItemIdsWithOnlyOneItem() {
        MockTransactionTemplate transactionTemplate = new MockTransactionTemplate();
        transactionTemplate.afterPropertiesSet();

        List<OnmsAlarm> alarmList = new ArrayList<>();
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(102);
        alarmList.add(alarm);

        final AlarmDao alarmDaoMock = EasyMock.createNiceMock(AlarmDao.class);
        EasyMock.expect(alarmDaoMock.countMatching((Criteria)EasyMock.anyObject())).andReturn(1);
        EasyMock.expect(alarmDaoMock.findMatching((Criteria)EasyMock.anyObject())).andReturn(alarmList);
        EasyMock.replay(alarmDaoMock);

        AlarmDaoContainer container = new AlarmDaoContainer(alarmDaoMock, transactionTemplate);

        List<Integer> items = container.getItemIds(0, 1);
        Assert.assertNotNull(items);
        Assert.assertEquals(1, items.size());
    }
}
