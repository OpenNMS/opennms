/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.model.HwEntityAttributeType;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsHwEntityAlias;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class HardwareInventoryResourceIT extends AbstractSpringJerseyRestTestCase {

	@Autowired
	private DatabasePopulator m_databasePopulator;

	/** The hardware entity DAO. */
	@Autowired
	HwEntityDao m_hwEntityDao;

	/** The hardware entity attribute type DAO. */
	@Autowired
	HwEntityAttributeTypeDao m_hwEntityAttributeTypeDao;


	@Override
	protected void afterServletStart() {
		MockLogAppender.setupLogging(true, "DEBUG");
		m_databasePopulator.populateDatabase();
	}

	@Test
	@JUnitTemporaryDatabase
	@Transactional
	public void testHwEntityWithInvalidOid() throws Exception {
		setHwEntitiesData();
		String postData = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
				"<hwEntity entPhysicalIndex=\"0\" nodeId=\"5\" entityId=\"197\"> \n" +
				"<children/>\n" +
				"<entPhysicalClass>unknown</entPhysicalClass>\n" +
				"<vendorAttributes>\n" +
				"<hwEntityAttribute class=\"string\" name=\"InitLoadParmSec\" oid=\".1.3.6.1.2.1.25.1.4\" " +
				"value=\"BOOT_IMAGE=/vmlinuz-3.10.0-1160.31.1.el7.x86_64 root=UUID=9707334d-8bf3-4fb0-a579-6a8bf375ab4e ro " +
				"console=tty1 console=ttyS0,115\"/>\n" +
				"<hwEntityAttribute class=\"string\" name=\"TotalMemSec\" oid=\".1.3.6.1.2.1.25.2.2\" value=\"100\"/>\n" +
				"<hwEntityAttribute class=\"string\" name=\"reyrty\" oid=\"jygg\" value=\"100\"/>\n" +
				"</vendorAttributes>\n" +
				"</hwEntity>";
		MockHttpServletResponse response = sendPost("nodes/1/hardwareInventory", postData, 400);
		assertEquals(response.getContentAsString(),"OID {jygg} provided in entity is not valid.");
	}

	@Test
	@JUnitTemporaryDatabase
	@Transactional
	public void testHwEntityWithValidOid() throws Exception {
		setHwEntitiesData();
		String postData = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
				"<hwEntity entPhysicalIndex=\"0\" nodeId=\"5\" entityId=\"197\"> \n" +
				"<children/>\n" +
				"<entPhysicalClass>unknown</entPhysicalClass>\n" +
				"<vendorAttributes>\n" +
				"<hwEntityAttribute class=\"string\" name=\"InitLoadParmSec\" oid=\".1.3.6.1.2.1.25.1.4\" " +
				"value=\"BOOT_IMAGE=/vmlinuz-3.10.0-1160.31.1.el7.x86_64 root=UUID=9707334d-8bf3-4fb0-a579-6a8bf375ab4e ro " +
				"console=tty1 console=ttyS0,115\"/>\n" +
				"<hwEntityAttribute class=\"string\" name=\"TotalMemSec\" oid=\".1.3.6.1.2.1.25.2.2\" value=\"100\"/>\n" +
				"<hwEntityAttribute class=\"string\" name=\"retry\" oid=\".1.3.6.1.2.1.25.2.5\" value=\"100\"/>\n" +
				"</vendorAttributes>\n" +
				"</hwEntity>";
		MockHttpServletResponse response = sendPost("nodes/1/hardwareInventory", postData, 204);
		System.err.println("response = " + stringifyResponse(response));
		HwEntityAttributeType t = m_hwEntityAttributeTypeDao.findTypeByName("retry");
		assertEquals(".1.3.6.1.2.1.25.2.5",t.getOid());
	}

	private void setHwEntitiesData(){
		HwEntityAttributeType ram = new HwEntityAttributeType(".1.3.6.1.4.1.9.9.195.1.1.1.1", "ram", "integer");
		m_hwEntityAttributeTypeDao.save(ram);
		m_hwEntityAttributeTypeDao.flush();

		OnmsNode node = m_databasePopulator.getNode1();
		Assert.assertNotNull(node);
		Assert.assertNotNull(node.getId());

		OnmsHwEntity root = new OnmsHwEntity();
		root.setEntPhysicalIndex(1);
		root.setEntPhysicalClass("chassis");
		root.setEntPhysicalName("Chassis");

		OnmsHwEntity m1 = new OnmsHwEntity();
		m1.setEntPhysicalIndex(2);
		m1.setEntPhysicalClass("module");
		m1.setEntPhysicalName("M1");
		m1.addAttribute(ram, "4");

		OnmsHwEntity m2 = new OnmsHwEntity();
		m2.setEntPhysicalIndex(3);
		m2.setEntPhysicalClass("module");
		m2.setEntPhysicalName("M2");
		m2.addAttribute(ram, "2");
		OnmsHwEntityAlias onmsHwEntityAlias = new OnmsHwEntityAlias(1, "0.1.12.3.4");
		onmsHwEntityAlias.setHwEntity(m2);
		m2.addEntAliases(new TreeSet<>(Arrays.asList(onmsHwEntityAlias)));

		root.addChildEntity(m1);
		root.addChildEntity(m2);
		Assert.assertNotNull(m1.getParent());
		Assert.assertNotNull(m2.getParent());

		root.setNode(node);
		Assert.assertNotNull(root.getNode());
		Assert.assertEquals(2, root.getChildren().size());

		// Saving root entity
		m_hwEntityDao.saveOrUpdate(root);
		m_hwEntityDao.flush();
	}
}