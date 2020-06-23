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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.cxf.jaxrs.ext.search.AndSearchCondition;
import org.apache.cxf.jaxrs.ext.search.ConditionType;
import org.apache.cxf.jaxrs.ext.search.PrimitiveSearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
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

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class CriteriaBuilderSearchVisitorIT {

	@Autowired
	private ScanReportDao m_dao;

	@Test
	public void testScanReportAndCondition() {
		CriteriaBuilder builder = new ScanReportRestService().getCriteriaBuilder(null);
		CriteriaBuilderSearchVisitor<ScanReport,ScanReport> visitor = new CriteriaBuilderSearchVisitor<>(builder, ScanReport.class);

		// Simulates /opennms/api/v2/scanreports?_s=applications%3D%3DLocal+Access;timestamp%3Dle%3D2016-02-01T15:07:14.340-0500&limit=20&offset=0&order=desc&orderBy=timestamp
		
		List<SearchCondition<ScanReport>> conditions = new ArrayList<SearchCondition<ScanReport>>();
		conditions.add(new PrimitiveSearchCondition<ScanReport>("applications", "blah", String.class, ConditionType.EQUALS, new ScanReport()));
		conditions.add(new PrimitiveSearchCondition<ScanReport>("timestamp", new Date(), Date.class, ConditionType.LESS_OR_EQUALS, new ScanReport()));
		SearchCondition<ScanReport> andCondition = new AndSearchCondition<ScanReport>(conditions);

		visitor.visit(andCondition);

		Criteria criteria = visitor.getQuery().toCriteria();
		System.out.println(criteria.toString());
		m_dao.countMatching(criteria);
	}

	@Test
	public void testScanReportTwoConditions() {
		CriteriaBuilder builder = new ScanReportRestService().getCriteriaBuilder(null);
		CriteriaBuilderSearchVisitor<ScanReport,ScanReport> visitor = new CriteriaBuilderSearchVisitor<>(builder, ScanReport.class);

		visitor.visit(new PrimitiveSearchCondition<ScanReport>("applications", "blah", String.class, ConditionType.EQUALS, new ScanReport()));
		visitor.visit(new PrimitiveSearchCondition<ScanReport>("timestamp", new Date(), Date.class, ConditionType.LESS_OR_EQUALS, new ScanReport()));

		Criteria criteria = visitor.getQuery().toCriteria();
		System.out.println(criteria.toString());
		m_dao.countMatching(criteria);
	}


	@Test
	public void testScanReportTwoConditionsWithIsNull() {
		CriteriaBuilder builder = new ScanReportRestService().getCriteriaBuilder(null);
		CriteriaBuilderSearchVisitor<ScanReport,ScanReport> visitor = new CriteriaBuilderSearchVisitor<>(builder, ScanReport.class);

		visitor.visit(new PrimitiveSearchCondition<ScanReport>("applications", CriteriaBuilderSearchVisitor.NULL_VALUE, String.class, ConditionType.EQUALS, new ScanReport()));
		visitor.visit(new PrimitiveSearchCondition<ScanReport>("timestamp", new Date(), Date.class, ConditionType.LESS_OR_EQUALS, new ScanReport()));

		Criteria criteria = visitor.getQuery().toCriteria();
		System.out.println(criteria.toString());
		m_dao.countMatching(criteria);
	}

	@Test
	public void testScanReportTwoConditionsWithIsNotNull() {
		CriteriaBuilder builder = new ScanReportRestService().getCriteriaBuilder(null);
		CriteriaBuilderSearchVisitor<ScanReport,ScanReport> visitor = new CriteriaBuilderSearchVisitor<>(builder, ScanReport.class);

		visitor.visit(new PrimitiveSearchCondition<ScanReport>("applications", CriteriaBuilderSearchVisitor.NULL_VALUE, String.class, ConditionType.NOT_EQUALS, new ScanReport()));
		visitor.visit(new PrimitiveSearchCondition<ScanReport>("timestamp", new Date(), Date.class, ConditionType.LESS_OR_EQUALS, new ScanReport()));

		Criteria criteria = visitor.getQuery().toCriteria();
		System.out.println(criteria.toString());
		m_dao.countMatching(criteria);
	}
}
