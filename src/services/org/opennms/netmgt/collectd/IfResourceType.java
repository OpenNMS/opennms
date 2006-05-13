package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opennms.netmgt.config.DataCollectionConfig;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.SnmpInstId;

public class IfResourceType extends DbResourceType {

    private TreeMap m_ifMap;

    public IfResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        super(agent, snmpCollection);
        m_ifMap = new TreeMap();
        addKnownIfResources();

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

    void addSnmpInterface(OnmsSnmpInterface snmpIface) {
        addIfInfo(new IfInfo(this, getAgent(), snmpIface));
    }

    void addKnownIfResources() {
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
            attributes.addAll(ifInfo.getAttributeTypes());
        }
        return new ArrayList(attributes);
    }

    public int getType() {
        return DataCollectionConfig.ALL_IF_ATTRIBUTES;
    }

    public CollectionResource findResource(SnmpInstId inst) {
        return getIfInfo(inst.toInt());
    }

    protected Collection identityAttributeTypes() {
        MibObject ifAliasMibObject = new MibObject();
        ifAliasMibObject.setOid(".1.3.6.1.2.1.31.1.1.1.18");
        ifAliasMibObject.setAlias("ifAlias");
        ifAliasMibObject.setType("DisplayString");
        ifAliasMibObject.setInstance("ifIndex");

        AttributeType type = new AttributeType(this, getCollectionName(), ifAliasMibObject);
        return Collections.singleton(type);
    }

    public Collection getResources() {
        return m_ifMap.values();
    }

}
