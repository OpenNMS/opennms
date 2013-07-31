package org.opennms.features.topology.plugins.browsers;


import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;

import java.util.ArrayList;
import java.util.List;

public class OnmsDaoContainerTest {

    @Test
    public void testGetItemIdsWithOnlyOneItem() {

        List<OnmsAlarm> alarmList = new ArrayList<OnmsAlarm>();
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(102);
        alarmList.add(alarm);

        final AlarmDao alarmDaoMock = EasyMock.createNiceMock(AlarmDao.class);
        EasyMock.expect(alarmDaoMock.countMatching((Criteria)EasyMock.anyObject())).andReturn(1);
        EasyMock.expect(alarmDaoMock.findMatching((Criteria)EasyMock.anyObject())).andReturn(alarmList);
        EasyMock.replay(alarmDaoMock);

        OnmsDaoContainer container = new AlarmDaoContainer(alarmDaoMock);

        List items = container.getItemIds(0, 1);
        Assert.assertNotNull(items);
        Assert.assertEquals(1, items.size());
    }
}
