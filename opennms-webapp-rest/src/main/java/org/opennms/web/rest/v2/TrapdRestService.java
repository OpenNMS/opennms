/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.v2;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.TrapdConfigDao;
import org.opennms.web.rest.v2.api.TrapdRestApi;
import org.opennms.web.rest.v2.model.TrapdConfigPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrapdRestService implements TrapdRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(TrapdRestService.class);

    @Autowired
    private TrapdConfigDao m_trapdConfigDao;

    @Override
    public Response uploadTrapdConfiguration(final Attachment attachment, final SecurityContext securityContext) {
        if (attachment == null) {
            return Response.status(Status.BAD_REQUEST).entity("Missing uploaded file field 'upload'.").build();
        }

        final TrapdConfiguration config;
        try (InputStream inputStream = attachment.getObject(InputStream.class)) {
            config = JaxbUtils.unmarshal(TrapdConfiguration.class, inputStream);
        } catch (Exception e) {
            LOG.warn("Failed to parse uploaded trapd configuration.", e);
            return Response.status(Status.BAD_REQUEST).entity("Invalid trapd XML configuration.").build();
        }

        try {
            m_trapdConfigDao.updateConfig(config);
            return Response.ok(m_trapdConfigDao.getConfig()).build();
        } catch (ValidationException e) {
            LOG.warn("Uploaded trapd configuration failed schema validation.", e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Failed to persist uploaded trapd configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist trapd configuration.").build();
        }
    }

    @Override
    public Response getTrapdConfiguration(final SecurityContext securityContext) {
        try {
            TrapdConfiguration config = m_trapdConfigDao.getConfig();
            if (config == null) {
                return Response.status(Status.NOT_FOUND).entity("Trapd configuration not found.").build();
            }
            return Response.ok(config).build();
        } catch (Exception e) {
            LOG.error("Failed to retrieve trapd configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve trapd configuration.").build();
        }
    }

    @Override
    public Response updateTrapdConfiguration(TrapdConfigPayload config, SecurityContext securityContext) {
        if (config == null) {
            return Response.status(Status.BAD_REQUEST).entity("Missing trapd configuration in request body.").build();
        }

        final TrapdConfiguration updatedConfig;
        try {
            updatedConfig = mergeTrapdConfiguration(config);
        } catch (Exception e) {
            LOG.warn("Failed to map trapd update payload.", e);
            return Response.status(Status.BAD_REQUEST).entity("Invalid trapd configuration payload.").build();
        }

        try {
            m_trapdConfigDao.updateConfig(updatedConfig);
            return Response.ok(m_trapdConfigDao.getConfig()).build();
        } catch (ValidationException e) {
            LOG.warn("Provided trapd configuration failed schema validation.", e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Failed to persist provided trapd configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist trapd configuration.").build();
        }
    }

    private TrapdConfiguration mergeTrapdConfiguration(final TrapdConfigPayload payload) {
        TrapdConfiguration config = m_trapdConfigDao.getConfig();
        if (config == null) {
            config = new TrapdConfiguration();
        }

        if (payload.getSnmpTrapAddress() != null) {
            config.setSnmpTrapAddress(payload.getSnmpTrapAddress());
        }
        if (payload.getSnmpTrapPort() != null) {
            config.setSnmpTrapPort(payload.getSnmpTrapPort());
        }
        if (payload.getNewSuspectOnTrap() != null) {
            config.setNewSuspectOnTrap(payload.getNewSuspectOnTrap());
        }
        if (payload.getIncludeRawMessage() != null) {
            config.setIncludeRawMessage(payload.getIncludeRawMessage());
        }
        if (payload.getThreads() != null) {
            config.setThreads(payload.getThreads());
        }
        if (payload.getQueueSize() != null) {
            config.setQueueSize(payload.getQueueSize());
        }
        if (payload.getBatchSize() != null) {
            config.setBatchSize(payload.getBatchSize());
        }
        if (payload.getBatchInterval() != null) {
            config.setBatchInterval(payload.getBatchInterval());
        }
        if (payload.getUseAddressFromVarbind() != null) {
            config.setUseAddressFromVarbind(payload.getUseAddressFromVarbind());
        }
        if (payload.getSnmpv3Users() != null) {
            config.setSnmpv3User(payload.getSnmpv3Users());
        }

        // Prevent generated helper flags from being persisted as schema properties.
//        config.deleteNewSuspectOnTrap();
//        config.deleteSnmpTrapPort();
        return config;
    }

}
