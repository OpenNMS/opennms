/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import java.text.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.MinionPropertyDao;
import org.opennms.netmgt.model.OnmsMinionCollection;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.model.minion.OnmsMinionProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("minions")
public class MinionRestService extends OnmsRestService {
    @Autowired
    private MinionDao m_minionDao;

    @Autowired
    private MinionPropertyDao m_minionPropertyDao;

    @Context
    UriInfo m_uriInfo;

    @Context
    HttpHeaders m_headers;

    @Context
    SecurityContext m_securityContext;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{minionId}")
    @Transactional
    public OnmsMinion getMinion(@PathParam("minionId") final String minionId) {
        readLock();
        try {
            return m_minionDao.get(minionId);
        } finally {
            readUnlock();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{minionId}/{key}")
    @Transactional
    public OnmsMinionProperty getMinionProperty(@PathParam("minionId") final String minionId, @PathParam("key") final String key) {
        readLock();
        try {
            return m_minionPropertyDao.findByKey(minionId, key);
        } finally {
            readUnlock();
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount() {
        readLock();
        try {
            return Integer.toString(m_minionDao.countAll());
        } finally {
            readUnlock();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public OnmsMinionCollection getMinions() throws ParseException {
        readLock();

        try {
            final CriteriaBuilder builder = getCriteriaBuilder(m_uriInfo.getQueryParameters());
            final OnmsMinionCollection coll = new OnmsMinionCollection(m_minionDao.findMatching(builder.toCriteria()));
            coll.setTotalCount(m_minionDao.countMatching(builder.clearOrder().toCriteria()));

            return coll;
        } finally {
            readUnlock();
        }
    }

    private CriteriaBuilder getCriteriaBuilder(final MultivaluedMap<String, String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMinion.class);
        builder.alias("minionProperties", "property", JoinType.LEFT_JOIN);
        builder.alias("properties", "property", JoinType.LEFT_JOIN);
        applyQueryFilters(params, builder);
        return builder;
    }

}
