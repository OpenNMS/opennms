/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
				"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
				"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
				"classpath:/META-INF/opennms/applicationContext-soa.xml",
				"classpath:/META-INF/opennms/applicationContext-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
@Transactional
public class CategorySearchProviderIT {

	@Before
	public void setUp() {

	}
	@Test
	public void verify() {

	}
}

//+
//		+	@Autowired
//+	private NodeDao nodeDao;
//+	@Autowired
//+	private EventDao eventDao;
//+	@Autowired
//+	private OutageDao outageDao;
//+	@Autowired
//+	private GenericPersistenceAccessor genericPersistenceAccessor;
//+	@Autowired
//+	private MonitoringLocationDao locationDao;
//+	@Autowired
//+	private DistPollerDao distPollerDao;
//+	@Autowired
//+	private MonitoredServiceDao monitoredServiceDao;
//+	@Autowired
//+	private IpInterfaceDao interfaceDao;
//+	@Autowired
//+	private ServiceTypeDao serviceTypeDao;