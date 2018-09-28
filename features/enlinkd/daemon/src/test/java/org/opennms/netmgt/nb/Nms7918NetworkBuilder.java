/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.nb;


import org.opennms.netmgt.dao.api.IpNetToMediaDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode.NodeType;

public class Nms7918NetworkBuilder extends NmsNetworkBuilder {

	NodeDao m_nodeDao;
	IpNetToMediaDao m_ipNetToMediaDao;
	
    public void buildNetwork7918() {
        NetworkBuilder nb = getNetworkBuilder();

        nb.addNode(PE01_NAME).setForeignSource("linkd").setForeignId(PE01_NAME).setSysObjectId(PE01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(PE01_IP).setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode(ASW01_NAME).setForeignSource("linkd").setForeignId(ASW01_NAME).setSysObjectId(ASW01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(ASW01_IP).setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();
        
        nb.addNode(OSPESS01_NAME).setForeignSource("linkd").setForeignId(OSPESS01_NAME).setSysObjectId(OSPESS01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(OSPESS01_IP).setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();
        
        nb.addNode(OSPWL01_NAME).setForeignSource("linkd").setForeignId(OSPWL01_NAME).setSysObjectId(OSPWL01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(OSPWL01_IP).setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();

        nb.addNode(SAMASW01_NAME).setForeignSource("linkd").setForeignId(SAMASW01_NAME).setSysObjectId(SAMASW01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(SAMASW01_IP).setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();

        nb.addNode(STCASW01_NAME).setForeignSource("linkd").setForeignId(STCASW01_NAME).setSysObjectId(STCASW01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(STCASW01_IP).setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();
    }    

    
	public NodeDao getNodeDao() {
		return m_nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		m_nodeDao = nodeDao;
	}
	
       public IpNetToMediaDao getIpNetToMediaDao() {
	           return m_ipNetToMediaDao;
	       }

	       public void setIpNetToMediaDao(IpNetToMediaDao ipNetToMediaDao) {
	           m_ipNetToMediaDao = ipNetToMediaDao;
	       }

	
}
