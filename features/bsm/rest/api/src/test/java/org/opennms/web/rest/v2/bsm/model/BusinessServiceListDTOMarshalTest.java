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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.MarshalAndUnmarshalTest;
import org.opennms.web.rest.api.ApiVersion;
import org.opennms.web.rest.api.ResourceLocation;

import com.google.common.collect.Lists;

public class BusinessServiceListDTOMarshalTest extends MarshalAndUnmarshalTest<BusinessServiceListDTO> {

    public BusinessServiceListDTOMarshalTest(Class<BusinessServiceListDTO> type, BusinessServiceListDTO sampleObject, String expectedJson, String expectedXml) {
        super(type, sampleObject, expectedJson, expectedXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        BusinessServiceListDTO listDTO = new BusinessServiceListDTO();
        listDTO.setServices(Lists.newArrayList(new ResourceLocation(ApiVersion.Version2, "business-services", "1")));

        return Arrays.asList(new Object[][]{{
            BusinessServiceListDTO.class,
            listDTO,
            "{" +
            "  \"business-services\" : [ \"/api/v2/business-services/1\" ]" +
            "}",
            "<business-services>" +
            "  <business-service>/api/v2/business-services/1</business-service>" +
            "</business-services>"
        }});
    }
}
