/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
                "   <numeric-attribute group=\"group-x\" name=\"cores\" type=\"gauge\" identifier=\"some-oid\" value=\"1\"/>\n" + 
                "</collection-resource>"
            }
        });
    }
}
