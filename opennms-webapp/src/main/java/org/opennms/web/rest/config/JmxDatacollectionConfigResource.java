/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.config;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.JMXDataCollectionConfigDao;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Component
@PerRequest
@Scope("prototype")
public class JmxDataCollectionConfigResource implements InitializingBean {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(JmxDataCollectionConfigResource.class);

    @Resource(name = "jmxDataCollectionConfigDao")
    private JMXDataCollectionConfigDao m_jmxDataCollectionConfigDao;

    @Context
    private ResourceContext m_context;

    @Context
    private UriInfo m_uriInfo;

    public void setJmxDataCollectionConfigDao(final JMXDataCollectionConfigDao dao) {
        m_jmxDataCollectionConfigDao = dao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_jmxDataCollectionConfigDao, "JmxDataCollectionConfigDao must be set!");
        Assert.isTrue(m_jmxDataCollectionConfigDao instanceof AbstractJaxbConfigDao<?, ?>);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getJmxDataCollectionConfig() throws ConfigurationResourceException {
        LOG.debug("getJmxDataCollectionConfigurationForLocation()");

        @SuppressWarnings("unchecked")
        final JmxDatacollectionConfig jmxDataCollectionConfig = m_jmxDataCollectionConfigDao.getContainer().getObject();

        if (jmxDataCollectionConfig == null) {
            return Response.status(404).build();
        }

        return Response.ok(jmxDataCollectionConfig).build();
    }
}
