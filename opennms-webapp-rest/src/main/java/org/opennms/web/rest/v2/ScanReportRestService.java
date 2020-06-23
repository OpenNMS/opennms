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
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.ScanReportDao;
import org.opennms.netmgt.model.ScanReport;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.v1.support.ScanReportList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link ScanReport} entity
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Component
@Path("scanreports")
@Transactional
public class ScanReportRestService extends AbstractDaoRestService<ScanReport,ScanReport,String,String> {

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
    protected Class<ScanReport> getQueryBeanClass() {
        return ScanReport.class;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(ScanReport.class);

        // Order by date (descending) by default
        builder.orderBy("timestamp").desc();

        return builder;
    }

    @Override
    protected JaxbListWrapper<ScanReport> createListWrapper(Collection<ScanReport> list) {
        return new ScanReportList(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.SCAN_REPORT_SERVICE_PROPERTIES;
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

    @Override
    protected ScanReport doGet(UriInfo uriInfo, String id) {
        return getDao().get(id);
    }

    @Override
    protected Response doUpdate(final SecurityContext securityContext, final UriInfo uriInfo, final String key, ScanReport targetObject) {
        if (!key.equals(targetObject.getId())) {
            throw getException(Status.BAD_REQUEST, "The ID of the object doesn't match the ID of the path: {} != {}", targetObject.getId(), key);
        }
        getDao().saveOrUpdate(targetObject);
        return Response.noContent().build();
    }

}
