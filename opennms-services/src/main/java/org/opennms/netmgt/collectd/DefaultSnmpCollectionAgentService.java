/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.opennms.netmgt.collection.core.DefaultCollectionAgentService;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

/**
 * Represents a remote SNMP agent on a specific IPv4 interface.
 *
 * @author ranger
 * @version $Id: $
 */
// Eventually, we should be constructing these instances in the context and using
// annotation-based transaction processing.
//@Transactional(propagation=Propagation.REQUIRED)
public class DefaultSnmpCollectionAgentService extends DefaultCollectionAgentService implements SnmpCollectionAgentService {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCollectionAgentService.class);
    
    /**
     * <p>create</p>
     *
     * @param ifaceId a {@link java.lang.Integer} object.
     * @param ifaceDao a {@link org.opennms.netmgt.dao.api.IpInterfaceDao} object.
     * @param transMgr a {@link org.springframework.transaction.PlatformTransactionManager} object.
     * @return a {@link org.opennms.netmgt.collection.api.CollectionAgentService} object.
     */
    public static SnmpCollectionAgentService create(Integer ifaceId, final IpInterfaceDao ifaceDao, final PlatformTransactionManager transMgr) {
        SnmpCollectionAgentService agent = new DefaultSnmpCollectionAgentService(ifaceId, ifaceDao);
        
        TransactionProxyFactoryBean bean = new TransactionProxyFactoryBean();
        bean.setTransactionManager(transMgr);
        bean.setTarget(agent);
        
        Properties props = new Properties();
        props.put("*", "PROPAGATION_REQUIRED");
        
        bean.setTransactionAttributes(props);
        
        bean.afterPropertiesSet();
        
        return (SnmpCollectionAgentService) bean.getObject();
    }

    protected DefaultSnmpCollectionAgentService(Integer ifaceId, IpInterfaceDao ifaceDao) {
        super(ifaceId, ifaceDao);
    }

    private OnmsNode getNode() {
        return getIpInterface().getNode();
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a int.
     */
    @Override
    public int getIfIndex() {
        return (getIpInterface().getIfIndex() == null ? -1 : getIpInterface().getIfIndex().intValue());
    }

    /**
     * <p>getSysObjectId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getSysObjectId() {
        return getIpInterface().getNode().getSysObjectId();
    }

    /**
     * <p>getIsSnmpPrimary</p>
     *
     * @return a {@link org.opennms.netmgt.model.PrimaryType} object.
     */
    @Override
    public PrimaryType getIsSnmpPrimary() {
        return getIpInterface().getIsSnmpPrimary();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "Agent[nodeid = "+getNodeId()+" ipaddr= "+getHostAddress()+']';
    }

    /**
     * <p>getAgentConfig</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    @Override
    public SnmpAgentConfig getAgentConfig() {
        return SnmpPeerFactory.getInstance().getAgentConfig(getInetAddress(), getLocationName());
    }

    /**
     * <p>getSnmpInterfaceData</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @Override
    public Set<SnmpIfData> getSnmpInterfaceData() {
        
        Set<OnmsSnmpInterface> snmpIfs = getSnmpInterfaces();
    	
        Set<SnmpIfData> ifData = new LinkedHashSet<SnmpIfData>(snmpIfs.size());
        
        for(OnmsSnmpInterface snmpIface : snmpIfs) {
    		logInitializeSnmpIf(snmpIface);
    		SnmpIfData snmpIfData = new SnmpIfData(snmpIface);
    		ifData.add(snmpIfData);
            //ifInfos.add(new IfInfo(type, agent, snmpIfData));
    	}
        return ifData;
    }


    private Set<OnmsSnmpInterface> getSnmpInterfaces() {
        OnmsNode node = getNode();
    
    	Set<OnmsSnmpInterface> snmpIfs = node.getSnmpInterfaces();
    	
    	if (snmpIfs.size() == 0) {
            LOG.debug("no known SNMP interfaces for node {}", node);
    	}
        return snmpIfs;
    }

    private static void logInitializeSnmpIf(OnmsSnmpInterface snmpIface) {
        LOG.debug("initialize: snmpifindex = {}, snmpifname = {}, snmpifdescr = {}, snmpphysaddr = -{}-", snmpIface.getIfIndex(), snmpIface.getIfName(), snmpIface.getIfDescr(), snmpIface.getPhysAddr());
        LOG.debug("initialize: ifLabel = '{}'", snmpIface.computeLabelForRRD());
    }

}
