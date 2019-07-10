/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.datachoices.internal;

import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DetailedUsageStatisticsReportDTO extends UsageStatisticsReportDTO {

    private String m_hostname;
    
    private int m_seLinuxEnforce;
    
    private String m_jvmUserName;
    
    private Map<String,Map<String,Number>> m_fsUtilInfo;

    private String m_rdbmsType;

    private String m_rdbmsVersion;

    private boolean m_rdbmsOnLocalhost;

    public String getHostname() {
        return m_hostname;
    }

    public void setHostname(String hostname) {
        m_hostname = hostname;
    }

    public String getJvmUserName() {
        return m_jvmUserName;
    }

    public void setJvmUserName(String jvmUserName) {
        m_jvmUserName = jvmUserName;
    }

    public int getSeLinuxEnforce() {
        return m_seLinuxEnforce;
    }

    public void setSeLinuxEnforce(int seLinuxEnforce) {
        m_seLinuxEnforce = seLinuxEnforce;
    }

    public Map<String, Map<String, Number>> getFsUtilInfo() {
        return m_fsUtilInfo;
    }

    public void setFsUtilInfo(Map<String, Map<String, Number>> fsUtilInfo) {
        m_fsUtilInfo = fsUtilInfo;
    }

    public String getRdbmsType() {
        return m_rdbmsType;
    }

    public void setRdbmsType(String rdbmsType) {
        m_rdbmsType = rdbmsType;
    }

    public String getRdbmsVersion() {
        return m_rdbmsVersion;
    }

    public void setRdbmsVersion(String rdbmsVersion) {
        m_rdbmsVersion = rdbmsVersion;
    }

    public boolean isRdbmsOnLocalhost() {
        return m_rdbmsOnLocalhost;
    }

    public void setRdbmsOnLocalhost(boolean rdbmsOnLocalhost) {
        m_rdbmsOnLocalhost = rdbmsOnLocalhost;
    }
    
    
}
