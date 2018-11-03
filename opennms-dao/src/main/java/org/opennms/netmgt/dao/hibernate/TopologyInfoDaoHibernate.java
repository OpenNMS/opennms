/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import java.util.List;

import org.opennms.netmgt.dao.api.TopologyInfoDao;
import org.opennms.netmgt.model.CdpLinkInfo;
import org.opennms.netmgt.model.VertexInfo;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class TopologyInfoDaoHibernate extends HibernateDaoSupport implements TopologyInfoDao {

    @Override
    public List<VertexInfo> getVertexInfos() {
        return (List<VertexInfo>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.model.VertexInfo(n.id, n.type, n.sysObjectId, n.label, n.location) from org.opennms.netmgt.model.OnmsNode n");
    }

    @Override
    public List<CdpLinkInfo> getCdpLinkInfo() {
        return (List<CdpLinkInfo>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.model.CdpLinkInfo(l.id, l.node.id, l.cdpCacheIfIndex, " +
                        "l.cdpInterfaceName, l.cdpCacheAddress, l.cdpCacheDeviceId, l.cdpCacheDevicePort) from org.opennms.netmgt.model.CdpLink l");
    }
}
