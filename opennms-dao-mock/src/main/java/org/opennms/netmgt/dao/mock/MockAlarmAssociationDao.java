/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
