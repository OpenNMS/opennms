/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opennms.netmgt.snmp.SnmpInstId;

/**
 * <p>IfResourceType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class IfResourceType extends ResourceType {

    private TreeMap<Integer, IfInfo> m_ifMap;

    /**
     * <p>Constructor for IfResourceType.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     */
    public IfResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        super(agent, snmpCollection);
        m_ifMap = new TreeMap<Integer, IfInfo>();
        addKnownIfResources();
    }
    
    private Map<Integer, IfInfo> getIfMap() {
        return m_ifMap;
    }

    private void addIfInfo(final IfInfo ifInfo) {
        getIfMap().put(ifInfo.getIndex(), ifInfo);
    }

    private void addKnownIfResources() {
    	Set<IfInfo> ifInfos = getAgent().getSnmpInterfaceInfo(this);
        
        for(IfInfo ifInfo : ifInfos) {
            addIfInfo(ifInfo);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public SnmpInstId[] getCollectionInstances() {
        List<SnmpInstId> instances = new ArrayList<SnmpInstId>();
        for (IfInfo ifInfo : m_ifMap.values()) {
            if (ifInfo.isCollectionEnabled()) {
                instances.add(new SnmpInstId(ifInfo.getIndex()));
            }
        }
        return instances.toArray(new SnmpInstId[instances.size()]);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpCollectionResource findResource(SnmpInstId inst) {
        return getIfMap().get(inst.toInt());
    }

    /** {@inheritDoc} */
    @Override
    public SnmpCollectionResource findAliasedResource(SnmpInstId inst, String ifAlias) {
        // This is here for completeness but it should not get called from here.
        // findResource should be called instead
        log().debug("findAliasedResource: Should not get called from IfResourceType");
        return null;
    }

    /**
     * <p>getResources</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<IfInfo> getResources() {
        return Collections.unmodifiableCollection(m_ifMap.values());
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<SnmpAttributeType> loadAttributeTypes() {
        return Collections.unmodifiableCollection(getCollection().getIndexedAttributeTypesForResourceType(getAgent(), this));
    }
    
    
}
