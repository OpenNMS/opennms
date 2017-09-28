/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.topology;

public class EdgeAlarmStatusSummary {

    public static final String getDefaultEdgeId(int sourceId,int targetId) {
        return Math.min(sourceId, targetId) + "|" + Math.max(sourceId, targetId);
    }

    private final String m_targetId;
    private final String m_sourceId;
    private String m_eventUEI;
    private final String m_id;

    public EdgeAlarmStatusSummary(String id,int sourceId, int targetId, String eventUEI) {
        m_id = id;
        m_sourceId = String.valueOf(sourceId);
        m_targetId = String.valueOf(targetId);
        m_eventUEI = eventUEI;
    }
    
    public EdgeAlarmStatusSummary(int sourceId, int targetId, String eventUEI) {
        m_id = Math.min(sourceId, targetId) + "|" + Math.max(sourceId, targetId);
        m_sourceId = String.valueOf(sourceId);
        m_targetId = String.valueOf(targetId);
        m_eventUEI = eventUEI;
    }

    public void setEventUEI(String eventUEI) {
        m_eventUEI = eventUEI;
    }

    public String getSourceId() {
        return m_sourceId;
    }

    public String getTargetId() {
        return m_targetId;
    }

    public String getId() {
        return m_id;
    }

    public String getEventUEI() { return m_eventUEI == null ? "unknown" : m_eventUEI; }

}
