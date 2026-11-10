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
package org.opennms.features.collection.dto;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.model.ResourcePath;

public class CollectionAgentDTOTest extends XmlTestNoCastor<CollectionAgentDTO> {

    public CollectionAgentDTOTest(CollectionAgentDTO sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        CollectionAgentDTO collectionAgentDTO = new CollectionAgentDTO();
        collectionAgentDTO.setAddress(InetAddressUtils.getInetAddress("192.168.1.1"));
        collectionAgentDTO.setAttribute("k1", "v1");
        collectionAgentDTO.setStoreByForeignSource(true);
        collectionAgentDTO.setNodeId(99);
        collectionAgentDTO.setNodeLabel("switch");
        collectionAgentDTO.setForeignSource("fs");
        collectionAgentDTO.setForeignId("fid");
        collectionAgentDTO.setLocationName("HQ");
        collectionAgentDTO.setStorageResourcePath(ResourcePath.get("tmp", "foo"));
        collectionAgentDTO.setSavedSysUpTime(149);

        return Arrays.asList(new Object[][] {
            {
                collectionAgentDTO,
                "<agent address=\"192.168.1.1\" store-by-fs=\"true\" node-id=\"99\" node-label=\"switch\" foreign-source=\"fs\" foreign-id=\"fid\" location=\"HQ\" storage-resource-path=\"tmp/foo\" sys-up-time=\"149\">\n" +
                "   <attribute key=\"k1\"><![CDATA[v1]]></attribute>\n" +
                "</agent>"
            }
        });
    }
}
