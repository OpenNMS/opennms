/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
