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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.snmpmetadata.SnmpMetadataBase;
import org.opennms.netmgt.model.snmpmetadata.SnmpMetadataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("snmpmetadata")
@Transactional
@Tag(name = "SnmpMetadata", description = "SNMP metadata API")
public class SnmpMetadataRestService {

    /** The node DAO. */
    @Autowired
    private NodeDao nodeDao;

    @GET
    @Path("{nodeCriteria}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(summary = "Get snmpmetadata by nodeId", description = "Get snmpmetadata by nodeId", operationId = "SnmpMetadataRestServiceGETSNMPMetaDataByNodId")
    public Response getSnmpMetadata(@PathParam("nodeCriteria") String nodeCriteria) {
        final OnmsNode node = nodeDao.get(nodeCriteria);
        if (node == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        final SnmpMetadataBase snmpMetadataBase = SnmpMetadataObject.fromOnmsMetadata(node.getMetaData(), "snmp");
        return Response.ok(snmpMetadataBase).build();
    }
}
