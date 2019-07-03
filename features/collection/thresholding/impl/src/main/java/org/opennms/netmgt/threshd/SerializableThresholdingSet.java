/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.rrd.RrdRepository;

public class SerializableThresholdingSet {

    private final int m_nodeId;

    private final String m_hostAddress;

    private final String m_serviceName;

    private final RrdRepository m_repository;

    private ServiceParameters m_svcParams;

    private final List<ThresholdGroup> m_thresholdGroups = new LinkedList<>();

    private final List<String> m_scheduledOutages = new ArrayList<>();

    public SerializableThresholdingSet(ThresholdingSetImpl set) {
        m_nodeId = set.getNodeId();
        m_hostAddress = set.getHostAddress();
        m_serviceName = set.getServiceName();
        m_repository = set.getRepository();
        m_svcParams = set.getServiceParameters();
        m_thresholdGroups.addAll(set.getThresholdGroups());
        m_scheduledOutages.addAll(set.getscheduledOutages());
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getHostAddress() {
        return m_hostAddress;
    }

    public String getServiceName() {
        return m_serviceName;
    }

    public RrdRepository getRepository() {
        return m_repository;
    }

    public ServiceParameters getSvcParams() {
        return m_svcParams;
    }

    public List<ThresholdGroup> getThresholdGroups() {
        return m_thresholdGroups;
    }

    public List<String> getScheduledOutages() {
        return m_scheduledOutages;
    }

}
