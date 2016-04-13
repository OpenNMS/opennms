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
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;
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
        final MapFunctionDTO ignoreDto = createMapFunctionDTO(new Ignore());
        final BusinessServiceResponseDTO bs = new BusinessServiceResponseDTO();
        bs.setId(1L);
        bs.setName("Web Servers");
        bs.addAttribute("dc", "RDU");
        bs.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "1"));
        bs.setOperationalStatus(Status.CRITICAL);
        bs.setReduceFunction(createReduceFunctionDTO(new HighestSeverity()));
        bs.getReductionKeys().add(createReductionKeyEdgeResponse(1L, "myReductionKeyA", ignoreDto, Status.CRITICAL, new ResourceLocation(ApiVersion.Version2, "test/1"), "reduction-key-a-friendly-name"));
        bs.getReductionKeys().add(createReductionKeyEdgeResponse(2L, "myReductionKeyB", ignoreDto, Status.NORMAL, new ResourceLocation(ApiVersion.Version2, "test/2"), "reduction-key-b-friendly-name"));
        bs.getChildren().add(createChildEdgeResponse(3L, 2L, ignoreDto, Status.MAJOR, new ResourceLocation(ApiVersion.Version2, "test/3")));
        bs.getChildren().add(createChildEdgeResponse(4L, 3L, ignoreDto, Status.MAJOR, new ResourceLocation(ApiVersion.Version2, "test/4")));
        bs.getIpServices().add(createIpServiceEdgeResponse(5L, createIpServiceResponse(), ignoreDto, Status.MINOR, new ResourceLocation(ApiVersion.Version2, "test/5"), "ip-service-friendly-name"));
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
            "    \"attribute\" : [ {" +
            "      \"key\" : \"dc\"," +
            "      \"value\" : \"RDU\"" +
            "    } ]" +
            "  }," +
            "  \"operational-status\" : \"CRITICAL\"," +
            "  \"reduce-function\" : {" +
            "    \"type\" : \"HighestSeverity\"," +
            "    \"properties\" : { }" +
            "  }," +
            "  \"reduction-key-edges\" : [ {" +
            "    \"id\" : 1," +
            "    \"operational-status\" : \"CRITICAL\"," +
            "    \"map-function\" : {" +
            "      \"type\" : \"Ignore\"," +
            "      \"properties\" : { }" +
            "    }," +
            "    \"weight\" : 9," +
            "    \"location\" : \"/api/v2/test/1\"," +
            "    \"reduction-keys\" : [ \"myReductionKeyA\" ]," +
                    "    \"friendly-name\" : \"reduction-key-a-friendly-name\"" +
            "  }, {" +
            "    \"id\" : 2," +
            "    \"operational-status\" : \"NORMAL\"," +
            "    \"map-function\" : {" +
            "      \"type\" : \"Ignore\"," +
            "      \"properties\" : { }" +
            "    }," +
            "    \"weight\" : 9," +
            "    \"location\" : \"/api/v2/test/2\"," +
            "    \"reduction-keys\" : [ \"myReductionKeyB\" ]," +
                    "    \"friendly-name\" : \"reduction-key-b-friendly-name\"" +
            "  } ]," +
            "  \"child-edges\" : [ {" +
            "    \"id\" : 3," +
            "    \"operational-status\" : \"MAJOR\"," +
            "    \"map-function\" : {" +
            "      \"type\" : \"Ignore\"," +
            "      \"properties\" : { }" +
            "    }," +
            "    \"weight\" : 7," +
            "    \"location\" : \"/api/v2/test/3\"," +
            "    \"reduction-keys\" : [ ]," +
            "    \"child-id\" : 2" +
            "  }, {" +
            "    \"id\" : 4," +
            "    \"operational-status\" : \"MAJOR\"," +
            "    \"map-function\" : {" +
            "      \"type\" : \"Ignore\"," +
            "      \"properties\" : { }" +
            "    }," +
            "    \"weight\" : 7," +
            "    \"location\" : \"/api/v2/test/4\"," +
            "    \"reduction-keys\" : [ ]," +
            "    \"child-id\" : 3" +
            "  } ]," +
            "  \"ip-service-edges\" : [ {" +
            "    \"id\" : 5," +
            "    \"operational-status\" : \"MINOR\"," +
            "    \"map-function\" : {" +
            "      \"type\" : \"Ignore\"," +
            "      \"properties\" : { }" +
            "    }," +
            "    \"weight\" : 5," +
            "    \"location\" : \"/api/v2/test/5\"," +
            "    \"reduction-keys\" : [ \"key1\", \"key2\" ]," +
            "    \"ip-service\" : {" +
            "      \"location\" : \"/api/v2/business-services/ip-services/17\"," +
            "      \"id\" : 17," +
            "      \"node-label\" : \"dummy\"," +
            "      \"service-name\" : \"ICMP\"," +
            "      \"ip-address\" : \"1.1.1.1\"" +
            "    }," +
            "    \"friendly-name\" : \"ip-service-friendly-name\"" +
            "  } ]," +
            "  \"parent-services\" : [ 11, 12 ]" +
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
            "      <ip-service-edge>\n" +
            "         <id>5</id>\n" +
            "         <operational-status>MINOR</operational-status>\n" +
            "         <map-function>\n" +
            "            <type>Ignore</type>\n" +
            "         </map-function>\n" +
            "         <location>/api/v2/test/5</location>\n" +
            "         <reduction-keys>\n" +
            "            <reduction-key>key1</reduction-key>\n" +
            "            <reduction-key>key2</reduction-key>\n" +
            "         </reduction-keys>\n" +
            "         <weight>5</weight>\n" +
            "         <ip-service>\n" +
            "            <id>17</id>\n" +
            "            <service-name>ICMP</service-name>\n" +
            "            <node-label>dummy</node-label>\n" +
            "            <ip-address>1.1.1.1</ip-address>\n" +
            "            <location>/api/v2/business-services/ip-services/17</location>\n" +
            "         </ip-service>\n" +
            "         <friendly-name>ip-service-friendly-name</friendly-name>\n" +
            "      </ip-service-edge>\n" +
            "   </ip-service-edges>\n" +
            "   <reduction-key-edges>\n" +
            "      <reduction-key-edge>\n" +
            "         <id>1</id>\n" +
            "         <operational-status>CRITICAL</operational-status>\n" +
            "         <map-function>\n" +
            "            <type>Ignore</type>\n" +
            "         </map-function>\n" +
            "         <location>/api/v2/test/1</location>\n" +
            "         <reduction-keys>\n" +
            "            <reduction-key>myReductionKeyA</reduction-key>\n" +
            "         </reduction-keys>\n" +
            "         <weight>9</weight>\n" +
            "         <friendly-name>reduction-key-a-friendly-name</friendly-name>\n" +
            "      </reduction-key-edge>\n" +
            "      <reduction-key-edge>\n" +
            "         <id>2</id>\n" +
            "         <operational-status>NORMAL</operational-status>\n" +
            "         <map-function>\n" +
            "            <type>Ignore</type>\n" +
            "         </map-function>\n" +
            "         <location>/api/v2/test/2</location>\n" +
            "         <reduction-keys>\n" +
            "            <reduction-key>myReductionKeyB</reduction-key>\n" +
            "         </reduction-keys>\n" +
            "         <weight>9</weight>\n" +
            "         <friendly-name>reduction-key-b-friendly-name</friendly-name>\n" +
            "      </reduction-key-edge>\n" +
            "   </reduction-key-edges>\n" +
            "   <child-edges>\n" +
            "      <child-edge>\n" +
            "         <id>3</id>\n" +
            "         <operational-status>MAJOR</operational-status>\n" +
            "         <map-function>\n" +
            "            <type>Ignore</type>\n" +
            "         </map-function>\n" +
            "         <location>/api/v2/test/3</location>\n" +
            "         <reduction-keys/>\n" +
            "         <weight>7</weight>\n" +
            "         <child-id>2</child-id>\n" +
            "      </child-edge>\n" +
            "      <child-edge>\n" +
            "         <id>4</id>\n" +
            "         <operational-status>MAJOR</operational-status>\n" +
            "         <map-function>\n" +
            "            <type>Ignore</type>\n" +
            "         </map-function>\n" +
            "         <location>/api/v2/test/4</location>\n" +
            "         <reduction-keys/>\n" +
            "         <weight>7</weight>\n" +
            "         <child-id>3</child-id>\n" +
            "      </child-edge>\n" +
            "   </child-edges>\n" +
            "   <parent-services>\n" +
            "      <parent-service>11</parent-service>\n" +
            "      <parent-service>12</parent-service>\n" +
            "   </parent-services>\n" +
            "   <reduce-function>\n" +
            "      <type>HighestSeverity</type>\n" +
            "   </reduce-function>\n" +
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

    private static IpServiceEdgeResponseDTO createIpServiceEdgeResponse(long id, IpServiceResponseDTO ipServiceResponseDTO, MapFunctionDTO mapFunctionDTO, Status status, ResourceLocation location, String friendlyName) {
        IpServiceEdgeResponseDTO responseDTO = new IpServiceEdgeResponseDTO();
        responseDTO.setOperationalStatus(status);
        responseDTO.setId(id);
        responseDTO.setLocation(location);
        responseDTO.getReductionKeys().add("key1");
        responseDTO.getReductionKeys().add("key2");
        responseDTO.setIpService(ipServiceResponseDTO);
        responseDTO.setWeight(5);
        responseDTO.setMapFunction(mapFunctionDTO);
        responseDTO.setFriendlyName(friendlyName);
        return responseDTO;
    }

    private static ChildEdgeResponseDTO createChildEdgeResponse(long id, long childId, MapFunctionDTO mapFunctionDTO, Status status, ResourceLocation location) {
        ChildEdgeResponseDTO responseDTO = new ChildEdgeResponseDTO();
        responseDTO.setOperationalStatus(status);
        responseDTO.setId(id);
        responseDTO.setLocation(location);
        responseDTO.setChildId(childId);
        responseDTO.setWeight(7);
        responseDTO.setMapFunction(mapFunctionDTO);
        return responseDTO;
    }

    private static ReductionKeyEdgeResponseDTO createReductionKeyEdgeResponse(long id, String reductionKey, MapFunctionDTO mapFunctionDTO, Status status, ResourceLocation location, String friendlyName) {
        ReductionKeyEdgeResponseDTO responseDTO = new ReductionKeyEdgeResponseDTO();
        responseDTO.setOperationalStatus(status);
        responseDTO.setId(id);
        responseDTO.setWeight(9);
        responseDTO.setLocation(location);
        responseDTO.setReductionKeys(Sets.newHashSet(reductionKey));
        responseDTO.setMapFunction(mapFunctionDTO);
        responseDTO.setFriendlyName(friendlyName);
        return responseDTO;
    }
}
