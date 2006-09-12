//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.web.svclayer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;



/**
 * Use this class to aggregate status to be presented in a view layer technology.
 * 
 * @author david hustace
 *
 */
public class AggregateStatus {
    
    private String m_label;
    private Integer m_totalEntityCount;
    private Integer m_downEntityCount;
    private String m_status;
    
    public static final String NODES_ARE_DOWN = "Critical";
    public static final String ONE_SERVICE_DOWN = "Warning";
    public static final String ALL_NODES_UP = "Normal";
    
    public AggregateStatus(Set<OnmsNode> nodes) {
    	computeStatusValues(nodes);
    }

    public String getStatus() {
        return m_status;
    }
    private void setStatus(String color) {
        m_status = color;
    }
    public Integer getDownEntityCount() {
        return m_downEntityCount;
    }
    private void setDownEntityCount(Integer downEntityCount) {
        m_downEntityCount = downEntityCount;
    }
    public String getLabel() {
        return m_label;
    }
    public void setLabel(String label) {
        m_label = label;
    }
    public Integer getTotalEntityCount() {
        return m_totalEntityCount;
    }
    private void setTotalEntityCount(Integer totalEntityCount) {
        m_totalEntityCount = totalEntityCount;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(m_label == null ? "null" : m_label);
        sb.append(": ");
        sb.append(m_downEntityCount == null ? -1 : m_downEntityCount);
        sb.append(" down of ");
        sb.append(m_totalEntityCount == null ? -1 : m_totalEntityCount);
        sb.append(" total.");
        return sb.toString();
    }
    
    
	private String computeStatus(Collection<OnmsNode> nodes) {
	    
	    String color = AggregateStatus.ALL_NODES_UP;
	    
	    if (getDownEntityCount() >= 1) {
	        color = AggregateStatus.NODES_ARE_DOWN;
	        return color;
	    }
	    
	    for (Iterator<OnmsNode> it = nodes.iterator(); it.hasNext();) {
	        OnmsNode node = it.next();
	        Set<OnmsIpInterface> ifs = node.getIpInterfaces();
	        for (Iterator<OnmsIpInterface> ifIter = ifs.iterator(); ifIter.hasNext();) {
	            OnmsIpInterface ipIf = ifIter.next();
	            Set<OnmsMonitoredService> svcs = ipIf.getMonitoredServices();
	            for (Iterator<OnmsMonitoredService> svcIter = svcs.iterator(); svcIter.hasNext();) {
	                OnmsMonitoredService svc = svcIter.next();
	                if (svc.isDown()) {
	                    color = AggregateStatus.ONE_SERVICE_DOWN;
	                    return color;  //quick exit this mess
	                }
	            }
	        }
	    }
	    return color;
	}
	private Integer computeDownCount(Collection<OnmsNode> nodes) {
	    int totalNodesDown = 0;
	    
	    for (OnmsNode node : nodes) {
	        if (node.isDown()) {
	            totalNodesDown += 1;
	        }
	    }
	    return new Integer(totalNodesDown);
	}
	private AggregateStatus computeStatusValues(Set<OnmsNode> nodes) {
		if (nodes == null || nodes.isEmpty()) {
	        setDownEntityCount(0);
	        setTotalEntityCount(0);
	        setStatus(AggregateStatus.ALL_NODES_UP);
	    } else {
	        setDownEntityCount(computeDownCount(nodes));
	        setTotalEntityCount(nodes.size());
	        setStatus(computeStatus(nodes));
	    }
	    return this;
	}

}
