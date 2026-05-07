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
package org.opennms.web.rest.v1.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class DataCollectionConfigResource implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionConfigResource.class);

    @Resource(name="dataCollectionConfigDao")
    private DataCollectionConfigDao m_dataCollectionConfigDao;

    public void setDataCollectionConfigDao(final DataCollectionConfigDao dao) {
        m_dataCollectionConfigDao = dao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_dataCollectionConfigDao, "DataCollectionConfigDao must be set!");
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getDataCollectionConfiguration() throws ConfigurationResourceException {
        LOG.debug("getDatacollectionConfigurationForLocation()");

        final DatacollectionConfig dcc = m_dataCollectionConfigDao.getRootDataCollection();
        if (dcc == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(dcc.toDataCollectionConfig()).build();
    }

    /**
     * Diagnostic endpoint: returns the MIB objects that would be collected
     * for a given sysoid, IP address, collection name, and interface type.
     *
     * Example: /rest/config/datacollection/lookup?sysoid=.1.3.6.1.4.1.8072.3.2.10&address=127.0.0.1
     */
    @GET
    @Path("/lookup")
    @Produces(MediaType.APPLICATION_JSON)
    public Response lookupMibObjects(
            @QueryParam("sysoid") final String sysoid,
            @QueryParam("address") @DefaultValue("127.0.0.1") final String address,
            @QueryParam("collection") @DefaultValue("default") final String collection,
            @QueryParam("ifType") @DefaultValue("-1") final int ifType) {

        if (sysoid == null || sysoid.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("sysoid parameter is required").build();
        }

        final List<MibObject> mibObjects = m_dataCollectionConfigDao.getMibObjectList(collection, sysoid, address, ifType);

        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("sysoid", sysoid);
        result.put("address", address);
        result.put("collection", collection);
        result.put("ifType", ifType);
        result.put("matchedObjectCount", mibObjects.size());
        result.put("objects", mibObjects.stream().map(obj -> {
            final Map<String, String> m = new LinkedHashMap<>();
            m.put("group", obj.getGroupName());
            m.put("oid", obj.getOid());
            m.put("alias", obj.getAlias());
            m.put("type", obj.getType());
            m.put("instance", obj.getInstance());
            return m;
        }).collect(Collectors.toList()));

        return Response.ok(result).build();
    }

    /**
     * Diagnostic endpoint: returns a summary of the in-memory config.
     */
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfigStatus() {
        final Map<String, Object> status = new LinkedHashMap<>();
        status.put("availableCollectionGroups", m_dataCollectionConfigDao.getAvailableDataCollectionGroups());
        status.put("availableSystemDefs", m_dataCollectionConfigDao.getAvailableSystemDefs().size());
        status.put("availableMibGroups", m_dataCollectionConfigDao.getAvailableMibGroups().size());
        status.put("configuredResourceTypes", m_dataCollectionConfigDao.getConfiguredResourceTypes().size());
        status.put("lastUpdate", m_dataCollectionConfigDao.getLastUpdate());

        final DatacollectionConfig config = m_dataCollectionConfigDao.getRootDataCollection();
        if (config != null) {
            status.put("snmpCollections", config.getSnmpCollections().stream()
                    .filter(c -> !"__resource_type_collection".equals(c.getName()))
                    .map(c -> {
                        final Map<String, Object> coll = new LinkedHashMap<>();
                        coll.put("name", c.getName());
                        coll.put("storageFlag", c.getSnmpStorageFlag());
                        coll.put("rrdStep", c.getRrd() != null ? c.getRrd().getStep() : null);
                        coll.put("groups", c.getGroups() != null ? c.getGroups().getGroups().size() : 0);
                        coll.put("systemDefs", c.getSystems() != null ? c.getSystems().getSystemDefs().size() : 0);
                        coll.put("resourceTypes", c.getResourceTypes().size());
                        return coll;
                    }).collect(Collectors.toList()));
        }

        return Response.ok(status).build();
    }
}
