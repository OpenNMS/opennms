package org.opennms.netmgt.linkd.nb;

import org.opennms.netmgt.model.NetworkBuilder;

public class Nms101NetworkBuilder extends LinkdNetworkBuilder {

	@SuppressWarnings("deprecation")
	public void buildNetwork101() {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode("test.example.com").setForeignSource("linkd").setForeignId("1").setSysObjectId(".1.3.6.1.4.1.1724.81").setType("A");
        nb.addInterface("192.168.1.10").setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("laptop").setForeignSource("linkd").setForeignId("laptop").setSysObjectId(".1.3.6.1.4.1.8072.3.2.255").setType("A");
        nb.addInterface("10.1.1.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(10).setIfType(6).setCollectionEnabled(true).setIfSpeed(1000000000).setPhysAddr("065568ae696c");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco7200a").setForeignSource("linkd").setForeignId("cisco7200a").setSysObjectId(".1.3.6.1.4.1.9.1.222").setType("A");
        nb.addInterface("10.1.1.1").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(3).setIfType(6).setCollectionEnabled(true).setIfSpeed(1000000000).setPhysAddr("ca0497a80038");
        nb.addInterface("10.1.2.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("ca0497a8001c");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco7200b").setForeignSource("linkd").setForeignId("cisco7200b").setSysObjectId(".1.3.6.1.4.1.9.1.222").setType("A");
        nb.addInterface("10.1.2.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(4).setIfType(6).setCollectionEnabled(true).setIfSpeed(10000000).setPhysAddr("ca0597a80038");
        nb.addInterface("10.1.3.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("ca0597a8001c");
        nb.addInterface("10.1.4.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(1).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("ca0597a80000");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco3700").setForeignSource("linkd").setForeignId("cisco3700").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType("A");
        nb.addInterface("10.1.3.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(10000000).setPhysAddr("c20197a50000");
        nb.addInterface("10.1.6.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(3).setIfType(6).setCollectionEnabled(false).setIfSpeed(1000000000).setPhysAddr("c20197a50001");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco2691").setForeignSource("linkd").setForeignId("cisco2691").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType("A");
        nb.addInterface("10.1.4.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(4).setIfType(6).setCollectionEnabled(false).setIfSpeed(10000000).setPhysAddr("c00397a70001");
        nb.addInterface("10.1.5.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("c00397a70000");
        nb.addInterface("10.1.7.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(1).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("c00397a70010");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco1700").setForeignSource("linkd").setForeignId("cisco1700").setSysObjectId(".1.3.6.1.4.1.9.1.200").setType("A");
        nb.addInterface("10.1.5.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("d00297a60000");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco1700b").setForeignSource("linkd").setForeignId("cisco1700b").setSysObjectId(".1.3.6.1.4.1.9.1.200").setType("A");
        nb.addInterface("10.1.5.1").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c00397a70000");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco3600").setForeignSource("linkd").setForeignId("cisco3600").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType("A");
        nb.addInterface("10.1.6.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("cc0097a30000");
        nb.addInterface("10.1.7.2").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("cc0097a30010");
        m_nodeDao.save(nb.getCurrentNode());

        m_nodeDao.flush();

	}
}
