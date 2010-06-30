//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.web.rest;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.dao.OnmsMapElementDao;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.OnmsMapElementList;
import org.opennms.netmgt.model.OnmsMap;
import com.sun.jersey.spi.resource.PerRequest;
import com.sun.jersey.api.core.ResourceContext;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;

@Component
/**
 * <p>OnmsMapElementResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@PerRequest
@Scope("prototype")
@Transactional
public class OnmsMapElementResource extends OnmsRestService {
    @Autowired
    private OnmsMapDao m_mapDao;

    @Autowired
    private OnmsMapElementDao m_mapElementDao;

    @Autowired
    private EventProxy m_eventProxy;

    @Context
    ResourceContext m_context;

    /**
     * <p>getMapElements</p>
     *
     * @param mapId a int.
     * @return a {@link org.opennms.netmgt.model.OnmsMapElementList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsMapElementList getMapElements(@PathParam("mapId") int mapId) {
        log().debug("getMapElements: reading elements for map " + mapId);
        OnmsMap map = m_mapDao.get(mapId);
        if (map == null)
            throwException(Response.Status.BAD_REQUEST, "getMapElements: can't find map " + mapId);
        return new OnmsMapElementList(map.getMapElements());
    }
}
