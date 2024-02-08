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
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.dto.CollectionResourceDTO;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.NumericAttribute;

public class CollectionResourceDTOTest extends XmlTestNoCastor<CollectionResourceDTO> {

    public CollectionResourceDTOTest(CollectionResourceDTO sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        NodeLevelResource nodeLevelResource = new NodeLevelResource(1);

        NumericAttribute attribute = new NumericAttribute("group-x", "cores", 1, AttributeType.GAUGE, "some-oid");
        CollectionResourceDTO dto = new CollectionResourceDTO();
        dto.setResource(nodeLevelResource);
        dto.getAttributes().add(attribute);

        return Arrays.asList(new Object[][] {
            {
                dto,
                "<collection-resource>\n" + 
                "   <node-level-resource node-id=\"1\"/>\n" + 
                "   <numeric-attribute group=\"group-x\" name=\"cores\" type=\"gauge\" identifier=\"some-oid\" value=\"1.0\"/>\n" +
                "</collection-resource>"
            }
        });
    }
}
