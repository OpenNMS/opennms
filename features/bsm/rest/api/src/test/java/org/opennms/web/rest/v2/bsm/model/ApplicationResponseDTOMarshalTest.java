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
import org.opennms.web.rest.v2.bsm.model.edge.ApplicationResponseDTO;

public class ApplicationResponseDTOMarshalTest extends MarshalAndUnmarshalTest<ApplicationResponseDTO> {

    public ApplicationResponseDTOMarshalTest(Class<ApplicationResponseDTO> clazz, ApplicationResponseDTO sampleObject, String sampleJson, String sampleXml) {
        super(clazz, sampleObject, sampleJson, sampleXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        ApplicationResponseDTO applicationResponseDTO = new ApplicationResponseDTO();
        applicationResponseDTO.setId(42);
        applicationResponseDTO.setApplicationName("MyApplication");
        applicationResponseDTO.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "applications", "42"));

        return Arrays.asList(new Object[][]{{
                ApplicationResponseDTO.class,
                applicationResponseDTO,
                "{" +
                        "   \"id\" : 42," +
                        "   \"location\" : \"/api/v2/business-services/applications/42\"," +
                        "   \"application-name\" : \"MyApplication\"" +
                        "}",
                "<application>\n" +
                        "   <id>42</id>\n" +
                        "   <application-name>MyApplication</application-name>\n" +
                        "   <location>/api/v2/business-services/applications/42</location>\n" +
                        "</application>"
        }});
    }
}
