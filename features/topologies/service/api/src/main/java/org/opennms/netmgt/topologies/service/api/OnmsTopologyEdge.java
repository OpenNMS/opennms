/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.topologies.service.api;

public class OnmsTopologyEdge extends OnmsTopologyAbstractRef implements OnmsTopologyRef {

    public static OnmsTopologyEdge create(OnmsTopologyVertex source, OnmsTopologyVertex target, Integer sourceIndex, Integer targetIndex) {
        if (source !=  null && target != null && !source.getId().equals(target.getId())) {
            return new OnmsTopologyEdge(source.getId()+":"+sourceIndex+"-"+target.getId()+":"+targetIndex, source, target);
        }
        
        return null;
    }
        
    private final OnmsTopologyVertex m_source;
    private final OnmsTopologyVertex m_target;
       
    private String m_sourcePort;
    private String m_targetPort;

    private String m_sourceAddr;
    private String m_targetAddr;

    private String m_speed;
    
    private Integer m_sourceIfIndex;
    private Integer m_targetIfIndex;
    private String m_discoveredBy;

    private OnmsTopologyEdge(String id, OnmsTopologyVertex source, OnmsTopologyVertex target) {
        super(id);
        m_source = source;
        m_target = target;
    }

    public String getSourcePort() {
        return m_sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        m_sourcePort = sourcePort;
    }

    public String getTargetPort() {
        return m_targetPort;
    }

    public void setTargetPort(String targetPort) {
        m_targetPort = targetPort;
    }

    public String getSourceAddr() {
        return m_sourceAddr;
    }

    public void setSourceAddr(String sourceAddr) {
        m_sourceAddr = sourceAddr;
    }

    public String getTargetAddr() {
        return m_targetAddr;
    }

    public void setTargetAddr(String targetAddr) {
        m_targetAddr = targetAddr;
    }

    public String getSpeed() {
        return m_speed;
    }

    public void setSpeed(String speed) {
        m_speed = speed;
    }

    public Integer getSourceIfIndex() {
        return m_sourceIfIndex;
    }

    public void setSourceIfIndex(Integer sourceIfIndex) {
        m_sourceIfIndex = sourceIfIndex;
    }

    public Integer getTargetIfIndex() {
        return m_targetIfIndex;
    }

    public void setTargetIfIndex(Integer targetIfIndex) {
        m_targetIfIndex = targetIfIndex;
    }

    public OnmsTopologyVertex getSource() {
        return m_source;
    }

    public OnmsTopologyVertex getTarget() {
        return m_target;
    }


    public String getDiscoveredBy() {
        return m_discoveredBy;
    }

    public void setDiscoveredBy(String discoveredBy) {
        m_discoveredBy = discoveredBy;
    }



    
}
