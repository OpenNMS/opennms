/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.reporting.availability;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-availabilityDatabasePopulator.xml",
        "classpath:META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AvailabilityDatabasePopulatorTest implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(AvailabilityDatabasePopulatorTest.class);

	@Autowired
	AvailabilityDatabasePopulator m_dbPopulator;

	@Autowired
	NodeDao m_nodeDao;

	@Autowired
	ServiceTypeDao m_serviceTypeDao;

	@Autowired
	IpInterfaceDao m_ipInterfaceDao;

	@Autowired
	OutageDao m_outageDao;

	@Autowired
	JdbcTemplate m_template;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	@Before
	public void setUp() throws Exception {
		m_dbPopulator.populateDatabase();
	}

	/**
	 * @throws Exception
	 */
	@Test
	@Transactional
	public void testAvailabilityDatabase() throws Exception {

		List<OnmsNode> nodes = m_nodeDao.findAll();
		for (OnmsNode node : nodes) {
			m_nodeDao.initialize(node);
			m_nodeDao.initialize(node.getDistPoller());
		}
		for (OnmsNode node : nodes) {
			System.err.println("NODE "+ node.toString());
		}
		List<OnmsIpInterface> ifs = m_ipInterfaceDao.findAll();
		for (OnmsIpInterface iface : ifs) {
			System.err.println("INTERFACE "+ iface.toString());
		}
		Assert.assertEquals("node DB count", 2, m_nodeDao.countAll());
		Assert.assertEquals("service DB count", 3, m_serviceTypeDao.countAll());
		Assert.assertEquals("IP interface DB count", 3, m_ipInterfaceDao.countAll());
		Assert.assertEquals("outages DB Count",6 ,m_outageDao.countAll());

		final OnmsIpInterface oneHundredDotOne = m_ipInterfaceDao.findByNodeIdAndIpAddress(1, "192.168.100.1");

		try {
			List<OnmsMonitoredService> stmt = m_template.query(
					"SELECT ifServices.serviceid, service.servicename FROM ifServices, ipInterface, node, " + "service WHERE ((ifServices.nodeid = 1 )" + 
					"AND (ifServices.ipaddr = '192.168.100.1') AND ipinterface.ipaddr = '192.168.100.1' AND ipinterface.isManaged ='M' AND " + 
					"(ifServices.serviceid = service.serviceid) AND (ifservices.status = 'A')) AND node.nodeid = 1 AND node.nodetype = 'A'", 
					new RowMapper<OnmsMonitoredService>() {
                                                @Override
						public OnmsMonitoredService mapRow(ResultSet rs, int rowNum) throws SQLException {
							OnmsMonitoredService retval = new OnmsMonitoredService(oneHundredDotOne, m_serviceTypeDao.findByName(rs.getString("servicename")));
							return retval;
						}
					}
			);
			// ResultSet srs = stmt.executeQuery("SELECT ipInterface.ipaddr, ipInterface.nodeid FROM ipInterface WHERE ipInterface.ipaddr = '192.168.100.1'" );
			Assert.assertTrue("interface results for 192.168.100.2", stmt.size() > 0);
			Assert.assertEquals(new Integer(1) ,stmt.get(0).getServiceId());
		} catch (Exception e) {
			LOG.error("unable to execute SQL", e);
			throw e;
		}

		/*
		Assert.assertEquals("node DB count", 2, m_db.countRows("select * from node"));
		Assert.assertEquals("service DB count", 3,
				m_db.countRows("select * from service"));
		Assert.assertEquals("ipinterface DB count", 3,
				m_db.countRows("select * from ipinterface"));
		Assert.assertEquals("interface services DB count", 3,
				m_db.countRows("select * from ifservices"));
		// Assert.assertEquals("outages DB count", 3, m_db.countRows("select * from
		// outages"));
		Assert.assertEquals(
				"ip interface DB count where ipaddr = 192.168.100.1",
				1,
				m_db.countRows("select * from ipinterface where ipaddr = '192.168.100.1'"));
		Assert.assertEquals(
				"number of interfaces returned from IPLIKE",
				3,
				m_db.countRows("select * from ipinterface where iplike(ipaddr,'192.168.100.*')"));
		 */
	}
}
