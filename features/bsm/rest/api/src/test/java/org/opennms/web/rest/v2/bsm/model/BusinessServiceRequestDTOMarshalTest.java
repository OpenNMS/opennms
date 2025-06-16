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
package org.opennms.web.rest.v2.bsm.model;

import static org.opennms.web.rest.v2.bsm.model.TestHelper.createMapFunctionDTO;
import static org.opennms.web.rest.v2.bsm.model.TestHelper.createReduceFunctionDTO;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.MarshalAndUnmarshalTest;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.map.Decrease;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;
import org.opennms.netmgt.bsm.service.model.functions.map.SetTo;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;

public class BusinessServiceRequestDTOMarshalTest extends MarshalAndUnmarshalTest<BusinessServiceRequestDTO> {

    public BusinessServiceRequestDTOMarshalTest(Class<BusinessServiceRequestDTO> type, BusinessServiceRequestDTO sampleObject, String expectedJson, String expectedXml) {
        super(type, sampleObject, expectedJson, expectedXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        final SetTo setTo = new SetTo();
        setTo.setStatus(Status.CRITICAL);

        final MapFunctionDTO increaseDto = createMapFunctionDTO(new Increase());
        final MapFunctionDTO decreaseDto = createMapFunctionDTO(new Decrease());
        final MapFunctionDTO setToDto = createMapFunctionDTO(setTo);
        final BusinessServiceRequestDTO requestDTO = new BusinessServiceRequestDTO();
        requestDTO.setReduceFunction(createReduceFunctionDTO(new HighestSeverity()));
        requestDTO.setName("Web Servers");
        requestDTO.addAttribute("dc", "RDU");
        requestDTO.addAttribute("some-key", "some-value");
        requestDTO.addChildService(2L, increaseDto, 5);
        requestDTO.addChildService(3L, setToDto, 5);
        requestDTO.addReductionKey("myReductionKeyA", increaseDto, 7, "reduction-key-a-friendly-name");
        requestDTO.addReductionKey("myReductionKeyB", increaseDto, 7, "reduction-key-b-friendly-name");
        requestDTO.addIpService(1, increaseDto, 9, "ip-service-friendly-name");
        requestDTO.addApplication(2, decreaseDto, 8);

        return Arrays.asList(new Object[][]{{
            BusinessServiceRequestDTO.class,
            requestDTO,
            "{" +
            "  \"name\" : \"Web Servers\"," +
            "  \"attributes\" : {" +
            "    \"attribute\" : [ {" +
            "      \"key\" : \"dc\"," +
            "      \"value\" : \"RDU\"" +
            "    }, {" +
            "      \"key\" : \"some-key\"," +
            "      \"value\" : \"some-value\"" +
            "    } ]" +
            "  }," +
            "  \"reduce-function\" : {" +
            "       \"type\" : \"HighestSeverity\"," +
            "       \"properties\" : { }" +
            "  }," +
            "  \"child-edges\" : [" +
            "       {" +
            "           \"map-function\" : {" +
            "               \"type\" : \"Increase\"," +
            "               \"properties\" : { }" +
            "           }," +
            "           \"weight\" : 5," +
            "           \"child-id\" : 2" +
            "       }," +
            "       {" +
            "           \"map-function\" : {" +
            "               \"type\" : \"SetTo\"," +
            "               \"properties\" : {" +
            "                   \"status\" : \"Critical\"" +
            "               }" +
            "           }," +
            "           \"weight\" : 5," +
            "           \"child-id\" : 3" +
            "       }" +
            "   ]," +
            "  \"ip-service-edges\" : [" +
            "       {" +
            "           \"map-function\" : {" +
            "               \"type\" : \"Increase\"," +
            "               \"properties\" : { }" +
            "           }," +
            "           \"weight\" : 9," +
            "           \"ip-service-id\" : 1," +
            "           \"friendly-name\" : \"ip-service-friendly-name\"" +
            "       }" +
            "   ]," +
            "  \"reduction-key-edges\" : [" +
            "       {" +
            "           \"map-function\" : {" +
            "               \"type\" : \"Increase\"," +
            "               \"properties\" : { }" +
            "           }," +
            "           \"weight\" : 7," +
            "           \"reduction-key\" : \"myReductionKeyA\"," +
            "           \"friendly-name\" : \"reduction-key-a-friendly-name\"" +
            "       }," +
            "       {" +
            "            \"map-function\" : {" +
            "               \"type\" : \"Increase\"," +
            "               \"properties\" : { }" +
            "           }," +
            "           \"weight\" : 7," +
            "           \"reduction-key\" : \"myReductionKeyB\"," +
            "           \"friendly-name\" : \"reduction-key-b-friendly-name\"" +
            "       }" +
            "   ]," +
            "  \"application-edges\" : [" +
            "       {" +
            "           \"map-function\" : {" +
            "               \"type\" : \"Decrease\"," +
            "               \"properties\" : { }" +
            "           }," +
            "           \"weight\" : 8," +
            "           \"application-id\" : 2" +
            "       }" +
            "   ]" +
            "}",
            "<business-service>\n" +
            "   <name>Web Servers</name>\n" +
            "   <attributes>\n" +
            "      <attribute>\n" +
            "         <key>dc</key>\n" +
            "         <value>RDU</value>\n" +
            "      </attribute>\n" +
            "      <attribute>\n" +
            "         <key>some-key</key>\n" +
            "         <value>some-value</value>\n" +
            "      </attribute>\n" +
            "   </attributes>\n" +
            "    <ip-service-edges>\n" +
            "      <ip-service-edge>\n" +
            "         <map-function>\n" +
            "            <type>Increase</type>\n" +
            "         </map-function>\n" +
            "         <weight>9</weight>\n" +
            "         <friendly-name>ip-service-friendly-name</friendly-name>\n" +
            "         <ip-service-id>1</ip-service-id>\n" +
            "      </ip-service-edge>\n" +
            "   </ip-service-edges>\n" +
            "   <child-edges>\n" +
            "      <child-edge>\n" +
            "         <map-function>\n" +
            "            <type>Increase</type>\n" +
            "         </map-function>\n" +
            "         <weight>5</weight>\n" +
            "         <child-id>2</child-id>\n" +
            "      </child-edge>\n" +
            "      <child-edge>\n" +
            "         <map-function>\n" +
            "            <type>SetTo</type>\n" +
            "            <properties>\n" +
            "               <entry>\n" +
            "                  <key>status</key>\n" +
            "                  <value>Critical</value>\n" +
            "               </entry>\n" +
            "            </properties>\n" +
            "         </map-function>\n" +
            "         <weight>5</weight>\n" +
            "         <child-id>3</child-id>\n" +
            "      </child-edge>\n" +
            "   </child-edges>\n" +
            "   <reduction-key-edges>\n" +
            "      <reduction-key-edge>\n" +
            "         <map-function>\n" +
            "            <type>Increase</type>\n" +
            "         </map-function>\n" +
            "         <weight>7</weight>\n" +
            "         <friendly-name>reduction-key-a-friendly-name</friendly-name>\n" +
            "         <reduction-key>myReductionKeyA</reduction-key>\n" +
            "      </reduction-key-edge>\n" +
            "      <reduction-key-edge>\n" +
            "         <map-function>\n" +
            "            <type>Increase</type>\n" +
            "         </map-function>\n" +
            "         <weight>7</weight>\n" +
            "         <friendly-name>reduction-key-b-friendly-name</friendly-name>\n" +
            "         <reduction-key>myReductionKeyB</reduction-key>\n" +
            "      </reduction-key-edge>\n" +
            "   </reduction-key-edges>\n" +
            "   <application-edges>\n" +
            "      <application-edge>\n" +
            "         <map-function>\n" +
            "            <type>Decrease</type>\n" +
            "         </map-function>\n" +
            "         <weight>8</weight>\n" +
            "         <application-id>2</application-id>\n" +
            "      </application-edge>\n" +
            "   </application-edges>\n" +
            "   <reduce-function>\n" +
            "      <type>HighestSeverity</type>\n" +
            "   </reduce-function>\n" +
            "</business-service>"
        }});
    }
}
