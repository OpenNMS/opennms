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
package org.opennms.netmgt.dao.castor.collector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.common.Rrd;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.Groups;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.config.datacollection.SystemDefChoice;
import org.opennms.netmgt.config.datacollection.Systems;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.springframework.core.io.FileSystemResource;

public class DataCollectionConfigFile {
	
	File m_file;
	
	public DataCollectionConfigFile(File file) {
		m_file = file;
	}
	
	public void visit(DataCollectionVisitor visitor) {
        DatacollectionConfig dataCollectionConfig = getDataCollectionConfig();
        visitor.visitDataCollectionConfig(dataCollectionConfig);
        
        for (Iterator<SnmpCollection> it = dataCollectionConfig.getSnmpCollectionCollection().iterator(); it.hasNext();) {
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
        
        for (Iterator<Group> it = groups.getGroupCollection().iterator(); it.hasNext();) {
            Group group = it.next();
            doVisit(group, visitor);
        }
        
    }

    private void doVisit(Group group, DataCollectionVisitor visitor) {
        visitor.visitGroup(group);
        
        // mibObj
        for (Iterator<MibObj> it = group.getMibObjCollection().iterator(); it.hasNext();) {
            MibObj mibObj = it.next();
            doVisit(mibObj, visitor);
        }
        
        // subGroups
        for (Iterator<String> it = group.getIncludeGroupCollection().iterator(); it.hasNext();) {
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
        for (Iterator<SystemDef> it = systems.getSystemDefCollection().iterator(); it.hasNext();) {
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
        for (Iterator<String> it = collect.getIncludeGroupCollection().iterator(); it.hasNext();) {
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
        for (Iterator<String> it = rrd.getRraCollection().iterator(); it.hasNext();) {
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
		try {
			return CastorUtils.unmarshal(DatacollectionConfig.class, new FileSystemResource(m_file));
		} catch (MarshalException e) {
			throw runtimeException("Syntax error in "+m_file, e);
		} catch (ValidationException e) {
			throw runtimeException("invalid attribute in "+m_file, e);
        } catch (FileNotFoundException e) {
            throw runtimeException("Unable to find file "+m_file, e);
        } catch (IOException e) {
            throw runtimeException("Unable to access file "+m_file, e);
		}
	}

	private RuntimeException runtimeException(String msg, Exception e) {
		log().error(msg, e);
		return new RuntimeException(msg, e);
	}

	private ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}
}
