/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import java.util.Collection;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.ScanReportDao;
import org.opennms.netmgt.model.ScanReport;
import org.opennms.web.rest.v1.support.ScanReportList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link ScanReport} entity
 *
 * @author Seth
 */
@Component
@Path("scanreports")
@Transactional
public class ScanReportRestService extends AbstractDaoRestService<ScanReport,String> {

	private static final Logger LOG = LoggerFactory.getLogger(ScanReportRestService.class);

	private static final String PROPERY_APPLICATIONS = "applications";

	/**
	 * We need to override certain CriteriaBuilder methods so that we can support
	 * filtering on values in the scanreportproperties table which is mapped as an
	 * {@link ElementCollection} {@link Map}.
	 * 
	 * @see https://hibernate.atlassian.net/browse/HHH-869
	 * @see https://hibernate.atlassian.net/browse/HHH-6103
	 */
	private static class PropertiesHandlerCriteriaBuilder extends CriteriaBuilder {

		public PropertiesHandlerCriteriaBuilder() {
			super(ScanReport.class);
		}

		@Override
		public CriteriaBuilder eq(final String attribute, final Object comparator) {
			if (PROPERY_APPLICATIONS.equalsIgnoreCase(attribute)) {
				// TODO: Escape SQL content in values
				sql(String.format("{alias}.id in (select scanreportid from scanreportproperties where property = '%s' and propertyvalue = '%s')", PROPERY_APPLICATIONS, comparator));
				return this;
			}
			return super.eq(attribute, comparator);
		}

		@Override
		public CriteriaBuilder ne(final String attribute, final Object comparator) {
			if (PROPERY_APPLICATIONS.equalsIgnoreCase(attribute)) {
				// TODO: Escape SQL content in values
				sql(String.format("{alias}.id in (select scanreportid from scanreportproperties where property = '%s' and propertyvalue != '%s')", PROPERY_APPLICATIONS, comparator));
				return this;
			}
			return super.ne(attribute, comparator);
		}
	}

	@Autowired
	private ScanReportDao m_dao;

	@Override
	protected ScanReportDao getDao() {
		return m_dao;
	}

	@Override
	protected Class<ScanReport> getDaoClass() {
		return ScanReport.class;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		final CriteriaBuilder builder = new PropertiesHandlerCriteriaBuilder();

		// Order by date (descending) by default
		builder.orderBy("timestamp").desc();

		return builder;
	}

	@Override
	protected JaxbListWrapper<ScanReport> createListWrapper(Collection<ScanReport> list) {
		return new ScanReportList(list);
	}

	@GET
	@Path("{id}/logs")
	@Produces({MediaType.TEXT_PLAIN})
	public Response getLogs(@PathParam("id") final String id) {
		final ScanReport report = getDao().get(id);
		if (report == null) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			if (report.getLog() == null || report.getLog().getLogText() == null) {
				return Response.status(Status.NO_CONTENT).build();
			} else {
				return Response.ok(report.getLog().getLogText()).build();
			}
		}
	}
}
