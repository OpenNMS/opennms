package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfig;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class IfResourceDef extends ResourceDef {

    private TreeMap m_ifMap;

    public IfResourceDef(CollectionAgent agent, String collectionName) {
        super(agent, collectionName);
        m_ifMap = new TreeMap();
        addIfResources();

    }
    
    private Map getIfMap() {
        return m_ifMap;
    }

    void addIfInfo(IfInfo ifInfo) {
        getIfMap().put(new Integer(ifInfo.getIndex()), ifInfo);
    }

    void logInitializeSnmpIface(OnmsSnmpInterface snmpIface) {
        if (log().isDebugEnabled()) {
    		log().debug(
    				"initialize: snmpifindex = " + snmpIface.getIfIndex().intValue()
    				+ ", snmpifname = " + snmpIface.getIfName()
    				+ ", snmpifdescr = " + snmpIface.getIfDescr()
    				+ ", snmpphysaddr = -"+ snmpIface.getPhysAddr() + "-");
    		log().debug("initialize: ifLabel = '" + snmpIface.computeLabelForRRD() + "'");
    	}
    
    
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    void addSnmpInterface(OnmsSnmpInterface snmpIface) {
    	CollectionAgent collectionAgent = getAgent();
        String collectionName = getCollectionName();
        addIfInfo(new IfInfo(this, collectionAgent, collectionName, snmpIface));
    }

    void addIfResources() {
    	CollectionAgent agent = getAgent();
    	OnmsNode node = agent.getNode();
    
    	Set snmpIfs = node.getSnmpInterfaces();
    	
    	for (Iterator it = snmpIfs.iterator(); it.hasNext();) {
    		OnmsSnmpInterface snmpIface = (OnmsSnmpInterface) it.next();
    		logInitializeSnmpIface(snmpIface);
    		addSnmpInterface(snmpIface);
    		
    	}
    	
    }

    IfInfo getIfInfo(int ifIndex) {
        return (IfInfo) getIfMap().get(new Integer(ifIndex));
    }

    public Collection getIfInfos() {
        return getIfMap().values();
    }

    List getCombinedInterfaceAttributes() {
        Set attributes = new LinkedHashSet();
        for (Iterator it = getIfInfos().iterator(); it.hasNext();) {
            CollectionResource ifInfo = (CollectionResource) it.next();
            attributes.addAll(ifInfo.getAttributeList());
        }
        return new ArrayList(attributes);
    }

    public int getType() {
        return DataCollectionConfig.ALL_IF_ATTRIBUTES;
    }

}
