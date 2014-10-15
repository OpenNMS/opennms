/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import java.sql.SQLException;

import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class EventUtilDaoImpl extends AbstractEventUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventUtilDaoImpl.class);
	
	@Autowired
	private NodeDao nodeDao;
	
	@Autowired
	private SnmpInterfaceDao snmpInterfaceDao;
	
	@Autowired
	private AssetRecordDao assetRecordDao;
	
	@Autowired
	private IpInterfaceDao ipInterfaceDao;
	
    @Override
    public String getNodeLabel(long nodeId) throws SQLException {
        return nodeDao.getLabelForId(Integer.valueOf((int)nodeId));
    }

    @Override
    public String getIfAlias(long nodeId, String ipaddr) throws SQLException {
        return ipInterfaceDao.findByNodeIdAndIpAddress((int)nodeId, ipaddr).getSnmpInterface().getIfAlias();
    }

    @Override
    public String getAssetFieldValue(String parm, long nodeId) {
        return assetRecordDao.findByNodeId((int)nodeId).getNode().getLabel();
    }

    @Override
    public String getHardwareFieldValue(String parm, long nodeId) {
	return "Unknown-Not-Implemented-Yet"; // FIXME
    }
}
