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

public class BusinessServiceRequestDTOMarshalTest extends MarshalAndUnmarshalTest<BusinessServiceRequestDTO> {

    public BusinessServiceRequestDTOMarshalTest(Class<BusinessServiceRequestDTO> type, BusinessServiceRequestDTO sampleObject, String expectedJson, String expectedXml) {
        super(type, sampleObject, expectedJson, expectedXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        final MapFunctionDTO increaseDto = createMapFunctionDTO(MapFunctionType.Increase, null);
        final MapFunctionDTO setToDto = createMapFunctionDTO(MapFunctionType.SetTo, new String[]{"status", "Critical"});
        final BusinessServiceRequestDTO requestDTO = new BusinessServiceRequestDTO();
        requestDTO.setReduceFunction(createReduceFunctionDTO(ReduceFunctionType.MostCritical));
        requestDTO.setName("Web Servers");
        requestDTO.addAttribute("dc", "RDU");
        requestDTO.addAttribute("some-key", "some-value");
        requestDTO.addChildService(2L, increaseDto);
        requestDTO.addChildService(3L, setToDto);
        requestDTO.addReductionKey("myReductionKeyA", increaseDto);
        requestDTO.addReductionKey("myReductionKeyB", increaseDto);
        requestDTO.addIpService(1, increaseDto);

        return Arrays.asList(new Object[][]{{
            BusinessServiceRequestDTO.class,
            requestDTO,
            "{" +
            "  \"name\" : \"Web Servers\"," +
            "  \"attributes\" : {" +
            "    \"dc\" : \"RDU\"," +
            "    \"some-key\" : \"some-value\"" +
            "  }," +
            "  \"reduceFunction\" : {" +
            "       \"type\" : \"MostCritical\"," +
            "       \"properties\" : {}" +
            "  }," +
            "  \"childServices\" : [" +
            "       {" +
            "           \"mapFunction\" : {" +
            "               \"type\" : \"Increase\"," +
            "               \"properties\" : null" +
            "           }," +
            "           \"value\" : 2" +
            "       }," +
            "       {" +
            "           \"mapFunction\" : {" +
            "               \"type\" : \"SetTo\"," +
            "               \"properties\" : {" +
            "                   \"status\" : \"Critical\"" +
            "               }" +
            "           }," +
            "           \"value\" : 3" +
            "       }," +
            "   ]," +
            "  \"ipServices\" : [" +
            "       {" +
            "           \"mapFunction\" : {" +
            "               \"type\" : \"Increase\"," +
            "               \"properties\" : null" +
            "           }," +
            "           \"value\" : 1" +
            "       }," +
            "   ]," +
            "  \"reductionKeys\" : [" +
            "       {" +
            "           \"mapFunction\" : {" +
            "               \"type\" : \"Increase\"," +
            "               \"properties\" : null" +
            "           }," +
            "           \"value\" : \"myReductionKeyA\"" +
            "       }," +
            "       {" +
            "            \"mapFunction\" : {" +
            "               \"type\" : \"Increase\"," +
            "               \"properties\" : null" +
            "           }," +
            "           \"value\" : \"myReductionKeyB\"" +
            "       }," +
            "   ]," +
            "}",
            "<business-service>\n" +
            "   <name>Web Servers</name>\n" +
            "   <attributes>\n" +
            "       <attribute>\n" +
            "           <key>dc</key>\n" +
            "           <value>RDU</value>\n" +
            "       </attribute>\n" +
            "       <attribute>\n" +
            "           <key>some-key</key>\n" +
            "           <value>some-value</value>\n" +
            "       </attribute>\n" +
            "   </attributes>\n" +
            "    <ip-services-edges>\n" +
            "      <ip-service-edge>\n" +
            "         <mapFunction>\n" +
            "            <type>Increase</type>\n" +
            "         </mapFunction>\n" +
            "         <value>1</value>\n" +
            "      </ip-service-edge>\n" +
            "   </ip-services-edges>\n" +
            "   <child-edges>\n" +
            "      <child-edge>\n" +
            "         <mapFunction>\n" +
            "            <type>Increase</type>\n" +
            "         </mapFunction>\n" +
            "         <value>2</value>\n" +
            "      </child-edge>\n" +
            "      <child-edge>\n" +
            "         <mapFunction>\n" +
            "            <type>SetTo</type>\n" +
            "            <properties>\n" +
            "               <entry>\n" +
            "                  <key>status</key>\n" +
            "                  <value>Critical</value>\n" +
            "               </entry>\n" +
            "            </properties>\n" +
            "         </mapFunction>\n" +
            "         <value>3</value>\n" +
            "      </child-edge>\n" +
            "   </child-edges>\n" +
            "   <reductionkey-edges>\n" +
            "      <reductionkey-edge>\n" +
            "         <mapFunction>\n" +
            "            <type>Increase</type>\n" +
            "         </mapFunction>\n" +
            "         <value>myReductionKeyA</value>\n" +
            "      </reductionkey-edge>\n" +
            "      <reductionkey-edge>\n" +
            "         <mapFunction>\n" +
            "            <type>Increase</type>\n" +
            "         </mapFunction>\n" +
            "         <value>myReductionKeyB</value>\n" +
            "      </reductionkey-edge>\n" +
            "   </reductionkey-edges>\n" +
            "   <reduce-function>\n" +
            "      <type>MostCritical</type>\n" +
            "   </reduce-function>\n" +
            "</business-service>"
        }});
    }
}
