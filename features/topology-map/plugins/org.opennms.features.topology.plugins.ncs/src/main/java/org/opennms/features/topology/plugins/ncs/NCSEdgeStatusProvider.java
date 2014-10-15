/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.ncs;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.features.topology.api.topo.*;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NCSEdgeStatusProvider implements EdgeStatusProvider{

    public class NCSLinkStatus implements Status{

        private final String m_status;

        public NCSLinkStatus(String status) {
            m_status = status.toLowerCase();
        }

        @Override
        public String computeStatus() {
            return m_status;
        }

        @Override
        public Map<String, String> getStatusProperties() {
            return null;
        }

    }

    private AlarmDao m_alarmDao;
    private final Pattern m_foreignSourcePattern;
    private final Pattern m_foreignIdPattern;

    public NCSEdgeStatusProvider() {
        m_foreignSourcePattern = Pattern.compile("foreignSource=(.*?)\\(.*?\\)");
        m_foreignIdPattern = Pattern.compile("foreignId=(.*?)\\(.*?\\)");
    }

    @Override
    public String getNameSpace() {
        return NCSPathEdgeProvider.PATH_NAMESPACE + "::NCS";
    }

    @Override
    public Map<EdgeRef, Status> getStatusForEdges(EdgeProvider edgeProvider, Collection<EdgeRef> edges, Criteria[] criteria) {
        List<EdgeRef> ncsEdges = new ArrayList<EdgeRef>(Collections2.filter(edges, new Predicate<EdgeRef>() {
            @Override
            public boolean apply(EdgeRef edgeRef) {
                return edgeRef.getNamespace().equals("ncs");
            }
        }));

        Set<String> alarms = getNCSImpactedAlarms();

        Map<EdgeRef, Status> statusMap = new HashMap<EdgeRef, Status>();
        for (EdgeRef edge : ncsEdges) {
            NCSEdgeProvider.NCSEdge e = (NCSEdgeProvider.NCSEdge) edge;
            e.setStatus("up");
            statusMap.put(edge, new NCSLinkStatus("up"));
            if (alarms.contains(e.getSourceElementName()) || alarms.contains(e.getTargetElementName())) {
                statusMap.put(edge, new NCSLinkStatus("down"));
                e.setStatus("down");
            }
        }

        return statusMap;
    }

    private Set<String> getNCSImpactedAlarms() {
        org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OnmsAlarm.class);
        criteria.addRestriction(new EqRestriction("uei", "uei.opennms.org/internal/ncs/componentImpacted"));

        List<OnmsAlarm> alarms = getAlarmDao().findMatching(criteria);


        Set<String> alarmsSet = new HashSet<String>();

        for (OnmsAlarm alarm : alarms) {

            String eventParms = alarm.getEventParms();
            Matcher foreignSourceMatcher = m_foreignSourcePattern.matcher(eventParms);
            Matcher foreignIdMatcher = m_foreignIdPattern.matcher(eventParms);

            String foreignSource = null;
            while (foreignSourceMatcher.find()) {
                foreignSource = foreignSourceMatcher.group(1);
            }

            String foreignId = null;
            while (foreignIdMatcher.find()) {
                foreignId = foreignIdMatcher.group(1);
            }


            if (foreignSource != null && foreignId != null) {
                alarmsSet.add(foreignSource + "::" + foreignId);
            }

        }

        return alarmsSet;
    }

    private void getParms(String eventParms) {
        String[] parms = eventParms.split(";");

    }

    @Override
    public boolean contributesTo(String namespace) {
        return namespace.equals("nodes");
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }
}
