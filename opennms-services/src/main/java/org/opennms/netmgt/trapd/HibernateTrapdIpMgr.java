//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 26: Rename TrapdIPMgr to JdbcTrapdIpMgr and create an interface for the key methods, TrapdIpMgr. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * This class represents a singular instance that is used to map trap IP
 * addresses to known nodes.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public class HibernateTrapdIpMgr implements TrapdIpMgr, InitializingBean {
    private IpInterfaceDao m_ipInterfaceDao;
    
    /**
     * A Map of IP addresses and node IDs
     */
    private Map<InetAddress, Integer> m_knownips = new HashMap<InetAddress, Integer>();

    /**
     * Default construct for the instance.
     */
    public HibernateTrapdIpMgr() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#dataSourceSync()
     */
    /**
     * <p>dataSourceSync</p>
     */
    @Transactional(readOnly = true)
    public synchronized void dataSourceSync() {
        m_knownips = m_ipInterfaceDao.getInterfacesForNodes();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#getNodeId(java.lang.String)
     */
    /** {@inheritDoc} */
    public synchronized long getNodeId(String addr) {
        if (addr == null) {
            return -1;
        }
        return longValue(m_knownips.get(addr));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#setNodeId(java.lang.String, long)
     */
    /** {@inheritDoc} */
    public synchronized long setNodeId(String addr, long nodeid) {
        if (addr == null || nodeid == -1) {
            return -1;
        }
        
        return longValue(m_knownips.put(InetAddressUtils.getInetAddress(addr), new Integer((int) nodeid)));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#removeNodeId(java.lang.String)
     */
    /** {@inheritDoc} */
    public synchronized long removeNodeId(String addr) {
        if (addr == null) {
            return -1;
        }
        return longValue(m_knownips.remove(addr));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#clearKnownIpsMap()
     */
    /**
     * <p>clearKnownIpsMap</p>
     */
    public synchronized void clearKnownIpsMap() {
        m_knownips.clear();
    }

    private static long longValue(Integer result) {
        return (result == null ? -1 : result.longValue());
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_ipInterfaceDao != null, "property ipInterfaceDao must be set");
    }

    /**
     * <p>getIpInterfaceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.IpInterfaceDao} object.
     */
    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    /**
     * <p>setIpInterfaceDao</p>
     *
     * @param ipInterfaceDao a {@link org.opennms.netmgt.dao.IpInterfaceDao} object.
     */
    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }
}
