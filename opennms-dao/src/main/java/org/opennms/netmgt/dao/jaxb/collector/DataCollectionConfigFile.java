/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb.collector;

import java.io.File;
import java.util.Iterator;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.Groups;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.config.datacollection.SystemDefChoice;
import org.opennms.netmgt.config.datacollection.Systems;
import org.springframework.core.io.FileSystemResource;

public class DataCollectionConfigFile {
	
	File m_file;
	
	/**
	 * <p>Constructor for DataCollectionConfigFile.</p>
	 *
	 * @param file a {@link java.io.File} object.
	 */
	public DataCollectionConfigFile(File file) {
		m_file = file;
	}
	
	/**
	 * <p>visit</p>
	 *
	 * @param visitor a {@link org.opennms.netmgt.dao.jaxb.collector.DataCollectionVisitor} object.
	 */
	public void visit(DataCollectionVisitor visitor) {
        DatacollectionConfig dataCollectionConfig = getDataCollectionConfig();
        visitor.visitDataCollectionConfig(dataCollectionConfig);
        
        for (Iterator<SnmpCollection> it = dataCollectionConfig.getSnmpCollections().iterator(); it.hasNext();) {
            SnmpCollection snmpCollection = it.next();
            doVisit(snmpCollection, visitor);
        }
        visitor.completeDataCollectionConfig(dataCollectionConfig);
    }
	
	private void doVisit(SnmpCollection snmpCollection, DataCollectionVisitor visitor) {
        visitor.visitSnmpCollection(snmpCollection);
        
        // rrd block
        doVisit(snmpCollection.getRrd(), visitor);
        
        // loop over systems
        doVisit(snmpCollection.getSystems(), visitor);
        
        // loop over groups
        doVisit(snmpCollection.getGroups(), visitor);
        
        visitor.completeSnmpCollection(snmpCollection);
    }

    private void doVisit(Groups groups, DataCollectionVisitor visitor) {
        
        for (Iterator<Group> it = groups.getGroups().iterator(); it.hasNext();) {
            Group group = it.next();
            doVisit(group, visitor);
        }
        
    }

    private void doVisit(Group group, DataCollectionVisitor visitor) {
        visitor.visitGroup(group);
        
        // mibObj
        for (Iterator<MibObj> it = group.getMibObjs().iterator(); it.hasNext();) {
            MibObj mibObj = it.next();
            doVisit(mibObj, visitor);
        }
        
        // subGroups
        for (Iterator<String> it = group.getIncludeGroups().iterator(); it.hasNext();) {
            String subGroup = it.next();
            doVisitSubGroup(subGroup, visitor);
        }
        
        visitor.completeGroup(group);
    }

    private void doVisitSubGroup(String subGroup, DataCollectionVisitor visitor) {
        visitor.visitSubGroup(subGroup);
        visitor.completeSubGroup(subGroup);
    }

    private void doVisit(MibObj mibObj, DataCollectionVisitor visitor) {
        visitor.visitMibObj(mibObj);
        visitor.completeMibObj(mibObj);
    }

    private void doVisit(Systems systems, DataCollectionVisitor visitor) {
        for (Iterator<SystemDef> it = systems.getSystemDefs().iterator(); it.hasNext();) {
            SystemDef systemDef = it.next();
            doVisit(systemDef, visitor);
        }
    }

    private void doVisit(SystemDef systemDef, DataCollectionVisitor visitor) {
        visitor.visitSystemDef(systemDef);
        
        // handle the choice between sysOid and sysOidMask
        doVisit(systemDef.getSystemDefChoice(), visitor);
        
        // handle ipList
        doVisit(systemDef.getIpList(), visitor);
        
        // handle collect
        doVisit(systemDef.getCollect(), visitor);
        
        visitor.completeSystemDef(systemDef);
    }

    private void doVisit(Collect collect, DataCollectionVisitor visitor) {
        visitor.visitCollect(collect);
        
        // visit all the includeGroup specs
        for (Iterator<String> it = collect.getIncludeGroups().iterator(); it.hasNext();) {
            String includeGroup = it.next();
            doVisitIncludeGroup(includeGroup, visitor);
            
        }
        visitor.completeCollect(collect);
    }

    private void doVisitIncludeGroup(String includeGroup, DataCollectionVisitor visitor) {
        visitor.visitIncludeGroup(includeGroup);
        visitor.completeIncludeGroup(includeGroup);
        
    }

    private void doVisit(IpList ipList, DataCollectionVisitor visitor) {
        if (ipList == null) return;
        
        visitor.visitIpList(ipList);
        visitor.completeIpList(ipList);
    }

    private void doVisit(SystemDefChoice systemDefChoice, DataCollectionVisitor visitor) {
        if (systemDefChoice.getSysoid() != null)
            doVisitSysOid(systemDefChoice.getSysoid(), visitor);
        
        if (systemDefChoice.getSysoidMask() != null)
            doVisitSysOidMask(systemDefChoice.getSysoidMask(), visitor);
        
    }

    private void doVisitSysOidMask(String sysoidMask, DataCollectionVisitor visitor) {
        visitor.visitSysOidMask(sysoidMask);
        visitor.completeSysOidMask(sysoidMask);
    }

    private void doVisitSysOid(String sysoid, DataCollectionVisitor visitor) {
        visitor.visitSysOid(sysoid);
        visitor.completeSysOid(sysoid);
    }

    private void doVisit(Rrd rrd, DataCollectionVisitor visitor) {
        visitor.visitRrd(rrd);
        for (Iterator<String> it = rrd.getRras().iterator(); it.hasNext();) {
            String rra = it.next();
            doVisitRra(rra, visitor);
        }
        visitor.completeRrd(rrd);
    }

    private void doVisitRra(String rra, DataCollectionVisitor visitor) {
        visitor.visitRra(rra);
        visitor.completeRra(rra);
    }

    private DatacollectionConfig getDataCollectionConfig() {
        return JaxbUtils.unmarshal(DatacollectionConfig.class, new FileSystemResource(m_file));
	}
}
