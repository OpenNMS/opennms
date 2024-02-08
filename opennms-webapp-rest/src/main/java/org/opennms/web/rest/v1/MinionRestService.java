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
package org.opennms.web.rest.v1;

import java.text.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.model.OnmsMinionCollection;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("minionRestService")
@Path("minions")
@Tag(name = "Minions", description = "Minions API")
public class MinionRestService extends OnmsRestService {
    @Autowired
    private MinionDao m_minionDao;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{minionId}")
    @Transactional
    public OnmsMinion getMinion(@PathParam("minionId") final String minionId) {
        final OnmsMinion minion = m_minionDao.get(minionId);
        if (minion == null) {
            throw getException(Status.NOT_FOUND, "Minion {} was not found.", minionId);
        }
        return minion;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{minionId}/{key}")
    @Transactional
    public String getMinionProperty(@PathParam("minionId") final String minionId, @PathParam("key") final String key) {
        final OnmsMinion minion = getMinion(minionId);
        final String value = minion.getProperties().get(key);
        if (value == null) {
            throw getException(Status.NOT_FOUND, "Property {} was not found on Minion {}.", key, minionId);
        }
        return value;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount() {
        return Integer.toString(m_minionDao.countAll());
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public OnmsMinionCollection getMinions(@Context final UriInfo uriInfo) throws ParseException {
        final CriteriaBuilder builder = getCriteriaBuilder(uriInfo.getQueryParameters());
        final OnmsMinionCollection coll = new OnmsMinionCollection(m_minionDao.findMatching(builder.toCriteria()));
        coll.setTotalCount(m_minionDao.countMatching(builder.clearOrder().toCriteria()));

        return coll;
    }

    private CriteriaBuilder getCriteriaBuilder(final MultivaluedMap<String, String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMinion.class);
        //builder.alias("properties", "property", JoinType.LEFT_JOIN);
        applyQueryFilters(params, builder);
        return builder;
    }

}
