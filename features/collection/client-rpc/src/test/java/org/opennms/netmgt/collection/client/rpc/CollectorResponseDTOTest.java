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
package org.opennms.netmgt.collection.client.rpc;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;

public class CollectorResponseDTOTest extends XmlTestNoCastor<CollectorResponseDTO> {

    public CollectorResponseDTOTest(CollectorResponseDTO sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        CollectionAgentDTO agent = new CollectionAgentDTO();
        CollectionSet collectionSet = new CollectionSetBuilder(agent)
                .withTimestamp(new Date(0))
                .build();
        CollectorResponseDTO response = new CollectorResponseDTO(collectionSet);
        return Arrays.asList(new Object[][] {
            {
                response,
                "<collector-response>\n" + 
                "   <collection-set status=\"SUCCEEDED\" timestamp=\"" + StringUtils.iso8601OffsetString(new Date(0), ZoneId.systemDefault(), ChronoUnit.SECONDS) + "\">\n" + 
                "      <agent node-id=\"0\" sys-up-time=\"0\"/>\n" + 
                "   </collection-set>\n" + 
                "</collector-response>"
            }
        });
    }
}
