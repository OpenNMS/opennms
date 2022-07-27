/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.datachoices.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

import com.google.common.base.Throwables;

@XmlRootElement
public class UsageStatisticsReportDTO {

    private String m_systemId;

    private String m_osName;
    private String m_osVersion;
    private String m_osArch;

    private String m_version;
    private String m_packageName;

    private int m_nodes;
    private int m_ipInterfaces;
    private int m_snmpInterfaces;
    private int m_monitoredServices;
    private int m_events;
    private int m_alarms;

    private Map<String, Long> m_nodesBySysOid = Collections.emptyMap();

    public void setSystemId(String systemId) {
        m_systemId = systemId;
    }

    public String getSystemId() {
        return m_systemId;
    }

    public void setOsName(String osName) {
        m_osName = osName;
    }

    public String getOsName() {
        return m_osName;
    }

    public void setOsVersion(String osVersion){
        m_osVersion = osVersion;
    }

    public String getOsVersion() {
        return m_osVersion;
    }

    public void setOsArch(String osArch) {
        m_osArch = osArch;
    }

    public String getOsArch() {
        return m_osArch;
    }

    public void setVersion(String version) {
        m_version = version;
    }

    public String getVersion() {
        return m_version;
    }

    public void setPackageName(String packageName) {
        m_packageName = packageName;
    }

    public String getPackageName() {
        return m_packageName;
    }

    public void setNodes(int nodes) {
        m_nodes = nodes;
    }

    public int getNodes() {
        return m_nodes;
    }

    public void setIpInterfaces(int ipInterfaces) {
        m_ipInterfaces = ipInterfaces;
    }

    public int getIpInterfaces() {
        return m_ipInterfaces;
    }

    public void setSnmpInterfaces(int snmpInterfaces) {
        m_snmpInterfaces = snmpInterfaces;
    }

    public int getSnmpInterfaces() {
        return m_snmpInterfaces;
    }

    public void setMonitoredServices(int monitoredServices) {
        m_monitoredServices = monitoredServices;
    }

    public int getMonitoredServices() {
        return m_monitoredServices;
    }

    public void setEvents(int events) {
        m_events = events;
    }

    public int getEvents() {
        return m_events;
    }

    public void setAlarms(int alarms) {
        m_alarms = alarms;
    }

    public int getAlarms() {
        return m_alarms;
    }

    public void setNodesBySysOid(Map<String, Long> nodesBySysOid) {
        m_nodesBySysOid = nodesBySysOid;
    }

    public Map<String, Long> getNodesBySysOid() {
        return m_nodesBySysOid;
    }

    public String toJson() {
        return toJson(false);
    }

    public String toJson(boolean prettyPrint) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(Feature.SORT_PROPERTIES_ALPHABETICALLY);
        if (prettyPrint) {
            mapper.enable(Feature.INDENT_OUTPUT);
        }
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
