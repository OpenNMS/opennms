/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.notification;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.netmgt.alarmd.api.Northbounder;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.notification.parser.AlarmNorthbounderConfig;
import org.opennms.netmgt.notification.parser.AlarmNotificationConfigDao;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class AlarmNorthbounderManager implements InitializingBean,
		DisposableBean {

	@Autowired
	private ServiceRegistry m_serviceRegistry;

	@Autowired
	private AlarmNotificationConfigDao m_configDao;

	private Registration m_registration = null;

	@Autowired
	private NodeDao m_nodeDao;
	@Autowired
	private SnmpInterfaceDao m_snmpInterfaceDao;

	@Autowired
	private IpInterfaceDao m_IpInterfaceDao;
	
	@Autowired
	private AlarmNotificationConfigDao m_alarmNotificationConfigDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		AlarmNorthbounderConfig config = m_configDao.getConfig();

		AlarmNorthbounder northbounder = new AlarmNorthbounder(config);
		northbounder.setNodeDao(m_nodeDao);
		northbounder.setSnmpInterfaceDao(m_snmpInterfaceDao);
		northbounder.setIpInterfaceDao(m_IpInterfaceDao);
		northbounder.setAlarmNotificationConfigDao(m_alarmNotificationConfigDao);
		m_serviceRegistry.register(northbounder, Northbounder.class);

	}

	@Override
	public void destroy() throws Exception {
		m_registration.unregister();
	}

}
