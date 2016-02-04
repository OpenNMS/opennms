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


package org.opennms.web.rest.support;

import static org.junit.Assert.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cxf.jaxrs.ext.search.ConditionType;
import org.apache.cxf.jaxrs.ext.search.PrimitiveSearchCondition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.ScanReportDao;
import org.opennms.netmgt.model.ScanReport;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.ScanReportRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


/*
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:META-INF/opennms/applicationContext-soa.xml",
        "classpath:META-INF/opennms/applicationContext-mockDao.xml"
})
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:META-INF/opennms/applicationContext-soa.xml",
		"classpath:META-INF/opennms/applicationContext-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class CriteriaBuilderSearchVisitorTest {

	@Autowired
	private ScanReportDao m_dao;

	@Test
	public void testScanReportPropertySearch() {
		CriteriaBuilder builder = new ScanReportRestService().getCriteriaBuilder();
		CriteriaBuilderSearchVisitor<ScanReport> visitor = new CriteriaBuilderSearchVisitor<ScanReport>(builder, ScanReport.class);

		visitor.visit(new PrimitiveSearchCondition<ScanReport>("applications", "blah", String.class, ConditionType.EQUALS, new ScanReport()));
		//builder.sql("{alias}.id in (select scanreportid from scanreportproperties where property = 'applications' and propertyvalue = 'hello')");

		Criteria criteria = visitor.getQuery().toCriteria();
		System.out.println(criteria.toString());
		m_dao.countMatching(criteria);
	}
}
