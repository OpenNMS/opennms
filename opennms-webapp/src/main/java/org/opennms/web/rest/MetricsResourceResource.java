/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;
import java.util.List;
import javax.persistence.Entity;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.web.svclayer.GraphResultsService;
import org.opennms.web.svclayer.RrdGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@PerRequest
@Scope("prototype")
@Path("resource")
@Transactional
public class MetricsResourceResource extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsResourceResource.class);

    @Context
    UriInfo m_uriInfo;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ResourceDao m_resourceDao;

    @Autowired
    private EventProxy m_eventProxy;

    @Context
    ResourceContext m_context;
    
    /**
     * <p>getAssetRecord</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAssetRecord} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MetricsResource getMetricsResource(@PathParam("nodeCriteria") String nodeCriteria) {
        readLock();
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "getCategories: Can't find node " + nodeCriteria);
            }
            return getMetricsResource(node);
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>updateAssetRecord</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateAssetRecord(@PathParam("nodeCriteria") String nodeCriteria,  MultivaluedMapImpl params) {
        writeLock();

        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "updateAssetRecord: Can't find node " + nodeCriteria);
            }

/*
            MetricsResource assetRecord = getMetricsResource(node);
            if (assetRecord == null) {
                throw getException(Status.BAD_REQUEST, "updateAssetRecord: Node " + node  + " could not update ");
            }
            LOG.debug("updateAssetRecord: updating category {}", assetRecord);
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(assetRecord);
            for(String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    String stringValue = params.getFirst(key);
                    Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }

            LOG.debug("updateAssetRecord: assetRecord {} updated", assetRecord);
*/
            m_nodeDao.saveOrUpdate(node);

            try {
                sendEvent(EventConstants.ASSET_INFO_CHANGED_EVENT_UEI, node.getId());
            } catch (EventProxyException e) {
                throw getException(Status.BAD_REQUEST, e.getMessage());
            }

            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } finally {
            writeUnlock();
        }
    }

    private MetricsResource getMetricsResource(OnmsNode node) {
        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        LOG.debug("resource: {}", resource);
        List<OnmsResource> resources = resource.getChildResources();
        LOG.debug("resources: {}", resources);

        return null;
    }


    private void sendEvent(String uei, int nodeId) throws EventProxyException {
        EventBuilder bldr = new EventBuilder(uei, getClass().getName());
        bldr.setNodeid(nodeId);
        m_eventProxy.send(bldr.getEvent());
    }

    @Entity
    @XmlRootElement(name = "metricsResource")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class MetricsResource {

        @XmlAttribute(name = "title", required = true)
        private String m_title;

        @XmlAttribute(name = "timespan", required = true)
        private String m_timespan;

        @XmlAttribute(name = "graphtype", required = true)
        private String m_graphtype;

        @XmlAttribute(name = "resourceId", required = false)
        private String m_resourceId;

        @XmlAttribute(name = "nodeId", required = false)
        private String m_nodeId;

        @XmlAttribute(name = "nodeSource", required = false)
        private String m_nodeSource;

        @XmlAttribute(name = "domain", required = false)
        private String m_domain;

        @XmlAttribute(name = "interfaceId", required = false)
        private String m_interfaceId;

        @XmlAttribute(name = "extlink", required = false)
        private String m_extlink;

        public MetricsResource() {

        }
    }

}
