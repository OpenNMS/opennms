package org.opennms.netmgt.nb;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode.NodeType;

public class Nms4930NetworkBuilder extends TestNetworkBuilder {

	NodeDao m_nodeDao;

	@SuppressWarnings("deprecation")
	public void buildNetwork4930() {
        NetworkBuilder nb = getNetworkBuilder();
        
        nb.addNode(DLINK1_NAME).setForeignSource("linkd").setForeignId(DLINK1_NAME).setSysObjectId(".1.3.6.1.4.1.9.1.122").setType(NodeType.ACTIVE);
        nb.addInterface(DLINK1_IP).setIsSnmpPrimary("P").setIsManaged("M")
        .addSnmpInterface(3).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90010");
        nb.addInterface("10.1.2.1").setIsSnmpPrimary("S").setIsManaged("M")
        .addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90000");
        nb.addInterface("10.1.3.1").setIsSnmpPrimary("S").setIsManaged("M")
        .addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90001");
        nb.addSnmpInterface(24).setIfType(6).setIfName("Fa0/24").setIfSpeed(100000000);
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode(DLINK2_NAME).setForeignSource("linkd").setForeignId(DLINK2_NAME).setSysObjectId(".1.3.6.1.4.1.9.1.122").setType(NodeType.ACTIVE);
        nb.addInterface(DLINK2_IP).setIsSnmpPrimary("P").setIsManaged("M")
        .addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2017db90000");
        nb.addInterface("10.1.5.1").setIsSnmpPrimary("S").setIsManaged("M")
        .addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2017db90001");
        nb.addSnmpInterface(10).setIfType(6).setIfName("FastEthernet0/10").setIfSpeed(100000000);
        m_nodeDao.save(nb.getCurrentNode());

        m_nodeDao.flush();

	}

	public NodeDao getNodeDao() {
		return m_nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		m_nodeDao = nodeDao;
	}
}
