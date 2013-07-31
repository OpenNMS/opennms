/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger LOG = LoggerFactory.getLogger(HibernateTrapdIpMgr.class);
	
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
    @Override
    public synchronized void dataSourceSync() {
        m_knownips = m_ipInterfaceDao.getInterfacesForNodes();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#getNodeId(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public synchronized long getNodeId(String addr) {
        if (addr == null) {
            return -1;
        }
        return longValue(m_knownips.get(InetAddressUtils.getInetAddress(addr)));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#setNodeId(java.lang.String, long)
     */
    /** {@inheritDoc} */
    @Override
    public synchronized long setNodeId(String addr, long nodeid) {
        if (addr == null || nodeid == -1) {
            return -1;
        }
        // Only add the address if it doesn't exist on the map. If it exists, only replace the current one if the new address is primary.
        boolean add = true;
        if (m_knownips.containsKey(InetAddressUtils.getInetAddress(addr))) {
            OnmsIpInterface intf = m_ipInterfaceDao.findByNodeIdAndIpAddress(Integer.valueOf((int) nodeid), addr);
            add = intf != null && intf.isPrimary();
            LOG.info("setNodeId: address found {}. Should be added? {}", intf, add);
        }
        return add ? longValue(m_knownips.put(InetAddressUtils.getInetAddress(addr), Integer.valueOf((int) nodeid))) : -1;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#removeNodeId(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public synchronized long removeNodeId(String addr) {
        if (addr == null) {
            return -1;
        }
        return longValue(m_knownips.remove(InetAddressUtils.getInetAddress(addr)));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.trapd.TrapdIpMgr#clearKnownIpsMap()
     */
    /**
     * <p>clearKnownIpsMap</p>
     */
    @Override
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
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_ipInterfaceDao != null, "property ipInterfaceDao must be set");
    }

    /**
     * <p>getIpInterfaceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.IpInterfaceDao} object.
     */
    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    /**
     * <p>setIpInterfaceDao</p>
     *
     * @param ipInterfaceDao a {@link org.opennms.netmgt.dao.api.IpInterfaceDao} object.
     */
    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

}
