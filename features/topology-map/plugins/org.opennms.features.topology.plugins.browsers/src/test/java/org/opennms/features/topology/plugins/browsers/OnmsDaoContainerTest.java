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
package org.opennms.features.topology.plugins.browsers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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

        final AlarmDao alarmDaoMock = mock(AlarmDao.class);
        when(alarmDaoMock.countMatching(any(Criteria.class))).thenReturn(1);
        when(alarmDaoMock.findMatching(any(Criteria.class))).thenReturn(alarmList);

        AlarmDaoContainer container = new AlarmDaoContainer(alarmDaoMock, transactionTemplate);

        List<Integer> items = container.getItemIds(0, 1);
        Assert.assertNotNull(items);
        Assert.assertEquals(1, items.size());
    }
}
