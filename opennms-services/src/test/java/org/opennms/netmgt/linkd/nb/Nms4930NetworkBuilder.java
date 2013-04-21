package org.opennms.netmgt.linkd.nb;

import org.opennms.netmgt.model.NetworkBuilder;

public class Nms4930NetworkBuilder extends LinkdNetworkBuilder {

	@SuppressWarnings("deprecation")
	public void buildNetwork4005() {
        NetworkBuilder nb = getNetworkBuilder();
        
        nb.addNode("cisco1").setForeignSource("linkd").setForeignId("cisco1").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType("A");
        nb.addInterface("10.1.1.2").setIsSnmpPrimary("P").setIsManaged("M")
        .addSnmpInterface(3).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90010");
        nb.addInterface("10.1.2.1").setIsSnmpPrimary("S").setIsManaged("M")
        .addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90000");
        nb.addInterface("10.1.3.1").setIsSnmpPrimary("S").setIsManaged("M")
        .addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90001");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco2").setForeignSource("linkd").setForeignId("cisco2").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType("A");
        nb.addInterface("10.1.2.2").setIsSnmpPrimary("P").setIsManaged("M")
        .addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2017db90000");
        nb.addInterface("10.1.5.1").setIsSnmpPrimary("S").setIsManaged("M")
        .addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2017db90001");
        m_nodeDao.save(nb.getCurrentNode());

        m_nodeDao.flush();

	}
}
