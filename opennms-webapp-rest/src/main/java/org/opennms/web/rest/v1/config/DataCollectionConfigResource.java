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

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
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
        Assert.isTrue(m_dataCollectionConfigDao instanceof AbstractJaxbConfigDao<?,?>);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getDataCollectionConfiguration() throws ConfigurationResourceException {
        LOG.debug("getDatacollectionConfigurationForLocation()");

        @SuppressWarnings("unchecked")
        final AbstractJaxbConfigDao<DatacollectionConfig,DatacollectionConfig> dao = (AbstractJaxbConfigDao<DatacollectionConfig,DatacollectionConfig>)m_dataCollectionConfigDao;
        final DatacollectionConfig dcc = dao.getContainer().getObject();
        if (dcc == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(dcc.toDataCollectionConfig()).build();
    }
}
