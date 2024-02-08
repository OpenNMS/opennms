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
package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.AlarmAssociationDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.AlarmAssociation;
import org.opennms.netmgt.model.OnmsAlarm;

public class MockAlarmAssociationDao extends AbstractMockDao<AlarmAssociation, Integer> implements AlarmAssociationDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public Integer save(final AlarmAssociation ass) {
        Integer retval = super.save(ass);
        updateSubObjects(ass);
        return retval;
    }

    @Override
    public void update(final AlarmAssociation ass) {
        super.update(ass);
        updateSubObjects(ass);
    }

    private void updateSubObjects(final AlarmAssociation ass) {
        if (ass.getRelatedAlarm().getId() == null) {
            getAlarmDao().save(ass.getRelatedAlarm());
        }
        if (ass.getSituationAlarm().getId() == null) {
            getAlarmDao().save(ass.getSituationAlarm());
        }
    }

    @Override
    protected void generateId(final AlarmAssociation ass) {
        ass.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final AlarmAssociation ass) {
        return ass.getId();
    }

    @Override
    public List<AlarmAssociation> getAssociationsForSituation(final int situationId) {
        final OnmsAlarm alarm = getAlarmDao().get(situationId);
        if (alarm == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(alarm.getAssociatedAlarms());
    }
}
