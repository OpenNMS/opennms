/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2.bsm.model;

import static org.opennms.web.rest.v2.bsm.model.TestHelper.createMapFunctionDTO;
import static org.opennms.web.rest.v2.bsm.model.TestHelper.createReduceFunctionDTO;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.MarshalAndUnmarshalTest;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.web.rest.api.ApiVersion;
import org.opennms.web.rest.api.ResourceLocation;
import org.opennms.web.rest.v2.bsm.model.edge.ChildEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeResponseDTO;

import com.google.common.collect.Sets;

public class BusinessServiceResponseDTOMarshalTest extends MarshalAndUnmarshalTest<BusinessServiceResponseDTO> {

    public BusinessServiceResponseDTOMarshalTest(Class<BusinessServiceResponseDTO> type, BusinessServiceResponseDTO sampleObject, String expectedJson, String expectedXml) {
        super(type, sampleObject, expectedJson, expectedXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        final MapFunctionDTO ignoreDto = createMapFunctionDTO(MapFunctionType.Ignore, null);
        final BusinessServiceResponseDTO bs = new BusinessServiceResponseDTO();
        bs.setId(1L);
        bs.setName("Web Servers");
        bs.addAttribute("dc", "RDU");
        bs.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "1"));
        bs.setOperationalStatus(Status.CRITICAL);
        bs.setReduceFunction(createReduceFunctionDTO(ReduceFunctionType.MostCritical));
        bs.getReductionKeys().add(createReductionKeyEdgeResponse(1L, "myReductionKeyA", ignoreDto, Status.CRITICAL, new ResourceLocation(ApiVersion.Version2, "test/1")));
        bs.getReductionKeys().add(createReductionKeyEdgeResponse(2L, "myReductionKeyB", ignoreDto, Status.NORMAL, new ResourceLocation(ApiVersion.Version2, "test/2")));
        bs.getChildren().add(createChildEdgeResponse(3L, 2L, ignoreDto, Status.MAJOR, new ResourceLocation(ApiVersion.Version2, "test/3")));
        bs.getChildren().add(createChildEdgeResponse(4L, 3L, ignoreDto, Status.MAJOR, new ResourceLocation(ApiVersion.Version2, "test/4")));
        bs.getIpServices().add(createIpServiceEdgeResponse(5L, createIpServiceResponse(), ignoreDto, Status.MINOR, new ResourceLocation(ApiVersion.Version2, "test/5")));
        bs.getParentServices().add(11L);
        bs.getParentServices().add(12L);

        return Arrays.asList(new Object[][]{{
            BusinessServiceResponseDTO.class,
            bs,
            "{" +
            "  \"location\" : \"/api/v2/business-services/1\"," +
            "  \"name\" : \"Web Servers\"," +
            "  \"id\" : 1," +
            "  \"attributes\" : {" +
            "    \"dc\" : \"RDU\"" +
            "  }," +
            "  \"operationalStatus\" : \"CRITICAL\"," +
            "  \"reduceFunction\" : {" +
            "    \"type\" : \"MostCritical\"," +
            "    \"properties\" : { }" +
            "  }," +
            "  \"reductionKeys\" : [ {" +
            "    \"id\" : 1," +
            "    \"operationalStatus\" : \"CRITICAL\"," +
            "    \"mapFunction\" : {" +
            "      \"type\" : \"Ignore\"," +
            "      \"properties\" : null" +
            "    }," +
            "    \"location\" : \"/api/v2/test/1\"," +
            "    \"reductionKeys\" : [ \"myReductionKeyA\" ]" +
            "  }, {" +
            "    \"id\" : 2," +
            "    \"operationalStatus\" : \"NORMAL\"," +
            "    \"mapFunction\" : {" +
            "      \"type\" : \"Ignore\"," +
            "      \"properties\" : null" +
            "    }," +
            "    \"location\" : \"/api/v2/test/2\"," +
            "    \"reductionKeys\" : [ \"myReductionKeyB\" ]" +
            "  } ]," +
            "  \"children\" : [ {" +
            "    \"id\" : 3," +
            "    \"operationalStatus\" : \"MAJOR\"," +
            "    \"mapFunction\" : {" +
            "      \"type\" : \"Ignore\"," +
            "      \"properties\" : null" +
            "    }," +
            "    \"location\" : \"/api/v2/test/3\"," +
            "    \"reductionKeys\" : [ ]," +
            "    \"childId\" : 2" +
            "  }, {" +
            "    \"id\" : 4," +
            "    \"operationalStatus\" : \"MAJOR\"," +
            "    \"mapFunction\" : {" +
            "      \"type\" : \"Ignore\"," +
            "      \"properties\" : null" +
            "    }," +
            "    \"location\" : \"/api/v2/test/4\"," +
            "    \"reductionKeys\" : [ ]," +
            "    \"childId\" : 3" +
            "  } ]," +
            "  \"ipServices\" : [ {" +
            "    \"id\" : 5," +
            "    \"operationalStatus\" : \"MINOR\"," +
            "    \"mapFunction\" : {" +
            "      \"type\" : \"Ignore\"," +
            "      \"properties\" : null" +
            "    }," +
            "    \"location\" : \"/api/v2/test/5\"," +
            "    \"reductionKeys\" : [ \"key1\", \"key2\" ]," +
            "    \"ipService\" : {" +
            "      \"location\" : \"/api/v2/business-services/ip-services/17\"," +
            "      \"id\" : 17," +
            "      \"nodeLabel\" : \"dummy\"," +
            "      \"serviceName\" : \"ICMP\"," +
            "      \"ipAddress\" : \"1.1.1.1\"" +
            "    }" +
            "  } ]," +
            "  \"parentServices\" : [ 11, 12 ]" +
            "}",
            "<business-service>\n" +
            "   <id>1</id>\n" +
            "   <name>Web Servers</name>\n" +
            "   <attributes>\n" +
            "      <attribute>\n" +
            "         <key>dc</key>\n" +
            "         <value>RDU</value>\n" +
            "      </attribute>\n" +
            "   </attributes>\n" +
            "   <ip-service-edges>\n" +
            "      <ip-service>\n" +
            "         <id>5</id>\n" +
            "         <operational-status>MINOR</operational-status>\n" +
            "         <map-function>\n" +
            "            <type>Ignore</type>\n" +
            "         </map-function>\n" +
            "         <location>/api/v2/test/5</location>\n" +
            "         <reductionKeys>\n" +
            "           <reductionKey>key1</reductionKey>\n" +
            "           <reductionKey>key2</reductionKey>\n" +
            "         </reductionKeys>\n" +
            "         <ip-service>\n" +
            "            <id>17</id>\n" +
            "            <service-name>ICMP</service-name>\n" +
            "            <node-label>dummy</node-label>\n" +
            "            <ip-address>1.1.1.1</ip-address>\n" +
            "            <location>/api/v2/business-services/ip-services/17</location>\n" +
            "         </ip-service>\n" +
            "      </ip-service>\n" +
            "   </ip-service-edges>\n" +
            "   <reductionKey-edges>\n" +
            "      <reductionKey>\n" +
            "         <id>1</id>\n" +
            "         <operational-status>CRITICAL</operational-status>\n" +
            "         <map-function>\n" +
            "            <type>Ignore</type>\n" +
            "         </map-function>\n" +
            "         <location>/api/v2/test/1</location>\n" +
            "         <reductionKeys>\n" +
            "            <reductionKey>myReductionKeyA</reductionKey>\n" +
            "         </reductionKeys>\n" +
            "      </reductionKey>\n" +
            "      <reductionKey>\n" +
            "         <id>2</id>\n" +
            "         <operational-status>NORMAL</operational-status>\n" +
            "         <map-function>\n" +
            "            <type>Ignore</type>\n" +
            "         </map-function>\n" +
            "         <location>/api/v2/test/2</location>\n" +
            "         <reductionKeys>\n" +
            "            <reductionKey>myReductionKeyB</reductionKey>\n" +
            "         </reductionKeys>\n" +
            "      </reductionKey>\n" +
            "   </reductionKey-edges>\n" +
            "   <child-edges>\n" +
            "      <child>\n" +
            "         <id>3</id>\n" +
            "         <operational-status>MAJOR</operational-status>\n" +
            "         <map-function>\n" +
            "            <type>Ignore</type>\n" +
            "         </map-function>\n" +
            "         <location>/api/v2/test/3</location>\n" +
            "         <reductionKeys/>\n" +
            "         <childId>2</childId>\n" +
            "      </child>\n" +
            "      <child>\n" +
            "         <id>4</id>\n" +
            "         <operational-status>MAJOR</operational-status>\n" +
            "         <map-function>\n" +
            "            <type>Ignore</type>\n" +
            "         </map-function>\n" +
            "         <location>/api/v2/test/4</location>\n" +
            "         <reductionKeys/>\n" +
            "         <childId>3</childId>\n" +
            "      </child>\n" +
            "   </child-edges>\n" +
            "   <parent-services>\n" +
            "      <parent-service>11</parent-service>\n" +
            "      <parent-service>12</parent-service>\n" +
            "   </parent-services>\n" +
            "   <reduction-function>\n" +
            "      <type>MostCritical</type>\n" +
            "   </reduction-function>\n" +
            "   <operational-status>CRITICAL</operational-status>\n" +
            "   <location>/api/v2/business-services/1</location>\n" +
            "</business-service>"
        }});
    }

    private static IpServiceResponseDTO createIpServiceResponse() {
        IpServiceResponseDTO ipService = new IpServiceResponseDTO();
        ipService.setId(17);
        ipService.setIpAddress("1.1.1.1");
        ipService.setNodeLabel("dummy");
        ipService.setServiceName("ICMP");
        ipService.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "ip-services", "17"));
        return ipService;
    }

    private static IpServiceEdgeResponseDTO createIpServiceEdgeResponse(long id, IpServiceResponseDTO ipServiceResponseDTO, MapFunctionDTO mapFunctionDTO, Status status, ResourceLocation location) {
        IpServiceEdgeResponseDTO responseDTO = new IpServiceEdgeResponseDTO();
        responseDTO.setOperationalStatus(status);
        responseDTO.setId(id);
        responseDTO.setLocation(location);
        responseDTO.getReductionKeys().add("key1");
        responseDTO.getReductionKeys().add("key2");
        responseDTO.setIpService(ipServiceResponseDTO);
        responseDTO.setMapFunction(mapFunctionDTO);
        return responseDTO;
    }

    private static ChildEdgeResponseDTO createChildEdgeResponse(long id, long childId, MapFunctionDTO mapFunctionDTO, Status status, ResourceLocation location) {
        ChildEdgeResponseDTO responseDTO = new ChildEdgeResponseDTO();
        responseDTO.setOperationalStatus(status);
        responseDTO.setId(id);
        responseDTO.setLocation(location);
        responseDTO.setChildId(childId);
        responseDTO.setMapFunction(mapFunctionDTO);
        return responseDTO;
    }

    private static ReductionKeyEdgeResponseDTO createReductionKeyEdgeResponse(long id, String reductionKey, MapFunctionDTO mapFunctionDTO, Status status, ResourceLocation location) {
        ReductionKeyEdgeResponseDTO responseDTO = new ReductionKeyEdgeResponseDTO();
        responseDTO.setOperationalStatus(status);
        responseDTO.setId(id);
        responseDTO.setLocation(location);
        responseDTO.setReductionKeys(Sets.newHashSet(reductionKey));
        responseDTO.setMapFunction(mapFunctionDTO);
        return responseDTO;
    }
}
