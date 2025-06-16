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

import java.util.List;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.CellStyleGenerator;

public class NodeTableCellStyleGenerator implements CellStyleGenerator {

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
        return alarm == null ? cellStyleRenderer.getStyle(OnmsSeverity.NORMAL.getId(), false) : cellStyleRenderer.getStyle(alarm);
    }

    public OnmsAlarm getAlarm(int nodeId) {
        CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
        builder.alias("node", "node");
        builder.ne("severity", OnmsSeverity.CLEARED);
        builder.orderBy("severity").desc();
        builder.eq("node.id", nodeId);
        builder.limit(Integer.valueOf(1));
        List<OnmsAlarm> alarms = alarmDao.findMatching(builder.toCriteria());
        return alarms == null || alarms.isEmpty() ? null : alarms.get(0);
    }
}
