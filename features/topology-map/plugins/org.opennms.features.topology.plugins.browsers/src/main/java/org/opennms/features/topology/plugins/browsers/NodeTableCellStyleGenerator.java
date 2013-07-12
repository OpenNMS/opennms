/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc. OpenNMS(R) is a
 * registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version. OpenNMS(R) is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/ For more information
 * contact: OpenNMS(R) Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.topology.plugins.browsers;

import java.util.List;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;

public class NodeTableCellStyleGenerator  implements CellStyleGenerator {

    private final AlarmCellStyleRenderer cellStyleRenderer = new AlarmCellStyleRenderer();
    private AlarmDao alarmDao;
    
    protected AlarmDao getAlarmDao() {
        return alarmDao;
    }
    
    public void setAlarmDao(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }
    
    @Override
    public String getStyle(Table source, Object itemId, Object propertyId) {
        if (itemId == null || !(itemId instanceof Integer)) return "";
        OnmsAlarm alarm = getAlarm(((Integer)itemId).intValue());
        return cellStyleRenderer.getStyle(alarm);
    }

    public OnmsAlarm getAlarm(int nodeId) {
        CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
        builder.alias("node", "node");
        builder.ge("severity", OnmsSeverity.WARNING);
        builder.orderBy("severity").desc();
        builder.eq("node.id", nodeId);
        builder.limit(Integer.valueOf(1));
        List<OnmsAlarm> alarms = alarmDao.findMatching(builder.toCriteria());
        return alarms == null || alarms.isEmpty() ? null : alarms.get(0);
    }
}
