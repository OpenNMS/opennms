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

package org.opennms.netmgt.tools.spectrum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlertMapping {
    private String m_alertCode;
    private String m_eventCode;
    private List<OidMapping> m_oidMappings;
    
    public AlertMapping(String alertCode) {
        if (alertCode == null) {
            throw new IllegalArgumentException("The alert code must not be null");
        }
        m_alertCode = alertCode;
        m_eventCode = null;
        m_oidMappings = new ArrayList<>();
    }
    
    public AlertMapping(String alertCode, String eventCode, List<OidMapping> oidMappings) {
        if (alertCode == null || eventCode == null || oidMappings == null) {
            throw new IllegalArgumentException("The alert code, event code, and OID mappings must not be null");
        }
        m_alertCode = alertCode;
        m_eventCode = eventCode;
        if (oidMappings != null) {
            m_oidMappings = oidMappings;
        } else {
            m_oidMappings = new ArrayList<>();
        }
    }
    
    public String getAlertCode() {
        return m_alertCode;
    }
    
    public void setAlertCode(String alertCode) {
        if (alertCode == null) {
            throw new IllegalArgumentException("The alert code must not be null");
        }
        m_alertCode = alertCode;
    }
    
    public String getEventCode() {
        return m_eventCode;
    }
    
    public void setEventCode(String eventCode) {
        if (eventCode == null) {
            throw new IllegalArgumentException("The event code must not be null");
        }
        if (! eventCode.matches("^0x[0-9A-Fa-f]{1,8}$")) {
            throw new IllegalArgumentException("The event code must be of the form 0xNNNNNNNN and must have a value between 0 and 0xffffffff, but received '" + eventCode + "'");
        }
        m_eventCode = eventCode;
    }
    
    public List<OidMapping> getOidMappings() {
        return Collections.unmodifiableList(m_oidMappings);
    }
    
    public void setOidMappings(List<OidMapping> oidMappings) {
        if (oidMappings == null) {
            throw new IllegalArgumentException("The list of OID mappings must not be null");
        }
        m_oidMappings = oidMappings;
    }
    
    public void addOidMapping(OidMapping oidMapping) {
        if (oidMapping == null) {
            throw new IllegalArgumentException("The OID mapping must not be null");
        }
        if (m_oidMappings == null) {
            m_oidMappings = new ArrayList<>();
        }
        if (! m_oidMappings.contains(oidMapping)) {
            m_oidMappings.add(oidMapping);
        }
    }
    
    private String[] getAlertCodeOidComponents() {
        String tempAlertCode = m_alertCode;
        if (m_alertCode.startsWith(".")) {
            tempAlertCode = m_alertCode.substring(1);
        }
        return tempAlertCode.split("\\."); 
    }
    
    public String getTrapOid() {
        String[] oidComponents = getAlertCodeOidComponents();
        final StringBuilder trapOidBuilder = new StringBuilder("");
        for (int i = 0; i < (oidComponents.length - 2); i++) {
            trapOidBuilder.append(".").append(oidComponents[i]);
        }
        return trapOidBuilder.toString();
    }
    
    public String getTrapGenericType() {
        String[] oidComponents = getAlertCodeOidComponents();
        return oidComponents[ oidComponents.length - 2 ];
    }
    
    public String getTrapSpecificType() {
        String[] oidComponents = getAlertCodeOidComponents();
        return oidComponents[ oidComponents.length - 1 ];
    }
}