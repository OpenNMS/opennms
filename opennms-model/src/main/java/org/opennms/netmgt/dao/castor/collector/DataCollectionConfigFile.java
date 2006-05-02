package org.opennms.netmgt.dao.castor.collector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
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

public class DataCollectionConfigFile {
	
	File m_file;
	
	public DataCollectionConfigFile(File file) {
		m_file = file;
	}
	
	public void visit(DataCollectionVisitor visitor) {
        DatacollectionConfig dataCollectionConfig = getDataCollectionConfig();
        visitor.visitDataCollectionConfig(dataCollectionConfig);
        
        for (Iterator it = dataCollectionConfig.getSnmpCollectionCollection().iterator(); it.hasNext();) {
            SnmpCollection snmpCollection = (SnmpCollection) it.next();
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
        
        for (Iterator it = groups.getGroupCollection().iterator(); it.hasNext();) {
            Group group = (Group) it.next();
            doVisit(group, visitor);
        }
        
    }

    private void doVisit(Group group, DataCollectionVisitor visitor) {
        visitor.visitGroup(group);
        
        // mibObj
        for (Iterator it = group.getMibObjCollection().iterator(); it.hasNext();) {
            MibObj mibObj = (MibObj) it.next();
            doVisit(mibObj, visitor);
        }
        
        // subGroups
        for (Iterator it = group.getIncludeGroupCollection().iterator(); it.hasNext();) {
            String subGroup = (String) it.next();
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
        for (Iterator it = systems.getSystemDefCollection().iterator(); it.hasNext();) {
            SystemDef systemDef = (SystemDef) it.next();
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
        for (Iterator it = collect.getIncludeGroupCollection().iterator(); it.hasNext();) {
            String includeGroup = (String) it.next();
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
        for (Iterator it = rrd.getRraCollection().iterator(); it.hasNext();) {
            String rra = (String) it.next();
            doVisitRra(rra, visitor);
        }
        visitor.completeRrd(rrd);
    }

    private void doVisitRra(String rra, DataCollectionVisitor visitor) {
        visitor.visitRra(rra);
        visitor.completeRra(rra);
    }

    private DatacollectionConfig getDataCollectionConfig() {
		FileReader in = null;
		try {
			in = new FileReader(m_file);
			return (DatacollectionConfig) Unmarshaller.unmarshal(DatacollectionConfig.class, in);
		} catch (MarshalException e) {
			throw runtimeException("Syntax error in "+m_file, e);
		} catch (ValidationException e) {
			throw runtimeException("invalid attribute in "+m_file, e);
		} catch (FileNotFoundException e) {
			throw runtimeException("Unable to find file "+m_file, e);
		} finally {
			closeQuietly(in);
		}
	}

	private RuntimeException runtimeException(String msg, Exception e) {
		log().error(msg, e);
		return new RuntimeException(msg, e);
	}

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	private void closeQuietly(FileReader in) {
		try {
			if (in != null) in.close();
		} catch (IOException e) {
		}		
	}

}
