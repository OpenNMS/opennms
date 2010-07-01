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
// Modifications:
//
// 2006 Aug 15: Use generics for collections, add a log message. - dj@opennms.org
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

package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.Collection;
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
    public SnmpCollectionResource findResource(SnmpInstId inst) {
        return getIfMap().get(inst.toInt());
    }

    /** {@inheritDoc} */
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
    public Collection<IfInfo> getResources() {
        return m_ifMap.values();
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<SnmpAttributeType> loadAttributeTypes() {
        return getCollection().getIndexedAttributeTypesForResourceType(getAgent(), this);
    }
    
    
}
