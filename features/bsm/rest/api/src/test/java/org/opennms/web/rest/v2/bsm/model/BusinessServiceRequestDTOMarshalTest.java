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
            "       }," +
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
            "       }," +
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
            "       }," +
            "   ]," +
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
            "   <reduce-function>\n" +
            "      <type>HighestSeverity</type>\n" +
            "   </reduce-function>\n" +
            "</business-service>"
        }});
    }
}
